package org.smartregister.cbhc.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatTextView;
import android.text.InputType;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.rengwuxian.materialedittext.MaterialEditText;
import com.vijay.jsonwizard.fragments.JsonFormFragment;
import com.vijay.jsonwizard.presenters.JsonFormFragmentPresenter;
import com.vijay.jsonwizard.utils.FormUtils;
import com.vijay.jsonwizard.widgets.DatePickerFactory;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.smartregister.Context;
import org.smartregister.cbhc.application.AncApplication;
import org.smartregister.cbhc.interactor.AncJsonFormInteractor;
import org.smartregister.cbhc.util.DBConstants;
import org.smartregister.cbhc.viewstate.AncJsonFormFragmentViewState;
import org.smartregister.commonregistry.CommonPersonObject;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.event.Listener;
import org.smartregister.util.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by keyman on 11/04/2017.
 */
public class PathJsonFormFragment extends JsonFormFragment {

    private Snackbar snackbar = null;
    private AlertDialog alertDialog = null;
    private boolean lookedUp = false;

    public static PathJsonFormFragment getFormFragment(String stepName) {
        PathJsonFormFragment jsonFormFragment = new PathJsonFormFragment();
        Bundle bundle = new Bundle();
        bundle.putString(DBConstants.KEY.STEPNAME, stepName);
        jsonFormFragment.setArguments(bundle);
        return jsonFormFragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    protected AncJsonFormFragmentViewState createViewState() {
        return new AncJsonFormFragmentViewState();
    }

    @Override
    protected JsonFormFragmentPresenter createPresenter() {
        return new JsonFormFragmentPresenter(this, AncJsonFormInteractor.getInstance());
    }

    public Context context() {
        return AncApplication.getInstance().getContext();
    }



    private void disableEditText(MaterialEditText editText) {
        editText.setInputType(InputType.TYPE_NULL);
    }

    private void enableEditText(MaterialEditText editText) {
        editText.setInputType(InputType.TYPE_CLASS_TEXT);
    }






    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
      return super.onOptionsItemSelected(item);
    }


}


