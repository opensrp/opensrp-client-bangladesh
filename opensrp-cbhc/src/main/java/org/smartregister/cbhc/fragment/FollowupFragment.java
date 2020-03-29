package org.smartregister.cbhc.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.smartregister.cbhc.R;
import org.smartregister.cbhc.application.AncApplication;
import org.smartregister.cbhc.util.Constants;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.immunization.view.ExpandableHeightGridView;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import org.smartregister.cbhc.util.JsonFormUtils;
import static org.smartregister.cbhc.fragment.ProfileOverviewFragment.EXTRA_HOUSEHOLD_DETAILS;
import static org.smartregister.cbhc.util.Constants.FOLLOWUP_FORM.Followup_Form_MHV_ANC;
import static org.smartregister.cbhc.util.Constants.FOLLOWUP_FORM.Followup_Form_MHV_DS_Female;
import static org.smartregister.cbhc.util.Constants.FOLLOWUP_FORM.Followup_Form_MHV_DS_Male;
import static org.smartregister.cbhc.util.Constants.FOLLOWUP_FORM.Followup_Form_MHV_DS_NewBorn;
import static org.smartregister.cbhc.util.Constants.FOLLOWUP_FORM.Followup_Form_MHV_DS_Toddler;
import static org.smartregister.cbhc.util.Constants.FOLLOWUP_FORM.Followup_Form_MHV_FP;
import static org.smartregister.cbhc.util.Constants.FOLLOWUP_FORM.Followup_Form_MHV_PNC;

public class FollowupFragment extends BaseProfileFragment {

    public View fragmentView;
    int age = -1;
    int gender = -1;
    int marital_status = 0;
    int pregnant_status = 0;
    ExpandableHeightGridView formList;
    ExpandableHeightGridView form_history;
    ArrayList<Constants.FOLLOWUP_FORM.FOLLOWUPFORMS> active_forms;
    FormListRowAdapter formListRowAdapter;
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

    public void notifyAdapter() {
        householdDetails.getColumnmaps().putAll(AncApplication.getInstance().getContext().detailsRepository().getAllDetailsForClient(householdDetails.entityId()));
        if (formListRowAdapter != null) {
            active_forms = getactivateforms();
            formListRowAdapter = new FormListRowAdapter(getActivity(), active_forms);
            formList.setAdapter(formListRowAdapter);
            formListRowAdapter.notifyDataSetChanged();
            form_history.setAdapter(new FormListAdapter(getActivity(), active_forms));
        }

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        fragmentView = inflater.inflate(R.layout.followup_fragment_overview, container, false);
        formList = fragmentView.findViewById(R.id.followup_form_list);
        form_history = fragmentView.findViewById(R.id.form_history);
        formList.setExpanded(true);
        form_history.setExpanded(true);
        active_forms = getactivateforms();
        formListRowAdapter = new FormListRowAdapter(getActivity(), active_forms);
        formList.setAdapter(formListRowAdapter);
        form_history.setAdapter(new FormListAdapter(getActivity(), active_forms));


        return fragmentView;
    }

    @Override
    protected void onCreation() {

    }

    @Override
    protected void onResumption() {

    }

    private int getPregnantStatus() {
        String ps = householdDetails.getColumnmaps().get("PregnancyStatus");

        if ((ps != null && (ps.equalsIgnoreCase("প্রসব পূর্ব") || ps.equalsIgnoreCase("Antenatal Period"))))
            return 1;
        if ((ps != null && (ps.equalsIgnoreCase("প্রসবোত্তর") || ps.contains("Postnatal"))))
            return 2;
        //Antenatal Period,Postnatal
        return 0;
    }

    public int getGender() {
        String gender = householdDetails.getColumnmaps().get("gender");
        if (gender == null)
            return -1;
        return gender.equals("M") ? 1 : 0;
    }

    public int getAge() {
        String age = householdDetails.getColumnmaps().get("age");
        String dob = householdDetails.getColumnmaps().get("dob");
        if (dob != null && dob.contains("T")) {
            dob = dob.substring(0, dob.indexOf('T'));
        }
        if (dob != null) {
            try {
                Date dateob = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).parse(dob);
//                Date dateob = new Date(dob);
                if (dateob != null) {
                    long time = new Date().getTime() - dateob.getTime();
                    long TWO_MONTHS = 62l * 24l * 60l * 60l * 1000l;
                    double YEAR = 365d * 24d * 60d * 60d * 1000d;
                    if (time <= TWO_MONTHS) {
                        return 0;
                    }
                    int years = (int) (time / YEAR);
                    return years;
                }

            } catch (Exception e) {
                org.smartregister.cbhc.util.Utils.appendLog(getClass().getName(), e);

            }


        }

        if ((age != null && !age.isEmpty())) {
            return Integer.parseInt(age.trim());
        }


