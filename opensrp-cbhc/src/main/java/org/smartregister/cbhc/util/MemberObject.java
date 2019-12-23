package org.smartregister.cbhc.util;

import android.os.AsyncTask;

import net.sqlcipher.Cursor;
import net.sqlcipher.database.SQLiteDatabase;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.cbhc.application.AncApplication;
import org.smartregister.cbhc.repository.AncRepository;
import org.smartregister.commonregistry.CommonPersonObjectClient;

import java.lang.reflect.Member;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
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

    public void putHouseholdId(final JSONObject memberObject, final String house_hold_id) {


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

}
