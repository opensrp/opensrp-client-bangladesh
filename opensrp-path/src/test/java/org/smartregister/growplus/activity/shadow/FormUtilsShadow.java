package org.smartregister.growplus.activity.shadow;

import android.support.annotation.Nullable;

import org.json.JSONException;
import org.json.JSONObject;
import org.mockito.Mockito;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.shadow.api.Shadow;
import org.smartregister.util.FormUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

/**
 * Created by kaderchowdhury on 14/12/17.
 */
@Implements(FormUtils.class)
public class FormUtilsShadow extends Shadow {

    @Implementation
    public static FormUtils getInstance(@Nullable  android.content.Context ctx) throws Exception {
        FormUtils formUtils = Mockito.mock(FormUtils.class);
        JSONObject object = new JSONObject();
        try {
            File file = getFileFromPath(new FormUtilsShadow(),"json_form/child_enrollment.json");
            object = new JSONObject(getStringFromFile(file));
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        Mockito.doReturn(object).when(formUtils).getFormJson("child_enrollment");
        return formUtils;
    }


    public static String getStringFromFile(File f) throws Exception {
        InputStream inputStream = new FileInputStream(f);
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(inputStream, "UTF-8"));
        String jsonString;
        StringBuilder stringBuilder = new StringBuilder();

        while ((jsonString = reader.readLine()) != null) {
            stringBuilder.append(jsonString);
        }
        inputStream.close();

        return stringBuilder.toString();


    }

    private static File getFileFromPath(Object obj, String fileName) {
        ClassLoader classLoader = obj.getClass().getClassLoader();
        URL resource = classLoader.getResource(fileName);
        return new File(resource.getPath());
    }
}
