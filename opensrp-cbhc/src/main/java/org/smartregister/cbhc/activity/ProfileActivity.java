package org.smartregister.cbhc.activity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
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
import org.smartregister.cbhc.barcode.BarcodeIntentIntegrator;
import org.smartregister.cbhc.barcode.BarcodeIntentResult;
import org.smartregister.cbhc.contract.ProfileContract;
import org.smartregister.cbhc.contract.RegisterContract;
import org.smartregister.cbhc.domain.AttentionFlag;
import org.smartregister.cbhc.fragment.ProfileContactsFragment;
import org.smartregister.cbhc.fragment.ProfileOverviewFragment;
import org.smartregister.cbhc.fragment.ProfileTasksFragment;
import org.smartregister.cbhc.fragment.QuickCheckFragment;
import org.smartregister.cbhc.helper.ImageRenderHelper;
import org.smartregister.cbhc.presenter.ProfilePresenter;
import org.smartregister.cbhc.presenter.RegisterPresenter;
import org.smartregister.cbhc.task.FetchProfileDataTask;
import org.smartregister.cbhc.util.Constants;
import org.smartregister.cbhc.util.DBConstants;
import org.smartregister.cbhc.util.ImageLoaderByGlide;
import org.smartregister.cbhc.util.JsonFormUtils;
import org.smartregister.cbhc.util.Utils;
import org.smartregister.cbhc.view.CopyToClipboardDialog;
import org.smartregister.commonregistry.CommonPersonObject;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.domain.FetchStatus;
import org.smartregister.domain.ProfileImage;
import org.smartregister.repository.DetailsRepository;
import org.smartregister.repository.ImageRepository;
import org.smartregister.util.DateUtil;
import org.smartregister.util.FileUtilities;
import org.smartregister.util.OpenSRPImageLoader;
import org.smartregister.util.PermissionUtils;
import org.smartregister.view.activity.DrishtiApplication;

import java.io.File;
import java.io.Serializable;
import java.util.List;

