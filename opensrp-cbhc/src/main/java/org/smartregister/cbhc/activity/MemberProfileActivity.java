package org.smartregister.cbhc.activity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.smartregister.cbhc.fragment.GMPFragment;
import org.smartregister.cbhc.util.JsonFormUtils;
import net.sqlcipher.Cursor;
import net.sqlcipher.database.SQLiteDatabase;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONObject;
import org.smartregister.CoreLibrary;
import org.smartregister.cbhc.R;
import org.smartregister.cbhc.adapter.ViewPagerAdapter;
import org.smartregister.cbhc.application.AncApplication;
import org.smartregister.cbhc.contract.ProfileContract;
import org.smartregister.cbhc.domain.UniqueId;
import org.smartregister.cbhc.fragment.ChildImmunizationFragment;
import org.smartregister.cbhc.fragment.FollowupFragment;
import org.smartregister.cbhc.fragment.MemberProfileContactsFragment;
import org.smartregister.cbhc.fragment.ProfileTasksFragment;
import org.smartregister.cbhc.fragment.WomanImmunizationFragment;
import org.smartregister.cbhc.helper.ImageRenderHelper;
import org.smartregister.cbhc.presenter.ProfilePresenter;
import org.smartregister.cbhc.repository.AncRepository;
import org.smartregister.cbhc.repository.HealthIdRepository;
import org.smartregister.cbhc.repository.UniqueIdRepository;
import org.smartregister.cbhc.util.Constants;
import org.smartregister.cbhc.util.DBConstants;
import org.smartregister.cbhc.util.ImageLoaderByGlide;
import org.smartregister.cbhc.util.Utils;
import org.smartregister.cbhc.view.CopyToClipboardDialog;
import org.smartregister.commonregistry.CommonPersonObject;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.domain.ProfileImage;
import org.smartregister.growthmonitoring.domain.HeightWrapper;
import org.smartregister.growthmonitoring.domain.MUACWrapper;
import org.smartregister.growthmonitoring.domain.WeightWrapper;
import org.smartregister.growthmonitoring.domain.ZScore;
import org.smartregister.growthmonitoring.listener.HeightActionListener;
import org.smartregister.growthmonitoring.listener.MUACActionListener;
import org.smartregister.growthmonitoring.listener.WeightActionListener;
import org.smartregister.immunization.domain.ServiceWrapper;
import org.smartregister.immunization.domain.VaccineWrapper;
import org.smartregister.immunization.listener.ServiceActionListener;
import org.smartregister.immunization.listener.VaccinationActionListener;
import org.smartregister.repository.ImageRepository;
import org.smartregister.util.DateUtil;
import org.smartregister.util.PermissionUtils;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import static org.smartregister.cbhc.fragment.ProfileOverviewFragment.EXTRA_HOUSEHOLD_DETAILS;
import static org.smartregister.cbhc.util.Constants.EventType.PREGNANT_STATUS;
import static org.smartregister.cbhc.util.Constants.FOLLOWUP_FORM.Followup_Form_MHV_Death;
import static org.smartregister.cbhc.util.Constants.FOLLOWUP_FORM.Followup_Form_MHV_Delivery;

import static org.smartregister.cbhc.util.Constants.FOLLOWUP_FORM.Followup_Form_MHV_Marital_F;
import static org.smartregister.cbhc.util.Constants.FOLLOWUP_FORM.Followup_Form_MHV_Marital_M;
import static org.smartregister.cbhc.util.Constants.FOLLOWUP_FORM.Followup_Form_MHV_Mobile_no;
import static org.smartregister.cbhc.util.Constants.FOLLOWUP_FORM.Followup_Form_MHV_Pregnant;
import static org.smartregister.cbhc.util.Constants.FOLLOWUP_FORM.Followup_Form_MHV_Risky_Habit;
import static org.smartregister.cbhc.util.Constants.FOLLOWUP_FORM.Followup_Form_MHV_Transfer;
import static org.smartregister.cbhc.util.JsonFormUtils.DATE_FORMAT;
import static org.smartregister.util.Utils.getName;
import static org.smartregister.util.Utils.getValue;

/**
 * Created by ndegwamartin on 10/07/2018.
 */
public class MemberProfileActivity extends BaseProfileActivity implements ProfileContract.View, VaccinationActionListener, ServiceActionListener, WeightActionListener, HeightActionListener, MUACActionListener {

    private TextView nameView;
    private TextView ageView;
    private TextView pregnant_statusView;
    private TextView gestationAgeView;
    private TextView ancIdView;
    private ImageView imageView;
    private ImageRenderHelper imageRenderHelper;
    private String womanPhoneNumber;
    String typeofMember;


    private static final String TAG = MemberProfileActivity.class.getCanonicalName();

    public static final String DIALOG_TAG = "PROFILE_DIALOG_TAG";
    private CommonPersonObjectClient householdDetails;

