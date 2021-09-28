package org.smartregister.cbhc.notification;

import android.content.Context;

public interface NotificationContract {

    public interface Presenter{
        void processNotification();

        View getView();
    }

    public interface View{

        void showProgressBar();

        void hideProgressBar();

        void updateAdapter();

        Context getContext();

    }

}
