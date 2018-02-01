package org.smartregister.path.activity.shadow;

import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.shadow.api.Shadow;
import org.smartregister.path.toolbar.LocationSwitcherToolbar;

/**
 * Created by kaderchowdhury on 17/12/17.
 */
@Implements(LocationSwitcherToolbar.class)
public class LocationSwitcherToolbarShadow extends Shadow {
}
