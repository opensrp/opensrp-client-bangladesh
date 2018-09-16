package org.smartregister.cbhc.fragment;

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
import org.smartregister.cbhc.util.DBConstants;
import org.smartregister.cbhc.util.JsonFormUtils;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.util.FormUtils;
import org.smartregister.view.customcontrols.CustomFontTextView;

import java.io.Serializable;

import static org.smartregister.cbhc.fragment.ProfileOverviewFragment.EXTRA_HOUSEHOLD_DETAILS;
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
        try {
            JSONObject form = FormUtils.getInstance(AncApplication.getInstance().getApplicationContext()).getFormJson(Constants.JSON_FORM.Household_REGISTER);
            JSONArray field = fields(form);
            for(int i = 0;i<field.length();i++){
                if(field.getJSONObject(i).has("hint")) {
                    LinearLayout LayoutForDetailRow = new LinearLayout(getActivity());
                    LayoutForDetailRow.setOrientation(LinearLayout.HORIZONTAL);
                    CustomFontTextView textLabel = new CustomFontTextView(getActivity());
                    textLabel.setTextSize(15);
                    CustomFontTextView textValue = new CustomFontTextView(getActivity());
                    textValue.setTextSize(15);

                    textLabel.setText(field.getJSONObject(i).getString("hint"));
                    textLabel.setSingleLine(false);
                    textValue.setText(householdDetails.getColumnmaps().get(field.getJSONObject(i).getString(JsonFormUtils.KEY)));
                    textValue.setSingleLine(false);
//                    textValue.setBackgroundColor(getResources().getColor(R.color.refer_close_red));

                    LinearLayout.LayoutParams params =new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
                    params.weight = 1;
                    params.setMargins(5,5,5,5);

                    LayoutForDetailRow.addView(textLabel,params);
                    LayoutForDetailRow.addView(textValue,params);

                    linearLayoutholder.addView(LayoutForDetailRow);
                }

            }

        }catch (Exception e){

        }
        return fragmentView;
    }
}
