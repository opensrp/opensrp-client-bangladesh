package org.smartregister.cbhc.activity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;

import com.vijay.jsonwizard.activities.JsonFormActivity;
import com.vijay.jsonwizard.constants.JsonFormConstants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.cbhc.application.AncApplication;
import org.smartregister.cbhc.domain.draft_form_object;
import org.smartregister.cbhc.fragment.AncJsonFormFragment;
import org.smartregister.cbhc.repository.DraftFormRepository;
import org.smartregister.cbhc.repository.HealthIdRepository;
import org.smartregister.cbhc.repository.UniqueIdRepository;
import org.smartregister.cbhc.util.DBConstants;
import org.smartregister.cbhc.util.JsonFormUtils;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.smartregister.cbhc.util.JsonFormUtils.METADATA;
import static org.smartregister.util.JsonFormUtils.getJSONObject;


/**
 * Created by ndegwamartin on 30/06/2018.
 */
public class AncJsonFormActivity extends JsonFormActivity {

    private static final String TAG = AncJsonFormActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        showform();
    }

    @Override
    public void initializeFormFragment() {
        initializeFormFragmentCore();
    }

    @Override
    public void writeValue(String stepName, String key, String value, String openMrsEntityParent, String openMrsEntity, String openMrsEntityId) throws JSONException {
        callSuperWriteValue(stepName, key, value, openMrsEntityParent, openMrsEntity, openMrsEntityId);
        calculateAgeFromBirthDate(key,value);
    }

    @Override
    public void onFormFinish() {
        callSuperFinish();
    }

    protected void callSuperFinish() {
        super.onFormFinish();
    }

    protected void callSuperWriteValue(String stepName, String key, String value, String openMrsEntityParent, String openMrsEntity, String openMrsEntityId) throws JSONException {
        super.writeValue(stepName, key, value, openMrsEntityParent, openMrsEntity, openMrsEntityId);

    }



    private void calculateAgeFromBirthDate(String key, String value) {
        if(key.equalsIgnoreCase("member_birth_date")&& value!=null){
            JSONObject currentFormState = getmJSONObject();
            try {
                currentFormState.getJSONObject("step1");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    protected void initializeFormFragmentCore() {
        AncJsonFormFragment ancJsonFormFragment = AncJsonFormFragment.getFormFragment(JsonFormConstants.FIRST_STEP_NAME);
        getSupportFragmentManager().beginTransaction()
                .add(com.vijay.jsonwizard.R.id.container, ancJsonFormFragment).commit();
    }

    @Override
    public void onBackPressed() {
        final String dialog_message = getConfirmCloseMessage().replace("cleared","saved in Draft");
        AlertDialog dialog = new AlertDialog.Builder(this, com.vijay.jsonwizard.R.style.AppThemeAlertDialog)
                .setTitle(getConfirmCloseTitle())
                .setMessage(dialog_message)
                .setNegativeButton(com.vijay.jsonwizard.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        saveDraft();
                        finish();
                    }
                })
                .setPositiveButton(com.vijay.jsonwizard.R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.d(TAG, "No button on dialog in " + JsonFormActivity.class.getCanonicalName());


                    }
                })
                .create();

        dialog.show();
    }
    private UniqueIdRepository uniqueIdRepository;
    private HealthIdRepository healthIdRepository;
    private void saveDraft() {
        JSONObject partialform = getmJSONObject();

        DraftFormRepository draftFormRepository = new DraftFormRepository(AncApplication.getInstance().getRepository());
        draft_form_object draftFormObject = new draft_form_object();
        draftFormObject.setDraftFormJson(currentJsonState());
        processDraftForm(partialform,draftFormObject);
        draftFormRepository.add(draftFormObject);
    }
    public UniqueIdRepository getUniqueIdRepository() {
        if (uniqueIdRepository == null) {
            uniqueIdRepository = AncApplication.getInstance().getUniqueIdRepository();
        }
        return uniqueIdRepository;
    }
    public HealthIdRepository getHealthIdRepository() {
        if (healthIdRepository == null) {
            healthIdRepository = AncApplication.getInstance().getHealthIdRepository();
        }
        return healthIdRepository;
    }
    private void processDraftForm(JSONObject partialform, draft_form_object draftFormObject) {
        try {
            String formname = partialform.getString("encounter_type");
            draftFormObject.setFormNAME(formname);



            JSONObject metadata = getJSONObject(partialform, METADATA);
            JSONObject lookUpJSONObject = getJSONObject(metadata, "look_up");
            String lookUpEntityId = "";
            String lookUpBaseEntityId = "";
            if (lookUpJSONObject != null) {
                lookUpEntityId = JsonFormUtils.getString(lookUpJSONObject, "entity_id");
                lookUpBaseEntityId = JsonFormUtils.getString(lookUpJSONObject, "value");
            }
            if(!isBlank(lookUpEntityId)&& !isBlank(lookUpBaseEntityId)){
                draftFormObject.setHousehold_BASE_ENTITY_ID(lookUpBaseEntityId);
            }
            String newOpenSRPId = getPatientIdentifier(partialform);

            if(formname.contains("Household")){
                uniqueIdRepository = getUniqueIdRepository();
                uniqueIdRepository.close(newOpenSRPId);
            }else{
                healthIdRepository = getHealthIdRepository();
                healthIdRepository.close(newOpenSRPId);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public String getPatientIdentifier(JSONObject formobject){
        String identifier = "";
        try{

            JSONArray jsonArray = formobject.getJSONObject("step1").getJSONArray("fields");
            for(int i=0;i<jsonArray.length();i++){
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                String key = jsonObject.getString("key");

                if(key.equalsIgnoreCase("Patient_Identifier")){
                    identifier = jsonObject.getString("value");
                    break;
                }

            }
        }catch(Exception e){

        }
        return identifier;
    }


}

