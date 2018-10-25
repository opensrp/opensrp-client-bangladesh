package org.smartregister.path.activity.shadow;

import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.shadows.ShadowApplication;
import org.smartregister.growplus.application.VaccinatorApplication;
import org.smartregister.growplus.repository.PathRepository;
import org.smartregister.repository.Repository;

import static org.smartregister.util.Log.logError;

/**
 * Created by kaderchowdhury on 14/12/17.
 */
@Implements(VaccinatorApplication.class)
public class VaccinatorApplicationShadow extends ShadowApplication {
    public static PathRepository pathRepository;

    @Implementation
    public Repository getRepository() {

        return pathRepository;
    }
}
