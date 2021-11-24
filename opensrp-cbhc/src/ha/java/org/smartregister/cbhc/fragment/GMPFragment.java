package org.smartregister.cbhc.fragment;

import static org.smartregister.cbhc.task.RemoteLoginTask.getOpenSRPContext;
import static org.smartregister.util.Utils.dobToDateTime;

import android.app.Activity;
import android.app.Dialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.joda.time.DateTime;
import org.json.JSONException;
import org.opensrp.api.constants.Gender;
import org.smartregister.cbhc.R;
import org.smartregister.cbhc.activity.MemberProfileActivity;
import org.smartregister.cbhc.application.AncApplication;
import org.smartregister.cbhc.helper.LocationHelper;
import org.smartregister.cbhc.job.HeightIntentServiceJob;
import org.smartregister.cbhc.job.MuactIntentServiceJob;
import org.smartregister.cbhc.job.WeightIntentServiceJob;
import org.smartregister.cbhc.util.DBConstants;
import org.smartregister.cbhc.util.GrowthUtil;
import org.smartregister.cbhc.util.JsonFormUtils;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.growthmonitoring.GrowthMonitoringLibrary;
import org.smartregister.growthmonitoring.domain.Height;
import org.smartregister.growthmonitoring.domain.HeightWrapper;
import org.smartregister.growthmonitoring.domain.MUAC;
import org.smartregister.growthmonitoring.domain.MUACWrapper;
import org.smartregister.growthmonitoring.domain.Weight;
import org.smartregister.growthmonitoring.domain.WeightWrapper;
import org.smartregister.growthmonitoring.domain.ZScore;
import org.smartregister.growthmonitoring.fragment.GrowthDialogFragment;
import org.smartregister.growthmonitoring.fragment.HeightMonitoringFragment;
import org.smartregister.growthmonitoring.fragment.MUACMonitoringFragment;
import org.smartregister.growthmonitoring.listener.HeightActionListener;
import org.smartregister.growthmonitoring.listener.MUACActionListener;
import org.smartregister.growthmonitoring.listener.WeightActionListener;
import org.smartregister.growthmonitoring.repository.HeightRepository;
import org.smartregister.growthmonitoring.repository.MUACRepository;
import org.smartregister.growthmonitoring.repository.WeightRepository;
import org.smartregister.growthmonitoring.util.HeightUtils;
import org.smartregister.growthmonitoring.util.MUACUtils;
import org.smartregister.util.DateUtil;
import org.smartregister.util.Utils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;

public class GMPFragment extends BaseProfileFragment implements WeightActionListener, HeightActionListener, MUACActionListener {
    public static final String DIALOG_TAG = "GMPFragment_DIALOG_TAG";
    View fragmentView;
    Activity mActivity;
    public CommonPersonObjectClient childDetails;
    public static GMPFragment newInstance(Bundle bundle) {
        Bundle args = bundle;
        GMPFragment fragment = new GMPFragment();
        if (args == null) {
            args = new Bundle();
        }
        fragment.setArguments(args);
        return fragment;
    }
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mActivity = (Activity)context;
    }

    @Override
    protected void onCreation() {

    }

    @Override
    protected void onResumption() {

    }
    public void setChildDetails(CommonPersonObjectClient childDetails)
    {
        this.childDetails = childDetails;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        fragmentView = inflater.inflate(R.layout.gmp_layout, container, false);
        return fragmentView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        String dobString = Utils.getValue(childDetails.getColumnmaps(), DBConstants.KEY.DOB, false);

        if(TextUtils.isEmpty(dobString)){
            Toast.makeText(mActivity,"DOB invalid formate",Toast.LENGTH_SHORT).show();
            return;
        }
        initViews();
        updateGenderInChildDetails();
        refreshEditWeightLayout(false);
        refreshEditHeightLayout(false);
        refreshEditMuacLayout(false);
        updateProfileColor();
    }

