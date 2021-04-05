package org.smartregister.cbhc.fragment;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.github.ybq.android.spinkit.style.FadingCircle;

import net.sqlcipher.database.SQLiteDatabase;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.smartregister.cbhc.R;
import org.smartregister.cbhc.activity.BaseRegisterActivity;
import org.smartregister.cbhc.activity.HomeRegisterActivity;
import org.smartregister.cbhc.activity.ProfileActivity;
import org.smartregister.cbhc.application.AncApplication;
import org.smartregister.cbhc.contract.RegisterFragmentContract;
import org.smartregister.cbhc.cursor.AdvancedMatrixCursor;
import org.smartregister.cbhc.domain.AttentionFlag;
import org.smartregister.cbhc.event.SyncEvent;
import org.smartregister.cbhc.job.ImageUploadServiceJob;
import org.smartregister.cbhc.job.SyncServiceJob;
import org.smartregister.cbhc.model.UnsendData;
import org.smartregister.cbhc.provider.RegisterProvider;
import org.smartregister.cbhc.receiver.SyncStatusBroadcastReceiver;
import org.smartregister.cbhc.repository.AncRepository;
import org.smartregister.cbhc.repository.UnSendDataRepository;
import org.smartregister.cbhc.task.EventLogServiceJob;
import org.smartregister.cbhc.util.Constants;
import org.smartregister.cbhc.util.DBConstants;
import org.smartregister.cbhc.util.MemberObject;
import org.smartregister.cbhc.util.NetworkUtils;
import org.smartregister.cbhc.util.Utils;
import org.smartregister.commonregistry.CommonPersonObject;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.commonregistry.CommonRepository;
import org.smartregister.configurableviews.model.Field;
import org.smartregister.cursoradapter.RecyclerViewFragment;
import org.smartregister.cursoradapter.RecyclerViewPaginatedAdapter;
import org.smartregister.cursoradapter.SmartRegisterQueryBuilder;
import org.smartregister.domain.FetchStatus;
import org.smartregister.provider.SmartRegisterClientsProvider;
import org.smartregister.repository.AllSharedPreferences;
import org.smartregister.view.activity.SecuredNativeSmartRegisterActivity;
import org.smartregister.view.dialog.DialogOption;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import static android.app.Activity.RESULT_OK;

/**
 * Created by keyman on 26/06/2018.
 */

public abstract class BaseRegisterFragment extends RecyclerViewFragment implements RegisterFragmentContract.View,
        SyncStatusBroadcastReceiver.SyncStatusListener {

    public static final String CLICK_VIEW_NORMAL = "click_view_normal";
    public static final String CLICK_VIEW_DOSAGE_STATUS = "click_view_dosage_status";
    public static final String CLICK_VIEW_SYNC = "click_view_sync";
    public static final String CLICK_VIEW_ATTENTION_FLAG = "click_view_attention_flag";
    private static final String TAG = BaseRegisterFragment.class.getCanonicalName();
    private static final int CMED_REQUEST_CODE = 5555;
    public static String TOOLBAR_TITLE = BaseRegisterActivity.class.getPackage() + ".toolbarTitle";
    protected RegisterActionHandler registerActionHandler = new RegisterActionHandler();
    protected RegisterFragmentContract.Presenter presenter;
    protected View rootView;
    protected TextView headerTextDisplay;
    protected TextView filterStatus;
    protected RelativeLayout filterRelativeLayout;
    protected MenuItem menuItem;
    protected String registerCondition;
    String typeofMember;
    protected View.OnKeyListener hideKeyboard = new View.OnKeyListener() {

        @Override
        public boolean onKey(View v, int keyCode, KeyEvent event) {
            if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                Utils.hideKeyboard(getActivity(), v);
                return true;
            }
            return false;
        }
    };
    TextView unsyncView;
    private Snackbar syncStatusSnackbar;
    private ImageView qrCodeScanImageView;
    private Button refer_cmed;

    private ProgressBar syncProgressBar;
    private boolean globalQrSearch = false;
    protected final TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            //Overriden Do something before Text Changed
        }

        @Override
        public void onTextChanged(final CharSequence cs, int start, int before, int count) {
            filter(cs.toString(), "", getMainCondition(), false);
//            setTotalPatients();
//            clientAdapter.
        }

        @Override
        public void afterTextChanged(Editable editable) {
            //Overriden Do something after Text Changed
//            int count = clientAdapter.getCursor().getCount();
//            setTotalPatients();
        }
    };
    private String default_sort_query = DBConstants.KEY.LAST_INTERACTED_WITH + " DESC";

    @Override
    protected SecuredNativeSmartRegisterActivity.DefaultOptionsProvider getDefaultOptionsProvider() {
        return null;
    }

    @Override
    protected SecuredNativeSmartRegisterActivity.NavBarOptionsProvider getNavBarOptionsProvider() {
        return new SecuredNativeSmartRegisterActivity.NavBarOptionsProvider() {

            @Override
            public DialogOption[] filterOptions() {
                return new DialogOption[]{};
            }

            @Override
            public DialogOption[] serviceModeOptions() {
                return new DialogOption[]{
                };
            }

            @Override
            public DialogOption[] sortingOptions() {
                return new DialogOption[]{
                };
            }

            @Override
            public String searchHint() {
                return context().getStringResource(R.string.search_hint);
            }
        };
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_register, container, false);
        rootView = view;//handle to the root


        Toolbar toolbar = view.findViewById(R.id.register_toolbar);
        AppCompatActivity activity = ((AppCompatActivity) getActivity());

        activity.setSupportActionBar(toolbar);
        activity.getSupportActionBar().setTitle(activity.getIntent().getStringExtra(TOOLBAR_TITLE));
        activity.getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        activity.getSupportActionBar().setLogo(R.drawable.round_white_background);
        activity.getSupportActionBar().setDisplayUseLogoEnabled(false);
        activity.getSupportActionBar().setDisplayShowTitleEnabled(false);
        unsyncView = toolbar.findViewById(R.id.unsync_count);
        refer_cmed = toolbar.findViewById(R.id.refer_cmed);
        refer_cmed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createReferJson();
            }
        });
        setupViews(view);
        renderView();
        return view;
    }

    public void createReferJson() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        AllSharedPreferences allSharedPreferences = new AllSharedPreferences(preferences);
        String anm_name = allSharedPreferences.fetchRegisteredANM();
        final String provider_name = allSharedPreferences.getPreference(anm_name);
        final String cc_id = allSharedPreferences.fetchDefaultTeamId(anm_name);
        final String local_id = allSharedPreferences.fetchDefaultLocalityId(anm_name);
        String cc_name = allSharedPreferences.fetchDefaultTeam(anm_name);
