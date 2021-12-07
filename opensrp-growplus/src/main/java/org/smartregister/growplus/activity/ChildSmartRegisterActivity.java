package org.smartregister.growplus.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.util.Pair;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.smartregister.adapter.SmartRegisterPaginatedAdapter;
import org.smartregister.clientandeventmodel.*;

import org.smartregister.domain.FetchStatus;
import org.smartregister.domain.form.FormSubmission;
import org.smartregister.event.Event;
import org.smartregister.event.Listener;
import org.smartregister.growplus.R;
import org.smartregister.growplus.adapter.PathRegisterActivityPagerAdapter;
import org.smartregister.growplus.fragment.AdvancedSearchFragment;
import org.smartregister.growplus.fragment.BaseSmartRegisterFragment;
import org.smartregister.growplus.fragment.ChildSmartRegisterFragment;
import org.smartregister.growplus.fragment.WomanSmartRegisterFragment;
import org.smartregister.growplus.view.LocationPickerView;
import org.smartregister.provider.SmartRegisterClientsProvider;
import org.smartregister.repository.AllSharedPreferences;
import org.smartregister.service.FormSubmissionService;
import org.smartregister.service.ZiggyService;
import org.smartregister.util.FormUtils;
import org.smartregister.view.dialog.DialogOptionModel;
import org.smartregister.view.viewpager.OpenSRPViewPager;

import butterknife.Bind;
import butterknife.ButterKnife;
import util.JsonFormUtils;

import static android.view.inputmethod.InputMethodManager.HIDE_NOT_ALWAYS;

/**
 * Created by Ahmed on 13-Oct-15.
 */
public class ChildSmartRegisterActivity extends BaseRegisterActivity {
    private static final String TAG = ChildSmartRegisterActivity.class.getCanonicalName();

    @Bind(R.id.view_pager)
    protected OpenSRPViewPager mPager;
    private FragmentPagerAdapter mPagerAdapter;
    private static final int REQUEST_CODE_GET_JSON = 3432;
    private int currentPage;
    public static final int ADVANCED_SEARCH_POSITION = 1;
    public Fragment sortFilterFragment;
    public android.support.v4.app.Fragment mBaseFragment = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ButterKnife.bind(this);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        sortFilterFragment = new org.smartregister.growplus.fragment.SortFilterFragment();
        mBaseFragment = new ChildSmartRegisterFragment();
        Fragment[] otherFragments = {new AdvancedSearchFragment(),sortFilterFragment};

