package org.smartregister.growplus.repository;

import android.content.ContentValues;
import android.database.Cursor;

import net.sqlcipher.database.SQLiteDatabase;

import org.smartregister.growplus.domain.ScheduleData;
import org.smartregister.repository.BaseRepository;
import org.smartregister.repository.Repository;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class ScheduleRepository extends BaseRepository {
    public static DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.ENGLISH);

    private static final String SCHEDULE_SQL = "CREATE TABLE schedule (_id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,campaign_base_entity_id VARCHAR NOT NULL,target_date VARCHAR NOT NULL,sync_status VARCHAR,updated_at DATETIME NULL,created_at DATETIME NOT NULL)";
    public static final String SCHEDULE_TABLE_NAME = "schedule";
    public static final String ID_COLUMN = "_id";
    public static final String BASE_ENTITY_ID = "campaign_base_entity_id";
    public static final String EVENT_ID = "event_id";
    public static final String FORMSUBMISSION_ID = "formSubmissionId";
    public static final String TARGET_DATE = "target_date";
    public static final String SYNC_STATUS = "sync_status";
    public static final String UPDATED_AT_COLUMN = "updated_at";
    public static final String CREATED_AT = "created_at";

    public ScheduleRepository(Repository repository) {
        super(repository);
    }

    public static void createTable(SQLiteDatabase database) {
        database.execSQL(SCHEDULE_SQL);
    }

    /**
     * saving schedule data here
     * @param form
     * @return
     */
    public long saveData(ScheduleData form) {
        SQLiteDatabase database = getWritableDatabase();
        return database.insert(SCHEDULE_TABLE_NAME,null,createFormValues(form));
    }

    /**
     * ContentValues to add specific data to sqlite db
     * @param object
     * @return
     */
    private ContentValues createFormValues(ScheduleData object) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        ContentValues values = new ContentValues();
        values.put(BASE_ENTITY_ID,object.getCampaignBaseEntityId());
        values.put(TARGET_DATE, dateFormat.format(object.getTargetDate()));
        values.put(CREATED_AT, dateFormat.format(object.getCreatedDate()));
        values.put(UPDATED_AT_COLUMN, dateFormat.format(object.getUpdatedDate()));
        values.put(SYNC_STATUS,  object.getSyncStatus());
        return values;
    }


    public long updateData(ScheduleData form) {
        SQLiteDatabase database = getWritableDatabase();
        return database.update(SCHEDULE_TABLE_NAME,updateFormValues(form),BASE_ENTITY_ID+" =?", new String[]{form.getCampaignBaseEntityId()});
    }

    /**
     * all schedule update
     * @param object
     * @return
     */
    private ContentValues updateFormValues(ScheduleData object) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        ContentValues values = new ContentValues();
        values.put(TARGET_DATE, dateFormat.format(object.getTargetDate()));
        values.put(UPDATED_AT_COLUMN, dateFormat.format(object.getUpdatedDate()));
        values.put(SYNC_STATUS,  object.getSyncStatus());
        return values;
    }

    /**
     * remove all schedule when target date updated
     * @param updateTargetDate
     * @param baseEntityId
     */
    public void removeScheduleFromToday(Date updateTargetDate,String baseEntityId) {
        SQLiteDatabase database = getWritableDatabase();
        database.delete(SCHEDULE_TABLE_NAME,TARGET_DATE+"<= ? and "+BASE_ENTITY_ID+" =?",new String[]{updateTargetDate.toString(),baseEntityId});
    }

    /**
     * getting all schedule data according to base entity id
     * @param baseEntityId
     * @return
     */
    public ArrayList<ScheduleData> getAllScheduleData(String baseEntityId) {
        ArrayList<ScheduleData> scheduleDataArrayList = new ArrayList<>();
        SQLiteDatabase sqLiteDatabase = getWritableDatabase();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        Cursor cursor = sqLiteDatabase.rawQuery("select * from schedule where campaign_base_entity_id =?"+" order by datetime(target_date) DESC", new String[]{baseEntityId});
        if (cursor != null && cursor.getCount() > 0 && cursor.moveToFirst()) {
            while (!cursor.isAfterLast()) {
                try {
                    scheduleDataArrayList.add(new ScheduleData(
                            cursor.getString(cursor.getColumnIndex(BASE_ENTITY_ID)),
                            dateFormat.parse(cursor.getString(cursor.getColumnIndex(TARGET_DATE))),
                            dateFormat.parse(cursor.getString(cursor.getColumnIndex(UPDATED_AT_COLUMN))),
                            dateFormat.parse(cursor.getString(cursor.getColumnIndex(CREATED_AT))),
                            cursor.getString(cursor.getColumnIndex(SYNC_STATUS)))
                    );
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                cursor.moveToNext();
            }
        }
        return scheduleDataArrayList;
    }
}
