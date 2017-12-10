package org.smartregister.path.fragment.mocks;

import android.os.Bundle;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.widget.LinearLayout;

import org.smartregister.path.R;
import org.smartregister.path.fragment.AdvancedSearchFragment;
import org.smartregister.provider.SmartRegisterClientsProvider;
import org.smartregister.view.activity.SecuredNativeSmartRegisterActivity;

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
        startFragment();
    }

    public void startFragment() {
        AdvancedSearchFragmentMock fragment = new AdvancedSearchFragmentMock();
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(fragment, "");
        fragmentTransaction.commit();
    }

//
//    @Override
//    protected DefaultOptionsProvider getDefaultOptionsProvider() {
//        return null;
//    }
//
//    @Override
//    protected NavBarOptionsProvider getNavBarOptionsProvider() {
//        return null;
//    }
//
//    @Override
//    protected SmartRegisterClientsProvider clientsProvider() {
//        return null;
//    }
//
//    @Override
//    protected void onInitialization() {
//
//    }
//
//    @Override
//    public void startRegistration() {
//
//    }
}
