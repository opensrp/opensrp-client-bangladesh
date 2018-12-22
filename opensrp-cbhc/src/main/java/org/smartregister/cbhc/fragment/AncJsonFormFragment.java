package org.smartregister.cbhc.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatTextView;
import android.text.InputType;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.rengwuxian.materialedittext.MaterialEditText;
import com.vijay.jsonwizard.fragments.JsonFormFragment;
import com.vijay.jsonwizard.presenters.JsonFormFragmentPresenter;
import com.vijay.jsonwizard.utils.FormUtils;
import com.vijay.jsonwizard.widgets.DatePickerFactory;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.smartregister.Context;
import org.smartregister.cbhc.R;
import org.smartregister.cbhc.application.AncApplication;
import org.smartregister.cbhc.interactor.AncJsonFormInteractor;
import org.smartregister.cbhc.provider.MotherLookUpSmartClientsProvider;
import org.smartregister.cbhc.util.Constants;
import org.smartregister.cbhc.util.DBConstants;
import org.smartregister.cbhc.util.MotherLookUpUtils;
import org.smartregister.cbhc.viewstate.AncJsonFormFragmentViewState;
import org.smartregister.commonregistry.CommonPersonObject;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.event.Listener;
import org.smartregister.util.DatePickerUtils;
import org.smartregister.util.DateUtil;
import org.smartregister.util.Utils;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.smartregister.cbhc.util.Constants.JSON_FORM_KEY.ENTITY_ID;
import static org.smartregister.cbhc.util.Constants.KEY.VALUE;
import static org.smartregister.util.Utils.getValue;


/**
 * Created by ndegwamartin on 30/06/2018.
 */
public class AncJsonFormFragment extends JsonFormFragment {

    private Snackbar snackbar = null;
    private AlertDialog alertDialog = null;
    private boolean lookedUp = false;
    public static String lookuptype = "";

    public static AncJsonFormFragment getFormFragment(String stepName) {
        AncJsonFormFragment jsonFormFragment = new AncJsonFormFragment();
        Bundle bundle = new Bundle();
        bundle.putString(DBConstants.KEY.STEPNAME, stepName);
        jsonFormFragment.setArguments(bundle);
        return jsonFormFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    protected AncJsonFormFragmentViewState createViewState() {
        return new AncJsonFormFragmentViewState();
    }

    @Override
    protected JsonFormFragmentPresenter createPresenter() {
        return new JsonFormFragmentPresenter(this, AncJsonFormInteractor.getInstance());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);

    }



    ///////////////////////////from path json fragment ////////////////////////////////
    public Context context() {
        return AncApplication.getInstance().getContext();
    }


    public Listener<HashMap<CommonPersonObject, List<CommonPersonObject>>> motherLookUpListener() {
        return motherLookUpListener;
    }

    private void showMotherLookUp(final HashMap<CommonPersonObject, List<CommonPersonObject>> map) {
        if (!map.isEmpty()) {
            tapToView(map);
        } else {
            if (snackbar != null) {
                snackbar.dismiss();
            }
        }
    }

    private void updateResults(final HashMap<CommonPersonObject, List<CommonPersonObject>> map) {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.mother_lookup_results, null);

