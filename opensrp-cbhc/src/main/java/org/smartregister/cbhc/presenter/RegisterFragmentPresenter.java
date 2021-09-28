package org.smartregister.cbhc.presenter;

import android.util.Log;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.smartregister.cbhc.R;
import org.smartregister.cbhc.contract.AdvancedSearchContract;
import org.smartregister.cbhc.contract.RegisterFragmentContract;
import org.smartregister.cbhc.cursor.AdvancedMatrixCursor;
import org.smartregister.cbhc.interactor.AdvancedSearchInteractor;
import org.smartregister.cbhc.model.RegisterFramentModel;
import org.smartregister.cbhc.notification.NotificationServiceJob;
import org.smartregister.cbhc.util.DBConstants;
import org.smartregister.configurableviews.model.Field;
import org.smartregister.configurableviews.model.RegisterConfiguration;
import org.smartregister.configurableviews.model.ViewConfiguration;
import org.smartregister.domain.Response;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class RegisterFragmentPresenter implements RegisterFragmentContract.Presenter, AdvancedSearchContract.InteractorCallBack {

    private WeakReference<RegisterFragmentContract.View> viewReference;

    private RegisterFragmentContract.Model model;

    private RegisterConfiguration config;

    protected AdvancedSearchContract.Interactor interactor;

    protected AdvancedMatrixCursor matrixCursor;

    protected Set<org.smartregister.configurableviews.model.View> visibleColumns = new TreeSet<>();
    private String viewConfigurationIdentifier;

    public RegisterFragmentPresenter(RegisterFragmentContract.View view, String viewConfigurationIdentifier) {
        this.viewReference = new WeakReference<>(view);
        this.model = new RegisterFramentModel();
        this.viewConfigurationIdentifier = viewConfigurationIdentifier;
        this.config = model.defaultRegisterConfiguration();

        this.interactor = new AdvancedSearchInteractor();
    }

    @Override
    public void processViewConfigurations() {
        if (StringUtils.isBlank(viewConfigurationIdentifier)) {
            return;
        }

        ViewConfiguration viewConfiguration = model.getViewConfiguration(viewConfigurationIdentifier);
        if (viewConfiguration != null) {
            config = (RegisterConfiguration) viewConfiguration.getMetadata();
            setVisibleColumns(model.getRegisterActiveColumns(viewConfigurationIdentifier));
        }

        if (config.getSearchBarText() != null && getView() != null) {
            getView().updateSearchBarHint(config.getSearchBarText());
        }
    }

    @Override
    public void initializeQueries(String mainCondition) {
        String tableName = DBConstants.HOUSEHOLD_TABLE_NAME;

        String countSelect = model.countSelect(tableName, mainCondition);
        String mainSelect = model.mainSelect(tableName, mainCondition);

        getView().initializeQueryParams(tableName, countSelect, mainSelect);
        getView().initializeAdapter(visibleColumns);

        getView().countExecute();
        getView().filterandSortInInitializeQueries();

    }



    @Override
    public void startSync() {

        //ServiceTools.startSyncService(getActivity());

    }
    
    @Override
    public void updateSortAndFilter(List<Field> filterList, Field sortField) {
        String filterText = model.getFilterText(filterList, getView().getString(R.string.filter));
        String sortText = model.getSortText(sortField);

        getView().updateFilterAndFilterStatus(filterText, sortText);
    }

    @Override
    public void searchGlobally(String ancId) {
        getView().showProgressView();

        Map<String, String> editMap = model.createEditMap(ancId);
        interactor.search(editMap, this, ancId);
    }

    @Override
    public void onResultsFound(Response<String> response, String ancId) {
    	JSONArray jsonArray = model.getJsonArray(response);
    	
    	if (jsonArray == null || jsonArray.length() <= 0) {
    		getView().showNotFoundPopup(ancId);
    	} else {
    		matrixCursor = model.createMatrixCursor(response);
			
    		getView().recalculatePagination(matrixCursor);
			
    		getView().filterandSortInInitializeQueries();
    		getView().hideProgressView();
    	}
    }


    protected RegisterFragmentContract.View getView() {
        if (viewReference != null)
            return viewReference.get();
        else
            return null;
    }

    private void setVisibleColumns(Set<org.smartregister.configurableviews.model.View> visibleColumns) {
        this.visibleColumns = visibleColumns;
    }

    public void setModel(RegisterFragmentContract.Model model) {
        this.model = model;
    }

    public void setMatrixCursor(AdvancedMatrixCursor matrixCursor) {
        this.matrixCursor = matrixCursor;
    }

    public AdvancedMatrixCursor getMatrixCursor() {
        return matrixCursor;
    }

    public void setInteractor(AdvancedSearchContract.Interactor interactor) {
        this.interactor = interactor;
    }
}