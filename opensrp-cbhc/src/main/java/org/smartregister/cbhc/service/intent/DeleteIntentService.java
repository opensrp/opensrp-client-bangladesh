package org.smartregister.cbhc.service.intent;

import android.annotation.SuppressLint;
import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import android.util.Pair;

import net.sqlcipher.database.SQLiteDatabase;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.cbhc.BuildConfig;
import org.smartregister.cbhc.application.AncApplication;
import org.smartregister.cbhc.helper.ECSyncHelper;
import org.smartregister.cbhc.receiver.SyncStatusBroadcastReceiver;
import org.smartregister.cbhc.repository.AncRepository;
import org.smartregister.cbhc.sync.AncClientProcessorForJava;
import org.smartregister.cbhc.util.Constants;
import org.smartregister.cbhc.util.NetworkUtils;
import org.smartregister.domain.FetchStatus;
import org.smartregister.domain.Response;
import org.smartregister.domain.db.EventClient;
import org.smartregister.repository.AllSharedPreferences;
import org.smartregister.service.HTTPAgent;
import org.smartregister.util.Utils;

import java.util.Date;
import java.util.List;


public class DeleteIntentService extends IntentService {


    public static final String SYNC_URL = "/rest/event/client-list-to-delete";
    public static final int EVENT_PULL_LIMIT = 250;
    private static final int EVENT_PUSH_LIMIT = 50;
    private Context context;
    private HTTPAgent httpAgent;

    public DeleteIntentService() {
        super("DeleteIntentService");
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
        //sendSyncStatusBroadcastMessage(FetchStatus.fetchStarted);
        doSync();
    }

    private void doSync() {
        if (!NetworkUtils.isNetworkAvailable()) {
            complete(FetchStatus.noConnection);
            return;
        }

        try {
            pullECFromServer();

        } catch (Exception e) {
            org.smartregister.cbhc.util.Utils.appendLog(getClass().getName(), e);
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
//            lastSyncDatetime = 0;
//            String url = baseUrl + SYNC_URL + "?" + Constants.SyncFilters.FILTER_TEAM_ID + "=" + teamId + "&serverVersion=" + lastSyncDatetime + "&limit=" + SyncIntentService.EVENT_PULL_LIMIT;
//            String url = baseUrl + SYNC_URL + "?" + Constants.SyncFilters.PROVIDER_ID + "=" + providerID + "&serverVersion=" + lastSyncDatetime + "&limit=" + SyncIntentService.EVENT_PULL_LIMIT;
            String url = baseUrl + SYNC_URL + "?" + Constants.SyncFilters.LOCATION_ID + "=" + locationid + "&serverVersion=" + lastSyncDatetime + "&limit=" + SyncIntentService.EVENT_PULL_LIMIT;
//            String url = baseUrl + SYNC_URL + "?" + Constants.SyncFilters.LOCATION_ID + "=" + locationid + "&serverVersion=" + 0 + "&limit=" + SyncIntentService.EVENT_PULL_LIMIT;
            Log.i(SyncIntentService.class.getName(), "URL: " + url);

            if (httpAgent == null) {
                complete(FetchStatus.fetchedFailed);
            }

            Response resp = httpAgent.fetch(url);
            if (resp.isFailure()) {
                fetchFailed(count);
            }

            JSONArray entity_ids = new JSONArray((String) resp.payload());
            String[] ids = new String[entity_ids.length()];
            if (entity_ids != null) {
                for (int i = 0; i < entity_ids.length(); i++) {
                    String id = entity_ids.getString(i);
                    ids[i] = id;
//                    System.out.println(id);
                }
            }
            delete_from_table(ids);
//            JSONObject jsonObject = new JSONObject((String) resp.payload());

//            int eCount = fetchNumberOfEvents(jsonObject);
//            Log.i(getClass().getName(), "Parse Network Event Count: " + eCount);

//            if (eCount == 0) {
//                complete(FetchStatus.nothingFetched);
//            } else if (eCount < 0) {
//                fetchFailed(count);
//            } else if (eCount > 0) {
//                final Pair<Long, Long> serverVersionPair = getMinMaxServerVersions(jsonObject);
//                long lastServerVersion = serverVersionPair.second - 1;
//                if (eCount < EVENT_PULL_LIMIT) {
//                    lastServerVersion = serverVersionPair.second;
//                }
//
//                ecSyncUpdater.saveAllClientsAndEvents(jsonObject);
//                ecSyncUpdater.updateLastSyncTimeStamp(lastServerVersion);
//
//                processClient(serverVersionPair);
//
//                fetchRetry(0);
//            }
        } catch (Exception e) {
            org.smartregister.cbhc.util.Utils.appendLog(getClass().getName(), e);
            Log.e(getClass().getName(), "Fetch Retry Exception: " + e.getMessage(), e.getCause());
            fetchFailed(count);
        }
    }

