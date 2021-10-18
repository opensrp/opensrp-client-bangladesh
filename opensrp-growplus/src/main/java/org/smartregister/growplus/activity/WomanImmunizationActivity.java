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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import net.sqlcipher.database.SQLiteDatabase;

import org.apache.commons.lang3.StringUtils;
import org.smartregister.commonregistry.AllCommonsRepository;
import org.smartregister.commonregistry.CommonPersonObject;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.commonregistry.CommonRepository;
import org.smartregister.domain.Alert;
import org.smartregister.domain.Photo;
import org.smartregister.growplus.adapter.CounsellingCardAdapter;
import org.smartregister.growplus.adapter.WomenFollowupRecyclerViewAdapter;
import org.smartregister.growplus.domain.Counselling;
import org.smartregister.growplus.listener.ActivityListener;
import org.smartregister.growplus.repository.CounsellingRepository;
import org.smartregister.growplus.repository.UniqueIdRepository;
import org.smartregister.growplus.view.LocationPickerView;
import org.smartregister.growthmonitoring.domain.Weight;
import org.smartregister.growthmonitoring.domain.WeightWrapper;
import org.smartregister.growthmonitoring.fragment.GrowthDialogFragment;
import org.smartregister.growthmonitoring.listener.WeightActionListener;
import org.smartregister.growthmonitoring.repository.WeightRepository;
import org.smartregister.immunization.domain.ServiceRecord;
import org.smartregister.immunization.domain.ServiceType;
import org.smartregister.immunization.domain.ServiceWrapper;
import org.smartregister.immunization.domain.Vaccine;
import org.smartregister.immunization.domain.VaccineSchedule;
import org.smartregister.immunization.domain.VaccineWrapper;
import org.smartregister.immunization.fragment.ServiceDialogFragment;
import org.smartregister.immunization.fragment.UndoServiceDialogFragment;
import org.smartregister.immunization.fragment.UndoVaccinationDialogFragment;
import org.smartregister.immunization.fragment.VaccinationDialogFragment;
import org.smartregister.immunization.listener.VaccinationActionListener;
import org.smartregister.immunization.repository.RecurringServiceRecordRepository;
import org.smartregister.immunization.repository.RecurringServiceTypeRepository;
import org.smartregister.immunization.repository.VaccineRepository;
import org.smartregister.immunization.util.VaccinateActionUtils;
import org.smartregister.immunization.view.ExpandableHeightGridView;
import org.smartregister.immunization.view.ServiceGroup;
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
import org.smartregister.view.activity.DrishtiApplication;
import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.opensrp.api.constants.Gender;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import util.ImageUtils;
import util.JsonFormUtils;
import util.PathConstants;

import static org.smartregister.growplus.activity.WomanSmartRegisterActivity.REQUEST_CODE_GET_JSON;
import static org.smartregister.util.Utils.fillValue;
import static org.smartregister.util.Utils.formatValue;
import static org.smartregister.util.Utils.getName;
import static org.smartregister.util.Utils.getValue;
import static org.smartregister.util.Utils.kgStringSuffix;
import static org.smartregister.util.Utils.startAsyncTask;


/**
 * Created by Jason Rogena - jrogena@ona.io on 16/02/2017.
 */

