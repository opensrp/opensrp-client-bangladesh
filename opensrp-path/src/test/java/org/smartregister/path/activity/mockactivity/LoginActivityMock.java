package org.smartregister.path.activity.mockactivity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import org.smartregister.Context;
import org.smartregister.domain.LoginResponse;
import org.smartregister.event.Listener;
import org.smartregister.path.activity.LoginActivity;
import org.smartregister.path.application.VaccinatorApplication;


/**
 * Created by Raihan Ahmed on 11/11/17.
 */

public class LoginActivityMock extends LoginActivity {

    public static Context mockactivitycontext;

    public static InputMethodManager inputManager;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        setTheme(org.smartregister.R.style.Base_Theme_AppCompat); //we need this here
        super.onCreate(savedInstanceState);
    }

    @Override
    public Object getSystemService(String name) {
        if (name.equalsIgnoreCase(INPUT_METHOD_SERVICE)) {
            return inputManager;
        } else {
            return super.getSystemService(name);
        }
    }

    @Nullable
    @Override
    public View getCurrentFocus() {
        return findViewById(org.smartregister.R.id.login_userNameText);
    }

    public static Context getOpenSRPContext() {
        return mockactivitycontext;
    }

    @Override
    public void tryRemoteLogin(final String userName, final String password, final Listener<LoginResponse> afterLoginCheck) {

    }
}
