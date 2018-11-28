package org.smartregister.growplus.wrapper;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.smartregister.growthmonitoring.domain.Weight;

import shared.BaseUnitTest;

/**
 * Created by kaderchowdhury on 10/12/17.
 */

public class WeightViewRecordUpdateWrapperTest extends BaseUnitTest {
    WeightViewRecordUpdateWrapper weightViewRecordUpdateWrapper;
    @Mock
    Weight weight;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        weightViewRecordUpdateWrapper = new WeightViewRecordUpdateWrapper();
    }

    @Test
    public void getWeight() {
        weightViewRecordUpdateWrapper.getWeight();
    }

    @Test
    public void setWeight() {
        weightViewRecordUpdateWrapper.setWeight(weight);
    }
}
