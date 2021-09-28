package org.smartregister.cbhc.notification;

import android.content.Intent;
import android.support.annotation.NonNull;

import org.smartregister.cbhc.job.BaseJob;
import org.smartregister.cbhc.service.intent.SyncIntentService;
import org.smartregister.cbhc.util.Constants;

/**
 * Created by ndegwamartin on 05/09/2018.
 */
public class NotificationServiceJob extends BaseJob {

    public static final String TAG = "NotificationServiceJob";

    @NonNull
    @Override
    protected Result onRunJob(@NonNull Params params) {
        Intent intent = new Intent(getApplicationContext(), NotificationIntentService.class);
        getApplicationContext().startService(intent);

        return params != null && params.getExtras().getBoolean(Constants.INTENT_KEY.TO_RESCHEDULE, false) ? Result.RESCHEDULE : Result.SUCCESS;
    }
}
