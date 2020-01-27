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
        editText.setTag(com.vijay.jsonwizard.R.id.locale_independent_value, jsonObject.optString(TimePickerFactory.KEY.VALUE));
        super.attachLayout(stepName, context, formFragment, jsonObject, editText, duration);

    }
}
