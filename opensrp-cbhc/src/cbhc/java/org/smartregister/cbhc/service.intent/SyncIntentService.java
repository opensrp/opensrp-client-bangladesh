package org.smartregister.cbhc.service.intent;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.util.Pair;

import net.sqlcipher.database.SQLiteDatabase;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.AllConstants;
import org.smartregister.CoreLibrary;
import org.smartregister.cbhc.BuildConfig;
import org.smartregister.cbhc.R;
import org.smartregister.cbhc.application.AncApplication;
import org.smartregister.cbhc.helper.ECSyncHelper;
import org.smartregister.cbhc.job.DataDeleteJob;
import org.smartregister.cbhc.job.DeleteIntentServiceJob;
import org.smartregister.cbhc.job.PullHealthIdsServiceJob;
import org.smartregister.cbhc.job.PullUniqueIdsServiceJob;
import org.smartregister.cbhc.receiver.SyncStatusBroadcastReceiver;
import org.smartregister.cbhc.repository.AncRepository;
import org.smartregister.cbhc.sync.AncClientProcessorForJava;
import org.smartregister.cbhc.util.Constants;
import org.smartregister.cbhc.util.NetworkUtils;
import org.smartregister.cbhc.util.Utils;
import org.smartregister.domain.FetchStatus;
import org.smartregister.domain.Response;
import org.smartregister.domain.db.EventClient;
import org.smartregister.repository.AllSharedPreferences;
import org.smartregister.repository.EventClientRepository;
import org.smartregister.service.HTTPAgent;

import java.text.MessageFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import timber.log.Timber;

public class SyncIntentService extends IntentService {
    public static final String SYNC_URL = "/rest/event/sync";
    public static final int EVENT_PULL_LIMIT = 250;
    private static final String ADD_URL = "/rest/event/add";
    private static final int EVENT_PUSH_LIMIT = 50;
    private Context context;
    private HTTPAgent httpAgent;
    protected boolean isEmptyToAdd = true;

    public SyncIntentService() {
        super("SyncIntentService");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        context = getBaseContext();
        httpAgent = AncApplication.getInstance().getContext().getHttpAgent();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        handleSync();
    }

    protected void handleSync() {
        sendSyncStatusBroadcastMessage(FetchStatus.fetchStarted);
        long start_time = new Date().getTime();
        Log.e("sync-time", "start-time:"+start_time);

        doSync();
        long end_time = new Date().getTime();
        Log.e("sync-time:", "end-time:"+end_time);
        Log.e("sync-time:", "time-difference:"+(end_time-start_time));

    }

    private void doSync() {
        if (!NetworkUtils.isNetworkAvailable()) {
            complete(FetchStatus.noConnection);
            return;
        }

        try {
            pushToServer();
            pullECFromServer();

        } catch (Exception e) {
            Utils.appendLog(getClass().getName(), e);
            Log.e(getClass().getName(), e.getMessage(), e);
            complete(FetchStatus.fetchedFailed);
        }
    }

    private void pullECFromServer() {
        fetchRetry(0);
    }

