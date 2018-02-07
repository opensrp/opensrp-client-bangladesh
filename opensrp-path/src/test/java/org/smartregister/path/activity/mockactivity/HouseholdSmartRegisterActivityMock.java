package org.smartregister.path.activity.mockactivity;

import android.os.Bundle;
import android.view.Menu;
import android.view.View;

import org.smartregister.Context;
import org.smartregister.growthmonitoring.repository.WeightRepository;
import org.smartregister.immunization.repository.VaccineRepository;
import org.smartregister.path.R;
import org.smartregister.path.activity.ChildImmunizationActivity;
import org.smartregister.path.activity.HouseholdSmartRegisterActivity;
import org.smartregister.path.application.VaccinatorApplication;
import org.smartregister.service.AlertService;

import java.util.Map;

/**
 * Created by kaderchowdhury on 04/12/17.
 */

public class HouseholdSmartRegisterActivityMock extends HouseholdSmartRegisterActivity {

    public static Context mContext;

    @Override
    public void onCreate(Bundle bundle) {
        setTheme(R.style.AppTheme); //we need this here
        super.onCreate(bundle);
    }

    public static void setmContext(Context Context) {
        mContext = Context;
    }

    @Override
    protected Context context() {
        return mContext;
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
