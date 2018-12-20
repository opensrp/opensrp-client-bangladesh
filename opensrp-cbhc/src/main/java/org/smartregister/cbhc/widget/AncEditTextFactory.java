package org.smartregister.cbhc.widget;

import android.content.Context;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.rengwuxian.materialedittext.MaterialEditText;
import com.rey.material.util.ViewUtil;
import com.vijay.jsonwizard.fragments.JsonFormFragment;
import com.vijay.jsonwizard.interfaces.CommonListener;
import com.vijay.jsonwizard.interfaces.JsonApi;
import com.vijay.jsonwizard.widgets.EditTextFactory;

import org.json.JSONArray;
import org.json.JSONObject;
import org.smartregister.cbhc.R;
import org.smartregister.cbhc.util.DBConstants;
import org.smartregister.cbhc.watchers.LookUpTextWatcher;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 * Created by ndegwamartin on 30/06/2018.
 */
public class AncEditTextFactory extends EditTextFactory {

    @Override
    public void attachJson(String stepName, Context context, JsonFormFragment formFragment, JSONObject jsonObject, MaterialEditText editText, ImageView editButton) throws Exception {
        super.attachJson(stepName, context, formFragment, jsonObject, editText, editButton);
        // lookup hook
        if (jsonObject.has("look_up") && jsonObject.get("look_up").toString().equalsIgnoreCase(Boolean.TRUE.toString())) {
            String entityId = jsonObject.getString("key");
            if(jsonObject.has("entity_id")) {
                entityId = jsonObject.getString("entity_id");
            }

            Map<String, List<View>> lookupMap = formFragment.getLookUpMap();
            List<View> lookUpViews = new ArrayList<>();
            if (lookupMap.containsKey(entityId)) {
                lookUpViews = lookupMap.get(entityId);
            }

            if (!lookUpViews.contains(editText)) {
                lookUpViews.add(editText);
            }
            lookupMap.put(entityId, lookUpViews);
            String household_id = "";
            if (jsonObject.has("relational_id")){
                household_id = jsonObject.getString("relational_id");
            }
            editText.addTextChangedListener(new LookUpTextWatcher(formFragment, editText, entityId,household_id));
            editText.setTag(com.vijay.jsonwizard.R.id.after_look_up, false);
        }
    }

    @Override
    public List<View> getViewsFromJson(String stepName, Context context, JsonFormFragment formFragment, JSONObject jsonObject, CommonListener listener) throws Exception {
        if (jsonObject.has(DBConstants.KEY.NUMBER_PICKER) && jsonObject.get(DBConstants.KEY.NUMBER_PICKER).toString().equalsIgnoreCase(Boolean.TRUE.toString())) {
            List<View> views = new ArrayList<>(1);

            RelativeLayout rootLayout = getRootLayout(context);
            final MaterialEditText editText = rootLayout.findViewById(R.id.edit_text);
            ImageView editButton = (ImageView)rootLayout.findViewById(com.vijay.jsonwizard.R.id.material_edit_text_edit_button);
            editButton.setVisibility(View.INVISIBLE);

            attachJson(stepName, context, formFragment, jsonObject, editText, editButton);

            JSONArray canvasIds = new JSONArray();
            rootLayout.setId(ViewUtil.generateViewId());
            canvasIds.put(rootLayout.getId());
            editText.setTag(com.vijay.jsonwizard.R.id.canvas_ids, canvasIds.toString());

            ((JsonApi) context).addFormDataView(editText);
            views.add(rootLayout);

            Button plusbutton = rootLayout.findViewById(R.id.addbutton);
            Button minusbutton = rootLayout.findViewById(R.id.minusbutton);

            plusbutton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String edittesxtstring = editText.getText().toString();
                    if (edittesxtstring.equalsIgnoreCase("")) {
                        editText.setText("0");
                    } else {
                        edittesxtstring = "" + (Integer.parseInt(edittesxtstring) + 1);
                        editText.setText(edittesxtstring);
                    }
                }
            });
            minusbutton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String edittesxtstring = editText.getText().toString();
                    if (edittesxtstring.equalsIgnoreCase("")) {
                        editText.setText("0");
                    } else {
                        edittesxtstring = "" + (Integer.parseInt(edittesxtstring) - 1);
                        editText.setText(edittesxtstring);
                    }
                }
            });

            editText.setInputType(InputType.TYPE_CLASS_NUMBER |
                    InputType.TYPE_NUMBER_FLAG_SIGNED);


            return views;
        } else {
            return super.getViewsFromJson(stepName, context, formFragment, jsonObject, listener);
        }

    }

    protected RelativeLayout getRootLayout(Context context) {
        return (RelativeLayout) LayoutInflater.from(context).inflate(
                R.layout.item_edit_text_number_picker, null);
    }

}
