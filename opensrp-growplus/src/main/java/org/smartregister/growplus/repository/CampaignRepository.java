package org.smartregister.growplus.repository;

import android.content.ContentValues;
import android.database.Cursor;

import net.sqlcipher.database.SQLiteDatabase;

import org.smartregister.growplus.domain.CampaignForm;
import org.smartregister.repository.BaseRepository;
import org.smartregister.repository.Repository;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

public class CampaignRepository extends BaseRepository {
    public static DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);

    private static final String CAMPAIGN_SQL = "CREATE TABLE campaign (_id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,base_entity_id VARCHAR NOT NULL,name VARCHAR NOT NULL,type VARCHAR NOT NULL,target_date DATETIME NOT NULL,sync_status VARCHAR,updated_at DATETIME NOT NULL,created_at DATETIME NOT NULL)";
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


    public CampaignRepository(Repository repository) {
        super(repository);
    }
    public static void createTable(SQLiteDatabase database) {
        database.execSQL(CAMPAIGN_SQL);
    }

    /**
     * getting all campaign data here
     * @return
     */
    public ArrayList<CampaignForm> getAllCampaign() {
        ArrayList<CampaignForm> campaignFormList = new ArrayList<>();
        try{

            SQLiteDatabase sqLiteDatabase = getWritableDatabase();
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy");
            Cursor cursor = sqLiteDatabase.rawQuery("select * from campaign order by updated_at DESC", null);
            if (cursor != null && cursor.getCount() > 0 && cursor.moveToFirst()) {
                while (!cursor.isAfterLast()) {
                    try {
                        campaignFormList.add(new CampaignForm(
                                cursor.getString(cursor.getColumnIndex(NAME)),
                                cursor.getString(cursor.getColumnIndex(TYPE)),
                                cursor.getString(cursor.getColumnIndex(BASE_ENTITY_ID)),
                                simpleDateFormat.parse(cursor.getString(cursor.getColumnIndex(TARGET_DATE))),
                                simpleDateFormat.parse(cursor.getString(cursor.getColumnIndex(UPDATED_AT_COLUMN))),
                                simpleDateFormat.parse(cursor.getString(cursor.getColumnIndex(CREATED_AT)))));
                    } catch (ParseException e) {
                    }
                    cursor.moveToNext();
                }
            }

        }catch (Exception e){}
        return campaignFormList;
    }

    /**
     * saving campaign data here
     * @param form
     * @return
     */
    public long saveData(CampaignForm form) {
        SQLiteDatabase database = getWritableDatabase();
        return database.insert(CAMPAIGN_TABLE_NAME,null,createFormValues(form));
    }

    /**
     * ContentValues to add specific data to sqlite db
     * @param object
     * @return
     */
    private ContentValues createFormValues(CampaignForm object) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

        ContentValues values = new ContentValues();

        values.put(NAME, object.getName());
        values.put(TYPE, object.getType());
        values.put(BASE_ENTITY_ID,object.getBaseEntityId());
        values.put(TARGET_DATE, dateFormat.format(object.getTargetDate()));
        values.put(CREATED_AT, dateFormat.format(object.getCreatedDate()));
        values.put(UPDATED_AT_COLUMN, dateFormat.format(object.getUpdatedDate()));
        values.put(SYNC_STATUS, "true");
        return values;
    }

    /**
     * ContentValues to update specific data to sqlite db
     * @param object
     * @return
     */
    private ContentValues updateFormValues(CampaignForm object) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

        ContentValues values = new ContentValues();

        values.put(NAME, object.getName());
        values.put(TYPE, object.getType());
        values.put(TARGET_DATE, dateFormat.format(object.getTargetDate()));
        values.put(UPDATED_AT_COLUMN, dateFormat.format(object.getUpdatedDate()));
        values.put(SYNC_STATUS, "true");
        return values;
    }

    /**
     * updating campaign data here
     * @return
     */
    public long updateData(CampaignForm campaignForm) {
        SQLiteDatabase database = getWritableDatabase();
        return database.update(CAMPAIGN_TABLE_NAME,updateFormValues(campaignForm),BASE_ENTITY_ID+" =?", new String[]{campaignForm.getBaseEntityId()});
    }
}