import static org.smartregister.cbhc.fragment.ProfileOverviewFragment.EXTRA_HOUSEHOLD_DETAILS;
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
       // DrishtiApplication.getCachedImageLoaderInstance().getImageByClientId(householdDetails.entityId(), OpenSRPImageLoader.getStaticImageListener((ImageView)findViewById(R.id.imageview_profile), 0, 0));
        setProfileAge(durationString);
        setProfileID(getValue(householdDetails.getColumnmaps(),"Patient_Identifier",true));
        gestationAgeView.setVisibility(View.GONE);
    }

    public void refreshProfileViews(){
        householdDetails = CommonPersonObjectToClient(AncApplication.getInstance().getContext().commonrepository(DBConstants.HOUSEHOLD_TABLE_NAME).findByBaseEntityId(householdDetails.entityId()));

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
                String formMetadata = JsonFormUtils.getHouseholdJsonEditFormString(this, householdDetails.getColumnmaps());
                try {
                    JsonFormUtils.startFormForEdit(this, JsonFormUtils.REQUEST_CODE_GET_JSON, formMetadata);
                } catch (Exception e) {

                }
                break;
            case R.id.edit_member:
                CommonPersonObjectClient pclient  = (CommonPersonObjectClient) view.getTag();
                pclient.getColumnmaps().put("relational_id",householdDetails.getCaseId());
                String formMetadataformembers = JsonFormUtils.getMemberJsonEditFormString(this, pclient.getColumnmaps());
                try {
                    JsonFormUtils.startFormForEdit(this, JsonFormUtils.REQUEST_CODE_GET_JSON, formMetadataformembers);
                } catch (Exception e) {

                }
                break;
            case R.id.profile_image_iv:
                CommonPersonObjectClient memberclient  = (CommonPersonObjectClient) view.getTag(R.id.clientformemberprofile);
                memberclient.getColumnmaps().put("relational_id",householdDetails.getCaseId());
                String clienttype = (String)view.getTag(R.id.typeofclientformemberprofile);
                Intent intent = new Intent(this, MemberProfileActivity.class);
                intent.putExtra(Constants.INTENT_KEY.BASE_ENTITY_ID, memberclient.getCaseId());
                intent.putExtra(ProfileOverviewFragment.EXTRA_HOUSEHOLD_DETAILS,memberclient);
                intent.putExtra("type_of_member",clienttype);
                startActivity(intent);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
          super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == JsonFormUtils.REQUEST_CODE_GET_JSON && resultCode == RESULT_OK) {
            try {
                String jsonString = data.getStringExtra("json");
                Log.d("JSONResult", jsonString);

                JSONObject form = new JSONObject(jsonString);
                if (form.getString(JsonFormUtils.ENCOUNTER_TYPE).equals(Constants.EventType.REGISTRATION)) {
                    presenter.saveForm(jsonString, false);
                }else if (form.getString(JsonFormUtils.ENCOUNTER_TYPE).equals(Constants.EventType.HouseholdREGISTRATION)) {
                    presenter.saveForm(jsonString, false);
                }else if (form.getString(JsonFormUtils.ENCOUNTER_TYPE).equals(Constants.EventType.UPDATE_Household_REGISTRATION)) {
                    presenter.saveForm(jsonString, true);
                }else if (form.getString(JsonFormUtils.ENCOUNTER_TYPE).equals(Constants.EventType.MemberREGISTRATION)) {
                    presenter.saveForm(jsonString, false);
                } else if (form.getString(JsonFormUtils.ENCOUNTER_TYPE).equals(Constants.EventType.CLOSE)) {
                    presenter.closeAncRecord(jsonString);
                }
            } catch (Exception e) {
                Log.e(TAG, Log.getStackTraceString(e));
            }

        } else if (requestCode == BarcodeIntentIntegrator.REQUEST_CODE && resultCode == RESULT_OK) {
//            BarcodeIntentResult res = BarcodeIntentIntegrator.parseActivityResult(requestCode, resultCode, data);
//            if (StringUtils.isNotBlank(res.getContents())) {
//                Log.d("Scanned QR Code", res.getContents());
//                mBaseFragment.onQRCodeSucessfullyScanned(res.getContents());
//                mBaseFragment.setSearchTerm(res.getContents());
//            } else
//                Log.i("", "NO RESULT FOR QR CODE");
        }
    }

    private ViewPager setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());

        profileOverviewFragment = ProfileOverviewFragment.newInstance(this.getIntent().getExtras());

        ProfileContactsFragment profileContactsFragment = ProfileContactsFragment.newInstance(this.getIntent().getExtras());
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
            arrayAdapter.add("খানা স্থানান্তর");
            arrayAdapter.add("জি আর এ খানা পাওয়া যায়নি");
//            arrayAdapter.add(getString(R.string.call));
//            arrayAdapter.add(getString(R.string.start_contact));
//            arrayAdapter.add(getString(R.string.close_anc_record));

            builderSingle.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String textClicked = arrayAdapter.getItem(which);
                    switch (textClicked) {
                        case "[+] নতুন সদস্য":
                            try{


                                presenter.startMemberRegistrationForm(Constants.JSON_FORM.MEMBER_REGISTER,null,null,null,householdDetails.entityId());
                            }catch(Exception e){
                                e.printStackTrace();
                            }

//                            Intent intent = new Intent(ProfileActivity.this, AncJsonFormActivity.class);
//                            intent.putExtra("json", JsonFormUtils.);
//                            startActivityForResult(intent, JsonFormUtils.REQUEST_CODE_GET_JSON);
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
        String baseEntityId = getIntent().getStringExtra(Constants.INTENT_KEY.BASE_ENTITY_ID);
        mProfilePresenter.refreshProfileView(baseEntityId);
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

    }

    @Override
    public void showLanguageDialog(List<String> displayValues) {

    }

    @Override
    public void startFormActivity(JSONObject form) {
        try{
            Intent intent = new Intent(this, AncJsonFormActivity.class);
            intent.putExtra("json", form.toString());
            startActivityForResult(intent, JsonFormUtils.REQUEST_CODE_GET_JSON);
        }catch(Exception e){

        }

    }

    @Override
    public void refreshList(FetchStatus fetchStatus) {

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
}

