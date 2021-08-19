package org.smartregister.growplus.job;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.evernote.android.job.Job;
import com.evernote.android.job.JobCreator;

/**
 *
 */
public class GrowPlusJobCreator implements JobCreator {
    @Nullable
    @Override
    public Job create(@NonNull String tag) {
        switch (tag) {
            case HeightIntentServiceJob.TAG:
                return new HeightIntentServiceJob();
            case MuactIntentServiceJob.TAG:
                return new MuactIntentServiceJob();
            default:
                return null;
        }
    }
}
