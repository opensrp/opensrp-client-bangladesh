package org.smartregister.cbhc.fragment;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.CursorAdapter;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;

import net.sqlcipher.database.SQLiteDatabase;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.opensrp.api.constants.Gender;
import org.smartregister.CoreLibrary;
import org.smartregister.cbhc.R;
import org.smartregister.cbhc.activity.ProfileActivity;
import org.smartregister.cbhc.application.AncApplication;
import org.smartregister.cbhc.repository.AncRepository;
import org.smartregister.cbhc.util.Constants;
import org.smartregister.cbhc.util.DBConstants;
import org.smartregister.cbhc.util.ImageLoaderByGlide;
import org.smartregister.cbhc.util.ImageUtils;
import org.smartregister.cbhc.util.JsonFormUtils;
import org.smartregister.cbhc.util.Utils;
import org.smartregister.commonregistry.CommonPersonObject;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.commonregistry.CommonRepository;
import org.smartregister.cursoradapter.SmartRegisterQueryBuilder;
import org.smartregister.domain.ProfileImage;
import org.smartregister.repository.DetailsRepository;
import org.smartregister.repository.ImageRepository;
import org.smartregister.util.DateUtil;
import org.smartregister.util.FileUtilities;
import org.smartregister.util.OpenSRPImageListener;
import org.smartregister.util.OpenSRPImageLoader;
import org.smartregister.view.activity.DrishtiApplication;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.smartregister.util.Utils.getName;
import static org.smartregister.util.Utils.getValue;

/**
 * Created by ndegwamartin on 12/07/2018.
 */
public class ProfileOverviewFragment extends BaseProfileFragment {

    private CommonPersonObjectClient householdDetails;
    private ListView householdList;
    public static final String EXTRA_HOUSEHOLD_DETAILS = "household_details";
    public View fragmentView;
    HashMap<String,Drawable>profile_photo = new HashMap<String,Drawable>();
    public View getFragmentView() {
        return fragmentView;
    }
    private Handler myHandler;
    private Activity mActivity;

    public static ProfileOverviewFragment newInstance(Bundle bundle) {
        Bundle args = bundle;
        ProfileOverviewFragment fragment = new ProfileOverviewFragment();
        if (args == null) {
            args = new Bundle();
        }
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        myHandler = new Handler();
        Bundle extras = this.getActivity().getIntent().getExtras();
        if (extras != null) {
            Serializable serializable = extras.getSerializable(EXTRA_HOUSEHOLD_DETAILS);
            if (serializable != null && serializable instanceof CommonPersonObjectClient) {
                householdDetails = (CommonPersonObjectClient) serializable;
            }
        }
    }

    @Override
    protected void onCreation() {
        //Overriden
    }

