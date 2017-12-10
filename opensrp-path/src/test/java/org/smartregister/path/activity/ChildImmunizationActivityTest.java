package org.smartregister.path.activity;

import android.content.Intent;
import android.util.Log;
import android.view.Menu;

import junit.framework.Assert;

import net.sqlcipher.MatrixCursor;
import net.sqlcipher.database.SQLiteDatabase;

import org.joda.time.DateTime;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.api.support.membermodification.MemberModifier;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.rule.PowerMockRule;
import org.robolectric.Robolectric;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;
import org.smartregister.Context;
import org.smartregister.CoreLibrary;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.domain.Alert;
import org.smartregister.domain.ProfileImage;
import org.smartregister.growthmonitoring.domain.Weight;
import org.smartregister.growthmonitoring.domain.WeightWrapper;
import org.smartregister.growthmonitoring.repository.WeightRepository;
import org.smartregister.immunization.domain.Vaccine;
import org.smartregister.path.activity.mocks.ChildImmunizationActivityMock;
import org.smartregister.path.activity.mocks.DrishtiApplicationMock;
import org.smartregister.path.activity.mocks.ImageRepositoryMock;
import org.smartregister.path.activity.mocks.SQLiteDatabaseMock;
import org.smartregister.path.activity.mocks.VaccineData;
import org.smartregister.path.application.VaccinatorApplication;
import org.smartregister.path.customshadow.MyShadowAsyncTask;
import org.smartregister.path.toolbar.BaseToolbar;
import org.smartregister.path.toolbar.LocationSwitcherToolbar;
import org.smartregister.repository.AllSharedPreferences;
import org.smartregister.repository.DetailsRepository;
import org.smartregister.repository.Repository;
import org.smartregister.view.activity.DrishtiApplication;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import shared.BaseUnitTest;
import shared.VaccinatorApplicationTestVersion;
import shared.customshadows.ImmunizationRowAdapterShadow;
import shared.customshadows.ImmunizationRowCardShadow;
import util.PathConstants;

import static org.powermock.configuration.ConfigurationType.PowerMock;

/**
 * Created by kaderchowdhury on 04/12/17.
 */

@PrepareForTest({DrishtiApplication.class,VaccinatorApplication.class,MyShadowAsyncTask.class})
@Config(shadows = {ImmunizationRowAdapterShadow.class, ImmunizationRowCardShadow.class})
public class ChildImmunizationActivityTest extends BaseUnitTest {
    @InjectMocks
    private ChildImmunizationActivityMock activity;
    private ActivityController<ChildImmunizationActivityMock> controller;
    @Mock
    private org.smartregister.Context context_;

    private CommonPersonObjectClient childDetails;
    @Mock
    private DetailsRepository detailsRepository;
    private static final String EXTRA_CHILD_DETAILS = "child_details";
    private static final String EXTRA_REGISTER_CLICKABLES = "register_clickables";
    @Mock
    LocationSwitcherToolbar toolbar;
    @Mock
    BaseToolbar baseToolbar;
    @Mock
    AllSharedPreferences allSharedPreferences;

    VaccinatorApplication mInstance;

    @Mock
    ImageRepositoryMock imageRepository;

    @Mock
    ProfileImage profileImage;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        Intent intent = new Intent(RuntimeEnvironment.application, ChildImmunizationActivityMock.class);
        childDetails = new CommonPersonObjectClient("baseEntityId",new HashMap<String, String>(),"");
        HashMap<String, String> columnMaps = new HashMap<String, String>();
        columnMaps.put("dob","1985-07-24T00:00:00.000Z");
        childDetails.setColumnmaps(columnMaps);
        childDetails.setDetails(new HashMap<String, String>());
        intent.putExtra(EXTRA_CHILD_DETAILS,childDetails);
        controller = Robolectric.buildActivity(ChildImmunizationActivityMock.class, intent);
        activity = Mockito.spy(controller.get());

        CoreLibrary.init(context_);

        mInstance = VaccinatorApplicationTestVersion.getInstance();
        Mockito.doReturn(context_).when(mInstance).context();
        Mockito.doReturn(context_).when(activity).getOpenSRPContext();
        Mockito.doReturn(detailsRepository).when(context_).detailsRepository();
        Mockito.doReturn(toolbar).when(activity).getToolbar();
        Mockito.doReturn(imageRepository).when(context_).imageRepository();
        Mockito.doReturn(profileImage).when(imageRepository).findByEntityId(Mockito.anyString());
        Mockito.doReturn(allSharedPreferences).when(context_).allSharedPreferences();
        Mockito.doReturn("ANM").when(allSharedPreferences).fetchRegisteredANM();
        controller.setup();
    }

    @Mock
    WeightRepository weightRepository;


    @Test
    public void onWeightTakenTest() {
        WeightWrapper tag = new WeightWrapper();
        tag.setUpdatedWeightDate(new DateTime(),true);
//        tag.setDbKey(0l);
//        tag.setGender(PathConstants.GENDER.FEMALE);
//        tag.setWeight(100f);
        Mockito.when(mInstance.weightRepository()).thenReturn(weightRepository);

        activity.childDetails = childDetails;
        activity.toolbar = toolbar;
        activity.onWeightTaken(tag);
    }

    @Ignore
    @Test
    public void addVaccineGroupTest() throws Exception {

        Intent intent = new Intent(RuntimeEnvironment.application, ChildImmunizationActivityMock.class);
        childDetails = new CommonPersonObjectClient("baseEntityId",new HashMap<String, String>(),"");
        HashMap<String, String> columnMaps = new HashMap<String, String>();
        columnMaps.put("dob","1985-07-24T00:00:00.000Z");
        childDetails.setColumnmaps(columnMaps);
        childDetails.setDetails(new HashMap<String, String>());
        intent.putExtra(EXTRA_CHILD_DETAILS,childDetails);
        ActivityController<ChildImmunizationActivityMock>controller = Robolectric.buildActivity(ChildImmunizationActivityMock.class, intent);
        ChildImmunizationActivityMock activity = controller.get();
        controller.setup();
        activity.addVaccineGroup(-1,new JSONObject(VaccineData.vaccineGroupData),new ArrayList<Vaccine>(),new ArrayList<Alert>());
        activity.finish();
        controller.pause().stop().destroy();
    }


    @Test
    public void testPrivateMethod(){
        Intent intent = new Intent(RuntimeEnvironment.application, ChildImmunizationActivity.class);
        ActivityController<ChildImmunizationActivity>controller = Robolectric.buildActivity(ChildImmunizationActivity.class, intent);
        ChildImmunizationActivity activity = controller.get();
        Weight weight = new Weight();
        activity.childDetails = childDetails;
        genericInvokMethod(activity,"updateWeightViews",1,weight);
    }


    public static Object genericInvokMethod(Object obj, String methodName,
                                            int paramCount, Object... params) {
        Method method;
        Object requiredObj = null;
        Object[] parameters = new Object[paramCount];
        Class<?>[] classArray = new Class<?>[paramCount];
        for (int i = 0; i < paramCount; i++) {
            parameters[i] = params[i];
            classArray[i] = params[i].getClass();
        }
        try {
            method = obj.getClass().getDeclaredMethod(methodName, classArray);
            method.setAccessible(true);
            requiredObj = method.invoke(obj, params);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }

        return requiredObj;
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
