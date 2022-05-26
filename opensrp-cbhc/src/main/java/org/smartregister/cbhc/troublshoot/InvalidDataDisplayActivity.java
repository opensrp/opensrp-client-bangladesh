package org.smartregister.cbhc.troublshoot;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import net.sqlcipher.database.SQLiteDatabase;

import org.smartregister.cbhc.R;
import org.smartregister.cbhc.application.AncApplication;
import org.smartregister.cbhc.util.AppExecutors;
import org.smartregister.domain.db.Client;
import org.smartregister.domain.db.Event;
import org.smartregister.repository.EventClientRepository;
import org.smartregister.sync.helper.ECSyncHelper;
import org.smartregister.view.activity.SecuredActivity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

public class InvalidDataDisplayActivity extends SecuredActivity {
    private static final String TYPE ="type";
    public static final int TYPE_CLIENT =1;
    public static final int TYPE_EVENT =2;
    private RecyclerView recyclerView;
    private TextView countTv;
    private AppExecutors appExecutors;
    private int type;
    private ArrayList<InvalidDataModel> invalidDataModels = new ArrayList<>();
    public static void startInvalidActivity(int type, Activity activity){
        Intent intent = new Intent(activity,InvalidDataDisplayActivity.class);
        intent.putExtra(TYPE,type);
        activity.startActivity(intent);
    }
    @Override
    protected void onCreation() {
        setContentView(R.layout.activity_invalid_data);
        findViewById(R.id.backBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        recyclerView = findViewById(R.id.recycler_view);
        countTv = findViewById(R.id.count_tv);
        this.type = getIntent().getIntExtra(TYPE,0);
        if(type==TYPE_CLIENT){
            findViewById(R.id.event_view).setVisibility(View.GONE);
        }else if(type==TYPE_EVENT){
            findViewById(R.id.client_view).setVisibility(View.GONE);
        }
        appExecutors = new AppExecutors();
        showProgressBar(true);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        loadInvalidData();

    }
    private void loadInvalidData(){
        Runnable runnable = () -> {
            EventClientRepository eventClientRepository = AncApplication.getInstance().getEventClientRepository();

            if(type == TYPE_CLIENT){
                Map<Integer,Client> clientMap = eventClientRepository.getInvalidClientList();
                Map<Integer,Client> reverseSortedMap = new TreeMap<Integer,Client>(Collections.reverseOrder());
                reverseSortedMap.putAll(clientMap);
                for(Integer key: reverseSortedMap.keySet()){
                    Client client = reverseSortedMap.get(key);
                    InvalidDataModel invalidDataModel = new InvalidDataModel();
                    invalidDataModel.baseEntityId = client.getBaseEntityId();
                    invalidDataModel.errorCause = "Synced,Invalid";
                    invalidDataModel.action = "সিঙ্ক বাটন প্রেস করুন";
                    if(TextUtils.isEmpty(invalidDataModel.baseEntityId)){
                        invalidDataModel.errorCause = "Base entity id null";
                        invalidDataModel.action = "ডাটা ডিলিট করে দিন";
                        invalidDataModel.needToDelete = true;
                        invalidDataModel.rowId = key;
                    }
                    invalidDataModel.address = client.getAddresses().toString();
                    if(client.getAddresses().size() == 0){
                        invalidDataModel.errorCause = "এড্রেস পাওয়া যায়নি";
                        invalidDataModel.action = "এর হাউসহোল্ড টি এডিট করুন";
                    }else{
                        invalidDataModel.address =  client.getAddresses().get(0).getCityVillage();
                    }
                    invalidDataModel.firstName = client.getFirstName();
                    if(client.getLastName()!=null && client.getLastName().equalsIgnoreCase("family")){
                        invalidDataModel.eventType = "Household";
                    }else{
                        invalidDataModel.eventType = "Member";
                    }
                    invalidDataModel.client = client;
                    invalidDataModel.date = client.getDateCreated();
                    invalidDataModel.unique_id = client.getIdentifier("opensrp_id");
                    invalidDataModels.add(invalidDataModel);
                }

            }

            else if(type == TYPE_EVENT){
                Map<Integer,Event> eventMap = eventClientRepository.getInvalidEventList();
                Map<Integer,Event> reverseSortedMap = new TreeMap<Integer,Event>(Collections.reverseOrder());
                reverseSortedMap.putAll(eventMap);
                for(Integer key: reverseSortedMap.keySet()){
                    Event event = reverseSortedMap.get(key);
                    event.setServerVersion(0);
                    InvalidDataModel invalidDataModel = new InvalidDataModel();
                    invalidDataModel.baseEntityId = event.getBaseEntityId();
                    invalidDataModel.errorCause = "Synced,Invalid";
                    invalidDataModel.action = "সিঙ্ক বাটন প্রেস করুন";
                    if(TextUtils.isEmpty(invalidDataModel.baseEntityId)){
                        invalidDataModel.errorCause = "Base entity id null";
                        invalidDataModel.action = "ডাটা ডিলিট করে দিন";
                        invalidDataModel.needToDelete = true;
                        invalidDataModel.rowId = key;
                    }
                    invalidDataModel.serverVersion = event.getServerVersion();
                    if(invalidDataModel.serverVersion ==0){
                        invalidDataModel.errorCause = "Server version null";
                        invalidDataModel.action = "মেনু থেকে সিঙ্ক বাটন প্রেস করুন";
                        invalidDataModel.needToDelete = false;
                    }
                    invalidDataModel.formSubmissionId = event.getFormSubmissionId();

                    if(TextUtils.isEmpty(invalidDataModel.formSubmissionId)){
                        invalidDataModel.errorCause = "formSubmissionId null";
                        invalidDataModel.action = "ডাটা ডিলিট করে দিন";
                        invalidDataModel.needToDelete = true;
                        invalidDataModel.rowId = key;
                    }
                    invalidDataModel.eventType = event.getEventType();
                    invalidDataModel.event = event;
                    invalidDataModel.date = event.getEventDate();
                    invalidDataModels.add(invalidDataModel);
                }

            }
            appExecutors.mainThread().execute(this::updateAdapter);
        };

        appExecutors.diskIO().execute(runnable);
    }
    private void showProgressBar(boolean isVisible){
        findViewById(R.id.progress_bar).setVisibility(isVisible? View.VISIBLE:View.GONE);
    }
    InvalidDataAdapter adapter;
    private void updateAdapter(){
        showProgressBar(false);
        if(type==TYPE_CLIENT){
            countTv.setText("No of invalid client:"+invalidDataModels.size()+"");
        }else if(type==TYPE_EVENT){
            countTv.setText("No of invalid event:"+invalidDataModels.size()+"");
            countTv.setText(countTv.getText()+"\n Last server version:"+ ECSyncHelper.getInstance(this).getLastSyncTimeStamp());
        }
        adapter = new InvalidDataAdapter(this, new InvalidDataAdapter.OnClickAdapter() {
            @Override
            public void onClick(int position, InvalidDataModel content) {

            }

            @Override
            public void onDelete(int position, InvalidDataModel content) {
                SQLiteDatabase db = AncApplication.getInstance().getRepository().getWritableDatabase();
                Log.v("DELETE_UUUU","rowid>>>"+content.rowId);

                if(content.client!=null){
                   db.execSQL("delete from client where rowid ='"+content.rowId+"'");
                }else if(content.event !=null){
                   db.execSQL("delete from event where rowid ='"+content.rowId+"'");
                }
                invalidDataModels.remove(position);
                adapter.notifyItemRemoved(position);

            }
        },type);
        adapter.setData(invalidDataModels);
        recyclerView.setAdapter(adapter);

    }

    @Override
    protected void onResumption() {

    }
}
