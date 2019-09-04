package org.smartregister.cbhc.activity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import net.sqlcipher.Cursor;
import net.sqlcipher.database.SQLiteDatabase;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.CoreLibrary;
import org.smartregister.cbhc.R;
import org.smartregister.cbhc.adapter.ViewPagerAdapter;
import org.smartregister.cbhc.application.AncApplication;
import org.smartregister.cbhc.barcode.BarcodeIntentIntegrator;
import org.smartregister.cbhc.contract.ProfileContract;
import org.smartregister.cbhc.contract.RegisterContract;
import org.smartregister.cbhc.domain.AttentionFlag;
import org.smartregister.cbhc.domain.UniqueId;
import org.smartregister.cbhc.fragment.ProfileContactsFragment;
import org.smartregister.cbhc.fragment.ProfileOverviewFragment;
import org.smartregister.cbhc.fragment.ProfileTasksFragment;
import org.smartregister.cbhc.fragment.QuickCheckFragment;
import org.smartregister.cbhc.helper.ImageRenderHelper;
import org.smartregister.cbhc.presenter.ProfilePresenter;
import org.smartregister.cbhc.presenter.RegisterPresenter;
import org.smartregister.cbhc.provider.RegisterProvider;
import org.smartregister.cbhc.repository.AncRepository;
import org.smartregister.cbhc.repository.HealthIdRepository;
import org.smartregister.cbhc.repository.UniqueIdRepository;
import org.smartregister.cbhc.util.Constants;
import org.smartregister.cbhc.util.DBConstants;
import org.smartregister.cbhc.util.ImageLoaderByGlide;
import org.smartregister.cbhc.util.JsonFormUtils;
import org.smartregister.cbhc.util.LookUpUtils;
import org.smartregister.cbhc.util.Utils;
import org.smartregister.cbhc.view.CopyToClipboardDialog;
import org.smartregister.commonregistry.CommonPersonObject;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.domain.FetchStatus;
import org.smartregister.domain.ProfileImage;
import org.smartregister.repository.ImageRepository;
import org.smartregister.util.DateUtil;
import org.smartregister.util.PermissionUtils;

import java.io.Serializable;
import java.util.List;

import static org.smartregister.cbhc.fragment.ProfileOverviewFragment.EXTRA_HOUSEHOLD_DETAILS;
import static org.smartregister.cbhc.util.Constants.FOLLOWUP_FORM.Followup_Form_MHV_Transfer;
import static org.smartregister.util.Utils.getName;
import static org.smartregister.util.Utils.getValue;

/**
 * Created by ndegwamartin on 10/07/2018.
 */
public class ProfileActivity extends BaseProfileActivity implements ProfileContract.View, RegisterContract.View {

    private TextView nameView;
    private TextView ageView;
    private TextView gestationAgeView;
    private TextView ancIdView;
    private ImageView imageView;
    private ImageRenderHelper imageRenderHelper;
    private String womanPhoneNumber;

    private static final String TAG = ProfileActivity.class.getCanonicalName();

    public static final String DIALOG_TAG = "PROFILE_DIALOG_TAG";
    private CommonPersonObjectClient householdDetails;
    public ProfileOverviewFragment profileOverviewFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle extras = this.getIntent().getExtras();
        if (extras != null) {
            Serializable serializable = extras.getSerializable(EXTRA_HOUSEHOLD_DETAILS);
            if (serializable != null && serializable instanceof CommonPersonObjectClient) {
                householdDetails = (CommonPersonObjectClient) serializable;
                if(RegisterProvider.memberCountHashMap.containsKey(householdDetails.entityId()))
                RegisterProvider.memberCountHashMap.remove(householdDetails.entityId());
            }
        }
        setUpViews();

        mProfilePresenter = new ProfilePresenter(this);
        mProfilePresenter.setProfileActivity(this);

        imageRenderHelper = new ImageRenderHelper(this);