        return 0;
    }

    private int getMaritalStatus() {
        String maritalStatus = householdDetails.getColumnmaps().get("MaritalStatus");
        //"বিবাহিত"
        return maritalStatus != null && (maritalStatus.equals("Married") || maritalStatus.equalsIgnoreCase("বিবাহিত")) ? 1 : 0;
    }

    public ArrayList<Constants.FOLLOWUP_FORM.FOLLOWUPFORMS> getactivateforms() {
        ArrayList<Constants.FOLLOWUP_FORM.FOLLOWUPFORMS> all_forms = new ArrayList<Constants.FOLLOWUP_FORM.FOLLOWUPFORMS>();
        String patient_identifier = householdDetails.getColumnmaps().get("Patient_Identifier");

        if (patient_identifier == null || (patient_identifier != null && patient_identifier.isEmpty()) || patient_identifier.equalsIgnoreCase("null")) {
            return all_forms;
        }
//        all_forms.addAll(followup_forms);
//        String [] remove_from_list = {Followup_Form_MHV_Mobile_no,Followup_Form_MHV_Marital,Followup_Form_MHV_Pregnant,Followup_Form_MHV_Risky_Habit,Followup_Form_MHV_Death};
//
//        for(Constants.FOLLOWUP_FORM.FOLLOWUPFORMS form:followup_forms){
//            if(ArrayUtils.contains(remove_from_list,form.getForm_name())){
//                all_forms.remove(form);
//            }
//        }
        this.age = getAge();
        this.gender = getGender();
        this.marital_status = getMaritalStatus();
        this.pregnant_status = getPregnantStatus();

        if (age == 0) {
            all_forms.add(new Constants.FOLLOWUP_FORM.FOLLOWUPFORMS(Followup_Form_MHV_DS_NewBorn, "শিশু (০-২ মাস)"));
        }
        if (age > 0 && age <= 5) {
            all_forms.add(new Constants.FOLLOWUP_FORM.FOLLOWUPFORMS(Followup_Form_MHV_DS_Toddler, "শিশু (২ মাস-৫ বছর)"));
        }
        if (age > 5) {
            if (gender == 1) {
                all_forms.add(new Constants.FOLLOWUP_FORM.FOLLOWUPFORMS(Followup_Form_MHV_DS_Male, "সাধারণ রোগ"));
            } else {
                all_forms.add(new Constants.FOLLOWUP_FORM.FOLLOWUPFORMS(Followup_Form_MHV_DS_Female, "সাধারণ রোগ"));
            }
            if (marital_status == 1) {

                if (pregnant_status == 1) {
                    all_forms.add(new Constants.FOLLOWUP_FORM.FOLLOWUPFORMS(Followup_Form_MHV_ANC, "প্রসব পূর্ব "));


                } else if (pregnant_status == 2) {
//                    all_forms.add(new Constants.FOLLOWUP_FORM.FOLLOWUPFORMS(Followup_Form_MHV_Delivery,"প্রসব"));
                    all_forms.add(new Constants.FOLLOWUP_FORM.FOLLOWUPFORMS(Followup_Form_MHV_PNC, "প্রসব পরবর্তী "));
                    all_forms.add(new Constants.FOLLOWUP_FORM.FOLLOWUPFORMS(Followup_Form_MHV_FP, "পরিবার পরিকল্পনা"));
                } else {
                    all_forms.add(new Constants.FOLLOWUP_FORM.FOLLOWUPFORMS(Followup_Form_MHV_FP, "পরিবার পরিকল্পনা"));
                }
            }
        }


        return all_forms;
    }

    class FormListRowAdapter extends BaseAdapter {
        Context context;
        List<Constants.FOLLOWUP_FORM.FOLLOWUPFORMS> form_list = new ArrayList<Constants.FOLLOWUP_FORM.FOLLOWUPFORMS>();

        public FormListRowAdapter(Context context, List<Constants.FOLLOWUP_FORM.FOLLOWUPFORMS> form_list) {

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
            if (view == null) {
                view = LayoutInflater.from(context).inflate(R.layout.followup_form_list_row, parent, false);
            }

            String form_name = form_list.get(position).getDisplay_name();
            LinearLayout row_container = view.findViewById(R.id.row_container);
            row_container.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getActivity().getIntent().putExtra(Constants.INTENT_KEY.BASE_ENTITY_ID, householdDetails.getCaseId());

                    JsonFormUtils.launchFollowUpForm(getActivity(), householdDetails.getColumnmaps(), form_list.get(position).getForm_name());
                }
            });
            TextView form_view = view.findViewById(R.id.form_name);

            form_view.setText(form_name);

            return view;
        }
    }

    class FormListAdapter extends BaseAdapter {
        Context context;
        List<Constants.FOLLOWUP_FORM.FOLLOWUPFORMS> form_list;

        public FormListAdapter(Context context, List<Constants.FOLLOWUP_FORM.FOLLOWUPFORMS> form_list) {

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
            if (view == null) {
                view = LayoutInflater.from(context).inflate(R.layout.followup_form_history_row, parent, false);
            }

            String form_name = form_list.get(position).getDisplay_name();
            TextView form_view = view.findViewById(R.id.saved_form_name);
            TextView date_view = view.findViewById(R.id.saved_form_date);
            form_view.setText(form_name);
            date_view.setText(new Date().toString());


            return view;
        }
    }
}
