package org.smartregister.cbhc.job;

import static org.smartregister.cbhc.util.Constants.INTENT_KEY.TO_RESCHEDULE;

import android.content.Intent;
import android.support.annotation.NonNull;

import org.smartregister.growthmonitoring.service.intent.MuacIntentService;

public class MuactIntentServiceJob extends BaseJob {
    public static final String TAG = "MuactIntentServiceJob";

    @NonNull
    @Override
    protected Result onRunJob(@NonNull Params params) {
        Intent intent = new Intent(getApplicationContext(), MuacIntentService.class);
        getApplicationContext().startService(intent);
        return params.getExtras().getBoolean(TO_RESCHEDULE, false) ? Result.RESCHEDULE : Result.SUCCESS;
    }
}
