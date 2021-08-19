package org.smartregister.growplus.job;

import android.content.Intent;
import android.support.annotation.NonNull;

import com.evernote.android.job.Job;

import org.smartregister.AllConstants;
import org.smartregister.growthmonitoring.service.intent.HeightIntentService;

import static util.Constants.INTENT_KEY.TO_RESCHEDULE;

public class HeightIntentServiceJob extends BaseJob {
    public static final String TAG = "HeightIntentServiceJob";

    @NonNull
    @Override
    protected Job.Result onRunJob(@NonNull Job.Params params) {
        Intent intent = new Intent(getApplicationContext(), HeightIntentService.class);
        getApplicationContext().startService(intent);
        return params.getExtras().getBoolean(TO_RESCHEDULE, false) ? Job.Result.RESCHEDULE : Job.Result.SUCCESS;
    }
}
