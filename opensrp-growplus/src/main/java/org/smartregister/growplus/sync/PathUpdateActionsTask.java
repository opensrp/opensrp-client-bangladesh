package org.smartregister.growplus.sync;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.AllConstants;
import org.smartregister.domain.DownloadStatus;
import org.smartregister.domain.FetchStatus;
import org.smartregister.domain.Response;
import org.smartregister.growthmonitoring.service.intent.ZScoreRefreshIntentService;
import org.smartregister.growplus.application.VaccinatorApplication;
import org.smartregister.growplus.receiver.SyncStatusBroadcastReceiver;
import org.smartregister.growplus.receiver.VaccinatorAlarmReceiver;
import org.smartregister.growplus.service.intent.PullUniqueIdsIntentService;
import org.smartregister.repository.AllSharedPreferences;
import org.smartregister.repository.EventClientRepository;
import org.smartregister.service.ActionService;
import org.smartregister.service.AllFormVersionSyncService;
import org.smartregister.service.HTTPAgent;
import org.smartregister.service.ImageUploadSyncService;
import org.smartregister.sync.AdditionalSyncService;
import org.smartregister.view.BackgroundAction;
import org.smartregister.view.LockingBackgroundTask;
import org.smartregister.view.ProgressIndicator;

import java.io.UnsupportedEncodingException;
import java.text.MessageFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import util.NetworkUtils;
import util.PathConstants;

import static java.text.MessageFormat.format;
import static org.smartregister.domain.FetchStatus.fetched;
import static org.smartregister.domain.FetchStatus.fetchedFailed;
import static org.smartregister.domain.FetchStatus.nothingFetched;
import static org.smartregister.util.Log.logError;
import static org.smartregister.util.Log.logInfo;

public class PathUpdateActionsTask {
    private static final String EVENTS_SYNC_PATH = "/rest/event/add";
    private static final String REPORTS_SYNC_PATH = "/rest/report/add";
    private static final String STOCK_Add_PATH = "/rest/stockresource/add/";
    private static final String STOCK_SYNC_PATH = "rest/stockresource/sync/";
    private final LockingBackgroundTask task;
    private final ActionService actionService;
    private final Context context;
    private final AllFormVersionSyncService allFormVersionSyncService;
    private AdditionalSyncService additionalSyncService;
    private PathAfterFetchListener pathAfterFetchListener;
    private final HTTPAgent httpAgent;


    public PathUpdateActionsTask(Context context, ActionService actionService, ProgressIndicator progressIndicator,
                                 AllFormVersionSyncService allFormVersionSyncService) {
        this.actionService = actionService;
        this.context = context;
        this.allFormVersionSyncService = allFormVersionSyncService;
        this.additionalSyncService = null;
        task = new LockingBackgroundTask(progressIndicator);
        this.httpAgent = VaccinatorApplication.getInstance().context().getHttpAgent();

    }

    public void setAdditionalSyncService(AdditionalSyncService additionalSyncService) {
        this.additionalSyncService = additionalSyncService;
    }

