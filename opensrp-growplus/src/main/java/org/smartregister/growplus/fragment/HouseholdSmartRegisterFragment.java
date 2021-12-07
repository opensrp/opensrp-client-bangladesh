package org.smartregister.growplus.fragment;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import net.sqlcipher.database.SQLiteDatabase;

import org.smartregister.Context;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.commonregistry.CommonRepository;
import org.smartregister.cursoradapter.CursorCommonObjectFilterOption;
import org.smartregister.cursoradapter.CursorCommonObjectSort;
import org.smartregister.cursoradapter.CursorSortOption;
import org.smartregister.cursoradapter.SmartRegisterPaginatedCursorAdapter;
import org.smartregister.cursoradapter.SmartRegisterQueryBuilder;
import org.smartregister.immunization.db.VaccineRepo;
import org.smartregister.immunization.util.VaccinateActionUtils;
import org.smartregister.growplus.R;
import org.smartregister.growplus.activity.HouseholdDetailActivity;
import org.smartregister.growplus.activity.HouseholdSmartRegisterActivity;
import org.smartregister.growplus.activity.LoginActivity;
import org.smartregister.growplus.application.VaccinatorApplication;
import org.smartregister.growplus.domain.RegisterClickables;
import org.smartregister.growplus.option.BasicSearchOption;
import org.smartregister.growplus.option.DateSort;
import org.smartregister.growplus.option.StatusSort;
import org.smartregister.growplus.provider.HouseholdSmartClientsProvider;
import org.smartregister.growplus.servicemode.VaccinationServiceModeOption;
import org.smartregister.growplus.view.LocationPickerView;
import org.smartregister.provider.SmartRegisterClientsProvider;
import org.smartregister.repository.AllSharedPreferences;
import org.smartregister.view.activity.SecuredNativeSmartRegisterActivity;
import org.smartregister.view.dialog.DialogOption;
import org.smartregister.view.dialog.FilterOption;
import org.smartregister.view.dialog.ServiceModeOption;
import org.smartregister.view.dialog.SortOption;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


import util.SortFilterUtil;

import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;

public class HouseholdSmartRegisterFragment extends BaseSmartRegisterFragment {
    private final ClientActionHandler clientActionHandler = new ClientActionHandler();
    private static final String EXTRA_HOUSEHOLD_DETAILS = "household_details";
    private LocationPickerView clinicSelection;
    private static final long NO_RESULT_SHOW_DIALOG_DELAY = 1000l;
    private Handler showNoResultDialogHandler;
    private NotInCatchmentDialogFragment notInCatchmentDialogFragment;
    private TextView filterCount;
    private View filterSection;
    private ImageView backButton;
    private TextView nameInitials;
    private int dueOverdueCount = 0;

