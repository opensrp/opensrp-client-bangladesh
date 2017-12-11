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
import org.smartregister.immunization.domain.VaccineType;
import org.smartregister.path.customshadow.MyShadowAsyncTask;

import shared.BaseUnitTest;

/**
 * Created by kaderchowdhury on 06/12/17.
 */
@Config(shadows = MyShadowAsyncTask.class)
public class StockControlActivityTest extends BaseUnitTest {

    StockControlActivity activity;
    ActivityController<StockControlActivity>controller;
    VaccineType vaccineType;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        vaccineType = new VaccineType(0l,1,"","","","");
        Intent intent = new Intent(RuntimeEnvironment.application,StockControlActivity.class);
        intent.putExtra("vaccine_type",vaccineType);
        controller = Robolectric.buildActivity(StockControlActivity.class,intent);
        activity = controller.get();
//        controller.setup();
        controller.create();
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