    int age = -1;
    int gender = -1;
    int marital_status = 0;
    private String from = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle extras = this.getIntent().getExtras();
        if (extras != null) {
            Serializable serializable = extras.getSerializable(EXTRA_HOUSEHOLD_DETAILS);
            if (serializable instanceof CommonPersonObjectClient) {
                householdDetails = (CommonPersonObjectClient) serializable;
            }
            typeofMember = extras.getString("type_of_member");
            from = extras.getString("from")==null?"":extras.getString("from");
        } else {
            Toast.makeText(this, "Details not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
//        AllSharedPreferences allSharedPreferences = GrowthMonitoringLibrary.getInstance().context()
//                .allSharedPreferences();
//        allSharedPreferences.saveDefaultLocalityId(allSharedPreferences.fetchRegisteredANM(),"testLocalityId");
//        AncApplication.getInstance().initLibraries();
//        AncApplication.getInstance().initOfflineSchedules();
        setUpViews();

        mProfilePresenter = new ProfilePresenter(this);
        mProfilePresenter.setProfileActivity(this);

        imageRenderHelper = new ImageRenderHelper(this);


    }


    private TabLayout tabLayout;

    private void setUpViews() {
        imageView = findViewById(R.id.imageview_profile);
        if (typeofMember != null) {
            if (typeofMember.equalsIgnoreCase("malechild")) {
                imageView.setImageDrawable(getResources().getDrawable(R.drawable.child_boy_infant));
            } else if (typeofMember.equalsIgnoreCase("femalechild")) {
                imageView.setImageDrawable(getResources().getDrawable(R.drawable.child_girl_infant));
            } else if (typeofMember.equalsIgnoreCase("woman")) {
                imageView.setImageDrawable(getResources().getDrawable(R.drawable.female_register_placeholder_profile));
            } else if (typeofMember.equalsIgnoreCase("member")) {
                imageView.setImageDrawable(getResources().getDrawable(R.drawable.male_register_placeholder_profile));
            }
        }

//        ImageRepository imageRepo = CoreLibrary.getInstance().context().imageRepository();
//        ProfileImage imageRecord = imageRepo.findByEntityId(householdDetails.entityId());
//
//        if(imageRecord!=null)
//            ImageLoaderByGlide.setImageAsTarget(imageRecord.getFilepath(),circleprofile,0);

        tabLayout = findViewById(R.id.tabs);
        ViewPager viewPager = findViewById(R.id.viewpager);
        tabLayout.setupWithViewPager(setupViewPager(viewPager));

        ageView = findViewById(R.id.textview_age);
        gestationAgeView = findViewById(R.id.textview_gestation_age);
        ancIdView = findViewById(R.id.textview_anc_id);
        nameView = findViewById(R.id.textview_name);
        pregnant_statusView = findViewById(R.id.textview_pregnant_status);

//setProfileImage(householdDetails.entityId());
        String firstName = org.smartregister.util.Utils.getValue(householdDetails.getColumnmaps(), DBConstants.KEY.FIRST_NAME, true);
        String lastName = org.smartregister.util.Utils.getValue(householdDetails.getColumnmaps(), DBConstants.KEY.LAST_NAME, true);
        if (lastName.equalsIgnoreCase("null") || lastName == null) {
            lastName = "";
        }

        String patientName = getName(firstName, lastName);

        updateEDD(householdDetails.getCaseId());
        setProfileName(patientName);
        String dobString = getValue(householdDetails.getColumnmaps(), "dob", true);
        String durationString = "";

        if(dobString!=null){
            if (StringUtils.isNotBlank(dobString)) {
                try {
                    DateTime birthDateTime = new DateTime(dobString);

                    String duration = DateUtil.getDuration(birthDateTime);
                    if (duration != null) {
                        durationString = duration;
                    }
                } catch (Exception e) {
                    Utils.appendLog(getClass().getName(),e);
                    Log.e(getClass().getName(), e.toString(), e);
                }
            }
        }else{
            String age = getValue(householdDetails.getColumnmaps(), "age", true);
            durationString = age;

        }
        ImageRepository imageRepo = CoreLibrary.getInstance().context().imageRepository();
        ProfileImage imageRecord = imageRepo.findByEntityId(householdDetails.entityId());
        if (imageRecord != null)
            ImageLoaderByGlide.setImageAsTarget(imageRecord.getFilepath(), imageView, 0);
        setProfileAge(durationString);
        setProfileID(getValue(householdDetails.getColumnmaps(), "Patient_Identifier", true));
        gestationAgeView.setVisibility(View.GONE);
        String profileColorText = getValue(householdDetails.getColumnmaps(), "child_status", false);
        Log.v("CHILD_STATUS","profileColorText>> from db>>"+profileColorText);
        updateProfileColorByText(profileColorText);
    }

    public void updateEDD(final String entity_id) {
        org.smartregister.util.Utils.startAsyncTask(new AsyncTask() {

            String lmp_date = "";
            String delivery_status = "";
            String Patient_identifier = "";

            @Override
            protected Object doInBackground(Object[] objects) {
                AncRepository repo = (AncRepository) AncApplication.getInstance().getRepository();
                SQLiteDatabase db = repo.getReadableDatabase();
                String sql = "SELECT lmp_date FROM ec_woman WHERE base_entity_id = '" + entity_id + "'";
                Cursor cursor = db.rawQuery(sql, new String[]{});

                try {
                    if (cursor.moveToNext()) {
                        lmp_date = cursor.getString(0);

                        delivery_status = householdDetails.getColumnmaps().get(PREGNANT_STATUS);
                        if (lmp_date != null && delivery_status != null) {
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd\'T\'HH:mm:ss.SSS\'Z\'");
                            Calendar c = Calendar.getInstance();
                            try {
                                c.setTime(sdf.parse(lmp_date));
                            } catch (ParseException e) {
                                Utils.appendLog(getClass().getName(),e);
                                e.printStackTrace();
                            }
                            c.add(Calendar.DATE, 280);  // number of days to add
                            sdf = new SimpleDateFormat("dd-MM-yyyy");
                            lmp_date = sdf.format(c.getTime());  // dt is now the new date

                        }
                    }
                } catch (Exception e) {
Utils.appendLog(getClass().getName(),e);

                } finally {
                    cursor.close();
                }
//                sql = "SELECT VALUE FROM ec_details WHERE (KEY = 'Patient_Identifier') AND base_entity_id = '"+entity_id+"'";
//                cursor = db.rawQuery(sql,new String[]{});
//                try {
//                    if (cursor.moveToNext()) {
//                       Patient_identifier = cursor.getString(0);
//                    }
//                }catch(Exception e) {
//Utils.appendLog(getClass().getName(),e);
//
//                }

                return null;
            }

            @Override
            protected void onPostExecute(Object o) {
                super.onPostExecute(o);
                if(delivery_status!=null) {
                    if (delivery_status.equalsIgnoreCase("প্রসব পূর্ব") || delivery_status.equalsIgnoreCase("Antenatal Period")) {
                        pregnant_statusView.setVisibility(View.VISIBLE);
                        pregnant_statusView.setText("EDD: " + lmp_date);
                    }
                }

//                if(Patient_identifier!=null){
//                    ancIdView.setText("ID: " + Patient_identifier);
//                }
            }
        }, null);
    }

    public void refreshProfileViews() {
//        if(typeofMember.equalsIgnoreCase("malechild")||typeofMember.equalsIgnoreCase("femalechild")){
//            householdDetails = CommonPersonObjectToClient(AncApplication.getInstance().getContext().commonrepository(DBConstants.CHILD_TABLE_NAME).findByBaseEntityId(householdDetails.entityId()));
//        }else if(typeofMember.equalsIgnoreCase("woman")){
//            householdDetails = CommonPersonObjectToClient(AncApplication.getInstance().getContext().commonrepository(DBConstants.WOMAN_TABLE_NAME).findByBaseEntityId(householdDetails.entityId()));
//        }else if(typeofMember.equalsIgnoreCase("member")){
//            householdDetails = CommonPersonObjectToClient(AncApplication.getInstance().getContext().commonrepository(DBConstants.MEMBER_TABLE_NAME).findByBaseEntityId(householdDetails.entityId()));
//        }
        org.smartregister.util.Utils.startAsyncTask(new AsyncTask(){
            @Override
            protected Object doInBackground(Object[] objects) {
                householdDetails.getColumnmaps().putAll(AncApplication.getInstance().getContext().detailsRepository().getAllDetailsForClient(householdDetails.entityId()));
                return null;
            }

            @Override
            protected void onPostExecute(Object o) {
                super.onPostExecute(o);
                String firstName = org.smartregister.util.Utils.getValue(householdDetails.getColumnmaps(), DBConstants.KEY.FIRST_NAME, true);
                String lastName = org.smartregister.util.Utils.getValue(householdDetails.getColumnmaps(), DBConstants.KEY.LAST_NAME, true);
                if (lastName.equalsIgnoreCase("null") || lastName == null) {
                    lastName = "";
                }
                String patientName = getName(firstName, lastName);


                setProfileName(patientName);

                String dobString = getValue(householdDetails.getColumnmaps(), "dob", true);
                String durationString = "";
                if (StringUtils.isNotBlank(dobString)) {
                    try {
                        DateTime birthDateTime = new DateTime(dobString);

                        String duration = DateUtil.getDuration(birthDateTime);
                        if (duration != null) {
                            durationString = duration;
                        }
                    } catch (Exception e) {
Utils.appendLog(getClass().getName(),e);
                        Log.e(getClass().getName(), e.toString(), e);
                    }
                }
                setProfileAge(durationString);
                setProfileID(getValue(householdDetails.getColumnmaps(), "Patient_Identifier", true));
                gestationAgeView.setVisibility(View.GONE);
                followupFragment.notifyAdapter();
                profileOverviewFragment.reloadView();
            }
        }, null);


    }

    private CommonPersonObjectClient CommonPersonObjectToClient(CommonPersonObject commonPersonObject) {
        CommonPersonObjectClient commonPersonObjectClient = new CommonPersonObjectClient(commonPersonObject.getCaseId(), commonPersonObject.getDetails(), DBConstants.HOUSEHOLD_TABLE_NAME);
        commonPersonObjectClient.setColumnmaps(commonPersonObject.getColumnmaps());
        return commonPersonObjectClient;
    }

    private UniqueIdRepository uniqueIdRepository;
    private HealthIdRepository healthIdRepository;

    public HealthIdRepository getHealthIdRepository() {
        if (healthIdRepository == null) {
            healthIdRepository = AncApplication.getInstance().getHealthIdRepository();
        }
        return healthIdRepository;
    }

    public void displayShortToast(int resourceId) {
        org.smartregister.util.Utils.showShortToast(this, this.getString(resourceId));
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_profile_registration_info:
                householdDetails.getColumnmaps().putAll(AncApplication.getInstance().getContext().detailsRepository().getAllDetailsForClient(householdDetails.entityId()));
                String patient_identifier = householdDetails.getColumnmaps().get("Patient_Identifier");

                if (patient_identifier == null || (patient_identifier != null && patient_identifier.isEmpty()) || patient_identifier.equalsIgnoreCase("null")) {
                    UniqueId uniqueId = getHealthIdRepository().getNextUniqueId();
                    final String entityId = uniqueId != null ? uniqueId.getOpenmrsId() : "";
                    if (StringUtils.isBlank(entityId)) {
                        displayShortToast(R.string.no_openmrs_id);
                    } else {
                        householdDetails.getColumnmaps().put("Patient_Identifier", entityId);
                    }
                }
                String formMetadataformembers;
                if(from.equals("")){
                    formMetadataformembers = JsonFormUtils.getMemberJsonEditFormString(this, householdDetails.getColumnmaps());
                }else{
                    formMetadataformembers = JsonFormUtils.getGuestMemberJsonEditFormString(this, householdDetails.getColumnmaps());
                }
                try {
                    JsonFormUtils.startFormForEdit(this, JsonFormUtils.REQUEST_CODE_GET_JSON, formMetadataformembers,from);
                } catch (Exception e) {
                    Utils.appendLog(getClass().getName(),e);
                }
                break;
            case R.id.edit_member:
//                CommonPersonObjectClient pclient  = (CommonPersonObjectClient) view.getTag();
//                String formMetadataformembers = JsonFormUtils.getMemberJsonEditFormString(this, pclient.getColumnmaps());
//                try {
//                    JsonFormUtils.startFormForEdit(this, JsonFormUtils.REQUEST_CODE_GET_JSON, formMetadataformembers);
//                } catch (Exception e) {
//Utils.appendLog(getClass().getName(),e);
//
//                }
                break;
        }
    }

