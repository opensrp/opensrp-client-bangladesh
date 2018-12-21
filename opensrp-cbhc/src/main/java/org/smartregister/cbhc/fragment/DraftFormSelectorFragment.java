package org.smartregister.cbhc.fragment;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.json.JSONException;
import org.json.JSONObject;

import org.smartregister.cbhc.R;
import org.smartregister.cbhc.activity.HomeRegisterActivity;
import org.smartregister.cbhc.application.AncApplication;
import org.smartregister.cbhc.domain.draft_form_object;
import org.smartregister.cbhc.repository.DraftFormRepository;
import org.smartregister.cbhc.util.Constants;
import org.smartregister.clientandeventmodel.Client;
import org.smartregister.clientandeventmodel.Event;
import org.smartregister.util.FormUtils;
import org.smartregister.util.Utils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class DraftFormSelectorFragment extends DialogFragment implements View.OnClickListener {


    public static String DIALOG_TAG = "draft_form_dialog";
    protected ProgressDialog progressDialog;
    Context context;
    String familyBaseEntityId;
    private List<draft_form_object> draftFormObjects;
    public static DraftFormRepository draftFormRepository;
    DateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss");
    private draftFormSelectorAdapter draft_FormSelector_Adapter;
    private ListView listview;


    public void setContext(Context context){
        this.context = context;
    }
    public void setFamilyBaseEntityId(String familyBaseEntityId){
        this.familyBaseEntityId = familyBaseEntityId;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        draftFormRepository = new DraftFormRepository(AncApplication.getInstance().getRepository());
        setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme_Holo_Light_Dialog_NoActionBar);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {

        ViewGroup dialogView = (ViewGroup) inflater.inflate(R.layout.fragment_draft_form_selector, container, false);
        (dialogView.findViewById(R.id.close)).setOnClickListener(this);


        return dialogView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ((LinearLayout)view.findViewById(R.id.start_form)).setOnClickListener(DraftFormSelectorFragment.this);


        listview = (ListView) view.findViewById(R.id.draft_form_list);
         draft_FormSelector_Adapter = new draftFormSelectorAdapter(context,0,draftFormObjects);
        listview.setAdapter(draft_FormSelector_Adapter);
        draft_FormSelector_Adapter.notifyDataSetChanged();
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



    public static DraftFormSelectorFragment newInstance() {
        DraftFormSelectorFragment draftFormSelectorFragment = new DraftFormSelectorFragment();
        return draftFormSelectorFragment;
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.delete_draft:
                draftFormRepository.deleteDraftForms(((draft_form_object)v.getTag()).getID_COLUMN());
                if(familyBaseEntityId==null){
                    draftFormObjects = ((HomeRegisterActivity)context).checkForDraft();
                    draft_FormSelector_Adapter = new draftFormSelectorAdapter(context,0,draftFormObjects);
                    listview.setAdapter(draft_FormSelector_Adapter);
                    draft_FormSelector_Adapter.notifyDataSetChanged();

                }else{
                    draftFormObjects = ((HomeRegisterActivity)context).checkForDraftWithEntityId(familyBaseEntityId);
                    draft_FormSelector_Adapter = new draftFormSelectorAdapter(context,0,draftFormObjects);
                    listview.setAdapter(draft_FormSelector_Adapter);
                    draft_FormSelector_Adapter.notifyDataSetChanged();
                }
                break;
            case R.id.form_column:
                try {
                    String draftForm = ((draft_form_object)v.getTag()).getDraftFormJson();
                    draftFormRepository.deleteDraftForms(((draft_form_object)v.getTag()).getID_COLUMN());
                    ((HomeRegisterActivity)context).startFormActivity(new JSONObject((draftForm)));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                dismiss();
                break;
            case R.id.start_form:
                if(familyBaseEntityId==null){
                    ((HomeRegisterActivity)context).startFormActivity(Constants.JSON_FORM.Household_REGISTER, null, null);
                }else{
                    ((HomeRegisterActivity)context).startMemberRegistrationForm(familyBaseEntityId);
                }
                dismiss();
                break;
            case R.id.close:
                dismiss();
                break;
        }

    }

    public void displayShortToast(int resourceId) {
        Utils.showShortToast(context, this.getString(resourceId));
    }

    public void displayToast(int stringID) {
        Utils.showShortToast(context, this.getString(stringID));
    }


    public void showProgressDialog(int saveMessageStringIdentifier) {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(context);
            progressDialog.setCancelable(false);
            progressDialog.setTitle(getString(saveMessageStringIdentifier));
            progressDialog.setMessage(getString(org.smartregister.R.string.please_wait_message));
        }
    }

    public void hideProgressDialog() {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }


    public void setDraftForms(List<draft_form_object> draftFormObjects) {
        this.draftFormObjects = draftFormObjects;
    }

    class draftFormSelectorAdapter extends ArrayAdapter<draft_form_object>{
        List<draft_form_object> draft_form_objects;

        public draftFormSelectorAdapter(@NonNull Context context, int resource, @NonNull List<draft_form_object> objects) {
            super(context, resource, objects);
            this.draft_form_objects  = objects;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View rowView = inflater.inflate(R.layout.draft_form_list_row, parent, false);
            ((TextView)rowView.findViewById(R.id.form_name)).setText(draft_form_objects.get(position).getFormNAME());


            //to convert Date to String, use format method of SimpleDateFormat class.
            String strDate = "";
//            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
            try {
                Date date = new Date(draft_form_objects.get(position).getDATE());

                strDate = dateFormat.format(date);
            } catch (Exception e) {
                e.printStackTrace();
            }
            ((TextView)rowView.findViewById(R.id.form_date)).setText(strDate);


            LinearLayout draft_form = (LinearLayout)rowView.findViewById(R.id.form_column);
            draft_form.setTag(draft_form_objects.get(position));
            draft_form.setOnClickListener(DraftFormSelectorFragment.this);
            ImageButton delete_draft = (ImageButton)rowView.findViewById(R.id.delete_draft);
            delete_draft.setTag(draft_form_objects.get(position));
            delete_draft.setOnClickListener(DraftFormSelectorFragment.this);



            return rowView;
        }
    }

}