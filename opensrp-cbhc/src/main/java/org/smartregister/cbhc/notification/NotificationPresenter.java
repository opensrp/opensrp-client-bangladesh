package org.smartregister.cbhc.notification;

import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;

import org.apache.http.NoHttpResponseException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.CoreLibrary;
import org.smartregister.cbhc.application.AncApplication;
import org.smartregister.cbhc.util.AppExecutors;
import org.smartregister.service.HTTPAgent;

import java.util.ArrayList;


public class NotificationPresenter implements NotificationContract.Presenter {
    private static final String NOTIFICATION_URL = "/message?";
    private static final String LAST_NOTIFICATION_TIME = "last_notification_sync";
    AppExecutors appExecutors;
    NotificationContract.View view;
    ArrayList<NotificationDTO> notificationArrayList;


    public NotificationPresenter(NotificationContract.View view){
        this.view = view;
        appExecutors = new AppExecutors();
        notificationArrayList = new ArrayList<>();
    }

    public ArrayList<NotificationDTO> getNotificationArrayList() {
        Log.v("NotificationPresenter: ",notificationArrayList.size()+"");
        return notificationArrayList;
    }

    @Override
    public void processNotification() {
        view.showProgressBar();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    long timestamp = 0;
                    JSONArray jsonArray = getNotificaionList();
                    Log.v("JSON array: ",jsonArray+"");

                    for(int i=0;i<jsonArray.length();i++){
                        try {
                            JSONObject object = jsonArray.getJSONObject(i);
                            NotificationDTO notification =  new Gson().fromJson(object.toString(), NotificationDTO.class);
                            if(notification != null){
                                notificationArrayList.add(notification);
                                AncApplication.getNotificationRepository().addOrUpdate(notification);
                                timestamp = notification.getTimestamp();
                                Log.v("TARGET_FETCH","lasttime:"+timestamp);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    if(jsonArray.length()>0){
                        CoreLibrary.getInstance().context().allSharedPreferences().savePreference(LAST_NOTIFICATION_TIME,timestamp+"");
                    }

                    appExecutors.mainThread().execute(new Runnable() {
                        @Override
                        public void run() {
                            view.hideProgressBar();
                            view.updateAdapter();
                        }
                    });

                } catch (Exception e) {
                    e.printStackTrace();
                    appExecutors.mainThread().execute(new Runnable() {
                        @Override
                        public void run() {
                            view.hideProgressBar();
                        }
                    });
                }
            }
        };

        appExecutors.diskIO().execute(runnable);
    }

    public void fetchNotification(){
        view.showProgressBar();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    ArrayList<NotificationDTO> notifications = AncApplication.getNotificationRepository().getAllNotification();
                    if(notifications != null && notifications.size()>0){
                        notificationArrayList.addAll(notifications);
                    }
                    appExecutors.mainThread().execute(new Runnable() {
                        @Override
                        public void run() {
                            view.hideProgressBar();
                            view.updateAdapter();
                            processNotification();
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    appExecutors.mainThread().execute(new Runnable() {
                        @Override
                        public void run() {
                            view.hideProgressBar();
                        }
                    });
                }
            }
        };
        appExecutors.diskIO().execute(runnable);

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

            Log.v("NOtification Fetch","url:"+url);
            org.smartregister.domain.Response resp = httpAgent.fetch(url);
            if (resp.isFailure()) {
                throw new NoHttpResponseException(NOTIFICATION_URL + " not returned data");
            }

            return new JSONArray((String) resp.payload());
        }catch (Exception e){

        }
        return null;

    }

    @Override
    public NotificationContract.View getView() {
        return null;
    }
}
