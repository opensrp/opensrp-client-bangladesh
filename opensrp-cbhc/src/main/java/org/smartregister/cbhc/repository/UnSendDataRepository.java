package org.smartregister.cbhc.repository;

import android.content.ContentValues;
import android.database.Cursor;
import android.util.Log;

import net.sqlcipher.database.SQLiteDatabase;

import org.smartregister.cbhc.model.UnsendData;
import org.smartregister.cbhc.util.Utils;
import org.smartregister.repository.BaseRepository;
import org.smartregister.repository.Repository;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

public class UnSendDataRepository extends BaseRepository {

    private static final String TAG = UnSendDataRepository.class.getCanonicalName();
    private static final String UN_SEND_SQL = "CREATE TABLE unsend_data (_id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,base_entity_id VARCHAR NOT NULL,type VARCHAR NOT NULL,is_send int,last_interacted_with long)";
    public static final String UN_SEND_TABLE_NAME = "unsend_data";
    public static final String ID_COLUMN = "_id";
    public static final String BASE_ENTITY_ID = "base_entity_id";
    public static final String DATE = "last_interacted_with";
    public static final String TYPE = "type";
    public static final String IS_SEND = "is_send";


    public UnSendDataRepository(Repository repository) {
        super(repository);
    }
    public static void createTable(SQLiteDatabase database) {
        database.execSQL(UN_SEND_SQL);
    }

    public void saveData(UnsendData object) {
        SQLiteDatabase database = getWritableDatabase();
        long isInserted = database.insert(UN_SEND_TABLE_NAME,null,createFormValues(object));
        Log.v("UNSEND_DATE","isInserted>>>"+isInserted);
    }
    public ArrayList<UnsendData> getAllUnsendData(){
        SQLiteDatabase database = getWritableDatabase();
        Cursor cursor = database.query(UN_SEND_TABLE_NAME, null, IS_SEND + " = 0 ", null, null, null, null, null);
        return readAllUnsendData(cursor);
    }
    private ArrayList<UnsendData> readAllUnsendData(Cursor cursor) {
        ArrayList<UnsendData> unsendDataArrayList = new ArrayList<>();

        try {

            if (cursor != null && cursor.getCount() > 0 && cursor.moveToFirst()) {
                while (!cursor.isAfterLast()) {
                    UnsendData unsendData = new UnsendData();
                    unsendData.setBaseEntityId(cursor.getString(cursor.getColumnIndex(BASE_ENTITY_ID)));
                    unsendData.setLastInteractedDate(cursor.getLong(cursor.getColumnIndex(DATE)));
                    unsendData.setSend(cursor.getInt(cursor.getColumnIndex(IS_SEND))==1);
                    unsendData.setType(cursor.getString(cursor.getColumnIndex(TYPE)));
                    unsendDataArrayList.add(unsendData);
                    cursor.moveToNext();
                }
            }
        } catch (Exception e) {
            Utils.appendLog(getClass().getName(), e);
            Log.e(TAG, Log.getStackTraceString(e));
        } finally {
            cursor.close();
        }
        return unsendDataArrayList;
    }

    private ContentValues createFormValues(UnsendData object) {
        ContentValues values = new ContentValues();

        values.put(BASE_ENTITY_ID, object.getBaseEntityId());
        values.put(TYPE, object.getType());
        values.put(DATE, object.getLastInteractedDate());
        values.put(IS_SEND, object.isSend()?1:0);
        Log.v("UNSEND_DATE","values>>>"+values);
        return values;
    }
    public int updateSendingStatus(String baseEntityId){
        ContentValues values = new ContentValues();
        values.put(IS_SEND, 1);
        return getWritableDatabase().update(UN_SEND_TABLE_NAME, values, BASE_ENTITY_ID + " = ?", new String[]{baseEntityId});

    }
}