    public void updateFromServer(final PathAfterFetchListener pathAfterFetchListener) {
        this.pathAfterFetchListener = pathAfterFetchListener;

        sendSyncStatusBroadcastMessage(context, FetchStatus.fetchStarted);
        if (VaccinatorApplication.getInstance().context().IsUserLoggedOut()) {
            logInfo("Not updating from server as user is not logged in.");
            return;
        }

        task.doActionInBackground(new BackgroundAction<FetchStatus>() {
            public FetchStatus actionToDoInBackgroundThread() {
                if (NetworkUtils.isNetworkAvailable()) {
                    FetchStatus fetchStatusForForms = sync();
                    FetchStatus fetchStatusForActions = actionService.fetchNewActions();
                    pathAfterFetchListener.partialFetch(fetchStatusForActions);

                    startImageUploadIntentService(context);
                    startPullUniqueIdsIntentService(context);

                    FetchStatus fetchStatusAdditional = additionalSyncService == null ? nothingFetched : additionalSyncService.sync();

                    if (VaccinatorApplication.getInstance().context().configuration().shouldSyncForm()) {

                        allFormVersionSyncService.verifyFormsInFolder();
                        FetchStatus fetchVersionStatus = allFormVersionSyncService.pullFormDefinitionFromServer();
                        DownloadStatus downloadStatus = allFormVersionSyncService.downloadAllPendingFormFromServer();

                        if (downloadStatus == DownloadStatus.downloaded) {
                            allFormVersionSyncService.unzipAllDownloadedFormFile();
                        }

                        if (fetchVersionStatus == fetched || downloadStatus == DownloadStatus.downloaded) {
                            return fetched;
                        }
                    }

                    if (fetchStatusForActions == fetched || fetchStatusForForms == fetched || fetchStatusAdditional == fetched)
                        return fetched;

                    return fetchStatusForForms;
                }

                return FetchStatus.noConnection;
            }

            public void postExecuteInUIThread(FetchStatus result) {
                Intent intent = new Intent(context, ZScoreRefreshIntentService.class);
                context.startService(intent);
                if (result.equals(FetchStatus.nothingFetched) || result.equals(FetchStatus.fetched)) {
                    ECSyncUpdater ecSyncUpdater = ECSyncUpdater.getInstance(context);
                    ecSyncUpdater.updateLastCheckTimeStamp(Calendar.getInstance().getTimeInMillis());
                }
                pathAfterFetchListener.afterFetch(result);
                sendSyncStatusBroadcastMessage(context, result);
            }
        });
    }

    private FetchStatus sync() {
        try {
            int totalCount = 0;
            pushToServer();
            ECSyncUpdater ecUpdater = ECSyncUpdater.getInstance(context);

            // Retrieve database host from preferences
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
            AllSharedPreferences allSharedPreferences = new AllSharedPreferences(preferences);
            while (true) {
                long startSyncTimeStamp = ecUpdater.getLastSyncTimeStamp();

                int eCount = ecUpdater.fetchAllClientsAndEvents(AllConstants.SyncFilters.FILTER_PROVIDER, allSharedPreferences.fetchRegisteredANM());
                totalCount += eCount;

                if (eCount <= 0) {
                    if (eCount < 0) totalCount = eCount;
                    break;
                }

                long lastSyncTimeStamp = ecUpdater.getLastSyncTimeStamp();
                PathClientProcessor.getInstance(context).processClient(ecUpdater.allEvents(startSyncTimeStamp, lastSyncTimeStamp));
                Log.i(getClass().getName(), "!!!!! Sync count:  " + eCount);
                pathAfterFetchListener.partialFetch(fetched);
            }

            if (totalCount == 0) {
                return nothingFetched;
            } else if (totalCount < 0) {
                return fetchedFailed;
            } else {
                return fetched;
            }
        } catch (Exception e) {
            Log.e(getClass().getName(), "", e);
            return fetchedFailed;
        }

    }

    private void pushToServer() {
        pushECToServer();
        pushReportsToServer();
    }

    private void pushECToServer() {
        EventClientRepository db = VaccinatorApplication.getInstance().eventClientRepository();
        boolean keepSyncing = true;
        int limit = 50;
        try {
            // db.markAllAsUnSynced();

            while (keepSyncing) {
                Map<String, Object> pendingEvents = null;
                pendingEvents = db.getUnSyncedEvents(limit);

                if (pendingEvents.isEmpty()) {
                    return;
                }

                String baseUrl = VaccinatorApplication.getInstance().context().configuration().dristhiBaseURL();
                if (baseUrl.endsWith("/")) {
                    baseUrl = baseUrl.substring(0, baseUrl.lastIndexOf("/"));
                }
                // create request body
                JSONObject request = new JSONObject();
                if (pendingEvents.containsKey("clients")) {
                    request.put("clients", pendingEvents.get("clients"));
                }
                if (pendingEvents.containsKey("events")) {
                    request.put("events", pendingEvents.get("events"));
                }
                String jsonPayload = request.toString();
                Response<String> response = httpAgent.post(
                        format("{0}/{1}",
                                baseUrl,
                                EVENTS_SYNC_PATH),
                        jsonPayload);
                if (response.isFailure()) {
                    Log.e(getClass().getName(), "Events sync failed.");
                    return;
                }
                db.markEventsAsSynced(pendingEvents);
                Log.i(getClass().getName(), "Events synced successfully.");
            }
        } catch (Exception e) {
            Log.e(getClass().getName(), e.getMessage());
        }

    }



