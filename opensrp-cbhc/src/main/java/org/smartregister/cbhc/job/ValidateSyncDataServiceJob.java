package org.smartregister.cbhc.job;

import android.content.Intent;
import android.support.annotation.NonNull;

import org.smartregister.cbhc.service.intent.ValidateIntentService;
import org.smartregister.cbhc.util.Constants;

/**
 * Created by ndegwamartin on 06/09/2018.
 */
public class ValidateSyncDataServiceJob extends BaseJob {

    public static final String TAG = "ValidateSyncDataServiceJob";

    @NonNull
    @Override
    protected Result onRunJob(@NonNull Params params) {
        Intent intent = new Intent(getApplicationContext(), ValidateIntentService.class);
        getApplicationContext().startService(intent);
        return params != null && params.getExtras().getBoolean(Constants.INTENT_KEY.TO_RESCHEDULE, false) ? Result.RESCHEDULE : Result.SUCCESS;
    }
}
