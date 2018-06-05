package org.smartregister.path.activity.mockactivity;

import android.os.Bundle;
import android.view.Menu;
import android.view.View;

import org.smartregister.growthmonitoring.repository.WeightRepository;
import org.smartregister.immunization.repository.VaccineRepository;
import org.smartregister.growplus.R;
import org.smartregister.growplus.activity.ChildImmunizationActivity;
import org.smartregister.growplus.application.VaccinatorApplication;
import org.smartregister.service.AlertService;

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
    }


//    @Override
    public void updateViewsLocal() {
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
