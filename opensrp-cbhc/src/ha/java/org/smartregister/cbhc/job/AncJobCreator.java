package org.smartregister.cbhc.job;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.evernote.android.job.Job;
import com.evernote.android.job.JobCreator;

/**
 * Created by ndegwamartin on 05/09/2018.
 */
public class AncJobCreator implements JobCreator {
    @Nullable
    @Override
    public Job create(@NonNull String tag) {
        switch (tag) {
            case HeightIntentServiceJob.TAG:
                return new HeightIntentServiceJob();
            case MuactIntentServiceJob.TAG:
                return new MuactIntentServiceJob();
            case WeightIntentServiceJob.TAG:
                return new WeightIntentServiceJob();
            case SyncServiceJob.TAG:
                return new SyncServiceJob();
            case ExtendedSyncServiceJob.TAG:
                return new ExtendedSyncServiceJob();
            case ImageUploadServiceJob.TAG:
                return new ImageUploadServiceJob();
            case PullUniqueIdsServiceJob.TAG:
                return new PullUniqueIdsServiceJob();
            case PullHealthIdsServiceJob.TAG:
                return new PullHealthIdsServiceJob();
//            case ValidateSyncDataServiceJob.TAG:
//                return new ValidateSyncDataServiceJob();
            case ViewConfigurationsServiceJob.TAG:
                return new ViewConfigurationsServiceJob();
            case DeleteIntentServiceJob.TAG:
                return new DeleteIntentServiceJob();
            default:
                return null;
        }
    }
}
