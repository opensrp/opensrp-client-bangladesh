package org.smartregister.growplus.activity;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import junit.framework.Assert;

import net.sqlcipher.Cursor;
import net.sqlcipher.MatrixCursor;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.robolectric.Robolectric;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowLooper;
import org.smartregister.CoreLibrary;
import org.smartregister.commonregistry.CommonPersonObject;
import org.smartregister.commonregistry.CommonRepository;
import org.smartregister.domain.Alert;
import org.smartregister.immunization.repository.VaccineRepository;
import org.smartregister.growplus.R;
import org.smartregister.growplus.activity.mockactivity.ChildSmartRegisterActivityMock;
import org.smartregister.growplus.activity.mockactivity.HouseholdSmartRegisterActivityMock;
import org.smartregister.growplus.activity.mockactivity.WomanImmunizationActivityMock;
import org.smartregister.growplus.activity.mockactivity.WomanSmartRegisterActivityMock;
import org.smartregister.growplus.activity.shadow.CommonRepositoryShadow;
import org.smartregister.growplus.activity.shadow.ContextShadow;
import org.smartregister.growplus.activity.shadow.JsonFormUtilsShadow;
import org.smartregister.growplus.activity.shadow.SecuredActivityShadow;
import org.smartregister.growplus.activity.shadow.SecuredFragmentShadow;
import org.smartregister.growplus.activity.shadow.ShadowContext;
import org.smartregister.growplus.activity.shadow.ShadowContextForRegistryActivity;
import org.smartregister.growplus.customshadow.LocationPickerViewShadow;
import org.smartregister.growplus.customshadow.MyShadowAsyncTask;
import org.smartregister.growplus.fragment.ChildSmartRegisterFragment;
import org.smartregister.growplus.fragment.HouseholdSmartRegisterFragment;
import org.smartregister.growplus.fragment.WomanSmartRegisterFragment;
import org.smartregister.repository.DetailsRepository;
import org.smartregister.service.AlertService;
import org.smartregister.service.ZiggyService;
import org.smartregister.view.controller.ANMLocationController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import shared.BaseUnitTest;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;

/**
 * Created by Raihan Ahmed on 18/12/17.
 */
@Config(shadows = {SecuredActivityShadow.class, LocationPickerViewShadow.class, JsonFormUtilsShadow.class, MyShadowAsyncTask.class, CommonRepositoryShadow.class, SecuredFragmentShadow.class, ShadowContextForRegistryActivity.class})
public class WomanSmartRegisterActivityTest extends BaseUnitTest {


    WomanSmartRegisterActivityMock activity;
    private Map<String, String> details;
    ActivityController<WomanSmartRegisterActivityMock> controller;
    List<Alert> alertList = new ArrayList<>();

    @Mock
    AlertService alertService;

    @Mock
    VaccineRepository vaccineRepository;

    @Mock
    private ZiggyService ziggyService;

    @Mock
    private org.smartregister.Context context_;

    @Mock
    private Context applicationContext;

    @Mock
    private CommonRepository commonRepository;

    @Mock
    private CoreLibrary coreLibrary;

    @Mock
    private ANMLocationController anmLocationController;

    @Mock
    private org.smartregister.repository.AllSharedPreferences allSharedPreferences;
//    @Mock
//    CommonPersonObject personObject;

