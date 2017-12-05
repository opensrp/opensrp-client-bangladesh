package org.smartregister.path.activity.mocks;

import android.content.Context;

import org.smartregister.path.application.VaccinatorApplication;
import org.smartregister.repository.Repository;
import org.smartregister.view.activity.DrishtiApplication;

/**
 * Created by kaderchowdhury on 05/12/17.
 */

public class DrishtiApplicationMock extends DrishtiApplication {

    public DrishtiApplicationMock() {

    }
    public static void setInstance(DrishtiApplication newInstance){
        mInstance = newInstance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    protected void attachBaseContext(Context base) {
    }

    @Override
    public Repository getRepository() {
        return null;
    }

    @Override
    public String getPassword() {
        return null;
    }

    @Override
    public void setPassword(String password) {
    }

    @Override
    public void logoutCurrentUser() {

    }
}
