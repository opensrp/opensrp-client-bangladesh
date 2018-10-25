package org.smartregister.growplus.activity.shadow;

import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.shadow.api.Shadow;
import org.smartregister.immunization.util.IMDatabaseUtils;

/**
 * Created by kaderchowdhury on 14/12/17.
 */
@Implements(IMDatabaseUtils.class)
public class IMDatabaseUtilsShadow extends Shadow {
    @Implementation
    public static void accessAssetsAndFillDataBaseForVaccineTypes(android.content.Context context, net.sqlcipher.database.SQLiteDatabase db) {

    }
}
