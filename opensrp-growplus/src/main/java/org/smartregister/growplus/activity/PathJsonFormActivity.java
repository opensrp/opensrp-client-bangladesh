package org.smartregister.growplus.activity;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;

import com.rengwuxian.materialedittext.MaterialEditText;
import com.shashank.sony.fancydialoglib.Animation;
import com.shashank.sony.fancydialoglib.FancyAlertDialog;
import com.shashank.sony.fancydialoglib.FancyAlertDialogListener;
import com.shashank.sony.fancydialoglib.Icon;
import com.vijay.jsonwizard.activities.JsonFormActivity;
import com.vijay.jsonwizard.constants.JsonFormConstants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.growplus.fragment.PathJsonFormFragment;

import java.util.ArrayList;

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
        refreshCalculateLogic(stepName,key, value);

    }

    @Override
    public void onFormFinish() {
        isLaunched = false;
        super.onFormFinish();
    }

    @Override
    public void refreshConstraints(String parentKey, String childKey) {
        super.refreshConstraints(parentKey,childKey);
        calculatelogicForCheckBox(parentKey);
    }

    private void calculatelogicForCheckBox(String parentKey) {
        try {
            JSONObject object = getStep("step1");
            JSONArray fields = object.getJSONArray("fields");
            for (int i = 0; i < fields.length(); i++) {
                JSONObject questionGroup = fields.getJSONObject(i);
                if (questionGroup.has("type") && questionGroup.getString("type").equalsIgnoreCase("check_box")) {
                    JSONArray checkBoxArray = questionGroup.getJSONArray("options");
                    ArrayList<String> selectedbox = new ArrayList<String>();
                    for (int j = 0; j < checkBoxArray.length(); j++) {
                        if (checkBoxArray.getJSONObject(j).getString("value").equalsIgnoreCase("true")) {
                            String valueOFCheckbox = checkBoxArray.getJSONObject(j).getString("key");
                            selectedbox.add(valueOFCheckbox);
                        }
                    }
                    if (questionGroup.has("has_media_content")) {
                        if (questionGroup.getString("key").equalsIgnoreCase(parentKey)) {
                            if (questionGroup.getBoolean("has_media_content")) {
                                JSONArray medias = questionGroup.getJSONArray("media");
                                for (int j = 0; j < medias.length(); j++) {
                                    JSONObject media = medias.getJSONObject(j);
                                    if (media.has("checkbox_count")) {
                                        if (media.getBoolean("checkbox_count")) {
                                            mediadialog(media, "" + selectedbox.size());
                                        }
                                    } else {
                                        for (int k = 0; k < selectedbox.size(); k++) {
                                            mediadialog(media, "" + selectedbox.get(k));
                                        }
                                    }
                                }
                            }
                        }

                    }
                }
            }
        }catch (Exception e){

        }
    }

    private void refreshCalculateLogic(String stepName,String key, String value) {
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
                String mediatext = media.getString("media_text");

                infodialog(value,mediatype,medialink,mediatext);
            }
        }catch (Exception e){

        }
    }

    private void infodialog(String value, String mediatype, String medialink, String mediatext) {
//        MediaDialogFragment.launchDialog(this,"mediaDialog",mediatype,medialink);
        FancyAlertDialog.Builder builder = new FancyAlertDialog.Builder(this);
        builder.setTitle("Info");
        builder.setBackgroundColor(Color.parseColor("#208CC5")).setPositiveBtnBackground(Color.parseColor("#208CC5"))  //Don't pass R.color.colorvalue
                .setPositiveBtnText("OK").setAnimation(Animation.SLIDE)
                .isCancellable(true)
                .setIcon(com.shashank.sony.fancydialoglib.R.drawable.ic_person_black_24dp, Icon.Visible)
                .OnPositiveClicked(new FancyAlertDialogListener() {
                    @Override
                    public void OnClick() {
                    }
                });
        builder.setMessage(mediatext);
        if(mediatype.equalsIgnoreCase("image")){
            builder.setImagetoshow(medialink);
        }else if (mediatype.equalsIgnoreCase("video")){
            builder.setVideopath(medialink);
        } else if(mediatype.equalsIgnoreCase("text")){

        }
        builder.build();
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

