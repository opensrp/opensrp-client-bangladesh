package org.smartregister.cbhc.watchers;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;

import com.vijay.jsonwizard.R;
import com.vijay.jsonwizard.fragments.JsonFormFragment;

import org.apache.commons.lang3.StringUtils;
import org.smartregister.Context;
import org.smartregister.cbhc.domain.EntityLookUp;
import org.smartregister.cbhc.fragment.AncJsonFormFragment;
import org.smartregister.cbhc.util.MotherLookUpUtils;
import org.smartregister.commonregistry.CommonPersonObject;
import org.smartregister.event.Listener;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.smartregister.cbhc.fragment.AncJsonFormFragment.lookuptype;


public class LookUpTextWatcher implements TextWatcher {
    private static Map<String, EntityLookUp> lookUpMap;
    public String relationalid;
    private View mView;
    private JsonFormFragment formFragment;
    private String mEntityId;


    public LookUpTextWatcher(JsonFormFragment formFragment, View view, String entityId, String relationalId) {
        this.formFragment = formFragment;
        mView = view;
        mEntityId = entityId;
        relationalid = relationalId;
        lookUpMap = new HashMap<>();

    }

    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
    }

    public void onTextChanged(CharSequence charSequence, int start, int before, int count) {

    }

    public void afterTextChanged(Editable editable) {
        String text = (String) mView.getTag(R.id.raw_value);

        if (text == null) {
            text = editable.toString();
        }

        String key = (String) mView.getTag(R.id.key);

        boolean afterLookUp = (Boolean) mView.getTag(R.id.after_look_up);
        if (afterLookUp) {
            mView.setTag(R.id.after_look_up, false);
            return;
        }

        EntityLookUp entityLookUp = new EntityLookUp();
        if (lookUpMap.containsKey(mEntityId)) {
            entityLookUp = lookUpMap.get(mEntityId);
        }

        if (StringUtils.isBlank(text)) {
            if (entityLookUp.containsKey(key)) {
                entityLookUp.remove(key);
            }
        } else {
            entityLookUp.put(key, text);
        }

        lookUpMap.put(mEntityId, entityLookUp);

        Context context = null;
        Listener<HashMap<CommonPersonObject, List<CommonPersonObject>>> listener = null;
        if (formFragment instanceof AncJsonFormFragment) {
            AncJsonFormFragment pathJsonFormFragment = (AncJsonFormFragment) formFragment;
            context = pathJsonFormFragment.context();
            listener = pathJsonFormFragment.motherLookUpListener();
        }
        lookuptype = mEntityId;
        MotherLookUpUtils.motherLookUp(context, lookUpMap.get(mEntityId), listener, null, relationalid, lookuptype);


    }

}