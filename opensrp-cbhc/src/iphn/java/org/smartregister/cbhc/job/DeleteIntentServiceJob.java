package org.smartregister.cbhc.job;

import android.content.Intent;
import android.support.annotation.NonNull;

import org.smartregister.cbhc.job.BaseJob;
import org.smartregister.cbhc.service.intent.DeleteIntentService;
import org.smartregister.cbhc.util.Constants;

public class DeleteIntentServiceJob extends BaseJob {

    public static final String TAG = "DeleteIntentServiceJob";

    @NonNull
    @Override
    protected Result onRunJob(@NonNull Params params) {
        Intent intent = new Intent(getApplicationContext(), DeleteIntentService.class);
        //getApplicationContext().startService(intent);
        return params != null && params.getExtras().getBoolean(Constants.INTENT_KEY.TO_RESCHEDULE, false) ? Result.RESCHEDULE : Result.SUCCESS;
    }
}