    @Override
    protected void onResumption() {
        //Overriden
//        if(fragmentView!=null){
//            myHandler.postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                    refreshadapter();
//                }
//            },1000);
//
//        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if(cursor!=null)
            cursor.close();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mActivity = (Activity)context;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mActivity = null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        fragmentView = inflater.inflate(R.layout.fragment_profile_overview, container, false);
        refreshadapter();
        return fragmentView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        refreshadapter();
    }
    Cursor cursor = null;
    public void refreshadapter() {
        if(fragmentView==null) return;
        (new AsyncTask(){
            ProgressDialog dialog;
            @Override
            protected void onPreExecute() {
                super.onPreExecute();

                dialog = ProgressDialog.show(ProfileOverviewFragment.this.getActivity(),"processing","please wait");
                setRelationMap();
            }

            @Override
            protected Object doInBackground(Object[] objects) {
                //householdDetails.getColumnmaps().putAll(AncApplication.getInstance().getContext().detailsRepository().getAllDetailsForClient(householdDetails.entityId()));
                AncRepository repo = (AncRepository) AncApplication.getInstance().getRepository();
                SQLiteDatabase db = repo.getReadableDatabase();
                String mother_id = householdDetails.getDetails().get("_id");

                String tableName = DBConstants.WOMAN_TABLE_NAME;
                String childtableName = DBConstants.CHILD_TABLE_NAME;
                String membertablename = DBConstants.MEMBER_TABLE_NAME;

//                SmartRegisterQueryBuilder queryBUilder = new SmartRegisterQueryBuilder();
//                queryBUilder.SelectInitiateMainTable(tableName, new String[]{
//                        tableName + ".relationalid",
//                        tableName + ".details",
//                        tableName + ".first_name",
//                        tableName + "." + DBConstants.KEY.LAST_NAME,
//                        tableName + ".dob"
//                });
//                String currentquery = queryBUilder.mainCondition("relational_id = '"+mother_id+"'").concat(" Union all ");
//                SmartRegisterQueryBuilder queryBUilder2 = new SmartRegisterQueryBuilder();
//                queryBUilder2.SelectInitiateMainTable(membertablename, new String[]{
//                        membertablename + ".relationalid",
//                        membertablename + ".details",
//                        membertablename + ".first_name",
//                        membertablename + "." + DBConstants.KEY.LAST_NAME,
//                        membertablename + ".dob"
//                });
//                currentquery = currentquery.concat(queryBUilder2.mainCondition("relational_id = '"+mother_id+"'").concat(" Union all "));
//                queryBUilder.SelectInitiateMainTable(childtableName, new String[]{
//                        childtableName + ".relationalid",
//                        childtableName + ".details",
//                        childtableName + ".first_name",
//                        childtableName + "." + DBConstants.KEY.LAST_NAME,
//                        childtableName + ".dob"
//                });

//        cursor = db.rawQuery(currentquery.concat(queryBUilder.mainCondition("relational_id = ?")),new String[]{mother_id});
                String rawQuery = queryfortheadapterthing(mother_id);
//                if(cursor!=null&&!cursor.isClosed()){
//                    cursor.close();
//                }

                try{
                    cursor = db.rawQuery(rawQuery,new String[]{});
                }catch(Exception e){

                }

                return cursor;

            }

            @Override
            protected void onPostExecute(Object o) {
                super.onPostExecute(o);
                if(mActivity == null) return;
                if(dialog!=null && dialog.isShowing())dialog.dismiss();
                householdList = (ListView)fragmentView.findViewById(R.id.household_list);
                profile_photo.clear();
                if(o!=null && o instanceof Cursor){
                    cursor = (Cursor)o;
                    HouseholdCursorAdpater cursorAdpater = new HouseholdCursorAdpater(getContext(),cursor);

                    householdList.setAdapter(cursorAdpater);
                }

            }
        }).execute();
        //setAdapter data of Household member

    }

    public String queryfortheadapterthing(String id) {
        String query = "SELECT * FROM "  +
                "        (select woman.id as _id , woman.relationalid , woman.Patient_Identifier, woman.details , woman.first_name , woman.last_name , woman.dob , woman.gender, woman.PregnancyStatus, woman.tasks, details.value as relation " +
                "FROM ec_woman as woman left join ec_details as details on (details.base_entity_id = woman.id and details.key = 'Realtion_With_Household_Head') " +
                "WHERE (woman.relational_id = '</>' and woman.date_removed IS NULL)" +
                " " +                "Union all  Select member.id as _id , member.relationalid , member.Patient_Identifier, member.details , member.first_name , member.last_name , member.dob,member.gender, member.PregnancyStatus, member.tasks, details.value as relation " +
                "FROM ec_member as member left join ec_details as details on (details.base_entity_id = member.id and details.key = 'Realtion_With_Household_Head') " +
                "WHERE (member.relational_id = '</>' and member.date_removed IS NULL)" +
                " " +
                "Union all Select child.id as _id , child.relationalid , child.Patient_Identifier, child.details , child.first_name , child.last_name , child.dob ,child.gender, child.PregnancyStatus, child.tasks, details.value as relation " +
                "FROM ec_child as child left join ec_details as details on (details.base_entity_id = child.id and details.key = 'Realtion_With_Household_Head') " +
                "WHERE (child.relational_id = '</>' and child.date_removed IS NULL)) group by _id" +
                " ORDER BY CASE WHEN relation = 'খানা প্রধান' THEN 1 " +
                " WHEN relation = 'Household_Head' THEN 1 " +
                "Else relation END ASC;";
        return query.replaceAll("</>",id);
    }

    HashMap<String,String>rmap = new HashMap<String,String>();

    class HouseholdCursorAdpater extends CursorAdapter {
        private LayoutInflater inflater = null;

