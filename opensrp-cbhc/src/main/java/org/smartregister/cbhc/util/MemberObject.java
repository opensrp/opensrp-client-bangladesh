package org.smartregister.cbhc.util;

import android.os.AsyncTask;

import net.sqlcipher.Cursor;
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
    public JSONObject getHHObject(String local_id, String mhv_id, String cc_id, String server_id, String house_hold_id, List<String> hhArrayList) {
        return populateHHObject(local_id,server_id,cc_id,mhv_id,house_hold_id,hhArrayList);
    }

    public JSONObject populateHHObject(String local_id,String server_id,String cc_id,String mhv_id,String house_hold_id,List<String> hhArrayList) {

        JSONObject hhObject = new JSONObject();
        try {
            hhObject.put("local_id", local_id);
            hhObject.put("server_id", server_id);
            hhObject.put("cc_id", cc_id);
            hhObject.put("mhv_id", mhv_id);
            hhObject.put("date_month", hhArrayList.get(0));
            hhObject.put("house_hold_head_name", hhArrayList.get(1));
            hhObject.put("house_hold_id", house_hold_id);
            hhObject.put("address", hhArrayList.get(2));
            hhObject.put("latrine_type", hhArrayList.get(3));
            hhObject.put("accommodation_type", hhArrayList.get(4));
            hhObject.put("drinking_water", hhArrayList.get(5));
            hhObject.put("monthly_expense", hhArrayList.get(6));
            hhObject.put("system_id", hhArrayList.get(7));
            hhObject.put("hh_code", hhArrayList.get(8));
            return hhObject;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
    public JSONObject getGroupMemberObject(String local_id, String mhv_id, String cc_id, String server_id,CommonPersonObjectClient client) {
        return  populateGroupMemberObject(local_id,cc_id,server_id,mhv_id, client);

    }
    public JSONObject populateGroupMemberObject(String local_id,String cc_id, String server_id,String mhv_id,CommonPersonObjectClient client) {

        JSONObject memberObject = new JSONObject();
        try {
            //memberObject.put("member_id", getValue("base_entity_id"));
            memberObject.put("local_id", local_id);
            memberObject.put("cc_id", cc_id);
            memberObject.put("server_id", server_id);
            memberObject.put("mhv_id", mhv_id);
            memberObject.put("first_name", getMemberValue("first_name",client));
            memberObject.put("last_name", getMemberValue("last_name",client));
            memberObject.put("date_of_birth", getMemberValue("dob",client));
            memberObject.put("health_id_card", getMemberValue("Patient_Identifier",client));
            memberObject.put("added_on", getMemberValue("member_Reg_Date",client));
            memberObject.put("is_heart_disease", false);
            memberObject.put("is_kidney_disease", false);
            memberObject.put("is_diabetes", getMemberValue("Non Communicable Disease",client).contains("Diabetes"));
            memberObject.put("is_pregnant", getMemberValue("PregnancyStatus",client).equals("Antenatal Period"));
            memberObject.put("is_child", getAge(getMemberValue("dob",client)) <= 5);
            memberObject.put("nid", getMemberValue("person_nid",client));
            memberObject.put("is_disable", getMemberValue("disable",client).equals("Yes"));
            memberObject.put("death_of_death", "");
            memberObject.put("contact_number", getMemberValue("phoneNumber",client));
            memberObject.put("profession", Utils.getOccupationIndex(getProfession()));
            memberObject.put("blood_group", getMemberValue("bloodgroup",client));
            memberObject.put("gender", getMemberValue("gender",client));
         /*   memberObject.put("added_on", getValue("member_Reg_Date"));
            memberObject.put("monthly_income", 0);
            memberObject.put("has_health_card", false);

            memberObject.put("nid", getValue("person_nid"));
            memberObject.put("is_disable", getValue("disable").equals("Yes"));
            memberObject.put("death_of_death", "");
            memberObject.put("contact_number", getValue("phone_number"));
            memberObject.put("profession", Utils.getOccupationIndex(getProfession()));
            memberObject.put("blood_group", getValue("bloodgroup"));
            memberObject.put("gender", getValue("gender"));*/

            return memberObject;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getProfession() {
        String category = getValue("Occupation Category");
        if (!StringUtils.isEmpty(category)) {
            String proffesion = getValue(category);
            return proffesion;
        }
        return "";
    }

    public int getAge(String dob) {
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
                org.smartregister.cbhc.util.Utils.appendLog(getClass().getName(), e);

            }


        }

        return 0;
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
