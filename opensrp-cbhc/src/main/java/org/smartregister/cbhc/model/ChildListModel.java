package org.smartregister.cbhc.model;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.support.v7.widget.LinearLayoutManager;

import net.sqlcipher.database.SQLiteDatabase;

import org.smartregister.cbhc.activity.MemberProfileActivity;
import org.smartregister.cbhc.adapter.ChildListAdapter;
import org.smartregister.cbhc.application.AncApplication;
import org.smartregister.cbhc.contract.ChildListContract;
import org.smartregister.cbhc.domain.ChildItemData;
import org.smartregister.cbhc.util.Constants;
import org.smartregister.cbhc.util.DBConstants;
import org.smartregister.commonregistry.CommonPersonObject;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.commonregistry.CommonRepository;

import java.util.ArrayList;

public class ChildListModel implements ChildListContract.Model {
    private Context context;
    private ArrayList<ChildItemData> childItemDataArrayList;
    
    public ChildListModel(){
       /* this.context = context;*/
        this.childItemDataArrayList = new ArrayList<>();
    }
    

    @Override
    public void fetchChildList(String query) {
        childItemDataArrayList.clear();
        SQLiteDatabase sqLiteDatabase = AncApplication.getInstance().getRepository().getWritableDatabase();
        Cursor cursor = sqLiteDatabase.rawQuery( query, null);
        if (cursor != null && cursor.getCount() > 0 && cursor.moveToFirst()) {
            while (!cursor.isAfterLast()) {
                CommonRepository commonRepository = org.smartregister.Context.getInstance().commonrepository(DBConstants.CHILD_TABLE_NAME);
                CommonPersonObject personinlist = commonRepository.readAllcommonforCursorAdapter(cursor);
                //personinlist.setCaseId(cursor.getString(cursor.getColumnIndex("_id")));
                CommonPersonObjectClient pClient   = new CommonPersonObjectClient(personinlist.getCaseId(), personinlist.getDetails(), personinlist.getDetails().get("FWHOHFNAME"));
                pClient.setColumnmaps(personinlist.getColumnmaps());

                int baseEntity = cursor.getColumnIndex("base_entity_id");
                int fName = cursor.getColumnIndex("first_name");
                int lName = cursor.getColumnIndex("last_name");
                int dob = cursor.getColumnIndex("dob");
                int gender = cursor.getColumnIndex("gender");
                int child_status = cursor.getColumnIndex("child_status");
                int child_weight = cursor.getColumnIndex("child_weight");
                int child_height = cursor.getColumnIndex("child_height");
                int last_vaccine_name = cursor.getColumnIndex("last_vaccine_name");
                int last_vaccine_date = cursor.getColumnIndex("last_vaccine_date");


                childItemDataArrayList.add(new ChildItemData(
                        cursor.isNull(baseEntity)?"":cursor.getString(baseEntity),
                        cursor.isNull(fName)?"":cursor.getString(fName),
                        cursor.isNull(lName)?"":cursor.getString(lName),
                        cursor.isNull(dob)?"":cursor.getString(dob),
                        cursor.isNull(gender)?"":cursor.getString(gender),
                        cursor.getString(child_weight),cursor.getString(child_height)
                        ,cursor.getString(last_vaccine_name),cursor.getString(last_vaccine_date),
                        cursor.isNull(child_status)?"":cursor.getString(child_status),
                        pClient));
                cursor.moveToNext();
            }
        }
        // if(adapter==null){
     /*   adapter = new ChildListAdapter(getActivity(), childItemDataArrayList, new ChildListAdapter.TotalChild() {
            @Override
            public void onChange(String totalStr) {
                totalChildTv.setText(totalStr);
            }
        }, new ChildListAdapter.OnClickHandler() {
            @Override
            public void onClick(int position, ChildItemData childItemData) {
                Intent intent = new Intent(getActivity(), MemberProfileActivity.class);
                intent.putExtra(Constants.INTENT_KEY.BASE_ENTITY_ID, childItemData.getBaseEntityId());
                startActivityForResult(intent, 1002);
            }
        });


       *//* }else{
            adapter.setData(childItemDataArrayList);
        }*//*
        childListRv.setLayoutManager(new LinearLayoutManager(getActivity()));
        childListRv.setAdapter(adapter);*/
    }

    @Override
    public ArrayList<ChildItemData> getChildData() {
        return childItemDataArrayList;
    }
}
