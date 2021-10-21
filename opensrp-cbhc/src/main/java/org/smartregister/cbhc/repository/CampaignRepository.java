package org.smartregister.cbhc.repository;

import android.content.ContentValues;
import android.database.Cursor;

import net.sqlcipher.database.SQLiteDatabase;

import org.smartregister.cbhc.domain.CampaignForm;
import org.smartregister.cbhc.domain.FollowupForm;
import org.smartregister.cbhc.domain.draft_form_object;
import org.smartregister.repository.BaseRepository;
import org.smartregister.repository.Repository;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

public class CampaignRepository extends BaseRepository {
    public static DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);

    private static final String CAMPAIGN_SQL = "CREATE TABLE campaign (_id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,base_entity_id VARCHAR NOT NULL,name VARCHAR NOT NULL,type VARCHAR NOT NULL,target_date VARCHAR NOT NULL,sync_status VARCHAR,updated_at DATETIME NULL,created_at DATETIME NOT NULL)";
    public static final String CAMPAIGN_TABLE_NAME = "campaign";
    public static final String ID_COLUMN = "_id";
    public static final String BASE_ENTITY_ID = "base_entity_id";
    public static final String EVENT_ID = "event_id";
    public static final String FORMSUBMISSION_ID = "formSubmissionId";
    public static final String NAME = "name";
    public static final String TYPE = "type";
    public static final String TARGET_DATE = "target_date";
    public static final String SYNC_STATUS = "sync_status";
    public static final String UPDATED_AT_COLUMN = "updated_at";
    public static final String CREATED_AT = "created_at";

    public static final String[] CAMPAIGN_TABLE_COLUMNS = {ID_COLUMN, NAME, TYPE,TARGET_DATE, SYNC_STATUS, UPDATED_AT_COLUMN, EVENT_ID, FORMSUBMISSION_ID, CREATED_AT};

    //private static final String BASE_ENTITY_ID_INDEX = "CREATE INDEX " + CAMPAIGN_TABLE_NAME + "_" + BASE_ENTITY_ID + "_index ON " + CAMPAIGN_TABLE_NAME + "(" + BASE_ENTITY_ID + " COLLATE NOCASE);";
    //private s/tatic final String UPDATED_AT_INDEX = "CREATE INDEX " + CAMPAIGN_TABLE_NAME + "_" + UPDATED_AT_COLUMN + "_index ON " + CAMPAIGN_TABLE_NAME + "(" + UPDATED_AT_COLUMN + ");";
    public CampaignRepository(Repository repository) {
        super(repository);
    }
    public static void createTable(SQLiteDatabase database) {
        database.execSQL(CAMPAIGN_SQL);
    }

    public ArrayList<CampaignForm> getAllCampaign() {
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
    }

    public long saveData(CampaignForm form) {
        SQLiteDatabase database = getWritableDatabase();
        return database.insert(CAMPAIGN_TABLE_NAME,null,createFormValues(form));
    }

    private ContentValues createFormValues(CampaignForm object) {
        ContentValues values = new ContentValues();

        values.put(NAME, object.getName());
        values.put(TYPE, object.getType());
        values.put(BASE_ENTITY_ID,object.getBaseEntityId());
        values.put(TARGET_DATE, object.getTargetDate());
        values.put(CREATED_AT, object.getCreatedDate().toString());
        values.put(UPDATED_AT_COLUMN, object.getUpdatedDate().toString());
        values.put(SYNC_STATUS, "true");
        return values;
    }
}
