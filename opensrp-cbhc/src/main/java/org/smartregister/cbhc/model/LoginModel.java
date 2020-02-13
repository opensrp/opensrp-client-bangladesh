package org.smartregister.cbhc.model;

import android.text.TextUtils;

import org.smartregister.cbhc.BuildConfig;
import org.smartregister.cbhc.application.AncApplication;
import org.smartregister.cbhc.contract.LoginContract;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by ndegwamartin on 27/06/2018.
 */
public class LoginModel implements LoginContract.Model {

    @Override
    public org.smartregister.Context getOpenSRPContext() {
        return AncApplication.getInstance().getContext();
    }

    @Override
    public boolean isPasswordValid(String password) {
        return !TextUtils.isEmpty(password) && password.length() > 1;
    }

    @Override
    public boolean isEmptyUsername(String username) {
        return username == null || TextUtils.isEmpty(username);
    }

    public String getBuildDate() {
        return new SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH).format(new Date(BuildConfig.BUILD_TIMESTAMP));
    }

    @Override
    public boolean isUserLoggedOut() {
        return getOpenSRPContext().IsUserLoggedOut();
    }

}