        // Instantiate a ViewPager and a PagerAdapter.
        mPagerAdapter = new PathRegisterActivityPagerAdapter(getSupportFragmentManager(), mBaseFragment, otherFragments);
        mPager.setOffscreenPageLimit(otherFragments.length);
        mPager.setAdapter(mPagerAdapter);
        mPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                currentPage = position;
            }
        });

        Event.ON_DATA_FETCHED.addListener(onDataFetchedListener);

    }
    public void switchToSortFilterFragment(){
        mPager.setCurrentItem(2);
    }

    public void switchToBaseFragment() {
        mPager.setCurrentItem(0);
    }

    public void applySortAndFilter(){
        switchToBaseFragment();
        ((ChildSmartRegisterFragment)mBaseFragment).requestUpdateView();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        Event.ON_DATA_FETCHED.removeListener(onDataFetchedListener);
    }

    @Override
    protected SmartRegisterPaginatedAdapter adapter() {
        return new SmartRegisterPaginatedAdapter(clientsProvider());
    }

    @Override
    protected DefaultOptionsProvider getDefaultOptionsProvider() {
        return null;
    }

    @Override
    protected void setupViews() {
    }

    @Override
    protected void onResumption() {
        final DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        LinearLayout childregister = (LinearLayout) drawer.findViewById(R.id.child_register);
        childregister.setBackgroundColor(getResources().getColor(R.color.tintcolor));

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
    protected void onInitialization() {
    }

    @Override
    public void startRegistration() {
    }

    @Override
    public void showFragmentDialog(DialogOptionModel dialogOptionModel, Object tag) {
        try {
          //  LoginActivity.setLanguage();
        } catch (Exception e) {
            Log.e(getClass().getCanonicalName(), e.getMessage());
        }
        super.showFragmentDialog(dialogOptionModel, tag);
    }

    @Override
    public void startFormActivity(String formName, String entityId, String metaData) {
        try {
            if (mBaseFragment instanceof ChildSmartRegisterFragment) {
                LocationPickerView locationPickerView = ((ChildSmartRegisterFragment) mBaseFragment).getLocationPickerView();
                String locationId = JsonFormUtils.getOpenMrsLocationId(context(), locationPickerView.getSelectedItem());
                JsonFormUtils.startForm(ChildSmartRegisterActivity.this, context(), REQUEST_CODE_GET_JSON, formName, entityId, metaData, locationId);
            }
        } catch (Exception e) {
            Log.e(TAG, Log.getStackTraceString(e));
        }

    }

    public void startAdvancedSearch() {
        try {
            mPager.setCurrentItem(ADVANCED_SEARCH_POSITION, false);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void updateAdvancedSearchFilterCount(int count) {
        AdvancedSearchFragment advancedSearchFragment = (AdvancedSearchFragment) findFragmentByPosition(ADVANCED_SEARCH_POSITION);
        if (advancedSearchFragment != null) {
            advancedSearchFragment.updateFilterCount(count);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_GET_JSON) {
            if (resultCode == RESULT_OK) {

                String jsonString = data.getStringExtra("json");
                Log.d("JSONResult", jsonString);

                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
                AllSharedPreferences allSharedPreferences = new AllSharedPreferences(preferences);

                  JsonFormUtils.saveForm(this, context(), jsonString, allSharedPreferences.fetchRegisteredANM());
//                Pair<Client, org.smartregister.clientandeventmodel.Event> values = JsonFormUtils.processRegistrationForm(context(),allSharedPreferences, jsonString,allSharedPreferences.fetchRegisteredANM(), "child");
//                System.out.println(values.toString());


            }
        }
    }

    @Override
    public void saveFormSubmission(String formSubmission, String id, String formName, JSONObject fieldOverrides) {
        // save the form
        try {
            FormUtils formUtils = FormUtils.getInstance(getApplicationContext());
            FormSubmission submission = formUtils.generateFormSubmisionFromXMLString(id, formSubmission, formName, fieldOverrides);

            org.smartregister.Context context = context();
            ZiggyService ziggyService = context.ziggyService();
            ziggyService.saveForm(getParams(submission), submission.instance());

            FormSubmissionService formSubmissionService = context.formSubmissionService();
            formSubmissionService.updateFTSsearch(submission);

            Log.v("we are here", "hhregister");
            //switch to forms list fragmentstregi
            switchToBaseFragment(formSubmission); // Unnecessary!! passing on data

        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }
    }

    public void switchToBaseFragment(final String data) {
        Log.v("we are here", "switchtobasegragment");
        final int prevPageIndex = currentPage;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mPager.setCurrentItem(0, false);
                refreshList(data);
            }
        });
    }

    @Override
    public void onBackPressed() {
        BaseSmartRegisterFragment registerFragment = (BaseSmartRegisterFragment) findFragmentByPosition(currentPage);
        if (registerFragment.onBackPressed()) {
            return;
        }
        if (currentPage != 0) {
            new AlertDialog.Builder(this, R.style.PathAlertDialog)
                    .setMessage(org.smartregister.growplus.R.string.form_back_confirm_dialog_message)
                    .setTitle(org.smartregister.growplus.R.string.form_back_confirm_dialog_title)
                    .setCancelable(false)
                    .setPositiveButton(org.smartregister.growplus.R.string.no_button_label,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {

                                }
                            })
                    .setNegativeButton(org.smartregister.growplus.R.string.yes_button_label,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    switchToBaseFragment(null);
                                }
                            })
                    .show();
        } else if (currentPage == 0) {
            super.onBackPressed(); // allow back key only if we are
        }
    }

    public android.support.v4.app.Fragment findFragmentByPosition(int position) {
        FragmentPagerAdapter fragmentPagerAdapter = mPagerAdapter;
        return getSupportFragmentManager().findFragmentByTag("android:switcher:" + mPager.getId() + ":" + fragmentPagerAdapter.getItemId(position));
    }

    private boolean currentActivityIsShowingForm() {
        return currentPage != 0;
    }

    @Override
    protected void onPause() {
        super.onPause();
    }



    public void filterSelection() {
        if (currentPage != 0) {
            switchToBaseFragment(null);
            BaseSmartRegisterFragment registerFragment = (BaseSmartRegisterFragment) findFragmentByPosition(0);
            if (registerFragment != null && registerFragment instanceof ChildSmartRegisterFragment) {
                ((ChildSmartRegisterFragment) registerFragment).triggerFilterSelection();
            }
        }
    }

    private void onQRCodeSucessfullyScanned(String qrCode) {
        Log.i(getClass().getName(), "QR code: " + qrCode);
        if (StringUtils.isNotBlank(qrCode)) {
            filterList(qrCode);
        }
    }

    private final Listener<FetchStatus> onDataFetchedListener = new Listener<FetchStatus>() {
        @Override
        public void onEvent(FetchStatus fetchStatus) {
            refreshList(fetchStatus);
        }
    };

    public void refreshList(final FetchStatus fetchStatus) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            BaseSmartRegisterFragment registerFragment = (BaseSmartRegisterFragment) findFragmentByPosition(0);
            if (registerFragment != null && fetchStatus.equals(FetchStatus.fetched)) {
                registerFragment.refreshListView();
            }
        } else {
            Handler handler = new android.os.Handler(Looper.getMainLooper());
            handler.post(new Runnable() {
                @Override
                public void run() {
                    BaseSmartRegisterFragment registerFragment = (BaseSmartRegisterFragment) findFragmentByPosition(0);
                    if (registerFragment != null && fetchStatus.equals(FetchStatus.fetched)) {
                        registerFragment.refreshListView();
                    }
                }
            });
        }

    }

    private void refreshList(String data) {
        BaseSmartRegisterFragment registerFragment = (BaseSmartRegisterFragment) findFragmentByPosition(0);
        if (registerFragment != null && data != null) {
            registerFragment.refreshListView();
        }

    }

    private void filterList(String filterString) {
        BaseSmartRegisterFragment registerFragment = (BaseSmartRegisterFragment) findFragmentByPosition(0);
        if (registerFragment != null) {
            registerFragment.openVaccineCard(filterString);
        }
    }

    public void hideKeyboard() {
        InputMethodManager inputManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), HIDE_NOT_ALWAYS);
    }

