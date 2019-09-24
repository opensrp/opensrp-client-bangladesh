package org.smartregister.cbhc.fragment;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import org.joda.time.DateTime;
import org.opensrp.api.constants.Gender;
import org.smartregister.cbhc.R;
import org.smartregister.cbhc.util.GrowthUtil;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.growthmonitoring.GrowthMonitoringLibrary;
import org.smartregister.growthmonitoring.domain.Height;
import org.smartregister.growthmonitoring.domain.HeightWrapper;
import org.smartregister.growthmonitoring.domain.Weight;
import org.smartregister.growthmonitoring.domain.WeightWrapper;
import org.smartregister.growthmonitoring.fragment.GrowthDialogFragment;
import org.smartregister.growthmonitoring.repository.HeightRepository;
import org.smartregister.growthmonitoring.repository.WeightRepository;
import org.smartregister.growthmonitoring.service.intent.WeightIntentService;
import org.smartregister.growthmonitoring.util.HeightUtils;
import org.smartregister.growthmonitoring.util.WeightUtils;
import org.smartregister.util.DateUtil;
import org.smartregister.util.Utils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import timber.log.Timber;

import static org.smartregister.cbhc.task.RemoteLoginTask.getOpenSRPContext;

public class GrowthFragment extends BaseProfileFragment {

    public static GrowthFragment newInstance(Bundle bundle) {
        Bundle args = bundle;
        GrowthFragment fragment = new GrowthFragment();
        if (args == null) {
            args = new Bundle();
        }
        fragment.setArguments(args);
        return fragment;
    }
    private static final String DIALOG_TAG = "DIALOG_TAG_DUUH";
    private static final String TAG = GrowthFragment.class.getCanonicalName();
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onCreation() {
        //Overriden
    }

