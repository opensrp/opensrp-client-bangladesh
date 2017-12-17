package org.smartregister.path.activity.shadow;

import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.shadow.api.Shadow;

/**
 * Created by kaderchowdhury on 17/12/17.
 */
@Implements(org.smartregister.Context.class)
public class ContextShadow extends  Shadow {
    @Implementation
    public Boolean IsUserLoggedOut() {
        return false;
    }
}
