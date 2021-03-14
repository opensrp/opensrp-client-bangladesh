package org.smartregister.cbhc.service.intent;

import android.app.IntentService;
import android.content.Intent;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.cbhc.application.AncApplication;
import org.smartregister.cbhc.repository.EventLog;
import org.smartregister.cbhc.repository.EventLogRepository;
import org.smartregister.cbhc.util.EVENT_TYPE;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import static org.smartregister.util.JsonFormUtils.gson;
import org.smartregister.clientandeventmodel.Event;
import org.smartregister.clientandeventmodel.Client;
import org.smartregister.clientandeventmodel.Obs;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.util.AssetHandler;

public class EventLogIntentService extends IntentService {
    EventLogRepository eventLogRepository;
    public static EventLogIntentService intentService;
    public EventLogIntentService() {
        super("VisitLogService");
    }
    public String getFormName(final String encounter_type){
        for (EVENT_TYPE et : EVENT_TYPE.values()){
            if(et.encounter_type.equals(encounter_type)){
                return et.form_name;
            }
        }

        return null;
    }
    @Override
    protected void onHandleIntent(Intent intent) {
        if(intentService!=null)return;
        intentService = this;
        eventLogRepository = AncApplication.getInstance().getEventLogRepository();
        ArrayList<EventLog> eventList = eventLogRepository.getEvents();

        for (int i = 0; i < eventList.size(); i++) {
            EventLog eventLog = eventList.get(i);
            String eventJson = eventLog.getEventJson();
            String clientJson = eventLog.getClientJson();

            try {
                JSONObject eventJsonObject = new JSONObject(eventJson);
                String eventId = eventJsonObject.getString("id");
                JSONObject clientJsonObject = new JSONObject(clientJson);
                String birthdate = clientJsonObject.getString("birthdate");
                if(birthdate.contains("+"))
                birthdate = birthdate.substring(0,birthdate.indexOf("+"))+"+06:00";
                clientJsonObject.put("birthdate",birthdate);
                Client baseClient = gson.fromJson(clientJsonObject.toString(), Client.class);
                Event baseEvent = gson.fromJson(eventJson, Event.class);
                HashMap<String,String>details = new HashMap<>();
                getValuesFromClientObject(baseClient,details);
                getValuesFromEventObject(baseEvent,details);
                String encounter_type = baseEvent.getEventType();
                String base_entity_id = baseClient.getBaseEntityId();
                final CommonPersonObjectClient client = new CommonPersonObjectClient(base_entity_id, details, "");
                client.setColumnmaps(details);
                String form_name = getFormName(encounter_type);
                if(form_name == null)continue;
                JSONObject form_object = loadFormFromAsset(form_name);

                JSONObject stepOne = form_object.getJSONObject(org.smartregister.util.JsonFormUtils.STEP1);
                JSONArray jsonArray = stepOne.getJSONArray(org.smartregister.util.JsonFormUtils.FIELDS);
                for (int k = 0; k < jsonArray.length(); k++) {
                    populateValuesForFormObject(client, jsonArray.getJSONObject(k));
                }

                EventLog log = new EventLog();
                log.setEventId(eventId);
                log.setEventType(baseEvent.getEventType());
                log.setBaseEntityId(base_entity_id);
                log.setFamilyId(baseClient.getRelationalBaseEntityId());
                log.setEventDate(baseEvent.getEventDate().toString());
                log.setFormJson(form_object.toString());
                eventLogRepository.add(log);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        intentService = null;
    }

    private static void populateValuesForFormObject(CommonPersonObjectClient client, JSONObject jsonObject) {
        try {
            String value = org.smartregister.util.Utils.getValue(client.getColumnmaps(),jsonObject.getString(org.smartregister.util.JsonFormUtils.KEY),false);
            //spinner
            if (jsonObject.has("openmrs_choice_ids")) {
                JSONObject choiceObject = jsonObject.getJSONObject("openmrs_choice_ids");

                for (int i = 0; i < choiceObject.names().length(); i++) {
                    if (value.equalsIgnoreCase(choiceObject.getString(choiceObject.names().getString(i)))) {
                        value = choiceObject.names().getString(i);
                    }
                }
                jsonObject.put(org.smartregister.util.JsonFormUtils.VALUE,value);
            }else if (jsonObject.has("options")) {

                    JSONArray option_array = jsonObject.getJSONArray("options");
                    for (int i = 0; i < option_array.length(); i++) {
                        JSONObject option = option_array.getJSONObject(i);
                        if (value.contains(option.optString("key"))) {
                            option.put("value", "true");
                        }
                    }

            }
            else{
                jsonObject.put(org.smartregister.util.JsonFormUtils.VALUE, value);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public JSONObject getDetailsToJson(HashMap<String,String>details){
        JSONObject object = new JSONObject();
        for(Map.Entry<String,String> entry:details.entrySet()){
            String key = entry.getKey();
            String val = entry.getValue();
            try {
                object.put(key,val);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return object;
    }

    public JSONObject loadFormFromAsset(String form_name) {

        try {
            String jsonString = AssetHandler.readFileFromAssetsFolder("json.form/"+form_name+".json", EventLogIntentService.this);
            return new JSONObject(jsonString);
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (NullPointerException e){

        }
        return new JSONObject();
    }

    public void getValuesFromEventObject(Event baseEvent,HashMap<String,String>details) {
        for (Obs o : baseEvent.getObs()) {
            if(details.containsKey(o.getFormSubmissionField())) {
                details.put(o.getFormSubmissionField(),details.get(o.getFormSubmissionField())+","+o.getValue());
            } else {
                details.put(o.getFormSubmissionField(),(String)o.getValue());
            }
        }
    }
    public void getValuesFromClientObject(Client baseClient, HashMap<String,String>details){
        details.put("first_name",baseClient.getFirstName());
        details.put("last_name",baseClient.getLastName());
        details.put("dob",baseClient.getBirthdate().toString());
        details.put("gender",baseClient.getGender());
        for (Map.Entry o: baseClient.getAttributes().entrySet()) {

            if(details.containsKey((String)o.getKey())) {
                details.put((String)o.getKey(),details.get((String)o.getKey())+","+(String)o.getValue());
            } else {
                details.put((String)o.getKey(),(String)o.getValue());
            }
        }
    }

}