    @Override
    protected void onResumption() {
        //Overriden

        View recordMUAC = fragmentView.findViewById(R.id.recordMUAC);
        recordMUAC.setClickable(true);
        recordMUAC.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                RecordMUACDialogFragment recordMUACDialogFragment = RecordMUACDialogFragment.newInstance();
                recordMUACDialogFragment.show(GrowthUtil.initFragmentTransaction(getActivity(), DIALOG_TAG),DIALOG_TAG);
            }
        });
        View recordWeight = fragmentView.findViewById(R.id.record_weight);
        recordWeight.setClickable(true);
        recordWeight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                view.setEnabled(false);

                GrowthUtil.showGrowthDialog(getActivity(), view, DIALOG_TAG);
                view.setEnabled(true);
            }
        });


        ImageButton growthChartButton = fragmentView.findViewById(R.id.growth_chart_button);
        growthChartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.setEnabled(false);
                Utils.startAsyncTask(new ShowGrowthChartTask(), null);
                v.setEnabled(true);
            }
        });

        refreshEditWeightLayout();
        refreshEditHeightLayout();
        startServices();

    }
    View fragmentView;
    boolean isChild = true;

    public void setIsChild(boolean iChild){
        this.isChild = iChild;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        fragmentView = inflater.inflate(R.layout.growth_activity_main, container, false);
        if(!isChild){
            fragmentView.findViewById(R.id.growth_chart_button).setVisibility(View.GONE);
        }
        return fragmentView;
    }
    public void startServices() {
        Intent vaccineIntent = new Intent(getActivity(), WeightIntentService.class);
        getActivity().startService(vaccineIntent);
    }

    private class ShowGrowthChartTask extends AsyncTask<Void, Void,Map<String, List>>  {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }
        public static final String WEIGHT = "weight";
        public static final String HEIGHT = "height";
        @Override
        protected Map<String, List> doInBackground(Void... voids) {
            Map<String, List> growthMonitoring = new HashMap<>();
            WeightRepository weightRepository = GrowthMonitoringLibrary.getInstance().weightRepository();
            List<Weight> allWeights = weightRepository.findByEntityId(GrowthUtil.ENTITY_ID);
            try {
//                DateTime dateTime = new DateTime(GrowthUtil.getDateOfBirth());
//
//                Weight weight = new Weight(-1l, null, (float) GrowthUtil.BIRTH_WEIGHT, dateTime.toDate(), null, null, null,
//                        Calendar.getInstance().getTimeInMillis(), null, null, 0);
//                allWeights.add(weight);
            } catch (Exception e) {
Utils.appendLog(getClass().getName(),e);
                Log.e(TAG, Log.getStackTraceString(e));
            }

            growthMonitoring.put(WEIGHT, allWeights);


            HeightRepository heightRepository = GrowthMonitoringLibrary.getInstance().heightRepository();
            List<Height> allHeights = heightRepository.findByEntityId(GrowthUtil.ENTITY_ID);
            try {
//                DateTime dateTime = new DateTime(GrowthUtil.getDateOfBirth());
//
//                Height height = new Height(-1l, null, (float) GrowthUtil.BIRTH_HEIGHT, dateTime.toDate(), null, null, null,
//                        Calendar.getInstance().getTimeInMillis(), null, null, 0);
//                allHeights.add(height);
            } catch (Exception e) {
Utils.appendLog(getClass().getName(),e);
                Log.e(TAG, Log.getStackTraceString(e));
            }
            growthMonitoring.put(HEIGHT, allHeights);

            return growthMonitoring;
        }

        @Override
        protected void onPostExecute(Map<String, List> growthMonitoring) {
            super.onPostExecute(growthMonitoring);

            if (growthMonitoring == null || growthMonitoring.isEmpty()) {
                Toast.makeText(getActivity(), "Record at least one set of growth details (height, Weight)",
                        Toast.LENGTH_LONG).show();
            } else {
                List<Weight> weights = new ArrayList<>();
                List<Height> heights = new ArrayList<>();

                if (growthMonitoring.containsKey(WEIGHT)) {
                    weights = growthMonitoring.get(WEIGHT);
                }

                if (growthMonitoring.containsKey(HEIGHT)) {
                    heights = growthMonitoring.get(HEIGHT);
                }
                if(childDetails.getColumnmaps().get("gender").equals("M")){
                    childDetails.getDetails().put("gender","male");
//                    childDetails.getColumnmaps().put("gender","male");
                }else if(childDetails.getColumnmaps().get("gender").equals("F")){
                    childDetails.getDetails().put("gender","female");
                }
                String first_name = childDetails.getDetails().get("first_name");
                String last_name = childDetails.getDetails().get("last_name");
                String dob = childDetails.getDetails().get("dob");
                String gender = childDetails.getDetails().get("gender");
                GrowthDialogFragment growthDialogFragment = GrowthDialogFragment
                        .newInstance(childDetails, weights, heights);
                growthDialogFragment.show(GrowthUtil.initFragmentTransaction(getActivity(), DIALOG_TAG), DIALOG_TAG);
            }
        }
    }
    CommonPersonObjectClient childDetails;
    public void setChildDetails(CommonPersonObjectClient childDetails){
        this.childDetails = childDetails;
        GrowthUtil.childDetails = childDetails;
        GrowthUtil.ENTITY_ID = childDetails.entityId();

        String g = childDetails.getColumnmaps().get("gender");
        String dobstring = childDetails.getColumnmaps().get("dob");
        GrowthUtil.DOB_STRING = dobstring;
        String genderString = g;

        if (genderString != null && genderString.toLowerCase().equals("F")) {
            GrowthUtil.GENDER = Gender.FEMALE.name().toLowerCase();
        } else if (genderString != null && genderString.toLowerCase().equals("M")) {
            GrowthUtil.GENDER = Gender.MALE.name().toLowerCase();
        }
    }
    private void refreshEditHeightLayout() {
        View heightWidget = fragmentView.findViewById(R.id.height_widget);

        LinkedHashMap<Long, Pair<String, String>> heightmap = new LinkedHashMap<>();
        ArrayList<Boolean> heightEditMode = new ArrayList<>();
        ArrayList<View.OnClickListener> listeners = new ArrayList<>();

        HeightRepository wp = GrowthMonitoringLibrary.getInstance().heightRepository();
        List<Height> heightList = wp.findLast5(childDetails.entityId());

        for (int i = 0; i < heightList.size(); i++) {
            Height height = heightList.get(i);
            String formattedAge = "";
            if (height.getDate() != null) {

                Date heightDate = height.getDate();
                DateTime birthday = new DateTime(GrowthUtil.getDateOfBirth());
                Date birth = birthday.toDate();
                long timeDiff = heightDate.getTime() - birth.getTime();
                Timber.v("%s", timeDiff);
                if (timeDiff >= 0) {
                    formattedAge = DateUtil.getDuration(timeDiff);
                    Timber.v(formattedAge);
                }
            }
            if (!formattedAge.equalsIgnoreCase("0d")) {
                heightmap.put(height.getId(), Pair.create(formattedAge, GrowthUtil.cmStringSuffix(height.getCm())));

                boolean lessThanThreeMonthsEventCreated = HeightUtils.lessThanThreeMonths(height);
                heightEditMode.add(lessThanThreeMonthsEventCreated);

                final int finalI = i;
                View.OnClickListener onClickListener = new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        v.setEnabled(false);
                        GrowthUtil.showEditGrowthMonitoringDialog(getActivity(), childDetails, finalI, DIALOG_TAG);
                        v.setEnabled(true);
                    }
                };
                listeners.add(onClickListener);
            }

        }
        if (heightmap.size() < 5) {
//            heightmap.put(0l, Pair.create(DateUtil.getDuration(0), GrowthUtil.BIRTH_HEIGHT + " cm"));
            heightEditMode.add(false);
            listeners.add(null);
        }

        if (heightmap.size() > 0) {
            GrowthUtil.createHeightWidget(getActivity(), heightWidget, heightmap, listeners, heightEditMode);
        }
    }

    public void onHeightTaken(HeightWrapper heightWrapper) {
        if (heightWrapper != null) {
            final HeightRepository heightRepository = GrowthMonitoringLibrary.getInstance().heightRepository();
            Height height = new Height();
            if (heightWrapper.getDbKey() != null) {
                height = heightRepository.find(heightWrapper.getDbKey());
            }
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
            Gender gender = Gender.UNKNOWN;

            String genderString = g;

            if (genderString != null && genderString.equalsIgnoreCase("F")) {
                gender = Gender.FEMALE;
            } else if (genderString != null && genderString.equalsIgnoreCase("M")) {
                gender = Gender.MALE;
            }

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

        refreshEditHeightLayout();
    }
    public void onWeightTaken(WeightWrapper tag) {
        if (tag != null) {
            final WeightRepository weightRepository = GrowthMonitoringLibrary.getInstance().weightRepository();
            Weight weight = new Weight();
            if (tag.getDbKey() != null) {
                weight = weightRepository.find(tag.getDbKey());
            }
            weight.setBaseEntityId(childDetails.entityId());
            weight.setKg(tag.getWeight());
            weight.setDate(tag.getUpdatedWeightDate().toDate());
            String anm = getOpenSRPContext().allSharedPreferences().fetchRegisteredANM();
            weight.setAnmId(getOpenSRPContext().allSharedPreferences().fetchRegisteredANM());
            weight.setLocationId(getOpenSRPContext().allSharedPreferences().fetchDefaultLocalityId(anm));
            weight.setTeam(getOpenSRPContext().allSharedPreferences().fetchDefaultTeam(anm));
            weight.setTeamId(getOpenSRPContext().allSharedPreferences().fetchDefaultTeamId(anm));

//            weight.setChildLocationId(getOpenSRPContext().allSharedPreferences().fetch);
            String g = childDetails.getColumnmaps().get("gender");
            String dobstring = childDetails.getColumnmaps().get("dob");
            GrowthUtil.DOB_STRING = dobstring;
            Gender gender = Gender.UNKNOWN;

            String genderString = g;

            if (genderString != null && genderString.equalsIgnoreCase("F")) {
                gender = Gender.FEMALE;
            } else if (genderString != null && genderString.equalsIgnoreCase("M")) {
                gender = Gender.MALE;
            }

            Date dob = null;
            if (!TextUtils.isEmpty(GrowthUtil.DOB_STRING)) {
                DateTime dateTime = new DateTime(GrowthUtil.DOB_STRING);
                dob = dateTime.toDate();
            }

            if (dob != null && gender != Gender.UNKNOWN) {
                weightRepository.add(dob, gender, weight);
            } else {
                weightRepository.add(weight);
            }

            tag.setDbKey(weight.getId());

        }

        refreshEditWeightLayout();
    }

    private void refreshEditWeightLayout() {
        View weightWidget = fragmentView.findViewById(R.id.weight_widget);

        LinkedHashMap<Long, Pair<String, String>> weightmap = new LinkedHashMap<>();
        ArrayList<Boolean> weighteditmode = new ArrayList<Boolean>();
        ArrayList<View.OnClickListener> listeners = new ArrayList<>();

        WeightRepository wp = GrowthMonitoringLibrary.getInstance().weightRepository();
        List<Weight> weightlist = wp.findLast5(childDetails.entityId());

        for (int i = 0; i < weightlist.size(); i++) {
            Weight weight = weightlist.get(i);
            String formattedAge = "";
            if (weight.getDate() != null) {

                Date weighttaken = weight.getDate();
                DateTime birthday = new DateTime(GrowthUtil.getDateOfBirth());
                Date birth = birthday.toDate();
                long timeDiff = weighttaken.getTime() - birth.getTime();
                Log.v("timeDiff is ", timeDiff + "");
                if (timeDiff >= 0) {
                    formattedAge = DateUtil.getDuration(timeDiff);
                    Log.v("age is ", formattedAge);
                }
            }
            if (!formattedAge.equalsIgnoreCase("0d")) {
                weightmap.put(weight.getId(), Pair.create(formattedAge, Utils.kgStringSuffix(weight.getKg())));

                boolean lessThanThreeMonthsEventCreated = WeightUtils.lessThanThreeMonths(weight);
                if (lessThanThreeMonthsEventCreated) {
                    weighteditmode.add(true);
                } else {
                    weighteditmode.add(false);
                }

                final int finalI = i;
                View.OnClickListener onclicklistener = new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        v.setEnabled(false);
                        GrowthUtil.showEditGrowthMonitoringDialog(getActivity(),childDetails, finalI, DIALOG_TAG);
                        v.setEnabled(true);
                    }
                };
                listeners.add(onclicklistener);
            }

        }
        if (weightmap.size() < 5) {
//            weightmap.put(0l, Pair.create(DateUtil.getDuration(0), GrowthUtil.BIRTH_WEIGHT + " kg"));
            weighteditmode.add(false);
            listeners.add(null);
        }

        if (weightmap.size() > 0) {
            GrowthUtil.createWeightWidget(getActivity(), weightWidget, weightmap, listeners, weighteditmode);
        }
    }

}