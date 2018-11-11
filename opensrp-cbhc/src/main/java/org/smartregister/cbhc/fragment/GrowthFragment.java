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
import org.smartregister.cbhc.application.AncApplication;
import org.smartregister.cbhc.helper.LocationHelper;
import org.smartregister.cbhc.util.GrowthUtil;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.growthmonitoring.GrowthMonitoringLibrary;
import org.smartregister.growthmonitoring.domain.Weight;
import org.smartregister.growthmonitoring.domain.WeightWrapper;
import org.smartregister.growthmonitoring.fragment.GrowthDialogFragment;
import org.smartregister.growthmonitoring.listener.WeightActionListener;
import org.smartregister.growthmonitoring.repository.WeightRepository;
import org.smartregister.growthmonitoring.service.intent.WeightIntentService;
import org.smartregister.growthmonitoring.util.WeightUtils;
import org.smartregister.util.DateUtil;
import org.smartregister.util.Utils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;

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
        View recordWeight = fragmentView.findViewById(R.id.record_weight);
        recordWeight.setClickable(true);
        recordWeight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                view.setEnabled(false);
                GrowthUtil.showWeightDialog(getActivity(), view, DIALOG_TAG);
                view.setEnabled(true);
            }
        });


        ImageButton growthChartButton = (ImageButton) fragmentView.findViewById(R.id.growth_chart_button);
        growthChartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.setEnabled(false);
                Utils.startAsyncTask(new ShowGrowthChartTask(), null);
                v.setEnabled(true);
            }
        });

        refreshEditWeightLayout();

        startServices();
    }
    View fragmentView;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        fragmentView = inflater.inflate(R.layout.growth_activity_main, container, false);
        return fragmentView;
    }
    public void startServices() {
        Intent vaccineIntent = new Intent(getActivity(), WeightIntentService.class);
        getActivity().startService(vaccineIntent);
    }

    private class ShowGrowthChartTask extends AsyncTask<Void, Void, List<Weight>> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected List<Weight> doInBackground(Void... params) {
            WeightRepository weightRepository = GrowthMonitoringLibrary.getInstance().weightRepository();
            List<Weight> allWeights = weightRepository.findByEntityId(GrowthUtil.ENTITY_ID);
            try {
                DateTime dateTime = new DateTime(GrowthUtil.DOB_STRING);

                Weight weight = new Weight(-1l, null, (float) GrowthUtil.BIRTH_WEIGHT, dateTime.toDate(), null, null, null, Calendar.getInstance().getTimeInMillis(), null, null, 0);
                allWeights.add(weight);
            } catch (Exception e) {
                Log.e(TAG, Log.getStackTraceString(e));
            }

            return allWeights;
        }

        @Override
        protected void onPostExecute(List<Weight> allWeights) {
            super.onPostExecute(allWeights);

            if (allWeights == null || allWeights.isEmpty()) {
                Toast.makeText(getActivity(), "Record atleast one weight", Toast.LENGTH_LONG).show();
            } else {
                GrowthDialogFragment growthDialogFragment = GrowthDialogFragment.newInstance(childDetails, allWeights);
                growthDialogFragment.show(GrowthUtil.initFragmentTransaction(getActivity(), DIALOG_TAG), DIALOG_TAG);
            }
        }
    }
    CommonPersonObjectClient childDetails;
    public void setChildDetails(CommonPersonObjectClient childDetails){
        this.childDetails = childDetails;
        GrowthUtil.childDetails = childDetails;
        GrowthUtil.ENTITY_ID = childDetails.entityId();

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

            Gender gender = Gender.UNKNOWN;

            String genderString = GrowthUtil.GENDER;

            if (genderString != null && genderString.toLowerCase().equals("female")) {
                gender = Gender.FEMALE;
            } else if (genderString != null && genderString.toLowerCase().equals("male")) {
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
        List<Weight> weightlist = wp.findLast5(GrowthUtil.ENTITY_ID);

        for (int i = 0; i < weightlist.size(); i++) {
            Weight weight = weightlist.get(i);
            String formattedAge = "";
            if (weight.getDate() != null) {

                Date weighttaken = weight.getDate();
                DateTime birthday = new DateTime(GrowthUtil.DOB_STRING);
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
                        GrowthUtil.showEditWeightDialog(getActivity(), finalI, DIALOG_TAG);
                        v.setEnabled(true);
                    }
                };
                listeners.add(onclicklistener);
            }

        }
        if (weightmap.size() < 5) {
            weightmap.put(0l, Pair.create(DateUtil.getDuration(0), GrowthUtil.BIRTH_WEIGHT + " kg"));
            weighteditmode.add(false);
            listeners.add(null);
        }

        if (weightmap.size() > 0) {
            GrowthUtil.createWeightWidget(getActivity(), weightWidget, weightmap, listeners, weighteditmode);
        }
    }

}