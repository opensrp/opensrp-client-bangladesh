package org.smartregister.cbhc.contract;

import android.content.Context;
import android.util.Pair;

import org.smartregister.cbhc.domain.GuestMemberData;
import org.smartregister.clientandeventmodel.Client;
import org.smartregister.clientandeventmodel.Event;

import java.util.ArrayList;

public interface GuestMemberContract {

    interface View{
        void showProgressBar();
        void hideProgressBar();
        void updateAdapter();
        void updateSuccessfullyFetchMessage();
        Presenter getPresenter();
        Context getContext();

        void updateNavAdapter();
    }
    interface Model{
        //ArrayList<GuestMemberData> getData();
        Pair<Client, Event> processRegistration(String jsonString);
        void saveRegistration(Pair<Client, Event> pair);
        void loadData();
        Context getContext();
        void fetchMemberData();
    }

    interface Presenter{
        ArrayList<GuestMemberData> getData();
        ArrayList<String> getNAvData();
        void saveMember(String jsonString);
        void fetchData();
        void filterData(String query, String ssName);
        View getView();
        void fetchMemberData();
    }
    interface Interactor{

        void processAndSaveRegistration(final String jsonString, final InteractorCallBack callBack);

        void fetchData(Context context, InteractorCallBack callBack);

        void filterData(Context context, String query, String ssName, InteractorCallBack callBack);

        ArrayList<GuestMemberData> getAllGuestMemberList();
        ArrayList<String> getAllNavItemList();

        void fetchMemberData(Context context, InteractorCallBack callBack);
    }

    interface InteractorCallBack{
        void updateAdapter();
        void successfullySaved();

        void updateNavAdapter();
    }
}