        public HouseholdCursorAdpater(Context context, Cursor c) {
            super(context, c);
            inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        }

        @Override
        public void bindView(View view, final Context context, Cursor cursor) {
            Log.e("------------","bind org.smartregister.cbhc.view call");
            CommonRepository commonRepository = org.smartregister.Context.getInstance().commonrepository(DBConstants.WOMAN_TABLE_NAME);
            CommonPersonObject personinlist = commonRepository.readAllcommonforCursorAdapter(cursor);
            final CommonPersonObjectClient pClient = new CommonPersonObjectClient(personinlist.getCaseId(), personinlist.getDetails(), personinlist.getDetails().get("FWHOHFNAME"));
            pClient.setColumnmaps(personinlist.getColumnmaps());
            TextView member_name = (TextView) view.findViewById(R.id.name_tv);
            TextView relation_tv = (TextView) view.findViewById(R.id.relation_tv);
            TextView member_age = (TextView) view.findViewById(R.id.age_tv);
            ImageView pregnant_icon = (ImageView) view.findViewById(R.id.pregnant_woman_present);
            Button noOfUnregisterButton=view.findViewById(R.id.total_birth_btn);
            String relation = pClient.getColumnmaps().get("relation");

            ImageView profileImageIV = (ImageView)view.findViewById(R.id.profile_image_iv);

            String pregnant_status = pClient.getColumnmaps().get("PregnancyStatus");
            String tasks_status = pClient.getColumnmaps().get("tasks");
            String gender = pClient.getColumnmaps().get("gender");
            if(pregnant_status!=null && (pregnant_status.contains("Antenatal Period")||pregnant_status.contains("প্রসব পূর্ব"))) {
                pregnant_icon.setImageResource(R.drawable.pregnant_woman);
                pregnant_icon.setVisibility(View.VISIBLE);
            }else{
                pregnant_icon.setImageResource(R.drawable.pregnant_woman);
                pregnant_icon.setVisibility(View.INVISIBLE);
            }
            String firstName = org.smartregister.util.Utils.getValue(pClient.getColumnmaps(), DBConstants.KEY.FIRST_NAME, true);
            String lastName = org.smartregister.util.Utils.getValue(pClient.getColumnmaps(), DBConstants.KEY.LAST_NAME, true);
            if((lastName!=null&&lastName.equalsIgnoreCase("null"))||lastName==null) {
                lastName = "";
            }
            if(relation!=null) {
                if(rmap.get(relation)!=null) {
                    relation = rmap.get(relation);
                }
                relation_tv.setText(" ("+relation+")");
            }

            String patientName = getName(firstName, lastName);

//            if(profile_photo.get(pClient.entityId())==null){
//                ImageRepository imageRepo = CoreLibrary.getInstance().context().imageRepository();
//                ProfileImage imageRecord = imageRepo.findByEntityId(pClient.entityId());
//                if(imageRecord!=null){
//                    profile_photo.put(pClient.entityId(),Drawable.createFromPath(imageRecord.getFilepath()));
//                }
//                else{
//                    DrishtiApplication.getCachedImageLoaderInstance().getImageByClientId(pClient.entityId(), OpenSRPImageLoader.getStaticImageListener(profileImageIV, R.drawable.male_cbhc_placeholder, R.drawable.male_cbhc_placeholder));
//                }
//            }

            ;
//            int nameColumnIndex = cursor.getColumnIndex("first_name");
//            member_name.setText("Name : " + cursor.getString(nameColumnIndex));
//            member_name.setText(getValue(personinlist.getColumnmaps(),"first_name",true));

            member_name.setText(patientName);


//            String noOfChild=getValue(detailmap,"Live Birth",true);
//            if(!TextUtils.isEmpty(noOfChild)){
//                int child=Integer.valueOf(noOfChild) - childCount(pClient.getCaseId());
//                if(child>0){
//                    noOfUnregisterButton.setVisibility(View.VISIBLE);
//                    noOfUnregisterButton.setText(getString(R.string.total_unregister_child,child+""));
//                }
//                else {
//                    noOfUnregisterButton.setVisibility(View.GONE);
//                }
//
//
//            }else{
//                noOfUnregisterButton.setVisibility(View.GONE);
//            }
            if(tasks_status!=null&&!tasks_status.isEmpty()){
                try{
                    int tasks_count = Integer.valueOf(tasks_status);
                    if(tasks_count>0){
                        noOfUnregisterButton.setText(getString(R.string.total_unregister_child,tasks_count+""));
                        noOfUnregisterButton.setVisibility(View.VISIBLE);
                    }else{
                        noOfUnregisterButton.setVisibility(View.GONE);
                    }
                }catch(Exception e){

                }

            }else{
                noOfUnregisterButton.setVisibility(View.GONE);
            }

            //            String dobString = cursor.getString(cursor.getColumnIndex("dob"));
            String dobString = getValue(pClient.getColumnmaps(),"dob",true);
            int age = 0;

            try {
                age =   getAge((new DateTime(dobString)));
            } catch (Exception e) {
                e.printStackTrace();
            }

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
            member_age.setText("Age : "+durationString);
            ((LinearLayout)view.findViewById(R.id.profile_name_layout)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
//                    WomanImmunizationActivity.launchActivity(HouseholdDetailActivity.this,pClient,null);
                }
            });


//            new ProfilePhotoAsyncTask(pClient,age,profileImageIV,view,pregnant_icon).execute();
//
//            if (pClient.entityId() != null) {//image already in local storage most likey ):
//                //set profile image by passing the client id.If the image doesn't exist in the image org.smartregister.cbhc.repository then download and save locally
//                profileImageIV.setTag(org.smartregister.R.id.entity_id, pClient.entityId());
//                DrishtiApplication.getCachedImageLoaderInstance().getImageByClientId(pClient.entityId(), OpenSRPImageLoader.getStaticImageListener((ImageView) profileImageIV, R.drawable.woman_placeholder, R.drawable.woman_placeholder));
//
//            }
            LinearLayout editButton = (LinearLayout)view.findViewById(R.id.edit_member);
            editButton.setTag(pClient);
            editButton.setOnClickListener((ProfileActivity)getActivity());
            Drawable d = null;
            String clientype = "";
            String url = "";
            if(age<5){
                if(!TextUtils.isEmpty(gender) && gender.equalsIgnoreCase("m")){
                    if (pClient.entityId() != null) {//image already in local storage most likey ):
                        //set profile image by passing the client id.If the image doesn't exist in the image org.smartregister.cbhc.repository then download and save locally
                        profileImageIV.setTag(org.smartregister.R.id.entity_id, pClient.entityId());
//                        if(profile_photo.get(pClient.entityId())==null){
////                            d = mActivity.getResources().getDrawable(R.drawable.child_boy_infant);
////                            profile_photo.put(pClient.entityId(),d);
//                            url = FileUtilities.getImageUrl(pClient.entityId()).replaceAll("///","/");
//                            ImageLoaderByGlide.setImageAsTarget(url,profileImageIV,R.drawable.child_boy_infant);
//
//                        }else{
                            DrishtiApplication.getCachedImageLoaderInstance().getImageByClientId(pClient.entityId(), OpenSRPImageLoader.getStaticImageListener(profileImageIV, R.drawable.child_boy_infant, R.drawable.child_boy_infant));
//                        }

                        pregnant_icon.setVisibility(View.VISIBLE);
                        pregnant_icon.setImageResource(R.drawable.male_child_cbhc);
                        clientype = "malechild";
                    }
                }else if(!TextUtils.isEmpty(gender) && gender.equalsIgnoreCase("f")){
                    if (pClient.entityId() != null) {//image already in local storage most likey ):
                        //set profile image by passing the client id.If the image doesn't exist in the image org.smartregister.cbhc.repository then download and save locally
                        profileImageIV.setTag(org.smartregister.R.id.entity_id, pClient.entityId());
//                        if(profile_photo.get(pClient.entityId())==null){
////                            d = mActivity.getResources().getDrawable(R.drawable.child_girl_infant);
////                            profile_photo.put(pClient.entityId(),d);
//                            url = FileUtilities.getImageUrl(pClient.entityId()).replaceAll("///","/");
//                            ImageLoaderByGlide.setImageAsTarget(url,profileImageIV,R.drawable.child_girl_infant);
//
//                        }else{
                            DrishtiApplication.getCachedImageLoaderInstance().getImageByClientId(pClient.entityId(), OpenSRPImageLoader.getStaticImageListener(profileImageIV, R.drawable.child_girl_infant, R.drawable.child_girl_infant));
//                        }

                        pregnant_icon.setVisibility(View.VISIBLE);
                        pregnant_icon.setImageResource(R.drawable.female_child_cbhc);
                        clientype = "femalechild";
                    }
                }
            }else{
                if(!TextUtils.isEmpty(gender) && gender.equalsIgnoreCase("m")){
                    if (pClient.entityId() != null) {//image already in local storage most likey ):
                        //set profile image by passing the client id.If the image doesn't exist in the image org.smartregister.cbhc.repository then download and save locally
                        profileImageIV.setTag(org.smartregister.R.id.entity_id, pClient.entityId());
//                        if(profile_photo.get(pClient.entityId())==null){
////                            d = mActivity.getResources().getDrawable(R.drawable.male_cbhc_placeholder);
////                            profile_photo.put(pClient.entityId(),d);
//                            url = FileUtilities.getImageUrl(pClient.entityId()).replaceAll("///","/");
//                            ImageLoaderByGlide.setImageAsTarget(url,profileImageIV,R.drawable.male_cbhc_placeholder);
//
//                        }else{
                            DrishtiApplication.getCachedImageLoaderInstance().getImageByClientId(pClient.entityId(), OpenSRPImageLoader.getStaticImageListener(profileImageIV, R.drawable.male_cbhc_placeholder, R.drawable.male_cbhc_placeholder));
//                        }

                        pregnant_icon.setVisibility(View.INVISIBLE);
                        clientype = "member";
                    }
                }else if(!TextUtils.isEmpty(gender) && gender.equalsIgnoreCase("f")){
                    if (pClient.entityId() != null) {//image already in local storage most likey ):
                        //set profile image by passing the client id.If the image doesn't exist in the image org.smartregister.cbhc.repository then download and save locally
                        profileImageIV.setTag(org.smartregister.R.id.entity_id, pClient.entityId());
//                        if(profile_photo.get(pClient.entityId())==null){
////                            d = mActivity.getResources().getDrawable(R.drawable.women_cbhc_placeholder);
////                            profile_photo.put(pClient.entityId(),d);
//                            url = FileUtilities.getImageUrl(pClient.entityId()).replaceAll("///","/");
//                            ImageLoaderByGlide.setImageAsTarget(url,profileImageIV,R.drawable.women_cbhc_placeholder);
//                        }else{
                            DrishtiApplication.getCachedImageLoaderInstance().getImageByClientId(pClient.entityId(), OpenSRPImageLoader.getStaticImageListener(profileImageIV, R.drawable.women_cbhc_placeholder, R.drawable.women_cbhc_placeholder));
//                        }

                        clientype = "woman";
                    }
                }
            }

//            if(profile_photo.get(pClient.entityId())!=null){
//                profileImageIV.setImageDrawable(profile_photo.get(pClient.entityId()));
//            }


