package org.smartregister.cbhc.sync;

import static com.vijay.jsonwizard.utils.FormUtils.fields;

import static java.lang.Float.parseFloat;
import static java.lang.Integer.parseInt;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
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
import org.smartregister.cbhc.util.GrowthUtil;
import org.smartregister.cbhc.util.JsonFormUtils;
import org.smartregister.cbhc.util.Utils;
import org.smartregister.clientandeventmodel.DateUtil;
import org.smartregister.commonregistry.AllCommonsRepository;
import org.smartregister.commonregistry.CommonRepository;
import org.smartregister.domain.db.Client;
import org.smartregister.domain.db.Event;
import org.smartregister.domain.db.EventClient;
import org.smartregister.domain.jsonmapping.ClientClassification;
import org.smartregister.domain.jsonmapping.ClientField;
import org.smartregister.domain.jsonmapping.Table;
import org.smartregister.growthmonitoring.GrowthMonitoringLibrary;
import org.smartregister.growthmonitoring.domain.Height;
import org.smartregister.growthmonitoring.domain.MUAC;
import org.smartregister.growthmonitoring.domain.Weight;
import org.smartregister.growthmonitoring.domain.ZScore;
import org.smartregister.growthmonitoring.repository.HeightRepository;
import org.smartregister.growthmonitoring.repository.MUACRepository;
import org.smartregister.growthmonitoring.repository.WeightRepository;
import org.smartregister.growthmonitoring.service.intent.HeightIntentService;
import org.smartregister.growthmonitoring.service.intent.MuacIntentService;
import org.smartregister.growthmonitoring.service.intent.WeightIntentService;
import org.smartregister.immunization.ImmunizationLibrary;
import org.smartregister.immunization.domain.ServiceRecord;
import org.smartregister.immunization.domain.ServiceType;
import org.smartregister.immunization.domain.Vaccine;
import org.smartregister.immunization.repository.RecurringServiceRecordRepository;
import org.smartregister.immunization.repository.RecurringServiceTypeRepository;
import org.smartregister.immunization.repository.VaccineRepository;
import org.smartregister.immunization.service.intent.RecurringIntentService;
import org.smartregister.immunization.service.intent.VaccineIntentService;
import org.smartregister.repository.AllSharedPreferences;
import org.smartregister.repository.DetailsRepository;
import org.smartregister.sync.ClientProcessorForJava;
import org.smartregister.util.AssetHandler;
import org.smartregister.util.FormUtils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
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
        String clientVaccineStr = getFileContents("ec_client_vaccine.json");
        String clientWeightStr = getFileContents("ec_client_weight.json");
        String clientMuacStr = getFileContents("ec_client_muac.json");
        String clientHeightStr = getFileContents("ec_client_height.json");
        String clientServiceStr = getFileContents("ec_client_service.json");

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
                    //iterate through the events
                    if (client != null) {
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
                else if(isGMP(eventType)){
                    if (eventType.equals(VaccineIntentService.EVENT_TYPE) || eventType.equals(VaccineIntentService.EVENT_TYPE_OUT_OF_CATCHMENT)) {
                       try{
                           JSONObject clientVaccineClassificationJson = new JSONObject(clientVaccineStr);
                           String eventJson = new Gson().toJson(event);
                           JSONObject jsonObject = new JSONObject(eventJson);
                           processVaccine(jsonObject, clientVaccineClassificationJson, eventType.equals(VaccineIntentService.EVENT_TYPE_OUT_OF_CATCHMENT));
                       }catch (Exception e){
                           e.printStackTrace();
                       }

                    }else if (eventType.equals(WeightIntentService.EVENT_TYPE) || eventType.equals(WeightIntentService.EVENT_TYPE_OUT_OF_CATCHMENT)) {
                        try{
                            JSONObject clientWeightClassificationJson = new JSONObject(clientWeightStr);
                            JSONObject jsonObject = new JSONObject(new Gson().toJson(event));
                            processWeight(jsonObject,event.getEventDate(), clientWeightClassificationJson, eventType.equals(WeightIntentService.EVENT_TYPE_OUT_OF_CATCHMENT));

                        }catch (Exception e){
                            e.printStackTrace();
                        }
                 }
                    else if (eventType.equals(HeightIntentService.EVENT_TYPE) || eventType.equals(HeightIntentService.EVENT_TYPE_OUT_OF_CATCHMENT)) {
                        try{
                            JSONObject clientWeightClassificationJson = new JSONObject(clientHeightStr);
                            JSONObject jsonObject = new JSONObject(new Gson().toJson(event));
                            processHeight(jsonObject,event.getEventDate(), clientWeightClassificationJson, eventType.equals(HeightIntentService.EVENT_TYPE_OUT_OF_CATCHMENT));

                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                    else if (eventType.equals(MuacIntentService.EVENT_TYPE) || eventType.equals(MuacIntentService.EVENT_TYPE_OUT_OF_CATCHMENT)) {
                        try{
                            JSONObject clientWeightClassificationJson = new JSONObject(clientMuacStr);
                            JSONObject jsonObject = new JSONObject(new Gson().toJson(event));
                            processMUAC(jsonObject,event.getEventDate(), clientWeightClassificationJson, eventType.equals(MuacIntentService.EVENT_TYPE_OUT_OF_CATCHMENT));

                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                    else if (eventType.equals(RecurringIntentService.EVENT_TYPE)) {
                        JSONObject clientServiceClassificationJson = new JSONObject(clientServiceStr);
                        JSONObject jsonObject = new JSONObject(new Gson().toJson(event));
                        processService(jsonObject, clientServiceClassificationJson);
                    }
                }
            }

            // Unsync events that are should not be in this device
            if (!unsyncEvents.isEmpty()) {
                unSync(unsyncEvents);
            }
        }
    }

    private String getFileContents(String fileName) {
        String jsonstring = AssetHandler.readFileFromAssetsFolder(fileName, AncApplication.getInstance().getApplicationContext());
        return jsonstring;
    }

    private boolean isGMP(String eventType) {
        switch (eventType){
            case VaccineIntentService.EVENT_TYPE:
            case WeightIntentService.EVENT_TYPE:
            case HeightIntentService.EVENT_TYPE:
            case MuacIntentService.EVENT_TYPE:
            case RecurringIntentService.EVENT_TYPE:
                return true;
            default: return false;
        }
    }
    private Boolean processVaccine(JSONObject event, JSONObject clientVaccineClassificationJson, boolean outOfCatchment) throws Exception {

        try {

            if (event == null) {
                return false;
            }

            if (clientVaccineClassificationJson == null || clientVaccineClassificationJson.length() == 0) {
                return false;
            }

            ContentValues contentValues = processCaseModel(event, clientVaccineClassificationJson);

            // save the values to db
            if (contentValues != null && contentValues.size() > 0) {
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
                Date date = simpleDateFormat.parse(contentValues.getAsString(VaccineRepository.DATE));

                VaccineRepository vaccineRepository = ImmunizationLibrary.getInstance().vaccineRepository();
                Vaccine vaccineObj = new Vaccine();
                vaccineObj.setBaseEntityId(contentValues.getAsString(VaccineRepository.BASE_ENTITY_ID));
                vaccineObj.setName(contentValues.getAsString(VaccineRepository.NAME));
                if (contentValues.containsKey(VaccineRepository.CALCULATION)) {
                    vaccineObj.setCalculation(parseInt(contentValues.getAsString(VaccineRepository.CALCULATION)));
                }
                vaccineObj.setDate(date);
                vaccineObj.setAnmId(contentValues.getAsString(VaccineRepository.ANMID));
                vaccineObj.setLocationId(contentValues.getAsString(VaccineRepository.LOCATION_ID));
                vaccineObj.setSyncStatus(VaccineRepository.TYPE_Synced);
                vaccineObj.setFormSubmissionId(event.has(VaccineRepository.FORMSUBMISSION_ID) ? event.getString(VaccineRepository.FORMSUBMISSION_ID) : null);
                try{
                    vaccineObj.setEventId(event.getString("id")); //FIXME hard coded id
                }catch (Exception e){

                }
                vaccineObj.setOutOfCatchment(outOfCatchment ? 1 : 0);

                vaccineRepository.add(vaccineObj);
            }
            return true;

        } catch (Exception e) {
            Log.e(TAG, e.toString(), e);
            return null;
        }
    }
    private Boolean processHeight(JSONObject height, DateTime date,JSONObject clientWeightClassificationJson, boolean outOfCatchment) throws Exception {

        try {

            if (height == null || height.length() == 0) {
                return false;
            }

            if (clientWeightClassificationJson == null || clientWeightClassificationJson.length() == 0) {
                return false;
            }

            ContentValues contentValues = processCaseModel(height, clientWeightClassificationJson);
            Log.v("CLIENT_PROCESSOR","eventheight>>"+height);
            Log.v("CLIENT_PROCESSOR","processHeight>>"+contentValues);

            // save the values to db
            if (contentValues != null && contentValues.size() > 0) {

                HeightRepository heightRepository = GrowthMonitoringLibrary.getInstance().getHeightRepository();
                Height heightObj = new Height();
                heightObj.setBaseEntityId(contentValues.getAsString(WeightRepository.BASE_ENTITY_ID));
                if (contentValues.containsKey(HeightRepository.CM)) {
                    heightObj.setCm(parseFloat(contentValues.getAsString(HeightRepository.CM)));
                }
                try{
                    heightObj.setDate(date.toDate());
                }catch (Exception e){

                }
                heightObj.setZScore(Double.parseDouble(contentValues.getAsString(HeightRepository.Z_SCORE)));
                heightObj.setAnmId(contentValues.getAsString(WeightRepository.ANMID));
                heightObj.setLocationId(contentValues.getAsString(WeightRepository.LOCATIONID));
                heightObj.setSyncStatus(WeightRepository.TYPE_Synced);
                heightObj.setFormSubmissionId(height.has(WeightRepository.FORMSUBMISSION_ID) ? height.getString(WeightRepository.FORMSUBMISSION_ID) : null);
               try{
                   heightObj.setEventId(height.getString("id"));
               }catch (Exception e){

               }
                heightObj.setOutOfCatchment(outOfCatchment ? 1 : 0);


                heightRepository.add(heightObj);
                String heightText = ZScore.getZScoreText(heightObj.getZScore());
                GrowthUtil.updateLastHeight(heightObj.getCm(),heightObj.getBaseEntityId(),heightText);
            }
            return true;

        } catch (Exception e) {
            Log.e(TAG, e.toString(), e);
            return null;
        }
    }
    private Boolean processMUAC(JSONObject muac, DateTime date,JSONObject clientWeightClassificationJson, boolean outOfCatchment) throws Exception {

        try {

            if (muac == null || muac.length() == 0) {
                return false;
            }

            if (clientWeightClassificationJson == null || clientWeightClassificationJson.length() == 0) {
                return false;
            }

            ContentValues contentValues = processCaseModel(muac, clientWeightClassificationJson);

            // save the values to db
            if (contentValues != null && contentValues.size() > 0) {


                MUACRepository muacRepository = GrowthMonitoringLibrary.getInstance().getMuacRepository();
                MUAC muacObj = new MUAC();
                muacObj.setBaseEntityId(contentValues.getAsString(WeightRepository.BASE_ENTITY_ID));
                if (contentValues.containsKey(HeightRepository.CM)) {
                    muacObj.setCm(parseFloat(contentValues.getAsString(HeightRepository.CM)));
                }
                try{
                    muacObj.setDate(date.toDate());
                }catch (Exception e){

                }
                muacObj.setAnmId(contentValues.getAsString(WeightRepository.ANMID));
                muacObj.setLocationId(contentValues.getAsString(WeightRepository.LOCATIONID));
                muacObj.setSyncStatus(WeightRepository.TYPE_Synced);
                muacObj.setFormSubmissionId(muac.has(WeightRepository.FORMSUBMISSION_ID) ? muac.getString(WeightRepository.FORMSUBMISSION_ID) : null);
                try{
                    muacObj.setEventId(muac.getString("id"));
                }catch (Exception e){

                }
                muacObj.setOutOfCatchment(outOfCatchment ? 1 : 0);
                muacObj.setEdemaValue(contentValues.getAsString(MUACRepository.EDEMA_VALUE));
                String status = ZScore.getMuacText(muacObj.getCm());

                muacRepository.add(muacObj);
                GrowthUtil.updateLastMuac(muacObj.getCm(),muacObj.getBaseEntityId(),status,muacObj.getEdemaValue());
            }
            return true;

        } catch (Exception e) {
            Log.e(TAG, e.toString(), e);
            return null;
        }
    }
    private Boolean processWeight(JSONObject weight, DateTime date, JSONObject clientWeightClassificationJson, boolean outOfCatchment) throws Exception {

        try {

            if (weight == null || weight.length() == 0) {
                return false;
            }

            if (clientWeightClassificationJson == null || clientWeightClassificationJson.length() == 0) {
                return false;
            }

            ContentValues contentValues = processCaseModel(weight, clientWeightClassificationJson);

            // save the values to db
            if (contentValues != null && contentValues.size() > 0) {

                WeightRepository weightRepository = AncApplication.getInstance().weightRepository();
                Weight weightObj = new Weight();
                weightObj.setBaseEntityId(contentValues.getAsString(WeightRepository.BASE_ENTITY_ID));
                if (contentValues.containsKey(WeightRepository.KG)) {
                    weightObj.setKg(parseFloat(contentValues.getAsString(WeightRepository.KG)));
                }
                weightObj.setDate(date.toDate());
                weightObj.setZScore(contentValues.getAsDouble(WeightRepository.Z_SCORE));
                weightObj.setAnmId(contentValues.getAsString(WeightRepository.ANMID));
                weightObj.setLocationId(contentValues.getAsString(WeightRepository.LOCATIONID));
                weightObj.setSyncStatus(WeightRepository.TYPE_Synced);
                weightObj.setFormSubmissionId(weight.has(WeightRepository.FORMSUBMISSION_ID) ? weight.getString(WeightRepository.FORMSUBMISSION_ID) : null);
                try{
                    weightObj.setEventId(weight.getString("id"));
                }catch (Exception e){

                }
                weightObj.setOutOfCatchment(outOfCatchment ? 1 : 0);
                Log.v("WEIGHT","taken>>>>"+weightObj.getKg());

                double zScore = ZScore.roundOff(weightObj.getZScore());
                String weightText = ZScore.getZScoreText(zScore);
                weightRepository.add(weightObj);
                //need to update child table
                GrowthUtil.updateLastWeight(weightObj.getKg(),weightObj.getBaseEntityId(),weightText);
            }
            return true;

        } catch (Exception e) {
            Log.e(TAG, e.toString(), e);
            return null;
        }
    }

    private Boolean processService(JSONObject service, JSONObject clientVaccineClassificationJson) throws Exception {

        try {

            if (service == null || service.length() == 0) {
                return false;
            }

            if (clientVaccineClassificationJson == null || clientVaccineClassificationJson.length() == 0) {
                return false;
            }

            ContentValues contentValues = processCaseModel(service, clientVaccineClassificationJson);

            // save the values to db
            if (contentValues != null && contentValues.size() > 0) {

                String name = contentValues.getAsString(RecurringServiceTypeRepository.NAME);
                if (StringUtils.isNotBlank(name)) {
                    name = name.replaceAll("_", " ").replace("dose", "").trim();
                }

                Date date = null;
                String eventDateStr = contentValues.getAsString(RecurringServiceRecordRepository.DATE);
                if (StringUtils.isNotBlank(eventDateStr)) {
                    date = DateUtil.getDateFromString(eventDateStr);
                    if (date == null) {
                        try {
                            date = DateUtil.parseDate(eventDateStr);
                        } catch (ParseException e) {
                            Log.e(TAG, e.toString(), e);
                        }
                    }
                }

                String value = null;

                if (StringUtils.containsIgnoreCase(name, "ITN")) {
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
                    String itnDateString = contentValues.getAsString("itn_date");
                    if (StringUtils.isNotBlank(itnDateString)) {
                        date = simpleDateFormat.parse(itnDateString);
                    }


                    value = RecurringIntentService.ITN_PROVIDED;
                    if (contentValues.getAsString("itn_has_net") != null) {
                        value = RecurringIntentService.CHILD_HAS_NET;
                    }

                }

                RecurringServiceTypeRepository recurringServiceTypeRepository = ImmunizationLibrary.getInstance().recurringServiceTypeRepository();
                List<ServiceType> serviceTypeList = recurringServiceTypeRepository.searchByName(name);
                if (serviceTypeList == null || serviceTypeList.isEmpty()) {
                    return false;
                }

                if (date == null) {
                    return false;
                }

                RecurringServiceRecordRepository recurringServiceRecordRepository = ImmunizationLibrary.getInstance().recurringServiceRecordRepository();
                ServiceRecord serviceObj = new ServiceRecord();
                serviceObj.setBaseEntityId(contentValues.getAsString(RecurringServiceRecordRepository.BASE_ENTITY_ID));
                serviceObj.setName(name);
                serviceObj.setDate(date);
                serviceObj.setAnmId(contentValues.getAsString(RecurringServiceRecordRepository.ANMID));
                serviceObj.setLocationId(contentValues.getAsString(RecurringServiceRecordRepository.LOCATION_ID));
                serviceObj.setSyncStatus(RecurringServiceRecordRepository.TYPE_Synced);
                serviceObj.setFormSubmissionId(service.has(RecurringServiceRecordRepository.FORMSUBMISSION_ID) ? service.getString(RecurringServiceRecordRepository.FORMSUBMISSION_ID) : null);
                try{
                    serviceObj.setEventId(service.getString("id")); //FIXME hard coded id
                }catch (Exception e){

                }
                serviceObj.setValue(value);
                serviceObj.setRecurringServiceId(serviceTypeList.get(0).getId());

                recurringServiceRecordRepository.add(serviceObj);
            }
            return true;

        } catch (Exception e) {
            Log.e(TAG, e.toString(), e);
            return null;
        }
    }
    private ContentValues processCaseModel(JSONObject entity, JSONObject clientClassificationJson) {
        try {
            JSONArray columns = clientClassificationJson.getJSONArray("columns");

            ContentValues contentValues = new ContentValues();

            for (int i = 0; i < columns.length(); i++) {
                JSONObject colObject = columns.getJSONObject(i);
                String columnName = colObject.getString("column_name");
                JSONObject jsonMapping = colObject.getJSONObject("json_mapping");
                String dataSegment = null;
                String fieldName = jsonMapping.getString("field");
                String fieldValue = null;
                String responseKey = null;
                String valueField = jsonMapping.has("value_field") ? jsonMapping.getString("value_field") : null;
                if (fieldName != null && fieldName.contains(".")) {
                    String fieldNameArray[] = fieldName.split("\\.");
                    dataSegment = fieldNameArray[0];
                    fieldName = fieldNameArray[1];
                    fieldValue = jsonMapping.has("concept") ? jsonMapping.getString("concept") : (jsonMapping.has("formSubmissionField") ? jsonMapping.getString("formSubmissionField") : null);
                    if (fieldValue != null) {
                        responseKey = VALUES_KEY;
                    }
                }

                Object jsonDocSegment = null;

                if (dataSegment != null) {
                    //pick data from a specific section of the doc
                    jsonDocSegment = entity.has(dataSegment) ? entity.get(dataSegment) : null;

                } else {
                    //else the use the main doc as the doc segment
                    jsonDocSegment = entity;

                }

                if (jsonDocSegment instanceof JSONArray) {

                    JSONArray jsonDocSegmentArray = (JSONArray) jsonDocSegment;

                    for (int j = 0; j < jsonDocSegmentArray.length(); j++) {
                        JSONObject jsonDocObject = jsonDocSegmentArray.getJSONObject(j);
                        String columnValue = null;
                        if (fieldValue == null) {
                            //this means field_value and response_key are null so pick the value from the json object for the field_name
                            if (jsonDocObject.has(fieldName)) {
                                columnValue = jsonDocObject.getString(fieldName);
                            }
                        } else {
                            //this means field_value and response_key are not null e.g when retrieving some value in the events obs section
                            String expectedFieldValue = jsonDocObject.getString(fieldName);
                            //some events can only be differentiated by the event_type value eg pnc1,pnc2, anc1,anc2

                            if (expectedFieldValue.equalsIgnoreCase(fieldValue)) {
                                if (StringUtils.isNotBlank(valueField) && jsonDocObject.has(valueField)) {
                                    columnValue = jsonDocObject.getString(valueField);
                                } else {
                                    List<String> values = getValues(jsonDocObject.get(responseKey));
                                    if (!values.isEmpty()) {
                                        columnValue = values.get(0);
                                    }
                                }
                            }
                        }
                        // after successfully retrieving the column name and value store it in Content value
                        if (columnValue != null) {
                            columnValue = getHumanReadableConceptResponse(columnValue, jsonDocObject);
                            contentValues.put(columnName, columnValue);
                        }
                    }

                } else {
                    //e.g client attributes section
                    String columnValue = null;
                    JSONObject jsonDocSegmentObject = (JSONObject) jsonDocSegment;
                    columnValue = jsonDocSegmentObject.has(fieldName) ? jsonDocSegmentObject.getString(fieldName) : "";
                    // after successfully retrieving the column name and value store it in Content value
                    if (columnValue != null) {
                        columnValue = getHumanReadableConceptResponse(columnValue, jsonDocSegmentObject);
                        contentValues.put(columnName, columnValue);
                    }

                }


            }

            return contentValues;
        } catch (Exception e) {
            Log.e(TAG, e.toString(), e);
        }
        return null;
    }
    protected List<String> getValues(Object jsonObject) throws JSONException {
        List<String> values = new ArrayList<String>();
        if (jsonObject == null) {
            return values;
        } else if (jsonObject instanceof JSONArray) {
            JSONArray jsonArray = (JSONArray) jsonObject;
            for (int i = 0; i < jsonArray.length(); i++) {
                values.add(jsonArray.get(i).toString());
            }
        } else {
            values.add(jsonObject.toString());
        }
        return values;
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

                //boolean detailsDeleted = detailsRepository.deleteDetails(baseEntityId);
                //Log.d(getClass().getName(), "DETAILS_DELETED: " + detailsDeleted);

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
    public boolean deleteCase(String tableName, String baseEntityId) {
        CommonRepository cr = org.smartregister.CoreLibrary.getInstance().context().commonrepository(tableName);
        return cr.deleteCase(baseEntityId, tableName);
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
            //DetailsRepository detailsRepository = AncApplication.getInstance().getContext().detailsRepository();
            ECSyncHelper ecUpdater = ECSyncHelper.getInstance(getContext());

            for (Event event : events) {
//                unSync(ecUpdater, detailsRepository, bindObjects, event, registeredAnm);
                unSync(ecUpdater, null, bindObjects, event, registeredAnm);
            }

            return true;

        } catch (Exception e) {
            Utils.appendLog(getClass().getName(), e);
            Log.e(TAG, e.toString(), e);
        }

        return false;
    }

    @Override
    public void updateClientDetailsTable(Event event, Client client) { }

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
