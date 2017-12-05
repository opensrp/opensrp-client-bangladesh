package org.smartregister.path.customshadow;

/**
 * Created by kaderchowdhury on 03/12/17.
 */
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import org.robolectric.annotation.Implements;
import org.robolectric.shadows.ShadowTextView;
import org.smartregister.path.view.LocationPickerView;
import org.smartregister.view.customcontrols.CustomFontTextView;
import org.smartregister.view.customcontrols.FontVariant;

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

    public void getSelectedItem() {

    }


    public void setOnLocationChangeListener() {

    }

    public void onClick() {

    }
}
