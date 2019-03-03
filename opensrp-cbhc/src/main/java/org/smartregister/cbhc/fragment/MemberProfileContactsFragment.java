package org.smartregister.cbhc.fragment;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;
import org.smartregister.cbhc.R;
import org.smartregister.cbhc.application.AncApplication;
import org.smartregister.cbhc.util.Constants;
import org.smartregister.cbhc.util.JsonFormUtils;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.util.FormUtils;
import org.smartregister.view.customcontrols.CustomFontTextView;

import java.io.Serializable;

import static org.smartregister.cbhc.fragment.ProfileContactsFragment.processPopulatableFieldsForHouseholds;
import static org.smartregister.cbhc.fragment.ProfileOverviewFragment.EXTRA_HOUSEHOLD_DETAILS;
import static org.smartregister.util.JsonFormUtils.fields;

/**
 * Created by ndegwamartin on 12/07/2018.
 */
public class MemberProfileContactsFragment extends BaseProfileFragment {

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

    LayoutInflater inflater;
    View fragmentView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        fragmentView = inflater.inflate(R.layout.fragment_profile_contacts, container, false);
        this.inflater = inflater;
        setupView();
        return fragmentView;
    }

    public void reloadView() {
        householdDetails.getColumnmaps().putAll(AncApplication.getInstance().getContext().detailsRepository().getAllDetailsForClient(householdDetails.entityId()));
        LinearLayout linearLayoutholder = (LinearLayout)fragmentView.findViewById(R.id.profile_overview_details_holder);
        linearLayoutholder.removeAllViews();
        setupView();
    }

    public void setupView() {
        LinearLayout linearLayoutholder = (LinearLayout)fragmentView.findViewById(R.id.profile_overview_details_holder);
        LinearLayout.LayoutParams mainparams =new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        try {
            JSONObject form = FormUtils.getInstance(AncApplication.getInstance().getApplicationContext()).getFormJson(Constants.JSON_FORM.MEMBER_REGISTER);
            JSONArray field = fields(form);
            for(int i=0;i<field.length();i++){
                processPopulatableFieldsForHouseholds(householdDetails.getColumnmaps(),field.getJSONObject(i));
            }
            for(int i = 0;i<field.length();i++) {
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
                        textValue.setText(field.getJSONObject(i).getString(JsonFormUtils.VALUE));
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

        } catch (Exception e){

        }
    }
}
