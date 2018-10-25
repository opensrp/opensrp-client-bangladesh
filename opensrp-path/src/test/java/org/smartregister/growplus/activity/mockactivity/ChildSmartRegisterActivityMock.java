package org.smartregister.growplus.activity.mockactivity;

import android.os.Bundle;
import android.view.Menu;
import android.view.inputmethod.InputMethodManager;

import org.smartregister.Context;
import org.smartregister.growplus.R;
import org.smartregister.growplus.activity.ChildSmartRegisterActivity;
import org.smartregister.growplus.activity.HouseholdSmartRegisterActivity;


/**
 * Created by kaderchowdhury on 04/12/17.
 */

public class ChildSmartRegisterActivityMock extends ChildSmartRegisterActivity {

    public static Context mContext;
    public static InputMethodManager inputManager;

    @Override
    public void onCreate(Bundle bundle) {
        setTheme(R.style.AppTheme); //we need this here
        super.onCreate(bundle);
    }

    public static void setmContext(Context Context) {
        mContext = Context;
    }

    @Override
    protected Context context() {
        return mContext;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public Object getSystemService(String name) {
        if (name.equalsIgnoreCase(INPUT_METHOD_SERVICE)) {
            return inputManager;
        } else {
            return super.getSystemService(name);
        }
    }

    @Override
    public void hideKeyboard() {

    }

}