//    private void saveRegistration(Pair<Client, Event> pair, String jsonString, boolean isEditMode) {
//        try {
//            Client baseClient = pair.first;
//            Event baseEvent = pair.second;
//            if (baseClient != null) {
//                JSONObject clientJson = new JSONObject(JsonFormUtils.gson.toJson(baseClient));
//                if (isEditMode) {
//                    JsonFormUtils.mergeAndSaveClient(getSyncHelper(), baseClient);
//                } else {
//                    getSyncHelper().addClient(baseClient.getBaseEntityId(), clientJson);
//                }
//            }
//            if (baseEvent != null) {
//                JSONObject eventJson = new JSONObject(JsonFormUtils.gson.toJson(baseEvent));
//                getSyncHelper().addEvent(baseEvent.getBaseEntityId(), eventJson);
//            }
//            if (isEditMode) {
//                // Unassign current OPENSRP ID
//                if (baseClient != null) {
//                    String newOpenSRPId = baseClient.getIdentifier(DBConstants.KEY.Patient_Identifier).replace("-", "");
//                    String currentOpenSRPId = JsonFormUtils.getString(jsonString, JsonFormUtils.CURRENT_OPENSRP_ID).replace("-", "");
//                    if (!newOpenSRPId.equals(currentOpenSRPId)) {
//                        //OPENSRP ID was changed
//                        getUniqueIdRepository().open(currentOpenSRPId);
//                    }
//                }
//            } else {
//                if (baseClient != null) {
//                    String opensrpId = baseClient.getIdentifier(DBConstants.KEY.ANC_ID);
//                    //mark OPENSRP ID as used
//                    getUniqueIdRepository().close(opensrpId);
//                }
//            }
//            if (baseClient != null || baseEvent != null) {
//                String imageLocation = JsonFormUtils.getFieldValue(jsonString, Constants.KEY.PHOTO);
//                JsonFormUtils.saveImage(baseEvent.getProviderId(), baseClient.getBaseEntityId(), imageLocation);
//            }
//            long lastSyncTimeStamp = getAllSharedPreferences().fetchLastUpdatedAtDate(0);
//            Date lastSyncDate = new Date(lastSyncTimeStamp);
//            getClientProcessorForJava().processClient(getSyncHelper().getEvents(lastSyncDate, BaseRepository.TYPE_Unsynced));
//            getAllSharedPreferences().saveLastUpdatedAtDate(lastSyncDate.getTime());
//            /////////////////////////try this out////////////////
////            ECSyncHelper ecUpdater = ECSyncHelper.getInstance(AncApplication.getInstance().getApplicationContext());
////            List<EventClient> events = ecUpdater.allEventClients(lastSyncTimeStamp-1, lastSyncTimeStamp);
////            AncClientProcessorForJava.getInstance(AncApplication.getInstance().getApplicationContext()).processClient(events);
//            ////////////////////////////////////////////////////////////////
//        } catch (Exception e) {
//            Log.e(TAG, Log.getStackTraceString(e));
//        }
//    }

}
