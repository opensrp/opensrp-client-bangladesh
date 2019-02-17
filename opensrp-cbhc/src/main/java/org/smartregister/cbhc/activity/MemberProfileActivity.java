package org.smartregister.cbhc.activity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.json.JSONObject;
import org.smartregister.CoreLibrary;
import org.smartregister.cbhc.R;
import org.smartregister.cbhc.adapter.ViewPagerAdapter;
import org.smartregister.cbhc.application.AncApplication;
import org.smartregister.cbhc.contract.ProfileContract;
import org.smartregister.cbhc.fragment.ChildImmunizationFragment;
import org.smartregister.cbhc.fragment.FollowupFragment;
import org.smartregister.cbhc.fragment.GrowthFragment;
import org.smartregister.cbhc.fragment.MemberProfileContactsFragment;
import org.smartregister.cbhc.fragment.ProfileOverviewFragment;
import org.smartregister.cbhc.fragment.ProfileTasksFragment;
import org.smartregister.cbhc.helper.ECSyncHelper;
import org.smartregister.cbhc.helper.ImageRenderHelper;
import org.smartregister.cbhc.presenter.ProfilePresenter;
import org.smartregister.cbhc.sync.AncClientProcessorForJava;
import org.smartregister.cbhc.util.Constants;
import org.smartregister.cbhc.util.DBConstants;
import org.smartregister.cbhc.util.ImageLoaderByGlide;
import org.smartregister.cbhc.util.JsonFormUtils;
import org.smartregister.cbhc.util.Utils;
import org.smartregister.cbhc.view.CopyToClipboardDialog;
import org.smartregister.clientandeventmodel.Event;
import org.smartregister.clientandeventmodel.Obs;
import org.smartregister.commonregistry.CommonPersonObject;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.domain.ProfileImage;
import org.smartregister.growthmonitoring.domain.WeightWrapper;
import org.smartregister.growthmonitoring.listener.WeightActionListener;
import org.smartregister.immunization.domain.ServiceWrapper;
import org.smartregister.immunization.domain.VaccineWrapper;
import org.smartregister.immunization.listener.ServiceActionListener;
import org.smartregister.immunization.listener.VaccinationActionListener;
import org.smartregister.repository.BaseRepository;
import org.smartregister.repository.EventClientRepository;
import org.smartregister.repository.ImageRepository;
import org.smartregister.util.DateUtil;
import org.smartregister.util.PermissionUtils;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import static org.smartregister.cbhc.fragment.ProfileOverviewFragment.EXTRA_HOUSEHOLD_DETAILS;
import static org.smartregister.cbhc.util.Constants.FOLLOWUP_FORM.Followup_Form_MHV_Death;
import static org.smartregister.cbhc.util.Constants.FOLLOWUP_FORM.Followup_Form_MHV_Delivery;

import static org.smartregister.cbhc.util.Constants.FOLLOWUP_FORM.Followup_Form_MHV_Marital_F;
import static org.smartregister.cbhc.util.Constants.FOLLOWUP_FORM.Followup_Form_MHV_Marital_M;
import static org.smartregister.cbhc.util.Constants.FOLLOWUP_FORM.Followup_Form_MHV_Mobile_no;
import static org.smartregister.cbhc.util.Constants.FOLLOWUP_FORM.Followup_Form_MHV_Pregnant;
import static org.smartregister.cbhc.util.Constants.FOLLOWUP_FORM.Followup_Form_MHV_Risky_Habit;
import static org.smartregister.cbhc.util.JsonFormUtils.addMetaData;
import static org.smartregister.util.Utils.getName;
import static org.smartregister.util.Utils.getValue;

/**
 * Created by ndegwamartin on 10/07/2018.
 */
public class MemberProfileActivity extends BaseProfileActivity implements ProfileContract.View, VaccinationActionListener, ServiceActionListener, WeightActionListener {

    private TextView nameView;
    private TextView ageView;
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
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle extras = this.getIntent().getExtras();
        if (extras != null) {
            Serializable serializable = extras.getSerializable(EXTRA_HOUSEHOLD_DETAILS);
            if (serializable != null && serializable instanceof CommonPersonObjectClient) {
                householdDetails = (CommonPersonObjectClient) serializable;
            }
            typeofMember = extras.getString("type_of_member");
        }


