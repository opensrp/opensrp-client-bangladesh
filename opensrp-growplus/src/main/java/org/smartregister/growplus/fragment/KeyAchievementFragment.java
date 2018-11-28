package org.smartregister.growplus.fragment;

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

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.helper.DateAsXAxisLabelFormatter;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.opensrp.api.constants.Gender;
import org.smartregister.commonregistry.CommonPersonObject;
import org.smartregister.commonregistry.CommonRepository;
import org.smartregister.growplus.R;
import org.smartregister.growplus.adapter.KeyAchievementCardAdapter;
import org.smartregister.growplus.application.VaccinatorApplication;
import org.smartregister.growthmonitoring.domain.Weight;
import org.smartregister.growthmonitoring.repository.WeightRepository;
import org.smartregister.repository.DetailsRepository;
import org.smartregister.repository.EventClientRepository;
import org.smartregister.util.DateUtil;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import util.PathConstants;

import static com.vijay.jsonwizard.utils.FormUtils.DATE_FORMAT;
import static org.smartregister.growplus.provider.ChildSmartClientsProvider.checkWeighGainVelocity;
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
     weightRepository = VaccinatorApplication.getInstance().weightRepository();
     commonRepository = VaccinatorApplication.getInstance().context().commonrepository("ec_child");
     detailRepository = VaccinatorApplication.getInstance().context().detailsRepository();
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
        titleList.add("Total Child Registered");
        iconList.add(getResources().getDrawable(R.drawable.key_achievement_children));
        try {
            counts.add(totalchildregister(sql_lite_DATE_FORMAT.format((new DateTime()).plusDays(1).toDate())));
        } catch (Exception e) {
            counts.add("N/A");
            e.printStackTrace();
        }

        titleList.add("Total Pregnant Woman Registered");
        iconList.add(getResources().getDrawable(R.drawable.pregnant_woman_key_achievement));
        try {
            counts.add(totalpregnantWoman());
        } catch (Exception e) {
            counts.add("N/A");
            e.printStackTrace();
        }

        titleList.add("% of children who are being reached");
        iconList.add(getResources().getDrawable(R.drawable.child_boy_infant_key_achievement));
        try {
            counts.add(totalChildrenCovered());
        } catch (Exception e) {
            counts.add("N/A");
            e.printStackTrace();
        }

        titleList.add("% of children who are growth faltering");
        iconList.add(getResources().getDrawable(R.drawable.child_boy_infant_key_achievement));
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
            String dobString = getValue(child.getColumnmaps(), PathConstants.KEY.DOB, false);
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
        Gender gender =   Gender.valueOf(getValue(childDetails.getColumnmaps(), PathConstants.KEY.GENDER, true).toUpperCase());


        formattedDob = DATE_FORMAT.format(dob);
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

    public String totalchildregister(String date){
        Cursor cursor =  commonRepository.rawCustomQueryForAdapter("SELECT count(*)FROM ec_child where date( ec_child.client_reg_date) < '"+date+"'");
        cursor.moveToFirst();
        String totalchildregister = cursor.getString(0);
        cursor.close();
        return totalchildregister;
    }

    public String totalpregnantWoman(){
        Cursor cursor =  commonRepository.rawCustomQueryForAdapter("select count(*) from ec_mother where ec_mother.base_entity_id in (select base_entity_id from ec_details where ec_details.value = 'Yes' and ec_details.key = 'pregnant')");
        cursor.moveToFirst();
        String totalpregnantWoman = cursor.getString(0);
        cursor.close();
        return totalpregnantWoman;
    }

    public String totalWomanReached(){
        Cursor cursor =  commonRepository.rawCustomQueryForAdapter("select count(distinct(baseEntityID)) from event where (event.eventType = 'Pregnant Woman Counselling' or event.eventType ='Woman Member Follow Up') and event.baseEntityId in (Select ec_mother.base_entity_id from ec_mother)");
        cursor.moveToFirst();
        String totalWomanReached = cursor.getString(0);
        cursor.close();

        Cursor cursor2 =  commonRepository.rawCustomQueryForAdapter("Select count(*) from ec_mother");
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
