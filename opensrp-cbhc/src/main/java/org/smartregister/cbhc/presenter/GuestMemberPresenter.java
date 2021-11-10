package org.smartregister.cbhc.presenter;
import android.util.Log;

import org.smartregister.cbhc.contract.GuestMemberContract;
import org.smartregister.cbhc.domain.GuestMemberData;
import org.smartregister.cbhc.interactor.GuestMemberInterator;
import org.smartregister.cbhc.model.GuestMemberModel;
import org.smartregister.cbhc.util.AppExecutors;
import java.util.ArrayList;

public class GuestMemberPresenter implements GuestMemberContract.Presenter, GuestMemberContract.InteractorCallBack {

    private GuestMemberContract.View view;
    private GuestMemberInterator interactor;

    public GuestMemberPresenter(GuestMemberContract.View view){
        this.view = view;
        interactor = new GuestMemberInterator(new AppExecutors(),new GuestMemberModel(view.getContext()));

    }
/*
    @Override
    public ArrayList<GuestMemberData> getData() {
        return interactor.getAllGuestMemberList();
    }*/

    @Override
    public ArrayList<GuestMemberData> getData() {
        return interactor.getAllGuestMemberList();
    }

    @Override
    public ArrayList<String> getNAvData() {
        return interactor.getAllNavItemList();
    }

    @Override
    public void saveMember(String jsonString) {
        //view.showProgressBar();
        interactor.processAndSaveRegistration(jsonString,this);

    }

    @Override
    public void filterData(String query, String ssName) {
        view.showProgressBar();
        interactor.filterData(view.getContext(),query,ssName,this);

    }

    @Override
    public void fetchData() {
        view.showProgressBar();
        interactor.fetchData(view.getContext(),this);

    }

    @Override
    public GuestMemberContract.View getView() {
        return view;
    }

    @Override
    public void fetchMemberData() {
        interactor.fetchMemberData(view.getContext(),this);
    }

    @Override
    public void updateAdapter() {
        view.hideProgressBar();
        view.updateAdapter();
        view.updateNavAdapter();

    }

    @Override
    public void successfullySaved() {
        view.hideProgressBar();
        Log.d("ttttt","suucessfullySaved");
        fetchData();
        fetchMemberData();



    }

    @Override
    public void updateNavAdapter() {
        view.updateNavAdapter();
    }
}