        setUpViews();

        mProfilePresenter = new ProfilePresenter(this);
        mProfilePresenter.setProfileActivity(this);

        imageRenderHelper = new ImageRenderHelper(this);


    }



    private TabLayout tabLayout;

    private void setUpViews() {
        ImageView circleprofile = (ImageView)findViewById(R.id.imageview_profile);



        if(typeofMember.equalsIgnoreCase("malechild")){
            circleprofile.setImageDrawable(getResources().getDrawable(R.drawable.child_boy_infant));
        }else if(typeofMember.equalsIgnoreCase("femalechild")){
            circleprofile.setImageDrawable(getResources().getDrawable(R.drawable.child_girl_infant));
        }else if(typeofMember.equalsIgnoreCase("woman")){
            circleprofile.setImageDrawable(getResources().getDrawable(R.drawable.female_register_placeholder_profile));
        }else if(typeofMember.equalsIgnoreCase("member")){
            circleprofile.setImageDrawable(getResources().getDrawable(R.drawable.male_register_placeholder_profile));
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
        imageView = findViewById(R.id.imageview_profile);

//setProfileImage(householdDetails.entityId());
        String firstName = org.smartregister.util.Utils.getValue(householdDetails.getColumnmaps(), DBConstants.KEY.FIRST_NAME, true);
        String lastName = org.smartregister.util.Utils.getValue(householdDetails.getColumnmaps(), DBConstants.KEY.LAST_NAME, true);
        if(lastName.equalsIgnoreCase("null")||lastName==null){
            lastName = "";
        }
        String patientName = getName(firstName, lastName);


        setProfileName(patientName);
        String dobString = getValue(householdDetails.getColumnmaps(),"dob",true);
        String durationString = "";
        if (StringUtils.isNotBlank(dobString)) {
            try {
                DateTime birthDateTime = new DateTime(dobString);

                String duration = DateUtil.getDuration(birthDateTime);
                if (duration != null) {
                    durationString = duration;
                }
            } catch (Exception e) {
                Log.e(getClass().getName(), e.toString(), e);
            }
        }
        ImageRepository imageRepo = CoreLibrary.getInstance().context().imageRepository();
        ProfileImage imageRecord = imageRepo.findByEntityId(householdDetails.entityId());
        if(imageRecord!=null)
            ImageLoaderByGlide.setImageAsTarget(imageRecord.getFilepath(),imageView,0);
        setProfileAge(durationString);
        setProfileID(getValue(householdDetails.getColumnmaps(),"Patient_Identifier",true));
        gestationAgeView.setVisibility(View.GONE);
    }

    public void refreshProfileViews(){
//        if(typeofMember.equalsIgnoreCase("malechild")||typeofMember.equalsIgnoreCase("femalechild")){
//            householdDetails = CommonPersonObjectToClient(AncApplication.getInstance().getContext().commonrepository(DBConstants.CHILD_TABLE_NAME).findByBaseEntityId(householdDetails.entityId()));
//        }else if(typeofMember.equalsIgnoreCase("woman")){
//            householdDetails = CommonPersonObjectToClient(AncApplication.getInstance().getContext().commonrepository(DBConstants.WOMAN_TABLE_NAME).findByBaseEntityId(householdDetails.entityId()));
//        }else if(typeofMember.equalsIgnoreCase("member")){
//            householdDetails = CommonPersonObjectToClient(AncApplication.getInstance().getContext().commonrepository(DBConstants.MEMBER_TABLE_NAME).findByBaseEntityId(householdDetails.entityId()));
//        }

        householdDetails.getColumnmaps().putAll(AncApplication.getInstance().getContext().detailsRepository().getAllDetailsForClient(householdDetails.entityId()));
        String firstName = org.smartregister.util.Utils.getValue(householdDetails.getColumnmaps(), DBConstants.KEY.FIRST_NAME, true);
        String lastName = org.smartregister.util.Utils.getValue(householdDetails.getColumnmaps(), DBConstants.KEY.LAST_NAME, true);
        if(lastName.equalsIgnoreCase("null")||lastName==null){
            lastName = "";
        }
        String patientName = getName(firstName, lastName);


        setProfileName(patientName);

        String dobString = getValue(householdDetails.getColumnmaps(),"dob",true);
        String durationString = "";
        if (StringUtils.isNotBlank(dobString)) {
            try {
                DateTime birthDateTime = new DateTime(dobString);

                String duration = DateUtil.getDuration(birthDateTime);
                if (duration != null) {
                    durationString = duration;
                }
            } catch (Exception e) {
                Log.e(getClass().getName(), e.toString(), e);
            }
        }
        setProfileAge(durationString);
        setProfileID(getValue(householdDetails.getColumnmaps(),"Patient_Identifier",true));
        gestationAgeView.setVisibility(View.GONE);
        followupFragment.notifyAdapter();
        profileOverviewFragment.reloadView();
    }

    private CommonPersonObjectClient CommonPersonObjectToClient(CommonPersonObject commonPersonObject) {
        CommonPersonObjectClient commonPersonObjectClient = new CommonPersonObjectClient(commonPersonObject.getCaseId(),commonPersonObject.getDetails(),DBConstants.HOUSEHOLD_TABLE_NAME);
        commonPersonObjectClient.setColumnmaps(commonPersonObject.getColumnmaps());
        return commonPersonObjectClient;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_profile_registration_info:
                householdDetails.getColumnmaps().putAll(AncApplication.getInstance().getContext().detailsRepository().getAllDetailsForClient(householdDetails.entityId()));
                String formMetadataformembers = JsonFormUtils.getMemberJsonEditFormString(this, householdDetails.getColumnmaps());
                try {
                    JsonFormUtils.startFormForEdit(this, JsonFormUtils.REQUEST_CODE_GET_JSON, formMetadataformembers);
                } catch (Exception e) {

                }
                break;
            case R.id.edit_member:
//                CommonPersonObjectClient pclient  = (CommonPersonObjectClient) view.getTag();
//                String formMetadataformembers = JsonFormUtils.getMemberJsonEditFormString(this, pclient.getColumnmaps());
//                try {
//                    JsonFormUtils.startFormForEdit(this, JsonFormUtils.REQUEST_CODE_GET_JSON, formMetadataformembers);
//                } catch (Exception e) {
//
//                }
                break;
        }
    }

    ChildImmunizationFragment childImmunizationFragment;
    GrowthFragment growthFragment;