    private Long getHighestTimestampFromStockPayLoad(String jsonPayload) {
        Long toreturn = 0l;
        try {
            JSONObject stockContainer = new JSONObject(jsonPayload);
            if (stockContainer.has("stocks")) {
                JSONArray stockArray = stockContainer.getJSONArray("stocks");
                for (int i = 0; i < stockArray.length(); i++) {

                    JSONObject stockObject = stockArray.getJSONObject(i);
                    if (stockObject.getLong("serverVersion") > toreturn) {
                        toreturn = stockObject.getLong("serverVersion");
                    }

                }
            }
        } catch (Exception e) {
            Log.e(getClass().getCanonicalName(), e.getMessage());
        }
        return toreturn;
    }




    private void pushReportsToServer() {
        EventClientRepository db = VaccinatorApplication.getInstance().eventClientRepository();
        try {
            boolean keepSyncing = true;
            int limit = 50;
            while (keepSyncing) {
                List<JSONObject> pendingReports = null;
                pendingReports = db.getUnSyncedReports(limit);

                if (pendingReports.isEmpty()) {
                    return;
                }

                String baseUrl = VaccinatorApplication.getInstance().context().configuration().dristhiBaseURL();
                if (baseUrl.endsWith("/")) {
                    baseUrl = baseUrl.substring(0, baseUrl.lastIndexOf("/"));
                }
                // create request body
                JSONObject request = new JSONObject();

                request.put("reports", pendingReports);
                String jsonPayload = request.toString();
                Log.v("SYNC","jsonPayload:"+jsonPayload);
                Response<String> response = httpAgent.post(
                        MessageFormat.format("{0}/{1}",
                                baseUrl,
                                REPORTS_SYNC_PATH),
                        jsonPayload);
                if (response.isFailure()) {
                    Log.e(getClass().getName(), "Reports sync failed.");
                    return;
                }
                db.markReportsAsSynced(pendingReports);
                Log.i(getClass().getName(), "Reports synced successfully.");
            }
        } catch (Exception e) {
            Log.e(getClass().getName(), e.getMessage());
        }
    }

    private void startImageUploadIntentService(Context context) {
        Intent intent = new Intent(context, ImageUploadSyncService.class);
        context.startService(intent);
    }

    private void startPullUniqueIdsIntentService(Context context) {
        Intent intent = new Intent(context, PullUniqueIdsIntentService.class);
        context.startService(intent);
    }


    private void sendSyncStatusBroadcastMessage(Context context, FetchStatus fetchStatus) {
        Intent intent = new Intent();
        intent.setAction(SyncStatusBroadcastReceiver.ACTION_SYNC_STATUS);
        intent.putExtra(SyncStatusBroadcastReceiver.EXTRA_FETCH_STATUS, fetchStatus);
        context.sendBroadcast(intent);
    }

    public static void setAlarms(Context context) {
        VaccinatorAlarmReceiver.setAlarm(context, 2, PathConstants.ServiceType.DAILY_TALLIES_GENERATION);
        VaccinatorAlarmReceiver.setAlarm(context, 2, PathConstants.ServiceType.WEIGHT_SYNC_PROCESSING);
        VaccinatorAlarmReceiver.setAlarm(context, 2, PathConstants.ServiceType.VACCINE_SYNC_PROCESSING);
        VaccinatorAlarmReceiver.setAlarm(context, 2, PathConstants.ServiceType.RECURRING_SERVICES_SYNC_PROCESSING);
    }

}
