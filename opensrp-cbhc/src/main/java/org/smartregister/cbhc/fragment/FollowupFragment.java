package org.smartregister.cbhc.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import org.smartregister.cbhc.R;
import org.smartregister.cbhc.application.AncApplication;
import org.smartregister.cbhc.util.Constants;
import org.smartregister.cbhc.util.JsonFormUtils;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.immunization.view.ExpandableHeightGridView;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.smartregister.cbhc.fragment.ProfileOverviewFragment.EXTRA_HOUSEHOLD_DETAILS;

public class FollowupFragment extends BaseProfileFragment {

    private CommonPersonObjectClient householdDetails;

    public static FollowupFragment newInstance(Bundle bundle) {
        Bundle args = bundle;
        FollowupFragment fragment = new FollowupFragment();
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
    public View fragmentView;
    ExpandableHeightGridView formList;
    ExpandableHeightGridView form_history;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        fragmentView = inflater.inflate(R.layout.followup_fragment_overview, container, false);
        formList = (ExpandableHeightGridView) fragmentView.findViewById(R.id.followup_form_list);
        form_history = (ExpandableHeightGridView)fragmentView.findViewById(R.id.form_history);
        formList.setExpanded(true);
        form_history.setExpanded(true);
        form_history.setAdapter(new FormListAdapter(getActivity(),Constants.FOLLOWUP_FORM.getFollowup_forms()));
        formList.setAdapter(new FormListRowAdapter(getActivity(),Constants.FOLLOWUP_FORM.getFollowup_forms()));

        return fragmentView;
    }

    class FormListRowAdapter extends BaseAdapter {
        Context context;
        List<Constants.FOLLOWUP_FORM.FOLLOWUPFORMS>form_list = new ArrayList<Constants.FOLLOWUP_FORM.FOLLOWUPFORMS>();
        public FormListRowAdapter(Context context,List<Constants.FOLLOWUP_FORM.FOLLOWUPFORMS>form_list){

            this.context = context;
            this.form_list = form_list;
        }

        @Override
        public int getCount() {
            return form_list.size();
        }

        @Override
        public Object getItem(int position) {
            return form_list.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            View view = convertView;
            if(view == null){
                view = LayoutInflater.from(context).inflate(R.layout.followup_form_list_row, parent, false);
            }

            String form_name = form_list.get(position).getDisplay_name();
            LinearLayout row_container = (LinearLayout)view.findViewById(R.id.row_container);
            row_container.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    JsonFormUtils.launchFollowUpForm(getActivity(),form_list.get(position).getForm_name());
                }
            });
            TextView form_view = (TextView)view.findViewById(R.id.form_name);

            form_view.setText(form_name);

            return view;
        }
    }
    class FormListAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return form_list.size();
        }

        @Override
        public Object getItem(int position) {
            return form_list.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }
        Context context;
        List<Constants.FOLLOWUP_FORM.FOLLOWUPFORMS>form_list;
        public FormListAdapter(Context context, List<Constants.FOLLOWUP_FORM.FOLLOWUPFORMS>form_list) {

            this.context = context;
            this.form_list = form_list;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            View view = convertView;
            if(view == null){
                view = LayoutInflater.from(context).inflate(R.layout.followup_form_history_row, parent, false);
            }

            String form_name = form_list.get(position).getDisplay_name();
            TextView form_view = (TextView)view.findViewById(R.id.saved_form_name);
            TextView date_view = (TextView)view.findViewById(R.id.saved_form_date);
            form_view.setText(form_name);
            date_view.setText(new Date().toString());


            return view;
        }
    }
    @Override
    protected void onCreation() {

    }

    @Override
    protected void onResumption() {

    }
}
