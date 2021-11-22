package org.smartregister.cbhc.fragment;
import static org.smartregister.cbhc.fragment.KeyAchievementFragment.checkWeighGainVelocity;
import static org.smartregister.growthmonitoring.repository.WeightRepository.ANMID;
import static org.smartregister.growthmonitoring.repository.WeightRepository.BASE_ENTITY_ID;
import static org.smartregister.growthmonitoring.repository.WeightRepository.CHILD_LOCATION_ID;
import static org.smartregister.growthmonitoring.repository.WeightRepository.CREATED_AT;
import static org.smartregister.growthmonitoring.repository.WeightRepository.DATE;
import static org.smartregister.growthmonitoring.repository.WeightRepository.DEFAULT_Z_SCORE;
import static org.smartregister.growthmonitoring.repository.WeightRepository.EVENT_ID;
import static org.smartregister.growthmonitoring.repository.WeightRepository.FORMSUBMISSION_ID;
import static org.smartregister.growthmonitoring.repository.WeightRepository.ID_COLUMN;
import static org.smartregister.growthmonitoring.repository.WeightRepository.KG;
import static org.smartregister.growthmonitoring.repository.WeightRepository.LOCATIONID;
import static org.smartregister.growthmonitoring.repository.WeightRepository.OUT_OF_AREA;
import static org.smartregister.growthmonitoring.repository.WeightRepository.PROGRAM_CLIENT_ID;
import static org.smartregister.growthmonitoring.repository.WeightRepository.SYNC_STATUS;
import static org.smartregister.growthmonitoring.repository.WeightRepository.TEAM;
import static org.smartregister.growthmonitoring.repository.WeightRepository.TEAM_ID;
import static org.smartregister.growthmonitoring.repository.WeightRepository.UPDATED_AT_COLUMN;
import static org.smartregister.growthmonitoring.repository.WeightRepository.Z_SCORE;
import static org.smartregister.util.Utils.getValue;

import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.helper.DateAsXAxisLabelFormatter;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.opensrp.api.constants.Gender;
import org.smartregister.cbhc.R;
import org.smartregister.cbhc.application.AncApplication;
import org.smartregister.cbhc.util.DBConstants;
import org.smartregister.cbhc.util.JsonFormUtils;
import org.smartregister.commonregistry.CommonPersonObject;
import org.smartregister.commonregistry.CommonRepository;
import org.smartregister.growthmonitoring.domain.Weight;
import org.smartregister.growthmonitoring.repository.WeightRepository;
import org.smartregister.repository.DetailsRepository;
import org.smartregister.repository.EventClientRepository;
import org.smartregister.util.DateUtil;
import org.smartregister.util.Utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;


public class GrowthFalteringTrendReportFragment extends Fragment {
 public static SimpleDateFormat sql_lite_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    public static SimpleDateFormat axis_DATE_FORMAT = new SimpleDateFormat("MMM");

 static final String TAG = WeightRepository.class.getCanonicalName();

 WeightRepository weightRepository;
 CommonRepository commonRepository;
 DetailsRepository detailRepository;
 public GrowthFalteringTrendReportFragment(){
     //weightRepository = AncApplication.getInstance().weightRepository();
     commonRepository = AncApplication.getInstance().getContext().commonrepository("ec_child");
     detailRepository = AncApplication.getInstance().getContext().detailsRepository();
 }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.growths_faltering_fragment, container, false);
        GraphView graph = (GraphView) view.findViewById(R.id.graph);



//        "2018-07-29");
        DateTime today = new DateTime();
        DateTime firstofNextMonth = today.plusMonths(-11).withDayOfMonth(1);
        DataPoint [] points = new DataPoint[12];
        String [] months = new String[12];
        for(int i =0;i<12;i++){
            points [i] =new DataPoint(firstofNextMonth.plusMonths(i).toDate(),getWeightOfCertainMonth(sql_lite_DATE_FORMAT.format(firstofNextMonth.plusMonths(i).toDate())));
             months[i] = firstofNextMonth.plusMonths(i).toString("MMM");
        }
        LineGraphSeries<DataPoint> series2 = new LineGraphSeries<>(points);
        graph.addSeries(series2);
