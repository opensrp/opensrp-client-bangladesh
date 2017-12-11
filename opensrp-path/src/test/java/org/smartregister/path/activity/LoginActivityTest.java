package org.smartregister.path.activity;

import android.content.Intent;

import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;
import org.robolectric.Robolectric;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.android.controller.ActivityController;
import org.smartregister.path.activity.mockactivity.LoginActivityMock;

import shared.BaseUnitTest;

/**
 * Created by kaderchowdhury on 11/12/17.
 */

public class LoginActivityTest extends BaseUnitTest {

    ActivityController<LoginActivity>controller;
    LoginActivity activity;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        Intent intent = new Intent(RuntimeEnvironment.application,LoginActivity.class);
        controller = Robolectric.buildActivity(LoginActivity.class,intent);
        activity = controller.get();
//        controller.setup();
//        controller.start();
        controller.create();
//        controller.stop();
//        controller.destroy();
    }

    @Test
    public void mockRunnable() {

    }

}
