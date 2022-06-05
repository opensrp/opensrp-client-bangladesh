package org.smartregister.cbhc.troublshoot;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import org.smartregister.CoreLibrary;
import org.smartregister.cbhc.R;
import org.smartregister.cbhc.application.AncApplication;
import org.smartregister.cbhc.job.SyncServiceJob;
import org.smartregister.cbhc.util.AppExecutors;
import org.smartregister.domain.FetchStatus;
import org.smartregister.job.DataSyncByBaseEntityServiceJob;
import org.smartregister.job.ForceSyncDataServiceJob;
import org.smartregister.job.InValidateSyncDataServiceJob;
import org.smartregister.receiver.SyncStatusBroadcastReceiver;
import org.smartregister.repository.EventClientRepository;
import org.smartregister.sync.intent.ForceSyncIntentService;
import org.smartregister.view.activity.SecuredActivity;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;

public class ForceSyncActivity extends SecuredActivity implements SyncStatusBroadcastReceiver.SyncStatusListener{
    private BroadcastReceiver invalidDataBroadcastReceiver;
    @Override
    protected void onCreation() {
        setContentView(R.layout.activity_troubleshoot);
        findViewById(R.id.backBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        findViewById(R.id.invalid_data).setOnClickListener(v -> checkInvalidData());
        findViewById(R.id.sync_unsync_btn).setOnClickListener( v -> forceSyncData() );
        findViewById(R.id.data_sync_by_id).setOnClickListener( v -> syncDataById() );
        findViewById(R.id.close_btn).setOnClickListener(v -> finish());
        findViewById(R.id.dump_btn).setOnClickListener( v -> dumpDatabase() );

    }
    ForceSynItemAdapter adapter;
    private void updateAdapter(ArrayList<ForceSyncModel> forceSyncModelArrayList) {
        adapter = new ForceSynItemAdapter(this, new ForceSynItemAdapter.OnClickAdapter() {
            @Override
            public void onClickItem(int position) {

            }
        });
        adapter.setData(forceSyncModelArrayList);
        ((RecyclerView)findViewById(R.id.recycler_view)).setAdapter(adapter);

    }


    private void dumpDatabase(){
        AppExecutors appExecutors = new AppExecutors();
        ((Button)findViewById(R.id.dump_btn)).setText("ডাটাবেস ডাম্প নেওয়া হচ্ছে ");
        Runnable runnable = () -> {
            try{

                String userName = CoreLibrary.getInstance().context().allSharedPreferences().fetchRegisteredANM();
                String password = CoreLibrary.getInstance().context().allSharedPreferences().fetchDefaultTeamId(userName);
                Log.v("DUMP_DB","password:"+password+":userName:"+userName);
                @SuppressLint("SdCardPath") File Db = new File("/data/data/"+getPackageName()+"/databases/drishti.db");
                String filePath = getExternalFilesDir(null) + "/db";
                File file = new File(filePath);
                if(!file.exists()){
                    file.mkdir();
                }
                filePath = (file.getAbsolutePath() + "/"+ "drishti.db");
                try {
                    File logFile = new File(file.getAbsolutePath() + "/"+ "keys.txt");
                    //BufferedWriter for performance, true to set append to file flag
                    BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true));
                    buf.append(password);
                    buf.newLine();
                    buf.close();
                } catch (IOException e) {

                }
                File finalFile = new File(filePath);

                finalFile.setWritable(true);

                copyFile(new FileInputStream(Db), new FileOutputStream(finalFile));

            }catch (Exception e){
                e.printStackTrace();

            }

            appExecutors.mainThread().execute(() ->  ((Button)findViewById(R.id.dump_btn)).setText("ডাম্প নেওয়া শেষ হয়েছে"));
        };
        appExecutors.diskIO().execute(runnable);
    }
    public static void copyFile(FileInputStream fromFile, FileOutputStream toFile) throws IOException {
        FileChannel fromChannel = null;
        FileChannel toChannel = null;
        try {
            fromChannel = fromFile.getChannel();
            toChannel = toFile.getChannel();
            fromChannel.transferTo(0, fromChannel.size(), toChannel);
        } finally {
            try {
                if (fromChannel != null) {
                    fromChannel.close();
                }
            } finally {
                if (toChannel != null) {
                    toChannel.close();
                }
            }
            Log.v("DUMP_DB","done");
        }
    }


    private void syncDataById() {
        invalidDataBroadcastReceiver = new InvalidSyncBroadcast();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("DATA_SYNC");
        registerReceiver(invalidDataBroadcastReceiver, intentFilter);
        showProgressDialog("ডাটা সিঙ্ক করা হচ্ছে....");
        DataSyncByBaseEntityServiceJob.scheduleJobImmediately(DataSyncByBaseEntityServiceJob.TAG);
    }
    private void forceSyncData() {
        invalidDataBroadcastReceiver = new InvalidSyncBroadcast();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ForceSyncIntentService.ACTION_SYNC);
        registerReceiver(invalidDataBroadcastReceiver, intentFilter);
        showProgressDialog("ডাটা সিঙ্ক করা হচ্ছে....");
        SyncStatusBroadcastReceiver.getInstance().addSyncStatusListener(ForceSyncActivity.this);
        ForceSyncDataServiceJob.scheduleJobImmediately(ForceSyncDataServiceJob.TAG);
    }
    private void checkInvalidData() {
        EventClientRepository eventClientRepository = AncApplication.getInstance().getEventClientRepository();
        int cc = eventClientRepository.getInvalidClientsCount();
        int ec = eventClientRepository.getInvalidEventsCount();
        showInvalidCountDialog(cc,ec,false);


    }
