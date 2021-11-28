package org.smartregister.cbhc.sync;

import static com.vijay.jsonwizard.utils.FormUtils.fields;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.cbhc.application.AncApplication;
import org.smartregister.cbhc.domain.FollowupForm;
import org.smartregister.cbhc.helper.ECSyncHelper;
import org.smartregister.cbhc.model.RegisterModel;
import org.smartregister.cbhc.repository.FollowupRepository;
import org.smartregister.cbhc.util.Constants;
import org.smartregister.cbhc.util.DBConstants;
import org.smartregister.cbhc.util.JsonFormUtils;
import org.smartregister.cbhc.util.Utils;
import org.smartregister.commonregistry.AllCommonsRepository;
import org.smartregister.domain.db.Client;
import org.smartregister.domain.db.Event;
import org.smartregister.domain.db.EventClient;
import org.smartregister.domain.jsonmapping.ClientClassification;
import org.smartregister.domain.jsonmapping.ClientField;
import org.smartregister.domain.jsonmapping.Table;
import org.smartregister.repository.AllSharedPreferences;
import org.smartregister.repository.DetailsRepository;
import org.smartregister.sync.ClientProcessorForJava;
import org.smartregister.util.FormUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by ndegwamartin on 15/03/2018.
 */

public class AncClientProcessorForJava extends ClientProcessorForJava {
    FormUtils formUtils=null;
    private static final String TAG = AncClientProcessorForJava.class.getCanonicalName();
    private static AncClientProcessorForJava instance;

    public AncClientProcessorForJava(Context context) {
        super(context);
    }

    public static AncClientProcessorForJava getInstance(Context context) {
        if (instance == null) {
            instance = new AncClientProcessorForJava(context);
        }

        return instance;
    }


    @Override
    public void processClient(List<EventClient> eventClients) throws Exception {

        ClientClassification clientClassification = assetJsonToJava("ec_client_classification.json", ClientClassification.class);

        if (!eventClients.isEmpty()) {
            List<Event> unsyncEvents = new ArrayList<>();
            for (EventClient eventClient : eventClients) {
                Event event = eventClient.getEvent();
                if (event == null) {
                    return;
                }

                String eventType = event.getEventType();
                Log.d("eventttt","evv "+eventType);
                if (eventType == null) {
                    continue;
                }

                if (eventType.equals(Constants.EventType.CLOSE)) {
                    unsyncEvents.add(event);
                } else if (eventType.equals(Constants.EventType.HouseholdREGISTRATION)
                        || eventType.equals(Constants.EventType.UPDATE_Household_REGISTRATION)
                        || eventType.equals(Constants.EventType.MemberREGISTRATION)
                        || eventType.equals(Constants.EventType.WomanMemberREGISTRATION)
                        || eventType.equals(Constants.EventType.Child_REGISTRATION)
                        || eventType.equals(Constants.EventType.UPDATE_REGISTRATION )
                        || eventType.equals(Constants.EventType.OOCMemberREGISTRATION)
                       /* || !Utils.notFollowUp(eventType)*/
                ) {
                    if (clientClassification == null) {
                        continue;
                    }

                    Client client = eventClient.getClient();
                    if (!eventType.contains("Household")) {
                        String log = "client: " + client.getRelationships();
                        System.out.println(log);
                    }
                    //iterate through the events
                    if (client != null && event != null && clientClassification != null) {
                        if (!eventType.contains("Household")) {
                            if (client.getRelationships() != null) {
                                processEvent(event, client, clientClassification);
                            }
                        } else {
                            processEvent(event, client, clientClassification);
                        }


                    }
                }
                else if(!Utils.notFollowUp(eventType)){
                    Gson gson = new Gson();
                    FollowupForm followupForm = new FollowupForm();
                    followupForm.setBase_entity_id(event.getBaseEntityId());
                    followupForm.setForm_name(event.getEventType());
                    followupForm.setDate(new Date());

                    JSONArray obsArr = new JSONArray(gson.toJson(event.getObs()));
                    //JsonFormUtils.startFormForEdit();
                    JSONObject form = getFormUtils().getFormJson(getFormByName(event.getEventType()));
                    JSONArray field = fields(form, "step1");

                    for(int i=0;i<field.length();i++){
                        JSONObject jsonObject = field.getJSONObject(i);
                        for(int j=0;j<obsArr.length();j++){
                           JSONObject jsonObjectObs = obsArr.getJSONObject(j);
                           if(jsonObjectObs.getString("fieldCode").trim().equals(jsonObject.getString("key").trim())){
                              // jsonObject.put("value", getString(jsonObjectObs.getJSONArray("values").get(0).toString()));
                              // Log.d("tttttest",event.getBaseEntityId()+"    "+jsonObjectObs.getString("fieldCode").trim()+"   "+jsonObject.getString("key").trim()+" "+jsonObjectObs.getJSONArray("values").get(0));
                               //break;
                               populateValuesForFormObject(jsonObjectObs.getJSONArray("values").get(0).toString(),jsonObjectObs.getJSONArray("values"),jsonObject,jsonObjectObs.getString("fieldCode"));
                               break;
                           }
                        }

                    }

                    followupForm.setFormFields(form.toString());

                    FollowupRepository followupFormRepository = new FollowupRepository(AncApplication.getInstance().getRepository());
                    followupFormRepository.saveForm(followupForm);
                }
            }

            // Unsync events that are should not be in this device
            if (!unsyncEvents.isEmpty()) {
                unSync(unsyncEvents);
            }
        }
    }

