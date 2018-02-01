package org.smartregister.path.customshadow;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MenuItem;

import org.robolectric.annotation.Implements;
import org.smartregister.path.activity.BaseActivity;
import org.smartregister.path.toolbar.LocationSwitcherToolbar;

/**
 * Created by kaderchowdhury on 04/12/17.
 */
@Implements(LocationSwitcherToolbar.class)
public class LocationSwitcherToolbarShadow {
    public void __constructor__(Context context) {
    }

    public void __constructor__(Context context, @Nullable AttributeSet attrs) {
    }

    public void __constructor__(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {

    }

    public void init(BaseActivity baseActivity) {
    }

    public void getCurrentLocation() {
    }

    public void setTitle(String title) {
    }

    public void setOnLocationChangeListener(LocationSwitcherToolbar.OnLocationChangeListener onLocationChangeListener) {
    }

    public void getSupportedMenu() {
    }

    public void prepareMenu() {
    }

    public void onMenuItemSelected(MenuItem menuItem) {
    }

    public void updateSeparatorView(int newView) {
    }
}
