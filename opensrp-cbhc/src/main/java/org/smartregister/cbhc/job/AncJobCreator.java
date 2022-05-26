package org.smartregister.cbhc.job;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.evernote.android.job.Job;
import com.evernote.android.job.JobCreator;

import org.smartregister.cbhc.notification.NotificationServiceJob;
import org.smartregister.cbhc.task.EventLogServiceJob;
import org.smartregister.job.DataSyncByBaseEntityServiceJob;
import org.smartregister.job.ForceSyncDataServiceJob;
import org.smartregister.job.InValidateSyncDataServiceJob;

/**
 * Created by ndegwamartin on 05/09/2018.
 */
public class AncJobCreator implements JobCreator {
    @Nullable
    @Override
    public Job create(@NonNull String tag) {
        switch (tag) {
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
            case NotificationServiceJob.TAG:
                return new NotificationServiceJob();
            case ViewConfigurationsServiceJob.TAG:
                return new ViewConfigurationsServiceJob();
            case DeleteIntentServiceJob.TAG:
                return new DeleteIntentServiceJob();
            case EventLogServiceJob.TAG:
                return new EventLogServiceJob();
            case DataDeleteJob.TAG:
                return new DataDeleteJob();
            case InValidateSyncDataServiceJob.TAG:
                return new InValidateSyncDataServiceJob();
            case DataSyncByBaseEntityServiceJob.TAG:
                return new DataSyncByBaseEntityServiceJob();
            case ForceSyncDataServiceJob.TAG:
                return new ForceSyncDataServiceJob();
            default:
                return null;
        }
    }
}
