package org.smartregister.path.fragment.mocks;

import org.robolectric.annotation.Implements;
import org.robolectric.shadows.ShadowActivity;
import org.smartregister.growplus.activity.BaseRegisterActivity;

/**
 * Created by kaderchowdhury on 12/12/17.
 */
@Implements(BaseRegisterActivity.class)
public class ShadowBaseRegisterActivity extends ShadowActivity {

    protected void getDefaultOptionsProvider() {

    }

    protected void getNavBarOptionsProvider() {

    }

    protected void clientsProvider() {

    }

    protected void onInitialization() {

    }

    public void startRegistration() {

    }
}
