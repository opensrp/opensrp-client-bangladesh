package org.smartregister.cbhc.fragment;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatButton;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.rengwuxian.materialedittext.MaterialEditText;
import com.vijay.jsonwizard.activities.JsonFormActivity;
import com.vijay.jsonwizard.constants.JsonFormConstants;
import com.vijay.jsonwizard.customviews.MaterialSpinner;
import com.vijay.jsonwizard.fragments.JsonFormFragment;
import com.vijay.jsonwizard.presenters.JsonFormFragmentPresenter;
import com.vijay.jsonwizard.utils.FormUtils;
import com.vijay.jsonwizard.utils.ImageUtils;
import com.vijay.jsonwizard.widgets.DatePickerFactory;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.Context;
import org.smartregister.CoreLibrary;
import org.smartregister.cbhc.R;
import org.smartregister.cbhc.activity.AncJsonFormActivity;
import org.smartregister.cbhc.application.AncApplication;
import org.smartregister.cbhc.interactor.AncJsonFormInteractor;
import org.smartregister.cbhc.provider.MotherLookUpSmartClientsProvider;
import org.smartregister.cbhc.provider.RegisterProvider;
import org.smartregister.cbhc.util.DBConstants;
import org.smartregister.cbhc.util.Jilla;
import org.smartregister.cbhc.util.MotherLookUpUtils;
import org.smartregister.cbhc.viewstate.AncJsonFormFragmentViewState;
import org.smartregister.commonregistry.CommonPersonObject;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.commonregistry.CommonRepository;
import org.smartregister.domain.ProfileImage;
import org.smartregister.event.Listener;
import org.smartregister.repository.ImageRepository;
import org.smartregister.util.Utils;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.vijay.jsonwizard.utils.FormUtils.dpToPixels;
import static org.smartregister.cbhc.util.Constants.JSON_FORM_KEY.ENTITY_ID;
import static org.smartregister.cbhc.util.Constants.KEY.VALUE;
import static org.smartregister.util.Utils.getValue;


/**
 * Created by ndegwamartin on 30/06/2018.
 */
public class AncJsonFormFragment extends JsonFormFragment {

    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd-MM-yyyy");
    public static String lookuptype = "";
    public static Drawable default_drawable;
    AncJsonFormActivity activity;
    JsonFormFragmentPresenter presenter;
    boolean permanentAddressFound = false;
    boolean flag = false;
    int relation_position = -1;
    private Snackbar snackbar = null;
    private AlertDialog alertDialog = null;
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
    private boolean lookedUp = true;
    private ProgressDialog validationProgressdialog;
    private boolean isPressed = false;
    private final Listener<HashMap<CommonPersonObject, List<CommonPersonObject>>> motherLookUpListener = new Listener<HashMap<CommonPersonObject, List<CommonPersonObject>>>() {
        @Override
        public void onEvent(HashMap<CommonPersonObject, List<CommonPersonObject>> data) {
            if (lookedUp && isPressed) {
                showMotherLookUp(data);
            }
        }
    };
    private int countSelect = 0;

    public static AncJsonFormFragment getFormFragment(String stepName) {
        AncJsonFormFragment jsonFormFragment = new AncJsonFormFragment();
        Bundle bundle = new Bundle();
        bundle.putString(DBConstants.KEY.STEPNAME, stepName);
        jsonFormFragment.setArguments(bundle);
        return jsonFormFragment;
    }

    public static Bitmap drawableToBitmap(Drawable drawable) {
        Bitmap bitmap = null;

        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            if (bitmapDrawable.getBitmap() != null) {
                return bitmapDrawable.getBitmap();
            }
        }

