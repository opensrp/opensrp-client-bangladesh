package org.smartregister.path.activity.mockactivity;

import android.os.Bundle;

import org.smartregister.path.R;
import org.smartregister.view.activity.LoginActivity;

/**
 * Created by kaderchowdhury on 11/12/17.
 */

public class LoginActivityMock extends LoginActivity {
    @Override
    public void onCreate(Bundle bundle) {
        setTheme(R.style.AppTheme); //we need this here
        super.onCreate(bundle);
    }
}
