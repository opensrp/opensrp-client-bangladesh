package org.smartregister.path.activity;

import android.content.Intent;

import org.junit.Before;
import org.junit.Test;
import org.robolectric.Robolectric;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;
import org.smartregister.clientandeventmodel.Gender;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.path.activity.shadow.BaseActivityShadow;
import org.smartregister.path.activity.shadow.ContextShadow;
import org.smartregister.path.activity.shadow.JsonFormUtilsShadow;
import org.smartregister.path.activity.shadow.LocationSwitcherToolbarShadow;
import org.smartregister.path.customshadow.MyShadowAsyncTask;

import java.util.HashMap;

import shared.BaseUnitTest;
import shared.customshadows.LocationPickerViewShadow;
import util.PathConstants;

/**
 * Created by kaderchowdhury on 17/12/17.
 */
@Config(shadows = {ContextShadow.class, JsonFormUtilsShadow.class, MyShadowAsyncTask.class,BaseActivityShadow.class})
public class WomanImmunizationActivityTest extends BaseUnitTest {

    private WomanImmunizationActivity activity;
    private ActivityController<WomanImmunizationActivity>controller;
    HashMap<String,String>details = new HashMap<>();
    private static final String EXTRA_CHILD_DETAILS = "child_details";

    @Before
    public void setUp() {

        Intent intent = new Intent(RuntimeEnvironment.application,WomanImmunizationActivity.class);
        CommonPersonObjectClient childDetails = new CommonPersonObjectClient("baseEntityId",new HashMap<String, String>(),"");
        HashMap<String, String> columnMaps = new HashMap<String, String>();
        columnMaps.put("dob","1985-07-24T00:00:00.000Z");
        columnMaps.put("gender", Gender.FEMALE.name());
        columnMaps.put(PathConstants.KEY.BIRTH_WEIGHT,"100.0");
        childDetails.setColumnmaps(columnMaps);
        childDetails.setDetails(details);
        intent.putExtra(EXTRA_CHILD_DETAILS,childDetails);
        controller = Robolectric.buildActivity(WomanImmunizationActivity.class,intent);
        activity = controller.get();
        controller.setup();
    }

    @Test
    public void mockRunable() {

    }
}
