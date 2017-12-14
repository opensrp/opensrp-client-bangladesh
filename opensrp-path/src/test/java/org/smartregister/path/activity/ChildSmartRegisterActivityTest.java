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

import shared.BaseUnitTest;

/**
 * Created by kaderchowdhury on 06/12/17.
 */

public class ChildSmartRegisterActivityTest extends BaseUnitTest {

    ChildSmartRegisterActivity activity;
    ActivityController<ChildSmartRegisterActivity>controller;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        Intent intent = new Intent(RuntimeEnvironment.application,ChildSmartRegisterActivity.class);
        controller = Robolectric.buildActivity(ChildSmartRegisterActivity.class,intent);
        activity = controller.get();
//        controller.setup();
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