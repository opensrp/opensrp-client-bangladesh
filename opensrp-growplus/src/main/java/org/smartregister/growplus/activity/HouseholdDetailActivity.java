package org.smartregister.growplus.activity;

import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.smartregister.commonregistry.CommonPersonObject;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.commonregistry.CommonRepository;
import org.smartregister.cursoradapter.SmartRegisterQueryBuilder;
import org.smartregister.growplus.R;
import org.smartregister.growplus.application.VaccinatorApplication;
import org.smartregister.growplus.fragment.HouseholdMemberAddFragment;
import org.smartregister.growplus.repository.PathRepository;
import org.smartregister.growplus.toolbar.LocationSwitcherToolbar;
import org.smartregister.repository.AllSharedPreferences;
import org.smartregister.repository.DetailsRepository;
import org.smartregister.util.DateUtil;
import org.smartregister.util.OpenSRPImageLoader;
import org.smartregister.util.Utils;
import org.smartregister.view.activity.DrishtiApplication;
import org.joda.time.DateTime;
import org.opensrp.api.constants.Gender;
import org.smartregister.view.customcontrols.CustomFontTextView;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;


import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import util.ImageUtils;
import util.JsonFormUtils;
import util.PathConstants;
import static org.smartregister.util.Utils.getValue;


/**
 * Created by habib on 25/07/17.
 */
public class HouseholdDetailActivity extends BaseActivity {
    public static boolean isLaunched = false;
    ListView householdList;
    private LocationSwitcherToolbar toolbar;
    public org.smartregister.Context context;

    private static final int REQUEST_CODE_GET_JSON = 3432;
    private CommonPersonObjectClient householdDetails;
    private static final String EXTRA_HOUSEHOLD_DETAILS = "household_details";


    static final int REQUEST_TAKE_PHOTO = 1;
    public static Gender gender;
    private File currentfile;
    private HouseholdMemberAddFragment addmemberFragment;
    boolean isMotherExist=false;
    String entityid="";
    @Bind(R.id.age_tv)
    TextView textViewAge;
    @Bind(R.id.houseHoldAddress)
    TextView textViewAddress;
    @Bind(R.id.name_tv)
    TextView textViewName;
    @Bind(R.id.child_id_tv)
    TextView textViewID;
    @Bind(R.id.profile_image_iv)
    ImageView profileImageView;
    @Bind(R.id.name_inits)
    TextView textViewNameInit;
    @Bind(R.id.drawer_layout)
    DrawerLayout drawerLayout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getContentView());
        isLaunched = true;
        ButterKnife.bind(this);
        toolbar = (LocationSwitcherToolbar) getToolbar();
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HouseholdDetailActivity.this, ChildSmartRegisterActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            }
        });

        Bundle extras = this.getIntent().getExtras();
        if (extras != null) {
            Serializable serializable = extras.getSerializable(EXTRA_HOUSEHOLD_DETAILS);
            if (serializable != null && serializable instanceof CommonPersonObjectClient) {
                householdDetails = (CommonPersonObjectClient) serializable;
            }
        }
        final ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.setDrawerListener(toggle);
        toggle.syncState();
        int toolbarResource = R.drawable.vertical_separator_male;
        toolbar.updateSeparatorView(toolbarResource);
        toolbar.init(this);
        setSupportActionBar(toolbar);
        toolbar.setTitle("Household Details");


        textViewName.setText(householdDetails.getDetails().get("first_name"));
        textViewID.setText(getString(R.string.hhid_format,householdDetails.getDetails().get("HHID")));
        String dobString = Utils.getValue(householdDetails.getDetails(), "dob", false);
        String durationString = "";
        if (!TextUtils.isEmpty(dobString)) {
            try {
                DateTime birthDateTime = new DateTime(dobString);
                durationString = DateUtil.getDuration(birthDateTime);
                textViewAge.setText(getString(R.string.age_format, durationString));
            } catch (Exception e) {
                Log.e(getClass().getName(), e.toString(), e);
            }
        }else{
            textViewAge.setVisibility(View.GONE);
        }
        String address=householdDetails.getDetails().get("address4");
        if(!TextUtils.isEmpty(address)){
            textViewAddress.setText(getString(R.string.address_format,address));
        }else{
            textViewAddress.setVisibility(View.GONE);
        }

        entityid  = householdDetails.getDetails().get("_id");
        if(entityid!=null) {
           //ImageLoaderByGlide.setImageAsTarget(FileUtilities.getImageUrl(entityid),profileImageView,R.drawable.houshold_register_placeholder);
            profileImageView.setTag(org.smartregister.R.id.entity_id, entityid);
           DrishtiApplication.getCachedImageLoaderInstance().getImageByClientId(entityid, OpenSRPImageLoader.getStaticImageListener((ImageView) profileImageView, R.drawable.houshold_register_placeholder, R.drawable.houshold_register_placeholder));

        }

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        AllSharedPreferences allSharedPreferences = org.smartregister.Context.getInstance().allSharedPreferences();
        String preferredName = allSharedPreferences.getANMPreferredName(allSharedPreferences.fetchRegisteredANM());
        if (!preferredName.isEmpty()) {
            String[] preferredNameArray = preferredName.split(" ");
            String initials = "";
            if (preferredNameArray.length > 1) {
                initials = String.valueOf(preferredNameArray[0].charAt(0)) + String.valueOf(preferredNameArray[1].charAt(0));
            } else if (preferredNameArray.length == 1) {
                initials = String.valueOf(preferredNameArray[0].charAt(0));
            }
            textViewNameInit.setText(initials);
        }

