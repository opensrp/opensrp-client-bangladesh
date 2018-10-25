package org.smartregister.path.wrapper;

import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;
import org.smartregister.domain.Alert;
import org.smartregister.immunization.domain.Vaccine;

import java.util.ArrayList;
import java.util.List;

import shared.BaseUnitTest;

/**
 * Created by kaderchowdhury on 10/12/17.
 */

public class VaccineViewRecordUpdateWrapperTest extends BaseUnitTest {

    VaccineViewRecordUpdateWrapper vaccineViewRecordUpdateWrapper;
    String dobString = "";
    List<Vaccine> vaccines = new ArrayList<>();
    List<Alert> alertList = new ArrayList<>();
    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        vaccineViewRecordUpdateWrapper = new VaccineViewRecordUpdateWrapper();
    }

    @Test
    public void getVaccines() {
        vaccineViewRecordUpdateWrapper.getVaccines();
    }

    @Test
    public void setVaccines() {
        vaccineViewRecordUpdateWrapper.setVaccines(vaccines);
    }

    @Test
    public void getAlertList() {
        vaccineViewRecordUpdateWrapper.getAlertList();
    }

    @Test
    public void setAlertList() {
        vaccineViewRecordUpdateWrapper.setAlertList(alertList);
    }

    @Test
    public void getDobString() {
        vaccineViewRecordUpdateWrapper.getDobString();
    }

    @Test
    public void setDobString() {
        vaccineViewRecordUpdateWrapper.setDobString(dobString);
    }
}
