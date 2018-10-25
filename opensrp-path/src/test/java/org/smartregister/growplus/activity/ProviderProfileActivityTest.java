package org.smartregister.growplus.activity;

import android.content.Intent;
import android.util.Log;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;
import org.robolectric.Robolectric;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.android.controller.ActivityController;
import org.smartregister.path.activity.*;

import shared.BaseUnitTest;

/**
 * Created by kaderchowdhury on 06/12/17.
 */

public class ProviderProfileActivityTest extends BaseUnitTest {

    ActivityController<org.smartregister.path.activity.ProviderProfileActivity>controller;
    org.smartregister.path.activity.ProviderProfileActivity activity;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        Intent intent = new Intent(RuntimeEnvironment.application, org.smartregister.path.activity.ProviderProfileActivity.class);
        controller = Robolectric.buildActivity(org.smartregister.path.activity.ProviderProfileActivity.class,intent);
        activity = controller.get();
//        controller.create();
//        controller.setup();
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
