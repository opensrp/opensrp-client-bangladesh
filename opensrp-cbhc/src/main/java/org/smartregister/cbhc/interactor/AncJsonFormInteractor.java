package org.smartregister.cbhc.interactor;

import com.vijay.jsonwizard.constants.JsonFormConstants;
import com.vijay.jsonwizard.interactors.JsonFormInteractor;
import com.vijay.jsonwizard.widgets.DatePickerFactory;

import org.smartregister.cbhc.widget.AncEditTextFactory;
import org.smartregister.cbhc.widget.CBHCDatePickerFactory;


/**
 * Created by ndegwamartin on 30/06/2018.
 */
public class AncJsonFormInteractor extends JsonFormInteractor {

    private static final JsonFormInteractor INSTANCE = new AncJsonFormInteractor();

    private AncJsonFormInteractor() {
        super();
    }

    @Override
    protected void registerWidgets() {
        super.registerWidgets();
        map.put(JsonFormConstants.EDIT_TEXT, new AncEditTextFactory());
        map.put(JsonFormConstants.DATE_PICKER, new CBHCDatePickerFactory());
    }

    public static JsonFormInteractor getInstance() {
        return INSTANCE;
    }
}
