package org.smartregister.growplus.customshadow;

/**
 * Created by kaderchowdhury on 03/12/17.
 */
import android.content.Context;
import android.util.AttributeSet;

import org.robolectric.annotation.Implements;
import org.smartregister.growplus.view.LocationPickerView;

import shared.customshadows.FontTextViewShadow;

/**
 * Created by onadev on 15/06/2017.
 */
@Implements(LocationPickerView.class)
public class LocationPickerViewShadow extends FontTextViewShadow {


    public void __constructor__(Context context, AttributeSet attrs, int defStyle) {

    }

    public void init(final org.smartregister.Context openSrpContext) {

    }

    public void __constructor__(Context context) {

    }

    public void __constructor__(Context context, AttributeSet attrs) {

    }

    public String getSelectedItem() {
        return "location";
    }


    public void setOnLocationChangeListener() {

    }

    public void onClick() {

    }
}
