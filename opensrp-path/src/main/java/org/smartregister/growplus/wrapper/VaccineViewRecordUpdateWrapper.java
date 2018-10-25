package org.smartregister.growplus.wrapper;

import org.smartregister.domain.Alert;
import org.smartregister.immunization.domain.Vaccine;
import org.smartregister.path.wrapper.*;

import java.util.List;

/**
 * Created by onaio on 14/09/2017.
 */

public class VaccineViewRecordUpdateWrapper extends org.smartregister.path.wrapper.BaseViewRecordUpdateWrapper {

    private List<Vaccine> vaccines;
    private List<Alert> alertList;
    private String dobString;

    public List<Vaccine> getVaccines() {
        return vaccines;
    }

    public void setVaccines(List<Vaccine> vaccines) {
        this.vaccines = vaccines;
    }

    public List<Alert> getAlertList() {
        return alertList;
    }

    public void setAlertList(List<Alert> alertList) {
        this.alertList = alertList;
    }

    public String getDobString() {
        return dobString;
    }

    public void setDobString(String dobString) {
        this.dobString = dobString;
    }
}
