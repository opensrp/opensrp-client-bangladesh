package org.smartregister.cbhc.task;

import android.os.AsyncTask;

import org.smartregister.Context;
import org.smartregister.cbhc.application.AncApplication;
import org.smartregister.cbhc.contract.LoginContract;
import org.smartregister.domain.LoginResponse;
import org.smartregister.event.Listener;

/**
 * Created by ndegwamartin on 22/06/2018.
 */
public class RemoteLoginTask extends AsyncTask<Void, Void, LoginResponse> {

    private final String mUsername;
    private final String mPassword;
    private final Listener<LoginResponse> afterLoginCheck;
    private LoginContract.View mLoginView;

    public RemoteLoginTask(LoginContract.View loginView, String username, String password, Listener<LoginResponse> afterLoginCheck) {
        mLoginView = loginView;
        mUsername = username;
        mPassword = password;
        this.afterLoginCheck = afterLoginCheck;
    }

    public static Context getOpenSRPContext() {
        return AncApplication.getInstance().getContext();
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        mLoginView.showProgress(true);
    }

    @Override
    protected LoginResponse doInBackground(Void... params) {

        LoginResponse loginResponse = null;
        if (getOpenSRPContext() != null && getOpenSRPContext().userService() != null)
            loginResponse = getOpenSRPContext().userService().isValidRemoteLogin(mUsername, mPassword);
//        Log.d("LOGINREPONSE",loginResponse.getRawData().toString());
        if (loginResponse != null && loginResponse.equals(LoginResponse.SUCCESS)) {
            getOpenSRPContext().userService().getAllSharedPreferences().updateANMUserName(mUsername);
            String password = getOpenSRPContext().userService().getGroupId(mUsername);
            if (password != null) {
                AncApplication.getInstance().initLibraries();

            }

        }
        return loginResponse;
    }

    @Override
    protected void onPostExecute(final LoginResponse loginResponse) {
        super.onPostExecute(loginResponse);

        mLoginView.showProgress(false);
        if (loginResponse != null)
            afterLoginCheck.onEvent(loginResponse);
        else
            afterLoginCheck.onEvent(LoginResponse.SUCCESS_WITHOUT_USER_DETAILS);
    }

    @Override
    protected void onCancelled() {
        mLoginView.showProgress(false);
    }

}

