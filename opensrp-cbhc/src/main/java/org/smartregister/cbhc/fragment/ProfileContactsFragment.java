package org.smartregister.cbhc.fragment;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.google.common.reflect.TypeToken;

import net.sqlcipher.database.SQLiteDatabase;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.cbhc.R;
import org.smartregister.cbhc.application.AncApplication;
import org.smartregister.cbhc.repository.AncRepository;
import org.smartregister.cbhc.util.Constants;
import org.smartregister.cbhc.util.DBConstants;
import org.smartregister.cbhc.util.ImageUtils;
import org.smartregister.cbhc.util.JsonFormUtils;
import org.smartregister.cbhc.util.Utils;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.cursoradapter.SmartRegisterQueryBuilder;
import org.smartregister.domain.Photo;
import org.smartregister.util.AssetHandler;
import org.smartregister.util.FormUtils;
import org.smartregister.view.customcontrols.CustomFontTextView;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.smartregister.cbhc.fragment.ProfileOverviewFragment.EXTRA_HOUSEHOLD_DETAILS;
import static org.smartregister.cbhc.util.JsonFormUtils.DATE_FORMAT;
import static org.smartregister.util.JsonFormUtils.fields;

/**
 * Created by ndegwamartin on 12/07/2018.
 */
public class ProfileContactsFragment extends BaseProfileFragment {

    private CommonPersonObjectClient householdDetails;

    public static ProfileContactsFragment newInstance(Bundle bundle) {
        Bundle args = bundle;
        ProfileContactsFragment fragment = new ProfileContactsFragment();
        if (args == null) {
            args = new Bundle();
        }
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle extras = this.getActivity().getIntent().getExtras();
        if (extras != null) {
            Serializable serializable = extras.getSerializable(EXTRA_HOUSEHOLD_DETAILS);
            if (serializable != null && serializable instanceof CommonPersonObjectClient) {
                householdDetails = (CommonPersonObjectClient) serializable;
                householdDetails.getColumnmaps().putAll(AncApplication.getInstance().getContext().detailsRepository().getAllDetailsForClient(householdDetails.entityId()));

            }
        }

    }

    @Override
    protected void onCreation() {
        //Overriden
    }

    @Override
    protected void onResumption() {
        //Overriden
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View fragmentView = inflater.inflate(R.layout.fragment_profile_contacts, container, false);
        LinearLayout linearLayoutholder = (LinearLayout)fragmentView.findViewById(R.id.profile_overview_details_holder);
        LinearLayout.LayoutParams mainparams =new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);

