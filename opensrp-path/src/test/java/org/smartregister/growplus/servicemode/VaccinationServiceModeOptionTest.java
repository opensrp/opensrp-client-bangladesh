package org.smartregister.path.servicemode;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.smartregister.provider.SmartRegisterClientsProvider;
import org.smartregister.view.activity.SecuredNativeSmartRegisterActivity;

import shared.BaseUnitTest;

/**
 * Created by kaderchowdhury on 10/12/17.
 */

public class VaccinationServiceModeOptionTest extends BaseUnitTest {

    @Mock
    SmartRegisterClientsProvider smartRegisterClientsProvider;
    VaccinationServiceModeOption vaccinationServiceModeOption;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        vaccinationServiceModeOption = new VaccinationServiceModeOption(smartRegisterClientsProvider,"NAME",new int[]{0}, new int[]{0});
        vaccinationServiceModeOption.name();
        SecuredNativeSmartRegisterActivity.ClientsHeaderProvider clientsHeaderProvider = vaccinationServiceModeOption.getHeaderProvider();
        clientsHeaderProvider.count();
        clientsHeaderProvider.headerTextResourceIds();
        clientsHeaderProvider.weights();
        clientsHeaderProvider.weightSum();
    }

    @Test
    public void mockRunable() {

    }
}
