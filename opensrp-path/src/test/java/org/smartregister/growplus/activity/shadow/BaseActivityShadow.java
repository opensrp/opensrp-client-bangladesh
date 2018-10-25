package org.smartregister.growplus.activity.shadow;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarDrawerToggle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import org.mockito.Mockito;
import org.opensrp.api.constants.Gender;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.shadows.ShadowActivity;
import org.smartregister.Context;
import org.smartregister.domain.FetchStatus;
import org.smartregister.growplus.activity.BaseActivity;
import org.smartregister.growplus.toolbar.BaseToolbar;

/**
 * Created by kaderchowdhury on 17/12/17.
 */
@Implements(BaseActivity.class)
public class BaseActivityShadow extends ShadowActivity {
    @Implementation
    protected void onCreate(Bundle savedInstanceState) {
    }

    @Implementation
    public void onSyncStart() {
    }

    @Implementation
    public void onSyncComplete(FetchStatus fetchStatus) {
    }

    @Implementation
    public BaseToolbar getBaseToolbar() {
        return Mockito.mock(BaseToolbar.class);
    }

    @Implementation
    protected ActionBarDrawerToggle getDrawerToggle() {
        return Mockito.mock(ActionBarDrawerToggle.class);
    }

    @Implementation
    protected void openDrawer() {
    }

    @Implementation
    protected void onResume() {
    }

    @Implementation
    protected void onPause() {
    }

    @Implementation
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Implementation
    public boolean onOptionsItemSelected(MenuItem item) {
        return true;
    }

    @Implementation
    public void onBackPressed() {

    }

    @Implementation
    protected String getLoggedInUserInitials() {
        return "IKC";
    }

    @Implementation
    public boolean onNavigationItemSelected(MenuItem item) {
        return false;
    }

    @Implementation
    protected int[] updateGenderViews(Gender gender) {
        return new int[]{0};
    }

    @Implementation
    protected void showNotification(int message, int notificationIcon, int positiveButtonText, View.OnClickListener positiveButtonClick, int negativeButtonText, View.OnClickListener negativeButtonClick, Object tag) {
    }

    @Implementation
    protected void hideNotification() {
    }

    @Implementation
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    }

    @Implementation
    protected BaseToolbar getToolbar() {
        return Mockito.mock(BaseToolbar.class);
    }


    @Implementation
    protected int getDrawerLayoutId() {
        return 0;
    }

    @Implementation
    protected int getToolbarId() {
        return 0;
    }

    @Implementation
    public Context getOpenSRPContext() {
        return Mockito.mock(Context.class);
    }

    @Implementation
    public Menu getMenu() {
        return Mockito.mock(Menu.class);
    }

    @Implementation
    protected Class onBackActivity() {
        return null;
    }

    @Implementation
    public void processInThread(Runnable runnable) {
    }

    @Implementation
    protected void showProgressDialog(String title, String message) {
    }

    @Implementation
    protected void showProgressDialog() {
    }

    @Implementation
    protected void hideProgressDialog() {
    }
}
