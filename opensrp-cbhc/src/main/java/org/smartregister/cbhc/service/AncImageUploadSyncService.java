package org.smartregister.cbhc.service;

import android.content.Intent;
import android.util.Log;

import org.smartregister.cbhc.receiver.AlarmReceiver;
import org.smartregister.service.ImageUploadSyncService;

/**
 * Created by ndegwamartin on 23/05/2018.
 */

public class AncImageUploadSyncService extends ImageUploadSyncService {
    private static final String TAG = AncImageUploadSyncService.class.getCanonicalName();

    @Override
    protected void onHandleIntent(Intent intent) {
        try {
            super.onHandleIntent(intent);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        } finally {
            AlarmReceiver.completeWakefulIntent(intent);
        }
    }
}

