package org.smartregister.cbhc.notification;

import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;

import org.apache.http.NoHttpResponseException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.CoreLibrary;
import org.smartregister.cbhc.application.AncApplication;
import org.smartregister.cbhc.util.Constants;
import org.smartregister.service.HTTPAgent;

public class NotificationIntentService extends IntentService {
    private static final String NOTIFICATION_URL = "/message?";
    private static final String LAST_NOTIFICATION_TIME = "last_notification_sync";
    public NotificationIntentService(){
        super("NotificationIntentService");
    }

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public NotificationIntentService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        long timestamp = 0;
        JSONArray jsonArray = getNotificaionList();
        if(jsonArray == null) return;
        StringBuilder nameCount = new StringBuilder();
        Log.v("NOTIFICATION_FETCH",jsonArray+"");
        for(int i=0;i<jsonArray.length();i++){
            try {
                JSONObject object = jsonArray.getJSONObject(i);
                NotificationDTO notification =  new Gson().fromJson(object.toString(), NotificationDTO.class);
                if(notification != null){
                    AncApplication.getNotificationRepository().addOrUpdate(notification);
                    timestamp = notification.getTimestamp();
                    Log.v("NOTIFICATION_FETCH","lasttime:"+timestamp);
                    nameCount.append(notification.getText()+"\n");
                    nameCount.append("-----------------------------------------------"+"\n");
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        if(jsonArray.length()>0){
            if(nameCount.length()>0){
                intent = new Intent(Constants.ACTION_NOTIFICATION_COME);
                intent.putExtra(Constants.EXTRA_NOTIFICATION_COME, nameCount.toString());
                sendBroadcast(intent);

            }
            CoreLibrary.getInstance().context().allSharedPreferences().savePreference(LAST_NOTIFICATION_TIME,timestamp+"");
        }

    }
    private JSONArray getNotificaionList(){
        try{
            HTTPAgent httpAgent = CoreLibrary.getInstance().context().getHttpAgent();
            String baseUrl = CoreLibrary.getInstance().context().
                    configuration().dristhiBaseURL();
            String endString = "/";
            if (baseUrl.endsWith(endString)) {
                baseUrl = baseUrl.substring(0, baseUrl.lastIndexOf(endString));
            }
            String userName = CoreLibrary.getInstance().context().allSharedPreferences().fetchRegisteredANM();
            if(TextUtils.isEmpty(userName)){
                return null;
            }
            String lastSynTime = CoreLibrary.getInstance().context().allSharedPreferences().getPreference(LAST_NOTIFICATION_TIME);
            if(TextUtils.isEmpty(lastSynTime)){
                lastSynTime ="0";
            }
            //testing
            String url = baseUrl + NOTIFICATION_URL + "username=" + userName+"&timestamp="+lastSynTime;
            //url = "http://cbhc.mpower-social.com:8080/opensrp/message?username=cc10006957@mhv.3.3&timestamp=0";

            Log.v("NOTIFICATION_FETCH","url:"+url);
            org.smartregister.domain.Response resp = httpAgent.fetch(url);
            if (resp.isFailure()) {
                throw new NoHttpResponseException(NOTIFICATION_URL + " not returned data");
            }

            return new JSONArray((String) resp.payload());
        }catch (Exception e){

        }
        return null;

    }
}
