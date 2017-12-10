package org.smartregister.path.activity.mocks;

import android.os.Bundle;
import android.view.Menu;
import android.view.View;

import org.json.JSONObject;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.domain.Alert;
import org.smartregister.growthmonitoring.repository.WeightRepository;
import org.smartregister.immunization.domain.Vaccine;
import org.smartregister.immunization.repository.RecurringServiceRecordRepository;
import org.smartregister.immunization.repository.RecurringServiceTypeRepository;
import org.smartregister.immunization.repository.VaccineRepository;
import org.smartregister.path.R;
import org.smartregister.path.activity.ChildImmunizationActivity;
import org.smartregister.path.application.VaccinatorApplication;
import org.smartregister.path.domain.RegisterClickables;
import org.smartregister.service.AlertService;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * Created by kaderchowdhury on 04/12/17.
 */

public class ChildImmunizationActivityMock extends ChildImmunizationActivity {
    private static final String EXTRA_CHILD_DETAILS = "child_details";
    @Override
    public void onCreate(Bundle bundle) {
        setTheme(R.style.AppTheme); //we need this here
        super.onCreate(bundle);
//        Bundle extras = this.getIntent().getExtras();
//        if (extras != null) {
//            Serializable serializable = extras.getSerializable(EXTRA_CHILD_DETAILS);
//            if (serializable != null && serializable instanceof CommonPersonObjectClient) {
//                childDetails = (CommonPersonObjectClient) serializable;
//            }
//        }

    }


    @Override
    public void updateViews() {
//        super.updateViews();
        findViewById(R.id.profile_name_layout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        // TODO: update all views using child data
        Map<String, String> details = detailsRepository.getAllDetailsForClient(childDetails.entityId());

        util.Utils.putAll(childDetails.getColumnmaps(), details);

        updateGenderViews();
        toolbar.setTitle(updateActivityTitle());
        updateAgeViews();
        updateChildIdViews();

        WeightRepository weightRepository = VaccinatorApplication.getInstance().weightRepository();

        VaccineRepository vaccineRepository = VaccinatorApplication.getInstance().vaccineRepository();

        RecurringServiceTypeRepository recurringServiceTypeRepository = VaccinatorApplication.getInstance().recurringServiceTypeRepository();

        RecurringServiceRecordRepository recurringServiceRecordRepository = VaccinatorApplication.getInstance().recurringServiceRecordRepository();

        AlertService alertService = getOpenSRPContext().alertService();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

}