//    String muacText;

    private void initViews() {

        ImageButton growthChartButton = (ImageButton) fragmentView.findViewById(R.id.growth_chart_button);
        growthChartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utils.startAsyncTask(new ShowGrowthChartTask(), null);
            }
        });
        fragmentView.findViewById(R.id.record_height).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GrowthUtil.showHeightRecordDialog(mActivity, childDetails, 1, DIALOG_TAG);
            }
        });
        fragmentView.findViewById(R.id.record_weight).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GrowthUtil.showWeightRecordDialog(mActivity, childDetails, 1, DIALOG_TAG);
            }
        });
        fragmentView.findViewById(R.id.height_chart_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utils.startAsyncTask(new ShowHeightChartTask(), null);
            }
        });
        fragmentView.findViewById(R.id.refer_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isRefered = JsonFormUtils.updateClientStatusAsEvent(mActivity,childDetails.entityId(),"","","ec_referel",JsonFormUtils.REFEREL_EVENT_TYPE);
                if(isRefered){
                    GrowthUtil.updateIsRefered(childDetails.entityId(),"true");
                    Toast.makeText(mActivity,"Successfully refered to clinic",Toast.LENGTH_SHORT).show();
                    fragmentView.findViewById(R.id.refer_btn).setVisibility(View.GONE);
                }
            }
        });

        View recordMUAC = fragmentView.findViewById(R.id.recordMUAC);
        recordMUAC.setClickable(true);
        recordMUAC.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                GrowthUtil.showMuacRecordDialog(mActivity, childDetails, DIALOG_TAG);
            }
        });
        fragmentView.findViewById(R.id.muac_chart_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utils.startAsyncTask(new ShowMuacChartTask(), null);
            }
        });

    }
    String heightText = "";
    String weightText = "";
    private String refreshEditWeightLayout(boolean isNeedToUpdateDb){
        LinearLayout fragmentContainer = (LinearLayout) fragmentView.findViewById(R.id.weight_group_canvas_ll);
        fragmentContainer.removeAllViews();
        fragmentContainer.addView(getLayoutInflater().inflate(R.layout.previous_weightview, null));
        TableLayout weightTable = fragmentView.findViewById(R.id.weights_table);
        LinkedHashMap<Long, Pair<String, String>> weightmap = new LinkedHashMap<>();
        ArrayList<Boolean> weighteditmode = new ArrayList<Boolean>();
        ArrayList<View.OnClickListener> listeners = new ArrayList<View.OnClickListener>();

        WeightRepository wp = GrowthMonitoringLibrary.getInstance().weightRepository();
        List<Weight> weightlist = wp.getMaximum12(childDetails.entityId());
        /////////////////////////////////////////////////
        String dobString = Utils.getValue(childDetails.getColumnmaps(), DBConstants.KEY.DOB, false);
        DateTime dateTime = new DateTime(dobString);
        Date dob  = dateTime.toDate();
        if (!TextUtils.isEmpty(Utils.getValue(childDetails.getColumnmaps(), DBConstants.KEY.BIRTH_WEIGHT, false))
                && !TextUtils.isEmpty(dobString)) {

            Double birthWeight = Double.valueOf(Utils.getValue(childDetails.getColumnmaps(), DBConstants.KEY.BIRTH_WEIGHT, false));

            Weight weight = new Weight(-1l, null, (float) birthWeight.doubleValue(), dateTime.toDate(), null, null, null, Calendar.getInstance().getTimeInMillis(), null, null, 0);
            weightlist.add(weight);
        }
        Gender gender = getGender();

        weightText = GrowthUtil.refreshPreviousWeightsTable(mActivity,weightTable, gender, dob, weightlist,isNeedToUpdateDb);
        return weightText;
    }
    private String refreshEditHeightLayout(boolean isNeedToUpdateDB) {
        LinearLayout fragmentContainer = (LinearLayout) fragmentView.findViewById(R.id.height_group_canvas_ll);
        fragmentContainer.removeAllViews();
        fragmentContainer.addView(getLayoutInflater().inflate(R.layout.previous_height_view, null));
        TableLayout heightTable = fragmentView.findViewById(R.id.heights_table);
        HeightRepository wp = GrowthMonitoringLibrary.getInstance().getHeightRepository();
        List<Height> heightList = wp.getMaximum12(childDetails.entityId());
        if (heightList.size() > 0) {
            try {
                HeightUtils.refreshPreviousHeightsTable(mActivity, heightTable, getGender(), dobToDateTime(childDetails).toDate(), heightList, Calendar.getInstance());
            } catch (Exception e) {
                e.printStackTrace();
            }
            Height height = heightList.get(0);
            heightText = ZScore.getZScoreText(height.getZScore());
            if(isNeedToUpdateDB) GrowthUtil.updateLastHeight(height.getCm(),height.getBaseEntityId(),heightText);
        }
        return heightText;
    }

    int muakColor = 0;
    String muakText = "";

    private String refreshEditMuacLayout(boolean isNeedToUpdateDB) {
        LinearLayout fragmentContainer = (LinearLayout) fragmentView.findViewById(R.id.muac_group_canvas_ll);
        fragmentContainer.removeAllViews();
        fragmentContainer.addView(getLayoutInflater().inflate(R.layout.previous_muac_view, null));
        TableLayout muacTable = fragmentView.findViewById(R.id.muac_table);
        MUACRepository wp = GrowthMonitoringLibrary.getInstance().getMuacRepository();
        List<MUAC> heightList = wp.getMaximum12(childDetails.entityId());
        if (heightList.size() > 0) {
            MUACUtils.refreshPreviousMuacTable(mActivity, muacTable, getGender(), dobToDateTime(childDetails).toDate(), heightList);
            MUAC latestMuac = heightList.get(0);
            muakColor = ZScore.getMuacColor(latestMuac.getCm());
            muakText = ZScore.getMuacText(latestMuac.getCm());
            if(isNeedToUpdateDB)GrowthUtil.updateLastMuac(latestMuac.getCm(),childDetails.entityId(),muakText);
        }
        return muakText;

    }

    private void updateProfileColor() {
        Log.v("MUAC", weightText+" "+heightText+ " "+ muakText);
        String resultText = "";
        int resultColor = 0;

        if(weightText.isEmpty() && heightText.isEmpty()){
            resultText = muakText;
            resultColor = muakColor;
        }
        if(weightText.contains("OVER WEIGHT") || heightText.contains("OVER WEIGHT")){
            resultText = "OVER WEIGHT";
            resultColor = ZScore.getZscoreColorByText(resultText);
        }
        else if(weightText.contains("SAM") || heightText.contains("SAM")){
            reblob:https://teams.microsoft.com/516cf240-34cf-419f-a8bd-a5853e6e1ea0sultText = "SAM";
            resultColor = ZScore.getZscoreColorByText(resultText);
        }
        else if(muakText.contains("SAM")){
            resultText = muakText;
            resultColor = muakColor;
        }
        else if(muakText.contains("MAM")){
            resultText = muakText;
            resultColor = muakColor;
        }
        else if(weightText.contains("MAM") || heightText.contains("MAM")){
            resultText = "MAM";
            resultColor = ZScore.getZscoreColorByText(resultText);
        }
        else if(weightText.contains("DARK YELLOW") || heightText.contains("DARK YELLOW")){
            resultText = "DARK YELLOW";
            resultColor = ZScore.getZscoreColorByText(resultText);
        }
        else {
            resultText = "NORMAL";
            resultColor = ZScore.getZscoreColorByText(resultText);
        }

//        if(!resultText.isEmpty()){
//            muacText = resultText;
////            muacText.setVisibility(View.VISIBLE);
////            muacText.setText(resultText);
////            muacText.setBackgroundColor(getResources().getColor(resultColor));
//        }
        updateChildProfileColor(resultColor,resultText);
    }
    private void updateChildProfileColor(int resultColor,String text){
        if(mActivity!=null && !mActivity.isFinishing()){
            MemberProfileActivity profileActivity = (MemberProfileActivity) mActivity;
            profileActivity.updateProfileIconColor(resultColor,text);

        }
        showReferedBtn();
    }
    private void updateGenderInChildDetails() {
        if (childDetails != null) {
            String genderString = Utils.getValue(childDetails, DBConstants.KEY.GENDER, false);
            if (genderString.equalsIgnoreCase("ছেলে") || genderString.equalsIgnoreCase("male")) {
                childDetails.getDetails().put("gender", "male");
            } else if (genderString.equalsIgnoreCase("মেয়ে") || genderString.equalsIgnoreCase("female")) {
                childDetails.getDetails().put("gender", "female");
            } else {
                childDetails.getDetails().put("gender", "male");
            }
        }
    }

    @Override
    public void onHeightTaken(HeightWrapper heightWrapper) {
        if (heightWrapper != null) {
            final HeightRepository heightRepository = GrowthMonitoringLibrary.getInstance().getHeightRepository();
            Height height = new Height();
//            if (heightWrapper.getDbKey() != null) {
//                height = heightRepository.find(heightWrapper.getDbKey());
//            }
            height.setBaseEntityId(childDetails.entityId());
            height.setCm(heightWrapper.getHeight());
            height.setDate(heightWrapper.getUpdatedHeightDate().toDate());
            String anm = getOpenSRPContext().allSharedPreferences().fetchRegisteredANM();
            height.setAnmId(getOpenSRPContext().allSharedPreferences().fetchRegisteredANM());
            height.setLocationId(getOpenSRPContext().allSharedPreferences().fetchDefaultLocalityId(anm));
            height.setTeam(getOpenSRPContext().allSharedPreferences().fetchDefaultTeam(anm));
            height.setTeamId(getOpenSRPContext().allSharedPreferences().fetchDefaultTeamId(anm));


            String g = childDetails.getColumnmaps().get("gender");
            String dobstring = childDetails.getColumnmaps().get("dob");
            GrowthUtil.DOB_STRING = dobstring;
            Gender gender = getGender();

            Date dob = null;
            if (!TextUtils.isEmpty(GrowthUtil.DOB_STRING)) {
                DateTime dateTime = new DateTime(GrowthUtil.DOB_STRING);
                dob = dateTime.toDate();
            }

            if (dob != null && gender != Gender.UNKNOWN) {
                heightRepository.add(dob, gender, height);
            } else {
                heightRepository.add(height);
            }

            heightWrapper.setDbKey(height.getId());

        }
        HeightIntentServiceJob.scheduleJobImmediately(HeightIntentServiceJob.TAG);

        String text = refreshEditHeightLayout(true);
        updateProfileColor();
        showGMPDialog(text);
    }

    @Override
    public void onMUACTaken(MUACWrapper muacWrapper) {
        if (muacWrapper != null) {
            final MUACRepository muacRepository = GrowthMonitoringLibrary.getInstance().getMuacRepository();
            MUAC muac = new MUAC();
//            if (muacWrapper.getDbKey() != null) {
//                height = heightRepository.find(muacWrapper.getDbKey());
//            }
            muac.setBaseEntityId(childDetails.entityId());
            muac.setCm(muacWrapper.getHeight());
            muac.setDate(muacWrapper.getUpdatedHeightDate().toDate());
            String anm = getOpenSRPContext().allSharedPreferences().fetchRegisteredANM();
            muac.setAnmId(getOpenSRPContext().allSharedPreferences().fetchRegisteredANM());
            muac.setLocationId(getOpenSRPContext().allSharedPreferences().fetchDefaultLocalityId(anm));
            muac.setTeam(getOpenSRPContext().allSharedPreferences().fetchDefaultTeam(anm));
            muac.setTeamId(getOpenSRPContext().allSharedPreferences().fetchDefaultTeamId(anm));
            muac.setEdemaValue(muacWrapper.getEdemaValue());

            String dobstring = childDetails.getColumnmaps().get("dob");
            GrowthUtil.DOB_STRING = dobstring;


//            Date dob = null;
//            if (!TextUtils.isEmpty(GrowthUtil.DOB_STRING)) {
//                DateTime dateTime = new DateTime(GrowthUtil.DOB_STRING);
//                dob = dateTime.toDate();
//            }
//            Gender gender = getGender();
            muacRepository.add(muac);
            muacWrapper.setDbKey(muac.getId());

        }
        MuactIntentServiceJob.scheduleJobImmediately(MuactIntentServiceJob.TAG);
        String text = refreshEditMuacLayout(true);
        updateProfileColor();
        showGMPDialog(text);
    }

    @Override
    public void onWeightTaken(WeightWrapper tag) {
        if (tag != null) {
            final WeightRepository weightRepository = GrowthMonitoringLibrary.getInstance().weightRepository();
            Weight weight = new Weight();
//            if (tag.getDbKey() != null) {
//                weight = weightRepository.find(tag.getDbKey());
//            }
            weight.setBaseEntityId(childDetails.entityId());
            weight.setKg(tag.getWeight());
            weight.setDate(tag.getUpdatedWeightDate().toDate());
            weight.setAnmId(getOpenSRPContext().allSharedPreferences().fetchRegisteredANM());
            try {
                String lastLocationId = LocationHelper.getInstance().getChildLocationId();

                weight.setLocationId(lastLocationId);
            } catch (Exception e) {
                e.printStackTrace();
            }

            Gender gender = getGender();

            Date dob = null;

            String formattedAge = "";
            if (isDataOk()) {
                String dobString = Utils.getValue(childDetails.getColumnmaps(), DBConstants.KEY.DOB, false);
                if (!TextUtils.isEmpty(dobString)) {
                    DateTime dateTime = new DateTime(dobString);
                    dob = dateTime.toDate();
                    long timeDiff = Calendar.getInstance().getTimeInMillis() - dob.getTime();
                    if (timeDiff >= 0) {
                        formattedAge = DateUtil.getDuration(timeDiff);
                    }
                }
            }

            if (dob != null && gender != Gender.UNKNOWN) {
                weightRepository.add(dob, gender, weight);
            } else {
                weightRepository.add(weight);
            }

            tag.setDbKey(weight.getId());
            tag.setPatientAge(formattedAge);
            WeightIntentServiceJob.scheduleJobImmediately(WeightIntentServiceJob.TAG);
            String text = refreshEditWeightLayout(true);
            showGMPDialog(text);
        }
    }
    private void showGMPDialog(String text){
        boolean isSam = text.equalsIgnoreCase("SAM");
        Dialog dialog = new Dialog(mActivity);
        dialog.setCancelable(false);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_with_one_button);
        TextView titleTv = dialog.findViewById(R.id.title_tv);
        titleTv.setText(isSam?"Inadequate Growth!!Please refer to the nearest clinic ":"Congratulation!Your child growth is adequacte");
        titleTv.setTextColor(isSam?mActivity.getResources().getColor(R.color.alert_urgent_red):mActivity.getResources().getColor(R.color.alert_completed));
        Button ok_btn = dialog.findViewById(R.id.ok_btn);
        showReferedBtn();

        ok_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.show();

    }
    private void showReferedBtn(){
        String isReferedValue = Utils.getValue(childDetails, "is_refered", false);
        boolean isAlreadyRefered = !TextUtils.isEmpty(isReferedValue)&&isReferedValue.equalsIgnoreCase("true");
        if(isAlreadyRefered) {
            fragmentView.findViewById(R.id.refer_btn).setVisibility(View.GONE);
            return;
        }
        if(muakText.equalsIgnoreCase("sam")||heightText.equalsIgnoreCase("sam")
           || weightText.equalsIgnoreCase("sam")){
            fragmentView.findViewById(R.id.refer_btn).setVisibility(View.VISIBLE);
        }
    }
    private boolean isDataOk() {
        return childDetails != null && childDetails.getDetails() != null;
    }
    private class ShowGrowthChartTask extends AsyncTask<Void, Void, List<Weight>> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected List<Weight> doInBackground(Void... params) {
            WeightRepository weightRepository = AncApplication.getInstance().weightRepository();
            List<Weight> allWeights = weightRepository.findByEntityId(childDetails.entityId());
            try {
                String dobString = Utils.getValue(childDetails.getColumnmaps(), DBConstants.KEY.DOB, false);
                if (!TextUtils.isEmpty(Utils.getValue(childDetails.getColumnmaps(), DBConstants.KEY.BIRTH_WEIGHT, false))
                        && !TextUtils.isEmpty(dobString)) {
                    DateTime dateTime = new DateTime(dobString);
                    Double birthWeight = Double.valueOf(Utils.getValue(childDetails.getColumnmaps(), DBConstants.KEY.BIRTH_WEIGHT, false));

                    Weight weight = new Weight(-1l, null, (float) birthWeight.doubleValue(), dateTime.toDate(), null, null, null, Calendar.getInstance().getTimeInMillis(), null, null, 0);
                    allWeights.add(weight);
                }
            } catch (Exception e) {
            }

            return allWeights;
        }

        @Override
        protected void onPostExecute(List<Weight> allWeights) {
            super.onPostExecute(allWeights);
            FragmentTransaction ft = mActivity.getFragmentManager().beginTransaction();
            Fragment prev = mActivity.getFragmentManager().findFragmentByTag(DIALOG_TAG);
            if (prev != null) {
                ft.remove(prev);
            }
            ft.addToBackStack(null);


            GrowthDialogFragment growthDialogFragment = GrowthDialogFragment.newInstance(childDetails, allWeights);
            growthDialogFragment.show(ft, DIALOG_TAG);
        }
    }

    private class ShowHeightChartTask extends AsyncTask<Void, Void, List<Height>> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected List<Height> doInBackground(Void... params) {
            HeightRepository heightRepository = GrowthMonitoringLibrary.getInstance().getHeightRepository();
            List<Height> allHeight = heightRepository.findByEntityId(childDetails.entityId());
            return allHeight;
        }

        @Override
        protected void onPostExecute(List<Height> allHeight) {
            super.onPostExecute(allHeight);
            FragmentTransaction ft = mActivity.getFragmentManager().beginTransaction();
            Fragment prev = mActivity.getFragmentManager().findFragmentByTag(DIALOG_TAG);
            if (prev != null) {
                ft.remove(prev);
            }
            ft.addToBackStack(null);
            String dobString = Utils.getValue(childDetails.getColumnmaps(), DBConstants.KEY.DOB, false);

            HeightMonitoringFragment growthDialogFragment = HeightMonitoringFragment.createInstance(dobString, getGender(), allHeight);
            growthDialogFragment.show(ft, DIALOG_TAG);
        }
    }

    private class ShowMuacChartTask extends AsyncTask<Void, Void, List<MUAC>> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected List<MUAC> doInBackground(Void... params) {
            MUACRepository heightRepository = GrowthMonitoringLibrary.getInstance().getMuacRepository();
            List<MUAC> allHeight = heightRepository.findByEntityId(childDetails.entityId());
            return allHeight;
        }

        @Override
        protected void onPostExecute(List<MUAC> allHeight) {
            super.onPostExecute(allHeight);
            FragmentTransaction ft = mActivity.getFragmentManager().beginTransaction();
            Fragment prev = mActivity.getFragmentManager().findFragmentByTag(DIALOG_TAG);
            if (prev != null) {
                ft.remove(prev);
            }
            ft.addToBackStack(null);
            String dobString = Utils.getValue(childDetails.getColumnmaps(), DBConstants.KEY.DOB, false);

            MUACMonitoringFragment growthDialogFragment = MUACMonitoringFragment.createInstance(dobString, getGender(), allHeight);
            growthDialogFragment.show(ft, DIALOG_TAG);
        }
    }

    private Gender getGender() {
        Gender gender = Gender.UNKNOWN;
        String genderString = Utils.getValue(childDetails, DBConstants.KEY.GENDER, false);

        if (genderString != null && genderString.equalsIgnoreCase("female")) {
            gender = Gender.FEMALE;
        } else if (genderString != null && genderString.equalsIgnoreCase("male")) {
            gender = Gender.MALE;
        }
        return gender;
    }
}
