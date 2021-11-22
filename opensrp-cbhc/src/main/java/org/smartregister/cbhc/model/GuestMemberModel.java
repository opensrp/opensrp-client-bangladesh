package org.smartregister.cbhc.model;

import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.json.JSONArray;
import org.json.JSONObject;
import org.smartregister.cbhc.application.AncApplication;
import org.smartregister.cbhc.contract.GuestMemberContract;
import org.smartregister.cbhc.domain.GuestMemberData;
import org.smartregister.cbhc.helper.ECSyncHelper;
import org.smartregister.cbhc.sync.AncClientProcessorForJava;
import org.smartregister.cbhc.util.DBConstants;
import org.smartregister.cbhc.util.JsonFormUtils;
import org.smartregister.clientandeventmodel.Client;
import org.smartregister.clientandeventmodel.Event;
import org.smartregister.commonregistry.CommonPersonObject;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.commonregistry.CommonRepository;
import org.smartregister.domain.tag.FormTag;
import org.smartregister.repository.AllSharedPreferences;
import org.smartregister.repository.BaseRepository;
import org.smartregister.sync.ClientProcessorForJava;

import java.util.ArrayList;
import java.util.Date;


public class GuestMemberModel extends JsonFormUtils implements GuestMemberContract.Model {

    private Context context;
    private ArrayList<GuestMemberData> guestMemberDataArrayList;
    private ArrayList<GuestMemberData> searchedGuestMemberDataArrayList;
    private ArrayList<String> navItemList;
    private boolean isFromSearch;
    public GuestMemberModel(Context context){
        this.context = context;
        this.guestMemberDataArrayList = new ArrayList<>();
        this.searchedGuestMemberDataArrayList = new ArrayList<>();
        this.navItemList = new ArrayList<>();
    }


    public void filterData(String query, String ssName){
        isFromSearch = false;
        searchedGuestMemberDataArrayList.clear();
        if(TextUtils.isEmpty(query) && TextUtils.isEmpty(ssName)) return;
        isFromSearch = true;
        for(GuestMemberData guestMemberData: guestMemberDataArrayList){
            if(!TextUtils.isEmpty(query)){
                String name = (guestMemberData.getfName()+" "+guestMemberData.getlName()).toLowerCase();
                if((name.contains(query.toLowerCase()))){
                    searchedGuestMemberDataArrayList.add(guestMemberData);
                }
            }
           /* else if(!TextUtils.isEmpty(ssName)){
                if( (guestMemberData.getfName()+" "+guestMemberData.getlName()).toLowerCase().equalsIgnoreCase(ssName)){
                    searchedGuestMemberDataArrayList.add(guestMemberData);
                }
            }
            else if(!TextUtils.isEmpty(query)){
               String name = guestMemberData.getName().toLowerCase();
                String phoneNo = guestMemberData.getPhoneNo().toLowerCase();
                Log.v("SEARCH_GUEST","name:"+name+":query:"+query);
                if(name.contains(query.toLowerCase()) || phoneNo.contains(query.toLowerCase())){
                    searchedGuestMemberDataArrayList.add(guestMemberData);
                }
            }*/

        }

    }

    /*@Override
    public ArrayList<GuestMemberData> getData() {
        return isFromSearch?searchedGuestMemberDataArrayList:guestMemberDataArrayList;
    }*/

