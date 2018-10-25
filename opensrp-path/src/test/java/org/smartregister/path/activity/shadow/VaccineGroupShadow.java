package org.smartregister.path.activity.shadow;

import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.shadows.ShadowViewGroup;
import org.smartregister.immunization.view.VaccineGroup;
/**
 * Created by kaderchowdhury on 17/12/17.
 */
@Implements(VaccineGroup.class)
public class VaccineGroupShadow extends ShadowViewGroup {
    @Implementation
    public void updateWrapperStatus(java.util.ArrayList<org.smartregister.immunization.domain.VaccineWrapper> tags, String type) {

    }
}
