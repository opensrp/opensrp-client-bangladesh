package org.smartregister.path.activity.shadow;

import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.shadow.api.Shadow;
import org.smartregister.growplus.view.LocationPickerView;

import java.util.ArrayList;

/**
 * Created by kaderchowdhury on 17/12/17.
 */
@Implements(LocationPickerView.class)
public class LocationPickerViewShadow extends Shadow {
    @Implementation
    private ArrayList<String> getLocations() {
        ArrayList<String>list = new ArrayList<>();
        list.add("NY");
        return list;
    }
    @Implementation
    public void init(final org.smartregister.Context openSrpContext) {

    }
    @Implementation
    private void init() {

    }
}
