package org.smartregister.cbhc.fragment;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import org.smartregister.cbhc.util.JsonFormUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.cbhc.R;
import org.smartregister.cbhc.application.AncApplication;
import org.smartregister.cbhc.util.Constants;
import org.smartregister.cbhc.util.DBConstants;
import org.smartregister.cbhc.util.JsonFormUtils;
import org.smartregister.cbhc.util.Utils;
import org.smartregister.commonregistry.CommonPersonObject;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.util.FormUtils;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.smartregister.cbhc.fragment.ProfileContactsFragment.processPopulatableFieldsForHouseholds;
import static org.smartregister.cbhc.fragment.ProfileOverviewFragment.EXTRA_HOUSEHOLD_DETAILS;
import static org.smartregister.util.JsonFormUtils.fields;

/**
 * Created by ndegwamartin on 12/07/2018.
 */
public class MemberProfileContactsFragment extends BaseProfileFragment {

    LayoutInflater inflater;
    View fragmentView;
    String typeofMember;
    private CommonPersonObjectClient householdDetails;

    public static MemberProfileContactsFragment newInstance(Bundle bundle) {
        Bundle args = bundle;
        MemberProfileContactsFragment fragment = new MemberProfileContactsFragment();
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
                typeofMember = extras.getString("type_of_member");
                setUpMemberDetails(typeofMember);
                //  householdDetails.getColumnmaps().putAll(AncApplication.getInstance().getContext().detailsRepository().getAllDetailsForClient(householdDetails.entityId()));

            }
        }

    }
    private CommonPersonObjectClient CommonPersonObjectToClient(CommonPersonObject commonPersonObject,String tableName) {
        CommonPersonObjectClient commonPersonObjectClient = new CommonPersonObjectClient(commonPersonObject.getCaseId(), commonPersonObject.getDetails(), tableName);
        commonPersonObjectClient.setColumnmaps(commonPersonObject.getColumnmaps());
        return commonPersonObjectClient;
    }

    public void setUpMemberDetails(String typeofMember){
        if (typeofMember != null) {
            if (typeofMember.equalsIgnoreCase("malechild") || typeofMember.equalsIgnoreCase("femalechild")) {
                CommonPersonObject commonPersonObject = AncApplication.getInstance().getContext().commonrepository(DBConstants.CHILD_TABLE_NAME).findByBaseEntityId(householdDetails.entityId());
                householdDetails =  CommonPersonObjectToClient(commonPersonObject,DBConstants.CHILD_TABLE_NAME);
            }
            else if (typeofMember.equalsIgnoreCase("woman")) {
                householdDetails =  CommonPersonObjectToClient(AncApplication.getInstance().getContext().commonrepository(DBConstants.WOMAN_TABLE_NAME).findByBaseEntityId(householdDetails.entityId()),DBConstants.WOMAN_TABLE_NAME);
            }
            else if (typeofMember.equalsIgnoreCase("member")) {
                householdDetails =  CommonPersonObjectToClient(AncApplication.getInstance().getContext().commonrepository(DBConstants.MEMBER_TABLE_NAME).findByBaseEntityId(householdDetails.entityId()),DBConstants.MEMBER_TABLE_NAME);
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

        fragmentView = inflater.inflate(R.layout.fragment_profile_contacts, container, false);
        this.inflater = inflater;
        setupView();
        return fragmentView;
    }

    public void reloadView() {
  //      householdDetails.getColumnmaps().putAll(AncApplication.getInstance().getContext().detailsRepository().getAllDetailsForClient(householdDetails.entityId()));
        setUpMemberDetails(typeofMember);
        LinearLayout linearLayoutholder = fragmentView.findViewById(R.id.profile_overview_details_holder);
        linearLayoutholder.removeAllViews();
        setupView();
    }

    public void processCheckboxValues(Map<String, String> womanClient, JSONArray field) {

        for (int i = 0; i < field.length(); i++) {
            try {
                JSONObject object = field.getJSONObject(i);
                if (object.has("type") && object.getString("type").equals("check_box")) {
                    String key = object.getString("key");
                    String openmrs_id = object.getString("openmrs_entity_id");
                    String value = "";
                    if (womanClient.get(key) != null) {
                        value = womanClient.get(key);
                    } else if (womanClient.get(openmrs_id) != null) {
                        value = womanClient.get(openmrs_id);
                    }
                    String[] vals = value.split(",");
                    HashMap<String, String> options_map = new HashMap<String, String>();
                    JSONArray options = object.getJSONArray("options");
                    if (options != null) {
                        for (int k = 0; k < options.length(); k++) {
                            JSONObject option_object = options.getJSONObject(k);
                            options_map.put(option_object.getString("key"), option_object.getString("text"));
                        }
                    }
                    String val = "";
                    if (!ArrayUtils.isEmpty(vals)) {
                        for (int k = 0; k < vals.length; k++) {
                            val = val + options_map.get(vals[k]) + ",";
                        }
                    }
                    value = val;

                    if (value.endsWith(",")) {
                        value = value.substring(0, value.length() - 1);
                    }
                    if (value.equalsIgnoreCase("null")) {
                        value = "";
                    }
                    long days = -1;
                    if (ProfileContactsFragment.date_of_birth != null) {
                        long diff = ProfileContactsFragment.date_of_birth.getTime() - new Date().getTime();
                        days = TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);
                    }

                    if (ProfileContactsFragment.age >= 5) {
                        if (key.equalsIgnoreCase("Disease_status_zero_to_two_month_by_age") || key.equalsIgnoreCase("Disease_status_two_month_to_five_year_by_age")) {
                            value = "";
                        }
                    } else if ((ProfileContactsFragment.age >= 1 && ProfileContactsFragment.age < 5) || (days >= 62 && days < 1826)) {
                        if (key.equalsIgnoreCase("Disease_status_zero_to_two_month_by_age") ||
                                key.equalsIgnoreCase("Non Communicable Disease") ||
                                key.equalsIgnoreCase("Communicable Disease") ||
                                key.equalsIgnoreCase("Disease_Type")) {
                            value = "";
                        }
                    } else {
                        if (key.equalsIgnoreCase("Disease_status_two_month_to_five_year_by_age") ||
                                key.equalsIgnoreCase("Non Communicable Disease") ||
                                key.equalsIgnoreCase("Communicable Disease") ||
                                key.equalsIgnoreCase("Disease_Type")) {
                            value = "";
                        }
                    }
                    object.put("value", value);
                }
            } catch (JSONException e) {
                Utils.appendLog(getClass().getName(), e);
                e.printStackTrace();
            }

        }
    }

    public void setPregnantStatus(Map<String, String> clientmap) {
        String pregnant_status = clientmap.get("PregnancyStatus");
        if (pregnant_status == null || (pregnant_status != null && pregnant_status.isEmpty())) {
            clientmap.put("LMP", "");
            clientmap.put("delivery_date", "");
        } else {
            if (pregnant_status.equalsIgnoreCase("প্রসব পূর্ব") || pregnant_status.equalsIgnoreCase("Antenatal Period")) {
                clientmap.put("delivery_date", "");
                clientmap.put("familyplanning", "");
            } else if (pregnant_status.equalsIgnoreCase("প্রসবোত্তর") || pregnant_status.equalsIgnoreCase("Postnatal")) {
                clientmap.put("LMP", "");
            } else {
                clientmap.put("LMP", "");
                clientmap.put("delivery_date", "");
            }
        }
    }

    public void setupView() {
        LinearLayout linearLayoutholder = fragmentView.findViewById(R.id.profile_overview_details_holder);
        LinearLayout.LayoutParams mainparams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        try {
            JSONObject form = FormUtils.getInstance(AncApplication.getInstance().getApplicationContext()).getFormJson(Constants.JSON_FORM.MEMBER_REGISTER);
            JSONArray field = fields(form);
            setPregnantStatus(householdDetails.getColumnmaps());
            ProfileContactsFragment.date_of_birth = null;
            ProfileContactsFragment.age = -1;

            for (int i = 0; i < field.length(); i++) {
                processPopulatableFieldsForHouseholds(householdDetails.getColumnmaps(), field.getJSONObject(i));
            }

            processCheckboxValues(householdDetails.getColumnmaps(), field);
            //processDiseaseStatus(householdDetails.getColumnmaps(),field);

            for (int i = 0; i < field.length(); i++) {
                if (field.getJSONObject(i).has("hint") || field.getJSONObject(i).has("label")) {
                    inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    View view = inflater.inflate(R.layout.overview_list_row, null, false);
                    LinearLayout LayoutForDetailRow = (LinearLayout) view;
//                    LinearLayout LayoutForDetailRow = new LinearLayout(getActivity());
//                    LayoutForDetailRow.setOrientation(LinearLayout.HORIZONTAL);
                    TextView textLabel = LayoutForDetailRow.findViewById(R.id.label);
                    TextView textValue = LayoutForDetailRow.findViewById(R.id.value);
                    textValue.setGravity(Gravity.LEFT);
//                    CustomFontTextView textLabel = new CustomFontTextView(getActivity());
                    textLabel.setTextSize(15);
//                    CustomFontTextView textValue = new CustomFontTextView(getActivity());
                    textValue.setTextSize(15);
                    String hint = "";
                    if (field.getJSONObject(i).has("hint")) {
                        hint = field.getJSONObject(i).getString("hint");
                    } else if (field.getJSONObject(i).has("label")) {
                        hint = field.getJSONObject(i).getString("label");
                    }

                    textLabel.setText(hint);
                    textLabel.setSingleLine(false);
                    String VALUE = "";
                    if (field.getJSONObject(i).has(JsonFormUtils.VALUE)) {
                        VALUE = field.getJSONObject(i).getString(JsonFormUtils.VALUE);
                        textValue.setText(VALUE);
                    }

                    String KEY = "";
                    if (field.getJSONObject(i).has(JsonFormUtils.KEY)) {
                        KEY = field.getJSONObject(i).getString(JsonFormUtils.KEY);
                    }

                    textValue.setSingleLine(false);
//                    if(KEY.equalsIgnoreCase("Disease_status_female_by_age")){
//                        String k = KEY;
//                        System.out.println(k);
//                    }

//                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
//                    params.weight = 1;
//                    params.setMargins(5, 5, 5, 5);
//                    LayoutForDetailRow.addView(textLabel, params);
//                    LayoutForDetailRow.addView(textValue, params);
//                    linearLayoutholder.addView(LayoutForDetailRow, mainparams);
                    if (!removeField(KEY, VALUE)) {
                        if ((hint.contains("অন্যান্য") && VALUE.isEmpty())) {

                        } else {
                            linearLayoutholder.addView(LayoutForDetailRow);
                        }
                    }

                }
            }

        } catch (Exception e) {
            Utils.appendLog(getClass().getName(), e);

        }
    }

    private void processDiseaseStatus(Map<String, String> columnmaps, JSONArray field) {
        String dateString = columnmaps.get("birthdate");
        int age = 0;
        Date date = new Date(dateString);
        for (int i = 0; i < field.length(); i++) {

        }
    }

    public boolean removeField(String KEY, String VALUE) {
        String[] keys = {"Child_birth_weight", "Birth_weight", "Used_7_1_Chlorohexidin", "marital_status", "spouseName_english",
                "spouseName_bengali", "contact_phone_number_by_age",
                "educational_qualification_by_age", "occupation_by_age", "pregnant_status", "lmp_date", "Delivery_date", "family_planning", "risky_habits",
                "Professional technical professionals", "Semi-skilled labor service", "Unskilled labor", "Factory worker, blue collar service", "Home based manufacturing",
                "Business", "Domestic Servant", "member_NID", "member_BRID", "Citizen_Card_number", "member_f_name_bengali", "Mother_Guardian_First_Name_bengali", "Father_Guardian_First_Name_bengali",
                "spouseName_bengali", "disability_type", "Occupation_Category", "Disease_Type", "Communicable Disease", "Non Communicable Disease",
                "Disease_status_zero_to_two_month_by_age", "Disease_status_two_month_to_five_year_by_age", "comments", "illness_information"};
        return ArrayUtils.contains(keys, KEY) && VALUE.isEmpty();

    }
}
