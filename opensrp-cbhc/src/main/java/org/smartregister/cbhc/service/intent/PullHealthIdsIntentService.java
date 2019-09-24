package org.smartregister.cbhc.service.intent;


import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;
import org.smartregister.cbhc.application.AncApplication;
import org.smartregister.cbhc.exception.PullUniqueIdsException;
import org.smartregister.cbhc.repository.HealthIdRepository;
import org.smartregister.cbhc.util.Constants;
import org.smartregister.cbhc.util.Utils;
import org.smartregister.domain.Response;
import org.smartregister.service.HTTPAgent;

import java.util.ArrayList;
import java.util.List;

public class PullHealthIdsIntentService extends IntentService {
    public static final String ID_URL = "/uniqueids/get";
    public static final String IDENTIFIERS = "identifiers";
    private static final String TAG = PullHealthIdsIntentService.class.getCanonicalName();
    private HealthIdRepository healthIdRepo;
//    @Override
//    public void onCreate() {
//        super.onCreate();
//        startForeground(1,new Notification());
//    }


    public PullHealthIdsIntentService() {
        super("PullHealthIdsIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        try {
            int numberToGenerate;
            if (healthIdRepo.countUnUsedIds() <= 10) { // first time pull no ids at all
//                numberToGenerate = Constants.OPENMRS_UNIQUE_ID_INITIAL_BATCH_SIZE;
                numberToGenerate = Constants.OPENMRS_UNIQUE_ID_BATCH_SIZE;
            }
//            else if (healthIdRepo.countUnUsedIds() <= 250) { //maintain a minimum of 250 else skip this pull
//                numberToGenerate = Constants.OPENMRS_UNIQUE_ID_BATCH_SIZE;
//            }
            else {
                return;
            }
            JSONObject ids = fetchOpenMRSIds(Constants.OPENMRS_UNIQUE_ID_SOURCE, numberToGenerate);
            if (ids != null && ids.has(IDENTIFIERS)) {
                parseResponse(ids);
            }
        } catch (Exception e) {
            Utils.appendLog(getClass().getName(), e);
            Log.e(TAG, e.getMessage(), e);
        }
    }

    private JSONObject fetchOpenMRSIds(int source, int numberToGenerate) throws Exception {
        HTTPAgent httpAgent = AncApplication.getInstance().getContext().getHttpAgent();
        String baseUrl = AncApplication.getInstance().getContext().
                configuration().dristhiBaseURL();
        String endString = "/";
        if (baseUrl.endsWith(endString)) {
            baseUrl = baseUrl.substring(0, baseUrl.lastIndexOf(endString));
        }

        String url = baseUrl + ID_URL + "/health-id";
        Log.i(PullUniqueIdsIntentService.class.getName(), "URL: " + url);

        if (httpAgent == null) {
            throw new PullUniqueIdsException(ID_URL + " http agent is null");
        }

        Response resp = httpAgent.fetch(url);
        if (resp.isFailure()) {
            throw new PullUniqueIdsException(ID_URL + " not returned data");
        }

        return new JSONObject((String) resp.payload());
    }

    private void parseResponse(JSONObject idsFromOMRS) throws Exception {
        JSONArray jsonArray = idsFromOMRS.getJSONArray(IDENTIFIERS);
        if (jsonArray != null && jsonArray.length() > 0) {
            List<String> ids = new ArrayList<>();
            for (int i = 0; i < jsonArray.length(); i++) {
                ids.add(jsonArray.getString(i));
            }
            healthIdRepo.bulkInserOpenmrsIds(ids);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        healthIdRepo = AncApplication.getInstance().getHealthIdRepository();
        return super.onStartCommand(intent, flags, startId);
    }
}
