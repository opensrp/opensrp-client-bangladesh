package org.smartregister.path.fragment.mocks;

import org.robolectric.annotation.Implements;
import org.smartregister.cursoradapter.SecuredNativeSmartRegisterCursorAdapterFragment;
import org.smartregister.provider.SmartRegisterClientsProvider;
import org.smartregister.view.activity.SecuredNativeSmartRegisterActivity;

/**
 * Created by kaderchowdhury on 12/12/17.
 */
@Implements(SecuredNativeSmartRegisterCursorAdapterFragment.class)
public class ShadowSecuredNativeSmartRegisterCursorAdapterFragment  {
    protected SecuredNativeSmartRegisterActivity.DefaultOptionsProvider getDefaultOptionsProvider() {
        return null;
    }

    protected SecuredNativeSmartRegisterActivity.NavBarOptionsProvider getNavBarOptionsProvider() {
        return null;
    }

    protected SmartRegisterClientsProvider clientsProvider() {
        return null;
    }

    protected void onInitialization() {

    }

    protected void startRegistration() {

    }

    protected void onCreation() {

    }
}