//    private void checkServerVersionNullData() {
//        EventClientRepository eventClientRepository = HnppApplication.getHNPPInstance().getEventClientRepository();
//        int cc = eventClientRepository.getInvalidClientsCount();
//        int ec = eventClientRepository.getInvalidEventsCount(true);
//        showInvalidCountDialog(cc,ec,true);
//
//
//    }
    @SuppressLint("SetTextI18n")
    private void showInvalidCountDialog(int cc, int ec, boolean isFromServerCheck ){
        Dialog dialog = new Dialog(this);
        dialog.setCancelable(false);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_invalid_data);
        TextView ccountTxt = dialog.findViewById(R.id.client_count_tv);
        ccountTxt.setText("No Of Invalid Client: "+cc);
        TextView ecountTxt = dialog.findViewById(R.id.event_count_tv);
        ecountTxt.setText("No Of Invalid Event: "+ec);
        Button clientShowBtn = dialog.findViewById(R.id.client_btn);
        Button eventShowBtn = dialog.findViewById(R.id.event_btn);
        Button syncBtn = dialog.findViewById(R.id.invalid_sync_btn);
        Button closeBtn = dialog.findViewById(R.id.close_btn);

        clientShowBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                InvalidDataDisplayActivity.startInvalidActivity(InvalidDataDisplayActivity.TYPE_CLIENT,ForceSyncActivity.this);
            }
        });
        eventShowBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                InvalidDataDisplayActivity.startInvalidActivity(InvalidDataDisplayActivity.TYPE_EVENT,ForceSyncActivity.this);

            }
        });

        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        syncBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(cc==0 && ec==0){
                    Toast.makeText(ForceSyncActivity.this,"কোনো ইনভ্যালিড ডাটা পাওয়া যায়নি",Toast.LENGTH_SHORT).show();
                    return;
                }
                invalidDataBroadcastReceiver = new InvalidSyncBroadcast();
                IntentFilter intentFilter = new IntentFilter();
                intentFilter.addAction("INVALID_SYNC");
                registerReceiver(invalidDataBroadcastReceiver, intentFilter);
                showProgressDialog("ইনভ্যালিড ডাটা সিঙ্ক করা হচ্ছে....");
                dialog.dismiss();
                SyncStatusBroadcastReceiver.getInstance().addSyncStatusListener(ForceSyncActivity.this);
                InValidateSyncDataServiceJob.scheduleJobImmediately(InValidateSyncDataServiceJob.TAG);

            }
        });
        dialog.show();

    }



    private ProgressDialog dialog;
    private void showProgressDialog(String message){
        if(dialog == null){
            dialog = new ProgressDialog(this);
            dialog.setMessage(message);
            dialog.setCancelable(true);
            dialog.show();
        }

    }
    private void hideProgressDialog(){
        if(dialog !=null && dialog.isShowing()){
            dialog.dismiss();
            dialog = null;
        }
    }
    @Override
    public void onDestroy() {
        SyncStatusBroadcastReceiver.getInstance().removeSyncStatusListener(this);
        if(invalidDataBroadcastReceiver!=null)unregisterReceiver(invalidDataBroadcastReceiver);
        super.onDestroy();
    }

    @Override
    protected void onResumption() {

    }

    @Override
    public void onSyncStart() {

    }

    @Override
    public void onSyncInProgress(FetchStatus fetchStatus) {

    }

    @Override
    public void onSyncComplete(FetchStatus fetchStatus) {

        hideProgressDialog();
        Toast.makeText(this,"সিঙ্ক কমপ্লিট। আরো ইনভ্যালিড ডাটা থাকলে সিঙ্ক করুন",Toast.LENGTH_SHORT).show();
        //finish();
    }
    private class InvalidSyncBroadcast extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            try{
                if(isFinishing()) return;
                hideProgressDialog();
                if(intent != null && intent.getAction().equalsIgnoreCase("INVALID_SYNC")){
                    String value = intent.getStringExtra("EXTRA_INVALID_SYNC");
                    Toast.makeText(ForceSyncActivity.this,value,Toast.LENGTH_SHORT).show();
                    showProgressDialog("ডাটা সিঙ্ক করা হচ্ছে....");
                    SyncServiceJob.scheduleJobImmediately(SyncServiceJob.TAG);
                }
                if(intent != null && intent.getAction().equalsIgnoreCase("DATA_SYNC")){
                    String value = intent.getStringExtra("EXTRA_DATA_SYNC");
                    Toast.makeText(ForceSyncActivity.this,value,Toast.LENGTH_SHORT).show();
                }
                if(intent != null && intent.getAction().equalsIgnoreCase("COMPARE_DATA")){
                    String value = intent.getStringExtra("EXTRA_COMPARE_DATA");
                    Toast.makeText(ForceSyncActivity.this,value,Toast.LENGTH_SHORT).show();
                }
                if(intent != null && intent.getAction().equalsIgnoreCase(ForceSyncIntentService.ACTION_SYNC)){
                    String value = intent.getStringExtra(ForceSyncIntentService.EXTRA_SYNC);
                    Toast.makeText(ForceSyncActivity.this,value,Toast.LENGTH_SHORT).show();
                    showProgressDialog("ডাটা সিঙ্ক করা হচ্ছে....");
                    SyncServiceJob.scheduleJobImmediately(SyncServiceJob.TAG);
                }
            }catch (Exception e){

            }

        }
    }
}
