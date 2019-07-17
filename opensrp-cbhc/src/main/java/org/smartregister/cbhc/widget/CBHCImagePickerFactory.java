package org.smartregister.cbhc.widget;

import android.content.Context;
import android.view.View;

import com.vijay.jsonwizard.fragments.JsonFormFragment;
import com.vijay.jsonwizard.interfaces.CommonListener;
import com.vijay.jsonwizard.widgets.ImagePickerFactory;

import org.json.JSONObject;

import java.util.List;

public class CBHCImagePickerFactory extends ImagePickerFactory {

    @Override
    public List<View> getViewsFromJson(String stepName, Context context, JsonFormFragment formFragment, JSONObject jsonObject, CommonListener listener, boolean popup) throws Exception {
        return super.getViewsFromJson(stepName, context, formFragment, jsonObject, listener, popup);
    }

    @Override
    public List<View> getViewsFromJson(String stepName, Context context, JsonFormFragment formFragment, JSONObject jsonObject, CommonListener listener) throws Exception {
        return super.getViewsFromJson(stepName, context, formFragment, jsonObject, listener);
    }
}
