package org.smartregister.path.view;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;

import org.robolectric.RuntimeEnvironment;
import org.smartregister.growplus.R;

/**
 * Created by kaderchowdhury on 10/12/17.
 */

public class ViewActivityMock extends Activity {

    Context context;

    LocationActionView locationActionView;
    @Override
    protected void onCreate(@Nullable Bundle bundle) {
        setTheme(R.style.AppTheme); //we need this here
        super.onCreate(bundle);
        context = RuntimeEnvironment.application;

    }

    public void loadLocationActionView(org.smartregister.Context opensrpContext) {
        locationActionView = new LocationActionView(context,opensrpContext);
        locationActionView = new LocationActionView(context,opensrpContext,ViewAttributes.attrs);
        locationActionView = new LocationActionView(context,opensrpContext,ViewAttributes.attrs,0);
        locationActionView = new LocationActionView(context,opensrpContext,ViewAttributes.attrs,0,0);
    }

}