//        toolbar.setOnLocationChangeListener(this);
//

        initQueries();


        context = org.smartregister.Context.getInstance().updateApplicationContext(this.getApplicationContext());
        //get Household members repository
    }
    @OnClick({R.id.profile_image_iv,R.id.name_inits,R.id.add_household_img})
    void onClickView(View view){
        switch (view.getId()){
            case R.id.profile_image_iv:
                dispatchTakePictureIntent();
            break;
            case R.id.name_inits:
                if (!drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.openDrawer(GravityCompat.START);
                }
                break;
            case R.id.add_household_img:
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                android.app.Fragment prev = getFragmentManager().findFragmentByTag(HouseholdMemberAddFragment.DIALOG_TAG);
                if (prev != null) {
                    ft.remove(prev);
                }
                ft.addToBackStack(null);
                String locationid = "";
                DetailsRepository detailsRepository;
                detailsRepository = org.smartregister.Context.getInstance().detailsRepository();
                Map<String, String> details = detailsRepository.getAllDetailsForClient(entityid);
                try {
                    locationid = JsonFormUtils.getOpenMrsLocationId(context(),getValue(details, "address4", false) );
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                addmemberFragment= HouseholdMemberAddFragment.newInstance(HouseholdDetailActivity.this,locationid,entityid,context,isMotherExist);
                addmemberFragment.show(ft, HouseholdMemberAddFragment.DIALOG_TAG);
                break;
        }
    }
    protected org.smartregister.Context context() {
        return VaccinatorApplication.getInstance().context();
    }
    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File

            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                currentfile = photoFile;
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                        Uri.fromFile(photoFile));
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
//        mCurrentPhotoPath = "file:" + image.getAbsolutePath();
        return image;
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        refreshadapter();
        if (requestCode == REQUEST_CODE_GET_JSON) {
            if (resultCode == RESULT_OK) {
                String jsonString = data.getStringExtra("json");
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
                AllSharedPreferences allSharedPreferences = new AllSharedPreferences(preferences);
                JsonFormUtils.saveForm(this, context(), jsonString, allSharedPreferences.fetchRegisteredANM());

            }
        }
