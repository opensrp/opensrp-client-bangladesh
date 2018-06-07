package org.smartregister.growplus.activity;

import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;

import com.rengwuxian.materialedittext.MaterialEditText;
import com.vijay.jsonwizard.activities.JsonFormActivity;
import com.vijay.jsonwizard.constants.JsonFormConstants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.growplus.fragment.MediaDialogFragment;
import org.smartregister.growplus.fragment.PathJsonFormFragment;

/**
 * Created by keyman on 11/04/2017.
 */
public class PathJsonFormActivity extends JsonFormActivity {

    private int generatedId = -1;
    private MaterialEditText balancetextview;
    private PathJsonFormFragment pathJsonFormFragment;
    public static boolean isLaunched = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        isLaunched = true;
    }

    @Override
    public void initializeFormFragment() {
        isLaunched = true;
        pathJsonFormFragment = PathJsonFormFragment.getFormFragment(JsonFormConstants.FIRST_STEP_NAME);
        getSupportFragmentManager().beginTransaction()
                .add(com.vijay.jsonwizard.R.id.container, pathJsonFormFragment).commit();
    }

    @Override
    public void writeValue(String stepName, String key, String value, String openMrsEntityParent, String openMrsEntity, String openMrsEntityId) throws JSONException {
        super.writeValue(stepName, key, value, openMrsEntityParent, openMrsEntity, openMrsEntityId);
        refreshCalculateLogic(key, value);

    }

    @Override
    public void onFormFinish() {
        isLaunched = false;
        super.onFormFinish();
    }

    private void refreshCalculateLogic(String key, String value) {
        try {
            JSONObject object = getStep("step1");
            JSONArray fields = object.getJSONArray("fields");
            for (int i = 0; i < fields.length(); i++) {
                JSONObject questionGroup = fields.getJSONObject(i);
                if (questionGroup.has("key") && questionGroup.has("has_media_content")) {
                    if(questionGroup.getString("key").equalsIgnoreCase(key)) {
                        if (questionGroup.getBoolean("has_media_content")) {
                            JSONArray medias = questionGroup.getJSONArray("media");
                            for(int j = 0;j<medias.length();j++) {
                                JSONObject media = medias.getJSONObject(j);
                                mediadialog(media,value);
                            }
                        }
                    }

                }
            }
        }catch(Exception e){

        }

    }

    public void mediadialog(JSONObject media, String value){
        try {
            if (media.getString("media_trigger_value").equalsIgnoreCase(value)) {
                String mediatype = media.getString("media_type");
                String medialink = media.getString("media_link");

                dummydialog(value,mediatype,medialink);
            }
        }catch (Exception e){

        }
    }

    private void dummydialog(String value, String mediatype, String medialink) {
        MediaDialogFragment.launchDialog(this,"mediaDialog",mediatype,medialink);
//        AlertDialog.Builder builder;
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            builder = new AlertDialog.Builder(this, android.R.style.Theme_Material_Dialog_Alert);
//        } else {
//            builder = new AlertDialog.Builder(this);
//        }
//        builder.setTitle(value)
//                .setMessage("Are you sure this is"+ value+ " ?")
//                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface dialog, int which) {
//                        // continue with delete
//                    }
//                })
//                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface dialog, int which) {
//                        // do nothing
//                    }
//                })
//                .setIcon(android.R.drawable.ic_dialog_alert)
//                .show();
    }


}