//        StaticLabelsFormatter staticLabelsFormatter = new StaticLabelsFormatter(graph);
//        staticLabelsFormatter.setHorizontalLabels(months);
//        graph.getGridLabelRenderer().setNumHorizontalLabels(12); // only 4 because of the space
        graph.getGridLabelRenderer().setLabelFormatter(new DateAsXAxisLabelFormatter(getActivity(),axis_DATE_FORMAT));
        graph.getGridLabelRenderer().setNumHorizontalLabels(3); // only 4 because of the space

//        graph.getGridLabelRenderer().setLabelFormatter(staticLabelsFormatter);
        graph.getViewport().setMinY(0);
        graph.getViewport().setMaxY(100);
        graph.getViewport().setYAxisBoundsManual(true);

        graph.getViewport().setXAxisBoundsManual(true);

// as we use dates as labels, the human rounding to nice readable numbers
// is not necessary
        graph.getGridLabelRenderer().setHumanRounding(false);
        return view;
    }

    public int getWeightOfCertainMonth(String date){
//     weightRepository.
       Cursor cursor =  commonRepository.rawCustomQueryForAdapter("SELECT * FROM weights where date( date / 1000, 'unixepoch') < '"+date+"' group by base_entity_id having date = max(date)");
       List <Weight> latest = readAllWeights(cursor);
       int falteredweight = 0;
       for(int i = 0;i<latest.size();i++){
           boolean check = processWeightForGrowthChart(latest.get(i));
           if(!check){
               falteredweight++;
           }
       }
       if(latest.size()>0){
           return (int)Math.round(((double)falteredweight/latest.size())*100);
       }else{
           return 0;
       }


 }

    private boolean processWeightForGrowthChart(Weight weight) {
        Date weightdate =weight.getDate();
        Cursor cursor =  commonRepository.rawCustomQueryForAdapter("SELECT * FROM weights where date( date / 1000, 'unixepoch') < date( "+weightdate.getTime()+"/1000,'unixepoch') and base_entity_id = '"+weight.getBaseEntityId()+"' group by base_entity_id having date = max(date)");
        List <Weight> previousweightlist = readAllWeights(cursor);
        Weight previousWeight = null;
        if(previousweightlist.size()>0){
            previousWeight = previousweightlist.get(0);
        }

        CommonPersonObject child = commonRepository.findByBaseEntityId(weight.getBaseEntityId());
        if(child!=null) {
            DateTime birthDateTime = null;
            String dobString = getValue(child.getColumnmaps(), DBConstants.KEY.DOB, false);
            String durationString = "";
            if (StringUtils.isNotBlank(dobString)) {
                try {
                    birthDateTime = new DateTime(dobString);
                    String duration = DateUtil.getDuration(birthDateTime);
                    if (duration != null) {
                        durationString = duration;
                    }
                } catch (Exception e) {
                    Log.e(getClass().getName(), e.toString(), e);
                }
            }

            boolean check = checkForWeightGainCalc(birthDateTime.toDate(), weight, previousWeight, child, detailRepository);
            return check;
        }else{
            return false;
        }
    }

    public static List<Weight> readAllWeights(Cursor cursor) {
        List<Weight> weights = new ArrayList<>();
        try {
            if (cursor != null && cursor.getCount() > 0 && cursor.moveToFirst()) {
                while (!cursor.isAfterLast()) {
                    Double zScore = cursor.getDouble(cursor.getColumnIndex(Z_SCORE));
                    if (zScore != null && zScore.equals(new Double(DEFAULT_Z_SCORE))) {
                        zScore = null;
                    }

                    Date createdAt = null;
                    String dateCreatedString = cursor.getString(cursor.getColumnIndex(CREATED_AT));
                    if (StringUtils.isNotBlank(dateCreatedString)) {
                        try {
                            createdAt = EventClientRepository.dateFormat.parse(dateCreatedString);
                        } catch (ParseException e) {
                            Log.e(TAG, Log.getStackTraceString(e));
                        }
                    }

                    Weight weight = new Weight(cursor.getLong(cursor.getColumnIndex(ID_COLUMN)),
                            cursor.getString(cursor.getColumnIndex(BASE_ENTITY_ID)),
                            cursor.getString(cursor.getColumnIndex(PROGRAM_CLIENT_ID)),
                            cursor.getFloat(cursor.getColumnIndex(KG)),
                            new Date(cursor.getLong(cursor.getColumnIndex(DATE))),
                            cursor.getString(cursor.getColumnIndex(ANMID)),
                            cursor.getString(cursor.getColumnIndex(LOCATIONID)),
                            cursor.getString(cursor.getColumnIndex(SYNC_STATUS)),
                            cursor.getLong(cursor.getColumnIndex(UPDATED_AT_COLUMN)),
                            cursor.getString(cursor.getColumnIndex(EVENT_ID)),
                            cursor.getString(cursor.getColumnIndex(FORMSUBMISSION_ID)),
                            zScore,
                            cursor.getInt(cursor.getColumnIndex(OUT_OF_AREA)),
                            createdAt);

                    weight.setTeam(cursor.getString(cursor.getColumnIndex(TEAM)));
                    weight.setTeamId(cursor.getString(cursor.getColumnIndex(TEAM_ID)));
                    weight.setChildLocationId(cursor.getString(cursor.getColumnIndex(CHILD_LOCATION_ID)));

                    weights.add(weight);

                    cursor.moveToNext();
                }
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return weights;

    }

    public static boolean checkForWeightGainCalc(Date dob, Weight weight,Weight previousWeight, CommonPersonObject childDetails, DetailsRepository detailsRepository) {
        String dobString = "";
        String formattedAge = "";
        String formattedDob = "";
        int monthLastWeightTaken = 0;
        Map<String, String> detailsMap = detailsRepository.getAllDetailsForClient(childDetails.getCaseId());
        childDetails.getColumnmaps().putAll(detailsMap);

        String genderString = getValue(childDetails.getColumnmaps(), DBConstants.KEY.GENDER, true);

        Gender validGender;
        try{
            validGender = Gender.valueOf(genderString);
        }catch (Exception ex){
            genderString = "UNKNOWN";
        }
        Gender gender =   Gender.valueOf(genderString.toUpperCase());


        formattedDob = JsonFormUtils.DATE_FORMAT.format(dob);
        long timeDiff = Calendar.getInstance().getTimeInMillis() - dob.getTime();

        int age_in_months = (int) Math.floor((float) timeDiff /
                TimeUnit.MILLISECONDS.convert(30, TimeUnit.DAYS));
        DateTime tempweighttime = null;
        if(previousWeight == null) {
            Float birthweight = new Float(getValue(detailsMap, "Birth_Weight", true));
            previousWeight = new Weight();
            previousWeight.setKg(birthweight);
            previousWeight.setDate(dob);
            monthLastWeightTaken = 0;
        }else{
            long timeDiffwhenLastWeightwastaken =  previousWeight.getDate().getTime() - dob.getTime();
            monthLastWeightTaken = (int) Math.round((float) timeDiffwhenLastWeightwastaken /
                    TimeUnit.MILLISECONDS.convert(30, TimeUnit.DAYS));
        }

        long timeDiffwhenWeightwastaken =  weight.getDate().getTime() - dob.getTime();

        int age_when_weight_taken = (int) Math.round((float) timeDiffwhenWeightwastaken /
                TimeUnit.MILLISECONDS.convert(30, TimeUnit.DAYS));

        boolean check = checkWeighGainVelocity(weight,previousWeight,age_when_weight_taken,monthLastWeightTaken,gender);
        return check;
//        net.sqlcipher.database.SQLiteDatabase db = wp.getPathRepository().getReadableDatabase();


    }

}
