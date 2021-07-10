package org.smartregister.growplus.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

import org.apache.commons.lang3.StringUtils;
import org.smartregister.growplus.R;
import org.smartregister.growplus.activity.PathJsonFormActivity;
import org.smartregister.growplus.application.VaccinatorApplication;
import org.smartregister.growplus.repository.UniqueIdRepository;
import org.smartregister.util.FormUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;

import util.JsonFormUtils;

@SuppressLint("ValidFragment")
public class HouseholdMemberAddFragment extends DialogFragment {
    private final Context context;
    private static final int REQUEST_CODE_GET_JSON = 3432;
    private static final String TAG = "AddFragment";
    public static final String DIALOG_TAG = "HouseholdMemberAddFragment";
    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy");
    private static final SimpleDateFormat DATE_TIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    String HouseholdEnitityID  = "";
    String locationId;
    private boolean isMotherExist=true;
    org.smartregister.Context opensrpcontext;
    Button addChild;
    private HouseholdMemberAddFragment(Context context) {
        this.context = context;

    }

    public static HouseholdMemberAddFragment newInstance(
            Context context, String locationId, String householdid, org.smartregister.Context context1) {
        HouseholdMemberAddFragment householdMemberAddFragment = new HouseholdMemberAddFragment(context);
        householdMemberAddFragment.HouseholdEnitityID = householdid;
        householdMemberAddFragment.locationId = locationId;
        householdMemberAddFragment.opensrpcontext = context1;
        return householdMemberAddFragment;
    }
    //using to remove "add child" button if any mother not found by this house hold
    public static HouseholdMemberAddFragment newInstance(
            Context context, String locationId, String householdid, org.smartregister.Context context1,boolean isMotherExist) {
        HouseholdMemberAddFragment householdMemberAddFragment = new HouseholdMemberAddFragment(context);
        householdMemberAddFragment.HouseholdEnitityID = householdid;
        householdMemberAddFragment.locationId = locationId;
        householdMemberAddFragment.opensrpcontext = context1;
        householdMemberAddFragment.isMotherExist=isMotherExist;
        Log.d("House_hold_fragment","isMotherExist:"+isMotherExist);
        return householdMemberAddFragment;
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_TITLE, android.R.style.Theme_Holo_Light_Dialog);
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {

        ViewGroup dialogView = (ViewGroup) inflater.inflate(R.layout.household_add_member_dialog_view, container, false);

        addChild= (Button) dialogView.findViewById(R.id.add_child);
        Button addWoman = (Button) dialogView.findViewById(R.id.add_woman);
        Button cancel = (Button) dialogView.findViewById(R.id.cancel);
        updateIsMotherExit(isMotherExist);
        addChild.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("------------","addChild");
                try {
                    JSONObject form = FormUtils.getInstance(getActivity().getApplicationContext()).getFormJson("child_enrollment");

                } catch (Exception e) {
                    e.printStackTrace();
                }

                try {
                    startForm((Activity) context,opensrpcontext,REQUEST_CODE_GET_JSON,"child_enrollment", null, null,locationId,HouseholdEnitityID);
                } catch (Exception e) {
                    e.printStackTrace();
                }
//                ((HouseholdSmartRegisterActivity) getActivity()).startFormActivity("child_enrollment", null, null);
            }
        });

        addWoman.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    JSONObject form = FormUtils.getInstance(getActivity().getApplicationContext()).getFormJson("woman_member_registration");

                } catch (Exception e) {
                    e.printStackTrace();
                }

                try {
                    startForm((Activity) context,opensrpcontext,REQUEST_CODE_GET_JSON,"woman_member_registration", null, null,locationId,HouseholdEnitityID);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        return dialogView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Verify that the host activity implements the callback interface
//        try {
//            // Instantiate the WeightActionListener so we can send events to the host
//            listener = (WeightActionListener) activity;
//        } catch (ClassCastException e) {
//            // The activity doesn't implement the interface, throw exception
//            throw new ClassCastException(activity.toString()
//                    + " must implement WeightActionListener");
//        }
    }
    public void updateIsMotherExit(boolean isMotherExist){
        this.isMotherExist=isMotherExist;
        if(addChild==null)return;
        if(isMotherExist){
            addChild.setVisibility(View.VISIBLE);
        }else{
            addChild.setVisibility(View.GONE);
        }
    }



    @Override
    public void onStart() {
        super.onStart();
        // without a handler, the window sizes itself correctly
        // but the keyboard does not show up
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                getDialog().getWindow().setLayout(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT);

            }
        });

    }
    public static void startForm(Activity context, org.smartregister.Context openSrpContext,
                                 int jsonFormActivityRequestCode,
                                 String formName, String entityId, String metaData,
                                 String currentLocationId,String HouseholdEnitityID) throws Exception {
        Intent intent = new Intent(context, PathJsonFormActivity.class);

        JSONObject form = FormUtils.getInstance(context).getFormJson(formName);
        if (form != null) {
            form.getJSONObject("metadata").put("encounter_location", currentLocationId);

            if (formName.equals("child_enrollment")) {
                if (StringUtils.isBlank(entityId)) {
                    UniqueIdRepository uniqueIdRepo = VaccinatorApplication.getInstance().uniqueIdRepository();
                    entityId = uniqueIdRepo.getNextUniqueId() != null ? uniqueIdRepo.getNextUniqueId().getOpenmrsId() : "";
                    if (entityId.isEmpty()) {
                        Toast.makeText(context, context.getString(R.string.no_openmrs_id), Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
                JSONObject stepOne = form.getJSONObject(JsonFormUtils.STEP1);
                JSONArray jsonArray = stepOne.getJSONArray(JsonFormUtils.FIELDS);
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    if (jsonObject.getString(JsonFormUtils.KEY)
                            .equalsIgnoreCase(JsonFormUtils.OpenMRS_ID)) {
                        jsonObject.remove(JsonFormUtils.VALUE);
                        jsonObject.put(JsonFormUtils.VALUE, entityId);
                        continue;
                    }
                }
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    if (jsonObject.getString(JsonFormUtils.KEY)
                            .equalsIgnoreCase("Mother_Guardian_First_Name")) {
                        jsonObject.put("household_id", HouseholdEnitityID);
                        continue;
                    }
                }

                JsonFormUtils.addHouseholdRegLocHierarchyQuestions(form, openSrpContext);

                String birthFacilityHierarchy = JsonFormUtils.getOpenMrsLocationHierarchy(
                        openSrpContext,currentLocationId ).toString();

                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    if (jsonObject.getString(JsonFormUtils.KEY)
                            .equalsIgnoreCase(JsonFormUtils.ZEIR_ID)) {
                        jsonObject.remove(JsonFormUtils.VALUE);
                        jsonObject.put(JsonFormUtils.VALUE, entityId);
                    }else if(jsonObject.getString(JsonFormUtils.KEY)
                            .equalsIgnoreCase("HIE_FACILITIES")){
                        jsonObject.put(JsonFormUtils.VALUE, birthFacilityHierarchy);
                    }
                }
            } else if (formName.equals("woman_member_registration")) {
                JSONObject metaDataJson = form.getJSONObject("metadata");
                JSONObject lookup = metaDataJson.getJSONObject("look_up");
                lookup.put("entity_id", "household");
                lookup.put("value", HouseholdEnitityID);
                JsonFormUtils.addHouseholdRegLocHierarchyQuestions(form, openSrpContext);

                if (StringUtils.isBlank(entityId)) {
                    UniqueIdRepository uniqueIdRepo = VaccinatorApplication.getInstance().uniqueIdRepository();
                    entityId = uniqueIdRepo.getNextUniqueId() != null ? uniqueIdRepo.getNextUniqueId().getOpenmrsId() : "";
                    if (entityId.isEmpty()) {
                        Toast.makeText(context, context.getString(R.string.no_openmrs_id), Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
                JSONObject stepOne = form.getJSONObject(JsonFormUtils.STEP1);
                JSONArray jsonArray = stepOne.getJSONArray(JsonFormUtils.FIELDS);
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    if (jsonObject.getString(JsonFormUtils.KEY)
                            .equalsIgnoreCase(JsonFormUtils.OpenMRS_ID)) {
                        jsonObject.remove(JsonFormUtils.VALUE);
                        jsonObject.put(JsonFormUtils.VALUE, entityId);
                        continue;
                    }
                }

            } else {
                Log.w(TAG, "Unsupported form requested for launch " + formName);
            }

            intent.putExtra("json", form.toString());
            Log.d(TAG, "form is " + form.toString());
            PathJsonFormActivity.isLaunched = true;
            context.startActivityForResult(intent, jsonFormActivityRequestCode);
        }

    }
}
