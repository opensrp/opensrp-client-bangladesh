package org.smartregister.cbhc.task;

import android.content.Intent;
import android.support.annotation.NonNull;

import com.evernote.android.job.Job;

import org.smartregister.cbhc.job.BaseJob;
import org.smartregister.cbhc.service.intent.EventLogIntentService;
import org.smartregister.cbhc.util.Constants;

public class EventLogServiceJob extends BaseJob{
    public static final String TAG = "EventLogServiceJob";

    @NonNull
    @Override
    protected Job.Result onRunJob(@NonNull Job.Params params) {
        Intent intent = new Intent(getApplicationContext(), EventLogIntentService.class);
        getApplicationContext().startService(intent);
        return params != null && params.getExtras().getBoolean(Constants.INTENT_KEY.TO_RESCHEDULE, false) ? Job.Result.RESCHEDULE : Job.Result.SUCCESS;
    }
}
