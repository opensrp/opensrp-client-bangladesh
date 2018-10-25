package org.smartregister.growplus.activity.shadow;

import org.mockito.Mock;
import org.mockito.Mockito;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.shadow.api.Shadow;
import org.smartregister.Context;
import org.smartregister.commonregistry.CommonRepository;
import org.smartregister.repository.AllBeneficiaries;
import org.smartregister.repository.AllEligibleCouples;
import org.smartregister.repository.Repository;
import org.smartregister.service.PendingFormSubmissionService;
import org.smartregister.view.controller.ANMController;
import org.smartregister.view.controller.ANMLocationController;

import static org.mockito.Mockito.when;

@Implements(Context.class)
public class ShadowContextForRegistryActivity extends Shadow {

    @Mock
    Repository repository;
    @Mock
    ANMController anmController;
    @Mock
    AllEligibleCouples allEligibleCouples;
    @Mock
    AllBeneficiaries allBeneficiaries;
    @Mock
    ANMLocationController anmLocationController;
    @Mock
    private PendingFormSubmissionService pendingFormSubmissionService;

    public static CommonRepository commonRepository;
    public static Context mInstance;

    @Implementation
    public Boolean IsUserLoggedOut() {
        return false;
    }

    @Implementation
    public ANMController anmController() {
        return anmController;
    }

    @Implementation
    public AllEligibleCouples allEligibleCouples() {
        return allEligibleCouples;
    }

    @Implementation
    public AllBeneficiaries allBeneficiaries() {
        return allBeneficiaries;
    }

    @Implementation
    public ANMLocationController anmLocationController() {
        anmLocationController = Mockito.mock(ANMLocationController.class);
        when(anmLocationController.getLocationJSON()).thenReturn("");
        return anmLocationController;
    }

    public PendingFormSubmissionService pendingFormSubmissionService() {
        pendingFormSubmissionService = Mockito.mock(PendingFormSubmissionService.class);
        when(pendingFormSubmissionService.pendingFormSubmissionCount()).thenReturn(0L);
        return pendingFormSubmissionService;
    }

    @Implementation
    public CommonRepository commonrepository(String tablename) {
        return commonRepository;
    }

    @Implementation
    public static Context getInstance() {
        if(mInstance == null){
            mInstance = Mockito.mock(Context.class);
        }
        return mInstance;
    }

}