//        if (requestCode == REQUEST_CODE_GET_JSON) {
//            if (resultCode == RESULT_OK) {
//                try {
//                    String jsonString = data.getStringExtra("json");
//                    Log.d("JSONResult", jsonString);
//
//                    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
//                    AllSharedPreferences allSharedPreferences = new AllSharedPreferences(preferences);
//
//                    JSONObject form = new JSONObject(jsonString);
//                    if (form.getString("encounter_type").equals("Death")) {
//                        confirmReportDeceased(jsonString, allSharedPreferences);
//                    } else if (form.getString("encounter_type").equals("Birth Registration")) {
//                        JsonFormUtils.editsave(this, getOpenSRPContext(), jsonString, allSharedPreferences.fetchRegisteredANM(), "Child_Photo", "child", "mother");
//                    } else if (form.getString("encounter_type").equals("AEFI")) {
//                        JsonFormUtils.saveAdverseEvent(jsonString, location_name,
//                                childDetails.entityId(), allSharedPreferences.fetchRegisteredANM());
//                    }
//                    childDataFragment.childDetails = childDetails;
//                    childDataFragment.loadData();
//                } catch (Exception e) {
//                    Log.e(TAG, e.getMessage());
//                }
//            }
//        } else
        else if (requestCode == REQUEST_TAKE_PHOTO) {
            if (resultCode == RESULT_OK) {
                String imageLocation = currentfile.getAbsolutePath();
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
                AllSharedPreferences allSharedPreferences = new AllSharedPreferences(preferences);
                String entityid = householdDetails.getDetails().get("_id");
                JsonFormUtils.saveImage(this, allSharedPreferences.fetchRegisteredANM(), entityid, imageLocation);
                updateProfilePicture(entityid);
            }
        }
    }

    private void updateProfilePicture(String entityid) {
        if(entityid!=null) {
            //ImageLoaderByGlide.setImageAsTarget(FileUtilities.getImageUrl(entityid),profileImageView,R.drawable.houshold_register_placeholder);

           // ImageView profileImageIV = (ImageView)findViewById(R.id.profile_image_iv);
            profileImageView.setTag(org.smartregister.R.id.entity_id, entityid);
            DrishtiApplication.getCachedImageLoaderInstance().getImageByClientId(entityid, OpenSRPImageLoader.getStaticImageListener((ImageView) profileImageView, R.drawable.houshold_register_placeholder, R.drawable.houshold_register_placeholder));

        }
    }

    private void initQueries(){

    }

    private void refreshadapter() {
        //setAdapter data of Household member
        PathRepository repo = (PathRepository) VaccinatorApplication.getInstance().getRepository();
        net.sqlcipher.database.SQLiteDatabase db = repo.getReadableDatabase();
        String mother_id = householdDetails.getDetails().get("_id");

        String tableName = PathConstants.MOTHER_TABLE_NAME;
        SmartRegisterQueryBuilder queryBUilder = new SmartRegisterQueryBuilder();
        queryBUilder.SelectInitiateMainTable(tableName, new String[]{
                tableName + ".relationalid",
                tableName + ".details",
                tableName + ".openmrs_id",
                tableName + ".relational_id",
                tableName + ".first_name",
                tableName + ".last_name",
                tableName + ".gender",
                tableName + ".father_name",
                tableName + ".dob",
                tableName + ".epi_card_number",
                tableName + ".contact_phone_number",
                tableName + ".client_reg_date",
                tableName + ".last_interacted_with"
        });

        Cursor cursor = db.rawQuery(queryBUilder.mainCondition("relational_id = ?"),new String[]{mother_id});
        if(cursor!=null && cursor.getCount()>0){
            isMotherExist=true;
        }
        if(addmemberFragment!=null)addmemberFragment.updateIsMotherExit(isMotherExist);

        householdList = (ListView) findViewById(R.id.household_list);

        HouseholdCursorAdpater cursorAdpater = new HouseholdCursorAdpater(getApplicationContext(),cursor);

        householdList.setAdapter(cursorAdpater);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isLaunched = false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshadapter();
        final DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        LinearLayout stockregister = (LinearLayout) drawer.findViewById(R.id.stockcontrol);
        ((CustomFontTextView)findViewById(R.id.title)).setText("Household Details");
        stockregister.setBackgroundColor(getResources().getColor(R.color.tintcolor));
    }

    @Override
    protected int getContentView() {
        return  R.layout.household_detail_activity;
    }
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        return true;
    }

    @Override
    protected int getDrawerLayoutId() {
        return  R.id.drawer_layout;
    }

    @Override
    protected int getToolbarId() {
        return LocationSwitcherToolbar.TOOLBAR_ID;
    }

    @Override
    protected Class onBackActivity() {
        return ChildSmartRegisterActivity.class;
    }

    class HouseholdCursorAdpater extends CursorAdapter {
        private Context context;
        private LayoutInflater inflater = null;

        public HouseholdCursorAdpater(Context context, Cursor c) {
            super(context, c);
            inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        }

        @Override
        public void bindView(View view, final Context context, Cursor cursor) {
            Log.e("------------","bind view call");
            CommonRepository commonRepository = org.smartregister.Context.getInstance().commonrepository(PathConstants.MOTHER_TABLE_NAME);
            CommonPersonObject personinlist = commonRepository.readAllcommonforCursorAdapter(cursor);
            final CommonPersonObjectClient pClient = new CommonPersonObjectClient(personinlist.getCaseId(), personinlist.getDetails(), personinlist.getDetails().get("FWHOHFNAME"));
            pClient.setColumnmaps(personinlist.getColumnmaps());
            DetailsRepository detailsRepository= org.smartregister.Context.getInstance().detailsRepository();
            Map<String, String> details = detailsRepository.getAllDetailsForClient(pClient.entityId());
            String uniqId=details.get("idtype");
            if(!uniqId.equalsIgnoreCase("NONE")){
                uniqId=uniqId+" : " +details.get("nationalId");
                textViewUniqueId.setVisibility(View.VISIBLE);
                textViewUniqueId.setText(getString(R.string.unique_id_format,uniqId));
            }else{
                textViewUniqueId.setVisibility(View.GONE);
            }
            textViewName.setText(getString(R.string.name_format, cursor.getString(cursor.getColumnIndex("first_name"))));
            String dobString = cursor.getString(cursor.getColumnIndex("dob"));
            if (StringUtils.isNotBlank(dobString)) {
                try {
                    DateTime birthDateTime = new DateTime(dobString);
                    String duration = DateUtil.getDuration(birthDateTime);
                    textViewAge.setVisibility(View.VISIBLE);
                    textViewAge.setText(getString(R.string.age_format,duration));
                } catch (Exception e) {
                    Log.e(getClass().getName(), e.toString(), e);
                }
            }else{
                textViewAge.setVisibility(View.GONE);
            }

            ((LinearLayout)view.findViewById(R.id.profile_name_layout)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    WomanImmunizationActivity.launchActivity(HouseholdDetailActivity.this,pClient,null);
                }
            });

            if (pClient.entityId() != null) {//image already in local storage most likey ):
                //set profile image by passing the client id.If the image doesn't exist in the image repository then download and save locally
                imageViewProfile.setTag(org.smartregister.R.id.entity_id, pClient.entityId());
                DrishtiApplication.getCachedImageLoaderInstance().getImageByClientId(pClient.entityId(), OpenSRPImageLoader.getStaticImageListener((ImageView) profileImageView, R.drawable.woman_path_register_logo, R.drawable.woman_path_register_logo));

            }
            LinearLayout child_added = (LinearLayout) view.findViewById(R.id.children_added);
            child_added.removeAllViews();
            addChild(child_added,pClient.entityId());
        }
        TextView textViewName,textViewAge,textViewUniqueId;
        ImageView imageViewProfile;

        TextView textViewNameChild,textViewageChild,textViewChildBRID,textViewGender;
        ImageView imageViewProfileChild;
        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            Log.e("------------","new view call");
            View view = inflater.inflate(R.layout.household_details_list_row,parent,false);
            textViewName=(TextView)view.findViewById(R.id.name_tv);
            textViewAge= (TextView) view.findViewById(R.id.age_tv);
            imageViewProfile= (ImageView)view.findViewById(R.id.profile_image_iv);
            textViewUniqueId=(TextView)view.findViewById(R.id.unique_id_tv);
            return  view;
        }

        public void addChild(LinearLayout household_details_list_row, String mother_id){
            Log.e("--------------",mother_id);


            PathRepository repo = (PathRepository) VaccinatorApplication.getInstance().getRepository();
            net.sqlcipher.database.SQLiteDatabase db = repo.getReadableDatabase();


            String tableName = PathConstants.CHILD_TABLE_NAME;
            String parentTableName = PathConstants.MOTHER_TABLE_NAME;
            SmartRegisterQueryBuilder queryBUilder = new SmartRegisterQueryBuilder();
            queryBUilder.SelectInitiateMainTable(tableName, new String[]{
                    tableName + ".relationalid",
                    tableName + ".details",
                    tableName + ".openmrs_id",
                    tableName + ".relational_id",
                    tableName + ".first_name",
                    tableName + ".last_name",
                    tableName + ".gender","ec_details.value",
                    parentTableName + ".first_name as mother_first_name",
                    parentTableName + ".last_name as mother_last_name",
                    parentTableName + ".dob as mother_dob",
                    parentTableName + ".nrc_number as mother_nrc_number",
                    tableName + ".father_name",
                    tableName + ".dob",
                    tableName + ".epi_card_number",
                    tableName + ".contact_phone_number",
                    tableName + ".pmtct_status",
                    tableName + ".provider_uc",
                    tableName + ".provider_town",
                    tableName + ".provider_id",
                    tableName + ".provider_location_id",
                    tableName + ".client_reg_date",
                    tableName + ".last_interacted_with",
                    tableName + ".inactive",
                    tableName + ".lost_to_follow_up"
            });
            queryBUilder.customJoin("LEFT JOIN " + parentTableName + " ON  " + tableName + ".relational_id =  " + parentTableName + ".id");
            queryBUilder.customJoin("LEFT JOIN ec_details ON "+tableName+".base_entity_id = "+" ec_details.base_entity_id and ec_details.key='Child_Birth_Certificate'");
            String mainCondition = " (dod is NULL OR dod = '' )";
            String mainSelect = queryBUilder.mainCondition(mainCondition);

            Cursor cursor = db.rawQuery(mainSelect+ "and "+tableName+".relational_id = ?",new String[]{mother_id});

            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                CommonRepository commonRepository = org.smartregister.Context.getInstance().commonrepository(tableName);
                CommonPersonObject personinlist = commonRepository.readAllcommonforCursorAdapter(cursor);
                final CommonPersonObjectClient pClient = new CommonPersonObjectClient(personinlist.getCaseId(), personinlist.getDetails(), personinlist.getDetails().get("FWHOHFNAME"));
                pClient.setColumnmaps(personinlist.getColumnmaps());
                LinearLayout childLayout= (LinearLayout)inflater.inflate(R.layout.household_details_child_row,household_details_list_row,false);
                    textViewNameChild=(TextView)childLayout.findViewById(R.id.name_tv);
                    textViewageChild= (TextView) childLayout.findViewById(R.id.age_tv);
                    imageViewProfileChild= (ImageView)childLayout.findViewById(R.id.profile_image_iv);
                    textViewChildBRID=(TextView)childLayout.findViewById(R.id.unique_id_tv);
                    textViewGender=(TextView)childLayout.findViewById(R.id.gender_tv);
                    imageViewProfileChild= (ImageView)childLayout.findViewById(R.id.profile_image_iv);
                    childLayout.findViewById(R.id.profile_name_layout).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            ChildImmunizationActivity.launchActivity(HouseholdDetailActivity.this,pClient,null);
                        }
                    });

                textViewNameChild.setText(getString(R.string.name_format, cursor.getString(cursor.getColumnIndex("first_name"))));

                String dobString = cursor.getString(cursor.getColumnIndex("dob"));
                String brid=cursor.getString(cursor.getColumnIndex("value"));
                if(!TextUtils.isEmpty(brid)){
                    brid="BRID:"+brid;
                    textViewChildBRID.setVisibility(View.VISIBLE);
                    textViewChildBRID.setText(getString(R.string.unique_id_format,brid));
                }else{
                    textViewChildBRID.setVisibility(View.GONE);
                }
                if (!TextUtils.isEmpty(dobString)) {
                    try {
                        DateTime birthDateTime = new DateTime(dobString);
                        String durationString = DateUtil.getDuration(birthDateTime);
                        textViewageChild.setVisibility(View.VISIBLE);
                        textViewageChild.setText(getString(R.string.age_format,durationString));
                    } catch (Exception e) {
                        Log.e(getClass().getName(), e.toString(), e);
                    }
                }else{
                    textViewageChild.setVisibility(View.GONE);
                }
                Gender gender = Gender.UNKNOWN;
                if (pClient.getDetails() != null) {
                    String genderString = Utils.getValue(pClient, "gender", false);
                    if (genderString != null && genderString.toLowerCase().equals("female")) {
                        gender = Gender.FEMALE;
                    } else if (genderString != null && genderString.toLowerCase().equals("male")) {
                        gender = Gender.MALE;
                    }
                    textViewGender.setText(getString(R.string.gender_format,gender.toString()));

                    if (pClient.entityId() != null) {//image already in local storage most likey ):
                        //set profile image by passing the client id.If the image doesn't exist in the image repository then download and save locally
                        imageViewProfileChild.setTag(org.smartregister.R.id.entity_id, pClient.entityId());
                        DrishtiApplication.getCachedImageLoaderInstance().getImageByClientId(pClient.entityId(), OpenSRPImageLoader.getStaticImageListener((ImageView) imageViewProfileChild, ImageUtils.profileImageResourceByGender(gender), ImageUtils.profileImageResourceByGender(gender)));

                    }
                }

                household_details_list_row.addView(childLayout);

                cursor.moveToNext();
            }
            cursor.close();

        }

    }
}
