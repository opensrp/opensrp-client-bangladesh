package org.smartregister.growplus.interactors;

import com.vijay.jsonwizard.constants.JsonFormConstants;
import com.vijay.jsonwizard.interactors.JsonFormInteractor;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.smartregister.path.interactors.*;

import shared.BaseUnitTest;

/**
 * Created by kaderchowdhury on 10/12/17.
 */

public class PathJsonFormInteractorTest extends BaseUnitTest {
    @Mock
    org.smartregister.path.interactors.PathJsonFormInteractor pathJsonFormInteractor;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        JsonFormInteractor jsonFormInteractor = org.smartregister.path.interactors.PathJsonFormInteractor.getInstance();
        pathJsonFormInteractor.registerWidgets();

    }

    @Test
    public void mockRunnable() {}

}
