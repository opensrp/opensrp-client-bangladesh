package org.smartregister.cbhc.util;

import android.database.Cursor;
import android.os.AsyncTask;
import android.text.TextUtils;

import net.sqlcipher.database.SQLiteDatabase;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.cbhc.application.AncApplication;
import org.smartregister.cbhc.model.UnsendData;
import org.smartregister.cbhc.repository.AncRepository;
import org.smartregister.commonregistry.CommonPersonObjectClient;

import java.lang.reflect.Member;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MemberObject {

    public static final String type1 = "groupActivity";
    public static final String type2 = "referral";
    public static final int type1_RESULT_CODE = 12121;
    public static final int type2_RESULT_CODE = 22121;
    Map<String, String> householdDetails;
    private String type;


    public MemberObject(CommonPersonObjectClient householdDetails, String type) {
        if (householdDetails != null)
            this.householdDetails = householdDetails.getColumnmaps();
        this.type = type;
    }

    public JSONObject getMemberObject(String mhv_id, String mhv_name, String cc_id, String cc_name, String cc_address,String house_hold_viewable_id) {
        JSONObject object = new JSONObject();
        try {
            object.put("type", type);
            object.put("mhv_id", mhv_id);
            object.put("cc_id", cc_id);
            object.put("cc_name", cc_name);
            object.put("cc_address", Utils.getLocationTree());

            object.put("mhv_name", mhv_name);
            if (type2.equalsIgnoreCase(type))
                object.put("member", populateMemberObject(mhv_id, mhv_name,house_hold_viewable_id));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return object;

    }
    public JSONObject populateMemberObject(String mhv_id, String mhv_name,String house_hold_viewable_id) {

        JSONObject memberObject = new JSONObject();
        try {
            memberObject.put("member_id", getValue("base_entity_id"));

            memberObject.put("mhv_id", mhv_id);
            memberObject.put("mhv_name", mhv_name);
            String house_hold_id = getValue("relational_id");
            memberObject.put("house_hold_id", house_hold_id);
            memberObject.put("house_hold_viewable_id", house_hold_viewable_id);
            memberObject.put("is_house_hold_head", getValue("relation").equals("Household_Head"));
            memberObject.put("first_name", getValue("first_name"));
            memberObject.put("last_name", getValue("last_name"));
            memberObject.put("date_of_birth", getValue("dob"));
            memberObject.put("is_hypertension", getValue("Non Communicable Disease").contains("High Blood Pressure"));
            memberObject.put("is_heart_disease", false);
            memberObject.put("is_kidney_disease", false);
            memberObject.put("is_diabetes", getValue("Non Communicable Disease").contains("Diabetes"));
            memberObject.put("is_pregnant", getValue("PregnancyStatus").equals("Antenatal Period"));
            memberObject.put("is_child", getAge(getValue("dob")) <= 5);
            memberObject.put("added_on", getValue("member_Reg_Date"));
            memberObject.put("monthly_income", 0);
            memberObject.put("has_health_card", false);
            memberObject.put("health_id_card", getValue("Patient_Identifier"));
            memberObject.put("nid", getValue("person_nid"));
            memberObject.put("is_disable", getValue("disable").equals("Yes"));
            memberObject.put("death_of_death", "");
            memberObject.put("contact_number", getValue("phone_number"));
            memberObject.put("profession", Utils.getOccupationIndex(getProfession()));
            memberObject.put("blood_group", getValue("bloodgroup"));
            memberObject.put("gender", getValue("gender"));

            return memberObject;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }


    public static JSONObject populateNewHHObject(String local_id,String cc_id, String server_id,String mhv_id,String baseEntityId,HashMap<String,String> map) {
        JSONObject memberObject = new JSONObject();
        try {
            //memberObject.put("member_id", getValue("base_entity_id"));
            memberObject.put("local_id", local_id);
            memberObject.put("cc_id", cc_id);
            memberObject.put("server_id", server_id);
            memberObject.put("mhv_id", mhv_id);
            ///
            memberObject.put("accommodation_type",getValueFromMap(map,"household_type"));
            memberObject.put("accuracy",getGPS(getValueFromMap(map,"geopoint"),"accuracy"));
            memberObject.put("altitude",getGPS(getValueFromMap(map,"geopoint"),"altitude"));
            memberObject.put("date_month",getValueFromMap(map,"Date_Of_Reg"));
            memberObject.put("drinking_water",getValueFromMap(map,"water_source"));
            memberObject.put("family_financial_state",getValueFromMap(map,"financial_status"));
            memberObject.put("has_house_hold",false);
            memberObject.put("health_care_name_give_service_to_the_family",getValueFromMap(map,"info_provider_name"));
            memberObject.put("holding_number",getValueFromMap(map,"householdCode"));
            memberObject.put("house_hold_head_name",getValueFromMap(map,"first_name"));
            memberObject.put("house_hold_head",getValueFromMap(map,"first_name"));
            memberObject.put("house_hold_head_id",getValueFromMap(map,"Patient_Identifier"));
            memberObject.put("house_hold_id",getValueFromMap(map,"Patient_Identifier"));
            memberObject.put("house_hold_id_string",getValueFromMap(map,"base_entity_id"));
            memberObject.put("informer_name",getValueFromMap(map,"first_name"));
            memberObject.put("is_address_same",getValueFromMap(map,"is_permanent_address").equalsIgnoreCase("হ্যাঁ"));
            memberObject.put("latitude",getGPS(getValueFromMap(map,"geopoint"),"latitude"));
            memberObject.put("longitude",getGPS(getValueFromMap(map,"geopoint"),"longitude"));
            memberObject.put("latrine_type",getValueFromMap(map,"latrine_type"));
            memberObject.put("monthly_expense",getValueFromMap(map,"Monthly_Expenditure"));
            memberObject.put("nearest_community_clinic_distance",getValueFromMap(map,"Clinic_Distance"));
            memberObject.put("nearest_community_clinic_name",getValueFromMap(map,"Community_Clinic"));
            memberObject.put("nearest_health_care_distance",getValueFromMap(map,"Health_Facility_Distance"));
            memberObject.put("nearest_health_care_name",getValueFromMap(map,"Health_Care_Center"));
            memberObject.put("permanent_address",getValueFromMap(map,"HIE_FACILITIES"));
            memberObject.put("post_office",getValueFromMap(map,"postOfficePresent"));
            memberObject.put("present_address",getValueFromMap(map,"ADDRESS_LINE"));
            memberObject.put("village_or_colony",getValueFromMap(map,"village"));
            return memberObject;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static JSONObject populateNewMemberObject(String local_id,String cc_id, String server_id,String mhv_id,String baseEntityId,HashMap<String,String> map) {

        JSONObject memberObject = new JSONObject();
        try {
            //memberObject.put("member_id", getValue("base_entity_id"));
            memberObject.put("local_id", local_id);
            memberObject.put("cc_id", cc_id);
            memberObject.put("server_id", server_id);
            memberObject.put("mhv_id", mhv_id);
            ///
            memberObject.put("added_on",getValueFromMap(map,"member_Reg_Date"));
            memberObject.put("birth_certificate_number",getValueFromMap(map,"person_brid"));
            memberObject.put("birth_place",getValueFromMap(map,"birthPlace"));
            memberObject.put("blood_group",getValueFromMap(map,"bloodgroup"));
            memberObject.put("comment",getValueFromMap(map,"comments"));
            memberObject.put("date_of_birth",getValueFromMap(map,"dob"));
            memberObject.put("disability_type",getValueFromMap(map,"Disability_Type"));
            memberObject.put("disease_type",getValueFromMap(map,"disease_type"));
            memberObject.put("domestic_diseases",getValueFromMap(map,"family_diseases_details"));
            memberObject.put("educational_qualification",getValueFromMap(map,"education"));
            memberObject.put("epi_card_number",getValueFromMap(map,"person_epi"));
            memberObject.put("ethnicity",getValueFromMap(map,"ethnicity"));
            memberObject.put("fathers_first_name",getValueFromMap(map,"fatherNameEnglish"));
            memberObject.put("fathers_first_name_bn",getValueFromMap(map,"fathernameBangla"));
            memberObject.put("first_name",getValueFromMap(map,"first_name"));
            memberObject.put("first_name_bn",getValueFromMap(map,"givenNameLocal"));
            memberObject.put("fullName",getValueFromMap(map,"first_name")+" "+getValueFromMap(map,"last_name"));
            memberObject.put("gender",getValueFromMap(map,"gender"));
            memberObject.put("health_id_card",getValueFromMap(map,"Patient_Identifier"));
            memberObject.put("house_hold_id_string",getValueFromMap(map,"relational_id"));
            memberObject.put("house_hold_id",getIdentifierOfHH(getValueFromMap(map,"relational_id")));
            memberObject.put("last_name",getValueFromMap(map,"last_name"));
            memberObject.put("marital_status",getValueFromMap(map,"MaritalStatus"));
            memberObject.put("mothers_first_name",getValueFromMap(map,"motherNameEnglish"));
            memberObject.put("mothers_first_name_bn",getValueFromMap(map,"motherNameBangla"));
            memberObject.put("nid",getValueFromMap(map,"person_nid"));
            memberObject.put("phone",getValueFromMap(map,"phone_number"));
            memberObject.put("profession_type",getValueFromMap(map,"Occupation_Category"));
            memberObject.put("relationship_with_household_head",getValueFromMap(map,"Realtion_With_Household_Head"));
            memberObject.put("religion",getValueFromMap(map,"Religion"));
            ///

            memberObject.put("member_id", baseEntityId);
            memberObject.put("is_disable", getValueFromMap(map,"disable").equalsIgnoreCase("Yes"));
            memberObject.put("death_of_death", "");
            memberObject.put("is_hypertension",  getValueFromMap(map,"NonComnta_Disease").contains("High Blood Pressure"));
            memberObject.put("is_heart_disease", false);
            memberObject.put("is_kidney_disease", false);
            memberObject.put("is_diabetes", getValueFromMap(map,"NonComnta_Disease").contains("Diabetes"));
            memberObject.put("is_pregnant", getValueFromMap(map,"PregnancyStatus").equals("Antenatal Period"));
            memberObject.put("is_child", getAge(getValueFromMap(map,"dob")) <= 5);
            memberObject.put("monthly_income", 0);
            memberObject.put("has_health_card", true);
            memberObject.put("is_house_hold_head",getValueFromMap(map,"Realtion_With_Household_Head").equals("Household_Head"));
            memberObject.put("isMale",getValueFromMap(map,"gender").equalsIgnoreCase("M"));
            memberObject.put("age", getAge(getValueFromMap(map,"dob")));
            return memberObject;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
    public static HashMap<String, String> getDetails(String baseEntityId, String type) {
        HashMap<String, String> map = new HashMap<>();
        String query = null;
        AncRepository repo = (AncRepository) AncApplication.getInstance().getRepository();
        SQLiteDatabase db = repo.getWritableDatabase();
        if(type.equalsIgnoreCase(Constants.CMED_KEY.HH_TYPE)){
            query = "select * from ec_household where base_entity_id='" + baseEntityId + "'";

        }else{
            query = getQuery(type,baseEntityId);
        }

        Cursor cursor = db.rawQuery(query, new String[]{});
        try {

            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                int columncount = cursor.getColumnCount();
                for(int i=0;i<columncount;i++){
                    map.put(cursor.getColumnName(i),cursor.getString(i));
                }
                cursor.moveToNext();
            }

        } catch (Exception e) {

        } finally {
            cursor.close();
        }

        return map;
    }
    private static String getQuery(String type, String baseEntityId) {
        if (type.equals(Constants.CMED_KEY.MM_TYPE)) {
            return "select * from ec_member where base_entity_id='" + baseEntityId + "'";
        }
        else if (type.equals(Constants.CMED_KEY.WOMEN_TYPE)) {
            return "select * from ec_woman where base_entity_id='" + baseEntityId + "'";
        }
        else if (type.equals(Constants.CMED_KEY.CHILD_TYPE)) {
            return "select * from ec_child where base_entity_id='" + baseEntityId + "'";
        }
        return "";
    }
    public static String getValueFromMap(HashMap<String,String> map, String key){
        try{
            String str = map.get(key);
            return TextUtils.isEmpty(str)?"null":str;
        }catch (Exception e){
            e.printStackTrace();
        }
        return "null";
    }

    private static String getGPS(String geopoint, String category){
        if(TextUtils.isEmpty(geopoint)) return "";
        String[] str = geopoint.split(" ");
        try{
            String lat = str[0];
            String lng = str[1];
            if(category.equalsIgnoreCase("latitude")) return lat;
            if(category.equalsIgnoreCase("longitude")) return lng;
        }catch (Exception e){

        }

        return "";
    }
    public String getProfession() {
        String category = getValue("Occupation_Category");
        if (!StringUtils.isEmpty(category)) {
            String proffesion = getValue(category);
            return proffesion;
        }
        return "";
    }

    public static int getAge(String dob) {
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

            }


        }

        return 0;
    }
    private static String getIdentifierOfHH(String relationalId){
        String identifier = "";
        AncRepository repo = (AncRepository) AncApplication.getInstance().getRepository();
        SQLiteDatabase db = repo.getWritableDatabase();
        String query = "select Patient_Identifier from ec_household where base_entity_id='" + relationalId + "'";

        Cursor cursor = db.rawQuery(query, new String[]{});
        try {

            if (cursor.moveToNext()) {
                identifier = cursor.getString(0);

            }


        } catch (Exception e) {

        } finally {
            cursor.close();
        }

        return identifier;
    }

    public String getValue(String key) {
        if (householdDetails != null && householdDetails.containsKey(key))
            return householdDetails.get(key) == null ? "" : householdDetails.get(key);
        return "";
    }
    public String getMemberValue(String key, CommonPersonObjectClient client) {
        String value = org.smartregister.util.Utils.getValue(client.getColumnmaps(), key, true);
        return value;
    }

}