//    public ProfileOverviewFragment profileOverviewFragment;
    public MemberProfileContactsFragment profileOverviewFragment;
    FollowupFragment followupFragment;
    private ViewPager setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());

//        profileOverviewFragment = ProfileOverviewFragment.newInstance(this.getIntent().getExtras());
        profileOverviewFragment = MemberProfileContactsFragment.newInstance(this.getIntent().getExtras());
        followupFragment = FollowupFragment.newInstance(this.getIntent().getExtras());
        ProfileTasksFragment profileTasksFragment = ProfileTasksFragment.newInstance(this.getIntent().getExtras());
        growthFragment = GrowthFragment.newInstance(this.getIntent().getExtras());
        growthFragment.setChildDetails(householdDetails);
        childImmunizationFragment = ChildImmunizationFragment.newInstance(this.getIntent().getExtras());
        childImmunizationFragment.setChildDetails(householdDetails);

//        adapter.addFragment(profileOverviewFragment, this.getString(R.string.members));
        adapter.addFragment(profileOverviewFragment, this.getString(R.string.household_overview));
        adapter.addFragment(followupFragment, "FOLLOWUP");
        if((typeofMember.equalsIgnoreCase("malechild")||(typeofMember.equalsIgnoreCase("femalechild")))&&getAge()<=5){
            adapter.addFragment(childImmunizationFragment, "IMMUNIZATION");
            adapter.addFragment(growthFragment, "GROWTH");
            tabLayout.setTabMode(TabLayout.MODE_SCROLLABLE);
        }


        viewPager.setAdapter(adapter);

        return viewPager;
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
//            arrayAdapter.add(getString(R.string.start_follow_up));
            if(age>=10){
                arrayAdapter.add("মোবাইল নম্বর");
            }

            if(age>=5&&marital_status==1&&gender==0){
                arrayAdapter.add("গর্ভাবস্থা");
//                arrayAdapter.add("জন্ম");

            }

            if(age>=5){
                arrayAdapter.add("বৈবাহিক অবস্থা");
                arrayAdapter.add("ঝুঁকিপূর্ণ অভ্যাস");
                arrayAdapter.add("স্থানান্তর");
            }


            arrayAdapter.add("মৃত্যু");
            arrayAdapter.add("সদস্য পাওয়া যায়নি");

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

                            getIntent().putExtra(Constants.INTENT_KEY.BASE_ENTITY_ID,householdDetails.getCaseId());
                            JsonFormUtils.launchFollowUpForm(MemberProfileActivity.this,householdDetails.getColumnmaps(),Followup_Form_MHV_Pregnant);
                            break;
                        case "মোবাইল নম্বর":
                            getIntent().putExtra(Constants.INTENT_KEY.BASE_ENTITY_ID,householdDetails.getCaseId());
                            JsonFormUtils.launchFollowUpForm(MemberProfileActivity.this,householdDetails.getColumnmaps(),Followup_Form_MHV_Mobile_no);
                            break;
                        case "বৈবাহিক অবস্থা":

                            String Followup_Form_MHV_Marital = Followup_Form_MHV_Marital_F;
                            if(MemberProfileActivity.this.gender==1)
                                Followup_Form_MHV_Marital = Followup_Form_MHV_Marital_M;
                            getIntent().putExtra(Constants.INTENT_KEY.BASE_ENTITY_ID,householdDetails.getCaseId());
                            JsonFormUtils.launchFollowUpForm(MemberProfileActivity.this,householdDetails.getColumnmaps(),Followup_Form_MHV_Marital);
                            break;
                        case "জন্ম":
                            getIntent().putExtra(Constants.INTENT_KEY.BASE_ENTITY_ID,householdDetails.getCaseId());
                            JsonFormUtils.launchFollowUpForm(MemberProfileActivity.this,householdDetails.getColumnmaps(),Followup_Form_MHV_Delivery);
                            break;
                        case "স্থানান্তর":
                            break;

                        case "ঝুঁকিপূর্ণ অভ্যাস":
                            getIntent().putExtra(Constants.INTENT_KEY.BASE_ENTITY_ID,householdDetails.getCaseId());
                            JsonFormUtils.launchFollowUpForm(MemberProfileActivity.this,householdDetails.getColumnmaps(),Followup_Form_MHV_Risky_Habit);
                            break;
                        case "মৃত্যু":
                            getIntent().putExtra(Constants.INTENT_KEY.BASE_ENTITY_ID,householdDetails.getCaseId());
                            JsonFormUtils.launchFollowUpForm(MemberProfileActivity.this,householdDetails.getColumnmaps(),Followup_Form_MHV_Death);
                            break;
                        case "সদস্য পাওয়া যায়নি":
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
//        refreshProfileViews();
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
        childImmunizationFragment.onGiveToday(serviceWrapper,view);
    }

    @Override
    public void onGiveEarlier(ServiceWrapper serviceWrapper, View view) {
        childImmunizationFragment.onGiveEarlier(serviceWrapper,view);
    }

    @Override
    public void onUndoService(ServiceWrapper serviceWrapper, View view) {
        childImmunizationFragment.onUndoService(serviceWrapper,view);
    }

    @Override
    public void onVaccinateToday(ArrayList<VaccineWrapper> arrayList, View view) {
        childImmunizationFragment.onVaccinateToday(arrayList,view);
    }

    @Override
    public void onVaccinateEarlier(ArrayList<VaccineWrapper> arrayList, View view) {
        childImmunizationFragment.onVaccinateEarlier(arrayList,view);
    }

    @Override
    public void onUndoVaccination(VaccineWrapper vaccineWrapper, View view) {
        childImmunizationFragment.onUndoVaccination(vaccineWrapper,view);
    }

    @Override
    public void onWeightTaken(WeightWrapper weightWrapper) {
        growthFragment.onWeightTaken(weightWrapper);
    }

    public int getGender(){
        String gender = householdDetails.getColumnmaps().get("gender");
        if(gender==null)
            return -1;
        return gender.equals("M")?1:0;
    }
    public int getAge() {
        String age = householdDetails.getColumnmaps().get("age");
        String dob = householdDetails.getColumnmaps().get("dob");
        if(dob!=null&&dob.contains("T")){
            dob = dob.substring(0,dob.indexOf('T'));
        }

        if(dob!=null){
            try{
                Date dateob=new SimpleDateFormat("yyyy-MM-dd").parse(dob);
//                Date dateob = new Date(dob);
                if(dateob!=null) {
                    long time = new Date().getTime()-dateob.getTime();
                    long TWO_MONTHS = 62l*24l*60l*60l*1000l;
                    double YEAR = 365d*24d*60d*60d*1000d;
                    if(time<=TWO_MONTHS){
                        return 0;
                    }
                    int years = (int)(time/YEAR);
                    return years;
                }

            }catch(Exception e){

            }


        }

        if((age!=null&&!age.isEmpty())){
            return Integer.parseInt(age.trim());
        }


        return 0;
    }
    private int getMaritalStatus() {
        String maritalStatus = householdDetails.getColumnmaps().get("MaritalStatus");

        //"বিবাহিত"
        return maritalStatus!=null&&(maritalStatus.equals("Married")||maritalStatus.equalsIgnoreCase("বিবাহিত"))?1:0;
    }

    public void updateClientStatusAsEvent(String attributeName, Object attributeValue, String entityType) {
        try {

            ECSyncHelper syncHelper = AncApplication.getInstance().getEcSyncHelper();


            Date date = new Date();
            EventClientRepository db = (EventClientRepository) AncApplication.getInstance().getEventClientRepository();


            JSONObject client = db.getClientByBaseEntityId(householdDetails.entityId());




            Event event = (Event) new Event()
                    .withBaseEntityId(householdDetails.entityId())
                    .withEventDate(new Date())
                    .withEventType("")
                    .withLocationId(context().allSharedPreferences().fetchCurrentLocality())
                    .withProviderId(context().allSharedPreferences().fetchRegisteredANM())
                    .withEntityType(entityType)
                    .withFormSubmissionId(JsonFormUtils.generateRandomUUIDString())
                    .withDateCreated(new Date());
            event.addObs((new Obs()).withFormSubmissionField(attributeName).withValue(attributeValue).withFieldCode(attributeName).withFieldType("formsubmissionField").withFieldDataType("text").withParentCode("").withHumanReadableValues(new ArrayList<Object>()));




            addMetaData(this, event, date);
            JSONObject eventJson = new JSONObject(JsonFormUtils.gson.toJson(event));
            db.addEvent(householdDetails.entityId(), eventJson);
            long lastSyncTimeStamp = context().allSharedPreferences().fetchLastUpdatedAtDate(0);
            Date lastSyncDate = new Date(lastSyncTimeStamp);
            AncClientProcessorForJava.getInstance(this).processClient(syncHelper.getEvents(lastSyncDate, BaseRepository.TYPE_Unsynced));
            context().allSharedPreferences().saveLastUpdatedAtDate(lastSyncDate.getTime());

            //update details



        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode,resultCode,data);
        householdDetails.getColumnmaps().putAll(AncApplication.getInstance().getContext().detailsRepository().getAllDetailsForClient(householdDetails.entityId()));
        followupFragment.notifyAdapter();
        if (requestCode == JsonFormUtils.REQUEST_CODE_GET_JSON && resultCode == RESULT_OK) {
            String jsonString = data.getStringExtra("json");

//            if(jsonString.contains("Followup Delivery")){
//                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
//
//                attentionFlagDialogView = LayoutInflater.from(this).inflate(R.layout.alert_dialog_attention_flag, null);
//                dialogBuilder.setView(attentionFlagDialogView);
//
//                attentionFlagDialogView.findViewById(R.id.closeButton).setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View view) {
//                        attentionFlagAlertDialog.dismiss();
//                    }
//                });
//
//                attentionFlagAlertDialog = dialogBuilder.create();
//            }
        }
//        data.getBundleExtra()



    }


}

