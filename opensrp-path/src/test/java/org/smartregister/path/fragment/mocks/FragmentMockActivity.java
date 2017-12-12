package org.smartregister.path.fragment.mocks;

import android.os.Bundle;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.widget.LinearLayout;

import org.smartregister.path.R;
import org.smartregister.path.activity.BaseRegisterActivity;
import org.smartregister.path.activity.HIA2ReportsActivity;
import org.smartregister.path.application.VaccinatorApplication;
import org.smartregister.path.fragment.AdvancedSearchFragment;
import org.smartregister.path.fragment.BaseSmartRegisterFragment;
import org.smartregister.path.fragment.DraftMonthlyFragment;
import org.smartregister.path.fragment.HouseholdMemberAddFragment;
import org.smartregister.provider.SmartRegisterClientsProvider;
import org.smartregister.view.activity.SecuredNativeSmartRegisterActivity;

import shared.VaccinatorApplicationTestVersion;

/**
 * Created by kaderchowdhury on 06/12/17.
 */

public class FragmentMockActivity extends HIA2ReportsActivity {
    @Override
    public void onCreate(Bundle bundle) {
        setTheme(R.style.AppTheme); //we need this here
        super.onCreate(bundle);
        LinearLayout linearLayout;
        linearLayout = new LinearLayout(this);
        setContentView(linearLayout);

    }

    public void startAdvancedSearchFragment() {
        AdvancedSearchFragmentMock fragment = new AdvancedSearchFragmentMock();
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(fragment, "");
        fragmentTransaction.commit();
    }


    public void startBaseSmartRegisterFragment() {
        BaseSmartRegisterFragment fragment = new BaseSmartRegisterFragment();
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(fragment, "");
        fragmentTransaction.commit();
    }//DraftMonthlyFragment

    public void startDraftMonthlyFragment() {
        DraftMonthlyFragment fragment = new DraftMonthlyFragment();
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(fragment, "");
        fragmentTransaction.commit();
    }

//org.smartregister.Context context_;
//    public void startHouseholdMemberAddFragment() {
//        HouseholdMemberAddFragment fragment = HouseholdMemberAddFragment.newInstance(this,"1","2", VaccinatorApplicationTestVersion.getInstance().context());
//        FragmentManager fragmentManager = getSupportFragmentManager();
//        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
//        fragmentTransaction.add(fragment, "");
//        fragmentTransaction.commit();
//    }
}
