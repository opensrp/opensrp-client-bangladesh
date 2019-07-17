package org.smartregister.cbhc.interactor;

import com.vijay.jsonwizard.constants.JsonFormConstants;
import com.vijay.jsonwizard.interactors.JsonFormInteractor;
import com.vijay.jsonwizard.widgets.DatePickerFactory;

import org.smartregister.cbhc.widget.AncEditTextFactory;
import org.smartregister.cbhc.widget.CBHCDatePickerFactory;
import org.smartregister.cbhc.widget.CBHCImagePickerFactory;
import org.smartregister.cbhc.widget.CBHCTimePickerFactory;
import org.smartregister.cbhc.widget.CBHCTreeViewFactory;


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
        map.put(JsonFormConstants.TREE, new CBHCTreeViewFactory());
        map.put(JsonFormConstants.TIME_PICKER,new CBHCTimePickerFactory());
        map.put(JsonFormConstants.CHOOSE_IMAGE,new CBHCImagePickerFactory());
    }

    public static JsonFormInteractor getInstance() {
        return INSTANCE;
    }
}