    @Override
    protected SecuredNativeSmartRegisterActivity.DefaultOptionsProvider getDefaultOptionsProvider() {
        return new SecuredNativeSmartRegisterActivity.DefaultOptionsProvider() {
            // FIXME path_conflict
//            @Override
            public FilterOption searchFilterOption() {
                return new BasicSearchOption(getDefaultOptionsProvider().nameInShortFormForTitle());
            }

            @Override
            public ServiceModeOption serviceMode() {
                return new VaccinationServiceModeOption(null, "Linda Clinic", new int[]{
                        R.string.child_profile, R.string.birthdate_age, R.string.epi_number, R.string.child_contact_number,
                        R.string.child_next_vaccine
                }, new int[]{5, 2, 2, 3, 3});
            }

            @Override
            public FilterOption villageFilter() {
                return new CursorCommonObjectFilterOption("no village filter", "");
            }

            @Override
            public SortOption sortOption() {
                return new CursorCommonObjectSort(getResources().getString(R.string.woman_alphabetical_sort), "HHID desc");
            }

            @Override
            public String nameInShortFormForTitle() {
                return getString(R.string.household);
            }
        };
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
                        new CursorCommonObjectSort(getResources().getString(R.string.woman_alphabetical_sort), "first_name"),
                        new DateSort("Age", "dob"),
                        new StatusSort("Due Status"),
                        new CursorCommonObjectSort(getResources().getString(R.string.id_sort), "zeir_id")
                };
            }

            @Override
            public String searchHint() {
                return getString(R.string.str_search_hint);
            }
        };
    }


    @Override
    protected SmartRegisterClientsProvider clientsProvider() {
        return null;
    }

    @Override
    protected void onInitialization() {
    }

    @Override
    protected void startRegistration() {
//        ((ChildSmartRegisterActivity) getActivity()).startFormActivity("child_enrollment", null, null);
        ((HouseholdSmartRegisterActivity) getActivity()).startFormActivity("household_registration", null, null);
//        ((HouseholdSmartRegisterActivity) getActivity()).startFormActivity("iycf_counselling_form_lactating_woman", null, null);

//        ((ChildSmartRegisterActivity) getActivity()).startFormActivity("woman_member_registration", null, null);


    }
    public void requestUpdateView(){
        Sortqueries = SortFilterUtil.getSortQuery();
        filters = SortFilterUtil.getFilterQuery();
        super.filterandSortExecute();
    }
    @Override
    protected void onCreation() {
    }

    @Override
    protected void onResumption() {
        super.onResumption();
        getDefaultOptionsProvider();
        if (isPausedOrRefreshList()) {
            initializeQueries();
        }
        updateSearchView();
        try {
           // LoginActivity.setLanguage();
        } catch (Exception e) {

        }

        updateLocationText();
        if (filterMode()) {
            toggleFilterSelection();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        this.getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        View view = inflater.inflate(R.layout.smart_register_activity_customized, container, false);
        mView = view;
        onInitialization();
        setupViews(view);
        onResumption();
        return view;
    }

    @Override
    public void setupViews(View view) {
        super.setupViews(view);
        view.findViewById(R.id.btn_report_month).setVisibility(INVISIBLE);
        view.findViewById(R.id.service_mode_selection).setVisibility(INVISIBLE);
        view.findViewById(R.id.global_search).setVisibility(INVISIBLE);
        view.findViewById(R.id.filter_selection).setVisibility(VISIBLE);
        view.findViewById(R.id.filter_count).setVisibility(INVISIBLE);

        filterSection = view.findViewById(R.id.filter_selection);
        filterSection.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view){
                //show filterandsortfragment action trigger
                ((HouseholdSmartRegisterActivity) getActivity()).switchToSortFilterFragment();
            }
        });

        filterCount = (TextView) view.findViewById(R.id.filter_count);
        filterCount.setVisibility(View.GONE);
        filterCount.setClickable(false);
        filterCount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (view.isClickable()) {
                    filterSection.performClick();
                }
            }
        });

        clientsView.setVisibility(VISIBLE);
        clientsProgressView.setVisibility(View.INVISIBLE);
        setServiceModeViewDrawableRight(null);
        initializeQueries();
        updateSearchView();
        populateClientListHeaderView(view);

        View qrCode = view.findViewById(R.id.scan_qr_code);
        qrCode.setOnClickListener(clientActionHandler);

        backButton = (ImageView) view.findViewById(R.id.back_button);
        nameInitials = (TextView) view.findViewById(R.id.name_inits);

        AllSharedPreferences allSharedPreferences = Context.getInstance().allSharedPreferences();
        String preferredName = allSharedPreferences.getANMPreferredName(allSharedPreferences.fetchRegisteredANM());
        if (!preferredName.isEmpty()) {
            String[] preferredNameArray = preferredName.split(" ");
            String initials = "";
            if (preferredNameArray.length > 1) {
                initials = String.valueOf(preferredNameArray[0].charAt(0)) + String.valueOf(preferredNameArray[1].charAt(0));
            } else if (preferredNameArray.length == 1) {
                initials = String.valueOf(preferredNameArray[0].charAt(0));
            }
            nameInitials.setText(initials);
        }

        View globalSearchButton = mView.findViewById(R.id.global_search);
        globalSearchButton.setOnClickListener(clientActionHandler);

    }

    @Override
    protected void goBack() {
        if (filterMode()) {
            toggleFilterSelection();
        } else {
            DrawerLayout drawer = (DrawerLayout) getActivity().findViewById(R.id.drawer_layout);
            if (!drawer.isDrawerOpen(GravityCompat.START)) {
                drawer.openDrawer(GravityCompat.START);
            }
        }
    }

    @Override
    public boolean onBackPressed() {
        if (filterMode()) {
            toggleFilterSelection();
            return true;
        }
        return false;
    }

    public LocationPickerView getLocationPickerView() {
        return getClinicSelection();
    }

    public void initializeQueries() {
        String tableName = "ec_household";

        HouseholdSmartClientsProvider hhscp = new HouseholdSmartClientsProvider(getActivity(),
                clientActionHandler, context().alertService(), VaccinatorApplication.getInstance().vaccineRepository(), VaccinatorApplication.getInstance().weightRepository(),this);
        clientAdapter = new SmartRegisterPaginatedCursorAdapter(getActivity(), null, hhscp, Context.getInstance().commonrepository(tableName));
        clientsView.setAdapter(clientAdapter);

        setTablename(tableName);
        SmartRegisterQueryBuilder countqueryBUilder = new SmartRegisterQueryBuilder();
        countqueryBUilder.SelectInitiateMainTableCounts(tableName);
        countSelect = countqueryBUilder.mainCondition("");
        mainCondition = "";
        super.CountExecute();
//        countOverDue();
//        countDueOverDue();

        SmartRegisterQueryBuilder queryBUilder = new SmartRegisterQueryBuilder();
        queryBUilder.SelectInitiateMainTable(tableName, new String[]{
                tableName + ".relationalid",
                tableName + ".first_name",
                tableName + ".last_name",
                tableName + ".dob",
                tableName + ".details",
                tableName + ".HHID",
                tableName + ".Date_Of_Reg",
                tableName + ".address1",
                tableName+".last_interacted_with",
                "(select ec_details.value from ec_details where ec_details.key='address2' and ec_details.base_entity_id=ec_household.id) as ward",
                "(select sum(count) from(select count(*) as count from ec_mother where ec_mother.relational_id=ec_household.id union select count(*) as count from ec_child where ec_child.relational_id in ( select ec_mother.id from ec_mother where ec_mother.relational_id=ec_household.id)))as member_count"
        });
        mainSelect = queryBUilder.mainCondition("");
        Sortqueries = ((CursorSortOption) getDefaultOptionsProvider().sortOption()).sort();

        currentlimit = 20;
        currentoffset = 0;

        super.filterandSortInInitializeQueries();

        updateSearchView();
        refresh();
    }

    private class ClientActionHandler implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            CommonPersonObjectClient client = null;
            if (view.getTag() != null && view.getTag() instanceof CommonPersonObjectClient) {
                client = (CommonPersonObjectClient) view.getTag();
            }
            RegisterClickables registerClickables = new RegisterClickables();

            Log.e("-------------------",view.getId() + "");
            switch (view.getId()) {
                case R.id.child_profile_info_layout:
                    Log.e("-------------------","Openning HouseholdDetails");
                    Intent intent = new Intent(getActivity(), HouseholdDetailActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putSerializable(EXTRA_HOUSEHOLD_DETAILS, client);
                    intent.putExtras(bundle);
                    startActivity(intent);
                    HouseholdDetailActivity.isLaunched = true;
                    break;
            }
        }
    }

    public void updateSearchView() {
        textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                filter("Where ((first_name Like '%"+s.toString()+"%' or HHID Like '%"+s.toString()+"%' or contact_phone_number LIKE '%"+s.toString()+"%' )" +
                        " or ec_household.id in (select ec_mother.relational_id from ec_mother where ec_mother.first_name like '%" +s.toString()+"%'"+" or contact_phone_number LIKE '%"+s.toString()+"%')" +
                        "or ec_household.id in (select ec_mother.relational_id from ec_mother where ec_mother.id in (select ec_child.relational_id from ec_child where ec_child.first_name like '%" +s.toString()+"%')))"
                        ,"","");
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        };
        getSearchView().removeTextChangedListener(textWatcher);
        getSearchView().addTextChangedListener(textWatcher);
    }

    public void triggerFilterSelection() {
        if (filterSection != null && !filterMode()) {
            filterSection.performClick();
        }
    }

    private void populateClientListHeaderView(View view) {
        LinearLayout clientsHeaderLayout = (LinearLayout) view.findViewById(org.smartregister.R.id.clients_header_layout);
        clientsHeaderLayout.setVisibility(View.GONE);

        LinearLayout headerLayout = (LinearLayout) getLayoutInflater(null).inflate(R.layout.smart_register_household_header, null);
        clientsView.addHeaderView(headerLayout);
        clientsView.setEmptyView(getActivity().findViewById(R.id.empty_view));

    }

    /*@Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        super.onLoadFinished(loader, cursor);
        // Check if query was issued
        if (searchView != null && searchView.getText().toString().length() > 0) {
            if (cursor.getCount() == 0) {// No search result found
                if (showNoResultDialogHandler != null) {
                    showNoResultDialogHandler.removeCallbacksAndMessages(null);
                    showNoResultDialogHandler = null;
                }

                showNoResultDialogHandler = new Handler();
                showNoResultDialogHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (notInCatchmentDialogFragment == null) {
                            notInCatchmentDialogFragment = new NotInCatchmentDialogFragment();
                        }

                        FragmentTransaction ft = getActivity().getFragmentManager().beginTransaction();
                        Fragment prev = getActivity().getFragmentManager().findFragmentByTag(DIALOG_TAG);
                        if (prev != null) {
                            ft.remove(prev);
                        }
                        ft.addToBackStack(null);
                        if(!notInCatchmentDialogFragment.isVisible()) {
                            notInCatchmentDialogFragment.show(ft, DIALOG_TAG);
                        }
                    }
                }, NO_RESULT_SHOW_DIALOG_DELAY);
            } else {
                if (showNoResultDialogHandler != null) {
                    showNoResultDialogHandler.removeCallbacksAndMessages(null);
                    showNoResultDialogHandler = null;
                }
            }
        }
    }*/

    private String filterSelectionCondition(boolean urgentOnly) {
        String mainCondition = " (inactive != 'true' and lost_to_follow_up != 'true') AND ( ";
        ArrayList<VaccineRepo.Vaccine> vaccines = VaccineRepo.getVaccines("child");

        for (int i = 0; i < vaccines.size(); i++) {
            VaccineRepo.Vaccine vaccine = vaccines.get(i);
            if (i == vaccines.size() - 1) {
                mainCondition += " " + VaccinateActionUtils.addHyphen(vaccine.display()) + " = 'urgent' ";
            } else {
                mainCondition += " " + VaccinateActionUtils.addHyphen(vaccine.display()) + " = 'urgent' or ";
            }
        }

        if (urgentOnly) {
            return mainCondition + " ) ";
        }

        mainCondition += " or ";
        for (int i = 0; i < vaccines.size(); i++) {
            VaccineRepo.Vaccine vaccine = vaccines.get(i);
            if (i == vaccines.size() - 1) {
                mainCondition += " " + VaccinateActionUtils.addHyphen(vaccine.display()) + " = 'normal' ";
            } else {
                mainCondition += " " + VaccinateActionUtils.addHyphen(vaccine.display()) + " = 'normal' or ";
            }
        }

        return mainCondition + " ) ";
    }


    public void countOverDue() {
        String mainCondition = filterSelectionCondition(true);
        int count = count(mainCondition);

        if (filterCount != null) {
            if (count > 0) {
                filterCount.setText(String.valueOf(count));
                filterCount.setVisibility(VISIBLE);
                filterCount.setClickable(true);
            } else {
                filterCount.setVisibility(View.GONE);
                filterCount.setClickable(false);
            }
        }

        ((HouseholdSmartRegisterActivity) getActivity()).updateAdvancedSearchFilterCount(count);
    }

    public void countDueOverDue() {
        String mainCondition = filterSelectionCondition(false);
        int count = count(mainCondition);
        dueOverdueCount = count;
    }

    private int count(String mainConditionString) {

        int count = 0;

        Cursor c = null;

        try {
            SmartRegisterQueryBuilder sqb = new SmartRegisterQueryBuilder(countSelect);
            String query = "";
            if (isValidFilterForFts(commonRepository())) {
                String sql = sqb.countQueryFts(tablename, "", mainConditionString, "");
                List<String> ids = commonRepository().findSearchIds(sql);
                query = sqb.toStringFts(ids, tablename + "." + CommonRepository.ID_COLUMN);
                query = sqb.Endquery(query);
            } else {
                sqb.addCondition(filters);
                query = sqb.orderbyCondition(Sortqueries);
                query = sqb.Endquery(query);
            }

            Log.i(getClass().getName(), query);
            c = commonRepository().rawCustomQueryForAdapter(query);
            c.moveToFirst();
            count = c.getInt(0);

        } catch (Exception e) {
            Log.e(getClass().getName(), e.toString(), e);
        } finally {
            if (c != null) {
                c.close();
            }
        }

        return count;

    }

    private void switchViews(boolean filterSelected) {
        if (filterSelected) {
            if (titleLabelView != null) {
                titleLabelView.setText(String.format(getString(R.string.overdue_due), dueOverdueCount));
            }
            nameInitials.setVisibility(View.GONE);
            backButton.setVisibility(VISIBLE);
        } else {
            if (titleLabelView != null) {
                titleLabelView.setText(getString(R.string.zeir));
            }
            nameInitials.setVisibility(VISIBLE);
            backButton.setVisibility(View.GONE);
        }
    }

    private void toggleFilterSelection() {
        if (filterSection != null) {
            String tagString = "PRESSED";
            if (filterSection.getTag() == null) {
                filter("", "", filterSelectionCondition(false));
                filterSection.setTag(tagString);
                filterSection.setBackgroundResource(R.drawable.transparent_clicked_background);
                switchViews(true);
            } else if (filterSection.getTag().toString().equals(tagString)) {
                filter("", "", "");
                filterSection.setTag(null);
                filterSection.setBackgroundResource(R.drawable.transparent_gray_background);
                switchViews(false);
            }
        }
    }

    private boolean filterMode() {
        return filterSection != null && filterSection.getTag() != null;
    }

}
