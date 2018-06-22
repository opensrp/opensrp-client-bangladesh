package org.smartregister.growplus.activity;

import android.content.Intent;
import android.util.Log;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.MockitoAnnotations;
import org.robolectric.Robolectric;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.android.controller.ActivityController;

import shared.BaseUnitTest;

/**
 * Created by kaderchowdhury on 06/12/17.
 */
@Ignore
public class SettingsActivityTest extends BaseUnitTest {

    SettingsActivity activity;
    ActivityController<SettingsActivity>controller;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        Intent intent = new Intent(RuntimeEnvironment.application,SettingsActivity.class);
        controller = Robolectric.buildActivity(SettingsActivity.class,intent);
        activity = controller.get();

    }

    @Test
    public void mockRunnable() {
        controller.setup();
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