        try {
            JSONObject form = FormUtils.getInstance(AncApplication.getInstance().getApplicationContext()).getFormJson(Constants.JSON_FORM.Household_REGISTER);
            JSONArray field = fields(form);
            JSONObject i9 = field.getJSONObject(9);
            for(int i=0;i<field.length();i++){
                processPopulatableFieldsForHouseholds(householdDetails.getColumnmaps(),field.getJSONObject(i));
            }
            for(int i = 0;i<field.length();i++){
                if(field.getJSONObject(i).has("hint")) {
                    inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    View view = inflater.inflate(R.layout.overview_list_row, null, false);
                    LinearLayout LayoutForDetailRow = (LinearLayout)view;
//                    LinearLayout LayoutForDetailRow = new LinearLayout(getActivity());
//                    LayoutForDetailRow.setOrientation(LinearLayout.HORIZONTAL);
                    TextView textLabel = (TextView)LayoutForDetailRow.findViewById(R.id.label);
                    TextView textValue = (TextView)LayoutForDetailRow.findViewById(R.id.value);


//                    CustomFontTextView textLabel = new CustomFontTextView(getActivity());
                    textLabel.setTextSize(15);
//                    CustomFontTextView textValue = new CustomFontTextView(getActivity());
                    textValue.setTextSize(15);
                    textLabel.setText(field.getJSONObject(i).getString("hint"));
                    textLabel.setSingleLine(false);
                    if(field.getJSONObject(i).has(JsonFormUtils.VALUE)) {
                        String value = field.getJSONObject(i).getString(JsonFormUtils.VALUE);
                        value = processLocationValue(value);
                        textValue.setText(value);
                    }
                                        textValue.setSingleLine(false);
//                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
//                    params.weight = 1;
//                    params.setMargins(5, 5, 5, 5);
//                    LayoutForDetailRow.addView(textLabel, params);
//                    LayoutForDetailRow.addView(textValue, params);
//                    linearLayoutholder.addView(LayoutForDetailRow, mainparams);
                    linearLayoutholder.addView(LayoutForDetailRow);
                }
            }



//            for(int i = 0;i<field.length();i++){
//                if(field.getJSONObject(i).has("openmrs_entity")) {
//                    if(field.getJSONObject(i).getString("openmrs_entity").equalsIgnoreCase("person_attribute")){
//                        if(field.getJSONObject(i).has("hint")) {
//                            LinearLayout LayoutForDetailRow = new LinearLayout(getActivity());
//                            LayoutForDetailRow.setOrientation(LinearLayout.HORIZONTAL);
//                            CustomFontTextView textLabel = new CustomFontTextView(getActivity());
//                            textLabel.setTextSize(15);
//                            CustomFontTextView textValue = new CustomFontTextView(getActivity());
//                            textValue.setTextSize(15);
//                            textLabel.setText(field.getJSONObject(i).getString("hint"));
//                            textLabel.setSingleLine(false);
//                            textValue.setText(householdDetails.getColumnmaps().get(field.getJSONObject(i).getString(JsonFormUtils.OPENMRS_ENTITY_ID)));
//                            textValue.setSingleLine(false);
//                            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
//                            params.weight = 1;
//                            params.setMargins(5, 5, 5, 5);
//                            LayoutForDetailRow.addView(textLabel, params);
//                            LayoutForDetailRow.addView(textValue, params);
//                            linearLayoutholder.addView(LayoutForDetailRow, mainparams);
//                        }
//                    }else if(field.getJSONObject(i).getString("openmrs_entity").equalsIgnoreCase("person")){
//                        if(field.getJSONObject(i).has("hint")) {
//                            LinearLayout LayoutForDetailRow = new LinearLayout(getActivity());
//                            LayoutForDetailRow.setOrientation(LinearLayout.HORIZONTAL);
//                            CustomFontTextView textLabel = new CustomFontTextView(getActivity());
//                            textLabel.setTextSize(15);
//                            CustomFontTextView textValue = new CustomFontTextView(getActivity());
//                            textValue.setTextSize(15);
//                            textLabel.setText(field.getJSONObject(i).getString("hint"));
//                            textLabel.setSingleLine(false);
//                            textValue.setText(householdDetails.getColumnmaps().get(field.getJSONObject(i).getString(JsonFormUtils.OPENMRS_ENTITY_ID)));
//                            textValue.setSingleLine(false);
//                            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
//                            params.weight = 1;
//                            params.setMargins(5, 5, 5, 5);
//                            LayoutForDetailRow.addView(textLabel, params);
//                            LayoutForDetailRow.addView(textValue, params);
//                            linearLayoutholder.addView(LayoutForDetailRow, mainparams);
//                        }
//                    }
//                    }
//                if(field.getJSONObject(i).has("hint")) {
//                    if(field.getJSONObject(i).has("hint")) {
//
//                        LinearLayout LayoutForDetailRow = new LinearLayout(getActivity());
//                        LayoutForDetailRow.setOrientation(LinearLayout.HORIZONTAL);
//                        CustomFontTextView textLabel = new CustomFontTextView(getActivity());
//                        textLabel.setTextSize(15);
//                        CustomFontTextView textValue = new CustomFontTextView(getActivity());
//                        textValue.setTextSize(15);
//                        textLabel.setText(field.getJSONObject(i).getString("hint"));
//                        textLabel.setSingleLine(false);
//                        textValue.setText(householdDetails.getColumnmaps().get(field.getJSONObject(i).getString(JsonFormUtils.KEY)));
//                        textValue.setSingleLine(false);
//                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
//                        params.weight = 1;
//                        params.setMargins(5, 5, 5, 5);
//                        LayoutForDetailRow.addView(textLabel, params);
//                        LayoutForDetailRow.addView(textValue, params);
//                        linearLayoutholder.addView(LayoutForDetailRow, mainparams);
//                    }
//
//                }
//
//            }

        }catch (Exception e){
            Log.e("ereor",e.getMessage());
        }
        return fragmentView;
    }

    private String processLocationValue(String value) {
        if(value.contains("[")){
            value = value.replace("[","").replace("]","");
            if(value.contains(",")){
                value = value.split(",")[value.split(",").length-1];
                if(value.contains("\"")){
                    value = value.replace("\"","");
                }
            }
        }
        return value;
    }

    public static void processPopulatableFieldsForHouseholds(Map<String, String> womanClient, JSONObject jsonObject) throws JSONException {


        if (jsonObject.getString(JsonFormUtils.KEY).equalsIgnoreCase(DBConstants.KEY.DOB) && !Boolean.valueOf(womanClient.get(DBConstants.KEY.DOB_UNKNOWN))) {

            String dobString = womanClient.get(DBConstants.KEY.DOB);
            if(dobString==null||dobString.isEmpty()){
                dobString = "1980-01-01T05:53:20.000+05:53:20";
            }
            Date dob = Utils.dobStringToDate(dobString);
            if (dob != null) {
                jsonObject.put(JsonFormUtils.VALUE, DATE_FORMAT.format(dob));
            }

        } else if (jsonObject.getString(JsonFormUtils.KEY).equalsIgnoreCase(DBConstants.KEY.HOME_ADDRESS)) {

            String homeAddress = womanClient.get(DBConstants.KEY.HOME_ADDRESS);
            jsonObject.put(JsonFormUtils.VALUE, homeAddress);
            jsonObject.toString();

            List<String> healthFacilityHierarchy = new ArrayList<>();
            String address5 = womanClient.get(DBConstants.KEY.HOME_ADDRESS);
            healthFacilityHierarchy.add(address5);

            String schoolFacilityHierarchyString = AssetHandler.javaToJsonString(healthFacilityHierarchy, new TypeToken<List<String>>() {
            }.getType());

            if (StringUtils.isNotBlank(schoolFacilityHierarchyString)) {
                jsonObject.put(JsonFormUtils.VALUE, schoolFacilityHierarchyString);
            }

        } else if (jsonObject.getString(JsonFormUtils.KEY).equalsIgnoreCase(Constants.KEY.PHOTO)) {

            Photo photo = ImageUtils.profilePhotoByClientID(womanClient.get(DBConstants.KEY.BASE_ENTITY_ID));

            if (StringUtils.isNotBlank(photo.getFilePath())) {

                jsonObject.put(JsonFormUtils.VALUE, photo.getFilePath());

            }
        } else if (jsonObject.getString(JsonFormUtils.KEY).equalsIgnoreCase(DBConstants.KEY.DOB_UNKNOWN)) {

            jsonObject.put(JsonFormUtils.READ_ONLY, false);
            JSONObject optionsObject = jsonObject.getJSONArray(Constants.JSON_FORM_KEY.OPTIONS).getJSONObject(0);
            optionsObject.put(JsonFormUtils.VALUE, womanClient.get(DBConstants.KEY.DOB_UNKNOWN));

        } else if (jsonObject.getString(JsonFormUtils.KEY).equalsIgnoreCase(DBConstants.KEY.AGE)) {

            jsonObject.put(JsonFormUtils.READ_ONLY, false);
            jsonObject.put(JsonFormUtils.VALUE, Utils.getAgeFromDate(womanClient.get(DBConstants.KEY.DOB)));

        } else if (jsonObject.getString(JsonFormUtils.KEY).equalsIgnoreCase(DBConstants.KEY.ANC_ID)) {

            jsonObject.put(JsonFormUtils.VALUE, womanClient.get(DBConstants.KEY.ANC_ID).replace("-", ""));

        } else if (womanClient.containsKey(jsonObject.getString(JsonFormUtils.KEY))) {
            String keyname = jsonObject.getString(JsonFormUtils.KEY);
            keyname = processAttributesForEdit(jsonObject,keyname);
            String value = womanClient.get(keyname);
            value = processValueWithChoiceIds(jsonObject,value);
            jsonObject.put(JsonFormUtils.READ_ONLY, false);
            jsonObject.put(JsonFormUtils.VALUE, value);
        } else {
            String keyname = jsonObject.getString(JsonFormUtils.KEY);
            keyname = processAttributesForEdit(jsonObject,keyname);
            String value = womanClient.get(keyname);
            if(value!=null) {
                value = processValueWithChoiceIds(jsonObject, value);
                jsonObject.put(JsonFormUtils.READ_ONLY, false);
                jsonObject.put(JsonFormUtils.VALUE, value);
            }
        }
    }

    private static String processValueWithChoiceIds(JSONObject jsonObject,String value) {
        try {
            if(jsonObject.has("openmrs_choice_ids")){
                JSONObject choiceObject = jsonObject.getJSONObject("openmrs_choice_ids");

                for(int i = 0; i<choiceObject.names().length(); i++){
                    if(value.equalsIgnoreCase(choiceObject.getString(choiceObject.names().getString(i)))){
                        value = choiceObject.names().getString(i);
                    }
                }


            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return value;
    }

    private static String processAttributesForEdit(JSONObject jsonObject,String keyname) {
        if(jsonObject.has("openmrs_entity")){
            try {
                if(jsonObject.getString("openmrs_entity").equalsIgnoreCase("person_attribute")
                        ||jsonObject.getString("openmrs_entity").equalsIgnoreCase("person")
                        ){
                    String attributename = jsonObject.getString("openmrs_entity_id");
                    keyname = attributename;
                }
                if(jsonObject.getString("openmrs_entity").equalsIgnoreCase("person_address")
                        &&jsonObject.getString("openmrs_entity_id").equalsIgnoreCase("address7")
                        ){
                    String attributename = jsonObject.getString("openmrs_entity_id");
                    keyname = attributename;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
        return keyname;
    }
}