        if (drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
            bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888); // Single color bitmap will be created of 1x1 pixel
        } else {
            bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        }

        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        try{
            super.onViewCreated(view, savedInstanceState);
        }catch (Exception e){}
        updateMemberCount();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                isPressed = true;
            }
        }, 10000);
    }

    @Override
    protected AncJsonFormFragmentViewState createViewState() {
        return new AncJsonFormFragmentViewState();
    }

    @Override
    protected JsonFormFragmentPresenter createPresenter() {
        presenter = new JsonFormFragmentPresenter(this, AncJsonFormInteractor.getInstance());
        return presenter;
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

        ListView listView = view.findViewById(R.id.list_view);

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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        showPreviewDialog();
    }

    public void showPreviewDialog() {
        if (!TextUtils.isEmpty(presenter.getmCurrentPhotoPath())) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setPositiveButton("ঠিক আছে", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            }).setNegativeButton("বাতিল করুন", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    triggerCameraIntent();
                    dialog.dismiss();
                }
            });
            final AlertDialog dialog = builder.create();
            LayoutInflater inflater = getLayoutInflater();
            Window window = dialog.getWindow();
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            window.setGravity(Gravity.CENTER);
            View dialogLayout = inflater.inflate(R.layout.go_preview_dialog_layout, null);
            dialog.setView(dialogLayout);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            try {

                ImageView image = dialogLayout.findViewById(R.id.goProDialogImage);
                Bitmap myBitmap = ImageUtils
                        .loadBitmapFromFile(getView().getContext(), presenter.getmCurrentPhotoPath(),
                                ImageUtils.getDeviceWidth(getView().getContext()),
                                dpToPixels(getView().getContext(), 100));
                if (myBitmap != null) {
                    image.setImageBitmap(myBitmap);
                    dialog.show();
                }
            } catch (Exception e) {
                org.smartregister.cbhc.util.Utils.appendLog(getClass().getName(), e);

            }

        }


    }

    private void clearView() {
//        snackbar = Snackbar
//                .make(getMainView(), "Undo Lookup.", Snackbar.LENGTH_INDEFINITE);
//        snackbar.setAction("Clear", new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                snackbar.dismiss();
//                clearMotherLookUp();
//            }
//        });
//        show(snackbar, 30000);
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

        final AppCompatButton actionView = snackbarView.findViewById(android.support.design.R.id.snackbar_action);
        actionView.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
        actionView.setGravity(Gravity.CENTER);
        actionView.setTextColor(getResources().getColor(R.color.text_black));

        TextView textView = snackbarView.findViewById(android.support.design.R.id.snackbar_text);
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

                        if (StringUtils.containsIgnoreCase(key, MotherLookUpUtils.Name) || (StringUtils.containsIgnoreCase(key, MotherLookUpUtils.Place)) || (StringUtils.containsIgnoreCase(key, MotherLookUpUtils.Address))) {
                            text = getValue(pc.getColumnmaps(), MotherLookUpUtils.firstName, true) + " " + getValue(pc.getColumnmaps(), MotherLookUpUtils.lastName, true);
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
                                    org.smartregister.cbhc.util.Utils.appendLog(getClass().getName(), e);
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

    private void disableEditText(MaterialEditText editText) {
        editText.setInputType(InputType.TYPE_NULL);
    }

    private void enableEditText(MaterialEditText editText) {
        editText.setInputType(InputType.TYPE_CLASS_TEXT);
    }

    public boolean isValidPermanentAddress(String value) {
        try {

            for (String address : Jilla.getPermanentAddressFields()) {
                if (value != null && !value.isEmpty() && address.trim().equalsIgnoreCase(value.trim())) {
                    return true;
                }
            }
        } catch (Exception e) {
            org.smartregister.cbhc.util.Utils.appendLog(getClass().getName(), e);

        }
        return false;
    }

    public void triggerCameraIntent() {


        presenter.onClickCameraIcon("household_photo", JsonFormConstants.CHOOSE_IMAGE);

    }

    public int getIsPermanentAddress() {
        getJsonApi().getmJSONObject();
        ArrayList<View> formdataviews = getJsonApi().getFormDataViews();

        for (int i = 0; i < formdataviews.size(); i++) {
            if (formdataviews.get(i) instanceof MaterialSpinner) {
                if (((MaterialSpinner) formdataviews.get(i)).getFloatingLabelText().toString()
                        .trim().equalsIgnoreCase("স্থায়ী ঠিকানা কি একই")) {
                    int position = ((MaterialSpinner) formdataviews.get(i)).getSelectedItemPosition();
                    //1==yes
                    //2==no
                    return position;
                }
            }
        }
        formdataviews.get(0);
        return 1;
    }

    public String getPermanentAddress() {
        getJsonApi().getmJSONObject();
        ArrayList<View> formdataviews = getJsonApi().getFormDataViews();

        for (int i = 0; i < formdataviews.size(); i++) {
            if (formdataviews.get(i) instanceof MaterialEditText) {
                if (((MaterialEditText) formdataviews.get(i)).getFloatingLabelText().toString()
                        .trim().equalsIgnoreCase("স্থায়ী ঠিকানা(ইউনিয়ন)")) {
                    String text = ((MaterialEditText) formdataviews.get(i)).getText().toString();
                    permanentAddressFound = true;
                    return text;
                }
            }
        }
        formdataviews.get(0);
        return "";
    }

    public void setAgeFromBirthDate(String text) {
        getJsonApi().getmJSONObject();
        ArrayList<View> formdataviews = getJsonApi().getFormDataViews();
        for (int i = 0; i < formdataviews.size(); i++) {
            if (formdataviews.get(i) instanceof MaterialEditText) {
                if (((MaterialEditText) formdataviews.get(i)).getFloatingLabelText().toString().trim().equalsIgnoreCase("না জানলে বয়স লিখুন (বছর)")) {
                    Date date = com.vijay.jsonwizard.utils.Utils.getDateFromString(text);
                    if (date != null) {
                        DateTime dateTime = new DateTime(date);

                        int age = Utils.getAgeFromDate(dateTime.toString());

                        ((MaterialEditText) formdataviews.get(i)).setText("" + age);
                    }

                }
            }
        }
        formdataviews.get(0);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == android.R.id.home) {
            presenter.onBackClick();
            return true;
        } else if (item.getItemId() == com.vijay.jsonwizard.R.id.action_next) {
            return next();
        } else if (item.getItemId() == com.vijay.jsonwizard.R.id.action_save) {

            if (getIsPermanentAddress() == 1 || isValidPermanentAddress(getPermanentAddress()) || !permanentAddressFound) {
                showform();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Boolean skipValidation = ((JsonFormActivity) mMainView.getContext()).getIntent().getBooleanExtra(JsonFormConstants.SKIP_VALIDATION,
                                    false);
                            flag = save(skipValidation);
                        } catch (Exception e) {
                            org.smartregister.cbhc.util.Utils.appendLog(getClass().getName(), e);
                            flag = save(false);
                        }
                    }
                }, 500);
                return flag;
            } else {
                Toast.makeText(mMainView.getContext(), "Please select permanent address from the list", Toast.LENGTH_LONG).show();

                return false;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean save(boolean skipValidation) {

        try {

            mMainView.setTag(com.vijay.jsonwizard.R.id.skip_validation, skipValidation);
            presenter.onSaveClick(mMainView);

            return true;


        } catch (Exception e) {
            org.smartregister.cbhc.util.Utils.appendLog(getClass().getName(), e);
            dissmissForm();
        }
        //dissmissForm();


        return false;
    }

    @Override
    public void scrollToView(final View view) {
        dissmissForm();

        view.requestFocus();
        if (!(view instanceof MaterialEditText)) {
            mScrollView.post(new Runnable() {
                @Override
                public void run() {
                    int y = view.getBottom() - view.getHeight();
                    if (y < 0) {
                        y = 0;
                    }
                    mScrollView.scrollTo(0, y);
                }
            });
        }
    }

    @Override
    public void finishWithResult(Intent returnIntent) {
        dissmissForm();
        getActivity().setResult(Activity.RESULT_OK, returnIntent);
        getActivity().finish();
    }

    public void showform() {
        validationProgressdialog = new ProgressDialog(this.getActivity());
        validationProgressdialog.setTitle("Processing");
        validationProgressdialog.setMessage("Checking Validations");
        validationProgressdialog.show();
        // show(Snackbar.make(mMainView, "Checking Validations", Snackbar.LENGTH_LONG),Snackbar.LENGTH_LONG);
    }

    public void dissmissForm() {
        if (validationProgressdialog != null) {
            if (validationProgressdialog.isShowing()) {
                validationProgressdialog.dismiss();
            }
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if (presenter != null && parent != null && view != null)
            presenter.onItemSelected(parent, view, position, id);
//        JSONObject currentObject = get
        if (parent instanceof MaterialSpinner) {
            if (((MaterialSpinner) parent).getFloatingLabelText().toString().equalsIgnoreCase("খানা প্রধানের সাথে সম্পর্ক")) {
                processHeadOfHouseHoldAsMember(position);
                relation_position = position;
            }
            if (((MaterialSpinner) parent).getFloatingLabelText().toString().equalsIgnoreCase("লিঙ্গ")) {
                processHeadOfHouseHoldRelation(position);
            }
//                if (((MaterialSpinner) parent).getFloatingLabelText().toString().equalsIgnoreCase("স্থায়ী ঠিকানা কি একই")) {
//                    processPermanentAddressField(position);
//                }
        }
//        processTimeField(0);
    }

    public void processTimeField(final int position) {
        Utils.startAsyncTask(new AsyncTask() {

            @Override
            protected Object doInBackground(Object[] objects) {

                return null;
            }

            @Override
            protected void onPostExecute(Object o) {
                super.onPostExecute(o);
                ArrayList<View> formdataviews = getJsonApi().getFormDataViews();
                for (int i = 0; i < formdataviews.size(); i++) {
                    if (formdataviews.get(i) instanceof MaterialEditText) {
                        if (((MaterialEditText) formdataviews.get(i)).getFloatingLabelText().
                                toString().trim().equalsIgnoreCase("মৃত্যুর সময়") ||
                                ((MaterialEditText) formdataviews.get(i)).getFloatingLabelText().
                                        toString().trim().equalsIgnoreCase("প্রসবের সময়")) {
                            if (((MaterialEditText) formdataviews.get(i)).getText().toString().isEmpty())
                                ((MaterialEditText) formdataviews.get(i)).setText("00:00");

                            break;
                        }
                    }
                }

            }
        }, null);
    }

    public void updateMemberCount() {
        Utils.startAsyncTask(new AsyncTask() {

            @Override
            protected Object doInBackground(Object[] objects) {
                JSONObject formObject = getJsonApi().getmJSONObject();
                if (formObject.has("metadata")) {
                    try {
                        JSONObject metadata = formObject.getJSONObject("metadata");
                        if (metadata.has("look_up")) {
                            JSONObject look_up = metadata.getJSONObject("look_up");
                            if (look_up.has("entity_id") && look_up.getString("entity_id").equalsIgnoreCase("household")) {
                                String relational_id = look_up.getString("value");
                                CommonRepository commonRepository = AncApplication.getInstance().getContext().commonrepository("ec_household");
                                CommonPersonObject household = commonRepository.findByBaseEntityId(relational_id);
                                if (RegisterProvider.memberCountHashMap != null)
                                    try{
                                        RegisterProvider.memberCountHashMap.remove(household.getCaseId());
                                    }catch (Exception e){

                                    }
                            }
                        }
                    } catch (JSONException e) {
                        org.smartregister.cbhc.util.Utils.appendLog(getClass().getName(), e);
                        e.printStackTrace();
                    }
                }
                return null;
            }

            @Override
            protected void onPostExecute(Object o) {
                super.onPostExecute(o);

            }
        }, null);
    }

    public void processHeadOfHouseHoldRelation(final int position) {

        Utils.startAsyncTask(new AsyncTask() {


            @Override
            protected Object doInBackground(Object[] objects) {

                return null;
            }

            @Override
            protected void onPostExecute(Object o) {
                super.onPostExecute(o);
                ArrayList<View> formdataviews = getJsonApi().getFormDataViews();

                update_spouse_hint(formdataviews, position + 1);

            }
        }, null);
    }


    private void processHeadOfHouseHoldAsMember(final int position) {
        if (position == 0 || position == 1 || position == 2) {
            Utils.startAsyncTask(new AsyncTask() {
                ProfileImage imageRecord;
                String headOfHouseholdFirstName = "";
                String headOfHouseholdLastName = "";
                String headOfHouseholdMobileNumber = "";
                String headOfHouseholdDOB = "";
                String headOfHouseholdDOBUnknown = "";
                String headOfHouseholdage = "";

                @Override
                protected Object doInBackground(Object[] objects) {
                    JSONObject formObject = getJsonApi().getmJSONObject();
                    if (formObject.has("metadata")) {
                        try {
                            JSONObject metadata = formObject.getJSONObject("metadata");
                            if (metadata.has("look_up")) {
                                JSONObject look_up = metadata.getJSONObject("look_up");
                                if (look_up.has("entity_id") && look_up.getString("entity_id").equalsIgnoreCase("household")) {
                                    String relational_id = look_up.getString("value");
                                    CommonRepository commonRepository = AncApplication.getInstance().getContext().commonrepository("ec_household");
                                    CommonPersonObject household = commonRepository.findByBaseEntityId(relational_id);
                                    headOfHouseholdFirstName = getValue(household.getColumnmaps(), "first_name", false);
                                    headOfHouseholdLastName = getValue(household.getColumnmaps(), "last_name", false);
                                    headOfHouseholdMobileNumber = getValue(household.getColumnmaps(), "phone_number", false);
                                    headOfHouseholdDOB = getValue(household.getColumnmaps(), "dob", false);
                                    headOfHouseholdDOBUnknown = getValue(household.getColumnmaps(), "dob_unknown", false);
                                    ImageRepository imageRepo = CoreLibrary.getInstance().context().imageRepository();
                                    imageRecord = imageRepo.findByEntityId(relational_id);
                                }
                            }
                        } catch (JSONException e) {
                            org.smartregister.cbhc.util.Utils.appendLog(getClass().getName(), e);
                            e.printStackTrace();
                        }
                    }
                    return null;
                }

                @Override
                protected void onPostExecute(Object o) {
                    super.onPostExecute(o);
                    ArrayList<View> formdataviews = getJsonApi().getFormDataViews();
                    if (position == 0) {

                        for (int i = 0; i < formdataviews.size(); i++) {
                            if (formdataviews.get(i) instanceof MaterialEditText) {
                                if (((MaterialEditText) formdataviews.get(i)).getFloatingLabelText().toString().trim().equalsIgnoreCase("নামের প্রথম অংশ (ইংরেজীতে)")) {
                                    if(StringUtils.isEmpty(getValueForKey("member_f_name")))
                                        ((MaterialEditText) formdataviews.get(i)).setText(headOfHouseholdFirstName);
                                }
                                if (((MaterialEditText) formdataviews.get(i)).getFloatingLabelText().toString().trim().equalsIgnoreCase("নামের শেষ অংশ (ইংরেজীতে)")) {
                                    if(StringUtils.isEmpty(getValueForKey("last_name")))
                                        ((MaterialEditText) formdataviews.get(i)).setText(headOfHouseholdLastName);
                                }
                                if (((MaterialEditText) formdataviews.get(i)).getFloatingLabelText().toString().trim().equalsIgnoreCase("মোবাইল নম্বর (ইংরেজীতে)")) {
                                    if(StringUtils.isEmpty(getValueForKey("contact_phone_number_by_age")))
                                        ((MaterialEditText) formdataviews.get(i)).setText(headOfHouseholdMobileNumber);
                                }
//                            if (((MaterialEditText) formdataviews.get(i)).getFloatingLabelText().toString().trim().equalsIgnoreCase("‘হ্যাঁ’ হলে জন্ম তারিখ")) {
//                                Date dob = org.smartregister.cbhc.util.Utils.dobStringToDate(headOfHouseholdDOB);
//                                headOfHouseholdDOB = DATE_FORMAT.format(dob);
//                                ((MaterialEditText) formdataviews.get(i)).setText(headOfHouseholdDOB);
//                            }
                            }
//                        if (formdataviews.get(i) instanceof MaterialSpinner) {
//                            if (((MaterialSpinner) formdataviews.get(i)).getFloatingLabelText().toString().trim().equalsIgnoreCase("জন্ম তারিখ জানা আছে কি?")) {
//                                if(headOfHouseholdDOBUnknown.equalsIgnoreCase("true")){
//                                    ((MaterialSpinner) formdataviews.get(i)).setSelection(0);
//                                }else{
//                                    ((MaterialSpinner) formdataviews.get(i)).setSelection(1);
//                                }
//                            }
//
//                        }
                            if (formdataviews.get(i) instanceof ImageView) {
                                try {
                                    ImageView imageView = (ImageView) formdataviews.get(i);
                                    String filePath = imageRecord.getFilepath();

                                    File sd = Environment.getExternalStorageDirectory();

                                    File image = new File(filePath);
                                    BitmapFactory.Options bmOptions = new BitmapFactory.Options();
                                    Bitmap bitmap = BitmapFactory.decodeFile(image.getAbsolutePath(), bmOptions);
//                            bitmap = Bitmap.createScaledBitmap(bitmap,imageView.getWidth(),imageView.getHeight(),true);
                                    default_drawable = imageView.getDrawable();
                                    updateRelevantImageView(bitmap, imageRecord.getFilepath(), (String) imageView.getTag(com.vijay.jsonwizard.R.id.key));

                                } catch (Exception e) {
                                    org.smartregister.cbhc.util.Utils.appendLog(getClass().getName(), e);

                                }
                            }
                        }

                    }
//
                }

            }, null);
        }

        if (position > 0 && isPressed) {
            ArrayList<View> formdataviews = getJsonApi().getFormDataViews();


            for (int i = 0; i < formdataviews.size(); i++) {
                if (formdataviews.get(i) instanceof MaterialEditText) {

                    if (((MaterialEditText) formdataviews.get(i)).getFloatingLabelText().toString().trim().equalsIgnoreCase("নামের প্রথম অংশ (ইংরেজীতে)")) {
                        ((MaterialEditText) formdataviews.get(i)).setText("");
                    }

                    if (((MaterialEditText) formdataviews.get(i)).getFloatingLabelText().toString().trim().equalsIgnoreCase("নামের শেষ অংশ (ইংরেজীতে)")) {
                        ((MaterialEditText) formdataviews.get(i)).setText("");
                    }

                    if (((MaterialEditText) formdataviews.get(i)).getFloatingLabelText().toString().trim().equalsIgnoreCase("মোবাইল নম্বর (ইংরেজীতে)")) {
                        ((MaterialEditText) formdataviews.get(i)).setText("");
                    }

                }
                if (formdataviews.get(i) instanceof ImageView) {
                    try {
                        ImageView imageView = (ImageView) formdataviews.get(i);
                        String filePath = "";

//                            File sd = Environment.getExternalStorageDirectory();
//
//                            File image = new File(filePath);
//                            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
//                            Bitmap bitmap = BitmapFactory.decodeFile(image.getAbsolutePath(), bmOptions);
//                            bitmap = Bitmap.createScaledBitmap(bitmap,imageView.getWidth(),imageView.getHeight(),true);

                        updateRelevantImageView(drawableToBitmap(default_drawable), "", (String) imageView.getTag(com.vijay.jsonwizard.R.id.key));

                    } catch (Exception e) {
                        org.smartregister.cbhc.util.Utils.appendLog(getClass().getName(), e);

                    }
                }
            }
        }

    }
    public String getValueForKey(String key){
        try {
            JSONArray jsonArray = getStep("step1").getJSONArray("fields");
            for(int i=0;i<jsonArray.length();i++){
                JSONObject object = jsonArray.getJSONObject(i);
                String K = object.getString("key");
                String V = "";
                if(object.has("value"))
                    V = object.getString("value");
                if(key.equalsIgnoreCase(K)){
                    return V;
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return "";
    }
    public void update_spouse_hint(ArrayList<View> formdataviews, int position) {

        for (int i = 0; i < formdataviews.size(); i++) {
            if (formdataviews.get(i) instanceof MaterialEditText) {
                if (position == 1) {
                    if (((MaterialEditText) formdataviews.get(i)).getFloatingLabelText().toString().trim().equalsIgnoreCase("স্বামী/স্ত্রীর নাম (ইংরেজীতে)")) {
                        ((MaterialEditText) formdataviews.get(i)).setHint("স্ত্রীর নাম (ইংরেজীতে)*");
                        ((MaterialEditText) formdataviews.get(i)).setFloatingLabelText("স্ত্রীর নাম (ইংরেজীতে)*");
                    }

                    if (((MaterialEditText) formdataviews.get(i)).getFloatingLabelText().toString().trim().equalsIgnoreCase("স্বামী/স্ত্রীর নাম (বাংলায়)")) {
                        ((MaterialEditText) formdataviews.get(i)).setHint("স্ত্রীর নাম (বাংলায়)*");
                        ((MaterialEditText) formdataviews.get(i)).setFloatingLabelText("স্ত্রীর নাম (বাংলায়)*");
                    }
                    if (((MaterialEditText) formdataviews.get(i)).getFloatingLabelText().toString().trim().equalsIgnoreCase("স্বামীর নাম (বাংলায়)*")) {
                        ((MaterialEditText) formdataviews.get(i)).setHint("স্ত্রীর নাম (বাংলায়)*");
                        ((MaterialEditText) formdataviews.get(i)).setFloatingLabelText("স্ত্রীর নাম (বাংলায়)*");
                    }
                    if (((MaterialEditText) formdataviews.get(i)).getFloatingLabelText().toString().trim().equalsIgnoreCase("স্বামীর নাম (ইংরেজীতে)*")) {
                        ((MaterialEditText) formdataviews.get(i)).setHint("স্ত্রীর নাম (ইংরেজীতে)*");
                        ((MaterialEditText) formdataviews.get(i)).setFloatingLabelText("স্ত্রীর নাম (ইংরেজীতে)*");
                    }


                } else if (position == 2) {
                    if (((MaterialEditText) formdataviews.get(i)).getFloatingLabelText().toString().trim().equalsIgnoreCase("স্বামী/স্ত্রীর নাম (ইংরেজীতে)")) {
                        ((MaterialEditText) formdataviews.get(i)).setHint("স্বামীর নাম (ইংরেজীতে)*");
                        ((MaterialEditText) formdataviews.get(i)).setFloatingLabelText("স্বামীর নাম (ইংরেজীতে)*");
                    }

                    if (((MaterialEditText) formdataviews.get(i)).getFloatingLabelText().toString().trim().equalsIgnoreCase("স্বামী/স্ত্রীর নাম (বাংলায়)")) {
                        ((MaterialEditText) formdataviews.get(i)).setHint("স্বামীর নাম (বাংলায়)*");
                        ((MaterialEditText) formdataviews.get(i)).setFloatingLabelText("স্বামীর নাম (বাংলায়)*");
                    }
                    if (((MaterialEditText) formdataviews.get(i)).getFloatingLabelText().toString().trim().equalsIgnoreCase("স্ত্রীর নাম (বাংলায়)*")) {
                        ((MaterialEditText) formdataviews.get(i)).setHint("স্বামীর নাম (বাংলায়)*");
                        ((MaterialEditText) formdataviews.get(i)).setFloatingLabelText("স্বামীর নাম (বাংলায়)*");
                    }
                    if (((MaterialEditText) formdataviews.get(i)).getFloatingLabelText().toString().trim().equalsIgnoreCase("স্ত্রীর নাম (ইংরেজীতে)*")) {
                        ((MaterialEditText) formdataviews.get(i)).setHint("স্বামীর নাম (ইংরেজীতে)*");
                        ((MaterialEditText) formdataviews.get(i)).setFloatingLabelText("স্বামীর নাম (ইংরেজীতে)*");
                    }

                }

            }
        }
    }

}


