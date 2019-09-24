package org.smartregister.cbhc.service.intent;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.util.Pair;

import net.sqlcipher.database.SQLiteDatabase;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.CoreLibrary;
import org.smartregister.cbhc.BuildConfig;
import org.smartregister.cbhc.R;
import org.smartregister.cbhc.application.AncApplication;
import org.smartregister.cbhc.helper.ECSyncHelper;
import org.smartregister.cbhc.helper.LocationHelper;
import org.smartregister.cbhc.job.DeleteIntentServiceJob;
import org.smartregister.cbhc.job.ImageUploadServiceJob;
import org.smartregister.cbhc.job.PullHealthIdsServiceJob;
import org.smartregister.cbhc.job.PullUniqueIdsServiceJob;
import org.smartregister.cbhc.receiver.SyncStatusBroadcastReceiver;
import org.smartregister.cbhc.repository.AncRepository;
import org.smartregister.cbhc.sync.AncClientProcessorForJava;
import org.smartregister.cbhc.util.Constants;
import org.smartregister.cbhc.util.NetworkUtils;
import org.smartregister.domain.FetchStatus;
import org.smartregister.domain.Response;
import org.smartregister.domain.db.Client;
import org.smartregister.domain.db.EventClient;
import org.smartregister.repository.AllSharedPreferences;
import org.smartregister.repository.EventClientRepository;
import org.smartregister.service.HTTPAgent;

import java.text.MessageFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class SyncIntentService extends IntentService {
    private static final String ADD_URL = "/rest/event/add";
    public static final String SYNC_URL = "/rest/event/sync";

    private Context context;
    private HTTPAgent httpAgent;

    public static final int EVENT_PULL_LIMIT = 250;
    private static final int EVENT_PUSH_LIMIT = 50;

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

        doSync();
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
Utils.appendLog(getClass().getName(),e);
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

            Long lastSyncDatetime = ecSyncUpdater.getLastSyncTimeStamp();
            Log.i(SyncIntentService.class.getName(), "LAST SYNC DT :" + new DateTime(lastSyncDatetime));
            lastSyncDatetime = lastSyncDatetime;
//            String url = baseUrl + SYNC_URL + "?" + Constants.SyncFilters.FILTER_TEAM_ID + "=" + teamId + "&serverVersion=" + lastSyncDatetime + "&limit=" + SyncIntentService.EVENT_PULL_LIMIT;
//            String url = baseUrl + SYNC_URL + "?" + Constants.SyncFilters.PROVIDER_ID + "=" + providerID + "&serverVersion=" + lastSyncDatetime + "&limit=" + SyncIntentService.EVENT_PULL_LIMIT;
            String url = baseUrl + SYNC_URL + "?" + Constants.SyncFilters.LOCATION_ID + "=" + locationid + "&serverVersion=" + lastSyncDatetime + "&limit=" + SyncIntentService.EVENT_PULL_LIMIT;

            Log.i(SyncIntentService.class.getName(), "URL: " + url);

            if (httpAgent == null) {
                complete(FetchStatus.fetchedFailed);
            }

            Response resp = httpAgent.fetch(url);
            if (resp.isFailure()) {
                fetchFailed(count);
            }

            JSONObject jsonObject = new JSONObject((String) resp.payload());

            int eCount = fetchNumberOfEvents(jsonObject);
            Log.i(getClass().getName(), "Parse Network Event Count: " + eCount);

            if (eCount == 0) {
                complete(FetchStatus.nothingFetched);
            } else if (eCount < 0) {
                fetchFailed(count);
            } else if (eCount > 0) {
                final Pair<Long, Long> serverVersionPair = getMinMaxServerVersions(jsonObject);
                long lastServerVersion = serverVersionPair.second - 1;
                if (eCount < EVENT_PULL_LIMIT) {
                    lastServerVersion = serverVersionPair.second;
                }

//                new ClientInsertThread(ecSyncUpdater,jsonObject,lastServerVersion).start();
                // long start  = System.currentTimeMillis();
                ecSyncUpdater.saveAllClientsAndEvents(jsonObject);
                ecSyncUpdater.updateLastSyncTimeStamp(lastServerVersion);
                processClient(serverVersionPair);
                // long end = System.currentTimeMillis();
                //  long diff = end - start;
                //System.out.println(diff);

                fetchRetry(0);
            }
        } catch (Exception e) {
Utils.appendLog(getClass().getName(),e);
            Log.e(getClass().getName(), "Fetch Retry Exception: " + e.getMessage(), e.getCause());
            fetchFailed(count);
        } finally {
            DeleteIntentServiceJob.scheduleJobImmediately(DeleteIntentServiceJob.TAG);
            PullHealthIdsServiceJob.scheduleJobImmediately(PullHealthIdsServiceJob.TAG);
            PullUniqueIdsServiceJob.scheduleJobImmediately(PullUniqueIdsServiceJob.TAG);

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
Utils.appendLog(getClass().getName(),e);
            Log.e(getClass().getName(), "Process Client Exception: " + e.getMessage(), e.getCause());
        }
    }

    // PUSH TO SERVER
    private void pushToServer() {
        pushECToServer();
    }

    private void pushECToServer() {
        EventClientRepository db = AncApplication.getInstance().getEventClientRepository();
        boolean keepSyncing = true;

        while (keepSyncing) {
            try {
                Map<String, Object> pendingEvents = db.getUnSyncedEvents(EVENT_PUSH_LIMIT);

                if (pendingEvents.isEmpty()) {
                    return;
                }

                String baseUrl = AncApplication.getInstance().getContext().configuration().dristhiBaseURL();
                if (baseUrl.endsWith(context.getString(R.string.url_separator))) {
                    baseUrl = baseUrl.substring(0, baseUrl.lastIndexOf(context.getString(R.string.url_separator)));
                }
                // create request body
                JSONObject request = new JSONObject();
                if (pendingEvents.containsKey(context.getString(R.string.clients_key))) {
                    request.put(context.getString(R.string.clients_key), pendingEvents.get(context.getString(R.string.clients_key)));
                }
                if (pendingEvents.containsKey(context.getString(R.string.events_key))) {
                    request.put(context.getString(R.string.events_key), pendingEvents.get(context.getString(R.string.events_key)));
                }
                String jsonPayload = request.toString();
                Response<String> response = httpAgent.post(
                        MessageFormat.format("{0}/{1}",
                                baseUrl,
                                ADD_URL),
                        jsonPayload);
                if (response.isFailure()) {
                    Log.e(getClass().getName(), "Events sync failed.");
                    return;
                }
                db.markEventsAsSynced(pendingEvents);
                Log.i(getClass().getName(), "Events synced successfully.");
            } catch (Exception e) {
Utils.appendLog(getClass().getName(),e);
                Log.e(getClass().getName(), e.getMessage(), e);
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
Utils.appendLog(getClass().getName(),e);
            Log.e(getClass().getName(), e.getMessage(), e);
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
Utils.appendLog(getClass().getName(),e);
            Log.e(getClass().getName(), e.getMessage(), e);
        }
        return count;
    }

}