            profileImageIV.setTag(R.id.typeofclientformemberprofile,clientype);
            profileImageIV.setTag(R.id.clientformemberprofile,pClient);

            profileImageIV.setOnClickListener((ProfileActivity)getActivity());
            noOfUnregisterButton.setTag(R.id.clientformemberprofile,pClient);
            noOfUnregisterButton.setOnClickListener((ProfileActivity)getActivity());

        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            Log.e("------------","new org.smartregister.cbhc.view call");

            View view = inflater.inflate(R.layout.household_details_list_row,parent,false);

            return  view;
        }
        class ProfilePhotoAsyncTask extends AsyncTask {
            CommonPersonObjectClient pClient;
            int age = 0;
            ImageView profileImageIV;
            View view;
            ImageView pregnant_icon;
            public ProfilePhotoAsyncTask(CommonPersonObjectClient pClient, int age,ImageView profileImageIV,View view,ImageView pregnant_icon){
                this.pClient = pClient;
                this.age = age;
                this.profileImageIV = profileImageIV;
                this.view = view;
                this.pregnant_icon = pregnant_icon;
            }
            String gender;
            String clientype = "";
            @Override
            protected Object doInBackground(Object[] objects) {
                DetailsRepository detailsRepository = AncApplication.getInstance().getContext().detailsRepository();
                Map<String, String> detailmap = detailsRepository.getAllDetailsForClient(pClient.getCaseId());
                gender = detailmap.get("gender");
                pClient.getColumnmaps().putAll(detailmap);

                Map<String,Object> map = new HashMap<String,Object>();
                        map.put("gender",gender);
                map.put("pclient",pClient);
                return map;
            }

            @Override
            protected void onPostExecute(Object o) {
                super.onPostExecute(o);
                if(mActivity == null) return;
                String gender = "";
                CommonPersonObjectClient pClient = null;
                if(o instanceof HashMap){

                    gender = (String)((HashMap) o).get("gender");
                    pClient = (CommonPersonObjectClient)((HashMap) o).get("pclient");
                }

            }
        }
        public int childCount(String motherId){
            String tableName = DBConstants.CHILD_TABLE_NAME;
            String parentTableName = DBConstants.WOMAN_TABLE_NAME;
            SmartRegisterQueryBuilder queryBUilder = new SmartRegisterQueryBuilder();
            queryBUilder.SelectInitiateMainTable(tableName, new String[]{
                    tableName + ".relationalid",
                    tableName + ".details",
                    tableName + ".relational_id"
            });
            queryBUilder.customJoin("LEFT JOIN " + parentTableName + " ON  " + tableName + ".relational_id =  " + parentTableName + ".id");
            String mainSelect = queryBUilder.mainCondition(" ");

            AncRepository repo = (AncRepository) AncApplication.getInstance().getRepository();
            net.sqlcipher.database.SQLiteDatabase db = repo.getReadableDatabase();

            Cursor cursor = db.rawQuery(mainSelect+tableName+".relational_id = ?",new String[]{motherId});
            if(cursor!=null && cursor.getCount()>0){
                return cursor.getCount();

            }
            return 0;
        }

        public void addChild(LinearLayout household_details_list_row, String mother_id) {
            Log.e("--------------",mother_id);


            AncRepository repo = (AncRepository) AncApplication.getInstance().getRepository();
            net.sqlcipher.database.SQLiteDatabase db = repo.getReadableDatabase();


            String tableName = DBConstants.CHILD_TABLE_NAME;
            String parentTableName = DBConstants.WOMAN_TABLE_NAME;
            SmartRegisterQueryBuilder queryBUilder = new SmartRegisterQueryBuilder();
            queryBUilder.SelectInitiateMainTable(tableName, new String[]{
                    tableName + ".relationalid",
                    tableName + ".details",
                    tableName + ".openmrs_id",
                    tableName + ".relational_id",
                    tableName + ".first_name",
                    tableName + ".last_name",
                    tableName + ".gender",
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
            String mainCondition = " (dod is NULL OR dod = '' )";
            String mainSelect = queryBUilder.mainCondition(mainCondition);




            Cursor cursor = db.rawQuery(mainSelect+ "and "+tableName+".relational_id = ?",new String[]{mother_id});
            if(cursor!=null && cursor.getCount()>0){

            }



            cursor.moveToFirst();
            while (cursor.isAfterLast() == false) {
                CommonRepository commonRepository = org.smartregister.Context.getInstance().commonrepository(tableName);
                CommonPersonObject personinlist = commonRepository.readAllcommonforCursorAdapter(cursor);
                final CommonPersonObjectClient pClient = new CommonPersonObjectClient(personinlist.getCaseId(), personinlist.getDetails(), personinlist.getDetails().get("FWHOHFNAME"));
                pClient.setColumnmaps(personinlist.getColumnmaps());
                LinearLayout childrenLayout = (LinearLayout)inflater.inflate(R.layout.household_details_child_row, null);
                ((TextView)childrenLayout.findViewById(R.id.name_tv)).setText("Name : " + cursor.getString(cursor.getColumnIndex("first_name")));

                String dobString = cursor.getString(cursor.getColumnIndex("dob"));
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
                ((TextView)childrenLayout.findViewById(R.id.age_tv)).setText("Age : "+durationString);

                Gender gender = Gender.UNKNOWN;
                if (pClient != null && pClient.getDetails() != null) {
                    String genderString = getValue(pClient, "gender", false);
                    if (genderString != null && genderString.toLowerCase().equals("female")) {
                        gender = Gender.FEMALE;
                    } else if (genderString != null && genderString.toLowerCase().equals("male")) {
                        gender = Gender.MALE;
                    }
                    ImageView profileImageIV = (ImageView)childrenLayout.findViewById(R.id.profile_image_iv);

                    if (pClient.entityId() != null) {//image already in local storage most likey ):
                        //set profile image by passing the client id.If the image doesn't exist in the image org.smartregister.cbhc.repository then download and save locally
                        profileImageIV.setTag(org.smartregister.R.id.entity_id, pClient.entityId());
                        //DrishtiApplication.getCachedImageLoaderInstance().getImageByClientId(pClient.entityId(), OpenSRPImageLoader.getStaticImageListener((ImageView) profileImageIV, ImageUtils.profileImageResourceByGender(gender), ImageUtils.profileImageResourceByGender(gender)));

                    }
                }


                household_details_list_row.addView(childrenLayout);
                childrenLayout.findViewById(R.id.profile_name_layout).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
//                        ChildImmunizationActivity.launchActivity(HouseholdDetailActivity.this,pClient,null);
                    }
                });
                cursor.moveToNext();
            }
            cursor.close();

        }

    }

    public static int getAge(DateTime dateOfBirth) {

        Calendar today = Calendar.getInstance();
        Calendar birthDate = Calendar.getInstance();

        int age = 0;

        SimpleDateFormat dateFormat = JsonFormUtils.DATE_FORMAT;
        Date convertedDate = new Date();
        try {
            convertedDate = dateOfBirth.toDate();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        birthDate.setTime(convertedDate);
        if (birthDate.after(today)) {
            throw new IllegalArgumentException("Can't be born in the future");
        }

        age = today.get(Calendar.YEAR) - birthDate.get(Calendar.YEAR);

        // If birth date is greater than todays date (after 2 days adjustment of
        // leap year) then decrement age one year
        if ((birthDate.get(Calendar.DAY_OF_YEAR)
                - today.get(Calendar.DAY_OF_YEAR) > 3)
                || (birthDate.get(Calendar.MONTH) > today.get(Calendar.MONTH))) {
            age--;

            // If birth date and todays date are of same month and birth day of
            // month is greater than todays day of month then decrement age
        } else if ((birthDate.get(Calendar.MONTH) == today.get(Calendar.MONTH))
                && (birthDate.get(Calendar.DAY_OF_MONTH) > today
                .get(Calendar.DAY_OF_MONTH))) {
            age--;
        }

        return age;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(myHandler!=null)myHandler.removeCallbacksAndMessages(null);
//        profile_photo.clear();
    }

    public void setRelationMap(){
        rmap.clear();
        rmap.put("Household_Head","খানা প্রধান");
        rmap.put("Husband_or_Wife","স্বামী/স্ত্রী");
        rmap.put("Husband","স্বামী");
        rmap.put("Wife","স্ত্রী");
        rmap.put("Son","পুত্র");
        rmap.put("Daughter","কন্যা");
        rmap.put("Daughter_in_law","পুত্রবধূ");
        rmap.put("Grandson","নাতি");
        rmap.put("Granddaughter","নাতনি");
        rmap.put("father","পিতা");
        rmap.put("Mother","মাতা");
        rmap.put("Brother","ভাই");
        rmap.put("Sister","বোন");
        rmap.put("Nephew(Paternal)","ভাইপো");
        rmap.put("Niece(Paternal)","ভাইঝি");
        rmap.put("Nephew(Maternal)","ভাগ্নে");
        rmap.put("Niece(Maternal)","ভাগ্নি");
        rmap.put("Father_in_Law","শ্বশুর");
        rmap.put("Mother in Law","শাশুড়ি");
        rmap.put("Brother_in_Law","শ্যালক");
        rmap.put("Sister_in_Law","শ্যালিকা");
        rmap.put("Brother_in_Law(Wife)","দেবর");
        rmap.put("Brother_in_Law_Wife(Wife)","জা");
        rmap.put("Sister_in_Law(Wife)","ননদ");
        rmap.put("Wife_of_Brother","ভাইয়ের স্ত্রী");
        rmap.put("Husband_of_Sister","ভগ্নিপতি");
        rmap.put("Son_in_Law","জামাতা");
        rmap.put("Others_Relative","অন্যান্য আত্মীয়");
        rmap.put("Others_Non_Relative","অন্যান্য অনাত্মীয়");

    }
}
