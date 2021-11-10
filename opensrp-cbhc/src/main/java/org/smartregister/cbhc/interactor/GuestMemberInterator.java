package org.smartregister.cbhc.interactor;

import android.content.Context;
import android.util.Log;
import android.util.Pair;

import org.smartregister.cbhc.contract.GuestMemberContract;
import org.smartregister.cbhc.domain.GuestMemberData;
import org.smartregister.cbhc.model.GuestMemberModel;
import org.smartregister.cbhc.util.AppExecutors;
import org.smartregister.clientandeventmodel.Client;
import org.smartregister.clientandeventmodel.Event;
import java.util.ArrayList;

public class GuestMemberInterator implements GuestMemberContract.Interactor {

    private AppExecutors appExecutors;
    private GuestMemberModel model;

    public GuestMemberInterator(AppExecutors appExecutors, GuestMemberModel model){
        this.appExecutors = appExecutors;
        this.model = model;
    }


/*    @Override
    public ArrayList<GuestMemberData> getAllGuestMemberList() {
        return model.getData();
    }*/

    @Override
    public void processAndSaveRegistration(String jsonString, GuestMemberContract.InteractorCallBack callBack) {
        Runnable runnable = () -> {
            Pair<Client, Event> processEventClient = model.processRegistration(jsonString);
            Log.d("ttttProcess",processEventClient.toString());
            if(processEventClient != null){
                model.saveRegistration(processEventClient);
            }

            appExecutors.mainThread().execute(callBack::successfullySaved);
        };
        appExecutors.diskIO().execute(runnable);

    }

    @Override
    public void fetchData(Context context, GuestMemberContract.InteractorCallBack callBack) {
        Runnable runnable = () -> {
            model.loadData();

            appExecutors.mainThread().execute(callBack::updateAdapter);
        };
        appExecutors.diskIO().execute(runnable);

    }
    @Override
    public void filterData(Context context,String query,String ssName, GuestMemberContract.InteractorCallBack callBack) {
        Runnable runnable = () -> {
            model.filterData(query,ssName);

            appExecutors.mainThread().execute(callBack::updateAdapter);
        };
        appExecutors.diskIO().execute(runnable);

    }


    @Override
    public ArrayList<GuestMemberData> getAllGuestMemberList() {
        return model.getData();
    }

    @Override
    public ArrayList<String> getAllNavItemList() {
        return model.getNavData();
    }

    @Override
    public void fetchMemberData(Context context,  GuestMemberContract.InteractorCallBack callBack) {
        Runnable runnable = () -> {
            model.fetchMemberData();

            appExecutors.mainThread().execute(callBack::updateNavAdapter);
        };
        appExecutors.diskIO().execute(runnable);
    }
}
