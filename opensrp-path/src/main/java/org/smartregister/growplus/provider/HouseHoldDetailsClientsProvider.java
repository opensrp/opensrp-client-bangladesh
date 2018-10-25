package org.smartregister.path.provider;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AbsListView;

import org.smartregister.cursoradapter.SmartRegisterCLientsProviderForCursorAdapter;
import org.smartregister.growthmonitoring.repository.WeightRepository;
import org.smartregister.immunization.repository.VaccineRepository;
import org.smartregister.growplus.fragment.HouseholdSmartRegisterFragment;
import org.smartregister.service.AlertService;
import org.smartregister.view.contract.SmartRegisterClient;
import org.smartregister.view.contract.SmartRegisterClients;
import org.smartregister.view.dialog.FilterOption;
import org.smartregister.view.dialog.ServiceModeOption;
import org.smartregister.view.dialog.SortOption;
import org.smartregister.view.viewholder.OnClickFormLauncher;

/**
 * Created by habib on 7/23/17.
 */
public class HouseHoldDetailsClientsProvider implements SmartRegisterCLientsProviderForCursorAdapter {

    private final LayoutInflater inflater;
    private final Context context;
    private final View.OnClickListener onClickListener;
    AlertService alertService;
    VaccineRepository vaccineRepository;
    WeightRepository weightRepository;
    private final AbsListView.LayoutParams clientViewLayoutParams;
    private static final String VACCINES_FILE = "vaccines.json";
    String locationId = "";
    private HouseholdSmartRegisterFragment mBaseFragment;
    private static final int REQUEST_CODE_GET_JSON = 3432;

    public HouseHoldDetailsClientsProvider(LayoutInflater inflater, Context context, View.OnClickListener onClickListener, AbsListView.LayoutParams clientViewLayoutParams, HouseholdSmartRegisterFragment mBaseFragment) {
        this.inflater = inflater;
        this.context = context;
        this.onClickListener = onClickListener;
        this.clientViewLayoutParams = clientViewLayoutParams;
        this.mBaseFragment = mBaseFragment;
    }



    @Override
    public void getView(Cursor cursor, SmartRegisterClient smartRegisterClient, View view) {

    }

    @Override
    public SmartRegisterClients updateClients(FilterOption villageFilter, ServiceModeOption serviceModeOption, FilterOption searchFilter, SortOption sortOption) {
        return null;
    }

    @Override
    public void onServiceModeSelected(ServiceModeOption serviceModeOption) {

    }

    @Override
    public OnClickFormLauncher newFormLauncher(String formName, String entityId, String metaData) {
        return null;
    }

    @Override
    public LayoutInflater inflater() {
        return inflater();
    }

    @Override
    public View inflatelayoutForCursorAdapter() {
        return null;
    }
}
