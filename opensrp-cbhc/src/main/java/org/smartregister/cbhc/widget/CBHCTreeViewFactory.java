package org.smartregister.cbhc.widget;

import android.text.TextUtils;
import android.util.Log;
import android.widget.EditText;

import com.vijay.jsonwizard.R;
import com.vijay.jsonwizard.customviews.TreeViewDialog;
import com.vijay.jsonwizard.widgets.TreeViewFactory;

import org.json.JSONArray;
import org.json.JSONException;
import org.smartregister.cbhc.util.Utils;

/**
 * @author Jason Rogena - jrogena@ona.io
 * @since 07/02/2017
 */
public class CBHCTreeViewFactory extends TreeViewFactory {
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
                Utils.appendLog(CBHCTreeViewFactory.class.getName(), e);
                Log.e(TAG, Log.getStackTraceString(e));
            }
        }

        editText.setText(readableValue);
    }


}