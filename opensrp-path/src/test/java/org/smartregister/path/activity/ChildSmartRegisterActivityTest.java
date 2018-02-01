package org.smartregister.path.activity;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import net.sqlcipher.MatrixCursor;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.Robolectric;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;
import org.smartregister.CoreLibrary;
import org.smartregister.commonregistry.CommonRepository;
import org.smartregister.path.activity.shadow.CommonRepositoryShadow;
import org.smartregister.path.activity.shadow.ContextShadow;
import org.smartregister.path.activity.shadow.JsonFormUtilsShadow;
import org.smartregister.path.activity.shadow.SecuredActivityShadow;
import org.smartregister.path.application.VaccinatorApplication;
import org.smartregister.path.customshadow.MyShadowAsyncTask;
import org.smartregister.view.controller.ANMLocationController;

import shared.BaseUnitTest;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Created by kaderchowdhury on 06/12/17.
 */
@Config(shadows = {SecuredActivityShadow.class,ContextShadow.class, JsonFormUtilsShadow.class, MyShadowAsyncTask.class, CommonRepositoryShadow.class})
public class ChildSmartRegisterActivityTest extends BaseUnitTest {

    ChildSmartRegisterActivity activity;
    ActivityController<ChildSmartRegisterActivity>controller;
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

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        String[] columns = new String[]{"_id", "relationalid", "FWHOHFNAME", "FWGOBHHID", "FWJIVHHID", "existing_Mauzapara", "ELCO"};
        MatrixCursor matrixCursor = new MatrixCursor(columns);
        matrixCursor.addRow(new Object[]{"1", "relationalid1", "FWHOHFNAME1", "FWGOBHHID1", "FWJIVHHID1", "existing_Mauzapara1", "ELCO1"});
        matrixCursor.addRow(new Object[]{"2", "relationalid2", "FWHOHFNAME2", "FWGOBHHID2", "FWJIVHHID2", "existing_Mauzapara2", "ELCO2"});
        for (int i = 3; i < 22; i++) {
            matrixCursor.addRow(new Object[]{"" + i, "relationalid" + i, "FWHOHFNAME" + i, "FWGOBHHID" + i, "FWJIVHHID+i", "existing_Mauzapara" + i, "ELCO" + i});
        }
        CoreLibrary.init(context_);

        when(context_.applicationContext()).thenReturn(applicationContext);
        when(context_.anmLocationController()).thenReturn(anmLocationController);
        when(context_.commonrepository(anyString())).thenReturn(commonRepository);
        when(commonRepository.rawCustomQueryForAdapter(anyString())).thenReturn(matrixCursor);
        Intent intent = new Intent(RuntimeEnvironment.application,ChildSmartRegisterActivity.class);
        controller = Robolectric.buildActivity(ChildSmartRegisterActivity.class,intent);
        activity = controller.get();
        controller.setup();
//controller.create();
    }

    @Test
    public void mockRunnable() {

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
}
