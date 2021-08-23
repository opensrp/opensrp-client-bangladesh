package org.smartregister.growplus.activity;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import net.sqlcipher.database.SQLiteDatabase;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.opensrp.api.constants.Gender;
import org.smartregister.commonregistry.AllCommonsRepository;
import org.smartregister.commonregistry.CommonPersonObject;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.domain.Alert;
import org.smartregister.domain.Photo;
import org.smartregister.growplus.adapter.CounsellingCardAdapter;
import org.smartregister.growplus.domain.Counselling;
import org.smartregister.growplus.job.HeightIntentServiceJob;
import org.smartregister.growplus.job.MuactIntentServiceJob;
import org.smartregister.growplus.listener.ActivityListener;
import org.smartregister.growplus.repository.CounsellingRepository;
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
import org.smartregister.growthmonitoring.fragment.RecordWeightDialogFragment;
import org.smartregister.growthmonitoring.listener.HeightActionListener;
import org.smartregister.growthmonitoring.listener.MUACActionListener;
import org.smartregister.growthmonitoring.listener.WeightActionListener;
import org.smartregister.growthmonitoring.repository.HeightRepository;
import org.smartregister.growthmonitoring.repository.MUACRepository;
import org.smartregister.growthmonitoring.repository.WeightRepository;
import org.smartregister.growthmonitoring.util.HeightUtils;
import org.smartregister.growthmonitoring.util.MUACUtils;
import org.smartregister.growthmonitoring.util.WeightUtils;
import org.smartregister.immunization.db.VaccineRepo;
import org.smartregister.immunization.domain.Vaccine;
import org.smartregister.immunization.domain.VaccineSchedule;
import org.smartregister.immunization.domain.VaccineWrapper;
import org.smartregister.immunization.fragment.VaccinationDialogFragment;
import org.smartregister.immunization.listener.VaccinationActionListener;
import org.smartregister.immunization.repository.RecurringServiceRecordRepository;
import org.smartregister.immunization.repository.RecurringServiceTypeRepository;
import org.smartregister.immunization.repository.VaccineRepository;
import org.smartregister.immunization.util.VaccinateActionUtils;
import org.smartregister.immunization.view.ExpandableHeightGridView;
import org.smartregister.immunization.view.VaccineGroup;
import org.smartregister.growplus.R;
import org.smartregister.growplus.application.VaccinatorApplication;
import org.smartregister.growplus.domain.RegisterClickables;
import org.smartregister.growplus.toolbar.LocationSwitcherToolbar;
import org.smartregister.growplus.view.SiblingPicturesGroup;
import org.smartregister.repository.AllSharedPreferences;
import org.smartregister.repository.DetailsRepository;
import org.smartregister.service.AlertService;
import org.smartregister.util.DateUtil;
import org.smartregister.util.FormUtils;
import org.smartregister.util.OpenSRPImageLoader;
import org.smartregister.util.Utils;
import org.smartregister.view.activity.DrishtiApplication;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import util.GrowthUtil;
import util.ImageUtils;
import util.JsonFormUtils;
import util.PathConstants;

import static org.smartregister.growplus.activity.WomanSmartRegisterActivity.REQUEST_CODE_GET_JSON;
import static org.smartregister.util.DateUtil.getDuration;
import static org.smartregister.util.Utils.dobToDateTime;
import static org.smartregister.util.Utils.getValue;
import static org.smartregister.util.Utils.kgStringSuffix;


/**
 * Created by Jason Rogena - jrogena@ona.io on 16/02/2017.
 */

public class ChildImmunizationActivity extends BaseActivity
        implements LocationSwitcherToolbar.OnLocationChangeListener, WeightActionListener, HeightActionListener, MUACActionListener, VaccinationActionListener {

    private static final String TAG = "ChildImmunoActivity";
    private static final String EXTRA_CHILD_DETAILS = "child_details";
    private static final String EXTRA_REGISTER_CLICKABLES = "register_clickables";
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy");
    public static final String DIALOG_TAG = "ChildImmunoActivity_DIALOG_TAG";
    private ArrayList<VaccineGroup> vaccineGroups;
    private static final ArrayList<String> COMBINED_VACCINES;
    private static final HashMap<String, String> COMBINED_VACCINES_MAP;
    private boolean bcgScarNotificationShown;
    private boolean weightNotificationShown;
    private final String BCG2_NOTIFICATION_DONE = "bcg2_not_done";
    private static final int RANDOM_MAX_RANGE = 4232;
    private static final int RANDOM_MIN_RANGE = 213;
    private static final int RECORD_WEIGHT_BUTTON_ACTIVE_MIN = 12;
    private Counselling mEditCounselling;

    static {
        COMBINED_VACCINES = new ArrayList<>();
        COMBINED_VACCINES_MAP = new HashMap<>();
        COMBINED_VACCINES.add("Measles 1");
        COMBINED_VACCINES_MAP.put("Measles 1", "Measles 1 / MR 1");
        COMBINED_VACCINES.add("MR 1");
        COMBINED_VACCINES_MAP.put("MR 1", "Measles 1 / MR 1");
        COMBINED_VACCINES.add("Measles 2");
        COMBINED_VACCINES_MAP.put("Measles 2", "Measles 2 / MR 2");
        COMBINED_VACCINES.add("MR 2");
        COMBINED_VACCINES_MAP.put("MR 2", "Measles 2 / MR 2");
    }

    // Views
    public LocationSwitcherToolbar toolbar;

    // Data
    public CommonPersonObjectClient childDetails;
    private RegisterClickables registerClickables;
    public DetailsRepository detailsRepository;
    private static final int REQUEST_CODE_GET_JSON_COUNSELLING_UPDATE = 512;
    private Button mFollowupDetail;
    private Map<String, String> mChildFollowupData;

    private String[] mChildFollowupKeys;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        detailsRepository = getOpenSRPContext().detailsRepository();

        toolbar = (LocationSwitcherToolbar) getToolbar();
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ChildImmunizationActivity.this, ChildSmartRegisterActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            }
        });
        toolbar.setOnLocationChangeListener(this);