    @Override
    public Pair<Client, Event> processRegistration(String jsonString) {
        try {
            Triple<Boolean, JSONObject, JSONArray> registrationFormParams = validateParameters(jsonString);

            if (!registrationFormParams.getLeft()) {
                return null;
            }
            JSONObject jsonForm = registrationFormParams.getMiddle();
            JSONArray fields = registrationFormParams.getRight();
            String entityId = getString(jsonForm, ENTITY_ID);
            if (StringUtils.isBlank(entityId)) {
                entityId = generateRandomUUIDString();
            }
            lastInteractedWith(fields);
            //dobEstimatedUpdateFromAge(fields);
            processAttributesWithChoiceIDsForSave(fields);
            FormTag formTag = formTag(getAllSharedPreferences());
           // formTag.appVersionName = BuildConfig.VERSION_NAME;
            Client baseClient = createBaseClient(fields, formTag,entityId);
           // baseClient.setLastName(GUEST_MEMBER_REGISTRATION);
            Event baseEvent = org.smartregister.util.JsonFormUtils.createEvent(fields, getJSONObject(jsonForm, METADATA), formTag, entityId, getString(jsonForm, ENCOUNTER_TYPE), /*CoreConstants.TABLE_NAME.CHILD*/"test");
            tagSyncMetadata(getAllSharedPreferences(), baseEvent);
            String encounterType = getString(jsonForm, ENCOUNTER_TYPE);
            String entity_id = baseClient.getBaseEntityId();
            updateFormSubmissionID(encounterType,entity_id,baseEvent);
/*
            JSONObject jsonObject = new JSONObject(jsonString);
            JSONObject jobkect = jsonObject.getJSONObject("step1");
            String villageIndex = jobkect.getString("village_index");
            String ssIndex = jobkect.getString("ss_index");*/
            //SSLocations ss = SSLocationHelper.getInstance().getSsModels().get(Integer.parseInt(ssIndex)).locations.get(Integer.parseInt(villageIndex));
        /*    JSONArray field = jobkect.getJSONArray(FIELDS);
            JSONObject villageIdObj = getFieldJSONObject(field, "village_id");
            String villageId = villageIdObj.getString(VALUE);*/
           /* try{
                String hhid = jobkect.getString( "hhid");
                GuestMemberIdRepository householdIdRepo = SampleApplication.getHNPPInstance().getGuestMemberIdRepository();
                householdIdRepo.close(villageId,hhid);
            }catch (Exception e){
                e.printStackTrace();

            }*/
          /*  List<Address> listAddress = new ArrayList<>();
            listAddress.add(SSLocationHelper.getInstance().getSSAddress(ss));
            baseClient.setAddresses(listAddress);*/
            return Pair.create(baseClient, baseEvent);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void saveRegistration(Pair<Client, Event> pair) {
        try{
            ECSyncHelper ecUpdater = ECSyncHelper.getInstance(context);

                Client baseClient = pair.first;
                Event baseEvent = pair.second;

                if (baseClient != null) {
                    JSONObject clientJson = new JSONObject(JsonFormUtils.gson.toJson(baseClient));

                        getSyncHelper().addClient(baseClient.getBaseEntityId(), clientJson);

                }

                if (baseEvent != null) {
                    JSONObject eventJson = new JSONObject(JsonFormUtils.gson.toJson(baseEvent));
                    getSyncHelper().addEvent(baseEvent.getBaseEntityId(), eventJson);
                }


                long lastSyncTimeStamp = getAllSharedPreferences().fetchLastUpdatedAtDate(0);
                Date lastSyncDate = new Date(lastSyncTimeStamp);
            //ClientProcessorForJava.getInstance(context).processClient(ecUpdater.getEvents(lastSyncDate, /*BaseRepository.TYPE_Unprocessed*/"unprocessed"));
           /* AncClientProcessorForJava.getInstance(context).processClient(ecUpdater.getEvents(lastSyncDate, BaseRepository.TYPE_Unsynced));
            //PathClientProcessor.getInstance(context).processClient(ecUpdater.getEvents(lastSyncDate, BaseRepository.TYPE_Unsynced));
            getAllSharedPreferences().saveLastUpdatedAtDate(lastSyncDate.getTime());*/


            AncApplication.getInstance().getClientProcessorForJava().processClient(ecUpdater.getEvents(lastSyncDate, BaseRepository.TYPE_Unsynced));
            getAllSharedPreferences().saveLastUpdatedAtDate(lastSyncDate.getTime());

        } catch (Exception e) {
                e.printStackTrace();
            }


    }

    public ArrayList<GuestMemberData> getData() {
        return isFromSearch?searchedGuestMemberDataArrayList:guestMemberDataArrayList;
    }

    public ArrayList<String> getNavData() {
        return navItemList;
    }
    public ECSyncHelper getSyncHelper() {
        return ECSyncHelper.getInstance(context.getApplicationContext());
    }


    public AllSharedPreferences getAllSharedPreferences() {
        return AncApplication.getInstance().getContext().allSharedPreferences();
    }

   /* public ClientProcessorForJava getClientProcessorForJava() {
        return VaccinatorApplication.getInstance().getClientProcessorForJava();
    }*/

    @Override
    public void loadData() {
        guestMemberDataArrayList.clear();
       /* String query =  "select ec_guest_member.base_entity_id,ec_guest_member.opensrp_id,ec_guest_member.first_name,ec_guest_member.dob,ec_guest_member.gender,ec_guest_member.phone_number from ec_guest_member " +
         " where ec_guest_member.date_removed is null order by ec_guest_member.last_interacted_with desc ";*/
        //String query =  "select * from ec_guest_member";
        //String query =  "select * from ec_guest_member";
        // String query = "Select child.id as _id , child.relationalid , child.Patient_Identifier, child.first_name , child.last_name , child.dob ,child.gender, child.PregnancyStatus, child.tasks, child.age as age, NULL as MaritalStatus, child.camp_type, child.child_status FROM ec_guest_member as child";
        String query = "Select child.id as _id , child.relationalid , child.Patient_Identifier, child.first_name , child.last_name , child.dob ,child.gender, child.PregnancyStatus, child.tasks, child.age as age, NULL as MaritalStatus, child.child_status FROM ec_guest_member as child";

        try (Cursor cursor = AncApplication.getInstance().getRepository().getReadableDatabase().rawQuery(query, new String[]{})) {
            if (cursor != null && cursor.getCount() > 0) {
                cursor.moveToFirst();
                while (!cursor.isAfterLast()) {

                    CommonRepository commonRepository = org.smartregister.Context.getInstance().commonrepository("ec_guest_member");
                    CommonPersonObject personinlist = commonRepository.readAllcommonforCursorAdapter(cursor);
                    //personinlist.setCaseId(cursor.getString(cursor.getColumnIndex("_id")));
                    CommonPersonObjectClient pClient   = new CommonPersonObjectClient(personinlist.getCaseId(), personinlist.getDetails(), personinlist.getDetails().get("FWHOHFNAME"));
                    pClient.setColumnmaps(personinlist.getColumnmaps());

                    int baseEntity = cursor.getColumnIndex("base_entity_id");
                    int fname = cursor.getColumnIndex("first_name");
                    int lname = cursor.getColumnIndex("last_name");
                    int dob = cursor.getColumnIndex("dob");
                    int gender = cursor.getColumnIndex("gender");
                    int age = cursor.getColumnIndex("age");

                    guestMemberDataArrayList.add(new GuestMemberData(
                            cursor.isNull(baseEntity) ? "" : cursor.getString(baseEntity),
                            cursor.isNull(fname) ? "" : cursor.getString(fname),
                            cursor.isNull(lname) ? "" : cursor.getString(lname),
                            cursor.isNull(dob) ? "" : cursor.getString(dob),
                            cursor.isNull(gender) ? "" : cursor.getString(gender),
                            cursor.isNull(age) ? "" : cursor.getString(age),
                            pClient
                    ));

                    cursor.moveToNext();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }



    }
    @Override
    public Context getContext() {
        return context;
    }

    @Override
    public void fetchMemberData() {
        navItemList.clear();
       /* String query =  "select ec_guest_member.base_entity_id,ec_guest_member.opensrp_id,ec_guest_member.first_name,ec_guest_member.dob,ec_guest_member.gender,ec_guest_member.phone_number from ec_guest_member " +
         " where ec_guest_member.date_removed is null order by ec_guest_member.last_interacted_with desc ";*/
        String query =  "select * from ec_guest_member";
        Cursor cursor = null;
        try{
            cursor = AncApplication.getInstance().getRepository().getReadableDatabase().rawQuery(query, new String[]{});
            if(cursor !=null && cursor.getCount() > 0) {
                navItemList.add("Member Count("+cursor.getCount()+")");
            }
        }catch (Exception e){
        }finally {
            if(cursor!=null) cursor.close();
        }
    }
}
