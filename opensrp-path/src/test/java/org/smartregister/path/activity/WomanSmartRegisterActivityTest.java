package org.smartregister.path.activity;

import android.content.Intent;
import android.util.Log;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;
import org.robolectric.Robolectric;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;
import org.smartregister.path.activity.shadow.CommonRepositoryShadow;
import org.smartregister.path.activity.shadow.ContextShadow;
import org.smartregister.path.activity.shadow.JsonFormUtilsShadow;
import org.smartregister.path.activity.shadow.SecuredActivityShadow;
import org.smartregister.path.customshadow.MyShadowAsyncTask;

import shared.BaseUnitTest;

/**
 * Created by kaderchowdhury on 18/12/17.
 */
@Config(shadows = {SecuredActivityShadow.class,ContextShadow.class, JsonFormUtilsShadow.class, MyShadowAsyncTask.class, CommonRepositoryShadow.class})
public class WomanSmartRegisterActivityTest extends BaseUnitTest {
    WomanSmartRegisterActivity activity;
    ActivityController<WomanSmartRegisterActivity> controller;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        Intent intent = new Intent(RuntimeEnvironment.application,WomanSmartRegisterActivity.class);
        controller = Robolectric.buildActivity(WomanSmartRegisterActivity.class,intent);
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
