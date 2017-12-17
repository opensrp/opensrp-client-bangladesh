package org.smartregister.path.activity;

import android.content.Intent;
import android.util.Log;
import android.view.View;

import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.robolectric.Robolectric;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;
import org.smartregister.clientandeventmodel.Gender;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.domain.Alert;
import org.smartregister.domain.AlertStatus;
import org.smartregister.domain.ProfileImage;
import org.smartregister.growthmonitoring.domain.Weight;
import org.smartregister.growthmonitoring.domain.WeightWrapper;
import org.smartregister.growthmonitoring.repository.WeightRepository;
import org.smartregister.immunization.domain.ServiceRecord;
import org.smartregister.immunization.domain.ServiceWrapper;
import org.smartregister.immunization.domain.Vaccine;
import org.smartregister.immunization.domain.VaccineWrapper;
import org.smartregister.immunization.repository.RecurringServiceRecordRepository;
import org.smartregister.immunization.repository.RecurringServiceTypeRepository;
import org.smartregister.immunization.repository.VaccineRepository;
import org.smartregister.immunization.view.VaccineGroup;
import org.smartregister.path.R;
import org.smartregister.path.activity.mockactivity.ChildImmunizationActivityMock;
import org.smartregister.path.activity.mocks.ImageRepositoryMock;
import org.smartregister.path.activity.shadow.CoreLibraryShadow;
import org.smartregister.path.activity.shadow.ImageRepositoryShadow;
import org.smartregister.path.activity.shadow.JsonFormUtilsShadow;
import org.smartregister.path.activity.shadow.VaccinateActionUtilsShadow;
import org.smartregister.path.activity.shadow.VaccineGroupShadow;
import org.smartregister.path.application.VaccinatorApplication;
import org.smartregister.path.customshadow.MyShadowAsyncTask;
import org.smartregister.path.customshadow.ViewGroupShadow;
import org.smartregister.path.toolbar.BaseToolbar;
import org.smartregister.path.toolbar.LocationSwitcherToolbar;
import org.smartregister.repository.AllSharedPreferences;
import org.smartregister.repository.DetailsRepository;
import org.smartregister.repository.ImageRepository;
import org.smartregister.service.AlertService;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import shared.BaseUnitTest;
import shared.VaccinatorApplicationTestVersion;
import shared.customshadows.ImageUtilsShadow;
import shared.customshadows.ImmunizationRowAdapterShadow;
import shared.customshadows.ImmunizationRowCardShadow;
import util.PathConstants;

/**
 * Created by kaderchowdhury on 04/12/17.
 */
@Config(shadows = {ImmunizationRowAdapterShadow.class, ImmunizationRowCardShadow.class ,ViewGroupShadow.class,MyShadowAsyncTask.class, ImageUtilsShadow.class, ImageRepositoryShadow.class, JsonFormUtilsShadow.class, VaccinateActionUtilsShadow.class,VaccineGroupShadow.class})
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
    ImageRepository imageRepository;

    @Mock
    ProfileImage profileImage;

    @Mock
    AlertService alertService;
    HashMap<String,String>details = new HashMap<>();
    List<Alert>alertList = new ArrayList<>();
    List<Vaccine> vaccineList = new ArrayList<>();
    List<ServiceRecord> serviceRecordList = new ArrayList<>();

    Weight weight = new Weight();

    @Mock
    RecurringServiceTypeRepository recurringServiceTypeRepository;
    @Mock
    VaccineRepository vaccineRepository;
    @Mock
    RecurringServiceRecordRepository recurringServiceRecordRepository;
    @Mock
    WeightRepository weightRepository;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        Intent intent = new Intent(RuntimeEnvironment.application, ChildImmunizationActivityMock.class);
        childDetails = new CommonPersonObjectClient("baseEntityId",new HashMap<String, String>(),"");
        HashMap<String, String> columnMaps = new HashMap<String, String>();
        columnMaps.put("dob","1985-07-24T00:00:00.000Z");
        columnMaps.put("gender", Gender.FEMALE.name());
        columnMaps.put(PathConstants.KEY.BIRTH_WEIGHT,"100.0");
        childDetails.setColumnmaps(columnMaps);
        childDetails.setDetails(details);
        intent.putExtra(EXTRA_CHILD_DETAILS,childDetails);
        controller = Robolectric.buildActivity(ChildImmunizationActivityMock.class, intent);
        activity = controller.get();
        details = new HashMap<String,String>();