    private synchronized void fetchRetry(final int count) {

        try {
            // Fetch team
            AllSharedPreferences sharedPreferences = AncApplication.getInstance().getContext().userService().getAllSharedPreferences();
            String teamId = sharedPreferences.fetchDefaultTeamId(sharedPreferences.fetchRegisteredANM());
            String providerID = sharedPreferences.fetchRegisteredANM();
            String locationid = AncApplication.getInstance().getContext().userService().getAllSharedPreferences().fetchDefaultLocalityId(providerID);
//            String locationid =LocationHelper.getInstance().getDefaultLocation();
            if (StringUtils.isBlank(teamId)) {
                complete(FetchStatus.fetchedFailed);
                return;
            }

            final ECSyncHelper ecSyncUpdater = ECSyncHelper.getInstance(context);
            String baseUrl = AncApplication.getInstance().getContext().
                    configuration().dristhiBaseURL();
            if (baseUrl.endsWith("/")) {
                baseUrl = baseUrl.substring(0, baseUrl.lastIndexOf("/"));
            }

            long lastSyncDatetime = ecSyncUpdater.getLastSyncTimeStamp();
            Log.v("REQUEST_URL", "LAST SYNC DT :" + lastSyncDatetime);

            String url = baseUrl + SYNC_URL + "?" + Constants.SyncFilters.LOCATION_ID + "=" + locationid + "&serverVersion=" + lastSyncDatetime + "&limit=" + SyncIntentService.EVENT_PULL_LIMIT+"&isEmptyToAdd="+isEmptyToAdd;

            Log.v(getClass().getName(), "URL: " + url);

            if (httpAgent == null) {
                complete(FetchStatus.fetchedFailed);
            }

            Response resp = httpAgent.fetch(url);

            if (resp.isFailure()) {
                fetchFailed(count);
                return;
            }

            JSONObject jsonObject = new JSONObject((String) resp.payload());

            int eCount = fetchNumberOfEvents(jsonObject);
            Log.v(getClass().getName(), "Parse Network Event Count: " + eCount);

            if (eCount == 0) {
                complete(FetchStatus.nothingFetched);
//                new DetailsStatusUpdate(jsonObject, eCount).start();
            } else if (eCount < 0) {
                fetchFailed(count);
            } else {
                final Pair<Long, Long> serverVersionPair = getMinMaxServerVersions(jsonObject);
                long lastServerVersion = serverVersionPair.second - 1;
                if (eCount < EVENT_PULL_LIMIT) {
                    lastServerVersion = serverVersionPair.second;
                }

                //new ClientInsertThread(ecSyncUpdater,jsonObject,lastServerVersion).start();
                // long start  = System.currentTimeMillis();
                if (ecSyncUpdater.saveAllClientsAndEvents(jsonObject)) {
                    processClient(serverVersionPair);
                    ecSyncUpdater.updateLastSyncTimeStamp(lastServerVersion);
                }
                // long end = System.currentTimeMillis();
                //  long diff = end - start;
                //System.out.println(diff);
//                new DetailsStatusUpdate(jsonObject, eCount).start();
                fetchRetry(0);
            }
        } catch (Exception e) {
            Utils.appendLog(getClass().getName(), e);
            Log.e(getClass().getName(), "Fetch Retry Exception: " + e.getMessage(), e.getCause());
            fetchFailed(count);
        } finally {
            DeleteIntentServiceJob.scheduleJobImmediately(DeleteIntentServiceJob.TAG);
            PullHealthIdsServiceJob.scheduleJobImmediately(PullHealthIdsServiceJob.TAG);
            PullUniqueIdsServiceJob.scheduleJobImmediately(PullUniqueIdsServiceJob.TAG);
            DataDeleteJob.scheduleJobImmediately(DataDeleteJob.TAG);

        }
    }

    public void fetchFailed(int count) {
        if (count < BuildConfig.MAX_SYNC_RETRIES) {
            int newCount = count + 1;
            fetchRetry(newCount);
        } else {
            complete(FetchStatus.fetchedFailed);
        }
    }

    private void processClient(Pair<Long, Long> serverVersionPair) {
//        new ClientEventThread(serverVersionPair).start();
        try {
            ECSyncHelper ecUpdater = ECSyncHelper.getInstance(context);
            List<EventClient> events = ecUpdater.allEventClients(serverVersionPair.first - 1, serverVersionPair.second);
            AncClientProcessorForJava.getInstance(context).processClient(events);
            sendSyncStatusBroadcastMessage(FetchStatus.fetched);
        } catch (Exception e) {
            Utils.appendLog(getClass().getName(), e);
            Log.v(getClass().getName(), "Process Client Exception: " + e.getMessage(), e.getCause());
        }
    }

