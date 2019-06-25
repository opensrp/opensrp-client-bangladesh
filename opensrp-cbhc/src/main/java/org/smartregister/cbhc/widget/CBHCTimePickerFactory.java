package org.smartregister.cbhc.widget;

import android.content.Context;
import android.widget.TextView;

import com.rengwuxian.materialedittext.MaterialEditText;
import com.vijay.jsonwizard.fragments.JsonFormFragment;
import com.vijay.jsonwizard.widgets.TimePickerFactory;

import org.json.JSONObject;

public class CBHCTimePickerFactory extends TimePickerFactory {

    @Override
    protected void attachLayout(String stepName, Context context, JsonFormFragment formFragment, JSONObject jsonObject, MaterialEditText editText, TextView duration) {

        super.attachLayout(stepName, context, formFragment, jsonObject, editText, duration);
        if(editText.getText().toString().isEmpty())
            editText.setText("00:00");
    }
}