    @Mock
    private DetailsRepository detailsRepository;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        details = new HashMap<>();
        context_ = ShadowContextForRegistryActivity.getInstance();
        SecuredFragmentShadow.mContext = context_;
        ShadowContextForRegistryActivity.commonRepository = commonRepository;
        String[] columns = new String[]{"_id", "relationalid", "first_name", "dob", "details", "HHID", "Date_Of_Reg", "address1"};
        MatrixCursor matrixCursor = new MatrixCursor(columns);
        matrixCursor.addRow(new Object[]{"1", "relationalid", "first_name", "dob", "details", "HHID", "Date_Of_Reg", "address1"});
        matrixCursor.addRow(new Object[]{"2", "relationalid", "first_name", "dob", "details", "HHID", "Date_Of_Reg", "address1"});
        for (int i = 3; i < 22; i++) {
            matrixCursor.addRow(new Object[]{"" + i, "relationalid" + i, "first_name" + i, "dob" + i, "details+i", "HHID" + i, "Date_Of_Reg" + i, "address"+i});
        }
        CommonPersonObject personObject = new CommonPersonObject("caseID","relationalID",new HashMap<String, String>(),"type");
        personObject.setColumnmaps(details);
        details.put("lmp","2018-01-01");
        WomanSmartRegisterActivityMock.setmContext(context_);
        when(context_.updateApplicationContext(isNull(Context.class))).thenReturn(context_);
        when(context_.updateApplicationContext(any(Context.class))).thenReturn(context_);
        when(context_.IsUserLoggedOut()).thenReturn(false);
        when(context_.applicationContext()).thenReturn(applicationContext);
        when(context_.anmLocationController()).thenReturn(anmLocationController);
        when(context_.allSharedPreferences()).thenReturn(allSharedPreferences);
        when(allSharedPreferences.fetchRegisteredANM()).thenReturn("Test User");
        when(allSharedPreferences.getANMPreferredName(anyString())).thenReturn("Test User");
        when(context_.commonrepository(anyString())).thenReturn(commonRepository);
        when(context_.detailsRepository()).thenReturn(getDetailsRepository());
        when(commonRepository.rawCustomQueryForAdapter(anyString())).thenReturn(matrixCursor);
        when(context_.ziggyService()).thenReturn(ziggyService);
        when(commonRepository.readAllcommonforCursorAdapter(any(Cursor.class))).thenReturn(personObject);
        Mockito.doReturn(alertService).when(context_).alertService();
        Mockito.doReturn(alertList).when(alertService).findByEntityIdAndAlertNames(Mockito.anyString(),Mockito.any(String[].class));
        CoreLibrary.init(context_);


        Intent intent = new Intent(RuntimeEnvironment.application,WomanSmartRegisterActivityMock.class);
        controller = Robolectric.buildActivity(WomanSmartRegisterActivityMock.class,intent);
        activity = controller.create()
                .start()
                .resume()
                .visible()
                .get();
//controller.create();
    }

    @Test
    public void assertActivityNotNull() {
        ((WomanSmartRegisterFragment)activity.mBaseFragment).refresh();
        Assert.assertNotNull(activity);
    }

    @Test
    public void listViewNavigationShouldWorkIfClientsSpanMoreThanOnePage() throws InterruptedException {
        ((WomanSmartRegisterFragment) activity.mBaseFragment).refresh();
        final ListView list = (ListView) activity.findViewById(R.id.list);
        ViewGroup footer = (ViewGroup) tryGetAdapter(list).getView(20, null, null);
        Button nextButton = (Button) activity.findViewById(R.id.btn_next_page);
        Button previousButton = (Button) activity.findViewById(R.id.btn_previous_page);
        TextView info = (TextView) activity.findViewById(R.id.txt_page_info);
        int count = tryGetAdapter(list).getCount();
        nextButton.performClick();
        assertEquals("Page 1 of 1", info.getText());
        previousButton.performClick();
    }


    private ListAdapter tryGetAdapter(final ListView list) {
        ListAdapter adapter = list.getAdapter();
        while (adapter.getCount() == 0) {
            ShadowLooper.idleMainLooper(1000);
            adapter = list.getAdapter();
        }
        return adapter;
    }


    @After
    public void tearDown() {
        destroyController();
        activity = null;
        controller = null;
    }

    private void destroyController() {
        try {
            activity.finish();
            controller.pause().stop().destroy(); //destroy controller if we can

        } catch (Exception e) {
            Log.e(getClass().getCanonicalName(), e.getMessage());
        }

        System.gc();
    }

    private DetailsRepository getDetailsRepository() {

        return new DetailsRepositoryLocal();
    }

    class DetailsRepositoryLocal extends DetailsRepository {

        @Override
        public Map<String, String> getAllDetailsForClient(String baseEntityId) {
            return details;
        }
    }
}
