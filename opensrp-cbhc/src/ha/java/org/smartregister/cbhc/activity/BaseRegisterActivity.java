package org.smartregister.cbhc.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import net.sqlcipher.database.SQLiteDatabase;
import org.smartregister.cbhc.util.JsonFormUtils;
import org.apache.commons.lang3.StringUtils;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONArray;
import org.json.JSONObject;
import org.smartregister.cbhc.R;
import org.smartregister.cbhc.adapter.PagerAdapter;
import org.smartregister.cbhc.application.AncApplication;
import org.smartregister.cbhc.barcode.Barcode;
import org.smartregister.cbhc.barcode.BarcodeIntentIntegrator;
import org.smartregister.cbhc.barcode.BarcodeIntentResult;
import org.smartregister.cbhc.contract.RegisterContract;
import org.smartregister.cbhc.domain.AttentionFlag;
import org.smartregister.cbhc.event.PatientRemovedEvent;
import org.smartregister.cbhc.event.ShowProgressDialogEvent;
import org.smartregister.cbhc.fragment.BaseRegisterFragment;
import org.smartregister.cbhc.fragment.HomeRegisterFragment;
import org.smartregister.cbhc.helper.BottomNavigationHelper;
import org.smartregister.cbhc.job.SyncServiceJob;
import org.smartregister.cbhc.listener.BottomNavigationListener;
import org.smartregister.cbhc.presenter.RegisterPresenter;
import org.smartregister.cbhc.repository.AncRepository;
import org.smartregister.cbhc.util.Constants;
import org.smartregister.cbhc.util.DBConstants;
import org.smartregister.cbhc.util.Jilla;
import org.smartregister.cbhc.util.Utils;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.configurableviews.ConfigurableViewsLibrary;
import org.smartregister.domain.FetchStatus;
import org.smartregister.provider.SmartRegisterClientsProvider;
import org.smartregister.view.activity.SecuredNativeSmartRegisterActivity;
import org.smartregister.view.viewpager.OpenSRPViewPager;

import java.text.SimpleDateFormat;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by keyman on 26/06/2018.
 */

public abstract class BaseRegisterActivity extends SecuredNativeSmartRegisterActivity implements RegisterContract.View {

    public static final String TAG = BaseRegisterActivity.class.getCanonicalName();
    private static final SimpleDateFormat dateFormatter = new SimpleDateFormat("dd/MM/yyyy");
    @Bind(R.id.view_pager)
    protected OpenSRPViewPager mPager;

