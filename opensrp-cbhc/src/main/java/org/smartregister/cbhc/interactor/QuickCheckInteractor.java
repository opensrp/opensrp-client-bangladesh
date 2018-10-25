package org.smartregister.cbhc.interactor;

import android.support.annotation.VisibleForTesting;
import android.util.Log;

import org.json.JSONObject;
import org.smartregister.cbhc.application.AncApplication;
import org.smartregister.cbhc.contract.QuickCheckContract;
import org.smartregister.cbhc.domain.QuickCheck;
import org.smartregister.cbhc.helper.ECSyncHelper;
import org.smartregister.cbhc.util.AppExecutors;
import org.smartregister.cbhc.util.JsonFormUtils;
import org.smartregister.clientandeventmodel.Event;
import org.smartregister.repository.AllSharedPreferences;

/**
 * Created by keyman 27/06/2018.
 */
public class QuickCheckInteractor implements QuickCheckContract.Interactor {

    public static final String TAG = QuickCheckInteractor.class.getName();

    private AppExecutors appExecutors;

    private ECSyncHelper syncHelper;

    private AllSharedPreferences allSharedPreferences;

    @VisibleForTesting
    QuickCheckInteractor(AppExecutors appExecutors) {
        this.appExecutors = appExecutors;
    }

    public QuickCheckInteractor() {
        this(new AppExecutors());
    }

    @Override
    public void saveQuickCheckEvent(final QuickCheck quickCheck, final String baseEntityId, final QuickCheckContract.InteractorCallback callback) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {

                boolean isSaved = false;
                try {
                    Event event = JsonFormUtils.createQuickCheckEvent(getAllSharedPreferences(), quickCheck, baseEntityId);
                    JSONObject eventJson = new JSONObject(JsonFormUtils.gson.toJson(event));
                    getSyncHelper().addEvent(baseEntityId, eventJson);
                    isSaved = true;
                } catch (Exception e) {
                    Log.e(TAG, Log.getStackTraceString(e));
                } finally {
                    final boolean finalIsSaved = isSaved;
                    appExecutors.mainThread().execute(new Runnable() {
                        @Override
                        public void run() {
                            callback.quickCheckSaved(quickCheck.getProceedRefer(), finalIsSaved);
                        }
                    });
                }
            }
        };

        appExecutors.diskIO().execute(runnable);
    }

    public void setSyncHelper(ECSyncHelper syncHelper) {
        this.syncHelper = syncHelper;
    }

    public ECSyncHelper getSyncHelper() {
        if (syncHelper == null) {
            syncHelper = ECSyncHelper.getInstance(AncApplication.getInstance().getApplicationContext());
        }
        return syncHelper;
    }

    public void setAllSharedPreferences(AllSharedPreferences allSharedPreferences) {
        this.allSharedPreferences = allSharedPreferences;
    }

    public AllSharedPreferences getAllSharedPreferences() {
        if (allSharedPreferences == null) {
            allSharedPreferences = AncApplication.getInstance().getContext().allSharedPreferences();
        }
        return allSharedPreferences;
    }
}
