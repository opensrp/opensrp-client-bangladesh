package org.smartregister.cbhc.contract;

import android.content.Context;

import org.smartregister.cbhc.domain.ChildItemData;

import java.util.ArrayList;

public interface ChildListContract {


    interface View {
        Presenter getPresenter();
        Context getContext();
        void updateChildList();
    }

    interface Interactor {
        void fetchData(String query, InteractorCallBack callBack);
        ArrayList<ChildItemData> getChildData();
    }

    interface Model {
        void fetchChildList(String query);
        ArrayList<ChildItemData> getChildData();
    }
    interface Presenter{
        ArrayList<ChildItemData>  getChildList();

        void fetchChildList(String query);
    }

    interface InteractorCallBack{
        void updateList();
    }
}
