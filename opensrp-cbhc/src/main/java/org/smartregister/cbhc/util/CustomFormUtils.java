package org.smartregister.cbhc.util;

import static com.vijay.jsonwizard.utils.FormUtils.fields;
import static com.vijay.jsonwizard.utils.FormUtils.getFieldJSONObject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Random;

public class CustomFormUtils {
    public static JSONObject addInitialUniqueId(JSONObject form) throws JSONException {
        JSONArray field = fields(form, "step1");
        JSONObject opensrp_unique_id = getFieldJSONObject(field, "opensrp_id");
        opensrp_unique_id.put(JsonFormUtils.VALUE,generateFiveDigitRandNum());
        return form;
    }
    static String generateFiveDigitRandNum(){
        Random r = new Random( System.currentTimeMillis() );
        return String.valueOf(10000 + r.nextInt(20000));
    }
}