    @SuppressLint("StaticFieldLeak")
    public void delete_from_table(final String[] ids) {
        Utils.startAsyncTask(new AsyncTask() {

            @Override
            protected void onPostExecute(Object o) {
                super.onPostExecute(o);
                removeTransferedSync();
            }

            @Override
            protected Object doInBackground(Object[] objects) {

                String[] tablename = {"ec_details", "ec_household", "ec_household_search", "ec_woman", "ec_woman_search", "ec_child", "ec_child_search", "ec_member", "ec_member_search"};
                AncRepository repo = (AncRepository) AncApplication.getInstance().getRepository();
                SQLiteDatabase db = repo.getReadableDatabase();
                if (!ArrayUtils.isEmpty(ids)) {
                    for (int i = 0; i < tablename.length; i++) {

                        String sql = "DELETE FROM " + tablename[i] + " WHERE ";
                        String condition = "";
                        for (int k = 0; k < ids.length; k++) {
                            condition = condition + " " + tablename[i] + ".base_entity_id='" + ids[k] + "' OR ";
                        }
                        if (condition.length() > 4)
                            condition = condition.substring(0, condition.length() - 4);
                        sql = sql + condition + ";";

                        if (db != null)
                            db.execSQL(sql);
                    }

                    tablename = new String[2];
                    tablename[0] = "event";
                    tablename[1] = "client";

                    for (int i = 0; i < tablename.length; i++) {

                        String sql = "DELETE FROM " + tablename[i] + " WHERE ";
                        String condition = "";
                        for (int k = 0; k < ids.length; k++) {
                            condition = condition + " " + tablename[i] + ".baseEntityId='" + ids[k] + "' OR ";
                        }
                        if (condition.length() > 4)
                            condition = condition.substring(0, condition.length() - 4);
                        sql = sql + condition + ";";

                        if (db != null)
                            db.execSQL(sql);
                    }

                }

                return null;
            }
        }, null);

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
        try {
            ECSyncHelper ecUpdater = ECSyncHelper.getInstance(context);
            List<EventClient> events = ecUpdater.allEventClients(serverVersionPair.first - 1, serverVersionPair.second);
            AncClientProcessorForJava.getInstance(context).processClient(events);
            //sendSyncStatusBroadcastMessage(FetchStatus.fetched);
        } catch (Exception e) {
            org.smartregister.cbhc.util.Utils.appendLog(getClass().getName(), e);
            Log.e(getClass().getName(), "Process Client Exception: " + e.getMessage(), e.getCause());
        }
    }

    public void removeTransferedSync() {
        org.smartregister.util.Utils.startAsyncTask((new AsyncTask() {
            @Override
            protected Object doInBackground(Object[] objects) {
                AncRepository repo = (AncRepository) AncApplication.getInstance().getRepository();
                SQLiteDatabase db = repo.getReadableDatabase();
                try {
                    String sql = "";
                    sql = "UPDATE ec_member SET date_removed = '01-01-1000' WHERE date_removed IS NULL AND base_entity_id IN (SELECT baseEntityId FROM event WHERE json LIKE '%Followup Death Status%' group by baseEntityId);";
                    db.execSQL(sql);
                    sql = "UPDATE ec_woman SET date_removed = '01-01-1000' WHERE date_removed IS NULL AND base_entity_id IN (SELECT baseEntityId FROM event WHERE json LIKE '%Followup Death Status%' group by baseEntityId);";
                    db.execSQL(sql);
                    sql = "UPDATE ec_child SET date_removed = '01-01-1000' WHERE date_removed IS NULL AND base_entity_id IN (SELECT baseEntityId FROM event WHERE json LIKE '%Followup Death Status%' group by baseEntityId);";
                    db.execSQL(sql);
                    sql = "UPDATE ec_household SET date_removed = '01-01-1000' WHERE date_removed IS NULL AND base_entity_id IN (SELECT baseEntityId FROM event WHERE json LIKE '%Followup HH Transfer%' group by baseEntityId);";
                    db.execSQL(sql);

                    sql = "UPDATE ec_member_search SET date_removed = '01-01-1000' WHERE date_removed IS NULL AND base_entity_id IN (SELECT baseEntityId FROM event WHERE json LIKE '%Followup Death Status%' group by baseEntityId);";
                    db.execSQL(sql);
                    sql = "UPDATE ec_woman_search SET date_removed = '01-01-1000' WHERE date_removed IS NULL AND base_entity_id IN (SELECT baseEntityId FROM event WHERE json LIKE '%Followup Death Status%' group by baseEntityId);";
                    db.execSQL(sql);
                    sql = "UPDATE ec_child_search SET date_removed = '01-01-1000' WHERE date_removed IS NULL AND base_entity_id IN (SELECT baseEntityId FROM event WHERE json LIKE '%Followup Death Status%' group by baseEntityId);";
                    db.execSQL(sql);
                    sql = "UPDATE ec_household_search SET date_removed = '01-01-1000' WHERE date_removed IS NULL AND base_entity_id IN (SELECT baseEntityId FROM event WHERE json LIKE '%Followup HH Transfer%' group by baseEntityId);";
                    db.execSQL(sql);
                } catch (Exception e) {
                    org.smartregister.cbhc.util.Utils.appendLog(getClass().getName(), e);

                }
                return null;
            }
        }), null);
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
            org.smartregister.cbhc.util.Utils.appendLog(getClass().getName(), e);
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
            org.smartregister.cbhc.util.Utils.appendLog(getClass().getName(), e);
            Log.e(getClass().getName(), e.getMessage(), e);
        }
        return count;
    }
}
