package org.smartregister.cbhc.interactor;

import org.smartregister.cbhc.contract.ChildListContract;
import org.smartregister.cbhc.domain.ChildItemData;
import org.smartregister.cbhc.model.ChildListModel;
import org.smartregister.cbhc.util.AppExecutors;

import java.util.ArrayList;

public class ChildListInteractor implements ChildListContract.Interactor {
    private AppExecutors appExecutors;
    private ChildListModel model;

    public ChildListInteractor(AppExecutors appExecutors, ChildListModel model) {
        this.appExecutors = appExecutors;
        this.model = model;
    }


    @Override
    public void fetchData(String query, ChildListContract.InteractorCallBack callBack) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                model.fetchChildList(query);
                appExecutors.mainThread().execute(callBack::updateList);
            }
        };
        appExecutors.diskIO().execute(runnable);
    }

    @Override
    public ArrayList<ChildItemData> getChildData() {
        return model.getChildData();
    }
}
