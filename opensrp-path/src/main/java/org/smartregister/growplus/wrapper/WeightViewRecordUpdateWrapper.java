package org.smartregister.growplus.wrapper;

import org.smartregister.growthmonitoring.domain.Weight;
import org.smartregister.path.wrapper.*;

/**
 * Created by onaio on 14/09/2017.
 */

public class WeightViewRecordUpdateWrapper extends org.smartregister.path.wrapper.BaseViewRecordUpdateWrapper {

    private Weight weight;

    public Weight getWeight() {
        return weight;
    }

    public void setWeight(Weight weight) {
        this.weight = weight;
    }
}