//        CoreLibrary.init(context_);

        mInstance = VaccinatorApplicationTestVersion.getInstance();
        Mockito.doReturn(context_).when(mInstance).context();


        Mockito.doReturn(detailsRepository).when(context_).detailsRepository();
        Mockito.doReturn(details).when(detailsRepository).getAllDetailsForClient(Mockito.anyString());
        Mockito.doReturn(imageRepository).when(context_).imageRepository();
        Mockito.doReturn(null).when(imageRepository).findByEntityId(Mockito.anyString());
        Mockito.doReturn(allSharedPreferences).when(context_).allSharedPreferences();
        Mockito.doReturn("ANM").when(allSharedPreferences).fetchRegisteredANM();

        Mockito.doReturn(alertService).when(context_).alertService();
        Mockito.doReturn(vaccineRepository).when(mInstance).vaccineRepository();
        Mockito.doReturn(weightRepository).when(mInstance).weightRepository();
        Mockito.doReturn(recurringServiceRecordRepository).when(mInstance).recurringServiceRecordRepository();
        Mockito.doNothing().when(recurringServiceRecordRepository).deleteServiceRecord(Mockito.anyLong());
        Mockito.doReturn(serviceRecordList).when(recurringServiceRecordRepository).findByEntityId(Mockito.anyString());

        setDataForTest();
        Mockito.doReturn(alertList).when(alertService).findByEntityIdAndAlertNames(Mockito.anyString(),Mockito.any(String[].class));
        Mockito.doReturn(vaccineList).when(vaccineRepository).findByEntityId(Mockito.anyString());
        Mockito.doReturn(weight).when(weightRepository).findUnSyncedByEntityId(Mockito.anyString());
        activity.childDetails = childDetails;
        activity.toolbar = toolbar;
        controller.setup();
    }


    @Test
    public void onWeightTakenTest() {
        WeightWrapper tag = new WeightWrapper();
        tag.setUpdatedWeightDate(new DateTime(),true);

        tag.setDbKey(0l);
        tag.setGender(PathConstants.GENDER.FEMALE);
        tag.setWeight(100f);
        Mockito.when(mInstance.weightRepository()).thenReturn(weightRepository);
        Mockito.doReturn(weight).when(weightRepository).find(Mockito.anyLong());
        activity.childDetails = childDetails;
        activity.toolbar = toolbar;
        activity.onWeightTaken(tag);
        activity.findViewById(R.id.growth_chart_button).performClick();
    }



    @Test
    public void testupdateVaccineGroupViews(){
        VaccineGroup vaccineGroup = new VaccineGroup(RuntimeEnvironment.application);
        vaccineGroup.setModalOpen(true);
        VaccineWrapper tag = new VaccineWrapper();
        tag.setDbKey(0l);
        tag.setUpdatedVaccineDate(new DateTime(0l),true);
        Mockito.doNothing().when(vaccineRepository).deleteVaccine(0l);

        activity.childDetails = childDetails;
        activity.onUndoVaccination(tag,vaccineGroup);
        controller.resume();//updates vaccinegroup
    }

    @Test
    public void testupdateOnVaccinateToday(){
        VaccineGroup vaccineGroup = new VaccineGroup(RuntimeEnvironment.application);
        vaccineGroup.setModalOpen(true);
        VaccineWrapper tag = new VaccineWrapper();
        tag.setDbKey(0l);
        tag.setName("OPV0");
        tag.setUpdatedVaccineDate(new DateTime(0l),true);
        Mockito.doNothing().when(vaccineRepository).deleteVaccine(0l);
        Vaccine vaccine = new Vaccine();
        vaccine.setName("OPV0");
        activity.childDetails = childDetails;
        Mockito.doReturn(vaccine).when(vaccineRepository).find(Mockito.anyLong());
        ArrayList<VaccineWrapper>list = new ArrayList<>();
        list.add(tag);
        View v = new View(RuntimeEnvironment.application);
        activity.onVaccinateToday(list,v);
        activity.onVaccinateEarlier(list,v);


    }

    @Test
    public void testupdateUndoServiceViews(){
        View v = new View(RuntimeEnvironment.application);
        ServiceWrapper tag = new ServiceWrapper();
        tag.setDbKey(0l);
        activity.childDetails = childDetails;
        activity.onUndoService(tag,v);
        activity.onGiveEarlier(tag,v);
        activity.onGiveToday(tag,v);

        controller.resume();
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

    public void setDataForTest() {
//        activity.set
        Alert alert = new Alert("","","", AlertStatus.normal,"","");
        alertList.add(alert);
        Vaccine vaccine = new Vaccine();
        vaccineList.add(vaccine);
    }

}
