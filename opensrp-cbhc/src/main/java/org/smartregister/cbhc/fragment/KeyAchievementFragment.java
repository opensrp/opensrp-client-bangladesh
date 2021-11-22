package org.smartregister.cbhc.fragment;

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

import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.opensrp.api.constants.Gender;
import org.smartregister.cbhc.R;
import org.smartregister.cbhc.adapter.KeyAchievementCardAdapter;
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

public class KeyAchievementFragment extends Fragment {
 public static SimpleDateFormat sql_lite_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    public static SimpleDateFormat axis_DATE_FORMAT = new SimpleDateFormat("MMM");

 static final String TAG = WeightRepository.class.getCanonicalName();

 WeightRepository weightRepository;
 CommonRepository commonRepository;
 DetailsRepository detailRepository;
 private RecyclerView recyclerView;
 private KeyAchievementCardAdapter adapter;
 private ArrayList<Drawable> iconList;
 private ArrayList<String> titleList;
 private ArrayList<String> counts;

 public KeyAchievementFragment(){
     //weightRepository = AncApplication.getInstance().weightRepository();
     commonRepository = AncApplication.getInstance().getContext().commonrepository("ec_child");
     detailRepository = AncApplication.getInstance().getContext().detailsRepository();
 }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.key_achievement_detail, container, false);
        recyclerView = (RecyclerView)view.findViewById(R.id.recycler_view);

        iconList = new ArrayList<>();
        titleList = new ArrayList<>();
        counts = new ArrayList<>();
        adapter = new KeyAchievementCardAdapter(getActivity(), iconList, titleList, counts);

        RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(getActivity(), 2);
        recyclerView.setLayoutManager(mLayoutManager);;
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addItemDecoration(new GridSpacingItemDecoration(2, dpToPx(2), true));
        recyclerView.setAdapter(adapter);
        workwithadapter();
        return view;
    }

    private void workwithadapter() {
        recyclerView.removeAllViews();
        titleList.removeAll(titleList);
        counts.removeAll(counts);
        iconList.removeAll(iconList);
        titleList.add(getResources().getString(R.string.total_registered_child));
        iconList.add(getResources().getDrawable(R.drawable.key_achievement_children));
        try {
            counts.add(totalchildregister(sql_lite_DATE_FORMAT.format((new DateTime()).plusDays(1).toDate())));
        } catch (Exception e) {
            counts.add("N/A");
            e.printStackTrace();
        }

        titleList.add(getResources().getString(R.string.total_registered_prg_wmn));
        iconList.add(getResources().getDrawable(R.drawable.pregnant_woman));
        try {
            counts.add(totalpregnantWoman());
        } catch (Exception e) {
            counts.add("N/A");
            e.printStackTrace();
        }

        titleList.add("% of children who are being reached");
        iconList.add(getResources().getDrawable(R.drawable.male_child_cbhc));
        try {
            counts.add(totalChildrenCovered());
        } catch (Exception e) {
            counts.add("N/A");
            e.printStackTrace();
        }

        titleList.add("% of children who are growth faltering");
        iconList.add(getResources().getDrawable(R.drawable.male_child_cbhc));
        try {
            counts.add(""+getWeightOfCertainMonth(sql_lite_DATE_FORMAT.format((new DateTime()).plusDays(1).toDate())));
        } catch (Exception e) {
            counts.add("N/A");
            e.printStackTrace();
        }

        titleList.add("% of the Woman being Reached");
        iconList.add(getResources().getDrawable(R.drawable.woman_reaching));
        try {
            counts.add(totalWomanReached());
        } catch (Exception e) {
            counts.add("N/A");
            e.printStackTrace();
        }

        adapter.notifyDataSetChanged();

    }

    private int dpToPx(int dp) {
      Resources r = getResources();
      return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics()));
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
        DateTime birthDateTime = null;
        if(child!=null) {
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

    private List<Weight> readAllWeights(Cursor cursor) {
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
        Gender gender =   Gender.valueOf(getValue(childDetails.getColumnmaps(), DBConstants.KEY.GENDER, true).toUpperCase());


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
    }

    public static boolean checkWeighGainVelocity(Weight weight, Weight previouseWeight, int age_when_weight_taken, int monthLastWeightTaken, Gender gender) {
        boolean check = true;
        Float weightDifference = weight.getKg() - previouseWeight.getKg();
        Float weightDifferenceInGrams = weightDifference*1000;
        if(age_when_weight_taken == 1 && monthLastWeightTaken == 0 ){
            if(gender.MALE == gender){
                if(weightDifferenceInGrams< 805){
                    check = false;
                }
            }else if (gender.FEMALE == gender){
                if(weightDifferenceInGrams< 697){
                    check = false;
                }
            }
        }
        if(age_when_weight_taken == 2 && monthLastWeightTaken == 0 ){
            if(gender.MALE == gender){
                if(weightDifferenceInGrams< 1890){
                    check = false;
                }
            }else if (gender.FEMALE == gender){
                if(weightDifferenceInGrams< 1604){
                    check = false;
                }
            }
        }
        if(age_when_weight_taken == 2 && monthLastWeightTaken == 1 ){
            if(gender.MALE == gender){
                if(weightDifferenceInGrams< 992){
                    check = false;
                }
            }else if (gender.FEMALE == gender){
                if(weightDifferenceInGrams< 829){
                    check = false;
                }
            }
        }
        if(age_when_weight_taken == 3 && monthLastWeightTaken == 0 ){
            if(gender.MALE == gender){
                if(weightDifferenceInGrams< 2608){
                    check = false;
                }
            }else if (gender.FEMALE == gender){
                if(weightDifferenceInGrams< 2247){
                    check = false;
                }
            }
        }
        if(age_when_weight_taken == 3 && monthLastWeightTaken == 1 ){
            if(gender.MALE == gender){
                if(weightDifferenceInGrams< 1701){
                    check = false;
                }
            }else if (gender.FEMALE == gender){
                if(weightDifferenceInGrams< 1450){
                    check = false;
                }
            }
        }
        if(age_when_weight_taken == 3 && monthLastWeightTaken == 2 ){
            if(gender.MALE == gender){
                if(weightDifferenceInGrams< 658){
                    check = false;
                }
            }else if (gender.FEMALE == gender){
                if(weightDifferenceInGrams< 571){
                    check = false;
                }
            }
        }
        if(age_when_weight_taken == 4 && monthLastWeightTaken == 0 ){
            if(gender.MALE == gender){
                if(weightDifferenceInGrams< 3204){
                    check = false;
                }
            }else if (gender.FEMALE == gender){
                if(weightDifferenceInGrams< 2806){
                    check = false;
                }
            }
        }
        if(age_when_weight_taken == 4 && monthLastWeightTaken == 1 ){
            if(gender.MALE == gender){
                if(weightDifferenceInGrams< 2214){
                    check = false;
                }
            }else if (gender.FEMALE == gender){
                if(weightDifferenceInGrams< 1941){
                    check = false;
                }
            }
        }
        if(age_when_weight_taken == 4 && monthLastWeightTaken == 2 ){
            if(gender.MALE == gender){
                if(weightDifferenceInGrams< 1202){
                    check = false;
                }
            }else if (gender.FEMALE == gender){
                if(weightDifferenceInGrams< 1088){
                    check = false;
                }
            }
        }
        if(age_when_weight_taken == 4 && monthLastWeightTaken == 3 ){
            if(gender.MALE == gender){
                if(weightDifferenceInGrams< 476){
                    check = false;
                }
            }else if (gender.FEMALE == gender){
                if(weightDifferenceInGrams< 448){
                    check = false;
                }
            }
        }
        if(age_when_weight_taken == 5 && monthLastWeightTaken <= 1 ){
            if(gender.MALE == gender){
                if(weightDifferenceInGrams< 2706){
                    check = false;
                }
            }else if (gender.FEMALE == gender){
                if(weightDifferenceInGrams< 2379){
                    check = false;
                }
            }
        }
        if(age_when_weight_taken == 5 && monthLastWeightTaken == 2 ){
            if(gender.MALE == gender){
                if(weightDifferenceInGrams< 1702){
                    check = false;
                }
            }else if (gender.FEMALE == gender){
                if(weightDifferenceInGrams< 1545){
                    check = false;
                }
            }
        }
        if(age_when_weight_taken == 5 && monthLastWeightTaken == 3 ){
            if(gender.MALE == gender){
                if(weightDifferenceInGrams< 930){
                    check = false;
                }
            }else if (gender.FEMALE == gender){
                if(weightDifferenceInGrams< 874){
                    check = false;
                }
            }
        }
        if(age_when_weight_taken == 5 && monthLastWeightTaken == 4 ){
            if(gender.MALE == gender){
                if(weightDifferenceInGrams< 383){
                    check = false;
                }
            }else if (gender.FEMALE == gender){
                if(weightDifferenceInGrams< 355){
                    check = false;
                }
            }
        }
        if(age_when_weight_taken == 6 && monthLastWeightTaken == 0 ){
            if(gender.MALE == gender){
                if(weightDifferenceInGrams< 4072){
                    check = false;
                }
            }else if (gender.FEMALE == gender){
                if(weightDifferenceInGrams< 3620){
                    check = false;
                }
            }
        }
        if(age_when_weight_taken == 6 && monthLastWeightTaken == 2 ){
            if(gender.MALE == gender){
                if(weightDifferenceInGrams< 2038){
                    check = false;
                }
            }else if (gender.FEMALE == gender){
                if(weightDifferenceInGrams< 1883){
                    check = false;
                }
            }
        }
        if(age_when_weight_taken == 6 && monthLastWeightTaken == 3 ){
            if(gender.MALE == gender){
                if(weightDifferenceInGrams< 1307){
                    check = false;
                }
            }else if (gender.FEMALE == gender){
                if(weightDifferenceInGrams< 1229){
                    check = false;
                }
            }
        }
        if(age_when_weight_taken == 6 && monthLastWeightTaken == 4 ){
            if(gender.MALE == gender){
                if(weightDifferenceInGrams< 738){
                    check = false;
                }
            }else if (gender.FEMALE == gender){
                if(weightDifferenceInGrams< 695){
                    check = false;
                }
            }
        }
        if(age_when_weight_taken == 6 && monthLastWeightTaken == 5 ){
            if(gender.MALE == gender){
                if(weightDifferenceInGrams< 287){
                    check = false;
                }
            }else if (gender.FEMALE == gender){
                if(weightDifferenceInGrams< 271){
                    check = false;
                }
            }
        }
        if(age_when_weight_taken == 7 && monthLastWeightTaken == 6 ){
            if(gender.MALE == gender){
                if(weightDifferenceInGrams< 223){
                    check = false;
                }
            }else if (gender.FEMALE == gender){
                if(weightDifferenceInGrams< 214){
                    check = false;
                }
            }
        }
        if(age_when_weight_taken == 7 && monthLastWeightTaken == 5 ){
            if(gender.MALE == gender){
                if(weightDifferenceInGrams< 585){
                    check = false;
                }
            }else if (gender.FEMALE == gender){
                if(weightDifferenceInGrams< 560){
                    check = false;
                }
            }
        }
        if(age_when_weight_taken == 7 && monthLastWeightTaken == 4 ){
            if(gender.MALE == gender){
                if(weightDifferenceInGrams< 1038){
                    check = false;
                }
            }else if (gender.FEMALE == gender){
                if(weightDifferenceInGrams< 995){
                    check = false;
                }
            }
        }
        if(age_when_weight_taken == 7 && monthLastWeightTaken == 3 ){
            if(gender.MALE == gender){
                if(weightDifferenceInGrams< 1602){
                    check = false;
                }
            }else if (gender.FEMALE == gender){
                if(weightDifferenceInGrams< 1522){
                    check = false;
                }
            }
        }
        if(age_when_weight_taken == 7 && monthLastWeightTaken == 1 ){
            if(gender.MALE == gender){
                if(weightDifferenceInGrams< 3406){
                    check = false;
                }
            }else if (gender.FEMALE == gender){
                if(weightDifferenceInGrams< 3033){
                    check = false;
                }
            }
        }
        if(age_when_weight_taken == 8 && monthLastWeightTaken == 7 ){
            if(gender.MALE == gender){
                if(weightDifferenceInGrams< 181){
                    check = false;
                }
            }else if (gender.FEMALE == gender){
                if(weightDifferenceInGrams< 178){
                    check = false;
                }
            }
        }
        if(age_when_weight_taken == 8 && monthLastWeightTaken == 6 ){
            if(gender.MALE == gender){
                if(weightDifferenceInGrams< 486){
                    check = false;
                }
            }else if (gender.FEMALE == gender){
                if(weightDifferenceInGrams< 469){
                    check = false;
                }
            }
        }
        if(age_when_weight_taken == 8 && monthLastWeightTaken == 5 ){
            if(gender.MALE == gender){
                if(weightDifferenceInGrams< 859){
                    check = false;
                }
            }else if (gender.FEMALE == gender){
                if(weightDifferenceInGrams< 825){
                    check = false;
                }
            }
        }
        if(age_when_weight_taken == 8 && monthLastWeightTaken == 4 ){
            if(gender.MALE == gender){
                if(weightDifferenceInGrams< 1312){
                    check = false;
                }
            }else if (gender.FEMALE == gender){
                if(weightDifferenceInGrams< 1248){
                    check = false;
                }
            }
        }
        if(age_when_weight_taken == 8 && monthLastWeightTaken == 2 ){
            if(gender.MALE == gender){
                if(weightDifferenceInGrams< 2662){
                    check = false;
                }
            }else if (gender.FEMALE == gender){
                if(weightDifferenceInGrams< 2480){
                    check = false;
                }
            }
        }
        if(age_when_weight_taken == 9 && monthLastWeightTaken == 8 ){
            if(gender.MALE == gender){
                if(weightDifferenceInGrams< 148){
                    check = false;
                }
            }else if (gender.FEMALE == gender){
                if(weightDifferenceInGrams< 139){
                    check = false;
                }
            }
        }
        if(age_when_weight_taken == 9 && monthLastWeightTaken == 7 ){
            if(gender.MALE == gender){
                if(weightDifferenceInGrams< 417){
                    check = false;
                }
            }else if (gender.FEMALE == gender){
                if(weightDifferenceInGrams< 399){
                    check = false;
                }
            }
        }
        if(age_when_weight_taken == 9 && monthLastWeightTaken == 6 ){
            if(gender.MALE == gender){
                if(weightDifferenceInGrams< 733){
                    check = false;
                }
            }else if (gender.FEMALE == gender){
                if(weightDifferenceInGrams< 697){
                    check = false;
                }
            }
        }
        if(age_when_weight_taken == 9 && monthLastWeightTaken == 5 ){
            if(gender.MALE == gender){
                if(weightDifferenceInGrams< 1097){
                    check = false;
                }
            }else if (gender.FEMALE == gender){
                if(weightDifferenceInGrams< 1042){
                    check = false;
                }
            }
        }
        if(age_when_weight_taken == 9 && monthLastWeightTaken == 3 ){
            if(gender.MALE == gender){
                if(weightDifferenceInGrams< 2141){
                    check = false;
                }
            }else if (gender.FEMALE == gender){
                if(weightDifferenceInGrams< 2030){
                    check = false;
                }
            }
        }
        if(age_when_weight_taken == 10 && monthLastWeightTaken == 9 ){
            if(gender.MALE == gender){
                if(weightDifferenceInGrams< 120){
                    check = false;
                }
            }else if (gender.FEMALE == gender){
                if(weightDifferenceInGrams< 110){
                    check = false;
                }
            }
        }
        if(age_when_weight_taken == 10 && monthLastWeightTaken == 8 ){
            if(gender.MALE == gender){
                if(weightDifferenceInGrams< 360){
                    check = false;
                }
            }else if (gender.FEMALE == gender){
                if(weightDifferenceInGrams< 336){
                    check = false;
                }
            }
        }
        if(age_when_weight_taken == 10 && monthLastWeightTaken == 7 ){
            if(gender.MALE == gender){
                if(weightDifferenceInGrams< 639){
                    check = false;
                }
            }else if (gender.FEMALE == gender){
                if(weightDifferenceInGrams< 598){
                    check = false;
                }
            }
        }
        if(age_when_weight_taken == 10 && monthLastWeightTaken == 6 ){
            if(gender.MALE == gender){
                if(weightDifferenceInGrams< 945){
                    check = false;
                }
            }else if (gender.FEMALE == gender){
                if(weightDifferenceInGrams< 891){
                    check = false;
                }
            }
        }
        if(age_when_weight_taken == 10 && monthLastWeightTaken == 4 ){
            if(gender.MALE == gender){
                if(weightDifferenceInGrams< 1789){
                    check = false;
                }
            }else if (gender.FEMALE == gender){
                if(weightDifferenceInGrams< 1692){
                    check = false;
                }
            }
        }
        if(age_when_weight_taken == 11 && monthLastWeightTaken == 10 ){
            if(gender.MALE == gender){
                if(weightDifferenceInGrams< 100){
                    check = false;
                }
            }else if (gender.FEMALE == gender){
                if(weightDifferenceInGrams< 95){
                    check = false;
                }
            }
        }
        if(age_when_weight_taken == 11 && monthLastWeightTaken == 9 ){
            if(gender.MALE == gender){
                if(weightDifferenceInGrams< 315){
                    check = false;
                }
            }else if (gender.FEMALE == gender){
                if(weightDifferenceInGrams< 297){
                    check = false;
                }
            }
        }
        if(age_when_weight_taken == 11 && monthLastWeightTaken == 8 ){
            if(gender.MALE == gender){
                if(weightDifferenceInGrams< 567){
                    check = false;
                }
            }else if (gender.FEMALE == gender){
                if(weightDifferenceInGrams< 528){
                    check = false;
                }
            }
        }
        if(age_when_weight_taken == 11 && monthLastWeightTaken == 7 ){
            if(gender.MALE == gender){
                if(weightDifferenceInGrams< 832){
                    check = false;
                }
            }else if (gender.FEMALE == gender){
                if(weightDifferenceInGrams< 785){
                    check = false;
                }
            }
        }
        if(age_when_weight_taken == 11 && monthLastWeightTaken == 5 ){
            if(gender.MALE == gender){
                if(weightDifferenceInGrams< 1524){
                    check = false;
                }
            }else if (gender.FEMALE == gender){
                if(weightDifferenceInGrams< 1446){
                    check = false;
                }
            }
        }
        if(age_when_weight_taken == 12 && monthLastWeightTaken == 11 ){
            if(gender.MALE == gender){
                if(weightDifferenceInGrams< 91){
                    check = false;
                }
            }else if (gender.FEMALE == gender){
                if(weightDifferenceInGrams< 88){
                    check = false;
                }
            }
        }
        if(age_when_weight_taken == 12 && monthLastWeightTaken == 10 ){
            if(gender.MALE == gender){
                if(weightDifferenceInGrams< 286){
                    check = false;
                }
            }else if (gender.FEMALE == gender){
                if(weightDifferenceInGrams< 274){
                    check = false;
                }
            }
        }
        if(age_when_weight_taken == 12 && monthLastWeightTaken == 9 ){
            if(gender.MALE == gender){
                if(weightDifferenceInGrams< 509){
                    check = false;
                }
            }else if (gender.FEMALE == gender){
                if(weightDifferenceInGrams< 481){
                    check = false;
                }
            }
        }
        if(age_when_weight_taken == 12 && monthLastWeightTaken == 8 ){
            if(gender.MALE == gender){
                if(weightDifferenceInGrams< 757){
                    check = false;
                }
            }else if (gender.FEMALE == gender){
                if(weightDifferenceInGrams< 712){
                    check = false;
                }
            }
        }
        if(age_when_weight_taken == 12 && monthLastWeightTaken == 6 ){
            if(gender.MALE == gender){
                if(weightDifferenceInGrams< 1346){
                    check = false;
                }
            }else if (gender.FEMALE == gender){
                if(weightDifferenceInGrams< 1271){
                    check = false;
                }
            }
        }
        if(age_when_weight_taken == 13 && monthLastWeightTaken == 11 ){
            if(gender.MALE == gender){
                if(weightDifferenceInGrams< 260){
                    check = false;
                }
            }else if (gender.FEMALE == gender){
                if(weightDifferenceInGrams< 254){
                    check = false;
                }
            }
        }
        if(age_when_weight_taken == 13 && monthLastWeightTaken == 10 ){
            if(gender.MALE == gender){
                if(weightDifferenceInGrams< 465){
                    check = false;
                }
            }else if (gender.FEMALE == gender){
                if(weightDifferenceInGrams< 451){
                    check = false;
                }
            }
        }
        if(age_when_weight_taken == 13 && monthLastWeightTaken == 9 ){
            if(gender.MALE == gender){
                if(weightDifferenceInGrams< 700){
                    check = false;
                }
            }else if (gender.FEMALE == gender){
                if(weightDifferenceInGrams< 659){
                    check = false;
                }
            }
        }
        if(age_when_weight_taken == 13 && monthLastWeightTaken == 7 ){
            if(gender.MALE == gender){
                if(weightDifferenceInGrams< 1212){
                    check = false;
                }
            }else if (gender.FEMALE == gender){
                if(weightDifferenceInGrams< 1147){
                    check = false;
                }
            }
        }
        if(age_when_weight_taken == 14 && monthLastWeightTaken == 12 ){
            if(gender.MALE == gender){
                if(weightDifferenceInGrams< 236){
                    check = false;
                }
            }else if (gender.FEMALE == gender){
                if(weightDifferenceInGrams< 238){
                    check = false;
                }
            }
        }
        if(age_when_weight_taken == 14 && monthLastWeightTaken == 11 ){
            if(gender.MALE == gender){
                if(weightDifferenceInGrams< 430){
                    check = false;
                }
            }else if (gender.FEMALE == gender){
                if(weightDifferenceInGrams< 429){
                    check = false;
                }
            }
        }
        if(age_when_weight_taken == 14 && monthLastWeightTaken == 11 ){
            if(gender.MALE == gender){
                if(weightDifferenceInGrams< 648){
                    check = false;
                }
            }else if (gender.FEMALE == gender){
                if(weightDifferenceInGrams< 624){
                    check = false;
                }
            }
        }
        if(age_when_weight_taken == 14 && monthLastWeightTaken == 8 ){
            if(gender.MALE == gender){
                if(weightDifferenceInGrams< 1106){
                    check = false;
                }
            }else if (gender.FEMALE == gender){
                if(weightDifferenceInGrams< 1063){
                    check = false;
                }
            }
        }
        if(age_when_weight_taken == 15 && monthLastWeightTaken == 13 ){
            if(gender.MALE == gender){
                if(weightDifferenceInGrams< 212){
                    check = false;
                }
            }else if (gender.FEMALE == gender){
                if(weightDifferenceInGrams< 227){
                    check = false;
                }
            }
        }
        if(age_when_weight_taken == 15 && monthLastWeightTaken == 12 ){
            if(gender.MALE == gender){
                if(weightDifferenceInGrams< 404){
                    check = false;
                }
            }else if (gender.FEMALE == gender){
                if(weightDifferenceInGrams< 413){
                    check = false;
                }
            }
        }
        if(age_when_weight_taken == 15 && monthLastWeightTaken == 11 ){
            if(gender.MALE == gender){
                if(weightDifferenceInGrams< 598){
                    check = false;
                }
            }else if (gender.FEMALE == gender){
                if(weightDifferenceInGrams< 601){
                    check = false;
                }
            }
        }
        if(age_when_weight_taken == 15 && monthLastWeightTaken == 9 ){
            if(gender.MALE == gender){
                if(weightDifferenceInGrams< 1024){
                    check = false;
                }
            }else if (gender.FEMALE == gender){
                if(weightDifferenceInGrams< 1009){
                    check = false;
                }
            }
        }
        if(age_when_weight_taken == 16 && monthLastWeightTaken == 14 ){
            if(gender.MALE == gender){
                if(weightDifferenceInGrams< 197){
                    check = false;
                }
            }else if (gender.FEMALE == gender){
                if(weightDifferenceInGrams< 220){
                    check = false;
                }
            }
        }
        if(age_when_weight_taken == 16 && monthLastWeightTaken == 13 ){
            if(gender.MALE == gender){
                if(weightDifferenceInGrams< 385){
                    check = false;
                }
            }else if (gender.FEMALE == gender){
                if(weightDifferenceInGrams< 403){
                    check = false;
                }
            }
        }
        if(age_when_weight_taken == 16 && monthLastWeightTaken == 12 ){
            if(gender.MALE == gender){
                if(weightDifferenceInGrams< 564){
                    check = false;
                }
            }else if (gender.FEMALE == gender){
                if(weightDifferenceInGrams< 586){
                    check = false;
                }
            }
        }
        if(age_when_weight_taken == 16 && monthLastWeightTaken == 10 ){
            if(gender.MALE == gender){
                if(weightDifferenceInGrams< 970){
                    check = false;
                }
            }else if (gender.FEMALE == gender){
                if(weightDifferenceInGrams< 975){
                    check = false;
                }
            }
        }
        if(age_when_weight_taken == 17 && monthLastWeightTaken == 15 ){
            if(gender.MALE == gender){
                if(weightDifferenceInGrams< 193){
                    check = false;
                }
            }else if (gender.FEMALE == gender){
                if(weightDifferenceInGrams< 216){
                    check = false;
                }
            }
        }
        if(age_when_weight_taken == 17 && monthLastWeightTaken == 14 ){
            if(gender.MALE == gender){
                if(weightDifferenceInGrams< 372){
                    check = false;
                }
            }else if (gender.FEMALE == gender){
                if(weightDifferenceInGrams< 397){
                    check = false;
                }
            }
        }
        if(age_when_weight_taken == 17 && monthLastWeightTaken == 13 ){
            if(gender.MALE == gender){
                if(weightDifferenceInGrams< 550){
                    check = false;
                }
            }else if (gender.FEMALE == gender){
                if(weightDifferenceInGrams< 578){
                    check = false;
                }
            }
        }
        if(age_when_weight_taken == 17 && monthLastWeightTaken == 11 ){
            if(gender.MALE == gender){
                if(weightDifferenceInGrams< 937){
                    check = false;
                }
            }else if (gender.FEMALE == gender){
                if(weightDifferenceInGrams< 956){
                    check = false;
                }
            }
        }
        if(age_when_weight_taken == 18 && monthLastWeightTaken == 16 ){
            if(gender.MALE == gender){
                if(weightDifferenceInGrams< 192){
                    check = false;
                }
            }else if (gender.FEMALE == gender){
                if(weightDifferenceInGrams< 212){
                    check = false;
                }
            }
        }
        if(age_when_weight_taken == 18 && monthLastWeightTaken == 15 ){
            if(gender.MALE == gender){
                if(weightDifferenceInGrams< 362){
                    check = false;
                }
            }else if (gender.FEMALE == gender){
                if(weightDifferenceInGrams< 392){
                    check = false;
                }
            }
        }
        if(age_when_weight_taken == 18 && monthLastWeightTaken == 14 ){
            if(gender.MALE == gender){
                if(weightDifferenceInGrams< 543){
                    check = false;
                }
            }else if (gender.FEMALE == gender){
                if(weightDifferenceInGrams< 572){
                    check = false;
                }
            }
        }
        if(age_when_weight_taken == 18 && monthLastWeightTaken == 12 ){
            if(gender.MALE == gender){
                if(weightDifferenceInGrams< 914){
                    check = false;
                }
            }else if (gender.FEMALE == gender){
                if(weightDifferenceInGrams< 942){
                    check = false;
                }
            }
        }
        if(age_when_weight_taken == 19 && monthLastWeightTaken == 17 ){
            if(gender.MALE == gender){
                if(weightDifferenceInGrams< 188){
                    check = false;
                }
            }else if (gender.FEMALE == gender){
                if(weightDifferenceInGrams< 205){
                    check = false;
                }
            }
        }
        if(age_when_weight_taken == 19 && monthLastWeightTaken == 16 ){
            if(gender.MALE == gender){
                if(weightDifferenceInGrams< 355){
                    check = false;
                }
            }else if (gender.FEMALE == gender){
                if(weightDifferenceInGrams< 385){
                    check = false;
                }
            }
        }
        if(age_when_weight_taken == 19 && monthLastWeightTaken == 15 ){
            if(gender.MALE == gender){
                if(weightDifferenceInGrams< 537){
                    check = false;
                }
            }else if (gender.FEMALE == gender){
                if(weightDifferenceInGrams< 565){
                    check = false;
                }
            }
        }
        if(age_when_weight_taken == 19 && monthLastWeightTaken == 13 ){
            if(gender.MALE == gender){
                if(weightDifferenceInGrams< 898){
                    check = false;
                }
            }else if (gender.FEMALE == gender){
                if(weightDifferenceInGrams< 931){
                    check = false;
                }
            }
        }
        if(age_when_weight_taken == 20 && monthLastWeightTaken == 18 ){
            if(gender.MALE == gender){
                if(weightDifferenceInGrams< 182){
                    check = false;
                }
            }else if (gender.FEMALE == gender){
                if(weightDifferenceInGrams< 196){
                    check = false;
                }
            }
        }
        if(age_when_weight_taken == 20 && monthLastWeightTaken == 17 ){
            if(gender.MALE == gender){
                if(weightDifferenceInGrams< 351){
                    check = false;
                }
            }else if (gender.FEMALE == gender){
                if(weightDifferenceInGrams< 376){
                    check = false;
                }
            }
        }
        if(age_when_weight_taken == 20 && monthLastWeightTaken == 16 ){
            if(gender.MALE == gender){
                if(weightDifferenceInGrams< 529){
                    check = false;
                }
            }else if (gender.FEMALE == gender){
                if(weightDifferenceInGrams< 556){
                    check = false;
                }
            }
        }
        if(age_when_weight_taken == 20 && monthLastWeightTaken == 14 ){
            if(gender.MALE == gender){
                if(weightDifferenceInGrams< 887){
                    check = false;
                }
            }else if (gender.FEMALE == gender){
                if(weightDifferenceInGrams< 920){
                    check = false;
                }
            }
        }
        if(age_when_weight_taken == 21 && monthLastWeightTaken == 19 ){
            if(gender.MALE == gender){
                if(weightDifferenceInGrams< 176){
                    check = false;
                }
            }else if (gender.FEMALE == gender){
                if(weightDifferenceInGrams< 188){
                    check = false;
                }
            }
        }
        if(age_when_weight_taken == 21 && monthLastWeightTaken == 18 ){
            if(gender.MALE == gender){
                if(weightDifferenceInGrams< 347){
                    check = false;
                }
            }else if (gender.FEMALE == gender){
                if(weightDifferenceInGrams< 366){
                    check = false;
                }
            }
        }
        if(age_when_weight_taken == 21 && monthLastWeightTaken == 17 ){
            if(gender.MALE == gender){
                if(weightDifferenceInGrams< 519){
                    check = false;
                }
            }else if (gender.FEMALE == gender){
                if(weightDifferenceInGrams< 546){
                    check = false;
                }
            }
        }
        if(age_when_weight_taken == 21 && monthLastWeightTaken == 15 ){
            if(gender.MALE == gender){
                if(weightDifferenceInGrams< 880){
                    check = false;
                }
            }else if (gender.FEMALE == gender){
                if(weightDifferenceInGrams< 908){
                    check = false;
                }
            }
        }
        if(age_when_weight_taken == 22 && monthLastWeightTaken == 20 ){
            if(gender.MALE == gender){
                if(weightDifferenceInGrams< 171){
                    check = false;
                }
            }else if (gender.FEMALE == gender){
                if(weightDifferenceInGrams< 178){
                    check = false;
                }
            }
        }
        if(age_when_weight_taken == 22 && monthLastWeightTaken == 19 ){
            if(gender.MALE == gender){
                if(weightDifferenceInGrams< 342){
                    check = false;
                }
            }else if (gender.FEMALE == gender){
                if(weightDifferenceInGrams< 354){
                    check = false;
                }
            }
        }
        if(age_when_weight_taken == 22 && monthLastWeightTaken == 18 ){
            if(gender.MALE == gender){
                if(weightDifferenceInGrams< 508){
                    check = false;
                }
            }else if (gender.FEMALE == gender){
                if(weightDifferenceInGrams< 532){
                    check = false;
                }
            }
        }
        if(age_when_weight_taken == 22 && monthLastWeightTaken == 16 ){
            if(gender.MALE == gender){
                if(weightDifferenceInGrams< 872){
                    check = false;
                }
            }else if (gender.FEMALE == gender){
                if(weightDifferenceInGrams< 894){
                    check = false;
                }
            }
        }
        if(age_when_weight_taken == 23 && monthLastWeightTaken == 21 ){
            if(gender.MALE == gender){
                if(weightDifferenceInGrams< 167){
                    check = false;
                }
            }else if (gender.FEMALE == gender){
                if(weightDifferenceInGrams< 164){
                    check = false;
                }
            }
        }
        if(age_when_weight_taken == 23 && monthLastWeightTaken == 20 ){
            if(gender.MALE == gender){
                if(weightDifferenceInGrams< 334){
                    check = false;
                }
            }else if (gender.FEMALE == gender){
                if(weightDifferenceInGrams< 340){
                    check = false;
                }
            }
        }
        if(age_when_weight_taken == 23 && monthLastWeightTaken == 19){
            if(gender.MALE == gender){
                if(weightDifferenceInGrams< 501){
                    check = false;
                }
            }else if (gender.FEMALE == gender){
                if(weightDifferenceInGrams< 517){
                    check = false;
                }
            }
        }
        if(age_when_weight_taken == 23 && monthLastWeightTaken == 17 ){
            if(gender.MALE == gender){
                if(weightDifferenceInGrams< 863){
                    check = false;
                }
            }else if (gender.FEMALE == gender){
                if(weightDifferenceInGrams< 876){
                    check = false;
                }
            }
        }
        if(age_when_weight_taken == 24 && monthLastWeightTaken == 22 ){
            if(gender.MALE == gender){
                if(weightDifferenceInGrams< 164){
                    check = false;
                }
            }else if (gender.FEMALE == gender){
                if(weightDifferenceInGrams< 150){
                    check = false;
                }
            }
        }
        if(age_when_weight_taken == 24 && monthLastWeightTaken == 21 ){
            if(gender.MALE == gender){
                if(weightDifferenceInGrams< 322){
                    check = false;
                }
            }else if (gender.FEMALE == gender){
                if(weightDifferenceInGrams< 326){
                    check = false;
                }
            }
        }
        if(age_when_weight_taken == 24 && monthLastWeightTaken == 20 ){
            if(gender.MALE == gender){
                if(weightDifferenceInGrams< 497){
                    check = false;
                }
            }else if (gender.FEMALE == gender){
                if(weightDifferenceInGrams< 503){
                    check = false;
                }
            }
        }
        if(age_when_weight_taken == 24 && monthLastWeightTaken == 18 ){
            if(gender.MALE == gender){
                if(weightDifferenceInGrams< 855){
                    check = false;
                }
            }else if (gender.FEMALE == gender){
                if(weightDifferenceInGrams< 857){
                    check = false;
                }
            }
        }




        return check;
    }


    public String totalchildregister(String date){
        //Cursor cursor =  commonRepository.rawCustomQueryForAdapter("SELECT count(*)FROM ec_child where date( ec_child.client_reg_date) < '"+date+"'");
        Cursor cursor =  commonRepository.rawCustomQueryForAdapter("SELECT count(*)FROM ec_child");
        cursor.moveToFirst();
        String totalchildregister = cursor.getString(0);
        cursor.close();
        return totalchildregister;
    }

    public String totalpregnantWoman(){
/*
        Cursor cursor =  commonRepository.rawCustomQueryForAdapter("select count(*) from ec_mother where ec_mother.base_entity_id in (select base_entity_id from ec_details where ec_details.value = 'হ্যাঁ' and ec_details.key = 'pregnant')");
*/
        Cursor cursor =  commonRepository.rawCustomQueryForAdapter("select count(*) from ec_woman where ec_woman.PregnancyStatus  = 'Antenatal Period'");
        cursor.moveToFirst();
        String totalpregnantWoman = cursor.getString(0);
        cursor.close();
        return totalpregnantWoman;
    }

    public String totalWomanReached(){
        Cursor cursor =  commonRepository.rawCustomQueryForAdapter("select count(distinct(baseEntityID)) from event where (event.eventType = 'Followup ANC' or event.eventType ='Followup Family Planning' or event.eventType ='Followup PNC') and event.baseEntityId in (Select ec_woman.base_entity_id from ec_woman)");
        cursor.moveToFirst();
        String totalWomanReached = cursor.getString(0);
        cursor.close();

        Cursor cursor2 =  commonRepository.rawCustomQueryForAdapter("Select count(*) from ec_woman");
        cursor2.moveToFirst();
        String totalWoman = cursor2.getString(0);
        cursor2.close();

        try{
            int totalwomanreached = Integer.parseInt(totalWomanReached);
            int totalwoman = Integer.parseInt(totalWoman);
            int percentagereached = (int)Math.round(((double)totalwomanreached/totalwoman)*100);
            return ""+percentagereached;

        }catch (Exception e){
            return "";
        }
    }

    public String totalChildrenCovered(){
        Cursor cursor =  commonRepository.rawCustomQueryForAdapter("select count(distinct(base_entity_id)) from weights");
        cursor.moveToFirst();
        String totalWomanReached = cursor.getString(0);
        cursor.close();

        Cursor cursor2 =  commonRepository.rawCustomQueryForAdapter("Select count(*) from ec_child");
        cursor2.moveToFirst();
        String totalWoman = cursor2.getString(0);
        cursor2.close();

        try{
            int totalChildrenreached = Integer.parseInt(totalWomanReached);
            int totalChild = Integer.parseInt(totalWoman);
            int percentagereached = (int)Math.round(((double)totalChildrenreached/totalChild)*100);
            return ""+percentagereached;

        }catch (Exception e){
            return "";
        }
    }

    public class GridSpacingItemDecoration extends RecyclerView.ItemDecoration {

        private int spanCount;
        private int spacing;
        private boolean includeEdge;

        public GridSpacingItemDecoration(int spanCount, int spacing, boolean includeEdge) {
            this.spanCount = spanCount;
            this.spacing = spacing;
            this.includeEdge = includeEdge;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            int position = parent.getChildAdapterPosition(view); // item position
            int column = position % spanCount; // item column

            if (includeEdge) {
                outRect.left = spacing - column * spacing / spanCount; // spacing - column * ((1f / spanCount) * spacing)
                outRect.right = (column + 1) * spacing / spanCount; // (column + 1) * ((1f / spanCount) * spacing)

                if (position < spanCount) { // top edge
                    outRect.top = spacing;
                }
                outRect.bottom = spacing; // item bottom
            } else {
                outRect.left = column * spacing / spanCount; // column * ((1f / spanCount) * spacing)
                outRect.right = spacing - (column + 1) * spacing / spanCount; // spacing - (column + 1) * ((1f /    spanCount) * spacing)
                if (position >= spanCount) {
                    outRect.top = spacing; // item top
                }
            }
        }
    }

}
