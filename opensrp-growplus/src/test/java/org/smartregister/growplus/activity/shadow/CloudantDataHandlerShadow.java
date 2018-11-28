package org.smartregister.growplus.activity.shadow;

import android.content.Context;

import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.shadow.api.Shadow;
import org.smartregister.sync.CloudantDataHandler;

/**
 * Handles Cloudant data access methods
 * Created by onamacuser on 11/03/2016.
 */
@Implements(CloudantDataHandler.class)
public class CloudantDataHandlerShadow extends Shadow {

    @Implementation
    public static CloudantDataHandler getInstance(Context context) throws Exception {

        return null;
    }

}
