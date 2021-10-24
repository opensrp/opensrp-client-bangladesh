package org.smartregister.cbhc.repository;

import android.content.ContentValues;
import android.database.Cursor;

import net.sqlcipher.database.SQLiteDatabase;

import org.smartregister.cbhc.domain.CampaignForm;
import org.smartregister.cbhc.domain.ScheduleData;
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


    //private static final String BASE_ENTITY_ID_INDEX = "CREATE INDEX " + CAMPAIGN_TABLE_NAME + "_" + BASE_ENTITY_ID + "_index ON " + CAMPAIGN_TABLE_NAME + "(" + BASE_ENTITY_ID + " COLLATE NOCASE);";
    //private s/tatic final String UPDATED_AT_INDEX = "CREATE INDEX " + CAMPAIGN_TABLE_NAME + "_" + UPDATED_AT_COLUMN + "_index ON " + CAMPAIGN_TABLE_NAME + "(" + UPDATED_AT_COLUMN + ");";

    public static void createTable(SQLiteDatabase database) {
        database.execSQL(SCHEDULE_SQL);
    }

    /*public ArrayList<CampaignForm> getAllSchedule() {
        ArrayList<CampaignForm> campaignFormList = new ArrayList<>();
        SQLiteDatabase sqLiteDatabase = getWritableDatabase();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEE MMM dd");
        Cursor cursor = sqLiteDatabase.rawQuery("select * from campaign order by updated_at DESC", null);
        if (cursor != null && cursor.getCount() > 0 && cursor.moveToFirst()) {
            while (!cursor.isAfterLast()) {
                try {
                    campaignFormList.add(new CampaignForm(
                            cursor.getString(cursor.getColumnIndex(NAME)),
                            cursor.getString(cursor.getColumnIndex(TYPE)),
                            cursor.getString(cursor.getColumnIndex(BASE_ENTITY_ID)),
                            cursor.getString(cursor.getColumnIndex(TARGET_DATE)),
                            simpleDateFormat.parse(cursor.getString(cursor.getColumnIndex(UPDATED_AT_COLUMN))),
                            simpleDateFormat.parse(cursor.getString(cursor.getColumnIndex(CREATED_AT)))));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                cursor.moveToNext();
            }
        }
        return campaignFormList;
    }*/

    public long saveData(ScheduleData form) {
        SQLiteDatabase database = getWritableDatabase();
        return database.insert(SCHEDULE_TABLE_NAME,null,createFormValues(form));
    }

    private ContentValues createFormValues(ScheduleData object) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        ContentValues values = new ContentValues();
        values.put(BASE_ENTITY_ID,object.getCampaignBaseEntityId());
        values.put(TARGET_DATE, dateFormat.format(object.getTargetDate()));
        values.put(CREATED_AT, dateFormat.format(object.getCreatedDate()));
        values.put(UPDATED_AT_COLUMN, dateFormat.format(object.getUpdatedDate()));
        values.put(SYNC_STATUS,  object.getSyncStatus());
        return values;
    }

    public void removeScheduleFromToday(Date updatedDate) {
        SQLiteDatabase database = getWritableDatabase();
        database.delete(SCHEDULE_TABLE_NAME,TARGET_DATE+"<= ?",new String[]{updatedDate.toString()});
    }
}
