package org.smartregister.cbhc.view;

import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.Window;

import org.json.JSONObject;
import org.smartregister.cbhc.R;
import org.smartregister.cbhc.application.AncApplication;
import org.smartregister.repository.EventClientRepository;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import timber.log.Timber;

import static org.smartregister.AllConstants.ROWID;

public class BlocksDialog extends Dialog implements View.OnClickListener {

    public BlocksDialog(@NonNull Context context) {
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.blockselectiondialog);

    }
    public static DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    public void updateLocationTable(String baseEntityId, JSONObject jsonObject, String syncStatus) {
        try {

            ContentValues values = new ContentValues();
            values.put("value", jsonObject.toString());

            AncApplication.getInstance().getRepository().getWritableDatabase()
                    .update("settings",values,"key = ?",new String[]{"anmLocation"});
// a case here would be if an event comes from openmrs
          //  AncApplication.getInstance().getRepository().getWritableDatabase().insert(EventClientRepository.Table.event.name(), null, values);


        } catch (Exception e) {
            Timber.e(e);
        }
    }
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.submitblocks:
                break;
        }
    }

}
