package org.smartregister.cbhc.sync;


import org.apache.commons.lang3.StringUtils;
import org.smartregister.SyncConfiguration;
import org.smartregister.SyncFilter;
import org.smartregister.cbhc.BuildConfig;
import org.smartregister.cbhc.application.AncApplication;
import org.smartregister.location.helper.LocationHelper;
import org.smartregister.util.Utils;

import java.util.List;

public class AncSyncConfiguration extends SyncConfiguration {
    @Override
    public int getSyncMaxRetries() {
        return BuildConfig.MAX_SYNC_RETRIES;
    }

    @Override
    public SyncFilter getSyncFilterParam() {
        return SyncFilter.LOCATION;
    }

    @Override
    public String getSyncFilterValue() {
        String providerId = AncApplication.getInstance().getContext().allSharedPreferences().fetchRegisteredANM();
        String userLocationId = AncApplication.getInstance().getContext().allSharedPreferences().fetchUserLocalityId(providerId);

        List<String> locationIds = LocationHelper.getInstance().locationsFromHierarchy(true, null);
        if (!Utils.isEmptyCollection(locationIds)) {
            int index = locationIds.indexOf(userLocationId);
            List<String> subLocationIds = locationIds.subList(index, locationIds.size());
            return StringUtils.join(subLocationIds, ",");
        }
        return "";
    }

    @Override
    public int getUniqueIdSource() {
        return BuildConfig.OPENMRS_UNIQUE_ID_SOURCE;
    }

    @Override
    public int getUniqueIdBatchSize() {
        return BuildConfig.OPENMRS_UNIQUE_ID_BATCH_SIZE;
    }

    @Override
    public int getUniqueIdInitialBatchSize() {
        return BuildConfig.OPENMRS_UNIQUE_ID_INITIAL_BATCH_SIZE;
    }

    @Override
    public boolean isSyncSettings() {
        return BuildConfig.IS_SYNC_SETTINGS;
    }

    @Override
    public SyncFilter getEncryptionParam() {
        return SyncFilter.LOCATION;
    }

    @Override
    public boolean updateClientDetailsTable() {
        return true;
    }
}
