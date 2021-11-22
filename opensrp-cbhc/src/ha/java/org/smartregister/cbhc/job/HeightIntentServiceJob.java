package org.smartregister.cbhc.job;

import static org.smartregister.cbhc.util.Constants.INTENT_KEY.TO_RESCHEDULE;

import android.content.Intent;
import android.support.annotation.NonNull;

import org.smartregister.growthmonitoring.service.intent.HeightIntentService;

public class HeightIntentServiceJob extends BaseJob {
    public static final String TAG = "HeightIntentServiceJob";

    @NonNull
    @Override
    protected Result onRunJob(@NonNull Params params) {
        Intent intent = new Intent(getApplicationContext(), HeightIntentService.class);
        getApplicationContext().startService(intent);
        return params.getExtras().getBoolean(TO_RESCHEDULE, false) ? Result.RESCHEDULE : Result.SUCCESS;
    }
}
