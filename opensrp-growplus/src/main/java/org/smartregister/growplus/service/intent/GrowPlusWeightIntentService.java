package org.smartregister.growplus.service.intent;

import android.app.IntentService;
import android.content.Intent;
import android.database.Cursor;
import android.text.TextUtils;
import android.util.Log;
import android.widget.LinearLayout;

import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONObject;
import org.smartregister.commonregistry.CommonPersonObject;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.commonregistry.CommonRepository;
import org.smartregister.growplus.application.VaccinatorApplication;
import org.smartregister.growthmonitoring.GrowthMonitoringLibrary;
import org.smartregister.growthmonitoring.domain.Weight;
import org.smartregister.growthmonitoring.repository.WeightRepository;
import org.smartregister.growthmonitoring.service.intent.WeightIntentService;
import org.smartregister.growthmonitoring.util.GMConstants;
import org.smartregister.growthmonitoring.util.JsonFormUtils;
import org.smartregister.repository.DetailsRepository;
import org.smartregister.util.Utils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import util.PathConstants;

import static org.smartregister.immunization.domain.ServiceSchedule.standardiseCalendarDate;

/**
 * Created by keyman on 3/01/2017.
 */
public class GrowPlusWeightIntentService extends IntentService {
    private static final String TAG = WeightIntentService.class.getCanonicalName();
    public static final String EVENT_TYPE = "Growth Monitoring";
    public static final String EVENT_TYPE_OUT_OF_CATCHMENT = "Out of Area Service - Growth Monitoring";
    public static final String ENTITY_TYPE = "weight";
    private WeightRepository weightRepository;
    private CommonRepository commonRepository;


