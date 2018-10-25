package org.smartregister.path.activity;

import android.os.Bundle;

import com.rengwuxian.materialedittext.MaterialEditText;
import com.vijay.jsonwizard.activities.JsonFormActivity;
import com.vijay.jsonwizard.constants.JsonFormConstants;

import org.json.JSONException;
import org.smartregister.path.fragment.PathJsonFormFragment;

/**
 * Created by keyman on 11/04/2017.
 */
public class PathJsonFormActivity extends JsonFormActivity {

    private int generatedId = -1;
    private MaterialEditText balancetextview;
    private PathJsonFormFragment pathJsonFormFragment;
    public static boolean isLaunched = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        isLaunched = true;
    }

    @Override
    public void initializeFormFragment() {
        isLaunched = true;
        pathJsonFormFragment = PathJsonFormFragment.getFormFragment(JsonFormConstants.FIRST_STEP_NAME);
        getSupportFragmentManager().beginTransaction()
                .add(com.vijay.jsonwizard.R.id.container, pathJsonFormFragment).commit();
    }

    @Override
    public void writeValue(String stepName, String key, String value, String openMrsEntityParent, String openMrsEntity, String openMrsEntityId) throws JSONException {
        super.writeValue(stepName, key, value, openMrsEntityParent, openMrsEntity, openMrsEntityId);
        refreshCalculateLogic(key, value);

    }

    @Override
    public void onFormFinish() {
        isLaunched = false;
        super.onFormFinish();
    }

    private void refreshCalculateLogic(String key, String value) {

    }






}