    // PUSH TO SERVER
    private void pushToServer() {
        isEmptyToAdd = true;
        pushECToServer();
    }
    protected void pushECToServer() {
        EventClientRepository db = CoreLibrary.getInstance().context().getEventClientRepository();
        boolean keepSyncing = true;
        isEmptyToAdd = true;

        while (keepSyncing) {
            try {
                Map<String, Object> pendingEvents = db.getUnSyncedEventsClients(EVENT_PUSH_LIMIT);

                if (pendingEvents.isEmpty()) {
                    return;
                }

                String baseUrl = CoreLibrary.getInstance().context().configuration().dristhiBaseURL();
                if (baseUrl.endsWith(context.getString(org.smartregister.R.string.url_separator))) {
                    baseUrl = baseUrl.substring(0, baseUrl.lastIndexOf(context.getString(org.smartregister.R.string.url_separator)));
                }
                // create request body
                JSONObject request = new JSONObject();
                if (pendingEvents.containsKey(AllConstants.KEY.CLIENTS)) {
                    request.put(AllConstants.KEY.CLIENTS, pendingEvents.get(AllConstants.KEY.CLIENTS));
                }
                if (pendingEvents.containsKey(AllConstants.KEY.EVENTS)) {
                    request.put(AllConstants.KEY.EVENTS, pendingEvents.get(AllConstants.KEY.EVENTS));
                }
                isEmptyToAdd = false;
                String jsonPayload = request.toString();
                String add_url =  MessageFormat.format("{0}/{1}",
                        baseUrl,
                        ADD_URL);
                Response<String> response = httpAgent.post(add_url
                        ,
                        jsonPayload);
                if (response.isFailure()) {
                    return;
                }
                db.markEventsAsSynced(pendingEvents);
            } catch (Exception e) {
                Timber.e(e);
                return;

            }
        }
    }
    private void sendSyncStatusBroadcastMessage(FetchStatus fetchStatus) {
        Intent intent = new Intent();
        intent.setAction(SyncStatusBroadcastReceiver.ACTION_SYNC_STATUS);
        intent.putExtra(SyncStatusBroadcastReceiver.EXTRA_FETCH_STATUS, fetchStatus);
        sendBroadcast(intent);
    }

    private void complete(FetchStatus fetchStatus) {
        Intent intent = new Intent();
        intent.setAction(SyncStatusBroadcastReceiver.ACTION_SYNC_STATUS);
        intent.putExtra(SyncStatusBroadcastReceiver.EXTRA_FETCH_STATUS, fetchStatus);
        intent.putExtra(SyncStatusBroadcastReceiver.EXTRA_COMPLETE_STATUS, true);

        sendBroadcast(intent);

        ECSyncHelper ecSyncUpdater = ECSyncHelper.getInstance(context);
        ecSyncUpdater.updateLastCheckTimeStamp(new Date().getTime());
        DeleteIntentServiceJob.scheduleJobImmediately(DeleteIntentServiceJob.TAG);
    }

    private Pair<Long, Long> getMinMaxServerVersions(JSONObject jsonObject) {
        final String EVENTS = "events";
        final String SERVER_VERSION = "serverVersion";
        try {
            if (jsonObject != null && jsonObject.has(EVENTS)) {
                JSONArray events = jsonObject.getJSONArray(EVENTS);

                long maxServerVersion = Long.MIN_VALUE;
                long minServerVersion = Long.MAX_VALUE;

                for (int i = 0; i < events.length(); i++) {
                    Object o = events.get(i);
                    if (o instanceof JSONObject) {
                        JSONObject jo = (JSONObject) o;
                        if (jo.has(SERVER_VERSION)) {
                            long serverVersion = jo.getLong(SERVER_VERSION);
                            if (serverVersion > maxServerVersion) {
                                maxServerVersion = serverVersion;
                            }

                            if (serverVersion < minServerVersion) {
                                minServerVersion = serverVersion;
                            }
                        }
                    }
                }
                return Pair.create(minServerVersion, maxServerVersion);
            }
        } catch (Exception e) {
            Utils.appendLog(getClass().getName(), e);
            Log.v(getClass().getName(), e.getMessage(), e);
        }
        return Pair.create(0L, 0L);
    }

