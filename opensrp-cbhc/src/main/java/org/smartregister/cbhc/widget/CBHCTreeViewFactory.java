package org.smartregister.cbhc.widget;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.RelativeLayout;

import com.rengwuxian.materialedittext.MaterialEditText;
import com.rey.material.util.ViewUtil;
import com.vijay.jsonwizard.R;
import com.vijay.jsonwizard.constants.JsonFormConstants;
import com.vijay.jsonwizard.customviews.GenericTextWatcher;
import com.vijay.jsonwizard.customviews.TreeViewDialog;
import com.vijay.jsonwizard.fragments.JsonFormFragment;
import com.vijay.jsonwizard.interfaces.CommonListener;
import com.vijay.jsonwizard.interfaces.FormWidgetFactory;
import com.vijay.jsonwizard.interfaces.JsonApi;
import com.vijay.jsonwizard.validators.edittext.RequiredValidator;
import com.vijay.jsonwizard.widgets.TreeViewFactory;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static android.view.inputmethod.InputMethodManager.HIDE_NOT_ALWAYS;

/**
 * @author Jason Rogena - jrogena@ona.io
 * @since 07/02/2017
 */
public class  CBHCTreeViewFactory extends TreeViewFactory {
    private static final String TAG = "TreeViewFactory";

    private static void showTreeDialog(TreeViewDialog treeViewDialog) {
        treeViewDialog.show();
    }

    private static void changeEditTextValue(EditText editText, String value, String name) {
        String readableValue = "";
        editText.setTag(R.id.raw_value, value);
        if (!TextUtils.isEmpty(name)) {
            try {
                JSONArray nameArray = new JSONArray(name);
                if (nameArray.length() > 0) {
                    readableValue = nameArray.getString(nameArray.length() - 1);

                    if (nameArray.length() > 1) {
                        readableValue = readableValue + ", "
                                + nameArray.getString(nameArray.length() - 1);
                    }
                }
            } catch (JSONException e) {
                Log.e(TAG, Log.getStackTraceString(e));
            }
        }

        editText.setText(readableValue);
    }



}