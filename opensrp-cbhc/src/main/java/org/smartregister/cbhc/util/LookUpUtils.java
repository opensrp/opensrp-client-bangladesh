package org.smartregister.cbhc.util;

import org.json.JSONArray;
import org.json.JSONObject;

public class LookUpUtils {

    public static void putRelationalIdInLookupObjects(JSONObject form, String relational_id){
        try {
            if (form.has("step1")) {
                JSONObject step1 = form.getJSONObject("step1");
                if (step1.has("fields")) {
                    JSONArray fields = step1.getJSONArray("fields");
                    for (int i = 0; i < fields.length(); i++) {
                        JSONObject field_object = fields.getJSONObject(i);
                        if(field_object.has("look_up")){
                            field_object.put("relational_id",relational_id);
                        }
                    }
                }
            }
        }catch(Exception e){

        }

    }
    public static void putMotherName(JSONObject form, String motherName){
        try {
            JSONObject stepOne = form.getJSONObject(JsonFormUtils.STEP1);
            JSONArray jsonArray = stepOne.getJSONArray(JsonFormUtils.FIELDS);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);

                if (jsonObject.getString(JsonFormUtils.KEY).equalsIgnoreCase("Mother_Guardian_First_Name_english")){
//                    jsonObject.put("mother_id",motherId);
                    jsonObject.put(JsonFormUtils.VALUE,motherName);
                }

            }
        }catch(Exception e){
            e.printStackTrace();

        }

    }
}