//        String cc_address = allSharedPreferences.fetchCurrentLocality();
        String cc_address = "";

        final MemberObject mo = new MemberObject(null, MemberObject.type1);
        final MemberObject hho = new MemberObject(null, MemberObject.type1);

        org.smartregister.util.Utils.startAsyncTask(new AsyncTask() {

            ArrayList<String> hhJsonArrayList = new ArrayList<>();
            ArrayList<String> mmJsonArrayList = new ArrayList<>();
            List<String> hhArrayList = new ArrayList<>();
            List<String> memberArrayList = new ArrayList<>();
            HashMap<String, String> memberMap = new HashMap<>();

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                showProgressView();
            }

            @Override
            protected Object doInBackground(Object[] objects) {
                AncRepository repo = (AncRepository) AncApplication.getInstance().getRepository();
                SQLiteDatabase db = repo.getReadableDatabase();
                ArrayList<UnsendData> unsendDataList = Utils.getCmedDataFromRepo(repo);
                for (UnsendData unsendData : unsendDataList) {
                    if (unsendData.getType().equals(Constants.CMED_KEY.HH_TYPE)) {
                        String sql = "select last_interacted_with, first_name, last_name, ADDRESS_LINE, latrine_structure, household_type, water_source, Monthly_Expenditure,Patient_Identifier,householdCode from ec_household where base_entity_id='" + unsendData.getBaseEntityId() + "'";
                        //               String sql = "SELECT VALUE FROM ec_details WHERE (KEY = 'lmp_date' OR KEY = 'LMP') AND base_entity_id = '" + entity_id + "'";
                        net.sqlcipher.Cursor cursor = db.rawQuery(sql, new String[]{});
                        try {
                            if (cursor.moveToNext()) {
                                String date_month = cursor.getString(0);
                                String house_hold_head_name = cursor.getString(1) + " " + cursor.getString(2);
                                String address = cursor.getString(3);
                                String latrine_type = cursor.getString(4);
                                String accommodation_type = cursor.getString(5);
                                String drinking_water = cursor.getString(6);
                                String monthly_expense = cursor.getString(7);
                                String systemId = cursor.getString(8);
                                String HHCode = cursor.getString(9);
                                String[] strs = {date_month, house_hold_head_name, address, latrine_type, accommodation_type, drinking_water, monthly_expense, systemId, HHCode};
                                hhArrayList = Arrays.asList(strs);
                                JSONObject jsonObject = hho.getHHObject(local_id, provider_name, cc_id, "", unsendData.getBaseEntityId(), hhArrayList);
                                hhJsonArrayList.add(jsonObject.toString());
                            }
                        } catch (Exception e) {
                            Utils.appendLog(getClass().getName(), e);

                        } finally {
                            cursor.close();
                        }
                    } else {
                        memberMap = getMemberDetails(unsendData);
                        JSONObject jsonObject = mo.getGroupMemberObject(local_id,provider_name,cc_id,"",unsendData.getBaseEntityId(),memberMap);
                        mmJsonArrayList.add(jsonObject.toString());
                        Log.v("MemberList:", String.valueOf(mmJsonArrayList));
                    }
                }


                return null;
            }

            @Override
            protected void onPostExecute(Object o) {
                super.onPostExecute(o);
                hideProgressView();
                Log.v("HouseHold_List: ", String.valueOf(hhJsonArrayList));
                Log.v("Member_List: ", String.valueOf(mmJsonArrayList));
                if (appInstalledOrNot(Constants.CMED_KEY.PACKAGE_NAME)) {

                    Intent intent = Utils.passToMHVAPP(hhJsonArrayList, mmJsonArrayList, getActivity());
                    startActivityForResult(intent, CMED_REQUEST_CODE);

                } else {
                    Toast.makeText(getContext(), "Application not installed", Toast.LENGTH_SHORT).show();
                }

            }
        }, null);


    }

    private CommonRepository getCommonRepository(UnsendData unsendData) {
        CommonRepository commonRepository = null;
        if (unsendData.getType().equals(Constants.CMED_KEY.MM_TYPE)) {
            commonRepository = context().commonrepository("ec_member");
        } else if (unsendData.getType().equals(Constants.CMED_KEY.WOMEN_TYPE)) {
            commonRepository = context().commonrepository("ec_woman");
        } else if (unsendData.getType().equals(Constants.CMED_KEY.CHILD_TYPE)) {
            commonRepository = context().commonrepository("ec_child");
        }

        return commonRepository;
    }

    private HashMap<String, String> getMemberDetails(UnsendData unsendData) {
        HashMap<String, String> map = new HashMap<>();
        String query = null;
        AncRepository repo = (AncRepository) AncApplication.getInstance().getRepository();
        SQLiteDatabase db = repo.getWritableDatabase();
        if (unsendData.getType().equals(Constants.CMED_KEY.MM_TYPE)) {
            query = "select Patient_Identifier, first_name, last_name,dob,birthPlace, member_Reg_Date, MaritalStatus, disable, " +
                    "ethnicity,education,Occupation_Category,bloodgroup,has_disease,family_diseases_details,RiskyHabit," +
                    "phoneNumber,gender from ec_member where base_entity_id='" + unsendData.getBaseEntityId() + "'";
            Cursor cursor = db.rawQuery(query, new String[]{});
            try {
                if (cursor.moveToNext()) {
                    map.put("health_id_card",cursor.getString(0));
                    map.put("member_name",cursor.getString(1) + " " + cursor.getString(2));
                    map.put("dob",cursor.getString(3));
                    map.put("address",cursor.getString(4));
                    map.put("add_date",cursor.getString(5));
                    map.put("marital_status",cursor.getString(6));
                    map.put("is_disable",cursor.getString(7));
                    map.put("ethnicity",cursor.getString(8));
                    map.put("education",cursor.getString(9));
                    map.put("occupation",cursor.getString(10));
                    map.put("bloog_group",cursor.getString(11));
                    map.put("has_disease",cursor.getString(12));
                    map.put("family_disease",cursor.getString(13));
                    map.put("risky_habit",cursor.getString(14));
                    map.put("phone_no",cursor.getString(15));
                    map.put("gender",cursor.getString(16));

                }
            } catch (Exception e) {
                Utils.appendLog(getClass().getName(), e);

            } finally {
                cursor.close();
            }
        } else if (unsendData.getType().equals(Constants.CMED_KEY.WOMEN_TYPE)) {
            query = "select Patient_Identifier, first_name, last_name,dob,birthPlace, member_Reg_Date, MaritalStatus, disable, " +
                    "ethnicity,education,Occupation_Category,bloodgroup,has_disease,family_diseases_details,RiskyHabit," +
                    "phoneNumber,gender,lmp_date,Pregnancy_Status from ec_woman where base_entity_id='" + unsendData.getBaseEntityId() + "'";
            net.sqlcipher.Cursor cursor = db.rawQuery(query, new String[]{});
            try {
                if (cursor.moveToNext()) {
                    map.put("health_id_card",cursor.getString(0));
                    map.put("member_name",cursor.getString(1) + " " + cursor.getString(2));
                    map.put("dob",cursor.getString(3));
                    map.put("address",cursor.getString(4));
                    map.put("add_date",cursor.getString(5));
                    map.put("marital_status",cursor.getString(6));
                    map.put("is_disable",cursor.getString(7));
                    map.put("ethnicity",cursor.getString(8));
                    map.put("education",cursor.getString(9));
                    map.put("occupation",cursor.getString(10));
                    map.put("bloog_group",cursor.getString(11));
                    map.put("has_disease",cursor.getString(12));
                    map.put("family_disease",cursor.getString(13));
                    map.put("risky_habit",cursor.getString(14));
                    map.put("phone_no",cursor.getString(15));
                    map.put("gender",cursor.getString(16));
                    map.put("lmp_date",cursor.getString(17));
                    map.put("preg_status",cursor.getString(18));
                }
            } catch (Exception e) {
                Utils.appendLog(getClass().getName(), e);

            } finally {
                cursor.close();
            }
        } else if (unsendData.getType().equals(Constants.CMED_KEY.CHILD_TYPE)) {
            query = "select Patient_Identifier, first_name, last_name,fatherNameEnglish,motherNameEnglish,dob,birthPlace, Birth_weight,member_Reg_Date, disable, " +
                    "ethnicity,bloodgroup,has_disease,family_diseases_details,gender,use_chlorohexidin from ec_woman where base_entity_id='" + unsendData.getBaseEntityId() + "'";
            net.sqlcipher.Cursor cursor = db.rawQuery(query, new String[]{});
            try {
                if (cursor.moveToNext()) {
                    map.put("health_id_card",cursor.getString(0));
                    map.put("member_name",cursor.getString(1) + " " + cursor.getString(2));
                    map.put("fatherName",cursor.getString(3));
                    map.put("motherName",cursor.getString(4));
                    map.put("dob",cursor.getString(5));
                    map.put("address",cursor.getString(6));
                    map.put("birth_weight",cursor.getString(7));
                    map.put("add_date",cursor.getString(8));
                    map.put("is_disable",cursor.getString(9));
                    map.put("ethnicity",cursor.getString(10));
                    map.put("bloog_group",cursor.getString(11));
                    map.put("has_disease",cursor.getString(12));
                    map.put("family_disease",cursor.getString(13));
                    map.put("gender",cursor.getString(14));
                    map.put("use_chlorohexin",cursor.getString(15));
                }
            } catch (Exception e) {
                Utils.appendLog(getClass().getName(), e);

            } finally {
                cursor.close();
            }
        }

        return map;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == MemberObject.type1_RESULT_CODE && resultCode == RESULT_OK) {
            Toast.makeText(getActivity(), "Successfully group activity done:" + data, Toast.LENGTH_LONG).show();
            return;
        } else if (requestCode == CMED_REQUEST_CODE && resultCode == RESULT_OK) {
            UnSendDataRepository unSendDataRepository = new UnSendDataRepository(AncApplication.getInstance().getRepository());
            boolean isUpdated = unSendDataRepository.updateSendingStatus() == 1;
            if (getActivity() != null && !getActivity().isFinishing()) {
                Toast.makeText(getActivity(), "Successfully send to MHV app, is updated:" + isUpdated, Toast.LENGTH_SHORT).show();

            }
        }
    }

    protected abstract void initializePresenter();

    protected void updateSearchView() {
        if (getSearchView() != null) {
            getSearchView().removeTextChangedListener(textWatcher);
            getSearchView().addTextChangedListener(textWatcher);
            getSearchView().setOnKeyListener(hideKeyboard);
        }
    }

    private boolean appInstalledOrNot(String uri) {
        PackageManager pm = getActivity().getPackageManager();
        try {
            pm.getPackageInfo(uri, PackageManager.GET_ACTIVITIES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
        }

        return false;
    }

    @Override
    public void updateSearchBarHint(String searchBarText) {
        if (getSearchView() != null) {
            getSearchView().setHint(searchBarText);
        }
    }

    public void setSearchTerm(String searchText) {
        if (getSearchView() != null) {
            getSearchView().setText(searchText);
        }
    }

    public void onQRCodeSucessfullyScanned(String qrCode) {
        Log.i(TAG, "QR code: " + qrCode);
        if (StringUtils.isNotBlank(qrCode)) {
            filter(qrCode.replace("-", ""), "", getMainCondition(), true);
            setAncId(qrCode);
        }
    }

    public void setAncId(String qrCode) {
        HomeRegisterActivity homeRegisterActivity = (HomeRegisterActivity) getActivity();
        android.support.v4.app.Fragment currentFragment =
                homeRegisterActivity.findFragmentByPosition(1);
        ((AdvancedSearchFragment) currentFragment).getAncId().setText(qrCode);
    }

    @Override
    public void setupViews(View view) {
        super.setupViews(view);
        clientsView.setVisibility(View.VISIBLE);
        clientsProgressView.setVisibility(View.INVISIBLE);
        try {
            presenter.processViewConfigurations();
        } catch (Exception e) {
            Utils.appendLog(getClass().getName(), e);

        }
        this.registerCondition = "";
        if (getMainCondition() != null && presenter != null) {
            presenter.initializeQueries(getMainCondition());
        }

        updateSearchView();
        setServiceModeViewDrawableRight(null);

        // QR Code
        qrCodeScanImageView = view.findViewById(R.id.scanQrCode);
        if (qrCodeScanImageView != null) {
            qrCodeScanImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    HomeRegisterActivity homeRegisterActivity = (HomeRegisterActivity) getActivity();
                    if (homeRegisterActivity != null) {
                        homeRegisterActivity.startQrCodeScanner();
                    }
                }
            });
        }

        //Sync
        ImageView womanSync = view.findViewById(R.id.woman_sync);
        if (womanSync != null) {
            womanSync.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //Todo implement sync
                    SyncServiceJob.scheduleJobImmediately(SyncServiceJob.TAG);
                    ImageUploadServiceJob.scheduleJobImmediately(ImageUploadServiceJob.TAG);
//                    Intent intent = new Intent(getActivity().getApplicationContext(), SyncIntentService.class);
//                    getActivity().getApplicationContext().startService(intent);
//                    Intent intent1= new Intent(getActivity().getApplicationContext(), ImageUploadSyncService.class);
//                    getActivity().getApplicationContext().startService(intent1);
                }
            });
        }

        View topLeftLayout = view.findViewById(R.id.top_left_layout);
        if (topLeftLayout != null) {
            topLeftLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    qrCodeScanImageView.performLongClick();
                }
            });
        }

        /*// Location
        facilitySelection = view.findViewById(R.id.facility_selection);
        if (facilitySelection != null) {m
            facilitySelection.init();
        }*/

        // Progress bar
        syncProgressBar = view.findViewById(R.id.sync_progress_bar);
        if (syncProgressBar != null) {
            FadingCircle circle = new FadingCircle();
            syncProgressBar.setIndeterminateDrawable(circle);
        }

        // Sort and Filter
        headerTextDisplay = view.findViewById(R.id.header_text_display);
        filterStatus = view.findViewById(R.id.filter_status);
        filterRelativeLayout = view.findViewById(R.id.filter_display_view);
    }

    @Override
    protected void onResumption() {
        super.onResumption();

    }

    public void refreshViews() {
        try {
            setRefreshList(true);

        } catch (Exception e) {
            Utils.appendLog(getClass().getName(), e);
            e.printStackTrace();
        }
        try {

            renderView();
        } catch (Exception e) {
            Utils.appendLog(getClass().getName(), e);
            e.printStackTrace();
        }

    }

    private void renderView() {
        getDefaultOptionsProvider();
        if (isPausedOrRefreshList()) {
            this.registerCondition = "";
            if (presenter != null && getMainCondition() != null) {
                presenter.initializeQueries(getMainCondition());
            }
        }
        updateSearchView();
        try {
            presenter.processViewConfigurations();
        } catch (Exception e) {
            Utils.appendLog(getClass().getName(), e);

        }
        // updateLocationText();
        refreshSyncProgressSpinner();
        setTotalPatients();

    }

    private void setTotalPatients(int itemCount) {
        if (headerTextDisplay != null) {
            headerTextDisplay.setText(itemCount > 1 ?
                    String.format(getString(R.string.clients), itemCount) :
                    String.format(getString(R.string.client), itemCount));

            filterRelativeLayout.setVisibility(View.GONE);
        }
    }

    private void setTotalPatients() {
        if (headerTextDisplay != null) {
            headerTextDisplay.setText(clientAdapter.getTotalcount() > 1 ?
                    String.format(getString(R.string.clients), clientAdapter.getTotalcount()) :
                    String.format(getString(R.string.client), clientAdapter.getTotalcount()));

            filterRelativeLayout.setVisibility(View.GONE);
        }
    }

    @Override
    public void initializeQueryParams(String tableName, String countSelect, String mainSelect) {
        this.tablename = tableName;
        this.mainCondition = getMainCondition();
        this.countSelect = countSelect;
        this.mainSelect = mainSelect;
        if (StringUtils.isBlank(this.Sortqueries))
            this.Sortqueries = default_sort_query;
    }

    @Override
    public void initializeAdapter(Set<org.smartregister.configurableviews.model.View> visibleColumns) {
        RegisterProvider registerProvider = new RegisterProvider(getActivity(), commonRepository(), visibleColumns, registerActionHandler, paginationViewHandler);
        clientAdapter = new RecyclerViewPaginatedAdapter(null, registerProvider, context().commonrepository(this.tablename));
        clientAdapter.setCurrentlimit(20);
        clientsView.setAdapter(clientAdapter);
    }

    public void filter(String filterString, String joinTableString, String mainConditionString, boolean qrCode) {
        if (getSearchCancelView() != null)
            getSearchCancelView().setVisibility(StringUtils.isEmpty(filterString) ? View.INVISIBLE : View.VISIBLE);
//        filterString = " and ec_household.phone_number like '%017%' ";
        this.filters = filterString;
        this.joinTable = joinTableString;
        this.mainCondition = mainConditionString;
        this.joinTables = new String[]{"ec_woman", "ec_child", "ec_member"};
        if (StringUtils.isEmpty(filterString)) {
            this.Sortqueries = default_sort_query;
        } else {
            this.Sortqueries = "first_name COLLATE NOCASE ASC," + this.Sortqueries;
        }

        countExecute();

        if (qrCode && StringUtils.isNotBlank(filterString) && clientAdapter.getTotalcount() == 0 && NetworkUtils.isNetworkAvailable()) {
            globalQrSearch = true;
            presenter.searchGlobally(filterString);
        } else {
            filterandSortExecute();
        }
        setTotalPatients();
    }

    @Override
    public void updateFilterAndFilterStatus(String filterText, String sortText) {
        if (headerTextDisplay != null) {
            headerTextDisplay.setText(Html.fromHtml(filterText));
            filterRelativeLayout.setVisibility(View.VISIBLE);
        }

        if (filterStatus != null) {
            filterStatus.setText(Html.fromHtml(clientAdapter.getTotalcount() + " patients " + sortText));
        }
    }

    public void clearSortAndFilter() {
        this.Sortqueries = default_sort_query;
        this.registerCondition = "";
        if (presenter != null)
            presenter.initializeQueries(getMainCondition());
        filter(this.filters, this.joinTable, this.mainCondition, false);
        setTotalPatients();
//        clientAdapter.notifyDataSetChanged();
    }

    public void updateSortAndFilter(List<Field> filterList, Field sortField) {
//        presenter.updateSortAndFilter(filterList, sortField);
        if (filterList.size() == 0) {
            this.registerCondition = "";
            presenter.initializeQueries(getMainCondition());
        } else {
            mainSelect = filterSelect(filterList.get(0).getDbAlias());
        }
        this.Sortqueries = sortField.getDbAlias();
        filter(this.filters, this.joinTable, this.mainCondition, false);
        setTotalPatients();
    }

    @Override
    protected SmartRegisterClientsProvider clientsProvider() {
        return null;
    }

    @Override
    protected void onInitialization() {//Implement Abstract Method
    }

    @Override
    protected void startRegistration() {
        ((HomeRegisterActivity) getActivity()).startFormActivity(Constants.JSON_FORM.ANC_REGISTER, null, null);
    }

    @Override
    protected void onCreation() {
        initializePresenter();

        Bundle extras = getActivity().getIntent().getExtras();
        if (extras != null) {
            boolean isRemote = extras.getBoolean(Constants.IS_REMOTE_LOGIN);
            if (isRemote) {
                presenter.startSync();
            }
        }
    }

    public boolean onBackPressed() {
        return false;
    }

    protected abstract String getMainCondition();

    private void goToPatientDetailActivity(CommonPersonObjectClient patient, boolean launchDialog) {
        if (launchDialog) {
            Log.i(BaseRegisterFragment.TAG, patient.name);
        }

        Intent intent = new Intent(getActivity(), ProfileActivity.class);
        intent.putExtra(Constants.INTENT_KEY.BASE_ENTITY_ID, patient.getCaseId());
        intent.putExtra(ProfileOverviewFragment.EXTRA_HOUSEHOLD_DETAILS, patient);
        startActivityForResult(intent, Utils.NOFILTER);
    }

    /*protected void updateLocationText() {
        if (facilitySelection != null) {
            facilitySelection.setText(LocationHelper.getInstance().getOpenMrsReadableName(
                    facilitySelection.getSelectedItem()));
            String locationId = LocationHelper.getInstance().getOpenMrsLocationId(facilitySelection.getSelectedItem());
            context().allSharedPreferences().savePreference(Constants.CURRENT_LOCATION_ID, locationId);

        }
    }

    public LocationPickerView getFacilitySelection() {
        return facilitySelection;
    }*/

    protected abstract void onViewClicked(View view);

    private void registerSyncStatusBroadcastReceiver() {
        SyncStatusBroadcastReceiver.getInstance().addSyncStatusListener(this);
    }

    private void unregisterSyncStatusBroadcastReceiver() {
        SyncStatusBroadcastReceiver.getInstance().removeSyncStatusListener(this);
    }

    @Override
    public void onSyncInProgress(FetchStatus fetchStatus) {
        Utils.postEvent(new SyncEvent(fetchStatus));
        refreshSyncStatusViews(fetchStatus);
    }

    @Override
    public void onSyncStart() {
        refreshSyncStatusViews(null);
    }

    @Override
    public void onSyncComplete(FetchStatus fetchStatus) {
        refreshSyncStatusViews(fetchStatus);
        EventLogServiceJob.scheduleJobImmediately(EventLogServiceJob.TAG);

    }

    private void refreshSyncStatusViews(FetchStatus fetchStatus) {
        Log.d("sync_status", "refreshSyncStatusViews>>" + SyncStatusBroadcastReceiver.getInstance().isSyncing() + ":fetchStatus:" + fetchStatus);

        if (SyncStatusBroadcastReceiver.getInstance().isSyncing()) {
            if (syncStatusSnackbar != null) syncStatusSnackbar.dismiss();
            syncStatusSnackbar = Snackbar.make(rootView, R.string.syncing,
                    Snackbar.LENGTH_LONG);
            syncStatusSnackbar.show();
            clearSortAndFilter();
        } else {
            if (fetchStatus != null) {
                if (syncStatusSnackbar != null) syncStatusSnackbar.dismiss();
                if (fetchStatus.equals(FetchStatus.fetchedFailed)) {
                    syncStatusSnackbar = Snackbar.make(rootView, R.string.sync_failed, Snackbar.LENGTH_INDEFINITE);
                    syncStatusSnackbar.setActionTextColor(getResources().getColor(R.color.snackbar_action_color));
                    syncStatusSnackbar.setAction(R.string.retry, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            presenter.startSync();
                        }
                    });
                } else if (fetchStatus.equals(FetchStatus.fetched)
                        || fetchStatus.equals(FetchStatus.nothingFetched)) {

                    try {
                        setRefreshList(true);

                    } catch (Exception e) {
                        Utils.appendLog(getClass().getName(), e);
                        e.printStackTrace();
                    }
                    try {

                        renderView();
                    } catch (Exception e) {
                        Utils.appendLog(getClass().getName(), e);
                        e.printStackTrace();
                    }


                    syncStatusSnackbar = Snackbar.make(rootView, R.string.sync_complete, Snackbar.LENGTH_LONG);
                    updateUnsyncCount();
                } else if (fetchStatus.equals(FetchStatus.noConnection)) {
                    syncStatusSnackbar = Snackbar.make(rootView, R.string.sync_failed_no_internet, Snackbar.LENGTH_LONG);
                }
                syncStatusSnackbar.show();
            }

        }

        refreshSyncProgressSpinner();
    }

    @Override
    public void onResume() {
        super.onResume();
        registerSyncStatusBroadcastReceiver();
        updateUnsyncCount();
        if (clientAdapter != null)
            clientAdapter.notifyDataSetChanged();
    }

    @Override
    public void onPause() {
        unregisterSyncStatusBroadcastReceiver();
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void refreshSyncProgressSpinner() {
        if (syncProgressBar == null) {
            syncProgressBar = rootView.findViewById(R.id.sync_progress_bar);
        }
        if (SyncStatusBroadcastReceiver.getInstance().isSyncing()) {
            if (rootView.findViewById(R.id.sync_progress_bar) != null) {
                rootView.findViewById(R.id.sync_progress_bar).setVisibility(View.VISIBLE);
            }
            if (qrCodeScanImageView != null) {
                qrCodeScanImageView.setVisibility(View.GONE);
            }
        } else {
            if (rootView.findViewById(R.id.sync_progress_bar) != null) {
                rootView.findViewById(R.id.sync_progress_bar).setVisibility(View.GONE);
            }
            if (qrCodeScanImageView != null) {
                qrCodeScanImageView.setVisibility(View.VISIBLE);
            }

        }
    }

    public void updateUnsyncCount() {
        org.smartregister.util.Utils.startAsyncTask(new AsyncTask() {

            int count = 0;

            @Override
            protected Object doInBackground(Object[] objects) {
                try {
                    AncRepository repo = (AncRepository) AncApplication.getInstance().getRepository();
                    SQLiteDatabase db = repo.getReadableDatabase();
                    Cursor cursor = db.rawQuery("SELECT * FROM event WHERE syncStatus='Unsynced'", new String[]{});
                    if (cursor != null && cursor.getCount() > 0) {
                        count = cursor.getCount();
                    }

                    cursor.close();
                } catch (Exception e) {
                    Utils.appendLog(getClass().getName(), e);

                }

                return null;
            }

            @Override
            protected void onPostExecute(Object o) {
                super.onPostExecute(o);
                if (unsyncView != null) {
                    if (count != 0) {
                        unsyncView.setVisibility(View.VISIBLE);
                        unsyncView.setText(count + "");
                    } else {
                        unsyncView.setVisibility(View.GONE);
                    }
                }

            }

        }, null);
    }

    @Override
    public void recalculatePagination(AdvancedMatrixCursor matrixCursor) {
        clientAdapter.setTotalcount(matrixCursor.getCount());
        Log.v("total count here", "" + clientAdapter.getTotalcount());
        clientAdapter.setCurrentlimit(20);
        if (clientAdapter.getTotalcount() > 0) {
            clientAdapter.setCurrentlimit(clientAdapter.getTotalcount());
        }
        clientAdapter.setCurrentoffset(0);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        final AdvancedMatrixCursor matrixCursor = presenter.getMatrixCursor();
        if (!globalQrSearch || matrixCursor == null) {
            return super.onCreateLoader(id, args);
        } else {
            globalQrSearch = false;
            switch (id) {
                case LOADER_ID:
                    // Returns a new CursorLoader
                    return new CursorLoader(getActivity()) {
                        @Override
                        public Cursor loadInBackground() {
                            return matrixCursor;
                        }
                    };
                default:
                    // An invalid id was passed in
                    return null;
            }
        }
    }

    ////////////////////////////////////////////////////////////////
    // Inner classes
    ////////////////////////////////////////////////////////////////

    public String filterSelect(String filter) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
        String dateString = format.format(new Date());
        String TWO_MONTHS = format.format(new Date(new Date().getTime() - 2l * 32l * 24l * 60l * 60l * 1000l));
        String FIVE_YEAR = format.format(new Date(new Date().getTime() - 5l * 12l * 30l * 24l * 60l * 60l * 1000l));
        String FIFTY_YEAR = format.format(new Date(new Date().getTime() - 50l * 12l * 30l * 24l * 60l * 60l * 1000l));

        SmartRegisterQueryBuilder queryBUilder = new SmartRegisterQueryBuilder();
        String tableName = "ec_household";
        String[] columns = new String[]{
                tableName + ".relationalid",
                tableName + "." + DBConstants.KEY.LAST_INTERACTED_WITH,
                tableName + "." + DBConstants.KEY.BASE_ENTITY_ID,
                tableName + "." + DBConstants.KEY.FIRST_NAME,
                tableName + "." + DBConstants.KEY.LAST_NAME,
                tableName + "." + DBConstants.KEY.DOB,
                tableName + "." + "Patient_Identifier",
                tableName + "." + DBConstants.KEY.PHONE_NUMBER,
                tableName + "." + DBConstants.KEY.DETAILSSTATUS,
                tableName + "." + DBConstants.KEY.PRESENT_ADDRESS
        };

        if (filter.equals("pregnant")) {
            columns = new String[]{
                    tableName + ".relationalid",
                    tableName + "." + DBConstants.KEY.LAST_INTERACTED_WITH,
                    tableName + "." + DBConstants.KEY.BASE_ENTITY_ID,
                    tableName + "." + DBConstants.KEY.FIRST_NAME,
                    tableName + "." + DBConstants.KEY.LAST_NAME,
                    tableName + "." + DBConstants.KEY.DOB,
                    tableName + "." + "Patient_Identifier",
                    tableName + "." + DBConstants.KEY.PHONE_NUMBER,
                    tableName + "." + DBConstants.KEY.DETAILSSTATUS,
                    "(select ec_woman.PregnancyStatus from ec_woman where (ec_woman.PregnancyStatus = 'Antenatal Period' or ec_woman.PregnancyStatus like '%প্রসব পূর্ব%') and ec_household.id=ec_woman.relational_id) as PregnancyStatus"
            };
            //"(select ec_details.value from ec_details where ec_details.key='Disease_status' and ec_details.value = 'Antenatal Period' and ec_details.base_entity_id=(select ec_woman.id from ec_woman where  ec_household.id=ec_woman.relational_id )) as Disease_status"
            registerCondition = " PregnancyStatus IS NOT NULL";
        } else if (filter.equals("infant")) {
            columns = new String[]{
                    tableName + ".relationalid",
                    tableName + "." + DBConstants.KEY.LAST_INTERACTED_WITH,
                    tableName + "." + DBConstants.KEY.BASE_ENTITY_ID,
                    tableName + "." + DBConstants.KEY.FIRST_NAME,
                    tableName + "." + DBConstants.KEY.LAST_NAME,
                    tableName + "." + DBConstants.KEY.DOB,
                    tableName + "." + "Patient_Identifier",
                    tableName + "." + DBConstants.KEY.PHONE_NUMBER,
                    tableName + "." + DBConstants.KEY.DETAILSSTATUS,
                    "(select ec_child.dob from ec_child where ec_child.relational_id = ec_household.id and ec_child.dob > DATE('" + TWO_MONTHS + "')) as child_dob"
            };
            registerCondition = " child_dob IS NOT NULL";
        } else if (filter.equals("toddler")) {
            columns = new String[]{
                    tableName + ".relationalid",
                    tableName + "." + DBConstants.KEY.LAST_INTERACTED_WITH,
                    tableName + "." + DBConstants.KEY.BASE_ENTITY_ID,
                    tableName + "." + DBConstants.KEY.FIRST_NAME,
                    tableName + "." + DBConstants.KEY.LAST_NAME,
                    tableName + "." + DBConstants.KEY.DOB,
                    tableName + "." + "Patient_Identifier",
                    tableName + "." + DBConstants.KEY.PHONE_NUMBER,
                    tableName + "." + DBConstants.KEY.DETAILSSTATUS,
                    "(select ec_child.dob from ec_child where ec_child.relational_id = ec_household.id and (ec_child.dob < DATE('" + TWO_MONTHS + "') and ec_child.dob > DATE('" + FIVE_YEAR + "'))) as child_dob"
            };
            registerCondition = " child_dob IS NOT NULL";
        } else if (filter.equals("adult")) {
            columns = new String[]{
                    tableName + ".relationalid",
                    tableName + "." + DBConstants.KEY.LAST_INTERACTED_WITH,
                    tableName + "." + DBConstants.KEY.BASE_ENTITY_ID,
                    tableName + "." + DBConstants.KEY.FIRST_NAME,
                    tableName + "." + DBConstants.KEY.LAST_NAME,
                    tableName + "." + DBConstants.KEY.DOB,
                    tableName + "." + "Patient_Identifier",
                    tableName + "." + DBConstants.KEY.PHONE_NUMBER,
                    tableName + "." + DBConstants.KEY.DETAILSSTATUS,
                    "(select ec_member.dob from ec_member where (ec_member.relational_id = ec_household.id and ec_member.dob < DATE('" + FIFTY_YEAR + "'))) as member_dob",
                    "(select ec_woman.dob from ec_woman where (ec_woman.relational_id = ec_household.id and ec_woman.dob < DATE('" + FIFTY_YEAR + "'))) as  woman_dob",
                    "(select ec_child.dob from ec_child where (ec_child.relational_id = ec_household.id and ec_child.dob < DATE('" + FIFTY_YEAR + "'))) as child_dob"
            };
            registerCondition = "(child_dob IS NOT NULL OR member_dob IS NOT NULL OR woman_dob IS NOT NULL ) ";

        } else if (filter.equals("rejected")) {
            columns = new String[]{
                    tableName + ".relationalid",
                    tableName + "." + DBConstants.KEY.LAST_INTERACTED_WITH,
                    tableName + "." + DBConstants.KEY.BASE_ENTITY_ID,
                    tableName + "." + DBConstants.KEY.FIRST_NAME,
                    tableName + "." + DBConstants.KEY.LAST_NAME,
                    tableName + "." + DBConstants.KEY.DOB,
                    tableName + "." + "Patient_Identifier",
                    tableName + "." + DBConstants.KEY.PHONE_NUMBER,
                    tableName + "." + DBConstants.KEY.DETAILSSTATUS

            };
            registerCondition = "(dataApprovalStatus = '0')";
        }


        queryBUilder.SelectInitiateMainTable(tableName, columns);
        String query = queryBUilder.mainCondition(mainCondition);
        this.countSelect = "SELECT COUNT(*) FROM ( " + query + " AND " + registerCondition + " ) ";
//        this.countSelect = query;
        return query;
    }

    private class RegisterActionHandler implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            if (view.getTag() != null && view.getTag(R.id.VIEW_ID) == CLICK_VIEW_NORMAL) {
                goToPatientDetailActivity((CommonPersonObjectClient) view.getTag(), false);
            } else if (view.getTag() != null && view.getTag(R.id.VIEW_ID) == CLICK_VIEW_DOSAGE_STATUS) {

                ((HomeRegisterActivity) getActivity()).showRecordBirthPopUp((CommonPersonObjectClient) view.getTag());

            } else if (view.getTag() != null && view.getTag(R.id.VIEW_ID) == CLICK_VIEW_ATTENTION_FLAG) {
                //Temporary for testing UI , To remove for real dynamic data
                List<AttentionFlag> dummyAttentionFlags = Arrays.asList(new AttentionFlag("Red Flag 1", true), new AttentionFlag("Red Flag 2", true), new AttentionFlag("Yellow Flag 1", false), new AttentionFlag("Yellow Flag 2", false));
                ((HomeRegisterActivity) getActivity()).showAttentionFlagsDialog(dummyAttentionFlags);
            } else if (view.getTag() != null && view.getTag(R.id.VIEW_ID) == CLICK_VIEW_SYNC) { // Need to implement move to catchment
                // TODO Move to catchment
            } else {
                onViewClicked(view);
            }
        }
    }
}