        presenter = new RegisterPresenter(ProfileActivity.this);
    }

    private void setUpViews() {

        TabLayout tabLayout = findViewById(R.id.tabs);
        ViewPager viewPager = findViewById(R.id.viewpager);
        tabLayout.setupWithViewPager(setupViewPager(viewPager));

        ageView = findViewById(R.id.textview_age);
        gestationAgeView = findViewById(R.id.textview_gestation_age);
        ancIdView = findViewById(R.id.textview_anc_id);
        nameView = findViewById(R.id.textview_name);
        imageView = findViewById(R.id.imageview_profile);


        String firstName = org.smartregister.util.Utils.getValue(householdDetails.getColumnmaps(), DBConstants.KEY.FIRST_NAME, true);
        String lastName = org.smartregister.util.Utils.getValue(householdDetails.getColumnmaps(), DBConstants.KEY.LAST_NAME, true);
        if (lastName.equalsIgnoreCase("null") || lastName == null) {
            lastName = "";
        }
        String patientName = getName(firstName, lastName);

        patientName = patientName + " (খানা প্রধান)";
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
                Log.e(getClass().getName(), e.toString(), e);
            }
        }
        ImageRepository imageRepo = CoreLibrary.getInstance().context().imageRepository();
        ProfileImage imageRecord = imageRepo.findByEntityId(householdDetails.entityId());
        if (imageRecord != null)
            ImageLoaderByGlide.setImageAsTarget(imageRecord.getFilepath(), imageView, 0);
        // DrishtiApplication.getCachedImageLoaderInstance().getImageByClientId(householdDetails.entityId(), OpenSRPImageLoader.getStaticImageListener((ImageView)findViewById(R.id.imageview_profile), 0, 0));
        setProfileAge(durationString);
        setProfileID(getValue(householdDetails.getColumnmaps(), "Patient_Identifier", true));
        gestationAgeView.setVisibility(View.GONE);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
    }

    public void refreshProfileViews() {
        org.smartregister.util.Utils.startAsyncTask(new AsyncTask() {
            @Override
            protected Object doInBackground(Object[] objects) {
//                householdDetails = CommonPersonObjectToClient(AncApplication.getInstance().getContext().commonrepository(DBConstants.HOUSEHOLD_TABLE_NAME).findByBaseEntityId(householdDetails.entityId()));
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
                patientName = patientName + " (খানা প্রধান)";

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
                        Log.e(getClass().getName(), e.toString(), e);
                    }
                }
                setProfileAge(durationString);
                setProfileID(getValue(householdDetails.getColumnmaps(), "Patient_Identifier", true));
                gestationAgeView.setVisibility(View.GONE);
            }
        },null);





    }

    private CommonPersonObjectClient CommonPersonObjectToClient(CommonPersonObject commonPersonObject) {
        CommonPersonObjectClient commonPersonObjectClient = new CommonPersonObjectClient(commonPersonObject.getCaseId(), commonPersonObject.getDetails(), DBConstants.HOUSEHOLD_TABLE_NAME);
        commonPersonObjectClient.setColumnmaps(commonPersonObject.getColumnmaps());
        return commonPersonObjectClient;
    }

    private UniqueIdRepository uniqueIdRepository;
    private HealthIdRepository healthIdRepository;

    public UniqueIdRepository getUniqueIdRepository() {
        if (uniqueIdRepository == null) {
            uniqueIdRepository = AncApplication.getInstance().getUniqueIdRepository();
        }
        return uniqueIdRepository;
    }

    String patient_identifier;

    public HealthIdRepository getHealthIdRepository() {
        if (healthIdRepository == null) {
            healthIdRepository = AncApplication.getInstance().getHealthIdRepository();
        }
        return healthIdRepository;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_profile_registration_info:
                householdDetails.getColumnmaps().putAll(AncApplication.getInstance().getContext().detailsRepository().getAllDetailsForClient(householdDetails.entityId()));
                patient_identifier = householdDetails.getColumnmaps().get("Patient_Identifier");

                if (patient_identifier == null || (patient_identifier != null && patient_identifier.isEmpty()) || patient_identifier.equalsIgnoreCase("null")) {
                    UniqueId uniqueId = getUniqueIdRepository().getNextUniqueId();
                    final String entityId = uniqueId != null ? uniqueId.getOpenmrsId() : "";
                    if (StringUtils.isBlank(entityId)) {
                        displayShortToast(R.string.no_openmrs_id);
                    } else {
                        householdDetails.getColumnmaps().put("Patient_Identifier", entityId);
                    }
                }


                String formMetadata = JsonFormUtils.getHouseholdJsonEditFormString(this, householdDetails.getColumnmaps());
                try {
                    JsonFormUtils.startFormForEdit(this, JsonFormUtils.REQUEST_CODE_GET_JSON, formMetadata);
                } catch (Exception e) {

                }
                break;
            case R.id.edit_member:
                CommonPersonObjectClient pclient = (CommonPersonObjectClient) view.getTag();
                pclient.getColumnmaps().putAll(AncApplication.getInstance().getContext().detailsRepository().getAllDetailsForClient(pclient.entityId()));
                patient_identifier = pclient.getColumnmaps().get("Patient_Identifier");
                pclient.getColumnmaps().put("relational_id", householdDetails.getCaseId());
                if (patient_identifier == null || (patient_identifier != null && patient_identifier.isEmpty()) || patient_identifier.equalsIgnoreCase("null")) {
                    Long unUsedIds = getHealthIdRepository().countUnUsedIds();
                    if (unUsedIds > 0l) {
                        householdDetails.getColumnmaps().put("Patient_Identifier", Utils.DEFAULT_IDENTIFIER);
                        launchFormEdit(pclient);
                    } else {
                        displayShortToast(R.string.no_openmrs_id);
                    }
                } else {
                    launchFormEdit(pclient);
                }


                break;
            case R.id.profile_image_iv:
                CommonPersonObjectClient memberclient = (CommonPersonObjectClient) view.getTag(R.id.clientformemberprofile);
                memberclient.getColumnmaps().put("relational_id", householdDetails.getCaseId());
                String clienttype = (String) view.getTag(R.id.typeofclientformemberprofile);
                Intent intent = new Intent(this, MemberProfileActivity.class);
                intent.putExtra(Constants.INTENT_KEY.BASE_ENTITY_ID, memberclient.getCaseId());
                intent.putExtra(ProfileOverviewFragment.EXTRA_HOUSEHOLD_DETAILS, memberclient);
                intent.putExtra("type_of_member", clienttype);
                startActivityForResult(intent, 1002);
                break;
            case R.id.total_birth_btn:
                CommonPersonObjectClient pClient = (CommonPersonObjectClient) view.getTag(R.id.clientformemberprofile);
                String firstNameEnglish = org.smartregister.util.Utils.getValue(pClient.getColumnmaps(), DBConstants.KEY.FIRST_NAME, true);
                String lastNameEnglish = org.smartregister.util.Utils.getValue(pClient.getColumnmaps(), DBConstants.KEY.LAST_NAME, true);
                motherNameEnglish = getName(firstNameEnglish, lastNameEnglish);
                try {
                    presenter.startMemberRegistrationForm(Constants.JSON_FORM.MEMBER_REGISTER, null, null, null, householdDetails.entityId());
                } catch (Exception e) {
                    e.printStackTrace();
                }

                break;
        }
    }

    private void launchFormEdit(CommonPersonObjectClient pclient) {
        String formMetadataformembers = JsonFormUtils.getMemberJsonEditFormString(this, pclient.getColumnmaps());
        try {
            JsonFormUtils.startFormForEdit(this, JsonFormUtils.REQUEST_CODE_GET_JSON, formMetadataformembers);
        } catch (Exception e) {

        }
    }

    private String motherNameEnglish;

    public void updatePatientIdentifier(JSONObject jsonForm) {
        try {
            if (jsonForm.has("step1")) {
                JSONObject step1 = jsonForm.getJSONObject("step1");
                if (step1.has("fields")) {
                    JSONArray flds = step1.getJSONArray("fields");
                    updatePatientIdentifier(flds);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void updatePatientIdentifier(JSONArray fields) {
        for (int i = 0; i < fields.length(); i++) {
            try {
                JSONObject fieldObject = fields.getJSONObject(i);
                if ("Patient_Identifier".equalsIgnoreCase(fieldObject.optString("key"))) {
                    String value = fieldObject.optString("value");
                    if (Utils.DEFAULT_IDENTIFIER.equalsIgnoreCase(value)) {

                        UniqueId uniqueId = getHealthIdRepository().getNextUniqueId();
                        final String entityId = uniqueId != null ? uniqueId.getOpenmrsId() : "";
                        if (!StringUtils.isBlank(entityId)) {
                            fieldObject.put("value", entityId);
                        }
                        break;
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        motherNameEnglish = "";

        if (requestCode == JsonFormUtils.REQUEST_CODE_GET_JSON && resultCode == RESULT_OK) {
            try {
                final String jsonString = data.getStringExtra("json");
                final JSONObject form = new JSONObject(jsonString);
//                updatePatientIdentifier(form);
//                final String jsonString = form.toString();
                if (form.getString(JsonFormUtils.ENCOUNTER_TYPE).equals(Constants.EventType.REGISTRATION)) {
                    presenter.saveForm(jsonString, false);
                } else if (form.getString(JsonFormUtils.ENCOUNTER_TYPE).equals(Constants.EventType.HouseholdREGISTRATION)) {

                    presenter.saveForm(jsonString, false);

                } else if (form.getString(JsonFormUtils.ENCOUNTER_TYPE).equals(Constants.EventType.UPDATE_Household_REGISTRATION)) {

                    presenter.saveForm(jsonString, true);

                } else if (form.getString(JsonFormUtils.ENCOUNTER_TYPE).equals(Constants.EventType.MemberREGISTRATION)) {
                    presenter.saveForm(jsonString, false);
                    updateScheduledTasks(form);
                } else if (form.getString(JsonFormUtils.ENCOUNTER_TYPE).equals(Constants.EventType.CLOSE)) {
                    presenter.closeAncRecord(jsonString);
                } else if (form.getString(JsonFormUtils.ENCOUNTER_TYPE).equals("Followup HH Transfer")) {
                    android.support.v7.app.AlertDialog alertDialog = new android.support.v7.app.AlertDialog.Builder(this).create();
                    alertDialog.setTitle("Are you confirm about the action?");
                    alertDialog.setCanceledOnTouchOutside(false);
                    alertDialog.setButton(android.support.v7.app.AlertDialog.BUTTON_NEGATIVE, "NO",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                    Utils.VIEWREFRESH = false;
                                }
                            });
                    alertDialog.setButton(android.support.v7.app.AlertDialog.BUTTON_POSITIVE, "CONFIRM",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    try {
                                        String entity_id = form.getString("entity_id");
                                        removeMember(entity_id);
                                        presenter.saveForm(jsonString, false);
                                        Utils.VIEWREFRESH = true;

                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
                    alertDialog.show();
                } else {
                    super.onActivityResult(requestCode, resultCode, data);
                }
            } catch (Exception e) {
                Log.e(TAG, Log.getStackTraceString(e));
            } finally {
                refreshList(null);
//                refreshProfileViews();
                Utils.VIEWREFRESH = true;
            }

        } else if (requestCode == BarcodeIntentIntegrator.REQUEST_CODE && resultCode == RESULT_OK) {
//            BarcodeIntentResult res = BarcodeIntentIntegrator.parseActivityResult(requestCode, resultCode, data);
//            if (StringUtils.isNotBlank(res.getContents())) {
//                Log.d("Scanned QR Code", res.getContents());
//                mBaseFragment.onQRCodeSucessfullyScanned(res.getContents());
//                mBaseFragment.setSearchTerm(res.getContents());
//            } else
//                Log.i("", "NO RESULT FOR QR CODE");
        } else {
            Utils.VIEWREFRESH = true;
            refreshList(null);
//            refreshProfileViews();
        }
    }

    private void updateScheduledTasks(final JSONObject form) {
        org.smartregister.util.Utils.startAsyncTask((new AsyncTask() {
            @Override
            protected Object doInBackground(Object[] objects) {
                AncRepository repo = (AncRepository) AncApplication.getInstance().getRepository();
                SQLiteDatabase db = repo.getReadableDatabase();
                try {
                    JSONArray fields = form.getJSONObject("step1").getJSONArray("fields");
                    String mother_name = "";
                    String entity_id = householdDetails.entityId();

                    for (int i = 0; i < fields.length(); i++) {
                        JSONObject field_object = fields.getJSONObject(i);
                        if (field_object.getString("key").equalsIgnoreCase("Mother_Guardian_First_Name_english")) {
                            String value = field_object.getString("value");
                            if (value != null && !StringUtils.isEmpty(value)) {
                                mother_name = value;
                                mother_name = mother_name.split(" ")[0];
                                break;
                            }
                        }

                    }
                    String sql = "UPDATE ec_woman SET tasks = tasks-1 WHERE relational_id = '" + entity_id + "' AND first_name like '%" + mother_name + "%' AND tasks IS NOT NULL;";
                    db.execSQL(sql);

                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }
        }), null);
    }

    ProfileContactsFragment profileContactsFragment;

    private ViewPager setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());

        profileOverviewFragment = ProfileOverviewFragment.newInstance(this.getIntent().getExtras());

        profileContactsFragment = ProfileContactsFragment.newInstance(this.getIntent().getExtras());
        ProfileTasksFragment profileTasksFragment = ProfileTasksFragment.newInstance(this.getIntent().getExtras());

        adapter.addFragment(profileOverviewFragment, this.getString(R.string.members));
        adapter.addFragment(profileContactsFragment, this.getString(R.string.household_overview));
//        adapter.addFragment(profileTasksFragment, this.getString(R.string.tasks));

        viewPager.setAdapter(adapter);


        return viewPager;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();

        // When user click home menu item then quit this activity.
        if (itemId == android.R.id.home) {
            finish();
        } else {
            AlertDialog.Builder builderSingle = new AlertDialog.Builder(this);

            final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
            arrayAdapter.add("[+] নতুন সদস্য");
//            arrayAdapter.add("খানা স্থানান্তর");
//            arrayAdapter.add("খানা পাওয়া যায়নি");
            arrayAdapter.add("খানার অবস্থান");
//            arrayAdapter.add(getString(R.string.call));
//            arrayAdapter.add(getString(R.string.start_contact));
//            arrayAdapter.add(getString(R.string.close_anc_record));

            builderSingle.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String textClicked = arrayAdapter.getItem(which);
                    switch (textClicked) {
                        case "খানার অবস্থান":
                            getIntent().putExtra(Constants.INTENT_KEY.BASE_ENTITY_ID, householdDetails.getCaseId());
                            JsonFormUtils.launchFollowUpForm(ProfileActivity.this, householdDetails.getColumnmaps(), Followup_Form_MHV_Transfer);
                            break;
                        case "[+] নতুন সদস্য":
                            try {

                                presenter.startMemberRegistrationForm(Constants.JSON_FORM.MEMBER_REGISTER, null, null, null, householdDetails.entityId());
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

//                            Intent intent = new Intent(ProfileActivity.this, AncJsonFormActivity.class);
//                            intent.putExtra("json", JsonFormUtils.);
//                            startActivityForResult(intent, JsonFormUtils.REQUEST_CODE_GET_JSON);
                            break;
                        case "খানা স্থানান্তর":
                            getIntent().putExtra(Constants.INTENT_KEY.BASE_ENTITY_ID, householdDetails.getCaseId());
                            JsonFormUtils.launchFollowUpForm(ProfileActivity.this, householdDetails.getColumnmaps(), Followup_Form_MHV_Transfer);
                            break;
                        case "খানা পাওয়া যায়নি":
                            removeHHAlertDialog();
                            break;
                        case "Call":
                            launchPhoneDialer(womanPhoneNumber);
                            break;
                        case "Start Contact":
                            QuickCheckFragment.launchDialog(ProfileActivity.this, DIALOG_TAG);
                            break;
                        case "Close ANC Record":
                            JsonFormUtils.launchANCCloseForm(ProfileActivity.this);
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

    protected void removeHHAlertDialog() {
        android.support.v7.app.AlertDialog alertDialog = new android.support.v7.app.AlertDialog.Builder(this).create();
        alertDialog.setTitle("Are you sure you want to remove this household?");

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

    RegisterPresenter presenter;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_profile_activity, menu);
        return true;
    }

    @Override
    public void onResume() {
        super.onResume();
//        String baseEntityId = getIntent().getStringExtra(Constants.INTENT_KEY.BASE_ENTITY_ID);
//        mProfilePresenter.refreshProfileView(baseEntityId);
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
        ageView.setVisibility(View.GONE);
//        ageView.setText("AGE " + age);

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

        return this.getIntent().getStringExtra(intentKey) == null ? "" : this.getIntent().getStringExtra(intentKey);
    }

    @Override
    public Context getContext() {
        return this;
    }

    @Override
    public void displaySyncNotification() {

    }

    @Override
    public void displayToast(int stringID) {

        Utils.showShortToast(this, this.getString(stringID));
    }

    @Override
    public void displayToast(String message) {

    }

    @Override
    public void displayShortToast(int resourceId) {
        org.smartregister.util.Utils.showShortToast(this, this.getString(resourceId));
    }

    @Override
    public void showLanguageDialog(List<String> displayValues) {

    }

    @Override
    public void startFormActivity(JSONObject form) {
        try {
            Intent intent = new Intent(this, AncJsonFormActivity.class);
            if (!TextUtils.isEmpty(motherNameEnglish)) {
                LookUpUtils.putMotherName(form, motherNameEnglish);
            }
            intent.putExtra("json", form.toString());
            startActivityForResult(intent, JsonFormUtils.REQUEST_CODE_GET_JSON);

        } catch (Exception e) {

        }

    }

    @Override
    public ProfileContract.View getView() {
        return this;
    }

    @Override
    public void refreshList(FetchStatus fetchStatus) {
        profileOverviewFragment.refreshadapter();

    }

    @Override
    public void showAttentionFlagsDialog(List<AttentionFlag> attentionFlags) {

    }

    @Override
    public void updateInitialsText(String initials) {

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

    public void removeMember(final String entity_id) {
        org.smartregister.util.Utils.startAsyncTask((new AsyncTask() {

            @Override
            protected void onPostExecute(Object o) {
                super.onPostExecute(o);
                ProfileActivity.this.finish();
            }

            @Override
            protected Object doInBackground(Object[] objects) {
                AncRepository repo = (AncRepository) AncApplication.getInstance().getRepository();
                SQLiteDatabase db = repo.getReadableDatabase();
                String tables[] = {"ec_household", "ec_member", "ec_child", "ec_woman", "ec_household_search", "ec_member_search", "ec_child_search", "ec_woman_search"};

                try {
                    for (int i = 0; i < tables.length; i++) {
                        String sql = "select * from " + tables[i] + " where base_entity_id = '" + entity_id + "';";
                        Cursor cursor = db.rawQuery(sql, new String[]{});
                        if (cursor != null && cursor.getCount() != 0) {
                            sql = "UPDATE " + tables[i] + " SET date_removed = '01-01-1000' WHERE base_entity_id = '" + entity_id + "';";
                            db.execSQL(sql);
//                        db.rawQuery(sql,new String[]{});

                        }
                        if (cursor != null)
                            cursor.close();
                    }
                } catch (Exception e) {

                }


                return null;
            }
        }), null);

    }
}

