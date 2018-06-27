package org.smartregister.growplus.activity;

import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.smartregister.Context;
import org.smartregister.adapter.SmartRegisterPaginatedAdapter;
import org.smartregister.domain.FetchStatus;
import org.smartregister.domain.form.FormSubmission;
import org.smartregister.event.Event;
import org.smartregister.event.Listener;
import org.smartregister.growplus.R;
import org.smartregister.growplus.adapter.PathRegisterActivityPagerAdapter;
import org.smartregister.growplus.application.VaccinatorApplication;
import org.smartregister.growplus.fragment.AdvancedSearchFragment;
import org.smartregister.growplus.fragment.BaseSmartRegisterFragment;
import org.smartregister.growplus.fragment.ChildSmartRegisterFragment;
import org.smartregister.growplus.fragment.HouseholdMemberAddFragment;
import org.smartregister.growplus.fragment.HouseholdSmartRegisterFragment;
import org.smartregister.growplus.repository.PathRepository;
import org.smartregister.growplus.repository.UniqueIdRepository;
import org.smartregister.growplus.view.LocationPickerView;
import org.smartregister.provider.SmartRegisterClientsProvider;
import org.smartregister.repository.AllSharedPreferences;
import org.smartregister.repository.DetailsRepository;
import org.smartregister.service.FormSubmissionService;
import org.smartregister.service.ZiggyService;
import org.smartregister.util.FormUtils;
import org.smartregister.view.dialog.DialogOptionModel;
import org.smartregister.view.viewpager.OpenSRPViewPager;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;
import util.JsonFormUtils;

import static android.view.inputmethod.InputMethodManager.HIDE_NOT_ALWAYS;
import static org.smartregister.util.Utils.getValue;

/**
 * Created by Ahmed on 13-Oct-15.
 */
public class HouseholdSmartRegisterActivity extends BaseRegisterActivity {
    private static String TAG = HouseholdSmartRegisterActivity.class.getCanonicalName();

    @Bind(R.id.view_pager)
    OpenSRPViewPager mPager;
    private FragmentPagerAdapter mPagerAdapter;
    private static final int REQUEST_CODE_GET_JSON = 3432;
    private static final int REQUEST_CODE_RECORD_OUT_OF_CATCHMENT = 1131;
    private int currentPage;
    public static final int ADVANCED_SEARCH_POSITION = 1;

