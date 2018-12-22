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


public class BirthDateTextWatcher implements TextWatcher {
    private static Map<String, EntityLookUp> lookUpMap;

    private View mView;
    private JsonFormFragment formFragment;
    private String mEntityId;
    public String relationalid;


    public BirthDateTextWatcher(JsonFormFragment formFragment, View view) {
        this.formFragment = formFragment;
        mView = view;
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
        ((AncJsonFormFragment)formFragment).setAgeFromBirthDate(text);
    }

}