    public GrowPlusWeightIntentService() {
        super("WeightService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        commonRepository = VaccinatorApplication.getInstance().context().commonrepository("ec_child");
        DetailsRepository detailsRepository = VaccinatorApplication.getInstance().context().detailsRepository();

        try {
            List<Weight> weights = weightRepository.findUnSyncedBeforeTime(GMConstants.WEIGHT_SYNC_TIME);
            if (!weights.isEmpty()) {
                for (Weight weight : weights) {

                    //Weight
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put(GMConstants.JsonForm.KEY, "Weight_Kgs");
                    jsonObject.put(GMConstants.JsonForm.OPENMRS_ENTITY, "concept");
                    jsonObject.put(GMConstants.JsonForm.OPENMRS_ENTITY_ID, "5089AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
                    jsonObject.put(GMConstants.JsonForm.OPENMRS_ENTITY_PARENT, "");
                    jsonObject.put(GMConstants.JsonForm.OPENMRS_DATA_TYPE, "decimal");
                    jsonObject.put(GMConstants.JsonForm.VALUE, weight.getKg());

                    //Zscore
                    JSONObject zScoreObject = new JSONObject();
                    zScoreObject.put(GMConstants.JsonForm.KEY, "Z_Score_Weight_Age");
                    zScoreObject.put(GMConstants.JsonForm.OPENMRS_ENTITY, "concept");
                    zScoreObject.put(GMConstants.JsonForm.OPENMRS_ENTITY_ID, "162584AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
                    zScoreObject.put(GMConstants.JsonForm.OPENMRS_ENTITY_PARENT, "");
                    zScoreObject.put(GMConstants.JsonForm.OPENMRS_DATA_TYPE, "calculation");
                    zScoreObject.put(GMConstants.JsonForm.VALUE, weight.getZScore());

                    JSONArray jsonArray = new JSONArray();




                    List<Weight> weightlist = weightRepository.findByEntityId(weight.getBaseEntityId());
                    CommonPersonObject childDetails = VaccinatorApplication.getInstance().context().commonrepository("ec_child").findByBaseEntityId(weight.getBaseEntityId());
                    Map<String,String> details =  detailsRepository.getAllDetailsForClient(childDetails.getCaseId());
                    childDetails.getColumnmaps().putAll(details);
                    String dobString = Utils.getValue(childDetails.getColumnmaps(), PathConstants.KEY.DOB, false);
                    Date dob = null;
                    if (!TextUtils.isEmpty(Utils.getValue(childDetails.getColumnmaps(), PathConstants.KEY.BIRTH_WEIGHT, false))
                            && !TextUtils.isEmpty(dobString)) {
                        DateTime dateTime = new DateTime(dobString);
                        dob = dateTime.toDate();
                        Double birthWeight = Double.valueOf(Utils.getValue(childDetails.getColumnmaps(), PathConstants.KEY.BIRTH_WEIGHT, false));

                        Weight birthweight = new Weight(-1l, null, (float) birthWeight.doubleValue(), dateTime.toDate(), null, null, null, Calendar.getInstance().getTimeInMillis(), null, null, 0);
                        weightlist.add(birthweight);
                    }
                    HashMap<Long, Weight> weightHashMap = new HashMap<>();
                    for (Weight curWeight : weightlist) {
                        if (curWeight.getDate() != null) {
                            Calendar curCalendar = Calendar.getInstance();
                            curCalendar.setTime(curWeight.getDate());
                            standardiseCalendarDate(curCalendar);

                            if (!weightHashMap.containsKey(curCalendar.getTimeInMillis())) {
                                weightHashMap.put(curCalendar.getTimeInMillis(), curWeight);
                            } else if (curWeight.getUpdatedAt() > weightHashMap.get(curCalendar.getTimeInMillis()).getUpdatedAt()) {
                                weightHashMap.put(curCalendar.getTimeInMillis(), curWeight);
                            }
                        }
                    }

                    List<Long> keys = new ArrayList<>(weightHashMap.keySet());
                    Collections.sort(keys, Collections.<Long>reverseOrder());

                    List<Weight> result = new ArrayList<>();
                    for (Long curKey : keys) {
                        result.add(weightHashMap.get(curKey));
                    }

                    weightlist = result;

                    int currentweightindex = -1;
                    for(int i = weightlist.size()-1;i>-1;i--){
                        if(weight.getId().equals(weightlist.get(i).getId())){
                            currentweightindex = i;
                        }
                    }
                    if(currentweightindex<(weightlist.size()-1)&&currentweightindex!=-1) {
                        Weight previousWeight = weightlist.get(currentweightindex + 1);



                        JSONObject previousWeightObject = new JSONObject();
                        previousWeightObject.put(GMConstants.JsonForm.KEY, "previous_weight");
                        previousWeightObject.put(GMConstants.JsonForm.OPENMRS_ENTITY, "");
                        previousWeightObject.put(GMConstants.JsonForm.OPENMRS_ENTITY_ID, "");
                        previousWeightObject.put(GMConstants.JsonForm.OPENMRS_ENTITY_PARENT, "");
                        previousWeightObject.put(GMConstants.JsonForm.OPENMRS_DATA_TYPE, "");
                        previousWeightObject.put(GMConstants.JsonForm.VALUE, previousWeight.getDate());
                        String previousweightdatetimestamp ="";
                        if(previousWeight.getId()!=-1) {
                            Cursor previousweightcursor = commonRepository.rawCustomQueryForAdapter("select date from weights where _id = '" + previousWeight.getId() + "'");
                            previousweightcursor.moveToFirst();
                            previousweightdatetimestamp = previousweightcursor.getString(0);
                            previousweightcursor.close();
                        }else{
                            previousweightdatetimestamp = ""+dob.getTime();
                        }

                        JSONObject previousWeightDateObject = new JSONObject();
                        previousWeightDateObject.put(GMConstants.JsonForm.KEY, "previous_weight_date");
                        previousWeightDateObject.put(GMConstants.JsonForm.OPENMRS_ENTITY, "");
                        previousWeightDateObject.put(GMConstants.JsonForm.OPENMRS_ENTITY_ID, "");
                        previousWeightDateObject.put(GMConstants.JsonForm.OPENMRS_ENTITY_PARENT, "");
                        previousWeightDateObject.put(GMConstants.JsonForm.OPENMRS_DATA_TYPE, "");
                        previousWeightDateObject.put(GMConstants.JsonForm.VALUE,    previousweightdatetimestamp);

                        jsonArray.put(previousWeightObject);
                        jsonArray.put(previousWeightDateObject);
                    }


                    jsonArray.put(jsonObject);
                    jsonArray.put(zScoreObject);

                    JsonFormUtils.createWeightEvent(getApplicationContext(), weight, EVENT_TYPE, ENTITY_TYPE, jsonArray);
                    if (weight.getBaseEntityId() == null || weight.getBaseEntityId().isEmpty()) {
                        JsonFormUtils.createWeightEvent(getApplicationContext(), weight, EVENT_TYPE_OUT_OF_CATCHMENT, ENTITY_TYPE, jsonArray);
                    }
                    weightRepository.close(weight.getId());
                }
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        weightRepository = GrowthMonitoringLibrary.getInstance().weightRepository();
        return super.onStartCommand(intent, flags, startId);
    }
}
