package org.smartregister.cbhc.service.intent;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import net.sqlcipher.database.SQLiteDatabase;

import org.apache.http.NoHttpResponseException;
import org.json.JSONArray;
import org.json.JSONObject;
import org.smartregister.AllConstants;
import org.smartregister.CoreLibrary;
import org.smartregister.cbhc.application.AncApplication;
import org.smartregister.domain.Response;
import org.smartregister.service.HTTPAgent;

/**
 * Created by keyman on 11/10/2017.
 */
public class DataDeleteIntentService extends IntentService {

    private Context context;
    private HTTPAgent httpAgent;
    private static final String ALL_DATA = "all_data";
    private static final String HH = "households";
    private static final String MEMBER = "members";
    private static final String SERVER_VERSION = "server_version";
    private static final String DELETE_FETCH = "/rest/event/deleting?";
    private static final String TAG = "DataDeleteIntentService";
    public static final String LAST_SYNC_TIME = "delete_last_sync_time";
    public DataDeleteIntentService() {
        super("DataDeleteIntentService");
    }
    SQLiteDatabase db;
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        context = getBaseContext();
        httpAgent = CoreLibrary.getInstance().context().getHttpAgent();
        return super.onStartCommand(intent, flags, startId);

    }

    @Override
    protected void onHandleIntent(Intent intent) {


        try {
            HTTPAgent httpAgent = CoreLibrary.getInstance().context().getHttpAgent();
            String baseUrl = CoreLibrary.getInstance().context().
                    configuration().dristhiBaseURL();
            String endString = "/";
            if (baseUrl.endsWith(endString)) {
                baseUrl = baseUrl.substring(0, baseUrl.lastIndexOf(endString));
            }
            String userName = CoreLibrary.getInstance().context().allSharedPreferences().fetchRegisteredANM();
            if(TextUtils.isEmpty(userName)){
                return;
            }
            String lastSynTime = CoreLibrary.getInstance().context().allSharedPreferences().getPreference(LAST_SYNC_TIME);
            if(TextUtils.isEmpty(lastSynTime)){
                lastSynTime ="0";
            }
            //testing
            String url = baseUrl + DELETE_FETCH + SERVER_VERSION+"="+lastSynTime;
            Log.v("DataDelete","getLocationList>>url:"+url);
            Response resp = httpAgent.fetch(url);
            if (resp.isFailure()) {
                throw new NoHttpResponseException(url + " not returned data");
            }
            JSONObject results = new JSONObject((String) resp.payload());
            //{"all_data":["3a5d5800-bf02-4d33-8c09-58e81a1ffaf4-test
            //da54f146-81d9-41c8-a9ea-aae2e434d26e-test"],"serverVersion":1642325679000,"members":["59188799-39d7-4c3a-9a86-20f52e54506a","c4de7552-9c58-410c-9329-a520b2f9f0fb"],"households":["bd42029e-cba0-40f6-81a6-5c7a36cbc588","3d7e52af-3330-476a-8123-af7d5ab227ff"],"events":["1cc2f544-2b5a-4d77-9774-390f6b1b455e","87d240ff-aa37-4cef-b0ed-10d2ea1a7f3a","bbd878b1-cb98-4d31-ac00-5224d1327f04"]}
//           String test ="{\"all_data\":[\"3a5d5800-bf02-4d33-8c09-58e81a1ffaf4-test\",\"da54f146-81d9-41c8-a9ea-aae2e434d26e-test\"],\"serverVersion\":1642325679000,\"members\":[\"c1377ea8-ab6c-4b11-8bbd-6d36ce3cac54-test\",\"9d376051-a0b1-4a0c-8712-01167dc6cfd6-test\"],\"households\":[],\"events\":[\"0ea01098-41b6-4ecb-8c14-1fdefc2c0655-test\"]}";
//            JSONObject results = new JSONObject(test);
            db = AncApplication.getInstance().getRepository().getWritableDatabase();
            if (results.has(HH)){
                JSONArray validHHClient = results.getJSONArray(HH);
                StringBuilder builder = new StringBuilder();
                StringBuilder builderHH = new StringBuilder();
                for (int i = 0; i < validHHClient.length(); i++) {
                    String hhClientString = validHHClient.getString(i);
                    //delete client table
                    //delete ec_household
                    if(builder.toString().isEmpty()){
                        builder.append(" baseEntityId = '"+hhClientString+"'");
                        builderHH.append(" base_entity_id = '"+hhClientString+"'");
                    }else{
                        builder.append(" OR ");
                        builder.append(" baseEntityId = '"+hhClientString+"'");
                        builderHH.append(" OR ");
                        builderHH.append(" base_entity_id = '"+hhClientString+"'");
                    }

                }
                if(!TextUtils.isEmpty(builder.toString())){
                    String q = "delete from client where "+builder.toString();
                    Log.v("DATA_DELETE","q:"+q);
                    db.execSQL(q);
                }
                if(!TextUtils.isEmpty(builderHH.toString())){
                    String h = "delete from ec_household where "+builderHH.toString();
                    Log.v("DATA_DELETE","h:"+h);
                    db.execSQL(h);
                }
            }

            if (results.has(MEMBER)) {

                try{
                    JSONArray validMemberEvents = results.getJSONArray(MEMBER);
                    deleteMember(validMemberEvents);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
            if (results.has(ALL_DATA)) {

                try{
                    JSONArray validMemberEvents = results.getJSONArray(ALL_DATA);
                    deleteMember(validMemberEvents);
                    deleteEvents(true,validMemberEvents);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
            if (results.has(AllConstants.KEY.EVENTS)) {
                try{
                    JSONArray validEvents = results.getJSONArray(AllConstants.KEY.EVENTS);
                    deleteEvents(false,validEvents);

                }catch (Exception e){
                    e.printStackTrace();
                }
            }
            if (results.has(SERVER_VERSION)){
                long serverVersion = results.getLong(SERVER_VERSION);
                if(serverVersion !=0){
                    CoreLibrary.getInstance().context().allSharedPreferences().savePreference(LAST_SYNC_TIME,serverVersion+"");
                }
                Log.v("DATA_DELETE","serverVersion:"+serverVersion);
            }

        } catch (Exception e) {
            Log.e("DATA_DELETE", "", e);
        }
    }

    private void deleteEvents(boolean byBaseEntityId,JSONArray validEvents) throws Exception{
        StringBuilder builderEvent = new StringBuilder();
        for (int i = 0; i < validEvents.length(); i++) {
            String formSubmissionId = validEvents.getString(i);
            //delete event table
            //delete visits,ec_visit_log,target

            final String str = byBaseEntityId ? " baseEntityId= '" + formSubmissionId + "'" : " formSubmissionId='" + formSubmissionId + "'";
            if(builderEvent.toString().isEmpty()){
                builderEvent.append(str);

            }else{
                builderEvent.append(" OR ");
                builderEvent.append(str);
            }
        }
        if(!TextUtils.isEmpty(builderEvent.toString())){
            String q = "delete from event where "+builderEvent.toString();
            Log.v("DATA_DELETE","q:"+q);
            db.execSQL(q);
        }


    }

    private void deleteMember(JSONArray validMemberEvents) throws Exception{
        StringBuilder builderClient = new StringBuilder();
        StringBuilder builderMember = new StringBuilder();
        StringBuilder builderChild = new StringBuilder();
        StringBuilder builderWomen = new StringBuilder();
        for (int i = 0; i < validMemberEvents.length(); i++) {
            String memberClientString = validMemberEvents.getString(i);
            //delete client table
            //delete ec_member,ec_child
            if(builderClient.toString().isEmpty()){
                builderClient.append(" baseEntityId = '"+memberClientString+"'");
                builderMember.append(" base_entity_id = '"+memberClientString+"'");
                builderChild.append(" base_entity_id = '"+memberClientString+"'");
                builderWomen.append(" base_entity_id = '"+memberClientString+"'");
            }else{
                builderClient.append(" OR ");
                builderClient.append(" baseEntityId = '"+memberClientString+"'");
                builderMember.append(" OR ");
                builderMember.append(" base_entity_id = '"+memberClientString+"'");
                builderChild.append(" OR ");
                builderChild.append(" base_entity_id = '"+memberClientString+"'");
                builderWomen.append(" OR ");
                builderWomen.append(" base_entity_id = '"+memberClientString+"'");
            }
        }
        if(!TextUtils.isEmpty(builderClient.toString())){
            String q = "delete from client where "+builderClient.toString();
            Log.v("DATA_DELETE","q:"+q);
            db.execSQL(q);
        }
        if(!TextUtils.isEmpty(builderMember.toString())){
            String h = "delete from ec_member where "+builderMember.toString();
            Log.v("DATA_DELETE","h:"+h);
            db.execSQL(h);
        }
        if(!TextUtils.isEmpty(builderChild.toString())){
            String h = "delete from ec_child where "+builderChild.toString();
            Log.v("DATA_DELETE","h:"+h);
            db.execSQL(h);
        }
        if(!TextUtils.isEmpty(builderWomen.toString())){
            String h = "delete from ec_women where "+builderWomen.toString();
            Log.v("DATA_DELETE","h:"+h);
            db.execSQL(h);
        }
    }


}