        ListView listView = (ListView) view.findViewById(R.id.list_view);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.PathDialog);
        builder.setView(view).setNegativeButton(R.string.dismiss, null);
        builder.setCancelable(true);

        alertDialog = builder.create();

        final List<CommonPersonObject> mothers = new ArrayList<>();
        for (Map.Entry<CommonPersonObject, List<CommonPersonObject>> entry : map.entrySet()) {
            mothers.add(entry.getKey());
        }

        final MotherLookUpSmartClientsProvider motherLookUpSmartClientsProvider = new MotherLookUpSmartClientsProvider(getActivity());
        BaseAdapter baseAdapter = new BaseAdapter() {
            @Override
            public int getCount() {
                return mothers.size();
            }

            @Override
            public Object getItem(int position) {
                return mothers.get(position);
            }

            @Override
            public long getItemId(int position) {
                return Long.valueOf(mothers.get(position).getCaseId().replaceAll("\\D+", ""));
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View v;
                if (convertView == null) {
                    v = motherLookUpSmartClientsProvider.inflatelayoutForCursorAdapter();
                } else {
                    v = convertView;
                }

                CommonPersonObject commonPersonObject = mothers.get(position);
                List<CommonPersonObject> children = map.get(commonPersonObject);

                motherLookUpSmartClientsProvider.getView(commonPersonObject, children, v);

                v.setOnClickListener(lookUpRecordOnClickLister);
                v.setTag(Utils.convert(commonPersonObject));
                return v;
            }
        };

        listView.setAdapter(baseAdapter);
        alertDialog.show();

    }

    private void clearMotherLookUp() {
        Map<String, List<View>> lookupMap = getLookUpMap();
        if (lookupMap.containsKey(DBConstants.WOMAN_TABLE_NAME)) {
            List<View> lookUpViews = lookupMap.get(DBConstants.CHILD_TABLE_NAME);
            if (lookUpViews != null && !lookUpViews.isEmpty()) {
                for (View view : lookUpViews) {
                    if (view instanceof MaterialEditText) {
                        MaterialEditText materialEditText = (MaterialEditText) view;
                        materialEditText.setEnabled(true);
                        enableEditText(materialEditText);
                        materialEditText.setTag(com.vijay.jsonwizard.R.id.after_look_up, false);
                        materialEditText.setText("");
                    }
                }

                Map<String, String> metadataMap = new HashMap<>();
                metadataMap.put(ENTITY_ID, "");
                metadataMap.put(VALUE, "");

                writeMetaDataValue(FormUtils.LOOK_UP_JAVAROSA_PROPERTY, metadataMap);

                lookedUp = false;
            }
        }
    }

    private void tapToView(final HashMap<CommonPersonObject, List<CommonPersonObject>> map) {
        snackbar = Snackbar
                .make(getMainView(), map.size() + " match(es).", Snackbar.LENGTH_INDEFINITE);
        snackbar.setAction("Tap to see results", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateResults(map);
                //updateResultTree(map);
            }
        });
        show(snackbar, 30000);

    }

    private void clearView() {
        snackbar = Snackbar
                .make(getMainView(), "Undo Lookup.", Snackbar.LENGTH_INDEFINITE);
        snackbar.setAction("Clear", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                snackbar.dismiss();
                clearMotherLookUp();
            }
        });
        show(snackbar, 30000);
    }

    private void show(final Snackbar snackbar, int duration) {
        if (snackbar == null) {
            return;
        }

        float drawablePadding = getResources().getDimension(R.dimen.register_drawable_padding);
        int paddingInt = Float.valueOf(drawablePadding).intValue();

        float textSize = getActivity().getResources().getDimension(R.dimen.snack_bar_text_size);

        View snackbarView = snackbar.getView();
        snackbarView.setMinimumHeight(Float.valueOf(textSize).intValue());
        snackbarView.setBackgroundResource(R.color.snackbar_background_yellow);

        final AppCompatButton actionView = (AppCompatButton) snackbarView.findViewById(android.support.design.R.id.snackbar_action);
        actionView.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
        actionView.setGravity(Gravity.CENTER);
        actionView.setTextColor(getResources().getColor(R.color.text_black));

        TextView textView = (TextView) snackbarView.findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
        textView.setGravity(Gravity.CENTER);
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                actionView.performClick();
            }
        });
        textView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_cross, 0, 0, 0);
        textView.setCompoundDrawablePadding(paddingInt);
        textView.setPadding(paddingInt, 0, 0, 0);
        textView.setTextColor(getResources().getColor(R.color.text_black));

        snackbarView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                actionView.performClick();
            }
        });

        snackbar.show();

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                snackbar.dismiss();
            }
        }, duration);

    }

    private void lookupDialogDismissed(CommonPersonObjectClient pc) {
        if (pc != null) {

            Map<String, List<View>> lookupMap = getLookUpMap();
            if (lookupMap.containsKey(lookuptype)) {
                List<View> lookUpViews = lookupMap.get(lookuptype);
                if (lookUpViews != null && !lookUpViews.isEmpty()) {

                    for (View view : lookUpViews) {

                        String key = (String) view.getTag(com.vijay.jsonwizard.R.id.key);
                        String text = "";

                        if (StringUtils.containsIgnoreCase(key, MotherLookUpUtils.Name)||(StringUtils.containsIgnoreCase(key, MotherLookUpUtils.Place))) {
                            text = getValue(pc.getColumnmaps(), MotherLookUpUtils.firstName, true)+" "+ getValue(pc.getColumnmaps(), MotherLookUpUtils.lastName, true);
                        }

//                        if (StringUtils.containsIgnoreCase(key, MotherLookUpUtils.lastName)) {
//                            text = getValue(pc.getColumnmaps(), MotherLookUpUtils.lastName, true);
//                        }

                        if (StringUtils.containsIgnoreCase(key, MotherLookUpUtils.birthDate)) {
                            String dobString = getValue(pc.getColumnmaps(), MotherLookUpUtils.dob, false);
                            if (StringUtils.isNotBlank(dobString)) {
                                try {
                                    DateTime birthDateTime = new DateTime(dobString);
                                    text = DatePickerFactory.DATE_FORMAT.format(birthDateTime.toDate());
                                } catch (Exception e) {
                                    Log.e(getClass().getName(), e.toString(), e);
                                }
                            }
                        }

                        if (view instanceof MaterialEditText) {
                            MaterialEditText materialEditText = (MaterialEditText) view;
//                            materialEditText.setEnabled(false);
                            materialEditText.setTag(com.vijay.jsonwizard.R.id.after_look_up, true);
                            materialEditText.setText(text);
//                            materialEditText.setInputType(InputType.TYPE_NULL);
//                            disableEditText(materialEditText);
                        }
                    }

//                    Map<String, String> metadataMap = new HashMap<>();
//                    metadataMap.put(ENTITY_ID, DBConstants.WOMAN_TABLE_NAME);
//                    metadataMap.put(VALUE, getValue(pc.getColumnmaps(), MotherLookUpUtils.baseEntityId, false));
//
//                    writeMetaDataValue(FormUtils.LOOK_UP_JAVAROSA_PROPERTY, metadataMap);

//                    lookedUp = true;
                    clearView();
                }
            }
        }
    }

    private final Listener<HashMap<CommonPersonObject, List<CommonPersonObject>>> motherLookUpListener = new Listener<HashMap<CommonPersonObject, List<CommonPersonObject>>>() {
        @Override
        public void onEvent(HashMap<CommonPersonObject, List<CommonPersonObject>> data) {
            if (!lookedUp) {
                showMotherLookUp(data);
            }
        }
    };

    private final View.OnClickListener lookUpRecordOnClickLister = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (alertDialog != null && alertDialog.isShowing()) {
                alertDialog.dismiss();
                CommonPersonObjectClient client = null;
                if (view.getTag() != null && view.getTag() instanceof CommonPersonObjectClient) {
                    client = (CommonPersonObjectClient) view.getTag();
                }

                if (client != null) {
                    lookupDialogDismissed(client);
                }
            }
        }
    };

    private void disableEditText(MaterialEditText editText) {
        editText.setInputType(InputType.TYPE_NULL);
    }

    private void enableEditText(MaterialEditText editText) {
        editText.setInputType(InputType.TYPE_CLASS_TEXT);
    }

    public void setAgeFromBirthDate(String text){
        getJsonApi().getmJSONObject();
        ArrayList<View> formdataviews = getJsonApi().getFormDataViews();
        for(int i = 0;i<formdataviews.size();i++){
            if(formdataviews.get(i) instanceof MaterialEditText){
                if(((MaterialEditText)formdataviews.get(i)).getFloatingLabelText().toString().trim().equalsIgnoreCase("না জানলে বয়স লিখুন (বছর)*")){
                    Date date = com.vijay.jsonwizard.utils.Utils.getDateFromString(text);
                    DateTime dateTime = new DateTime(date);

                    int age = Utils.getAgeFromDate(dateTime.toString());

                    ((MaterialEditText) formdataviews.get(i)).setText(""+age);
                }
            }
        }
        formdataviews.get(0);

    }


}


