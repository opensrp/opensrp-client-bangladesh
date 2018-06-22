package org.smartregister.growplus.activity.shadow;

import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.shadow.api.Shadow;
import org.smartregister.domain.Alert;
import org.smartregister.domain.AlertStatus;
import org.smartregister.immunization.domain.Vaccine;
import org.smartregister.immunization.util.VaccinateActionUtils;

import java.util.Date;

/**
 * Created by kaderchowdhury on 14/12/17.
 */
@Implements(VaccinateActionUtils.class)
public class VaccinateActionUtilsShadow extends Shadow {
    @Implementation
    public static void addBcg2SpecialVaccine(android.content.Context context, org.json.JSONObject vaccineGroupObject, java.util.List<org.smartregister.immunization.domain.Vaccine> vaccineList) {

    }

    @Implementation
    public static boolean hasVaccine(java.util.List<org.smartregister.immunization.domain.Vaccine> vaccineList, org.smartregister.immunization.db.VaccineRepo.Vaccine v) {
        return false;
    }

    @Implementation
    public static org.smartregister.immunization.domain.Vaccine getVaccine(java.util.List<org.smartregister.immunization.domain.Vaccine> vaccineList, org.smartregister.immunization.db.VaccineRepo.Vaccine v) {
        return new Vaccine(1l, "1", "BCG", 1, new Date(0l), "1", "1", "1", "1", 1l, "1", "1", 1);
    }

    @Implementation
    public static org.smartregister.domain.Alert getAlert(java.util.List<org.smartregister.domain.Alert> alerts, org.smartregister.immunization.db.VaccineRepo.Vaccine vaccine) {
        return new Alert("","","", AlertStatus.normal,"","");
    }
}
