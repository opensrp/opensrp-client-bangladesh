package org.smartregister.path.activity;

import android.content.Intent;
import android.util.Log;
import android.view.Menu;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.api.support.membermodification.MemberModifier;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.robolectric.Robolectric;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;
import org.smartregister.Context;
import org.smartregister.CoreLibrary;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.domain.ProfileImage;
import org.smartregister.path.activity.mocks.ChildImmunizationActivityMock;
import org.smartregister.path.activity.mocks.DrishtiApplicationMock;
import org.smartregister.path.application.VaccinatorApplication;
import org.smartregister.path.customshadow.LocationPickerViewShadow;
import org.smartregister.path.customshadow.LocationSwitcherToolbarShadow;
import org.smartregister.path.toolbar.BaseToolbar;
import org.smartregister.path.toolbar.LocationSwitcherToolbar;
import org.smartregister.repository.AllSharedPreferences;
import org.smartregister.repository.DetailsRepository;
import org.smartregister.repository.ImageRepository;
import org.smartregister.view.activity.DrishtiApplication;

import java.lang.reflect.Field;
import java.util.HashMap;

import shared.BaseUnitTest;
import shared.customshadows.ImageUtilsShadow;
import shared.customshadows.ImmunizationRowAdapterShadow;
import shared.customshadows.ImmunizationRowCardShadow;
import util.ImageUtils;

/**
 * Created by kaderchowdhury on 04/12/17.
 */
@PrepareForTest({VaccinatorApplication.class, ImageUtils.class})
@Config(shadows = {ImmunizationRowAdapterShadow.class, ImmunizationRowCardShadow.class, ImageUtilsShadow.class})
public class ChildImmunizationActivityTest extends BaseUnitTest {
    @InjectMocks
    private ChildImmunizationActivityMock activity;
    private ActivityController<ChildImmunizationActivityMock> controller;
    @Mock
    private org.smartregister.Context context_;

    private CommonPersonObjectClient childDetails;
    private DetailsRepository detailsRepository;
    private static final String EXTRA_CHILD_DETAILS = "child_details";
    private static final String EXTRA_REGISTER_CLICKABLES = "register_clickables";
    @Mock
    LocationSwitcherToolbar toolbar;
    @Mock
    BaseToolbar baseToolbar;
    @Mock
    AllSharedPreferences allSharedPreferences;
    @Mock
    DrishtiApplication mInstance;
    @Mock
    ImageRepository imageRepository;
    @Mock
    ProfileImage profileImage;
    String mockString = "mockId";
    private static final String LOCATION_HIERARCHY = "locationsHierarchy";
    private static final String MAP = "map";
    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        Intent intent = new Intent(RuntimeEnvironment.application, ChildImmunizationActivityMock.class);
        childDetails = Mockito.spy(new CommonPersonObjectClient("",new HashMap<String, String>(),""));
        HashMap<String, String> columnMaps = new HashMap<String, String>();
        columnMaps.put("dob","1985-07-24T00:00:00.000Z");
        childDetails.setColumnmaps(columnMaps);
        childDetails.setDetails(new HashMap<String, String>());
        intent.putExtra(EXTRA_CHILD_DETAILS,childDetails);
        controller = Robolectric.buildActivity(ChildImmunizationActivityMock.class, intent);
        activity = Mockito.spy(controller.get());
        org.mockito.MockitoAnnotations.initMocks(this);
        CoreLibrary.init(context_);

//        Field field = MemberModifier.field(DrishtiApplication.class,"mInstance");
//        field.setAccessible(true);
//        field.set(DrishtiApplication.class, mInstance);
        PowerMockito.doReturn(context_).when(activity).getOpenSRPContext();
        PowerMockito.doReturn(detailsRepository).when(context_).detailsRepository();
        PowerMockito.doReturn(toolbar).when(activity).getToolbar();

//        DrishtiApplicationMock.setInstance(mInstance);
//        PowerMockito.when(childDetails.entityId()).thenReturn("baseEntityId");
//        PowerMockito.doReturn(context_).when((VaccinatorApplication)mInstance).context();
//        PowerMockito.doReturn(imageRepository).when(context_).imageRepository();
//        PowerMockito.doReturn(profileImage).when(imageRepository).findByEntityId(Mockito.anyString());


//        Mockito.doNothing().when(toolbar).prepareMenu();
//        System.out.println(VaccinatorApplication.getInstance());
        controller.setup();

    }

    @Test
    public void mockTest() {
        Assert.assertNotNull(activity);
    }

    @After
    public void tearDown() {
        destroyController();
        activity = null;
        controller = null;
        context_ = null;
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