public class WomanImmunizationActivity extends BaseActivity
        implements LocationSwitcherToolbar.OnLocationChangeListener, WeightActionListener, VaccinationActionListener{

    private static final String TAG = "ChildImmunoActivity";
    private static final String VACCINES_FILE = "vaccines.json";
    private static final String EXTRA_CHILD_DETAILS = "child_details";
    private static final String EXTRA_REGISTER_CLICKABLES = "register_clickables";
    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy");
    private static final String DIALOG_TAG = "ChildImmunoActivity_DIALOG_TAG";
    private static final int REQUEST_CODE_GET_JSON_COUNSELLING_UPDATE = 512;
    private ArrayList<VaccineGroup> vaccineGroups;
    private ArrayList<ServiceGroup> serviceGroups;
    private static final ArrayList<String> COMBINED_VACCINES;
    private static final HashMap<String, String> COMBINED_VACCINES_MAP;
    private boolean bcgScarNotificationShown;
    private boolean weightNotificationShown;
    private Button mFollowupDetail;
    private Map<String, String> mWomenFollowupData;

    private String[] mWomenFollowupKeys; // = { "Visit_date", "is_pregnant","date_of_delivery", "Date_Of_next_appointment" };

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
    public org.smartregister.Context context;
    private WomenFollowupRecyclerViewAdapter mWomenFollowupRecyclerViewAdapter;
//    private String[] mWomenCounsellingKeys;
//    private Map<String, String> mWomenCounsellingData;
    private Counselling mEditCounselling;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        detailsRepository = org.smartregister.Context.getInstance().detailsRepository();

        toolbar = (LocationSwitcherToolbar) getToolbar();
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(WomanImmunizationActivity.this, WomanSmartRegisterActivity.class);
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
        }

        bcgScarNotificationShown = false;
        weightNotificationShown = false;

        toolbar.init(this);
        setLastModified(false);

        mFollowupDetail = (Button) findViewById(R.id.followup_details);
        mFollowupDetail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showWomenFollowupDetail();
            }
        });

        try {
            showWomenFollowupData();
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    private void showWomenFollowupDetail() {

//        final AlertDialog.Builder mBuilder = new AlertDialog.Builder(WomanImmunizationActivity.this);
//        mBuilder.setTitle("Followup Details");
//        LayoutInflater inflater = getLayoutInflater();
//        View convertView = inflater.inflate(R.layout.followup_detail_dialog, null);
//
//        mWomenFollowupRecyclerViewAdapter = new WomenFollowupRecyclerViewAdapter(this, mWomenFollowupData, mWomenFollowupKeys);
//
//        RecyclerView list = convertView.findViewById(R.id.women_followup_recycler_view);
//        list.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
//        list.setAdapter(mWomenFollowupRecyclerViewAdapter);
//        mBuilder.setView(convertView); // setView
//
//        AlertDialog dialog = mBuilder.create();
//        dialog.setCancelable(true);
//        dialog.setCanceledOnTouchOutside(true);
//        dialog.show();

//        Map<String,String> detailmaps = childDetails.getColumnmaps();
//        detailmaps.putAll(childDetails.getDetails());
//        Map<String,String> detailmaps = childDetails.getColumnmaps();
//        detailmaps.putAll(childDetails.getDetails());
        String formMetadata = JsonFormUtils.getAutoPopulatedJsonEditFormString(this, mWomenFollowupData, "woman_followup", "Woman Member Follow Up");

        Intent intent = new Intent(WomanImmunizationActivity.this, PathJsonFormActivity.class);
        intent.putExtra("json", formMetadata);
        startActivityForResult(intent, REQUEST_CODE_GET_JSON);
       // startActivity(intent);
    }

    private void showWomenFollowupData() throws Exception {
        //Cursor c = db.rawQuery("SELECT Visit_date, is_pregnant, columnN FROM ec_followup;");
        SQLiteDatabase database =  VaccinatorApplication.getInstance().getRepository().getReadableDatabase();
        Cursor cursor =  database.rawQuery("SELECT * FROM ec_followup where mother_id = '" + childDetails.entityId()+"'", null);
       // Cursor cursor =  database.rawQuery("SELECT * FROM ec_followup where base_entity_id = '" + childDetails.entityId()+"'", null);
        mWomenFollowupKeys = cursor.getColumnNames();
        mWomenFollowupData = new HashMap<>();

        if (cursor != null) {
            cursor.moveToFirst();
            int i;
            for (i = 0; i < mWomenFollowupKeys.length - 1; i++) {
                String key = mWomenFollowupKeys[i];
                String value = cursor.getString(cursor.getColumnIndex(key));
                Log.e(WomanImmunizationActivity.class.getSimpleName(), key + ": " + value);
                mWomenFollowupData.put(key, value);
                //String DestinationDB = cursor.getString(cursor.getColumnIndex("Name"));

            }
        }
//

//        Map<String,String> detailmaps = childDetails.getColumnmaps();
//        detailmaps.putAll(childDetails.getDetails());

        String visitDate = cursor.getString(cursor.getColumnIndex("Visit_date"));
     //   String visitDate = detailmaps.get("Visit_date");
        cursor.close();

        ((TextView)findViewById(R.id.visit_date)).setText(visitDate);
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

        if (serviceGroups != null) {
            LinearLayout serviceGroupCanvasLL = (LinearLayout) findViewById(R.id.service_group_canvas_ll);
            serviceGroupCanvasLL.removeAllViews();
            serviceGroups = null;
        }
        updateViews();
    }

    private boolean isDataOk() {
        return childDetails != null && childDetails.getDetails() != null;
    }

    private void updateViews() {
        ((LinearLayout) findViewById(R.id.profile_name_layout)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                launchDetailActivity(WomanImmunizationActivity.this, childDetails, null);
            }
        });
        // TODO: update all views using child data
        Map<String, String> details = detailsRepository.getAllDetailsForClient(childDetails.entityId());
        //details.putAll(childDetails.getColumnmaps());
        //):( prrrr
        childDetails.getColumnmaps().putAll(details);
        updateGenderViews();
        toolbar.setTitle(updateActivityTitle());
        updateAgeViews();
        updateChildIdViews();

        WeightRepository weightRepository = VaccinatorApplication.getInstance().weightRepository();

        VaccineRepository vaccineRepository = VaccinatorApplication.getInstance().vaccineRepository();


        AlertService alertService = getOpenSRPContext().alertService();
        if(!StringUtils.isBlank(getValue(childDetails.getColumnmaps(), "lmp", false))) {
            UpdateViewTask updateViewTask = new UpdateViewTask();
            updateViewTask.setWeightRepository(weightRepository);
//            updateViewTask.setVaccineRepository(vaccineRepository);
//        updateViewTask.setRecurringServiceTypeRepository(recurringServiceTypeRepository);
//        updateViewTask.setRecurringServiceRecordRepository(recurringServiceRecordRepository);
            updateViewTask.setAlertService(alertService);
            startAsyncTask(updateViewTask, null);
        }
        CounsellingRepository counsellingRepository= VaccinatorApplication.getInstance().counsellingRepository();
        updateCounsellingViews(counsellingRepository.findByEntityId(childDetails.entityId()),(LinearLayout)findViewById(R.id.counselling_group_canvas_ll));
        try {
            showWomenFollowupData();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void updateProfilePicture(Gender gender) {
        if (isDataOk()) {
            ImageView profileImageIV = (ImageView) findViewById(R.id.profile_image_iv);

            if (childDetails.entityId() != null) {//image already in local storage most likey ):
                //set profile image by passing the client id.If the image doesn't exist in the image repository then download and save locally
                profileImageIV.setTag(org.smartregister.R.id.entity_id, childDetails.entityId());
                DrishtiApplication.getCachedImageLoaderInstance().getImageByClientId(childDetails.entityId(), OpenSRPImageLoader.getStaticImageListener((ImageView) profileImageIV, R.drawable.woman_path_register_logo, R.drawable.woman_path_register_logo));

            }
        }
    }

    public void updateChildIdViews() {
        String name = "";
        String childId = "";
        if (isDataOk()) {
            name = constructChildName();
            childId = getValue(childDetails.getColumnmaps(), "openmrs_id", false);
        }

        TextView nameTV = (TextView) findViewById(R.id.name_tv);
        nameTV.setText(name);
        TextView childIdTV = (TextView) findViewById(R.id.child_id_tv);
        childIdTV.setText(String.format("%s: %s", getString(R.string.label_openmrsid), childId));

        String dobString = "";
        String formattedDob = "";
        if (isDataOk()) {
            dobString = getValue(childDetails.getColumnmaps(), "dob", false);
            if (!TextUtils.isEmpty(dobString)) {
                DateTime dateTime = new DateTime(dobString);
                Date dob = dateTime.toDate();
                formattedDob = DATE_FORMAT.format(dob);
            }
        }

        String openmrsid = childId;
        String birthdate = formattedDob;
        String husbaname = getValue(childDetails.getColumnmaps(), "spouseName", false);
        String contactno = getValue(childDetails.getColumnmaps(), "phoneNumber", false);
        childIdTV.setText(openmrsid);
        ((TextView) findViewById(R.id.birthdate_id_tv)).setText(birthdate);
        ((TextView) findViewById(R.id.husband_name_id_tv)).setText(husbaname);
        ((TextView) findViewById(R.id.contact_number_id_tv)).setText(contactno);
        ImageView add_child = (ImageView)findViewById(R.id.add_child);
        add_child.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                String metadata = getmetaDataForEditForm(childDetails);
                Intent intent = new Intent(WomanImmunizationActivity.this, PathJsonFormActivity.class);

                intent.putExtra("json", metadata);

                startActivityForResult(intent, REQUEST_CODE_GET_JSON);

            }
        });
        final String lmpstring = getValue(childDetails.getColumnmaps(), "lmp", false);
        final String eddstring = getValue(childDetails.getColumnmaps(), "edd", false);
        String pregnant = "No";
        if(childDetails.getColumnmaps().get("pregnant")!=null){
            if(childDetails.getColumnmaps().get("pregnant").equalsIgnoreCase("Yes")){
                pregnant = "Yes";

            }
        }

        fillValue((TextView) findViewById(R.id.lmp_id_tv), lmpstring);
        fillValue((TextView) findViewById(R.id.edd_id_tv), eddstring);
        fillValue((TextView) findViewById(R.id.pregnant_id_tv), pregnant);
        startAsyncTask(new GetSiblingsTask(), null);
    }

    private String getmetaDataForEditForm(CommonPersonObjectClient pc) {
        org.smartregister.Context context = VaccinatorApplication.getInstance().context();
        try {
            JSONObject form = FormUtils.getInstance(this).getFormJson("child_enrollment");
            LocationPickerView lpv = new LocationPickerView(this);
            lpv.init(context);
            JsonFormUtils.addHouseholdRegLocHierarchyQuestions(form, context);
            Log.d("add child form", "Form is " + form.toString());
            if (form != null) {
                JSONObject metaDataJson = form.getJSONObject("metadata");
                JSONObject lookup = metaDataJson.getJSONObject("look_up");
                lookup.put("entity_id", "mother");
                lookup.put("value", pc.entityId());

                UniqueIdRepository uniqueIdRepo = VaccinatorApplication.getInstance().uniqueIdRepository();
                String entityId = uniqueIdRepo.getNextUniqueId() != null ? uniqueIdRepo.getNextUniqueId().getOpenmrsId() : "";
                if (entityId.isEmpty()) {
                    Toast.makeText(context.applicationContext(), context.getInstance().applicationContext().getString(R.string.no_openmrs_id), Toast.LENGTH_SHORT).show();
                }
//
//                JSONObject stepOne = form.getJSONObject(JsonFormUtils.STEP1);
//                JSONArray jsonArray = stepOne.getJSONArray(JsonFormUtils.FIELDS);
//                for (int i = 0; i < jsonArray.length(); i++) {
//                    JSONObject jsonObject = jsonArray.getJSONObject(i);
//                    if (jsonObject.getString(JsonFormUtils.KEY)
//                            .equalsIgnoreCase(JsonFormUtils.OpenMRS_ID)) {
//                        jsonObject.remove(JsonFormUtils.VALUE);
//                        jsonObject.put(JsonFormUtils.VALUE, entityId);
//                        continue;
//                    }
//                }
                String locationid = "";
                DetailsRepository detailsRepository;
                detailsRepository = org.smartregister.Context.getInstance().detailsRepository();
                Map<String, String> details = detailsRepository.getAllDetailsForClient(pc.entityId());
                locationid = JsonFormUtils.getOpenMrsLocationId(context,getValue(details, "address3", false) );

                String birthFacilityHierarchy = JsonFormUtils.getOpenMrsLocationHierarchy(
                        context,locationid ).toString();
                //inject zeir id into the form
                JSONObject stepOne = form.getJSONObject(JsonFormUtils.STEP1);
                JSONArray jsonArray = stepOne.getJSONArray(JsonFormUtils.FIELDS);
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    if (jsonObject.getString(JsonFormUtils.KEY).equalsIgnoreCase(JsonFormUtils.OpenMRS_ID)) {
                        jsonObject.remove(JsonFormUtils.VALUE);
                        jsonObject.put(JsonFormUtils.VALUE, entityId);
                    }
                    if (jsonObject.getString(JsonFormUtils.KEY).equalsIgnoreCase("HIE_FACILITIES")) {
                        jsonObject.put(JsonFormUtils.VALUE, birthFacilityHierarchy);

                    }
                    if (jsonObject.getString(JsonFormUtils.KEY).equalsIgnoreCase("Mother_Guardian_First_Name")) {
                        jsonObject.put(JsonFormUtils.READ_ONLY, true);
                        jsonObject.put(JsonFormUtils.VALUE, (getValue(pc.getDetails(), "first_name", true).isEmpty() ? getValue(pc.getDetails(), "first_name", true) : getValue(pc.getDetails(), "first_name", true)));

                    }
                    if (jsonObject.getString(JsonFormUtils.KEY).equalsIgnoreCase("Mother_Guardian_Last_Name")) {
                        jsonObject.put(JsonFormUtils.READ_ONLY, true);
                        jsonObject.put(JsonFormUtils.VALUE, (getValue(pc.getDetails(), "last_name", true).isEmpty() ? getValue(pc.getDetails(), "last_name", true) : getValue(pc.getDetails(), "last_name", true)));
                    }
                    if (jsonObject.getString(JsonFormUtils.KEY).equalsIgnoreCase("Mother_Guardian_Date_Birth")) {
                        jsonObject.put(JsonFormUtils.READ_ONLY, true);
                        if (!TextUtils.isEmpty(getValue(pc.getDetails(), "dob", true))) {
                            try {
                                DateTime dateTime = new DateTime(getValue(pc.getDetails(), "dob", true));
                                Date dob = dateTime.toDate();
                                Date defaultDate = DATE_FORMAT.parse(JsonFormUtils.MOTHER_DEFAULT_DOB);
                                long timeDiff = Math.abs(dob.getTime() - defaultDate.getTime());
                                if (timeDiff > 86400000) {// Mother's date of birth occurs more than a day from the default date
                                    jsonObject.put(JsonFormUtils.VALUE, DATE_FORMAT.format(dob));
                                }
                            } catch (Exception e) {
                            }
                        }
                    }





                }
//            intent.putExtra("json", form.toString());
//            startActivityForResult(intent, REQUEST_CODE_GET_JSON);
                return form.toString();
            }
        } catch (Exception e) {
            Log.e("exception in addchild", e.getMessage());
        }

        return "";
    }

    public void updateAgeViews() {
        String dobString = "";
        String formattedAge = "";
        String formattedDob = "";
        if (isDataOk()) {
            dobString = getValue(childDetails.getColumnmaps(), "dob", false);
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
        dobTV.setText("");
        TextView ageTV = (TextView) findViewById(R.id.age_tv);
        ageTV.setText(formattedAge);
    }

    public void updateGenderViews() {
        Gender gender = Gender.MALE;
        if (isDataOk()) {
            String genderString = getValue(childDetails, "gender", false);
            if (genderString != null && genderString.toLowerCase().equals("female")) {
                gender = Gender.FEMALE;
            } else if (genderString != null && genderString.toLowerCase().equals("male")) {
                gender = Gender.MALE;
            }
        }
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
        childSiblingsTV.setText(
                "Her Children".toUpperCase());
        updateProfilePicture(gender);

        return selectedColor;
    }

    private void updateServiceViews(Map<String, List<ServiceType>> serviceTypeMap, List<ServiceRecord> serviceRecordList, List<Alert> alerts) {

        Map<String, List<ServiceType>> foundServiceTypeMap = new LinkedHashMap<>();
        if (serviceGroups == null) {
            for (String type : serviceTypeMap.keySet()) {
                if (foundServiceTypeMap.containsKey(type)) {
                    continue;
                }

                for (ServiceRecord serviceRecord : serviceRecordList) {
                    if (serviceRecord.getSyncStatus().equals(RecurringServiceTypeRepository.TYPE_Unsynced)) {
                        if (serviceRecord.getType().equals(type)) {
                            foundServiceTypeMap.put(type, serviceTypeMap.get(type));
                            break;
                        }
                    }
                }

                if (foundServiceTypeMap.containsKey(type)) {
                    continue;
                }

                for (Alert a : alerts) {
                    if (StringUtils.containsIgnoreCase(a.scheduleName(), type)
                            || StringUtils.containsIgnoreCase(a.visitCode(), type)) {
                        foundServiceTypeMap.put(type, serviceTypeMap.get(type));
                        break;
                    }
                }

            }

            if (foundServiceTypeMap.isEmpty()) {
                return;
            }


            serviceGroups = new ArrayList<>();
            LinearLayout serviceGroupCanvasLL = (LinearLayout) findViewById(R.id.service_group_canvas_ll);

            ServiceGroup curGroup = new ServiceGroup(this);
            curGroup.setData(childDetails, foundServiceTypeMap, serviceRecordList, alerts);
            curGroup.setOnServiceClickedListener(new ServiceGroup.OnServiceClickedListener() {
                @Override
                public void onClick(ServiceGroup serviceGroup, ServiceWrapper
                        serviceWrapper) {
                    addServiceDialogFragment(serviceWrapper, serviceGroup);
                }
            });
            curGroup.setOnServiceUndoClickListener(new ServiceGroup.OnServiceUndoClickListener() {
                @Override
                public void onUndoClick(ServiceGroup serviceGroup, ServiceWrapper serviceWrapper) {
                    addServiceUndoDialogFragment(serviceGroup, serviceWrapper);
                }
            });
            serviceGroupCanvasLL.addView(curGroup);
            serviceGroups.add(curGroup);
        }

    }

    private void updateVaccinationViews(List<Vaccine> vaccineList, List<Alert> alerts) {
//        if(false) {
//            if (vaccineGroups == null) {
//                vaccineGroups = new ArrayList<>();
//                String supportedVaccinesString = VaccinatorUtils.getSupportedWomanVaccines(this);
//
//                try {
//                    JSONArray supportedVaccines = new JSONArray(supportedVaccinesString);
//
//                    for (int i = 0; i < supportedVaccines.length(); i++) {
//                        addVaccineGroup(-1, supportedVaccines.getJSONObject(i), vaccineList, alerts);
//                    }
//                } catch (JSONException e) {
//                    Log.e(TAG, Log.getStackTraceString(e));
//                }
//            }
//        }

    }

    private void updateCounsellingViews(List<Counselling> counsellingList, LinearLayout counsellingCanvas) {
        counsellingCanvas.removeAllViews();
        LayoutInflater layoutInflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        LinearLayout counselling_group = (LinearLayout) layoutInflater.inflate(R.layout.view_counselling_group,null, true);
        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
//        counselling_group.setLayoutParams(layoutParams);
        counsellingCanvas.addView(counselling_group);
        CounsellingCardAdapter counsellingCardAdapter = new CounsellingCardAdapter(this,counsellingList);
        counsellingCardAdapter.setActivityListener(new ActivityListener(){
            @Override
            public void onCallbackActivity(Counselling counselling) {

                mEditCounselling = counselling;
                Map<String, String> counsellingFormData = counselling.getFormfields();
                String formMetadata;

                Map<String,String> detailmaps = childDetails.getColumnmaps();
                detailmaps.putAll(childDetails.getDetails());

                boolean pregnant = false;
                boolean lactating = false;
                Intent intent = new Intent(WomanImmunizationActivity.this, PathJsonFormActivity.class);
                intent.putExtra("skipdialog", true);
                if(detailmaps.get("pregnant")!=null){
                    if(detailmaps.get("pregnant").equalsIgnoreCase("Yes")){
                        pregnant = true;
                    }
                }
                if(detailmaps.get("lactating_woman")!=null){
                    if(detailmaps.get("lactating_woman").equalsIgnoreCase("Yes")){
                        lactating = true;
                    }
                }

                if( pregnant && !lactating){
                    formMetadata = JsonFormUtils.getAutoPopulatedJsonEditFormString(WomanImmunizationActivity.this,
                            counsellingFormData, "iycf_counselling_form_pregnant_woman", "Pregnant Woman Counselling");
                    //intent = new Intent(WomanImmunizationActivity.this, PathJsonFormActivity.class);
                    intent.putExtra("json", formMetadata);
                    startActivityForResult(intent, REQUEST_CODE_GET_JSON_COUNSELLING_UPDATE);
                }

                if( lactating){
                    formMetadata = JsonFormUtils.getAutoPopulatedJsonEditFormString(WomanImmunizationActivity.this,
                            counsellingFormData, "iycf_counselling_form_lactating_woman", "Lactating Woman Counselling");
                    //intent = new Intent(WomanImmunizationActivity.this, PathJsonFormActivity.class);
                    intent.putExtra("json", formMetadata);
                    startActivityForResult(intent, REQUEST_CODE_GET_JSON_COUNSELLING_UPDATE);

                }
//                formMetadata = JsonFormUtils.getAutoPopulatedJsonEditFormString(WomanImmunizationActivity.this,
//                        counsellingFormData, "iycf_counselling_form_pregnant_woman", "Pregnant Woman Counselling");
//                //intent = new Intent(WomanImmunizationActivity.this, PathJsonFormActivity.class);
//                intent.putExtra("json", formMetadata);
                //startActivityForResult(intent, REQUEST_CODE_GET_JSON_COUNSELLING_UPDATE);

            }
        } );
        ExpandableHeightGridView expandableHeightGridView = (ExpandableHeightGridView)counselling_group.findViewById(R.id.counselling_gv);
//        final float scale = getResources().getDisplayMetrics().density;
//        GridView.LayoutParams gridlayoutparams = new GridView.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
//                ViewGroup.LayoutParams.WRAP_CONTENT);
//        gridlayoutparams.height = (int)(scale*50*counsellingList.size());
        TextView recordnewCounselling = (TextView)counselling_group.findViewById(R.id.record_all_tv);
        recordnewCounselling.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean pregnant = false;
                boolean lactating = false;
                Map<String,String> detailmaps = childDetails.getColumnmaps();
                detailmaps.putAll(childDetails.getDetails());
                if(detailmaps.get("pregnant")!=null){
                    if(detailmaps.get("pregnant").equalsIgnoreCase("Yes")){
                        pregnant = true;

                    }
                }
                if(detailmaps.get("lactating_woman")!=null){
                    if(detailmaps.get("lactating_woman").equalsIgnoreCase("Yes")){
                        lactating = true;
                    }
                }
                if(pregnant&&!lactating){

                    String metadata = getmetaDataForPregnantCounsellingForm(childDetails,WomanImmunizationActivity.this);
                    Intent intent = new Intent(WomanImmunizationActivity.this, PathJsonFormActivity.class);

                    intent.putExtra("json", metadata);

                    startActivityForResult(intent, REQUEST_CODE_GET_JSON);

                }
                if(lactating){

                    String metadata = getmetaDataForLactatingCounsellingForm(childDetails);
                    Intent intent = new Intent(WomanImmunizationActivity.this, PathJsonFormActivity.class);

                    intent.putExtra("json", metadata);

                    startActivityForResult(intent, REQUEST_CODE_GET_JSON);

                }
            }
        });
        expandableHeightGridView.setExpanded(true);
        expandableHeightGridView.setAdapter(counsellingCardAdapter);
        counsellingCardAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if ( resultCode == RESULT_OK) {
            if (requestCode == REQUEST_CODE_GET_JSON) {
                String jsonString = data.getStringExtra("json");
                Log.d("JSONResult", jsonString);

                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
                AllSharedPreferences allSharedPreferences = new AllSharedPreferences(preferences);

                JsonFormUtils.saveForm(this, context(), jsonString, allSharedPreferences.fetchRegisteredANM());
                updateViews();
            }
            if(requestCode == REQUEST_CODE_GET_JSON_COUNSELLING_UPDATE){
                String jsonString = data.getStringExtra("json");
                Log.d("JSONResult", jsonString);

                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
                AllSharedPreferences allSharedPreferences = new AllSharedPreferences(preferences);
                JsonFormUtils.updateCounsellingForm(this, context(), jsonString, allSharedPreferences.fetchRegisteredANM());
                mEditCounselling.setFormfields(JsonFormUtils.getCouncellingField());
                VaccinatorApplication.getInstance().counsellingRepository().
                        update(VaccinatorApplication.getInstance().getRepository().getReadableDatabase(),mEditCounselling);
            }
        }
    }

    private String getmetaDataForLactatingCounsellingForm(CommonPersonObjectClient pc) {
        org.smartregister.Context context = VaccinatorApplication.getInstance().context();
        try {
            JSONObject form = FormUtils.getInstance(this).getFormJson("iycf_counselling_form_lactating_woman");

            if (form != null) {


                JSONObject jsonObject = form;
                if (jsonObject.getString(JsonFormUtils.ENTITY_ID) != null) {
                    jsonObject.remove(JsonFormUtils.ENTITY_ID);
                    jsonObject.put(JsonFormUtils.ENTITY_ID, pc.entityId());
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

    public static String getmetaDataForPregnantCounsellingForm(CommonPersonObjectClient pc,Context activitycontext) {
        org.smartregister.Context context = VaccinatorApplication.getInstance().context();
        try {
            JSONObject form = FormUtils.getInstance(activitycontext).getFormJson("iycf_counselling_form_pregnant_woman");

            if (form != null) {


                JSONObject jsonObject = form;
                if (jsonObject.getString(JsonFormUtils.ENTITY_ID) != null) {
                    jsonObject.remove(JsonFormUtils.ENTITY_ID);
                    jsonObject.put(JsonFormUtils.ENTITY_ID, pc.entityId());
                }

                JSONObject stepOne = form.getJSONObject(JsonFormUtils.STEP1);
                JSONArray jsonArray = stepOne.getJSONArray(JsonFormUtils.FIELDS);

                if(pc.getColumnmaps().get("pregnant_counselling_actions_for_next_meeting")!=null) {
                    if(!pc.getColumnmaps().get("pregnant_counselling_actions_for_next_meeting").equalsIgnoreCase("")) {
                        String[] valuelist = pc.getColumnmaps().get("pregnant_counselling_actions_for_next_meeting").split(",");
                        String ValueString = "";
                        for(int i = 0;i<valuelist.length;i++){
                            if(valuelist[i].equalsIgnoreCase("ifa_each_day")){
                                valuelist[i] = "Take IFA each day or try to take it as frequently as possible";
                            }
                            if(valuelist[i].equalsIgnoreCase("iodized_salt")){
                                valuelist[i] = "Use iodized salt instead of regular salt if available";
                            }
                            if(valuelist[i].equalsIgnoreCase("extra_snack")){
                                valuelist[i] = "Eat an extra snack a day";
                            }
                            if(valuelist[i].equalsIgnoreCase("discuss_husband_extra_food")){
                                valuelist[i] = "Discuss with husband or mother in law about eating extra and diverse food throughout the day";
                            }
                            if(valuelist[i].equalsIgnoreCase("negotiate_family_anc_visit")){
                                valuelist[i] = "Negotiate with family members to take on some of her workload and to attend ANC visits together";
                            }
                            if(i !=0) {
                                ValueString = ValueString +","+ valuelist[i];
                            }else if(i==0){
                                ValueString = valuelist[i];
                            }
                        }

                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject fieldjsonObject = jsonArray.getJSONObject(i);
                            if (fieldjsonObject.getString(JsonFormUtils.KEY)
                                    .equalsIgnoreCase("pregnant_counselling_actions_decided_previous_meeting")) {
//                                fieldjsonObject.remove(JsonFormUtils.VALUE);
                                fieldjsonObject.put("hint", "In the last session, your resolution was- "+ValueString+ "- Did you practice this resolution?");
                                fieldjsonObject.remove("hidden");
                                fieldjsonObject.put("hidden", false);

                                continue;
                            }
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
    protected org.smartregister.Context context() {
        return VaccinatorApplication.getInstance().context();
    }


    private void addVaccineGroup(int canvasId, JSONObject vaccineGroupData, List<Vaccine> vaccineList, List<Alert> alerts) {
//        LinearLayout vaccineGroupCanvasLL = (LinearLayout) findViewById(R.id.vaccine_group_canvas_ll);
//        VaccineGroup curGroup = new VaccineGroup(this);
//        curGroup.setData(vaccineGroupData, childDetails, vaccineList, alerts,"woman");
//        curGroup.setOnRecordAllClickListener(new VaccineGroup.OnRecordAllClickListener() {
//            @Override
//            public void onClick(VaccineGroup vaccineGroup, ArrayList<VaccineWrapper> dueVaccines) {
//                addVaccinationDialogFragment(dueVaccines, vaccineGroup);
//            }
//        });
//        curGroup.setOnVaccineClickedListener(new VaccineGroup.OnVaccineClickedListener() {
//            @Override
//            public void onClick(VaccineGroup vaccineGroup, VaccineWrapper vaccine) {
//                ArrayList<VaccineWrapper> vaccineWrappers = new ArrayList<VaccineWrapper>();
//                vaccineWrappers.add(vaccine);
//                addVaccinationDialogFragment(vaccineWrappers, vaccineGroup);
//            }
//        });
//        curGroup.setOnVaccineUndoClickListener(new VaccineGroup.OnVaccineUndoClickListener() {
//            @Override
//            public void onUndoClick(VaccineGroup vaccineGroup, VaccineWrapper vaccine) {
//                addVaccineUndoDialogFragment(vaccineGroup, vaccine);
//            }
//        });
//
//        LinearLayout parent;
//        if (canvasId == -1) {
//            Random r = new Random();
//            canvasId = r.nextInt(4232 - 213) + 213;
//            parent = new LinearLayout(this);
//            parent.setId(canvasId);
//            vaccineGroupCanvasLL.addView(parent);
//        } else {
//            parent = (LinearLayout) findViewById(canvasId);
//            parent.removeAllViews();
//        }
//        parent.addView(curGroup);
//        curGroup.setTag(R.id.vaccine_group_vaccine_data, vaccineGroupData.toString());
//        curGroup.setTag(R.id.vaccine_group_parent_id, String.valueOf(canvasId));
//        vaccineGroups.add(curGroup);
    }

    private void addVaccineUndoDialogFragment(VaccineGroup vaccineGroup, VaccineWrapper vaccineWrapper) {
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        Fragment prev = getFragmentManager().findFragmentByTag(DIALOG_TAG);
        if (prev != null) {
            ft.remove(prev);
        }

        ft.addToBackStack(null);
        vaccineGroup.setModalOpen(true);

        UndoVaccinationDialogFragment undoVaccinationDialogFragment = UndoVaccinationDialogFragment.newInstance(vaccineWrapper);
        undoVaccinationDialogFragment.show(ft, DIALOG_TAG);
    }

    private void addServiceUndoDialogFragment(ServiceGroup serviceGroup, ServiceWrapper serviceWrapper) {
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        Fragment prev = getFragmentManager().findFragmentByTag(DIALOG_TAG);
        if (prev != null) {
            ft.remove(prev);
        }

        ft.addToBackStack(null);
        serviceGroup.setModalOpen(true);

        UndoServiceDialogFragment undoServiceDialogFragment = UndoServiceDialogFragment.newInstance(serviceWrapper);
        undoServiceDialogFragment.show(ft, DIALOG_TAG);
    }

    private void updateWeightViews(Weight lastUnsyncedWeight) {

        String childName = constructChildName();
        String gender = getValue(childDetails.getColumnmaps(), "gender", true);
        String motherFirstName = getValue(childDetails.getColumnmaps(), "mother_first_name", true);
        if (StringUtils.isBlank(childName) && StringUtils.isNotBlank(motherFirstName)) {
            childName = "B/o " + motherFirstName.trim();
        }

        String zeirId = getValue(childDetails.getColumnmaps(), "zeir_id", false);
        String duration = "";
        String dobString = getValue(childDetails.getColumnmaps(), "dob", false);
        if (StringUtils.isNotBlank(dobString)) {
            DateTime dateTime = new DateTime(getValue(childDetails.getColumnmaps(), "dob", false));
            duration = DateUtil.getDuration(dateTime);
        }

        Photo photo = ImageUtils.profilePhotoByClient(childDetails);

        WeightWrapper weightWrapper = new WeightWrapper();
        weightWrapper.setId(childDetails.entityId());
        weightWrapper.setGender(gender);
        weightWrapper.setPatientName(childName);
        weightWrapper.setPatientNumber(zeirId);
        weightWrapper.setPatientAge(duration);
        weightWrapper.setPhoto(photo);
        weightWrapper.setPmtctStatus(getValue(childDetails.getColumnmaps(), "pmtct_status", false));

        if (lastUnsyncedWeight != null) {
            weightWrapper.setWeight(lastUnsyncedWeight.getKg());
            weightWrapper.setDbKey(lastUnsyncedWeight.getId());
            weightWrapper.setUpdatedWeightDate(new DateTime(lastUnsyncedWeight.getDate()), false);
        }

        updateRecordWeightViews(weightWrapper);

        ImageButton growthChartButton = (ImageButton) findViewById(R.id.growth_chart_button);
        growthChartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startAsyncTask(new ShowGrowthChartTask(), null);
            }
        });
    }

    private void updateRecordWeightViews(WeightWrapper weightWrapper) {
        View recordWeight = findViewById(R.id.record_weight);
        recordWeight.setClickable(true);
        recordWeight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showWeightDialog(view);
            }
        });

        recordWeight.setBackground(getResources().getDrawable(R.drawable.record_weight_bg));
        if (weightWrapper.getDbKey() != null && weightWrapper.getWeight() != null) {
            TextView recordWeightText = (TextView) findViewById(R.id.record_weight_text);
            recordWeightText.setText(kgStringSuffix(weightWrapper.getWeight()));

            ImageView recordWeightCheck = (ImageView) findViewById(R.id.record_weight_check);
            recordWeightCheck.setVisibility(View.VISIBLE);

            if (weightWrapper.getUpdatedWeightDate() != null) {
                long timeDiff = Calendar.getInstance().getTimeInMillis()
                        - weightWrapper.getUpdatedWeightDate().getMillis();

                if (timeDiff < TimeUnit.MILLISECONDS.convert(1, TimeUnit.DAYS)) {
                    //disable the button
                    recordWeight.setClickable(false);
                    recordWeight.setBackground(new ColorDrawable(getResources()
                            .getColor(android.R.color.transparent)));
                }
            }
        }

        recordWeight.setTag(weightWrapper);

    }

    private void showWeightDialog(View view) {
//        FragmentTransaction ft = this.getFragmentManager().beginTransaction();
//        Fragment prev = this.getFragmentManager().findFragmentByTag(DIALOG_TAG);
//        if (prev != null) {
//            ft.remove(prev);
//        }
//        ft.addToBackStack(null);
//        WeightWrapper weightWrapper = (WeightWrapper) view.getTag();
//        RecordWeightDialogFragment recordWeightDialogFragment = RecordWeightDialogFragment.newInstance(weightWrapper);
//        recordWeightDialogFragment.show(ft, DIALOG_TAG);

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
        Intent intent = new Intent(fromContext, WomanImmunizationActivity.class);
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
            bundle.putString("location_name", JsonFormUtils.getOpenMrsLocationId(getOpenSRPContext(), toolbar.getCurrentLocation()));
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
        return R.layout.activity_woman_immunization;
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
            if (tag.getDbKey() != null) {
                weight = weightRepository.find(tag.getDbKey());
            }
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

            Gender gender = Gender.UNKNOWN;
            String genderString = getValue(childDetails, "gender", false);
            if (genderString != null && genderString.toLowerCase().equals("female")) {
                gender = Gender.FEMALE;
            } else if (genderString != null && genderString.toLowerCase().equals("male")) {
                gender = Gender.MALE;
            }

            Date dob = null;
            String dobString = getValue(childDetails.getColumnmaps(), "dob", false);
            if (!TextUtils.isEmpty(dobString)) {
                DateTime dateTime = new DateTime(dobString);
                dob = dateTime.toDate();
            }

            if (dob != null && gender != Gender.UNKNOWN) {
                weightRepository.add(dob, gender, weight);
            } else {
                weightRepository.add(weight);
            }

            tag.setDbKey(weight.getId());
            updateRecordWeightViews(tag);
            setLastModified(true);
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
        startAsyncTask(new UndoVaccineTask(tag, v), null);
    }

    public void addVaccinationDialogFragment(ArrayList<VaccineWrapper> vaccineWrappers, VaccineGroup vaccineGroup) {

        FragmentTransaction ft = this.getFragmentManager().beginTransaction();
        Fragment prev = this.getFragmentManager().findFragmentByTag(DIALOG_TAG);
        if (prev != null) {
            ft.remove(prev);
        }

        ft.addToBackStack(null);
        vaccineGroup.setModalOpen(true);
        String dobString = getValue(childDetails.getColumnmaps(), "dob", false);
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

    public void addServiceDialogFragment(ServiceWrapper serviceWrapper, ServiceGroup serviceGroup) {

        FragmentTransaction ft = this.getFragmentManager().beginTransaction();
        Fragment prev = this.getFragmentManager().findFragmentByTag(DIALOG_TAG);
        if (prev != null) {
            ft.remove(prev);
        }

        ft.addToBackStack(null);
        serviceGroup.setModalOpen(true);

        List<ServiceRecord> serviceRecordList = VaccinatorApplication.getInstance().recurringServiceRecordRepository()
                .findByEntityId(childDetails.entityId());

        ServiceDialogFragment serviceDialogFragment = ServiceDialogFragment.newInstance(serviceRecordList, serviceWrapper);
        serviceDialogFragment.show(ft, DIALOG_TAG);
    }

    public void performRegisterActions() {
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
        startAsyncTask(backgroundTask, arrayTags);

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
                vaccineGroup.updateWrapperStatus(wrappers,"woman");
            }
            vaccineGroup.updateViews(wrappers);

        } else {
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(new Runnable() {
                @Override
                public void run() {
                    if (undo) {
                        vaccineGroup.setVaccineList(vaccineList);
                        vaccineGroup.updateWrapperStatus(wrappers,"woman");
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
                                new MarkAlertAsDoneTask(getOpenSRPContext().alertService())
                                        .execute(alert);
                            }
                        }
                    }, 0, null, alert);
        }
    }

    private class MarkAlertAsDoneTask extends AsyncTask<Alert, Void, Void> {
        private final AlertService alertService;

        public MarkAlertAsDoneTask(AlertService alertService) {
            this.alertService = alertService;
        }

        @Override
        protected Void doInBackground(Alert... params) {
            for (int i = 0; i < params.length; i++) {
                alertService.changeAlertStatusToComplete(params[i].caseId(), params[i].visitCode());
            }
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
            String dobString = getValue(childDetails.getColumnmaps(), "lmp", false);
            if (!TextUtils.isEmpty(dobString)) {
                SimpleDateFormat lmp_DATE_FORMAT = new SimpleDateFormat("dd-MM-yyyy");
                Date dateTime = null;
                try {
                    dateTime = lmp_DATE_FORMAT.parse(dobString);
                    affectedVaccines = VaccineSchedule.updateOfflineAlerts(childDetails.entityId(), new DateTime(dateTime.getTime()), "woman");
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
            vaccineList = vaccineRepository.findByEntityId(childDetails.entityId());
            alertList = alertService.findByEntityIdAndAlertNames(childDetails.entityId(),
                    VaccinateActionUtils.allAlertNames("woman"));

            return pair;
        }
    }

    private String constructChildName() {
        String firstName = getValue(childDetails.getColumnmaps(), "first_name", true);
        String lastName = getValue(childDetails.getColumnmaps(), "last_name", true).replaceAll(Pattern.quote("."),"");
        return getName(firstName, lastName).trim();
    }

    @Override
    public void finish() {
        if (isLastModified()) {
            String tableName = PathConstants.CHILD_TABLE_NAME;
            AllCommonsRepository allCommonsRepository = getOpenSRPContext().allCommonsRepositoryobjects(tableName);
            ContentValues contentValues = new ContentValues();
            contentValues.put("last_interacted_with", (new Date()).getTime());
            allCommonsRepository.update(tableName, contentValues, childDetails.entityId());
            allCommonsRepository.updateSearch(childDetails.entityId());
        }
        //mother last visit update
        ContentValues cv = new ContentValues();
        cv.put("last_interacted_with",""+((new DateTime()).getMillis()));
        context = org.smartregister.Context.getInstance().updateApplicationContext(this.getApplicationContext());
        CommonRepository commonRepository = context.commonrepository("ec_household");
        commonRepository.updateColumn("ec_mother",cv,childDetails.entityId());
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

    private class UpdateViewTask extends AsyncTask<Void, Void, Map<String, NamedObject<?>>> {

        private VaccineRepository vaccineRepository;
        private WeightRepository weightRepository;
        private RecurringServiceTypeRepository recurringServiceTypeRepository;
        private RecurringServiceRecordRepository recurringServiceRecordRepository;
        private AlertService alertService;

        public void setVaccineRepository(VaccineRepository vaccineRepository) {
            this.vaccineRepository = vaccineRepository;
        }

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

            Map<String, List<ServiceType>> serviceTypeMap = new LinkedHashMap<>();
            List<ServiceRecord> serviceRecords = new ArrayList<>();

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

            if (map.containsKey(ServiceType.class.getName())) {
                NamedObject<?> namedObject = map.get(ServiceType.class.getName());
                if (namedObject != null) {
                    serviceTypeMap = (Map<String, List<ServiceType>>) namedObject.object;
                }

            }

            if (map.containsKey(ServiceRecord.class.getName())) {
                NamedObject<?> namedObject = map.get(ServiceRecord.class.getName());
                if (namedObject != null) {
                    serviceRecords = (List<ServiceRecord>) namedObject.object;
                }

            }

            if (map.containsKey(Alert.class.getName())) {
                NamedObject<?> namedObject = map.get(Alert.class.getName());
                if (namedObject != null) {
                    alertList = (List<Alert>) namedObject.object;
                }

            }

            updateWeightViews(weight);
            updateServiceViews(serviceTypeMap, serviceRecords, alertList);
            updateVaccinationViews(vaccineList, alertList);
            performRegisterActions();
        }

        @Override
        protected Map<String, NamedObject<?>> doInBackground(Void... voids) {
            String dobString = getValue(childDetails.getColumnmaps(), "lmp", false);
            if (!TextUtils.isEmpty(dobString)) {
                 SimpleDateFormat lmp_DATE_FORMAT = new SimpleDateFormat("dd-MM-yyyy");
                Date dateTime = null;
                try {
                    dateTime = lmp_DATE_FORMAT.parse(dobString);
                    VaccineSchedule.updateOfflineAlerts(childDetails.entityId(), new DateTime(dateTime.getTime()), "woman");
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }

            List<Vaccine> vaccineList = new ArrayList<>();
            Weight weight = null;

            Map<String, List<ServiceType>> serviceTypeMap = new LinkedHashMap<>();
            List<ServiceRecord> serviceRecords = new ArrayList<>();

            List<Alert> alertList = new ArrayList<>();
            if (vaccineRepository != null) {
                vaccineList = vaccineRepository.findByEntityId(childDetails.entityId());

            }
            if (weightRepository != null) {
                weight = weightRepository.findUnSyncedByEntityId(childDetails.entityId());
            }

            if (recurringServiceRecordRepository != null) {
                serviceRecords = recurringServiceRecordRepository.findByEntityId(childDetails.entityId());
            }

            if (recurringServiceTypeRepository != null) {
                List<String> types = recurringServiceTypeRepository.fetchTypes();
                for (String type : types) {
                    List<ServiceType> subTypes = recurringServiceTypeRepository.findByType(type);
                    serviceTypeMap.put(type, subTypes);
                }
            }

            if (alertService != null) {
                alertList = alertService.findByEntityId(childDetails.entityId());
            }

            Map<String, NamedObject<?>> map = new HashMap<>();

            NamedObject<List<Vaccine>> vaccineNamedObject = new NamedObject<>(Vaccine.class.getName(), vaccineList);
            map.put(vaccineNamedObject.name, vaccineNamedObject);

            NamedObject<Weight> weightNamedObject = new NamedObject<>(Weight.class.getName(), weight);
            map.put(weightNamedObject.name, weightNamedObject);

            NamedObject<Map<String, List<ServiceType>>> serviceTypeNamedObject = new NamedObject<>(ServiceType.class.getName(), serviceTypeMap);
            map.put(serviceTypeNamedObject.name, serviceTypeNamedObject);

            NamedObject<List<ServiceRecord>> serviceRecordNamedObject = new NamedObject<>(ServiceRecord.class.getName(), serviceRecords);
            map.put(serviceRecordNamedObject.name, serviceRecordNamedObject);

            NamedObject<List<Alert>> alertsNamedObject = new NamedObject<>(Alert.class.getName(), alertList);
            map.put(alertsNamedObject.name, alertsNamedObject);

            return map;
        }
    }

    private class UndoVaccineTask extends AsyncTask<Void, Void, Void> {

        private VaccineWrapper tag;
        private View v;
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
                    String dobString = getValue(childDetails.getColumnmaps(), "lmp", false);
                    if (!TextUtils.isEmpty(dobString)) {
                            SimpleDateFormat lmp_DATE_FORMAT = new SimpleDateFormat("dd-MM-yyyy");
                            Date dateTime = null;
                            try {
                                dateTime = lmp_DATE_FORMAT.parse(dobString);
                                VaccineSchedule.updateOfflineAlerts(childDetails.entityId(), new DateTime(dateTime.getTime()), "woman");
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                        vaccineList = vaccineRepository.findByEntityId(childDetails.entityId());
                        alertList = alertService.findByEntityIdAndAlertNames(childDetails.entityId(),
                                VaccinateActionUtils.allAlertNames("woman"));
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
        }
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
                            for (int i = 0; i < allSisters.length; i++) {
                                if (allSisters[i].replace(" ", "").equalsIgnoreCase(curAffectedVaccineName.replace(" ", ""))) {
                                    curWrapperName = allSisters[i];
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

    private class GetSiblingsTask extends AsyncTask<Void, Void, ArrayList<String>> {

        @Override
        protected ArrayList<String> doInBackground(Void... params) {
            String baseEntityId = childDetails.entityId();
//                    Utils.getValue(childDetails.getColumnmaps(), "base_entity_id", false);
            String motherBaseEntityId = getValue(childDetails.getColumnmaps(), "relational_id", false);
            if (!TextUtils.isEmpty(motherBaseEntityId) && !TextUtils.isEmpty(baseEntityId)) {
                List<CommonPersonObject> children = getOpenSRPContext().commonrepository(PathConstants.CHILD_TABLE_NAME)
                        .findByRelational_IDs(baseEntityId);

                if (children != null) {
                    ArrayList<String> baseEntityIds = new ArrayList<>();
                    for (CommonPersonObject curChild : children) {
                        if (!baseEntityId.equals(getValue(curChild.getColumnmaps(),
                                "base_entity_id", false))) {
                            baseEntityIds.add(getValue(curChild.getColumnmaps(),
                                    "base_entity_id", false));
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
            if (baseEntityIds == null) {
                baseEntityIds = new ArrayList<>();
            }

            Collections.reverse(baseEntityIds);

            SiblingPicturesGroup siblingPicturesGroup = (SiblingPicturesGroup) WomanImmunizationActivity.this.findViewById(R.id.sibling_pictures);
            siblingPicturesGroup.setSiblingBaseEntityIds(WomanImmunizationActivity.this, baseEntityIds);
        }
    }

    private class NamedObject<T> {
        public final String name;
        public final T object;

        public NamedObject(String name, T object) {
            this.name = name;
            this.object = object;
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
                String dobString = getValue(childDetails.getColumnmaps(), "dob", false);
                if (!TextUtils.isEmpty(getValue(childDetails.getColumnmaps(), "Birth_Weight", false))
                        && !TextUtils.isEmpty(dobString)) {
                    DateTime dateTime = new DateTime(dobString);
                    Double birthWeight = Double.valueOf(getValue(childDetails.getColumnmaps(), "Birth_Weight", false));

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
            FragmentTransaction ft = WomanImmunizationActivity.this.getFragmentManager().beginTransaction();
            Fragment prev = WomanImmunizationActivity.this.getFragmentManager().findFragmentByTag(DIALOG_TAG);
            if (prev != null) {
                ft.remove(prev);
            }
            ft.addToBackStack(null);


            GrowthDialogFragment growthDialogFragment = GrowthDialogFragment.newInstance(childDetails, allWeights);
            growthDialogFragment.show(ft, DIALOG_TAG);
        }
    }
}