//       View view= toolbar.findViewById(R.id.immunization_separator);
//        view.setBackground(R.drawable.vertical_seperator_female);

        // Get child details from bundled data
        Bundle extras = this.getIntent().getExtras();
        if (extras != null) {
            Serializable serializable = extras.getSerializable(EXTRA_CHILD_DETAILS);
            if (serializable != null && serializable instanceof CommonPersonObjectClient) {
                childDetails = (CommonPersonObjectClient) serializable;
            }

            serializable = extras.getSerializable(EXTRA_REGISTER_CLICKABLES);
            if (serializable != null && serializable instanceof RegisterClickables) {
                registerClickables = (RegisterClickables) serializable;
            }
            updateGenderInChildDetails();
        }

        bcgScarNotificationShown = false;
        weightNotificationShown = false;

        toolbar.init(this);
        setLastModified(false);


        mFollowupDetail = (Button) findViewById(R.id.child_followup_details);
        mFollowupDetail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showChildFollowupDetail();
            }
        });

        try {
            showChildFollowupData();
        } catch (Exception e) {
            e.printStackTrace();
        }
        initViews();


    }

    TextView muacText;

    private void initViews() {
        muacText = findViewById(R.id.muac_text);
        muacText.setVisibility(View.GONE);

        ImageButton growthChartButton = (ImageButton) findViewById(R.id.growth_chart_button);
        growthChartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utils.startAsyncTask(new ShowGrowthChartTask(), null);
            }
        });
        findViewById(R.id.record_height).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GrowthUtil.showHeightRecordDialog(ChildImmunizationActivity.this, childDetails, 1, DIALOG_TAG);
            }
        });
        findViewById(R.id.record_weight).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GrowthUtil.showWeightRecordDialog(ChildImmunizationActivity.this, childDetails, 1, DIALOG_TAG);
            }
        });
        findViewById(R.id.height_chart_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utils.startAsyncTask(new ShowHeightChartTask(), null);
            }
        });

        View recordMUAC = findViewById(R.id.recordMUAC);
        recordMUAC.setClickable(true);
        recordMUAC.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                GrowthUtil.showMuacRecordDialog(ChildImmunizationActivity.this, childDetails, DIALOG_TAG);
            }
        });
        findViewById(R.id.muac_chart_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utils.startAsyncTask(new ShowMuacChartTask(), null);
            }
        });
    }

    private void updateGenderInChildDetails() {
        if (childDetails != null) {
            String genderString = Utils.getValue(childDetails, PathConstants.KEY.GENDER, false);
            if (genderString.equalsIgnoreCase("ছেলে") || genderString.equalsIgnoreCase("male")) {
                childDetails.getDetails().put("gender", "male");
            } else if (genderString.equalsIgnoreCase("মেয়ে") || genderString.equalsIgnoreCase("female")) {
                childDetails.getDetails().put("gender", "female");
            } else {
                childDetails.getDetails().put("gender", "male");
            }
        }
    }

    private void showChildFollowupDetail() {

        String formMetadata = JsonFormUtils.getAutoPopulatedJsonEditFormString(this, mChildFollowupData, "child_followup", "Child Member Follow Up");

        Intent intent = new Intent(ChildImmunizationActivity.this, PathJsonFormActivity.class);
        intent.putExtra("json", formMetadata);
        startActivityForResult(intent, REQUEST_CODE_GET_JSON);
        // startActivity(intent);
    }

    private void showChildFollowupData() throws Exception {
        //Cursor c = db.rawQuery("SELECT Visit_date, is_pregnant, columnN FROM ec_followup;");
        SQLiteDatabase database = VaccinatorApplication.getInstance().getRepository().getReadableDatabase();
        Log.e(ChildImmunizationActivity.TAG, childDetails.entityId());
        Cursor cursor = database.rawQuery("SELECT * FROM ec_followup_child where base_entity_id = '" + childDetails.entityId() + "'", null);
        // Cursor cursor =  database.rawQuery("SELECT * FROM ec_followup where base_entity_id = '" + childDetails.entityId()+"'", null);
        mChildFollowupKeys = cursor.getColumnNames();
        mChildFollowupData = new HashMap<>();

        if (cursor != null) {
            cursor.moveToFirst();
            int i;
            for (i = 0; i < mChildFollowupKeys.length - 1; i++) {
                String key = mChildFollowupKeys[i];
                String value = cursor.getString(cursor.getColumnIndex(key));
                Log.e(ChildImmunizationActivity.class.getSimpleName(), key + ": " + value);
                mChildFollowupData.put(key, value);
                //String DestinationDB = cursor.getString(cursor.getColumnIndex("Name"));

            }
        }
//

//        Map<String,String> detailmaps = childDetails.getColumnmaps();
//        detailmaps.putAll(childDetails.getDetails());

        String visitDate = cursor.getString(cursor.getColumnIndex("Visit_date"));
        //   String visitDate = detailmaps.get("Visit_date");
        cursor.close();

        ((TextView) findViewById(R.id.child_visit_date)).setText(visitDate);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(EXTRA_CHILD_DETAILS, childDetails);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        Serializable serializable = savedInstanceState.getSerializable(EXTRA_CHILD_DETAILS);
        if (serializable != null && serializable instanceof CommonPersonObjectClient) {
            childDetails = (CommonPersonObjectClient) serializable;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (vaccineGroups != null) {
            LinearLayout vaccineGroupCanvasLL = (LinearLayout) findViewById(R.id.vaccine_group_canvas_ll);
            vaccineGroupCanvasLL.removeAllViews();
            vaccineGroups = null;
        }

        LinearLayout serviceGroupCanvasLL = (LinearLayout) findViewById(R.id.service_group_canvas_ll);
        serviceGroupCanvasLL.removeAllViews();

        updateViews();
        try {
            showChildFollowupData();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return false;
    }

    private boolean isDataOk() {
        return childDetails != null && childDetails.getDetails() != null;
    }

    public void updateViews() {
        findViewById(R.id.profile_name_layout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                launchDetailActivity(ChildImmunizationActivity.this, childDetails, null);
            }
        });

        // TODO: update all views using child data
        Map<String, String> details = detailsRepository.getAllDetailsForClient(childDetails.entityId());

        util.Utils.putAll(childDetails.getColumnmaps(), details);

        updateGenderViews();
        //  toolbar.setTitle(updateActivityTitle());
        updateAgeViews();
        updateChildIdViews();

        TextView titleTV = (TextView) toolbar.findViewById(R.id.title);
        String title = childDetails.getColumnmaps().get("first_name");
        titleTV.setText(title);

        String location = childDetails.getColumnmaps().get("stateProvince");
        toolbar.findViewById(R.id.location_name).setVisibility(View.VISIBLE);
        ((TextView) toolbar.findViewById(R.id.location_name)).setText(location);


        WeightRepository weightRepository = VaccinatorApplication.getInstance().weightRepository();

//        VaccineRepository vaccineRepository = VaccinatorApplication.getInstance().vaccineRepository();

        AlertService alertService = getOpenSRPContext().alertService();

        UpdateViewTask updateViewTask = new UpdateViewTask();
        updateViewTask.setWeightRepository(weightRepository);
        updateViewTask.setAlertService(alertService);
        Utils.startAsyncTask(updateViewTask, null);
        createWeightLayout((LinearLayout) findViewById(R.id.weight_group_canvas_ll), false, getLayoutInflater());
        CounsellingRepository counsellingRepository = VaccinatorApplication.getInstance().counsellingRepository();
        updateCounsellingViews(counsellingRepository.findByEntityId(childDetails.entityId()), (LinearLayout) findViewById(R.id.counselling_group_canvas_ll));
        refreshEditHeightLayout();
        refreshEditMuacLayout();
        updateProfileColor();
    }

    private void updateProfilePicture(Gender gender) {
        if (isDataOk()) {
            ImageView profileImageIV = (ImageView) findViewById(R.id.profile_image_iv);

            if (childDetails.entityId() != null) { //image already in local storage most likey ):
                //set profile image by passing the client id.If the image doesn't exist in the image repository then download and save locally
                profileImageIV.setTag(org.smartregister.R.id.entity_id, childDetails.entityId());
                DrishtiApplication.getCachedImageLoaderInstance().getImageByClientId(childDetails.entityId(), OpenSRPImageLoader.getStaticImageListener(profileImageIV, ImageUtils.profileImageResourceByGender(gender), ImageUtils.profileImageResourceByGender(gender)));

            }
        }
    }

    public void updateChildIdViews() {

        String name = "";
        String childId = "";
        if (isDataOk()) {
            name = constructChildName();
            childId = Utils.getValue(childDetails.getColumnmaps(), "openmrs_id", false);
        }

        TextView nameTV = (TextView) findViewById(R.id.name_tv);
        nameTV.setText(name);
        TextView childIdTV = (TextView) findViewById(R.id.child_id_tv);
        childIdTV.setText(childId);
        ((TextView) findViewById(R.id.mother_name_id_tv)).setText(Utils.getValue(childDetails.getColumnmaps(), PathConstants.KEY.MOTHER_FIRST_NAME, true));
        ((TextView) findViewById(R.id.father_name_id_tv)).setText(Utils.getValue(childDetails.getColumnmaps(), "Father_Guardian_Name", true));
        ((TextView) findViewById(R.id.birth_weight_id_tv)).setText(getValue(childDetails.getColumnmaps(), "Birth_Weight", true) + " kg");

        Utils.startAsyncTask(new GetSiblingsTask(), null);
    }

    public void updateAgeViews() {
        String dobString = "";
        String formattedAge = "";
        String formattedDob = "";
        if (isDataOk()) {
            dobString = Utils.getValue(childDetails.getColumnmaps(), PathConstants.KEY.DOB, false);
            if (!TextUtils.isEmpty(dobString)) {
                DateTime dateTime = new DateTime(dobString);
                Date dob = dateTime.toDate();
                formattedDob = DATE_FORMAT.format(dob);
                long timeDiff = Calendar.getInstance().getTimeInMillis() - dob.getTime();

                if (timeDiff >= 0) {
                    formattedAge = DateUtil.getDuration(timeDiff);
                }
            }
        }
        TextView dobTV = (TextView) findViewById(R.id.dob_tv);
        dobTV.setText(formattedDob);
        TextView ageTV = (TextView) findViewById(R.id.age_tv);
        ageTV.setText(formattedAge);
    }

    public void updateGenderViews() {
        Gender gender = getGender();
        updateGenderViews(gender);
    }

    @Override
    protected int[] updateGenderViews(Gender gender) {
        int[] selectedColor = super.updateGenderViews(gender);

        String identifier = getString(R.string.neutral_sex_id);
        int toolbarResource = 0;
        if (gender.equals(Gender.FEMALE)) {
            toolbarResource = R.drawable.vertical_separator_female;
            identifier = getString(R.string.female_sex_id);
        } else if (gender.equals(Gender.MALE)) {
            toolbarResource = R.drawable.vertical_separator_male;
            identifier = getString(R.string.male_sex_id);
        }
        toolbar.updateSeparatorView(toolbarResource);

        TextView childSiblingsTV = (TextView) findViewById(R.id.child_siblings_tv);
        //  childSiblingsTV.setText(String.format(getString(R.string.child_siblings), identifier).toUpperCase()); TODO
        updateProfilePicture(gender);

        return selectedColor;
    }

    private void createWeightLayout(LinearLayout fragmentContainer, boolean editmode, LayoutInflater inflater) {
        fragmentContainer.removeAllViews();
        fragmentContainer.addView(inflater.inflate(R.layout.previous_weightview, null));
        LinkedHashMap<Long, Pair<String, String>> weightmap = new LinkedHashMap<>();
        ArrayList<Boolean> weighteditmode = new ArrayList<Boolean>();
        ArrayList<View.OnClickListener> listeners = new ArrayList<View.OnClickListener>();

        WeightRepository wp = VaccinatorApplication.getInstance().weightRepository();
        List<Weight> weightlist = wp.getMaximum12(childDetails.entityId());
        /////////////////////////////////////////////////
        String dobString = Utils.getValue(childDetails.getColumnmaps(), PathConstants.KEY.DOB, false);
        Date dob = null;
        if (!TextUtils.isEmpty(Utils.getValue(childDetails.getColumnmaps(), PathConstants.KEY.BIRTH_WEIGHT, false))
                && !TextUtils.isEmpty(dobString)) {
            DateTime dateTime = new DateTime(dobString);
            dob = dateTime.toDate();
            Double birthWeight = Double.valueOf(Utils.getValue(childDetails.getColumnmaps(), PathConstants.KEY.BIRTH_WEIGHT, false));

            Weight weight = new Weight(-1l, null, (float) birthWeight.doubleValue(), dateTime.toDate(), null, null, null, Calendar.getInstance().getTimeInMillis(), null, null, 0);
            weightlist.add(weight);
        }

        ////////////////////////////////////////////////
        Gender gender = getGender();

        refreshPreviousWeightsTable(fragmentContainer, gender, dob, weightlist);

    }

    private void updateVaccinationViews(List<Vaccine> vaccineList, List<Alert> alerts) {
//        if(false) {
//            if (vaccineGroups == null) {
//                vaccineGroups = new ArrayList<>();
//                String supportedVaccinesString = VaccinatorUtils.getSupportedVaccines(this);
//
//                try {
//                    JSONArray supportedVaccines = new JSONArray(supportedVaccinesString);
//
//                    for (int i = 0; i < supportedVaccines.length(); i++) {
//                        JSONObject vaccineGroupObject = supportedVaccines.getJSONObject(i);
//
//                        //Add BCG2 special vaccine to birth vaccine group
//                        VaccinateActionUtils.addBcg2SpecialVaccine(this, vaccineGroupObject, vaccineList);
//
//                        addVaccineGroup(-1, vaccineGroupObject, vaccineList, alerts);
//                    }
//                } catch (JSONException e) {
//                    Log.e(TAG, Log.getStackTraceString(e));
//                }
//            }
//
//            showVaccineNotifications(vaccineList, alerts);
//        }
    }

    private void updateCounsellingViews(List<Counselling> counsellingList, LinearLayout counsellingCanvas) {
        counsellingCanvas.removeAllViews();
        LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        LinearLayout counselling_group = (LinearLayout) layoutInflater.inflate(R.layout.view_counselling_group, null, true);
        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
//        counselling_group.setLayoutParams(layoutParams);
        counsellingCanvas.addView(counselling_group);
        CounsellingCardAdapter counsellingCardAdapter = new CounsellingCardAdapter(this, counsellingList);
        ExpandableHeightGridView expandableHeightGridView = (ExpandableHeightGridView) counselling_group.findViewById(R.id.counselling_gv);
//        final float scale = getResources().getDisplayMetrics().density;
//        GridView.LayoutParams gridlayoutparams = new GridView.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
//                ViewGroup.LayoutParams.WRAP_CONTENT);
//        gridlayoutparams.height = (int)(scale*50*counsellingList.size());
        TextView recordnewCounselling = (TextView) counselling_group.findViewById(R.id.record_all_tv);
        recordnewCounselling.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String metadata = getmetaDataForLactatingCounsellingForm(childDetails);
                Intent intent = new Intent(ChildImmunizationActivity.this, PathJsonFormActivity.class);
                intent.putExtra("json", metadata);
                startActivityForResult(intent, REQUEST_CODE_GET_JSON);
            }
        });
        expandableHeightGridView.setExpanded(true);
        expandableHeightGridView.setAdapter(counsellingCardAdapter);
        counsellingCardAdapter.notifyDataSetChanged();

        counsellingCardAdapter.setActivityListener(new ActivityListener() {
            @Override
            public void onCallbackActivity(Counselling counselling) {
                Log.e(TAG, " Callback");
                mEditCounselling = counselling;
                Map<String, String> counsellingFormData = counselling.getFormfields();
                Intent intent = new Intent(ChildImmunizationActivity.this, PathJsonFormActivity.class);
                String formMetadata;

                formMetadata = JsonFormUtils.getAutoPopulatedJsonEditFormString(ChildImmunizationActivity.this,
                        counsellingFormData, "iycf_counselling_form_lactating_woman", "Lactating Woman Counselling");
                //intent = new Intent(WomanImmunizationActivity.this, PathJsonFormActivity.class);
                intent.putExtra("json", formMetadata);
                intent.putExtra("skipdialog", true);
                startActivityForResult(intent, REQUEST_CODE_GET_JSON_COUNSELLING_UPDATE);

            }
        });
    }

    private String getmetaDataForLactatingCounsellingForm(CommonPersonObjectClient pc) {
        int age_in_months = 0;
        if (isDataOk()) {
            String dobString = Utils.getValue(childDetails.getColumnmaps(), PathConstants.KEY.DOB, false);
            if (!TextUtils.isEmpty(dobString)) {
                DateTime dateTime = new DateTime(dobString);
                Date dob = dateTime.toDate();
                long timeDiff = Calendar.getInstance().getTimeInMillis() - dob.getTime();
                age_in_months = (int) Math.floor((float) timeDiff /
                        TimeUnit.MILLISECONDS.convert(30, TimeUnit.DAYS));
            }
        }


        org.smartregister.Context context = VaccinatorApplication.getInstance().context();
        try {
            JSONObject form = FormUtils.getInstance(this).getFormJson("iycf_counselling_form_lactating_woman");

            if (form != null) {


                JSONObject jsonObject = form;
                if (jsonObject.getString(JsonFormUtils.ENTITY_ID) != null) {
                    jsonObject.remove(JsonFormUtils.ENTITY_ID);
                    jsonObject.put(JsonFormUtils.ENTITY_ID, pc.entityId());
                }
                JSONObject stepOne = form.getJSONObject(JsonFormUtils.STEP1);
                JSONArray jsonArray = stepOne.getJSONArray(JsonFormUtils.FIELDS);

                if (pc.getColumnmaps().get("lactating_woman_counselling_actions_for_next_meeting") != null) {
                    if (!pc.getColumnmaps().get("lactating_woman_counselling_actions_for_next_meeting").equalsIgnoreCase("")) {
                        String valuelist = pc.getColumnmaps().get("lactating_woman_counselling_actions_for_next_meeting");
                        String ValueString = "";
                        if (valuelist.equalsIgnoreCase("If child is growth faltering for 2 or more months or severely underweight")) {
                            ValueString = "CHILD IS GROWTH FALTERING, SPEND MORE TIME ON ACTION PLAN AND ENSURING MOTHER UNDERSTANDS ALL ASPECTS DISCUSSED DURING INTERACTION. \n            Refer child to a social support program to receive extra assistance, or refer to health clinic\"";
                        }
                        if (valuelist.equalsIgnoreCase("All children")) {
                            ValueString = "Some possible actions:\n - Improve consistency of food that I give my child, make sure the food I give is thick enough (if between 6-23 months)\n- Give an egg to my child at least once every day\n- Talk to my husband to ask if he could feed the child one meal tomorrow\n- Corralling my chickens so my child is not exposed to them during the day and night";
                        }

                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject fieldjsonObject = jsonArray.getJSONObject(i);

                            if (fieldjsonObject.getString(JsonFormUtils.KEY)
                                    .equalsIgnoreCase("lactating_counselling_actions_decided_previous_meeting")) {
//                                fieldjsonObject.remove(JsonFormUtils.VALUE);
                                fieldjsonObject.put("hint", "In the last session, your resolution was- " + ValueString + "- Did you practice this resolution?");
                                fieldjsonObject.remove("hidden");
                                fieldjsonObject.put("hidden", false);
                            }

                        }
                    }
                }
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject fieldjsonObject = jsonArray.getJSONObject(i);
                    if (fieldjsonObject.getString(JsonFormUtils.KEY)
                            .equalsIgnoreCase("age_in_months_for_calculation_complimentary_feeding")) {
                        fieldjsonObject.remove(JsonFormUtils.VALUE);
                        fieldjsonObject.put(JsonFormUtils.VALUE, age_in_months);
                    }
                    if (fieldjsonObject.getString(JsonFormUtils.KEY)
                            .equalsIgnoreCase("complimentary_feeding_negative")) {
//                                fieldjsonObject.remove(JsonFormUtils.VALUE);
                        if (age_in_months >= 6 && age_in_months <= 8) {
                            complimentary_feeding_negative_6_to_8_months(fieldjsonObject);
                        } else if (age_in_months >= 9 && age_in_months <= 11) {
                            complimentary_feeding_negative_9_to_11_months(fieldjsonObject);
                        } else if (age_in_months >= 12 && age_in_months <= 23) {
                            complimentary_feeding_negative_12_to_23_months(fieldjsonObject);
                        }
                    }
                    if (fieldjsonObject.getString(JsonFormUtils.KEY)
                            .equalsIgnoreCase("complimentary_feeding_positive")) {
//                                fieldjsonObject.remove(JsonFormUtils.VALUE);
                        if (age_in_months >= 6 && age_in_months <= 8) {
                            complimentary_feeding_positive_6_to_8_months(fieldjsonObject);
                        } else if (age_in_months >= 9 && age_in_months <= 11) {
                            complimentary_feeding_positive_9_to_11_months(fieldjsonObject);
                        } else if (age_in_months >= 12 && age_in_months <= 23) {
                            complimentary_feeding_positive_12_to_23_months(fieldjsonObject);
                        }
                    }
                    //////complementary amount/////////////////

                    if (fieldjsonObject.getString(JsonFormUtils.KEY)
                            .equalsIgnoreCase("complimentary_feeding_amount_positive")) {
//                                fieldjsonObject.remove(JsonFormUtils.VALUE);
                        if (age_in_months >= 6 && age_in_months <= 8) {
                            complimentary_feeding_positive_amount(fieldjsonObject, " Give 2 to 3 tablespoonfuls (‘tastes’) at each feed.");
                        } else if (age_in_months >= 9 && age_in_months <= 11) {
                            complimentary_feeding_positive_amount(fieldjsonObject, "At each feed:Increase amount to half (½) cup (250 ml cup: show amount in cup brought by mother). Use a separate plate to make sure young child eats all the food given");
                        } else if (age_in_months >= 12 && age_in_months <= 23) {
                            complimentary_feeding_positive_amount(fieldjsonObject, "Increase amount to three-quarters (¾) to 1 cup (250 ml cup: how amount in cup brought by mother). Use a separate plate to make sure young child eats all the food given");
                        }
                    }
                    //////////////////////////////////////////////////////////////
                    if (fieldjsonObject.getString(JsonFormUtils.KEY)
                            .equalsIgnoreCase("complimentary_feeding_under_six_months")) {
//                                fieldjsonObject.remove(JsonFormUtils.VALUE);
                        if (age_in_months < 6) {
//                                    fieldjsonObject.remove("relevance");
                            complimentary_feeding_less_than_6_months(fieldjsonObject);
                        }
                    }
                    if (fieldjsonObject.getString(JsonFormUtils.KEY)
                            .equalsIgnoreCase("complimentary_feeding_4_months")) {
//                                fieldjsonObject.remove(JsonFormUtils.VALUE);
                        if (age_in_months == 4) {
                            complimentary_feeding_less_than_6_months(fieldjsonObject);

                        }
                    }
                    if (fieldjsonObject.getString(JsonFormUtils.KEY)
                            .equalsIgnoreCase("complimentary_feeding_5_months")) {
//                                fieldjsonObject.remove(JsonFormUtils.VALUE);
                        if (age_in_months == 5) {
                            complimentary_feeding_less_than_6_months(fieldjsonObject);
                        }
                    }
                    if (fieldjsonObject.getString(JsonFormUtils.KEY)
                            .equalsIgnoreCase("complimentary_feeding_amount_4_months")) {
//                                fieldjsonObject.remove(JsonFormUtils.VALUE);
                        if (age_in_months == 4) {
                            complimentary_feeding_less_than_6_months(fieldjsonObject);

                        }
                    }
                    if (fieldjsonObject.getString(JsonFormUtils.KEY)
                            .equalsIgnoreCase("complimentary_feeding_amount_5_months")) {
//                                fieldjsonObject.remove(JsonFormUtils.VALUE);
                        if (age_in_months == 5) {
                            complimentary_feeding_less_than_6_months(fieldjsonObject);
                        }
                    }
                }

//            intent.putExtra("json", form.toString());
//            startActivityForResult(intent, REQUEST_CODE_GET_JSON);
                return form.toString();
            }
        } catch (Exception e) {
            Log.e("exception counselling", e.getMessage());
        }

        return "";
    }

    private void complimentary_feeding_less_than_6_months(JSONObject fieldjsonObject) {
        try {
            JSONObject relevance = fieldjsonObject.getJSONObject("relevance");
            relevance.remove("step1:age_in_months_for_calculation_complimentary_feeding");
            JSONObject newCondition = new JSONObject();
            newCondition.put("type", "string");
            newCondition.put("ex", "regex(., \"(?i).*?\\bcomplementary_feeding\\b.*?\")");
            relevance.put("step1:nutrition_to_discuss", newCondition);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void complimentary_feeding_negative_6_to_8_months(JSONObject fieldjsonObject) {
        try {
            JSONObject relevance = fieldjsonObject.getJSONObject("relevance");
            JSONObject condition = relevance.getJSONObject("step1:complimentary_frequency");
            condition.remove("ex");
            condition.put("ex", "lessThan(., \"3\")");
            fieldjsonObject.remove("text");
            fieldjsonObject.put("text", "It is important to feed your baby complementary foods 3 times a day. Discuss ways to add another meal to a child’s diet.");

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void complimentary_feeding_positive_amount(JSONObject fieldjsonObject, String message) {
        try {
            JSONObject relevance = fieldjsonObject.getJSONObject("relevance");
            relevance.remove("step1:complimentary_amount");
            JSONObject newCondition = new JSONObject();
            newCondition.put("type", "string");
            newCondition.put("ex", "regex(., \"(?i).*?\\bcomplementary_feeding\\b.*?\")");
            relevance.put("step1:nutrition_to_discuss", newCondition);


            fieldjsonObject.remove("text");
            fieldjsonObject.put("text", message);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void complimentary_feeding_positive_6_to_8_months(JSONObject fieldjsonObject) {
        try {
            JSONObject relevance = fieldjsonObject.getJSONObject("relevance");
            JSONObject condition = relevance.getJSONObject("step1:complimentary_frequency");
            condition.remove("ex");
            condition.put("ex", "greaterThanEqualTo(., \"3\")");
            fieldjsonObject.remove("text");
            fieldjsonObject.put("text", "Congratulate mother and encourage her to continue");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void complimentary_feeding_negative_9_to_11_months(JSONObject fieldjsonObject) {
        try {
            JSONObject relevance = fieldjsonObject.getJSONObject("relevance");
            JSONObject condition = relevance.getJSONObject("step1:complimentary_frequency");
            condition.remove("ex");
            condition.put("ex", "lessThan(., \"4\")");
            fieldjsonObject.remove("text");
            fieldjsonObject.put("text", "It is important to feed your baby complementary foods 4 times a day.");

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    private void complimentary_feeding_positive_9_to_11_months(JSONObject fieldjsonObject) {
        try {
            JSONObject relevance = fieldjsonObject.getJSONObject("relevance");
            JSONObject condition = relevance.getJSONObject("step1:complimentary_frequency");
            condition.remove("ex");
            condition.put("ex", "greaterThanEqualTo(., \"4\")");
            fieldjsonObject.remove("text");
            fieldjsonObject.put("text", "Congratulate mother and encourage her to continue");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void complimentary_feeding_negative_12_to_23_months(JSONObject fieldjsonObject) {
        try {
            JSONObject relevance = fieldjsonObject.getJSONObject("relevance");
            JSONObject condition = relevance.getJSONObject("step1:complimentary_frequency");
            condition.remove("ex");
            condition.put("ex", "lessThan(., \"5\")");
            fieldjsonObject.remove("text");
            fieldjsonObject.put("text", " It is important to feed your baby complementary foods 5 times a day");

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void complimentary_feeding_positive_12_to_23_months(JSONObject fieldjsonObject) {
        try {
            JSONObject relevance = fieldjsonObject.getJSONObject("relevance");
            JSONObject condition = relevance.getJSONObject("step1:complimentary_frequency");
            condition.remove("ex");
            condition.put("ex", "greaterThanEqualTo(., \"5\")");
            fieldjsonObject.remove("text");
            fieldjsonObject.put("text", "Congratulate mother and encourage her to continue");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_CODE_GET_JSON) {

                String jsonString = data.getStringExtra("json");
                Log.d("JSONResult", jsonString);

                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
                AllSharedPreferences allSharedPreferences = new AllSharedPreferences(preferences);

                JsonFormUtils.saveForm(this, context(), jsonString, allSharedPreferences.fetchRegisteredANM());
                updateViews();
            }
            if (requestCode == REQUEST_CODE_GET_JSON_COUNSELLING_UPDATE) {
                String jsonString = data.getStringExtra("json");
                Log.d("JSONResult", jsonString);

                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
                AllSharedPreferences allSharedPreferences = new AllSharedPreferences(preferences);
                JsonFormUtils.updateCounsellingForm(this, context(), jsonString, allSharedPreferences.fetchRegisteredANM());
                mEditCounselling.setFormfields(JsonFormUtils.getCouncellingField());
                VaccinatorApplication.getInstance().counsellingRepository().
                        update(VaccinatorApplication.getInstance().getRepository().getReadableDatabase(), mEditCounselling);
            }


        }
    }

    protected org.smartregister.Context context() {
        return VaccinatorApplication.getInstance().context();
    }


    private void showVaccineNotifications(List<Vaccine> vaccineList, List<Alert> alerts) {

        DetailsRepository detailsRepository = VaccinatorApplication.getInstance().context().detailsRepository();
        Map<String, String> details = detailsRepository.getAllDetailsForClient(childDetails.entityId());

        if (details.containsKey(BCG2_NOTIFICATION_DONE)) {
            return;
        }

        if (VaccinateActionUtils.hasVaccine(vaccineList, VaccineRepo.Vaccine.bcg2)) {
            return;
        }

        Vaccine bcg = VaccinateActionUtils.getVaccine(vaccineList, VaccineRepo.Vaccine.bcg);
        if (bcg == null) {
            return;
        }

        Alert alert = VaccinateActionUtils.getAlert(alerts, VaccineRepo.Vaccine.bcg2);
        if (alert == null || alert.isComplete()) {
            return;
        }

        int bcgOffsetInWeeks = 12;
        Calendar twelveWeeksLaterDate = Calendar.getInstance();
        twelveWeeksLaterDate.setTime(bcg.getDate());
        twelveWeeksLaterDate.add(Calendar.WEEK_OF_YEAR, bcgOffsetInWeeks);

        Calendar today = Calendar.getInstance();

        if (today.getTime().after(twelveWeeksLaterDate.getTime()) || DateUtils.isSameDay(twelveWeeksLaterDate, today)) {
            showCheckBcgScarNotification(alert);
        }
    }

    public void addVaccineGroup(int canvasId, JSONObject vaccineGroupData, List<Vaccine> vaccineList, List<Alert> alerts) {

    }

//
//    private void updateWeightViews(Weight lastUnsyncedWeight) {
//
//        String childName = constructChildName();
//        String gender = Utils.getValue(childDetails.getColumnmaps(), PathConstants.KEY.GENDER, true);
//        String motherFirstName = Utils.getValue(childDetails.getColumnmaps(), PathConstants.KEY.MOTHER_FIRST_NAME, true);
//        if (StringUtils.isBlank(childName) && StringUtils.isNotBlank(motherFirstName)) {
//            childName = "B/o " + motherFirstName.trim();
//        }
//
//        String zeirId = Utils.getValue(childDetails.getColumnmaps(), PathConstants.KEY.ZEIR_ID, false);
//        String duration = "";
//        String dobString = Utils.getValue(childDetails.getColumnmaps(), PathConstants.KEY.DOB, false);
//        if (StringUtils.isNotBlank(dobString)) {
//            DateTime dateTime = new DateTime(Utils.getValue(childDetails.getColumnmaps(), PathConstants.KEY.DOB, false));
//            duration = DateUtil.getDuration(dateTime);
//        }
//
//        Photo photo = ImageUtils.profilePhotoByClient(childDetails);
//
//        WeightWrapper weightWrapper = new WeightWrapper();
//        weightWrapper.setId(childDetails.entityId());
//        weightWrapper.setGender(gender);
//        weightWrapper.setPatientName(childName);
//        weightWrapper.setPatientNumber(zeirId);
//        weightWrapper.setPatientAge(duration);
//        weightWrapper.setPhoto(photo);
//        weightWrapper.setPmtctStatus(Utils.getValue(childDetails.getColumnmaps(), PathConstants.KEY.PMTCT_STATUS, false));
//
//
//        String formattedAge = "";
//        String formattedDob = "";
//        if (isDataOk()) {
//            dobString = Utils.getValue(childDetails.getColumnmaps(), PathConstants.KEY.DOB, false);
//            if (!TextUtils.isEmpty(dobString)) {
//                DateTime dateTime = new DateTime(dobString);
//                Date dob = dateTime.toDate();
//                formattedDob = DATE_FORMAT.format(dob);
//                long timeDiff = Calendar.getInstance().getTimeInMillis() - dob.getTime();
//
//                if (timeDiff >= 0) {
//                    formattedAge = DateUtil.getDuration(timeDiff);
//                }
//            }
//        }
//
//
//        weightWrapper.setPatientAge(formattedAge);
//
//        if (lastUnsyncedWeight != null) {
//            weightWrapper.setWeight(lastUnsyncedWeight.getKg());
//            weightWrapper.setDbKey(lastUnsyncedWeight.getId());
//            weightWrapper.setUpdatedWeightDate(new DateTime(lastUnsyncedWeight.getDate()), false);
//        }
//
//        updateRecordWeightViews(weightWrapper);
//
//    }

//    private void updateRecordWeightViews(WeightWrapper weightWrapper) {
//        View recordWeight = findViewById(R.id.record_weight);
//        recordWeight.setClickable(true);
//        recordWeight.setBackground(getResources().getDrawable(R.drawable.record_weight_bg));
//
//        TextView recordWeightText = (TextView) findViewById(R.id.record_weight_text);
//        recordWeightText.setText(R.string.record_weight);
//
//        ImageView recordWeightCheck = (ImageView) findViewById(R.id.record_weight_check);
//        recordWeightCheck.setVisibility(View.GONE);
//        recordWeight.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                showWeightDialog(view);
//            }
//        });
//
////        if (weightWrapper.getDbKey() != null && weightWrapper.getWeight() != null) {
////            recordWeightText.setText(Utils.kgStringSuffix(weightWrapper.getWeight()));
////            recordWeightCheck.setVisibility(View.VISIBLE);
////
////            if (weightWrapper.getUpdatedWeightDate() != null) {
////                long timeDiff = Calendar.getInstance().getTimeInMillis() - weightWrapper.getUpdatedWeightDate().getMillis();
////
////                if (timeDiff <= TimeUnit.MILLISECONDS.convert(RECORD_WEIGHT_BUTTON_ACTIVE_MIN, TimeUnit.HOURS)) {
////                    //disable the button
////                    recordWeight.setClickable(false);
////                    recordWeight.setBackground(new ColorDrawable(getResources()
////                            .getColor(android.R.color.transparent)));
////                } else {
////                    //reset state
////                    recordWeight.setClickable(true);
////                    recordWeight.setBackground(getResources().getDrawable(R.drawable.record_weight_bg));
////                    recordWeightText.setText(R.string.record_weight);
////                    recordWeightCheck.setVisibility(View.GONE);
////                }
////            }
////        }
//
//        recordWeight.setTag(weightWrapper);
//
//    }

    private void showWeightDialog(View view) {
        FragmentTransaction ft = this.getFragmentManager().beginTransaction();
        Fragment prev = this.getFragmentManager().findFragmentByTag(DIALOG_TAG);
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);
        Date dob = null;
        String formattedDob = "";
        if (isDataOk()) {
            String dobString = Utils.getValue(childDetails.getColumnmaps(), PathConstants.KEY.DOB, false);
            if (!TextUtils.isEmpty(dobString)) {
                DateTime dateTime = new DateTime(dobString);
                dob = dateTime.toDate();
                formattedDob = DATE_FORMAT.format(dob);

            }
        }
        WeightWrapper weightWrapper = (WeightWrapper) view.getTag();
        RecordWeightDialogFragment recordWeightDialogFragment = RecordWeightDialogFragment.newInstance(dob, weightWrapper);
        recordWeightDialogFragment.show(ft, DIALOG_TAG);

    }

    private String readAssetContents(String path) {
        String fileContents = null;
        try {
            InputStream is = getAssets().open(path);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            fileContents = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            Log.e(TAG, ex.toString(), ex);
        }

        return fileContents;
    }

    public static void launchActivity(Context fromContext, CommonPersonObjectClient childDetails, RegisterClickables registerClickables) {
        Intent intent = new Intent(fromContext, ChildImmunizationActivity.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable(EXTRA_CHILD_DETAILS, childDetails);
        bundle.putSerializable(EXTRA_REGISTER_CLICKABLES, registerClickables);
        intent.putExtras(bundle);
        fromContext.startActivity(intent);
    }

    public void launchDetailActivity(Context fromContext, CommonPersonObjectClient childDetails, RegisterClickables registerClickables) {
        Intent intent = new Intent(fromContext, ChildDetailTabbedActivity.class);
        Bundle bundle = new Bundle();
        try {
            bundle.putString(PathConstants.KEY.LOCATION_NAME, JsonFormUtils.getOpenMrsLocationId(getOpenSRPContext(), toolbar.getCurrentLocation()));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        bundle.putSerializable(EXTRA_CHILD_DETAILS, childDetails);
        bundle.putSerializable(EXTRA_REGISTER_CLICKABLES, registerClickables);
        intent.putExtras(bundle);

        fromContext.startActivity(intent);
    }

    public String updateActivityTitle() {
        String name = "";
        if (isDataOk()) {
            name = constructChildName();
        }
        return String.format("%s > %s", getString(R.string.app_name), name.trim());
    }

    @Override
    public void onLocationChanged(final String newLocation) {
        // TODO: Do whatever needs to be done when the location is changed
    }

    @Override
    protected int getContentView() {
        return R.layout.activity_child_immunization;
    }

    @Override
    protected int getDrawerLayoutId() {
        return R.id.drawer_layout;
    }

    @Override
    protected int getToolbarId() {
        return LocationSwitcherToolbar.TOOLBAR_ID;
    }

    @Override
    protected Class onBackActivity() {
        return ChildSmartRegisterActivity.class;
    }

    @Override
    public void onWeightTaken(WeightWrapper tag) {
        if (tag != null) {
            final WeightRepository weightRepository = VaccinatorApplication.getInstance().weightRepository();
            Weight weight = new Weight();
//            if (tag.getDbKey() != null) {
//                weight = weightRepository.find(tag.getDbKey());
//            }
            weight.setBaseEntityId(childDetails.entityId());
            weight.setKg(tag.getWeight());
            weight.setDate(tag.getUpdatedWeightDate().toDate());
            weight.setAnmId(getOpenSRPContext().allSharedPreferences().fetchRegisteredANM());
            try {
                weight.setLocationId(JsonFormUtils.getOpenMrsLocationId(getOpenSRPContext(),
                        toolbar.getCurrentLocation()));
            } catch (JSONException e) {
                e.printStackTrace();
            }

            Gender gender = getGender();

            Date dob = null;

            String formattedAge = "";
            if (isDataOk()) {
                String dobString = Utils.getValue(childDetails.getColumnmaps(), PathConstants.KEY.DOB, false);
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
            // updateRecordWeightViews(tag);
            setLastModified(true);
            updateViews();
        }

    }

    @Override
    public void onVaccinateToday(ArrayList<VaccineWrapper> tags, View v) {
        if (tags != null && !tags.isEmpty()) {
            View view = getLastOpenedView();
            saveVaccine(tags, view);
        }
    }

    @Override
    public void onVaccinateEarlier(ArrayList<VaccineWrapper> tags, View v) {
        if (tags != null && !tags.isEmpty()) {
            View view = getLastOpenedView();
            saveVaccine(tags, view);
        }
    }

    @Override
    public void onUndoVaccination(VaccineWrapper tag, View v) {
        Utils.startAsyncTask(new UndoVaccineTask(tag, v), null);
    }

    private void addVaccinationDialogFragment(ArrayList<VaccineWrapper> vaccineWrappers, VaccineGroup vaccineGroup) {

        FragmentTransaction ft = this.getFragmentManager().beginTransaction();
        Fragment prev = this.getFragmentManager().findFragmentByTag(DIALOG_TAG);
        if (prev != null) {
            ft.remove(prev);
        }

        ft.addToBackStack(null);
        vaccineGroup.setModalOpen(true);
        String dobString = Utils.getValue(childDetails.getColumnmaps(), PathConstants.KEY.DOB, false);
        Date dob = Calendar.getInstance().getTime();
        if (!TextUtils.isEmpty(dobString)) {
            DateTime dateTime = new DateTime(dobString);
            dob = dateTime.toDate();
        }

        List<Vaccine> vaccineList = VaccinatorApplication.getInstance().vaccineRepository()
                .findByEntityId(childDetails.entityId());
        if (vaccineList == null) vaccineList = new ArrayList<>();

        VaccinationDialogFragment vaccinationDialogFragment = VaccinationDialogFragment.newInstance(dob, vaccineList, vaccineWrappers);
        vaccinationDialogFragment.show(ft, DIALOG_TAG);
    }

    private void performRegisterActions() {
        if (registerClickables != null) {
            if (registerClickables.isRecordWeight()) {
                final View recordWeight = findViewById(R.id.record_weight);
                recordWeight.post(new Runnable() {
                    @Override
                    public void run() {
                        recordWeight.performClick();
                    }
                });
            } else if (registerClickables.isRecordAll()) {
                performRecordAllClick(0);
            }

            //Reset register actions
            registerClickables.setRecordAll(false);
            registerClickables.setRecordWeight(false);
        }
    }

    private void performRecordAllClick(final int index) {
        if (vaccineGroups != null && vaccineGroups.size() > index) {
            final VaccineGroup vaccineGroup = vaccineGroups.get(index);
            vaccineGroup.post(new Runnable() {
                @Override
                public void run() {
                    ArrayList<VaccineWrapper> vaccineWrappers = vaccineGroup.getDueVaccines();
                    if (!vaccineWrappers.isEmpty()) {
                        final TextView recordAllTV = (TextView) vaccineGroup.findViewById(R.id.record_all_tv);
                        recordAllTV.post(new Runnable() {
                            @Override
                            public void run() {
                                recordAllTV.performClick();
                            }
                        });
                    } else {
                        performRecordAllClick(index + 1);
                    }
                }
            });
        }
    }

    private void saveVaccine(ArrayList<VaccineWrapper> tags, final View view) {
        if (tags.isEmpty()) {
            return;
        }

        VaccineRepository vaccineRepository = VaccinatorApplication.getInstance().vaccineRepository();

        VaccineWrapper[] arrayTags = tags.toArray(new VaccineWrapper[tags.size()]);
        SaveVaccinesTask backgroundTask = new SaveVaccinesTask();
        backgroundTask.setVaccineRepository(vaccineRepository);
        backgroundTask.setView(view);
        Utils.startAsyncTask(backgroundTask, arrayTags);

    }

    private void saveVaccine(VaccineRepository vaccineRepository, VaccineWrapper tag) {
        if (tag.getUpdatedVaccineDate() == null) {
            return;
        }


        Vaccine vaccine = new Vaccine();
        if (tag.getDbKey() != null) {
            vaccine = vaccineRepository.find(tag.getDbKey());
        }
        vaccine.setBaseEntityId(childDetails.entityId());
        vaccine.setName(tag.getName());
        vaccine.setDate(tag.getUpdatedVaccineDate().toDate());
        vaccine.setAnmId(getOpenSRPContext().allSharedPreferences().fetchRegisteredANM());
        try {
            vaccine.setLocationId(JsonFormUtils.getOpenMrsLocationId(getOpenSRPContext(),
                    toolbar.getCurrentLocation()));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String lastChar = vaccine.getName().substring(vaccine.getName().length() - 1);
        if (StringUtils.isNumeric(lastChar)) {
            vaccine.setCalculation(Integer.valueOf(lastChar));
        } else {
            vaccine.setCalculation(-1);
        }
        vaccineRepository.add(vaccine);
        tag.setDbKey(vaccine.getId());
        setLastModified(true);
    }

    private void updateVaccineGroupViews(View view, final ArrayList<VaccineWrapper> wrappers, List<Vaccine> vaccineList) {
        updateVaccineGroupViews(view, wrappers, vaccineList, false);
    }

    private void updateVaccineGroupViews(View view, final ArrayList<VaccineWrapper> wrappers, final List<Vaccine> vaccineList, final boolean undo) {
        if (view == null || !(view instanceof VaccineGroup)) {
            return;
        }
        final VaccineGroup vaccineGroup = (VaccineGroup) view;
        vaccineGroup.setModalOpen(false);

        if (Looper.myLooper() == Looper.getMainLooper()) {
            if (undo) {
                vaccineGroup.setVaccineList(vaccineList);
                vaccineGroup.updateWrapperStatus(wrappers, "child");
            }
            vaccineGroup.updateViews(wrappers);

        } else {
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(new Runnable() {
                @Override
                public void run() {
                    if (undo) {
                        vaccineGroup.setVaccineList(vaccineList);
                        vaccineGroup.updateWrapperStatus(wrappers, "child");
                    }
                    vaccineGroup.updateViews(wrappers);
                }
            });
        }
    }

    private void showRecordWeightNotification() {
        if (!weightNotificationShown) {
            weightNotificationShown = true;
            showNotification(R.string.record_weight_notification, R.drawable.ic_weight_notification,
                    R.string.record_weight,
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            View recordWeight = findViewById(R.id.record_weight);
                            showWeightDialog(recordWeight);
                            hideNotification();
                        }
                    }, R.string.cancel, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            hideNotification();
                        }
                    }, null);
        }
    }

    private void showCheckBcgScarNotification(Alert alert) {
        if (!bcgScarNotificationShown) {
            bcgScarNotificationShown = true;
            showNotification(R.string.check_child_bcg_scar, R.drawable.ic_check_bcg_scar,
                    R.string.ok_button_label, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            hideNotification();
                            Alert alert = (Alert) v.getTag();
                            if (alert != null) {
                                Utils.startAsyncTask(new MarkBcgTwoAsDoneTask(), null);
                            }
                        }
                    }, 0, null, alert);
        }
    }

    private String constructChildName() {
        String firstName = Utils.getValue(childDetails.getColumnmaps(), PathConstants.KEY.FIRST_NAME, true);
        String lastName = Utils.getValue(childDetails.getColumnmaps(), PathConstants.KEY.LAST_NAME, true).replaceAll(Pattern.quote("."), "");
        return Utils.getName(firstName, lastName).trim();
    }

    @Override
    public void finish() {
        if (isLastModified()) {
            String tableName = PathConstants.CHILD_TABLE_NAME;
            AllCommonsRepository allCommonsRepository = getOpenSRPContext().allCommonsRepositoryobjects(tableName);
            ContentValues contentValues = new ContentValues();
            contentValues.put(PathConstants.KEY.LAST_INTERACTED_WITH, (new Date()).getTime());
            allCommonsRepository.update(tableName, contentValues, childDetails.entityId());
            allCommonsRepository.updateSearch(childDetails.entityId());
        }
        super.finish();
    }

    private boolean isLastModified() {
        VaccinatorApplication application = (VaccinatorApplication) getApplication();
        return application.isLastModified();
    }

    private void setLastModified(boolean lastModified) {
        VaccinatorApplication application = (VaccinatorApplication) getApplication();
        if (lastModified != application.isLastModified()) {
            application.setLastModified(lastModified);
        }
    }

    private VaccineGroup getLastOpenedView() {
        if (vaccineGroups == null) {
            return null;
        }

        for (VaccineGroup vaccineGroup : vaccineGroups) {
            if (vaccineGroup.isModalOpen()) {
                return vaccineGroup;
            }
        }

        return null;
    }

    private void updateVaccineGroupsUsingAlerts(List<String> affectedVaccines, List<Vaccine> vaccineList, List<Alert> alerts) {
        if (affectedVaccines != null && vaccineList != null) {
            // Update all other affected vaccine groups
            HashMap<VaccineGroup, ArrayList<VaccineWrapper>> affectedGroups = new HashMap<>();
            for (String curAffectedVaccineName : affectedVaccines) {
                boolean viewFound = false;
                // Check what group it is in
                for (VaccineGroup curGroup : vaccineGroups) {
                    ArrayList<VaccineWrapper> groupWrappers = curGroup.getAllVaccineWrappers();
                    if (groupWrappers == null) groupWrappers = new ArrayList<>();
                    for (VaccineWrapper curWrapper : groupWrappers) {
                        String curWrapperName = curWrapper.getName();

                        // Check if current wrapper is one of the combined vaccines
                        if (COMBINED_VACCINES.contains(curWrapperName)) {
                            // Check if any of the sister vaccines is currAffectedVaccineName
                            String[] allSisters = COMBINED_VACCINES_MAP.get(curWrapperName).split(" / ");
                            for (String allSister : allSisters) {
                                if (allSister.replace(" ", "").equalsIgnoreCase(curAffectedVaccineName.replace(" ", ""))) {
                                    curWrapperName = allSister;
                                    break;
                                }
                            }
                        }

                        if (curWrapperName.replace(" ", "").toLowerCase()
                                .contains(curAffectedVaccineName.replace(" ", "").toLowerCase())) {
                            if (!affectedGroups.containsKey(curGroup)) {
                                affectedGroups.put(curGroup, new ArrayList<VaccineWrapper>());
                            }

                            affectedGroups.get(curGroup).add(curWrapper);
                            viewFound = true;
                        }

                        if (viewFound) break;
                    }

                    if (viewFound) break;
                }
            }

            for (VaccineGroup curGroup : affectedGroups.keySet()) {
                try {
                    vaccineGroups.remove(curGroup);
                    addVaccineGroup(Integer.valueOf((String) curGroup.getTag(R.id.vaccine_group_parent_id)),
                            new JSONObject((String) curGroup.getTag(R.id.vaccine_group_vaccine_data)),
                            vaccineList, alerts);
                } catch (Exception e) {
                    Log.e(TAG, Log.getStackTraceString(e));
                }
            }
        }
    }


    ////////////////////////////////////////////////////////////////
    // Inner classes
    ////////////////////////////////////////////////////////////////

    private class UpdateViewTask extends AsyncTask<Void, Void, Map<String, NamedObject<?>>> {

        //        private VaccineRepository vaccineRepository;
        private WeightRepository weightRepository;
        private RecurringServiceTypeRepository recurringServiceTypeRepository;
        private RecurringServiceRecordRepository recurringServiceRecordRepository;
        private AlertService alertService;


        public void setWeightRepository(WeightRepository weightRepository) {
            this.weightRepository = weightRepository;
        }

        public void setRecurringServiceTypeRepository(RecurringServiceTypeRepository recurringServiceTypeRepository) {
            this.recurringServiceTypeRepository = recurringServiceTypeRepository;
        }

        public void setRecurringServiceRecordRepository(RecurringServiceRecordRepository recurringServiceRecordRepository) {
            this.recurringServiceRecordRepository = recurringServiceRecordRepository;
        }

        public void setAlertService(AlertService alertService) {
            this.alertService = alertService;
        }


        @Override
        protected void onPreExecute() {
            showProgressDialog(getString(R.string.updating_dialog_title), null);
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void onPostExecute(Map<String, NamedObject<?>> map) {
            hideProgressDialog();

            List<Vaccine> vaccineList = new ArrayList<>();
            Weight weight = null;

            List<Alert> alertList = new ArrayList<>();

            if (map.containsKey(Weight.class.getName())) {
                NamedObject<?> namedObject = map.get(Weight.class.getName());
                if (namedObject != null) {
                    weight = (Weight) namedObject.object;
                }

            }

            if (map.containsKey(Vaccine.class.getName())) {
                NamedObject<?> namedObject = map.get(Vaccine.class.getName());
                if (namedObject != null) {
                    vaccineList = (List<Vaccine>) namedObject.object;
                }

            }

            if (map.containsKey(Alert.class.getName())) {
                NamedObject<?> namedObject = map.get(Alert.class.getName());
                if (namedObject != null) {
                    alertList = (List<Alert>) namedObject.object;
                }

            }

            // updateWeightViews(weight);
            updateVaccinationViews(vaccineList, alertList);
            performRegisterActions();
        }

        @Override
        protected Map<String, NamedObject<?>> doInBackground(Void... voids) {
            String dobString = Utils.getValue(childDetails.getColumnmaps(), PathConstants.KEY.DOB, false);
            if (!TextUtils.isEmpty(dobString)) {
                DateTime dateTime = new DateTime(dobString);
                VaccineSchedule.updateOfflineAlerts(childDetails.entityId(), dateTime, PathConstants.KEY.CHILD);
            }

            List<Vaccine> vaccineList = new ArrayList<>();
            Weight weight = null;


            List<Alert> alertList = new ArrayList<>();

            if (weightRepository != null) {
                weight = weightRepository.findUnSyncedByEntityId(childDetails.entityId());
            }

            if (alertService != null) {
                alertList = alertService.findByEntityId(childDetails.entityId());
            }

            Map<String, NamedObject<?>> map = new HashMap<>();

            NamedObject<List<Vaccine>> vaccineNamedObject = new NamedObject<>(Vaccine.class.getName(), vaccineList);
            map.put(vaccineNamedObject.name, vaccineNamedObject);

            NamedObject<Weight> weightNamedObject = new NamedObject<>(Weight.class.getName(), weight);
            map.put(weightNamedObject.name, weightNamedObject);


            NamedObject<List<Alert>> alertsNamedObject = new NamedObject<>(Alert.class.getName(), alertList);
            map.put(alertsNamedObject.name, alertsNamedObject);

            return map;
        }
    }


    private class ShowGrowthChartTask extends AsyncTask<Void, Void, List<Weight>> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showProgressDialog();
        }

        @Override
        protected List<Weight> doInBackground(Void... params) {
            WeightRepository weightRepository = VaccinatorApplication.getInstance().weightRepository();
            List<Weight> allWeights = weightRepository.findByEntityId(childDetails.entityId());
            try {
                String dobString = Utils.getValue(childDetails.getColumnmaps(), PathConstants.KEY.DOB, false);
                if (!TextUtils.isEmpty(Utils.getValue(childDetails.getColumnmaps(), PathConstants.KEY.BIRTH_WEIGHT, false))
                        && !TextUtils.isEmpty(dobString)) {
                    DateTime dateTime = new DateTime(dobString);
                    Double birthWeight = Double.valueOf(Utils.getValue(childDetails.getColumnmaps(), PathConstants.KEY.BIRTH_WEIGHT, false));

                    Weight weight = new Weight(-1l, null, (float) birthWeight.doubleValue(), dateTime.toDate(), null, null, null, Calendar.getInstance().getTimeInMillis(), null, null, 0);
                    allWeights.add(weight);
                }
            } catch (Exception e) {
                Log.e(TAG, Log.getStackTraceString(e));
            }

            return allWeights;
        }

        @Override
        protected void onPostExecute(List<Weight> allWeights) {
            super.onPostExecute(allWeights);
            hideProgressDialog();
            FragmentTransaction ft = ChildImmunizationActivity.this.getFragmentManager().beginTransaction();
            Fragment prev = ChildImmunizationActivity.this.getFragmentManager().findFragmentByTag(DIALOG_TAG);
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
            showProgressDialog();
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
            hideProgressDialog();
            FragmentTransaction ft = ChildImmunizationActivity.this.getFragmentManager().beginTransaction();
            Fragment prev = ChildImmunizationActivity.this.getFragmentManager().findFragmentByTag(DIALOG_TAG);
            if (prev != null) {
                ft.remove(prev);
            }
            ft.addToBackStack(null);
            String dobString = Utils.getValue(childDetails.getColumnmaps(), PathConstants.KEY.DOB, false);

            HeightMonitoringFragment growthDialogFragment = HeightMonitoringFragment.createInstance(dobString, getGender(), allHeight);
            growthDialogFragment.show(ft, DIALOG_TAG);
        }
    }

    private class ShowMuacChartTask extends AsyncTask<Void, Void, List<MUAC>> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showProgressDialog();
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
            hideProgressDialog();
            FragmentTransaction ft = ChildImmunizationActivity.this.getFragmentManager().beginTransaction();
            Fragment prev = ChildImmunizationActivity.this.getFragmentManager().findFragmentByTag(DIALOG_TAG);
            if (prev != null) {
                ft.remove(prev);
            }
            ft.addToBackStack(null);
            String dobString = Utils.getValue(childDetails.getColumnmaps(), PathConstants.KEY.DOB, false);

            MUACMonitoringFragment growthDialogFragment = MUACMonitoringFragment.createInstance(dobString, getGender(), allHeight);
            growthDialogFragment.show(ft, DIALOG_TAG);
        }
    }

    private Gender getGender() {
        Gender gender = Gender.UNKNOWN;
        String genderString = Utils.getValue(childDetails, PathConstants.KEY.GENDER, false);

        if (genderString != null && genderString.equalsIgnoreCase("female")) {
            gender = Gender.FEMALE;
        } else if (genderString != null && genderString.equalsIgnoreCase("male")) {
            gender = Gender.MALE;
        }
        return gender;
    }

    private class MarkBcgTwoAsDoneTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            DetailsRepository detailsRepository = VaccinatorApplication.getInstance().context().detailsRepository();
            detailsRepository.add(childDetails.entityId(), BCG2_NOTIFICATION_DONE, Boolean.TRUE.toString(), new Date().getTime());
            return null;
        }

    }

    private class SaveVaccinesTask extends AsyncTask<VaccineWrapper, Void, Pair<ArrayList<VaccineWrapper>, List<Vaccine>>> {

        private View view;
        private VaccineRepository vaccineRepository;
        private AlertService alertService;
        private List<String> affectedVaccines;
        private List<Vaccine> vaccineList;
        private List<Alert> alertList;

        public void setView(View view) {
            this.view = view;
        }

        public void setVaccineRepository(VaccineRepository vaccineRepository) {
            this.vaccineRepository = vaccineRepository;
            alertService = getOpenSRPContext().alertService();
            affectedVaccines = new ArrayList<>();
        }

        @Override
        protected void onPreExecute() {
            showProgressDialog();
        }

        @Override
        protected void onPostExecute(Pair<ArrayList<VaccineWrapper>, List<Vaccine>> pair) {
            hideProgressDialog();
            updateVaccineGroupViews(view, pair.first, pair.second);
            View recordWeight = findViewById(R.id.record_weight);
            WeightWrapper weightWrapper = (WeightWrapper) recordWeight.getTag();
            if (weightWrapper == null || weightWrapper.getWeight() == null) {
                showRecordWeightNotification();
            }

            updateVaccineGroupsUsingAlerts(affectedVaccines, vaccineList, alertList);
            showVaccineNotifications(vaccineList, alertList);
        }

        @Override
        protected Pair<ArrayList<VaccineWrapper>, List<Vaccine>> doInBackground(VaccineWrapper... vaccineWrappers) {

            ArrayList<VaccineWrapper> list = new ArrayList<>();
            if (vaccineRepository != null) {
                for (VaccineWrapper tag : vaccineWrappers) {
                    saveVaccine(vaccineRepository, tag);
                    list.add(tag);
                }
            }

            Pair<ArrayList<VaccineWrapper>, List<Vaccine>> pair = new Pair<>(list, vaccineList);
            String dobString = Utils.getValue(childDetails.getColumnmaps(), PathConstants.KEY.DOB, false);
            if (!TextUtils.isEmpty(dobString)) {
                DateTime dateTime = new DateTime(dobString);
                affectedVaccines = VaccineSchedule.updateOfflineAlerts(childDetails.entityId(), dateTime, PathConstants.KEY.CHILD);
            }
            vaccineList = vaccineRepository.findByEntityId(childDetails.entityId());
            alertList = alertService.findByEntityIdAndAlertNames(childDetails.entityId(),
                    VaccinateActionUtils.allAlertNames(PathConstants.KEY.CHILD));

            return pair;
        }
    }


    private class UndoVaccineTask extends AsyncTask<Void, Void, Void> {

        private final VaccineWrapper tag;
        private final View v;
        private final VaccineRepository vaccineRepository;
        private final AlertService alertService;
        private List<Vaccine> vaccineList;
        private List<Alert> alertList;
        private List<String> affectedVaccines;

        public UndoVaccineTask(VaccineWrapper tag, View v) {
            this.tag = tag;
            this.v = v;
            vaccineRepository = VaccinatorApplication.getInstance().vaccineRepository();
            alertService = getOpenSRPContext().alertService();
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... params) {
            if (tag != null) {

                if (tag.getDbKey() != null) {
                    Long dbKey = tag.getDbKey();
                    vaccineRepository.deleteVaccine(dbKey);
                    String dobString = Utils.getValue(childDetails.getColumnmaps(), PathConstants.KEY.DOB, false);
                    if (!TextUtils.isEmpty(dobString)) {
                        DateTime dateTime = new DateTime(dobString);
                        affectedVaccines = VaccineSchedule.updateOfflineAlerts(childDetails.entityId(), dateTime, PathConstants.KEY.CHILD);
                        vaccineList = vaccineRepository.findByEntityId(childDetails.entityId());
                        alertList = alertService.findByEntityIdAndAlertNames(childDetails.entityId(),
                                VaccinateActionUtils.allAlertNames(PathConstants.KEY.CHILD));
                    }
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void params) {
            super.onPostExecute(params);

            // Refresh the vaccine group with the updated vaccine
            tag.setUpdatedVaccineDate(null, false);
            tag.setDbKey(null);

            View view = getLastOpenedView();

            ArrayList<VaccineWrapper> wrappers = new ArrayList<>();
            wrappers.add(tag);
            updateVaccineGroupViews(view, wrappers, vaccineList, true);
            updateVaccineGroupsUsingAlerts(affectedVaccines, vaccineList, alertList);
            showVaccineNotifications(vaccineList, alertList);
        }
    }

    private class GetSiblingsTask extends AsyncTask<Void, Void, ArrayList<String>> {

        @Override
        protected ArrayList<String> doInBackground(Void... params) {
            String baseEntityId = childDetails.entityId();
            String motherBaseEntityId = Utils.getValue(childDetails.getColumnmaps(), PathConstants.KEY.RELATIONAL_ID, false);
            if (!TextUtils.isEmpty(motherBaseEntityId) && !TextUtils.isEmpty(baseEntityId)) {
                List<CommonPersonObject> children = getOpenSRPContext().commonrepository(PathConstants.CHILD_TABLE_NAME)
                        .findByRelational_IDs(motherBaseEntityId);

                if (children != null) {
                    ArrayList<String> baseEntityIds = new ArrayList<>();
                    for (CommonPersonObject curChild : children) {
                        if (!baseEntityId.equals(curChild.getCaseId())) {
                            baseEntityIds.add(curChild.getCaseId());
                        }
                    }

                    return baseEntityIds;
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(ArrayList<String> baseEntityIds) {
            super.onPostExecute(baseEntityIds);
            ArrayList<String> ids = new ArrayList<>();
            if (baseEntityIds != null) {
                ids = baseEntityIds;
            }

            Collections.reverse(ids);

            SiblingPicturesGroup siblingPicturesGroup = (SiblingPicturesGroup) ChildImmunizationActivity.this.findViewById(R.id.sibling_pictures);
            siblingPicturesGroup.setSiblingBaseEntityIds(ChildImmunizationActivity.this, ids);
        }
    }

    String weightText = "";

    private void refreshPreviousWeightsTable(final LinearLayout previousweightholder, Gender gender, Date dob, List<Weight> weights) {
        HashMap<Long, Weight> weightHashMap = new HashMap<>();
        for (Weight curWeight : weights) {
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

        weights = result;


        Calendar[] weighingDates = getMinAndMaxWeighingDates(dob);
        Calendar minWeighingDate = weighingDates[0];
        Calendar maxWeighingDate = weighingDates[1];
        if (minWeighingDate == null || maxWeighingDate == null) {
            return;
        }

        TableLayout tableLayout = (TableLayout) previousweightholder.findViewById(org.smartregister.growthmonitoring.R.id.weights_table);
//        tableLayout.removeAllViews();
        for (Weight weight : weights) {
            TableRow dividerRow = new TableRow(previousweightholder.getContext());
            View divider = new View(previousweightholder.getContext());
            TableRow.LayoutParams params = (TableRow.LayoutParams) divider.getLayoutParams();
            if (params == null) params = new TableRow.LayoutParams();
            params.width = TableRow.LayoutParams.MATCH_PARENT;
            params.height = getResources().getDimensionPixelSize(org.smartregister.growthmonitoring.R.dimen.weight_table_divider_height);
            params.span = 3;
            divider.setLayoutParams(params);
            divider.setBackgroundColor(getResources().getColor(org.smartregister.growthmonitoring.R.color.client_list_header_dark_grey));
            dividerRow.addView(divider);
            tableLayout.addView(dividerRow);

            TableRow curRow = new TableRow(previousweightholder.getContext());

            TextView ageTextView = new TextView(previousweightholder.getContext());
            ageTextView.setHeight(getResources().getDimensionPixelSize(org.smartregister.growthmonitoring.R.dimen.table_contents_text_height));
            ageTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                    getResources().getDimension(org.smartregister.growthmonitoring.R.dimen.weight_table_contents_text_size));
            ageTextView.setText(DateUtil.getDuration(weight.getDate().getTime() - dob.getTime()));
            ageTextView.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
            ageTextView.setTextColor(getResources().getColor(org.smartregister.growthmonitoring.R.color.client_list_grey));
            curRow.addView(ageTextView);

            TextView weightTextView = new TextView(previousweightholder.getContext());
            weightTextView.setHeight(getResources().getDimensionPixelSize(org.smartregister.growthmonitoring.R.dimen.table_contents_text_height));
            weightTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                    getResources().getDimension(org.smartregister.growthmonitoring.R.dimen.weight_table_contents_text_size));
            weightTextView.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
            weightTextView.setText(
                    String.format("%s %s", String.valueOf(weight.getKg()), getString(org.smartregister.growthmonitoring.R.string.kg)));
            weightTextView.setTextColor(getResources().getColor(org.smartregister.growthmonitoring.R.color.client_list_grey));
            curRow.addView(weightTextView);

            TextView zScoreTextView = new TextView(previousweightholder.getContext());
            zScoreTextView.setHeight(getResources().getDimensionPixelSize(org.smartregister.growthmonitoring.R.dimen.table_contents_text_height));
            zScoreTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                    getResources().getDimension(org.smartregister.growthmonitoring.R.dimen.weight_table_contents_text_size));
            zScoreTextView.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
//            if (weight.getDate().compareTo(maxWeighingDate.getTime()) > 0) {
//                zScoreTextView.setText("");
//            } else { //TODO
            Double zScoreDouble = ZScore.calculate(gender, dob, weight.getDate(), weight.getKg());
            double zScore = (zScoreDouble == null) ? 0 : zScoreDouble.doubleValue();
            // double zScore = ZScore.calculate(gender, dob, weight.getDate(), weight.getKg());
            zScore = ZScore.roundOff(zScore);
            zScoreTextView.setTextColor(getResources().getColor(ZScore.getZScoreColor(zScore)));
            zScoreTextView.setText(String.valueOf(zScore));
            //}
            curRow.addView(zScoreTextView);
            tableLayout.addView(curRow);
        }
        //Now set the expand button if items are too many

        if (weights.size() > 0) {
            Weight weight = weights.get(0);
            Double zScoreDouble = ZScore.calculate(gender, dob, weight.getDate(), weight.getKg());
            double zScore = (zScoreDouble == null) ? 0 : zScoreDouble.doubleValue();
            // double zScore = ZScore.calculate(gender, dob, weight.getDate(), weight.getKg());
            zScore = ZScore.roundOff(zScore);
            weightText = ZScore.getZScoreText(zScore);
        }
    }


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

        refreshEditHeightLayout();
        updateProfileColor();
    }

    @Override
    public void onMUACTaken(MUACWrapper muacWrapper) {
        if (muacWrapper != null) {
            final MUACRepository heightRepository = GrowthMonitoringLibrary.getInstance().getMuacRepository();
            MUAC height = new MUAC();
//            if (muacWrapper.getDbKey() != null) {
//                height = heightRepository.find(muacWrapper.getDbKey());
//            }
            height.setBaseEntityId(childDetails.entityId());
            height.setCm(muacWrapper.getHeight());
            height.setDate(muacWrapper.getUpdatedHeightDate().toDate());
            String anm = getOpenSRPContext().allSharedPreferences().fetchRegisteredANM();
            height.setAnmId(getOpenSRPContext().allSharedPreferences().fetchRegisteredANM());
            height.setLocationId(getOpenSRPContext().allSharedPreferences().fetchDefaultLocalityId(anm));
            height.setTeam(getOpenSRPContext().allSharedPreferences().fetchDefaultTeam(anm));
            height.setTeamId(getOpenSRPContext().allSharedPreferences().fetchDefaultTeamId(anm));


            String dobstring = childDetails.getColumnmaps().get("dob");
            GrowthUtil.DOB_STRING = dobstring;


            Date dob = null;
            if (!TextUtils.isEmpty(GrowthUtil.DOB_STRING)) {
                DateTime dateTime = new DateTime(GrowthUtil.DOB_STRING);
                dob = dateTime.toDate();
            }
            Gender gender = getGender();
            heightRepository.add(height);
            muacWrapper.setDbKey(height.getId());

        }
        MuactIntentServiceJob.scheduleJobImmediately(MuactIntentServiceJob.TAG);
        refreshEditMuacLayout();
        updateProfileColor();
    }

    String heightText = "";

    private void refreshEditHeightLayout() {
        LinearLayout fragmentContainer = (LinearLayout) findViewById(R.id.height_group_canvas_ll);
        fragmentContainer.removeAllViews();
        fragmentContainer.addView(getLayoutInflater().inflate(R.layout.previous_height_view, null));
        TableLayout heightTable = findViewById(R.id.heights_table);
        HeightRepository wp = GrowthMonitoringLibrary.getInstance().getHeightRepository();
        List<Height> heightList = wp.getMaximum12(childDetails.entityId());
        if (heightList.size() > 0) {
            try {
                HeightUtils.refreshPreviousHeightsTable(this, heightTable, getGender(), dobToDateTime(childDetails).toDate(), heightList, Calendar.getInstance());
            } catch (Exception e) {
                e.printStackTrace();
            }
            Height height = heightList.get(0);
            heightText = ZScore.getZScoreText(height.getZScore());
        }
    }

    int muakColor = 0;
    String muakText = "";

    private void refreshEditMuacLayout() {
        LinearLayout fragmentContainer = (LinearLayout) findViewById(R.id.muac_group_canvas_ll);
        fragmentContainer.removeAllViews();
        fragmentContainer.addView(getLayoutInflater().inflate(R.layout.previous_muac_view, null));
        TableLayout muacTable = findViewById(R.id.muac_table);
        MUACRepository wp = GrowthMonitoringLibrary.getInstance().getMuacRepository();
        List<MUAC> heightList = wp.getMaximum12(childDetails.entityId());
        if (heightList.size() > 0) {
            MUACUtils.refreshPreviousMuacTable(this, muacTable, getGender(), dobToDateTime(childDetails).toDate(), heightList);
            MUAC latestMuac = heightList.get(0);
            muakColor = ZScore.getMuacColor(latestMuac.getCm());
            muakText = ZScore.getMuacText(latestMuac.getCm());
        }

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
            resultText = "SAM";
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

        if(!resultText.isEmpty()){
            muacText.setVisibility(View.VISIBLE);
            muacText.setText(resultText);
            muacText.setBackgroundColor(getResources().getColor(resultColor));
        }

    }

    private static final int GRAPH_MONTHS_TIMELINE = 12;

    private Calendar[] getMinAndMaxWeighingDates(Date dob) {
        Calendar minGraphTime = null;
        Calendar maxGraphTime = null;
        if (dob != null) {
            Calendar dobCalendar = Calendar.getInstance();
            dobCalendar.setTime(dob);
            standardiseCalendarDate(dobCalendar);

            minGraphTime = Calendar.getInstance();
            maxGraphTime = Calendar.getInstance();

            if (ZScore.getAgeInMonths(dob, maxGraphTime.getTime()) > ZScore.MAX_REPRESENTED_AGE) {
                Calendar cal = Calendar.getInstance();
                cal.setTime(dob);
                cal.add(Calendar.MONTH, (int) Math.round(ZScore.MAX_REPRESENTED_AGE));
                maxGraphTime = cal;
                minGraphTime = (Calendar) maxGraphTime.clone();
            }

            minGraphTime.add(Calendar.MONTH, -GRAPH_MONTHS_TIMELINE);
            standardiseCalendarDate(minGraphTime);
            standardiseCalendarDate(maxGraphTime);

            if (minGraphTime.getTimeInMillis() < dobCalendar.getTimeInMillis()) {
                minGraphTime.setTime(dob);
                standardiseCalendarDate(minGraphTime);

                maxGraphTime = (Calendar) minGraphTime.clone();
                maxGraphTime.add(Calendar.MONTH, GRAPH_MONTHS_TIMELINE);
            }
        }

        return new Calendar[]{minGraphTime, maxGraphTime};
    }

    private static void standardiseCalendarDate(Calendar calendarDate) {
        calendarDate.set(Calendar.HOUR_OF_DAY, 0);
        calendarDate.set(Calendar.MINUTE, 0);
        calendarDate.set(Calendar.SECOND, 0);
        calendarDate.set(Calendar.MILLISECOND, 0);
    }


    private class NamedObject<T> {
        public final String name;
        public final T object;

        public NamedObject(String name, T object) {
            this.name = name;
            this.object = object;
        }
    }

}
