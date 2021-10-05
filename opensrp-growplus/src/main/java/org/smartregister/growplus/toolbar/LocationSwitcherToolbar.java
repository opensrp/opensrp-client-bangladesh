package org.smartregister.growplus.toolbar;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MenuItem;
import android.view.View;

import org.smartregister.growplus.R;
import org.smartregister.growplus.activity.BaseActivity;
import org.smartregister.growplus.view.LocationActionView;
import org.smartregister.growplus.view.LocationPickerView;
import org.smartregister.view.customcontrols.CustomFontTextView;

/**
 * To use this toolbar in your activity, include the following line as the first child in your
 * activity's main {@link android.support.design.widget.CoordinatorLayout}
 * <p/>
 * <include layout="@layout/toolbar_location_switcher" />
 * <p/>
 * Created by Jason Rogena - jrogena@ona.io on 17/02/2017.
 */

public class LocationSwitcherToolbar extends BaseToolbar {
    public static final int TOOLBAR_ID = R.id.location_switching_toolbar;
    private BaseActivity baseActivity;
    private OnLocationChangeListener onLocationChangeListener;
    private String title;
    private int separatorResourceId;

    public LocationSwitcherToolbar(Context context) {
        super(context);
    }

    public LocationSwitcherToolbar(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public LocationSwitcherToolbar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void init(BaseActivity baseActivity) {
        this.baseActivity = baseActivity;
    }

    public String getCurrentLocation() {
        if (baseActivity != null && baseActivity.getMenu() != null) {
            return ((LocationActionView) baseActivity.getMenu().findItem(R.id.location_switcher)
                    .getActionView()).getSelectedItem();
        }

        return null;
    }

    public void setTitle(String title) {
        this.title = title;
    }


    public void setOnLocationChangeListener(OnLocationChangeListener onLocationChangeListener) {
        this.onLocationChangeListener = onLocationChangeListener;
    }

    @Override
    public int getSupportedMenu() {
        return R.menu.menu_location_switcher;
    }

    @Override
    public void prepareMenu() {
        if (baseActivity != null) {
            LocationActionView locationActionView = new LocationActionView(baseActivity,
                    baseActivity.getOpenSRPContext());

            locationActionView.getLocationPickerView()
                    .setOnLocationChangeListener(new LocationPickerView.OnLocationChangeListener() {
                        @Override
                        public void onLocationChange(String newLocation) {
                            if (onLocationChangeListener != null) {
                                onLocationChangeListener.onLocationChanged(newLocation);
                            }
                        }
                    });
            CustomFontTextView titleTV = (CustomFontTextView) baseActivity.findViewById(R.id.title);
            View separatorV = baseActivity.findViewById(R.id.separator_v);
            titleTV.setText(title);
            baseActivity.getMenu().findItem(R.id.location_switcher).setActionView(locationActionView);
            //separatorV.setBackgroundDrawable(baseActivity.getResources().getDrawable(separatorResourceId));
        }
    }

    @Override
    public MenuItem onMenuItemSelected(MenuItem menuItem) {
        return menuItem;
    }

    public void updateSeparatorView(int newView) {
        separatorResourceId = newView;
    }

    public interface OnLocationChangeListener {
        void onLocationChanged(String newLocation);
    }
}