    public Fragment mBaseFragment = null;
    private AdvancedSearchFragment advancedSearchFragment;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ButterKnife.bind(this);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        mBaseFragment = new HouseholdSmartRegisterFragment();
        advancedSearchFragment = new AdvancedSearchFragment();
        Fragment[] otherFragments = {new AdvancedSearchFragment()};

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
            LoginActivity.setLanguage();
        } catch (Exception e) {

        }
        super.showFragmentDialog(dialogOptionModel, tag);
    }

    @Override
    public void startFormActivity(String formName, String entityId, String metaData) {
        Log.d("-------------",formName);
        (new AsyncTask(){
            ProgressDialog prog;
            @Override
            protected void onPreExecute() {
                super.onPreExecute();

                prog = new ProgressDialog(HouseholdSmartRegisterActivity.this);
                prog.setTitle("making dummy household");
                prog.setMessage("making dummies");
                prog.setIndeterminate(false);
                prog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                prog.show();
            }

            @Override
            protected Object doInBackground(Object[] objects) {
                for(int z= 0;z < 2000;z++ ) {
                    householdregistrydummy();
                    publishProgress(""+((z/2000)*100));
                }
                return null;
            }

            @Override
            protected void onProgressUpdate(Object[] values) {
                super.onProgressUpdate(values);
                int progress = Integer.parseInt((String)values[0]);
                prog.setProgress(progress);
            }

            @Override
            protected void onPostExecute(Object o) {
                super.onPostExecute(o);
                Toast.makeText(HouseholdSmartRegisterActivity.this, "members added", Toast.LENGTH_SHORT).show();
                prog.dismiss();

            }
        }).execute();
//        try {
//            if (mBaseFragment instanceof HouseholdSmartRegisterFragment) {
//                LocationPickerView locationPickerView = ((HouseholdSmartRegisterFragment) mBaseFragment).getLocationPickerView();
//                String locationId = JsonFormUtils.getOpenMrsLocationId(context(), locationPickerView.getSelectedItem());
//                JsonFormUtils.startForm(this, context(), REQUEST_CODE_GET_JSON, formName, entityId,
//                        metaData, locationId);
//            }
//        } catch (Exception e) {
//            Log.e(TAG, Log.getStackTraceString(e));
//        }

    }

    public void startWomanFormActivity(String formName, String entityId, String metaData) {

        Intent intent = new Intent(getApplicationContext(), PathJsonFormActivity.class);

//        intent.putExtra("json", metaData);
        startActivityForResult(intent, REQUEST_CODE_GET_JSON);


    }

    public void householdregistrydummy(){
            try {


                JSONObject form = new JSONObject("{\"count\":\"1\",\"encounter_type\":\"Household Registration\",\"entity_id\":\"\",\"relational_id\":\"\",\"metadata\":{\"start\":{\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"concept\",\"openmrs_data_type\":\"start\",\"openmrs_entity_id\":\"163137AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\"value\":\"2018-06-27 04:59:46\"},\"end\":{\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"concept\",\"openmrs_data_type\":\"end\",\"openmrs_entity_id\":\"163138AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\"value\":\"2018-06-27 05:00:21\"},\"today\":{\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"encounter\",\"openmrs_entity_id\":\"encounter_date\",\"value\":\"27-06-2018\"},\"deviceid\":{\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"concept\",\"openmrs_data_type\":\"deviceid\",\"openmrs_entity_id\":\"163149AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\"value\":\"868030028857769\"},\"subscriberid\":{\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"concept\",\"openmrs_data_type\":\"subscriberid\",\"openmrs_entity_id\":\"163150AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\"value\":\"470010100063805\"},\"simserial\":{\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"concept\",\"openmrs_data_type\":\"simserial\",\"openmrs_entity_id\":\"163151AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\"value\":\"8988010101000638058f\"},\"phonenumber\":{\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"concept\",\"openmrs_data_type\":\"phonenumber\",\"openmrs_entity_id\":\"163152AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\"value\":\"\"},\"encounter_location\":\"d65e41e3-3281-4594-958a-46bf23fefa8e\",\"look_up\":{\"entity_id\":\"\",\"value\":\"\"}},\"step1\":{\"title\":\"Household Registration\",\"fields\":[{\"key\":\"HIE_FACILITIES\",\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"\",\"openmrs_entity_id\":\"\",\"openmrs_data_type\":\"text\",\"type\":\"tree\",\"hint\":\"Sub-Block\",\"tree\":[{\"name\":\"Bangladesh\",\"key\":\"Bangladesh\",\"level\":\"\",\"nodes\":[{\"name\":\"Dhaka\",\"key\":\"Dhaka\",\"level\":\"\",\"nodes\":[{\"name\":\"Gazipur\",\"key\":\"Gazipur\",\"level\":\"\",\"nodes\":[{\"name\":\"Kaliganj\",\"key\":\"Kaliganj\",\"level\":\"\",\"nodes\":[{\"name\":\"Bahadursadi\",\"key\":\"Bahadursadi\",\"level\":\"\",\"nodes\":[{\"name\":\"Ward-1\",\"key\":\"Bahadursadi:Ward-1\",\"level\":\"\",\"nodes\":[{\"name\":\"Ga-1\",\"key\":\"Bahadursadi:Ward-1:Ga-1\",\"level\":\"\",\"nodes\":[]},{\"name\":\"Ga-2\",\"key\":\"Bahadursadi:Ward-1:Ga-2\",\"level\":\"\",\"nodes\":[]},{\"name\":\"Gha-1\",\"key\":\"Bahadursadi:Ward-1:Gha-1\",\"level\":\"\",\"nodes\":[]},{\"name\":\"Gha-2\",\"key\":\"Bahadursadi:Ward-1:Gha-2\",\"level\":\"\",\"nodes\":[]},{\"name\":\"Ka-1\",\"key\":\"Bahadursadi:Ward-1:Ka-1\",\"level\":\"\",\"nodes\":[]},{\"name\":\"Ka-2\",\"key\":\"Bahadursadi:Ward-1:Ka-2\",\"level\":\"\",\"nodes\":[]},{\"name\":\"Kha-1\",\"key\":\"Bahadursadi:Ward-1:Kha-1\",\"level\":\"\",\"nodes\":[]},{\"name\":\"Kha-2\",\"key\":\"Bahadursadi:Ward-1:Kha-2\",\"level\":\"\",\"nodes\":[]}]}]}]}]}]}]}],\"v_required\":{\"value\":true,\"err\":\"Please enter the Household head's home facility\"},\"default\":\"[\\\"Bangladesh\\\",\\\"Dhaka\\\",\\\"Gazipur\\\",\\\"Kaliganj\\\"]\",\"value\":\"[\\\"Bangladesh\\\",\\\"Dhaka\\\",\\\"Gazipur\\\",\\\"Kaliganj\\\",\\\"Bahadursadi\\\",\\\"Bahadursadi:Ward-1\\\",\\\"Bahadursadi:Ward-1:Ga-2\\\"]\"},{\"key\":\"ADDRESS_LINE\",\"openmrs_entity_parent\":\"usual_residence\",\"openmrs_entity\":\"person_address\",\"openmrs_entity_id\":\"address1\",\"openmrs_data_type\":\"text\",\"type\":\"edit_text\",\"hint\":\"Address\",\"value\":\"alkaran\"},{\"key\":\"First_Name\",\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"person\",\"openmrs_entity_id\":\"first_name\",\"type\":\"edit_text\",\"hint\":\"Household Head Name\",\"edit_type\":\"name\",\"v_regex\":{\"value\":\"[A-Za-z\\\\s.-]*\",\"err\":\"Please enter a valid name\"},\"v_required\":{\"value\":\"true\",\"err\":\"Please enter the Child's ZEIR ID\"},\"value\":\"headoofhousehold\"},{\"key\":\"OpenMRS_ID\",\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"person_identifier\",\"openmrs_entity_id\":\"OpenMRS_ID\",\"type\":\"edit_text\",\"hint\":\"OpenMRS ID *\",\"read_only\":\"true\",\"v_numeric\":{\"value\":\"true\",\"err\":\"Please enter a valid ID\"},\"v_required\":{\"value\":\"true\",\"err\":\"Please enter the Child's ZEIR ID\"}},{\"key\":\"Sex\",\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"person\",\"openmrs_entity_id\":\"gender\",\"type\":\"spinner\",\"hint\":\"Gender *\",\"values\":[\"Male\",\"Female\"],\"v_required\":{\"value\":\"true\",\"err\":\"Please enter the sex\"},\"value\":\"Male\"},{\"key\":\"Date_Of_Reg\",\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"\",\"openmrs_entity_id\":\"\",\"type\":\"date_picker\",\"hint\":\"Registration Date\",\"expanded\":false,\"min_date\":\"today-5y\",\"max_date\":\"today\",\"v_required\":{\"value\":\"true\",\"err\":\"Please enter the date of Registration\"},\"value\":\"27-06-2018\"},{\"key\":\"contact_phone_number\",\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"person_attribute\",\"openmrs_entity_id\":\"phoneNumber\",\"openmrs_data_type\":\"text\",\"type\":\"edit_text\",\"hint\":\"Mobile Number\",\"v_numeric\":{\"value\":\"true\",\"err\":\"Number must begin with 095, 096, or 097 and must be a total of 11 digits in length\"},\"v_regex\":{\"value\":\"(01[5-9][0-9]{8})|s*\",\"err\":\"Number must begin with 015, 016,017,018 or 019 and must be a total of 11 digits in length\"},\"value\":\"01716958804\"},{\"key\":\"HHID\",\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"person_attribute\",\"openmrs_entity_id\":\"householdCode\",\"type\":\"edit_text\",\"hint\":\"Household ID\",\"value\":\"0001\"}]}}");
                String entityId = "";
                if (StringUtils.isBlank(entityId)) {
                    UniqueIdRepository uniqueIdRepo = VaccinatorApplication.getInstance().uniqueIdRepository();
                    entityId = uniqueIdRepo.getNextUniqueId() != null ? uniqueIdRepo.getNextUniqueId().getOpenmrsId() : "";
                    if (entityId.isEmpty()) {
                        Toast.makeText(this, getString(R.string.no_openmrs_id), Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
                JSONObject stepOne = form.getJSONObject(JsonFormUtils.STEP1);
                JSONArray jsonArray = stepOne.getJSONArray(JsonFormUtils.FIELDS);
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    if (jsonObject.getString(JsonFormUtils.KEY)
                            .equalsIgnoreCase(JsonFormUtils.OpenMRS_ID)) {
                        jsonObject.remove(JsonFormUtils.VALUE);
                        jsonObject.put(JsonFormUtils.VALUE, entityId);
                        continue;
                    }
                }
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
                AllSharedPreferences allSharedPreferences = new AllSharedPreferences(preferences);
                JsonFormUtils.saveForm(this, context(), form.toString(), allSharedPreferences.fetchRegisteredANM());
            } catch (Exception e) {

            }

    }


    public void updateAdvancedSearchFilterCount(int count) {
        AdvancedSearchFragment advancedSearchFragment = (AdvancedSearchFragment) findFragmentByPosition(ADVANCED_SEARCH_POSITION);
        if(advancedSearchFragment != null){
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

                JSONObject form = null;
                try {
                    form = new JSONObject(jsonString);
                    if (form.getString("encounter_type").equals("Household Registration")) {
                        FragmentTransaction ft = this.getFragmentManager().beginTransaction();
                        android.app.Fragment prev = this.getFragmentManager().findFragmentByTag(HouseholdMemberAddFragment.DIALOG_TAG);
                        if (prev != null) {
                            ft.remove(prev);
                        }
                        ft.addToBackStack(null);
                        PathRepository repo = (PathRepository) VaccinatorApplication.getInstance().getRepository();
                        net.sqlcipher.database.SQLiteDatabase db = repo.getReadableDatabase();
                        Cursor c = db.rawQuery("SELECT base_entity_id FROM ec_household WHERE last_interacted_with=(SELECT MAX(last_interacted_with) FROM ec_household)", null);
                        String householdid = "";
                        if(c.getCount()>0) {
                            c.moveToFirst();
                            if(c.getString(0)!=null && !StringUtils.isBlank(c.getString(0)))
                                householdid = c.getString(0);
                            c.close();
                        }else{
                            c.close();
                        }
                        String locationid = "";
                        DetailsRepository detailsRepository;
                        detailsRepository = org.smartregister.Context.getInstance().detailsRepository();
                        Map<String, String> details = detailsRepository.getAllDetailsForClient(householdid);
                        locationid = JsonFormUtils.getOpenMrsLocationId(context(),getValue(details, "address4", false) );




                        LocationPickerView locationPickerView = ((HouseholdSmartRegisterFragment) mBaseFragment).getLocationPickerView();

                        String locationId = JsonFormUtils.getOpenMrsLocationId(context(), locationPickerView.getSelectedItem());
                        if(!StringUtils.isBlank(locationid) || locationid.equalsIgnoreCase("")){
                            locationId = locationid;
                        }
                        HouseholdMemberAddFragment addmemberFragment = HouseholdMemberAddFragment.newInstance(this,locationId,householdid,context());
                        addmemberFragment.show(ft, HouseholdMemberAddFragment.DIALOG_TAG);
//                       startFormActivity("woman_member_registration", null, null);

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }
    }

    @Override
    public void saveFormSubmission(String formSubmission, String id, String formName, JSONObject fieldOverrides) {
        // save the form
        try {
            FormUtils formUtils = FormUtils.getInstance(getApplicationContext());
            FormSubmission submission = formUtils.generateFormSubmisionFromXMLString(id, formSubmission, formName, fieldOverrides);

            Context context = context();
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
            new AlertDialog.Builder(this)
                    .setMessage(R.string.form_back_confirm_dialog_message)
                    .setTitle(R.string.form_back_confirm_dialog_title)
                    .setCancelable(false)
                    .setPositiveButton(R.string.yes_button_label,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    switchToBaseFragment(null);
                                }
                            })
                    .setNegativeButton(R.string.no_button_label,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                }
                            })
                    .show();
        } else if (currentPage == 0) {
            super.onBackPressed(); // allow back key only if we are
        }
    }

    public Fragment findFragmentByPosition(int position) {
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
        if(currentPage != 0){
            switchToBaseFragment(null);
            BaseSmartRegisterFragment registerFragment = (BaseSmartRegisterFragment) findFragmentByPosition(0);
            if (registerFragment != null && registerFragment instanceof ChildSmartRegisterFragment) {
                ((ChildSmartRegisterFragment)registerFragment).triggerFilterSelection();
            }
        }
    }

//    private void onQRCodeSucessfullyScanned(String qrCode) {
//        Log.i(getClass().getName(), "QR code: " + qrCode);
//        if (StringUtils.isNotBlank(qrCode)) {
//            filterList(qrCode);
//        }
//    }

    private Listener<FetchStatus> onDataFetchedListener = new Listener<FetchStatus>() {
        @Override
        public void onEvent(FetchStatus fetchStatus) {
            refreshList(fetchStatus);
        }
    };

    private void refreshList(final FetchStatus fetchStatus) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            BaseSmartRegisterFragment registerFragment = (BaseSmartRegisterFragment) findFragmentByPosition(0);
            if (registerFragment != null && fetchStatus.equals(FetchStatus.fetched)) {
                registerFragment.refreshListView();
            }
        } else {
            Handler handler = new Handler(Looper.getMainLooper());
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


}
