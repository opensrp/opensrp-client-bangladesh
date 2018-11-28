package org.smartregister.growplus.activity.shadow;

import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.shadow.api.Shadow;
import org.smartregister.Context;
import org.smartregister.view.fragment.SecuredFragment;
/**
 * Created by Raihan Ahmed on 14/12/17.
 */
@Implements(SecuredFragment.class)
public class SecuredFragmentShadow extends Shadow {
    public static Context mContext;

    @Implementation
    protected Context context()
    {
        return mContext;
    }
}