    private int fetchNumberOfEvents(JSONObject jsonObject) {
        int count = -1;
        final String NO_OF_EVENTS = "no_of_events";
        try {
            if (jsonObject != null && jsonObject.has(NO_OF_EVENTS)) {
                count = jsonObject.getInt(NO_OF_EVENTS);
            }
        } catch (JSONException e) {
            Utils.appendLog(getClass().getName(), e);
            Log.e(getClass().getName(), e.getMessage(), e);
        }
        return count;
    }

    class DetailsStatusUpdate extends Thread {

        JSONObject obj;
        int eCount = 0;

        public DetailsStatusUpdate(JSONObject obj, int eCount) {
            DetailsStatusUpdate.this.obj = obj;
            DetailsStatusUpdate.this.eCount = eCount;
        }

        @Override
        public void run() {

            AncRepository repo = (AncRepository) AncApplication.getInstance().getRepository();
            SQLiteDatabase db = repo.getReadableDatabase();
            try {
                if (obj != null && obj.has("clients")) {
                    String[] tablenames = {"ec_household", "ec_woman", "ec_child", "ec_member"};
//                    for (String table : tablenames) {
//                        String setDefaultQuery = "update " + table + " set dataApprovalStatus = '1' where dataApprovalStatus!='1'";
//                        db.execSQL(setDefaultQuery);
//                    }
                    JSONArray clients = obj.getJSONArray("clients");

                    String rejected_ids = "";
                    if (clients != null && clients.length() != 0) {
                        for (int i = 0; i < clients.length(); i++) {
                            JSONObject clientObject = clients.getJSONObject(i);
                            if (clientObject != null && clientObject.has("dataApprovalStatus")) {
                                String dataApprovalStatus = clientObject.getString("dataApprovalStatus");
                                String dataApprovalComments = clientObject.getString("dataApprovalComments");
                                String baseEntityId = clientObject.getString("baseEntityId");
                                if ("0".equals(dataApprovalStatus)) {
                                    String[] tablename = {"ec_woman", "ec_child", "ec_member"};
                                    for (String table : tablename) {
                                        String update = "update " + table + " set " +
                                                "dataApprovalStatus = '0', dataApprovalComments = '" + dataApprovalComments + "' " +
                                                "where " + table + ".base_entity_id = '" + baseEntityId + "'";
                                        db.execSQL(update);

                                    }
//                                    rejected_ids += "'" + baseEntityId + "',";
                                }

                            }

                        }
                    }

                }
            } catch (Exception e) {
                Utils.appendLog(getClass().getName(), e);
                e.printStackTrace();
            }

        }
    }

    class ClientInsertThread extends Thread {
        ECSyncHelper ecSyncUpdater;
        JSONObject jsonObject;
        long lastServerVersion;

        public ClientInsertThread(ECSyncHelper ecSyncUpdater, JSONObject jsonObject, long lastServerVersion) {
            this.ecSyncUpdater = ecSyncUpdater;
            this.jsonObject = jsonObject;
            this.lastServerVersion = lastServerVersion;
        }

        @Override
        public void run() {
            ecSyncUpdater.saveAllClientsAndEvents(jsonObject);
            ecSyncUpdater.updateLastSyncTimeStamp(lastServerVersion);
        }

    }

    class ClientEventThread extends Thread {
        Pair<Long, Long> serverVersionPair;

        public ClientEventThread(Pair<Long, Long> serverVersionPair) {
            this.serverVersionPair = serverVersionPair;
        }

        @Override
        public void run() {
            try {
                ECSyncHelper ecUpdater = ECSyncHelper.getInstance(context);
                List<EventClient> events = ecUpdater.allEventClients(serverVersionPair.first - 1, serverVersionPair.second);
                AncClientProcessorForJava.getInstance(context).processClient(events);
                sendSyncStatusBroadcastMessage(FetchStatus.fetched);
            } catch (Exception e) {
                Utils.appendLog(getClass().getName(), e);
                Log.e(getClass().getName(), "Process Client Exception: " + e.getMessage(), e.getCause());
            }
        }
    }

}
