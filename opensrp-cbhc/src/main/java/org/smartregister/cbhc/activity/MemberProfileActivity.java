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
import org.smartregister.cbhc.R;
import org.smartregister.cbhc.adapter.ViewPagerAdapter;
import org.smartregister.cbhc.application.AncApplication;
import org.smartregister.cbhc.contract.ProfileContract;
import org.smartregister.cbhc.fragment.MemberProfileContactsFragment;
import org.smartregister.cbhc.fragment.ProfileContactsFragment;
import org.smartregister.cbhc.fragment.ProfileOverviewFragment;
import org.smartregister.cbhc.fragment.ProfileTasksFragment;
import org.smartregister.cbhc.fragment.QuickCheckFragment;
import org.smartregister.cbhc.helper.ImageRenderHelper;
import org.smartregister.cbhc.presenter.ProfilePresenter;
import org.smartregister.cbhc.util.Constants;
import org.smartregister.cbhc.util.DBConstants;
import org.smartregister.cbhc.util.JsonFormUtils;
import org.smartregister.cbhc.util.Utils;
import org.smartregister.cbhc.view.CopyToClipboardDialog;
import org.smartregister.commonregistry.CommonPersonObject;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.util.DateUtil;
import org.smartregister.util.PermissionUtils;

import java.io.Serializable;

import static org.smartregister.cbhc.fragment.ProfileOverviewFragment.EXTRA_HOUSEHOLD_DETAILS;
import static org.smartregister.util.Utils.getName;
import static org.smartregister.util.Utils.getValue;

/**
 * Created by ndegwamartin on 10/07/2018.
 */
public class MemberProfileActivity extends BaseProfileActivity implements ProfileContract.View {

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
//        mProfilePresenter.setProfileActivity(this);

        imageRenderHelper = new ImageRenderHelper(this);


    }

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


    private ViewPager setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());

        ProfileOverviewFragment profileOverviewFragment = ProfileOverviewFragment.newInstance(this.getIntent().getExtras());
        MemberProfileContactsFragment profileContactsFragment = MemberProfileContactsFragment.newInstance(this.getIntent().getExtras());
        ProfileTasksFragment profileTasksFragment = ProfileTasksFragment.newInstance(this.getIntent().getExtras());

//        adapter.addFragment(profileOverviewFragment, this.getString(R.string.members));
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
            arrayAdapter.add(getString(R.string.call));
            arrayAdapter.add(getString(R.string.start_follow_up));
//            arrayAdapter.add(getString(R.string.close_anc_record));

            builderSingle.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String textClicked = arrayAdapter.getItem(which);
                    switch (textClicked) {
                        case "Call":
                            launchPhoneDialer(womanPhoneNumber);
                            break;
                        case "Follow Up":
                            getIntent().putExtra(Constants.INTENT_KEY.BASE_ENTITY_ID,householdDetails.getCaseId());
//                            (Constants.INTENT_KEY.BASE_ENTITY_ID)
                            JsonFormUtils.launchFollowUpForm(MemberProfileActivity.this);
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
}

