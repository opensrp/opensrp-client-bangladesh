package org.smartregister.growplus.activity.shadow;

import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.shadow.api.Shadow;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.domain.Photo;
import org.smartregister.immunization.util.IMDatabaseUtils;

import util.ImageUtils;

/**
 * Created by kaderchowdhury on 14/12/17.
 */
@Implements(IMDatabaseUtils.class)
public class IMDatabaseUtilsShadow extends Shadow {
    @Implementation
    public static void accessAssetsAndFillDataBaseForVaccineTypes(android.content.Context context, net.sqlcipher.database.SQLiteDatabase db) {

    }
}