    protected RegisterPresenter presenter;
    protected BaseRegisterFragment mBaseFragment = null;
    protected String userInitials;
    protected BottomNavigationHelper bottomNavigationHelper;
    protected BottomNavigationView bottomNavigationView;
    private ProgressDialog progressDialog;
    private FragmentPagerAdapter mPagerAdapter;
    private int currentPage;
    private AlertDialog recordBirthAlertDialog;
    private AlertDialog attentionFlagAlertDialog;
    private View attentionFlagDialogView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base_register);
        ButterKnife.bind(this);

        Fragment[] otherFragments = getOtherFragments();

        mBaseFragment = getRegisterFragment();
        mBaseFragment.setArguments(this.getIntent().getExtras());

        // Instantiate a ViewPager and a PagerAdapter.
        mPagerAdapter = new PagerAdapter(getSupportFragmentManager(), mBaseFragment, otherFragments);
        mPager.setOffscreenPageLimit(otherFragments.length);
        mPager.setAdapter(mPagerAdapter);
        mPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {

            @Override
            public void onPageSelected(int position) {
                currentPage = position;
            }

        });
        initializePresenter();
        presenter.updateInitials();
        recordBirthAlertDialog = createAlertDialog();

        registerBottomNavigation();
        createAttentionFlagsAlertDialog();
    }

    private void registerBottomNavigation() {
        bottomNavigationHelper = new BottomNavigationHelper();
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        if (bottomNavigationView != null) {
            bottomNavigationView.getMenu().add(Menu.NONE, R.string.action_me, Menu.NONE, "Profile")
                    .setIcon(bottomNavigationHelper
                            .writeOnDrawable(R.drawable.initials_background, userInitials, getResources()));
            bottomNavigationHelper.disableShiftMode(bottomNavigationView);

            BottomNavigationListener bottomNavigationListener = new BottomNavigationListener(this);
            bottomNavigationView.setOnNavigationItemSelectedListener(bottomNavigationListener);
        }

    }

    @Override
    protected void onDestroy() {
        presenter.onDestroy(isChangingConfigurations());
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    public void onBackPressed() {
        Fragment fragment = findFragmentByPosition(currentPage);
        if (fragment instanceof BaseRegisterFragment) {
            setSelectedBottomBarMenuItem(R.id.action_clients);
            BaseRegisterFragment registerFragment = (BaseRegisterFragment) fragment;
            if (registerFragment.onBackPressed()) {
                return;
            }
        }

        if (currentPage == 0) {
            super.onBackPressed();
        } else {
            switchToBaseFragment();
            setSelectedBottomBarMenuItem(R.id.action_clients);
        }
    }

    protected abstract void initializePresenter();

    protected abstract BaseRegisterFragment getRegisterFragment();

    protected abstract Fragment[] getOtherFragments();

    @Override
    public void displaySyncNotification() {
        Snackbar syncStatusSnackbar =
                Snackbar.make(this.getWindow().getDecorView(), R.string.manual_sync_triggered, Snackbar.LENGTH_LONG);
        syncStatusSnackbar.show();
    }

    @Override
    public void showLanguageDialog(final List<String> displayValues) {

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout
                .simple_list_item_1,
                displayValues.toArray(new String[displayValues.size()])) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                TextView view = (TextView) super.getView(position, convertView, parent);
                view.setTextColor(
                        ConfigurableViewsLibrary.getInstance().getContext().getColorResource(R.color.customAppThemeBlue));

                return view;
            }
        };
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(this.getString(R.string.select_language));
        builder.setSingleChoiceItems(adapter, 0, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String selectedItem = displayValues.get(which);
                presenter.saveLanguage(selectedItem);
                dialog.dismiss();
            }
        });

        final AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    public void displayToast(int resourceId) {
        displayToast(getString(resourceId));
    }

    @Override
    public void displayToast(String message) {
        Utils.showToast(getApplicationContext(), message);
    }

    @Override
    public void displayShortToast(int resourceId) {
        Utils.showShortToast(getApplicationContext(), getString(resourceId));
    }

    @Override
    protected DefaultOptionsProvider getDefaultOptionsProvider() {
        return null;
    }

    @Override
    protected NavBarOptionsProvider getNavBarOptionsProvider() {
        return null;
    }

    @Override
    protected SmartRegisterClientsProvider clientsProvider() {
        return null;
    }

    @Override
    protected void setupViews() {//Implement Abstract Method
    }

    @Override
    protected void onResumption() {
        presenter.registerViewConfigurations(getViewIdentifiers());
    }

    @Override
    protected void onInitialization() {//Implement Abstract Method
    }

    @Override
    public void startRegistration() {
        startFormActivity(Constants.JSON_FORM.Household_REGISTER, null, null);
    }

    public void refreshList(final FetchStatus fetchStatus) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            BaseRegisterFragment registerFragment = (BaseRegisterFragment) findFragmentByPosition(0);
            if (registerFragment != null && fetchStatus.equals(FetchStatus.fetched)) {
                registerFragment.refreshListView();
            }
        } else {
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(new Runnable() {
                @Override
                public void run() {
                    BaseRegisterFragment registerFragment = (BaseRegisterFragment) findFragmentByPosition(0);
                    if (registerFragment != null && fetchStatus.equals(FetchStatus.fetched)) {
                        registerFragment.refreshListView();
                    }
                }
            });
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);
        if (bottomNavigationView.getSelectedItemId() != R.id.action_clients) {
            setSelectedBottomBarMenuItem(R.id.action_clients);
        }
    }

    @Override
    public void onPause() {
        EventBus.getDefault().unregister(this);
        super.onPause();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void showProgressDialogHandler(ShowProgressDialogEvent showProgressDialogEvent) {
        if (showProgressDialogEvent != null) {
            showProgressDialog(R.string.saving_dialog_title);
        }
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void removePatientHandler(PatientRemovedEvent event) {
        if (event != null) {
            Utils.removeStickyEvent(event);
            refreshList(FetchStatus.fetched);
            hideProgressDialog();
        }
    }

    @Override
    public void showProgressDialog(int titleIdentifier) {
        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setTitle(titleIdentifier);
        progressDialog.setMessage(getString(R.string.please_wait_message));
        if (!isFinishing())
            progressDialog.show();
        progressDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                mBaseFragment.clearSortAndFilter();
            }
        });
    }

    @Override
    public void hideProgressDialog() {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }

    public Fragment findFragmentByPosition(int position) {
        FragmentPagerAdapter fragmentPagerAdapter = mPagerAdapter;
        return getSupportFragmentManager()
                .findFragmentByTag("android:switcher:" + mPager.getId() + ":" + fragmentPagerAdapter.getItemId(position));
    }

    @Override
    protected void onStop() {
        super.onStop();
        presenter.unregisterViewConfiguration(getViewIdentifiers());
    }

    public abstract List<String> getViewIdentifiers();

    @Override
    public Context getContext() {
        return this;
    }

    public void startQrCodeScanner() {
        BarcodeIntentIntegrator barcodeIntentIntegrator = new BarcodeIntentIntegrator(this);
        barcodeIntentIntegrator.addExtra(Barcode.SCAN_MODE, Barcode.QR_MODE);
        barcodeIntentIntegrator.initiateScan();
    }

    @Override
    public void startFormActivity(String formName, String entityId, String metaData) {
        try {
            if (mBaseFragment instanceof HomeRegisterFragment) {
                /* LocationPickerView locationPickerView = ((HomeRegisterFragment) mBaseFragment).getLocationPickerView();*/
                presenter.startForm(formName, entityId, metaData, "");
            }
        } catch (Exception e) {
            Utils.appendLog(getClass().getName(), e);
            Log.e(TAG, Log.getStackTraceString(e));
            displayToast(getString(R.string.error_unable_to_start_form));
        }

    }

    @Override
    public void startFormActivity(JSONObject form) {
        Intent intent = new Intent(this, AncJsonFormActivity.class);
        intent.putExtra("json", form.toString());
        startActivityForResult(intent, JsonFormUtils.REQUEST_CODE_GET_JSON);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        if (data == null && Utils.VIEWREFRESH) {
//            mBaseFragment.clearSortAndFilter();
//            Utils.VIEWREFRESH = false;
//            return;
//        }
        if (requestCode == JsonFormUtils.REQUEST_CODE_GET_JSON && resultCode == RESULT_OK) {

            try {
                String jsonString = data.getStringExtra("json");
                Log.d("JSONResult", jsonString);
                JSONObject form = new JSONObject(jsonString);
                if (form.getString(JsonFormUtils.ENCOUNTER_TYPE).equals(Constants.EventType.REGISTRATION)) {
                    presenter.saveForm(jsonString, false);
                } else if (form.getString(JsonFormUtils.ENCOUNTER_TYPE).equals(Constants.EventType.HouseholdREGISTRATION)) {
                    presenter.saveForm(jsonString, false);
                } else if (form.getString(JsonFormUtils.ENCOUNTER_TYPE).equals(Constants.EventType.UPDATE_Household_REGISTRATION)) {
                    presenter.saveForm(jsonString, true);
                } else if (form.getString(JsonFormUtils.ENCOUNTER_TYPE).equals(Constants.EventType.MemberREGISTRATION)) {
                    presenter.saveForm(jsonString, false);
                    updateScheduledTasks(form);
                } else if (form.getString(JsonFormUtils.ENCOUNTER_TYPE).equals(Constants.EventType.CLOSE)) {
                    presenter.closeAncRecord(jsonString);
                }
            } catch (Exception e) {
                Utils.appendLog(getClass().getName(), e);
                Log.e(TAG, Log.getStackTraceString(e));
            }
            mBaseFragment.clearSortAndFilter();
            SyncServiceJob.scheduleJobImmediately(SyncServiceJob.TAG);

        } else if (requestCode == BarcodeIntentIntegrator.REQUEST_CODE && resultCode == RESULT_OK) {
            BarcodeIntentResult res = BarcodeIntentIntegrator.parseActivityResult(requestCode, resultCode, data);
            if (StringUtils.isNotBlank(res.getContents())) {
                Log.d("Scanned QR Code", res.getContents());
                mBaseFragment.onQRCodeSucessfullyScanned(res.getContents());
                mBaseFragment.setSearchTerm(res.getContents());
            } else
                Log.i("", "NO RESULT FOR QR CODE");
            mBaseFragment.clearSortAndFilter();
        }

    }

    public boolean isValidPermanentAddress(JSONObject hhObject) {
        try {

            JSONObject step1 = hhObject.getJSONObject("step1");
            JSONArray fields = step1.getJSONArray("fields");
            for (int i = 0; i < fields.length(); i++) {
                JSONObject fieldObject = fields.getJSONObject(i);
                String key = fieldObject.getString("key");
                if (key.equalsIgnoreCase("permanent_address")) {
                    String value = fieldObject.getString("value");
                    for (String address : Jilla.getPermanentAddressFields()) {
                        if (value != null && !value.isEmpty() && address.trim().equalsIgnoreCase(value.trim())) {
                            return true;
                        }
                    }
                }
            }
        } catch (Exception e) {
            Utils.appendLog(getClass().getName(), e);

        }
        return false;
    }

    private void updateScheduledTasks(final JSONObject form) {
        org.smartregister.util.Utils.startAsyncTask((new AsyncTask() {
            @Override
            protected Object doInBackground(Object[] objects) {
                AncRepository repo = (AncRepository) AncApplication.getInstance().getRepository();
                SQLiteDatabase db = repo.getReadableDatabase();
                try {
                    JSONArray fields = form.getJSONObject("step1").getJSONArray("fields");
                    String mother_name = "";
                    String entity_id = form.getString("relational_id");

                    for (int i = 0; i < fields.length(); i++) {
                        JSONObject field_object = fields.getJSONObject(i);
                        if (field_object.getString("key").equalsIgnoreCase("Mother_Guardian_First_Name_english")) {
                            String value = field_object.getString("value");
                            if (value != null && !StringUtils.isEmpty(value)) {
                                mother_name = value;
                                mother_name = mother_name.split(" ")[0];
                                break;
                            }
                        }

                    }
                    String sql = "UPDATE ec_woman SET tasks = tasks-1 WHERE relational_id = '" + entity_id + "' AND first_name like '%" + mother_name + "%' AND tasks IS NOT NULL;";
                    db.execSQL(sql);

                } catch (Exception e) {
                    Utils.appendLog(getClass().getName(), e);
                    e.printStackTrace();
                }
                return null;
            }
        }), null);
    }

    public void switchToFragment(final int position) {
        Log.v("we are here", "switchtofragragment");
        try {
            if (Looper.myLooper() == Looper.getMainLooper()) {
                mPager.setCurrentItem(position, false);
            } else {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mPager.setCurrentItem(position, false);
                    }
                });
            }
        } catch (Exception e) {
            Utils.appendLog(getClass().getName(), e);
            Log.e(TAG, e.getMessage(), e);
        }
    }

    public void showRecordBirthPopUp(CommonPersonObjectClient client) {
        getIntent().putExtra(Constants.INTENT_KEY.BASE_ENTITY_ID, client.getColumnmaps().get(DBConstants.KEY.BASE_ENTITY_ID));
//
        try {
            getPresenter().startMemberRegistrationForm(Constants.JSON_FORM.MEMBER_REGISTER, null, null, null, client.getColumnmaps().get(DBConstants.KEY.BASE_ENTITY_ID));
        } catch (Exception e) {
            Utils.appendLog(getClass().getName(), e);

        }

//        client.getColumnmaps().put(DBConstants.KEY.EDD, "2018-12-25"); //To remove temporary for dev testing
//
//        getIntent()
//                .putExtra(Constants.INTENT_KEY.BASE_ENTITY_ID, client.getColumnmaps().get(DBConstants.KEY.BASE_ENTITY_ID));
//        recordBirthAlertDialog.setMessage(
//                "GA: " + Utils.getGestationAgeFromDate(client.getColumnmaps().get(DBConstants.KEY.EDD)) + " weeks\nEDD: "
//                        + Utils.convertDateFormat(Utils.dobStringToDate(client.getColumnmaps().get(DBConstants.KEY.EDD)),
//                        dateFormatter) + " (" + Utils.getDuration(client.getColumnmaps().get(DBConstants.KEY.EDD))
//                        + " to go). \n\n" + client.getColumnmaps().get(DBConstants.KEY.FIRST_NAME)
//                        + " should come in immediately for delivery.");
//        recordBirthAlertDialog.show();
    }

    @NonNull
    protected AlertDialog createAlertDialog() {
        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle(getString(R.string.record_birth) + "?");

        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, getString(R.string.cancel).toUpperCase(),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.record_birth).toUpperCase(),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        JsonFormUtils.launchANCCloseForm(BaseRegisterActivity.this);
                        try {
                            getPresenter().startMemberRegistrationForm(Constants.JSON_FORM.MEMBER_REGISTER, null, null, null, getIntent().getExtras().getString(Constants.INTENT_KEY.BASE_ENTITY_ID));
                        } catch (Exception e) {
                            Utils.appendLog(getClass().getName(), e);

                        }
                    }
                });
        return alertDialog;
    }

    public RegisterPresenter getPresenter() {
        return presenter;
    }

    @Override
    public void showAttentionFlagsDialog(List<AttentionFlag> attentionFlags) {
        ViewGroup red_flags_container = attentionFlagDialogView.findViewById(R.id.red_flags_container);
        ViewGroup yellow_flags_container = attentionFlagDialogView.findViewById(R.id.yellow_flags_container);

        red_flags_container.removeAllViews();
        yellow_flags_container.removeAllViews();

        for (AttentionFlag flag : attentionFlags) {
            if (flag.isRedFlag()) {
                LinearLayout redRow = (LinearLayout) LayoutInflater.from(this)
                        .inflate(R.layout.alert_dialog_attention_flag_row_red, red_flags_container, false);
                ((TextView) redRow.getChildAt(1)).setText(flag.getTitle());
                red_flags_container.addView(redRow);
            } else {

                LinearLayout yellowRow = (LinearLayout) LayoutInflater.from(this)
                        .inflate(R.layout.alert_dialog_attention_flag_row_yellow, yellow_flags_container, false);
                ((TextView) yellowRow.getChildAt(1)).setText(flag.getTitle());
                yellow_flags_container.addView(yellowRow);
            }
        }

        attentionFlagAlertDialog.show();
    }

    @NonNull
    protected AlertDialog createAttentionFlagsAlertDialog() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);

        attentionFlagDialogView = LayoutInflater.from(this).inflate(R.layout.alert_dialog_attention_flag, null);
        dialogBuilder.setView(attentionFlagDialogView);

        attentionFlagDialogView.findViewById(R.id.closeButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attentionFlagAlertDialog.dismiss();
            }
        });

        attentionFlagAlertDialog = dialogBuilder.create();

        return attentionFlagAlertDialog;
    }

    @Override
    public void updateInitialsText(String initials) {
        this.userInitials = initials;
    }

    public void switchToBaseFragment() {
        switchToFragment(0);
    }

    public void setSelectedBottomBarMenuItem(int itemId) {
        bottomNavigationView.setSelectedItemId(itemId);
    }


}
