package org.smartregister.cbhc.presenter;

import org.smartregister.cbhc.contract.ChildListContract;
import org.smartregister.cbhc.domain.ChildItemData;
import org.smartregister.cbhc.interactor.ChildListInteractor;
import org.smartregister.cbhc.model.ChildListModel;
import org.smartregister.cbhc.util.AppExecutors;

import java.util.ArrayList;

public class ChildListPresenter implements ChildListContract.Presenter,ChildListContract.InteractorCallBack {
    ChildListContract.View view;
    ChildListInteractor interactor;

    public ChildListPresenter(ChildListContract.View view) {
        this.view = view;
        this.interactor = new ChildListInteractor(new AppExecutors(), new ChildListModel());
    }

    @Override
    public void updateList() {
        view.updateChildList();
    }


    @Override
    public ArrayList<ChildItemData> getChildList() {
        return interactor.getChildData();
    }

    @Override
    public void fetchChildList(String query) {
        interactor.fetchData(query,this);

    }
}
