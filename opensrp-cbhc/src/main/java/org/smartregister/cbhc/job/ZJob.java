package org.smartregister.cbhc.job;

import android.content.Intent;
import android.support.annotation.NonNull;

import org.smartregister.cbhc.util.Constants;
import org.smartregister.growthmonitoring.service.intent.ZScoreRefreshIntentService;

public class ZJob extends BaseJob {

    @NonNull
    @Override
    protected Result onRunJob(@NonNull Params params) {
        Intent intent = new Intent(this.getApplicationContext(), ZScoreRefreshIntentService.class);
        getApplicationContext().startService(intent);
        return params != null && params.getExtras().getBoolean(Constants.INTENT_KEY.TO_RESCHEDULE, false) ? Result.RESCHEDULE : Result.SUCCESS;
    }
}
