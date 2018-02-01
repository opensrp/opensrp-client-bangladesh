package org.smartregister.path.fragment.mocks;

import android.os.Bundle;

import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.widget.LinearLayout;

import org.smartregister.path.R;
import org.smartregister.path.fragment.BaseSmartRegisterFragment;

/**
 * Created by kaderchowdhury on 06/12/17.
 */

public class FragmentMockActivity extends ActionBarActivity {
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



//org.smartregister.Context context_;
//    public void startHouseholdMemberAddFragment() {
//        HouseholdMemberAddFragment fragment = HouseholdMemberAddFragment.newInstance(this,"1","2", VaccinatorApplicationTestVersion.getInstance().context());
//        FragmentManager fragmentManager = getSupportFragmentManager();
//        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
//        fragmentTransaction.add(fragment, "");
//        fragmentTransaction.commit();
//    }
}