    public static void populateValuesForFormObject(String resultValue,JSONArray jsonArray, JSONObject jsonObject,String resultKey) {
        try {
            String value = resultValue;

            if (jsonObject.has("openmrs_choice_ids")) {
                JSONObject choiceObject = jsonObject.getJSONObject("openmrs_choice_ids");
                try{
                    for (int i = 0; i < choiceObject.names().length(); i++) {
                        if (value.equalsIgnoreCase(choiceObject.getString(choiceObject.names().getString(i)))) {
                            value = choiceObject.names().getString(i);
                        }
                    }
                }catch ( Exception e){

                }

                if(!TextUtils.isEmpty(value)){
                    jsonObject.put(JsonFormUtils.VALUE,value);
                }

            }else if (jsonObject.has("options")) {
                  /*  JSONArray option_array = jsonObject.getJSONArray("options");
                    Log.d("tttest",jsonObject.getString("key")+"   "+resultKey+"  "+option_array);
                    for (int i = 0; i < option_array.length(); i++) {
                        JSONObject option = option_array.getJSONObject(i);
                        if(jsonObject.getString("key").equalsIgnoreCase(resultKey)){
                           // for(String name : strs){
                               // if (name.equalsIgnoreCase(option.optString("key"))) {
                                    option.put("value", resultValue);
                               // }
                            //}
                        }else{
                            option.put("value", "false");
                        }
                    }*/

            }
            else{
                jsonObject.put(JsonFormUtils.VALUE, value);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }







    private String getString(String values) {
        if(values.equals("yes"))return "হ্যাঁ";
        else if (values.equals("no")) return "না";
        return values;
    }


    private FormUtils getFormUtils() {
        if (formUtils == null) {
            try {
                formUtils = FormUtils.getInstance(AncApplication.getInstance().getApplicationContext());
            } catch (Exception e) {
                Utils.appendLog(getClass().getName(),e);
                Log.e(RegisterModel.class.getCanonicalName(), e.getMessage(), e);
            }
        }
        return formUtils;
    }

    private String getFormByName(String eventType) {
        if(eventType.equals("Followup ANC")){
            return "followup/mhv/Followup_Form_MHV_ANC";
        }else if(eventType.equals("Followup Death Status")){
            return "followup/mhv/Followup_Form_MHV_Death";
        }else if(eventType.equals("Followup Delivery")){
            return "followup/mhv/Followup_Form_MHV_Delivery";
        }else if(eventType.equals("Member Registration")){
            return "followup/mhv/Followup_Form_MHV_DS";
        }else if(eventType.equals("Followup Family Planning")){
            return "followup/mhv/Followup_Form_MHV_FP";
        }else if(eventType.equals("Followup HH Transfer")){
            return "followup/mhv/Followup_Form_MHV_HH_Transfer";
        }else if(eventType.equals("Followup Marital Status Female")){
            return "followup/mhv/Followup_Form_MHV_Marital_F";
        }else if(eventType.equals("Followup Marital Status Male")){
            return "followup/mhv/Followup_Form_MHV_Marital_M";
        }else if(eventType.equals("Followup Member Transfer")){
            return "followup/mhv/Followup_Form_MHV_Member_Transfer";
        }else if(eventType.equals("Followup Mobile Number")){
            return "followup/mhv/Followup_Form_MHV_Mobile_no";
        }else if(eventType.equals("Followup PNC")){
            return "followup/mhv/Followup_Form_MHV_PNC";
        }else if(eventType.equals("Followup Pregnant Status")){
            return "followup/mhv/Followup_Form_MHV_Pregnant";
        }else if(eventType.equals("Followup Risky Habit")){
            return "followup/mhv/Followup_Form_MHV_Risky_Habit";
        }
        //DS
        else if(eventType.equals("Followup Disease Female")){
            return "followup/mhv/DS/Followup_Form_MHV_DS_Female";
        }else if(eventType.equals("Followup Disease Male")){
            return "followup/mhv/DS/Followup_Form_MHV_DS_Male";
        }else if(eventType.equals("Followup Disease Child")){
            return "followup/mhv/DS/Followup_Form_MHV_DS_NewBorn";
        }else if(eventType.equals("Followup Disease Toddler")){
            return "followup/mhv/DS/Followup_Form_MHV_DS_Toddler";
        }
        return "";
    }

    /*
        private Integer parseInt(String string) {
            try {
                return Integer.valueOf(string);
            } catch (NumberFormatException e) {
Utils.appendLog(getClass().getName(),e);
                Log.e(TAG, e.toString(), e);
            }
            return null;
        }

        private ContentValues processCaseModel(EventClient eventClient, Table table) {
            try {
                List<Column> columns = table.columns;
                ContentValues contentValues = new ContentValues();

                for (Column column : columns) {
                    processCaseModel(eventClient.getEvent(), eventClient.getClient(), column, contentValues);
                }

                return contentValues;
            } catch (Exception e) {
Utils.appendLog(getClass().getName(),e);
                Log.e(TAG, e.toString(), e);
            }
            return null;
        }

        private Date getDate(String eventDateStr) {
            Date date = null;
            if (StringUtils.isNotBlank(eventDateStr)) {
                try {
                    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZZZZZ");
                    date = dateFormat.parse(eventDateStr);
                } catch (ParseException e) {
Utils.appendLog(getClass().getName(),e);
                    try {
                        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
                        date = dateFormat.parse(eventDateStr);
                    } catch (ParseException pe) {
                        try {
                            date = DateUtil.parseDate(eventDateStr);
                        } catch (ParseException pee) {
                            Log.e(TAG, pee.toString(), pee);
                        }
                    }
                }
            }
            return date;
        }

    */
    private boolean unSync(ECSyncHelper ecSyncHelper, DetailsRepository detailsRepository, List<Table> bindObjects, Event event, String registeredAnm) {
        try {
            String baseEntityId = event.getBaseEntityId();
            String providerId = event.getProviderId();

            if (providerId.equals(registeredAnm)) {
                boolean eventDeleted = ecSyncHelper.deleteEventsByBaseEntityId(baseEntityId);
                boolean clientDeleted = ecSyncHelper.deleteClient(baseEntityId);
                Log.d(getClass().getName(), "EVENT_DELETED: " + eventDeleted);
                Log.d(getClass().getName(), "ClIENT_DELETED: " + clientDeleted);

                boolean detailsDeleted = detailsRepository.deleteDetails(baseEntityId);
                Log.d(getClass().getName(), "DETAILS_DELETED: " + detailsDeleted);

                for (Table bindObject : bindObjects) {
                    String tableName = bindObject.name;

                    boolean caseDeleted = deleteCase(tableName, baseEntityId);
                    Log.d(getClass().getName(), "CASE_DELETED: " + caseDeleted);
                }

                return true;
            }
        } catch (Exception e) {
            Utils.appendLog(getClass().getName(), e);
            Log.e(TAG, e.toString(), e);
        }
        return false;
    }

    private boolean unSync(List<Event> events) {
        try {

            if (events == null || events.isEmpty()) {
                return false;
            }

            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
            AllSharedPreferences allSharedPreferences = new AllSharedPreferences(preferences);
            String registeredAnm = allSharedPreferences.fetchRegisteredANM();

            ClientField clientField = assetJsonToJava("ec_client_fields.json", ClientField.class);
            if (clientField == null) {
                return false;
            }

            List<Table> bindObjects = clientField.bindobjects;
            DetailsRepository detailsRepository = AncApplication.getInstance().getContext().detailsRepository();
            ECSyncHelper ecUpdater = ECSyncHelper.getInstance(getContext());

            for (Event event : events) {
                unSync(ecUpdater, detailsRepository, bindObjects, event, registeredAnm);
            }

            return true;

        } catch (Exception e) {
            Utils.appendLog(getClass().getName(), e);
            Log.e(TAG, e.toString(), e);
        }

        return false;
    }


    @Override
    public String[] getOpenmrsGenIds() {
        return new String[]{DBConstants.KEY.ANC_ID};
    }

    @Override
    public void updateFTSsearch(String tableName, String entityId, ContentValues contentValues) {

        Log.i(TAG, "Starting updateFTSsearch table: " + tableName);

        AllCommonsRepository allCommonsRepository = org.smartregister.CoreLibrary.getInstance().context().
                allCommonsRepositoryobjects(tableName);

        if (allCommonsRepository != null) {
            allCommonsRepository.updateSearch(entityId);
        }

        Log.i(TAG, "Finished updateFTSsearch table: " + tableName);
    }
}
