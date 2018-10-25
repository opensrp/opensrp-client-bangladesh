package org.smartregister.growplus.activity.shadow;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.shadow.api.Shadow;

import java.util.ArrayList;

import util.JsonFormUtils;

/**
 * Created by kaderchowdhury on 14/12/17.
 */
@Implements(JsonFormUtils.class)
public class JsonFormUtilsShadow extends Shadow {
    @Implementation
    public static String getOpenMrsLocationId(org.smartregister.Context context,
                                              String locationName) throws JSONException {
        return "0";
    }
    @Implementation
    public static String getOpenMrsReadableName(String name) {
        return "shadow-text";
    }

    @Implementation
    public static void addChildRegLocHierarchyQuestions(JSONObject form,
                                                        org.smartregister.Context context) {

    }

    @Implementation
    public static JSONArray generateDefaultLocationHierarchy(org.smartregister.Context context, ArrayList<String> allowedLevels) {
        return new JSONArray();
    }
}