    WomanImmunizationFragment womanImmunizationFragment;
    ChildImmunizationFragment childImmunizationFragment;
    GMPFragment growthFragment;
    //    public ProfileOverviewFragment profileOverviewFragment;
    public MemberProfileContactsFragment profileOverviewFragment;
    FollowupFragment followupFragment;

    private ViewPager setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());

//        profileOverviewFragment = ProfileOverviewFragment.newInstance(this.getIntent().getExtras());
        profileOverviewFragment = MemberProfileContactsFragment.newInstance(this.getIntent().getExtras());
        followupFragment = FollowupFragment.newInstance(this.getIntent().getExtras());
        ProfileTasksFragment profileTasksFragment = ProfileTasksFragment.newInstance(this.getIntent().getExtras());
        growthFragment = GMPFragment.newInstance(this.getIntent().getExtras());
        growthFragment.setChildDetails(householdDetails);
        childImmunizationFragment = ChildImmunizationFragment.newInstance(this.getIntent().getExtras());
        childImmunizationFragment.setChildDetails(householdDetails);

        womanImmunizationFragment = WomanImmunizationFragment.newInstance(this.getIntent().getExtras());
        womanImmunizationFragment.setChildDetails(householdDetails);

//        adapter.addFragment(profileOverviewFragment, this.getString(R.string.members));
        adapter.addFragment(profileOverviewFragment, this.getString(R.string.household_overview));
        adapter.addFragment(followupFragment, "FOLLOWUP");
        String delivery_status = householdDetails.getColumnmaps().get(PREGNANT_STATUS);
        delivery_status = delivery_status == null ? "" : delivery_status;
        if (((typeofMember.equalsIgnoreCase("malechild") || (typeofMember.equalsIgnoreCase("femalechild"))) && getAge() <= 5)) {
            womanImmunizationFragment = null;
            adapter.addFragment(childImmunizationFragment, "IMMUNIZATION");
            adapter.addFragment(growthFragment, "GMP");
            tabLayout.setTabMode(TabLayout.MODE_SCROLLABLE);
        }else{
            if (getGender()==0&&getAge()>=15) {
                childImmunizationFragment = null;
                adapter.addFragment(womanImmunizationFragment, "IMMUNIZATION");
                tabLayout.setTabMode(TabLayout.MODE_SCROLLABLE);
            }
            if (delivery_status.equalsIgnoreCase("প্রসব পূর্ব") || delivery_status.equalsIgnoreCase("Antenatal Period")) {
                //growthFragment.setIsChild(false);
                adapter.addFragment(growthFragment, "GROWTH");
                tabLayout.setTabMode(TabLayout.MODE_SCROLLABLE);
            }
        }



        viewPager.setAdapter(adapter);

        return viewPager;
    }
    private void updateProfileColorByText(String resultText){
        updateProfileIconColor(ZScore.getZscoreColorByText(resultText),resultText);
    }
    public void updateProfileIconColor(int color,String text){

        if(!TextUtils.isEmpty(text)){
            imageView.setColorFilter(ContextCompat.getColor(this, color), android.graphics.PorterDuff.Mode.MULTIPLY);

        }
    }
    public void updateWeightHeightStatus(){
        AncRepository repo = (AncRepository) AncApplication.getInstance().getRepository();
        SQLiteDatabase db = repo.getReadableDatabase();

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        this.age = getAge();
        this.gender = getGender();
        this.marital_status = getMaritalStatus();
        // When user click home menu item then quit this activity.
        if (itemId == android.R.id.home) {
            finish();
        } else {
            AlertDialog.Builder builderSingle = new AlertDialog.Builder(this);

            final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
            String patient_identifier = householdDetails.getColumnmaps().get("Patient_Identifier");

            if (!(patient_identifier == null || (patient_identifier != null && patient_identifier.isEmpty()) || patient_identifier.equalsIgnoreCase("null"))) {
                //            arrayAdapter.add(getString(R.string.start_follow_up));
                if (age >= 10) {
                    arrayAdapter.add("মোবাইল নম্বর");
                }

                if (age >= 5 && marital_status == 1 && gender == 0) {
                    arrayAdapter.add("গর্ভাবস্থা");
//                arrayAdapter.add("জন্ম");

                }

                if (age >= 5) {
                    arrayAdapter.add("বৈবাহিক অবস্থা");
                    arrayAdapter.add("ঝুঁকিপূর্ণ অভ্যাস");
//                arrayAdapter.add("স্থানান্তর");
                }
            }


            arrayAdapter.add("তথ্য সংগ্রহ সম্ভব নয়");
//            arrayAdapter.add("সদস্য পাওয়া যায়নি");

//            arrayAdapter.add(getString(R.string.close_anc_record));

            builderSingle.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String textClicked = arrayAdapter.getItem(which);
                    switch (textClicked) {
                        case "Call":
                            launchPhoneDialer(womanPhoneNumber);
                            break;
                        case "গর্ভাবস্থা":

                            getIntent().putExtra(Constants.INTENT_KEY.BASE_ENTITY_ID, householdDetails.getCaseId());
                            JsonFormUtils.launchFollowUpForm(MemberProfileActivity.this, householdDetails.getColumnmaps(), Followup_Form_MHV_Pregnant);
                            break;
                        case "মোবাইল নম্বর":
                            getIntent().putExtra(Constants.INTENT_KEY.BASE_ENTITY_ID, householdDetails.getCaseId());
                            JsonFormUtils.launchFollowUpForm(MemberProfileActivity.this, householdDetails.getColumnmaps(), Followup_Form_MHV_Mobile_no);
                            break;
                        case "বৈবাহিক অবস্থা":

                            String Followup_Form_MHV_Marital = Followup_Form_MHV_Marital_F;
                            if (MemberProfileActivity.this.gender == 1)
                                Followup_Form_MHV_Marital = Followup_Form_MHV_Marital_M;
                            getIntent().putExtra(Constants.INTENT_KEY.BASE_ENTITY_ID, householdDetails.getCaseId());
                            JsonFormUtils.launchFollowUpForm(MemberProfileActivity.this, householdDetails.getColumnmaps(), Followup_Form_MHV_Marital);
                            break;
                        case "জন্ম":
                            getIntent().putExtra(Constants.INTENT_KEY.BASE_ENTITY_ID, householdDetails.getCaseId());
                            JsonFormUtils.launchFollowUpForm(MemberProfileActivity.this, householdDetails.getColumnmaps(), Followup_Form_MHV_Delivery);
                            break;
                        case "স্থানান্তর":
                            getIntent().putExtra(Constants.INTENT_KEY.BASE_ENTITY_ID, householdDetails.getCaseId());
                            JsonFormUtils.launchFollowUpForm(MemberProfileActivity.this, householdDetails.getColumnmaps(), Followup_Form_MHV_Transfer);
                            break;

                        case "ঝুঁকিপূর্ণ অভ্যাস":
                            getIntent().putExtra(Constants.INTENT_KEY.BASE_ENTITY_ID, householdDetails.getCaseId());
                            JsonFormUtils.launchFollowUpForm(MemberProfileActivity.this, householdDetails.getColumnmaps(), Followup_Form_MHV_Risky_Habit);
                            break;
                        case "তথ্য সংগ্রহ সম্ভব নয়":
                            getIntent().putExtra(Constants.INTENT_KEY.BASE_ENTITY_ID, householdDetails.getCaseId());
                            JsonFormUtils.launchFollowUpForm(MemberProfileActivity.this, householdDetails.getColumnmaps(), Followup_Form_MHV_Death);
                            break;
                        case "সদস্য পাওয়া যায়নি":
                            removeChildAlertDialog();
                            break;
                        case "Close ANC Record":
                            JsonFormUtils.launchANCCloseForm(MemberProfileActivity.this);
                            break;
                        default:
                            break;
                    }

                    dialog.dismiss();
                }

            });
            builderSingle.show();
        }

        return super.onOptionsItemSelected(item);
    }

    protected void removeChildAlertDialog() {
        android.support.v7.app.AlertDialog alertDialog = new android.support.v7.app.AlertDialog.Builder(this).create();
        alertDialog.setTitle("Are you sure you want to remove this member?");

        alertDialog.setButton(android.support.v7.app.AlertDialog.BUTTON_NEGATIVE, "NO",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.setButton(android.support.v7.app.AlertDialog.BUTTON_POSITIVE, "YES",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
        alertDialog.show();
    }

    protected android.support.v7.app.AlertDialog createChildAlertDialog() {
        android.support.v7.app.AlertDialog alertDialog = new android.support.v7.app.AlertDialog.Builder(this).create();
        alertDialog.setTitle("Add Child");

        alertDialog.setButton(android.support.v7.app.AlertDialog.BUTTON_NEGATIVE, "Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.setButton(android.support.v7.app.AlertDialog.BUTTON_POSITIVE, "Add [+]",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
        return alertDialog;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_profile_activity, menu);
        return true;
    }

    @Override
    public void onResume() {
        super.onResume();
        String baseEntityId = getIntent().getStringExtra(Constants.INTENT_KEY.BASE_ENTITY_ID);
        mProfilePresenter.refreshProfileView(baseEntityId);
        refreshProfileViews();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mProfilePresenter.onDestroy(isChangingConfigurations());
    }

    @Override
    protected void onCreation() { //Overriden from Secured Activity
    }

    @Override
    protected void onResumption() {//Overriden from Secured Activity

    }

    @Override
    public void setProfileName(String fullName) {
        this.womanName = fullName;
        nameView.setText(fullName);
    }

    @Override
    public void setProfileID(String ancId) {
        ancIdView.setText("ID: " + ancId);
    }

    @Override
    public void setProfileAge(String age) {
        ageView.setText("AGE " + age);

    }

    @Override
    public void setProfileGestationAge(String gestationAge) {
        gestationAgeView.setText(gestationAge != null ? "GA: " + gestationAge + " WEEKS" : "GA");
    }

    @Override
    public void setProfileImage(String baseEntityId) {
        imageRenderHelper.refreshProfileImage(baseEntityId, imageView);
    }

    @Override
    public void setWomanPhoneNumber(String phoneNumber) {
        womanPhoneNumber = phoneNumber;
    }

    @Override
    public void startFormActivity(JSONObject form) {
        try {
            Intent intent = new Intent(this, AncJsonFormActivity.class);
            intent.putExtra("json", form.toString());
            startActivityForResult(intent, JsonFormUtils.REQUEST_CODE_GET_JSON);
        } catch (Exception e) {
Utils.appendLog(getClass().getName(),e);

        }

    }

    @Override
    public ProfileContract.View getView() {
        return this;
    }


    @Override
    public String getIntentString(String intentKey) {

        return this.getIntent().getStringExtra(intentKey);
    }

    @Override
    public void displayToast(int stringID) {

        Utils.showShortToast(this, this.getString(stringID));
    }

    protected void launchPhoneDialer(String phoneNumber) {
        if (PermissionUtils.isPermissionGranted(this, Manifest.permission.READ_PHONE_STATE, PermissionUtils.PHONE_STATE_PERMISSION_REQUEST_CODE)) {
            try {
                Intent intent = new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", phoneNumber, null));
                this.startActivity(intent);
            } catch (Exception e) {
Utils.appendLog(getClass().getName(),e);

                Log.i(TAG, "No dial application so we launch copy to clipboard...");
                CopyToClipboardDialog copyToClipboardDialog = new CopyToClipboardDialog(this, R.style.copy_clipboard_dialog);
                copyToClipboardDialog.setContent(phoneNumber);
                copyToClipboardDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                copyToClipboardDialog.show();
            }
        }
    }

    @Override
    public void onGiveToday(ServiceWrapper serviceWrapper, View view) {
        if (childImmunizationFragment != null)
            childImmunizationFragment.onGiveToday(serviceWrapper, view);
        else
            womanImmunizationFragment.onGiveToday(serviceWrapper, view);
    }

    @Override
    public void onGiveEarlier(ServiceWrapper serviceWrapper, View view) {
        if (childImmunizationFragment != null)
            childImmunizationFragment.onGiveEarlier(serviceWrapper, view);
        else
            womanImmunizationFragment.onGiveEarlier(serviceWrapper, view);

    }

    @Override
    public void onUndoService(ServiceWrapper serviceWrapper, View view) {
        if (childImmunizationFragment != null)
            childImmunizationFragment.onUndoService(serviceWrapper, view);
        else
            womanImmunizationFragment.onUndoService(serviceWrapper, view);

    }

    @Override
    public void onVaccinateToday(ArrayList<VaccineWrapper> arrayList, View view) {
        if (childImmunizationFragment != null)
            childImmunizationFragment.onVaccinateToday(arrayList, view);
        else
            womanImmunizationFragment.onVaccinateToday(arrayList, view);
    }

    @Override
    public void onVaccinateEarlier(ArrayList<VaccineWrapper> arrayList, View view) {
        if (childImmunizationFragment != null)
            childImmunizationFragment.onVaccinateEarlier(arrayList, view);
        else
            womanImmunizationFragment.onVaccinateEarlier(arrayList, view);
    }

    @Override
    public void onUndoVaccination(VaccineWrapper vaccineWrapper, View view) {
        if (childImmunizationFragment != null)
            childImmunizationFragment.onUndoVaccination(vaccineWrapper, view);
        else
            womanImmunizationFragment.onUndoVaccination(vaccineWrapper, view);
    }

    @Override
    public void onWeightTaken(WeightWrapper weightWrapper) {
        growthFragment.onWeightTaken(weightWrapper);
    }

    @Override
    public void onHeightTaken(HeightWrapper heightWrapper) {
        growthFragment.onHeightTaken(heightWrapper);
    }
    @Override
    public void onMUACTaken(MUACWrapper muacWrapper) {
        growthFragment.onMUACTaken(muacWrapper);
    }
    public int getGender() {
        String gender = householdDetails.getColumnmaps().get("gender");
        if (gender == null)
            return -1;
        return gender.equals("M") ? 1 : 0;
    }

    public int getAge() {
        String age = householdDetails.getColumnmaps().get("age");
        String dob = householdDetails.getColumnmaps().get("dob");
        if (dob != null && dob.contains("T")) {
            dob = dob.substring(0, dob.indexOf('T'));
        }

        if (dob != null) {
            try {
                Date dateob = new SimpleDateFormat("yyyy-MM-dd").parse(dob);
//                Date dateob = new Date(dob);
                if (dateob != null) {
                    long time = new Date().getTime() - dateob.getTime();
                    long TWO_MONTHS = 62l * 24l * 60l * 60l * 1000l;
                    double YEAR = 365d * 24d * 60d * 60d * 1000d;
                    if (time <= TWO_MONTHS) {
                        return 0;
                    }
                    int years = (int) (time / YEAR);
                    return years;
                }

            } catch (Exception e) {
Utils.appendLog(getClass().getName(),e);

            }


        }

        if ((age != null && !age.isEmpty())) {
            return Integer.parseInt(age.trim());
        }


        return 0;
    }

    private int getMaritalStatus() {
        String maritalStatus = householdDetails.getColumnmaps().get("MaritalStatus");

        //"বিবাহিত"
        return maritalStatus != null && (maritalStatus.equals("Married") || maritalStatus.equalsIgnoreCase("বিবাহিত")) ? 1 : 0;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        householdDetails.getColumnmaps().putAll(AncApplication.getInstance().getContext().detailsRepository().getAllDetailsForClient(householdDetails.entityId()));
        followupFragment.notifyAdapter();
        if (requestCode == JsonFormUtils.REQUEST_CODE_GET_JSON && resultCode == RESULT_OK) {
            try {
                String jsonString = data.getStringExtra("json");
                final JSONObject form = new JSONObject(jsonString);
                if (form.getString(JsonFormUtils.ENCOUNTER_TYPE).equals("Followup Delivery")) {
                    android.support.v7.app.AlertDialog alertDialog = new android.support.v7.app.AlertDialog.Builder(this).create();
                    alertDialog.setTitle("Add Child");
                    alertDialog.setCanceledOnTouchOutside(false);
                    alertDialog.setButton(android.support.v7.app.AlertDialog.BUTTON_NEGATIVE, "CANCEL",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                    alertDialog.setButton(android.support.v7.app.AlertDialog.BUTTON_POSITIVE, "ADD [+]",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    try {

                                        mProfilePresenter.startMemberRegistrationForm(Constants.JSON_FORM.MEMBER_REGISTER, null, null, null, householdDetails.entityId());
                                    } catch (Exception e) {
Utils.appendLog(getClass().getName(),e);
                                        e.printStackTrace();
                                    }
                                }
                            });
                    alertDialog.show();
                } else if (form.getString(JsonFormUtils.ENCOUNTER_TYPE).equals("Followup Death Status")) {
                    android.support.v7.app.AlertDialog alertDialog = new android.support.v7.app.AlertDialog.Builder(this).create();
                    alertDialog.setTitle("Are you confirm about the action?");
                    alertDialog.setCanceledOnTouchOutside(false);
                    alertDialog.setButton(android.support.v7.app.AlertDialog.BUTTON_NEGATIVE, "NO",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                    alertDialog.setButton(android.support.v7.app.AlertDialog.BUTTON_POSITIVE, "CONFIRM",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    try {
                                        String entity_id = form.getString("entity_id");
                                        removeMember(entity_id);


                                    } catch (Exception e) {
Utils.appendLog(getClass().getName(),e);
                                        e.printStackTrace();
                                    }
                                }
                            });
                    alertDialog.show();
                } else if (form.getString(JsonFormUtils.ENCOUNTER_TYPE).equals("Followup Pregnant Status")) {
                    updateScheduledTasks(form);
                } else if (form.getString(JsonFormUtils.ENCOUNTER_TYPE).equals(Constants.EventType.MemberREGISTRATION) ||
                        form.getString(JsonFormUtils.ENCOUNTER_TYPE).equals(Constants.EventType.OOCMemberREGISTRATION)) {
                    mProfilePresenter.saveForm(jsonString, true);
                }

            } catch (Exception e) {
Utils.appendLog(getClass().getName(),e);
                Log.e(TAG, Log.getStackTraceString(e));
            }
        }

    }

    public void updateScheduledTasks(final JSONObject form) {
        org.smartregister.util.Utils.startAsyncTask((new AsyncTask() {
            @Override
            protected Object doInBackground(Object[] objects) {
                AncRepository repo = (AncRepository) AncApplication.getInstance().getRepository();
                SQLiteDatabase db = repo.getReadableDatabase();
                try {
                    JSONArray fields = form.getJSONObject("step1").getJSONArray("fields");
                    int live_birth = 0;
                    String entity_id = householdDetails.entityId();

                    for (int i = 0; i < fields.length(); i++) {
                        JSONObject field_object = fields.getJSONObject(i);
                        if (field_object.getString("key").equalsIgnoreCase("Live Birth")) {
                            String value = field_object.getString("value");
                            if (value != null && !StringUtils.isEmpty(value)) {
                                live_birth = Integer.valueOf(value);
                                break;
                            }
                        }

                    }
                    String sql = "UPDATE ec_woman SET tasks = '" + live_birth + "' WHERE base_entity_id = '" + entity_id + "';";
                    db.execSQL(sql);

                } catch (Exception e) {
Utils.appendLog(getClass().getName(),e);
                    e.printStackTrace();
                }
                return null;
            }
        }), null);
    }

    public void removeMember(final String entity_id) {
        org.smartregister.util.Utils.startAsyncTask((new AsyncTask() {

            @Override
            protected void onPostExecute(Object o) {
                super.onPostExecute(o);
                MemberProfileActivity.this.finish();
            }

            @Override
            protected Object doInBackground(Object[] objects) {
                AncRepository repo = (AncRepository) AncApplication.getInstance().getRepository();
                SQLiteDatabase db = repo.getReadableDatabase();
                String tables[] = {"ec_household", "ec_member", "ec_child", "ec_woman", "ec_household_search", "ec_member_search", "ec_child_search", "ec_woman_search"};

                Cursor cursor = null;
                try {
                    for (int i = 0; i < tables.length; i++) {
                        String sql = "select * from " + tables[i] + " where base_entity_id = '" + entity_id + "';";
                        cursor = db.rawQuery(sql, new String[]{});
                        if (cursor != null && cursor.getCount() != 0) {
                            sql = "UPDATE " + tables[i] + " SET 'date_removed' = '20-12-2019' WHERE base_entity_id = '" + entity_id + "';";
                            db.execSQL(sql);
//                        db.rawQuery(sql,new String[]{});

                        }
                        if (cursor != null)
                            cursor.close();
                    }

                } catch (Exception e) {
Utils.appendLog(getClass().getName(),e);

                }


                return null;
            }
        }), null);

    }


}

