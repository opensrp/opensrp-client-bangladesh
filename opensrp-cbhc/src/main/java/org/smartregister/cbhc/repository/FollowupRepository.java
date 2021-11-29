package org.smartregister.cbhc.repository;

import android.content.ContentValues;
import android.database.Cursor;

import net.sqlcipher.database.SQLiteDatabase;

import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.cbhc.application.AncApplication;
import org.smartregister.cbhc.domain.FollowupForm;
import org.smartregister.cbhc.domain.GuestMemberData;
import org.smartregister.cbhc.domain.draft_form_object;
import org.smartregister.commonregistry.CommonPersonObject;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.commonregistry.CommonRepository;
import org.smartregister.repository.BaseRepository;
import org.smartregister.repository.Repository;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

public class FollowupRepository extends BaseRepository {
    public static DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);

    private static final String TAG = FollowupRepository.class.getCanonicalName();
    private static final String FOLLOWUP_SQL = "CREATE TABLE followup (_id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,base_entity_id VARCHAR NOT NULL,name VARCHAR NOT NULL,date DATETIME NOT NULL,anmid VARCHAR NULL,location_id VARCHAR NULL,event_id VARCHAR NULL,formSubmissionId VARCHAR,sync_status VARCHAR,updated_at INTEGER NULL,formfields VARCHAR,created_at DATETIME NOT NULL)";
    public static final String FOLLOWUP_TABLE_NAME = "followup";
    public static final String ID_COLUMN = "_id";
    public static final String BASE_ENTITY_ID = "base_entity_id";
    public static final String EVENT_ID = "event_id";
    public static final String FORMSUBMISSION_ID = "formSubmissionId";
    public static final String NAME = "name";
    public static final String DATE = "date";
    public static final String ANMID = "anmid";
    public static final String LOCATIONID = "location_id";
    public static final String SYNC_STATUS = "sync_status";
    public static final String UPDATED_AT_COLUMN = "updated_at";
    public static final String FORMFIELDS = "formfields";
    public static final String CREATED_AT = "created_at";

    public static final String[] FOLLOWUP_TABLE_COLUMNS = {ID_COLUMN, BASE_ENTITY_ID, NAME, DATE, ANMID, LOCATIONID, SYNC_STATUS, UPDATED_AT_COLUMN, EVENT_ID, FORMSUBMISSION_ID, CREATED_AT,FORMFIELDS};

    private static final String BASE_ENTITY_ID_INDEX = "CREATE INDEX " + FOLLOWUP_TABLE_NAME + "_" + BASE_ENTITY_ID + "_index ON " + FOLLOWUP_TABLE_NAME + "(" + BASE_ENTITY_ID + " COLLATE NOCASE);";
    private static final String UPDATED_AT_INDEX = "CREATE INDEX " + FOLLOWUP_TABLE_NAME + "_" + UPDATED_AT_COLUMN + "_index ON " + FOLLOWUP_TABLE_NAME + "(" + UPDATED_AT_COLUMN + ");";
    public FollowupRepository(Repository repository) {
        super(repository);
    }
    private static ArrayList<String> formFieldList = new ArrayList<>();

    public static void createTable(SQLiteDatabase database) {
        database.execSQL(FOLLOWUP_SQL);
    }

    public void saveForm(FollowupForm form) {
        SQLiteDatabase database = getWritableDatabase();
        database.insert(FOLLOWUP_TABLE_NAME,null,createFormValues(form));
    }
    public static JSONObject getJsonObjectFromField(String baseEntityId,String eventType){
        formFieldList.clear();
        String query = "select * from followup where followup.base_entity_id = '"+baseEntityId+"' and followup.name = '"+eventType+"'";

        try (Cursor cursor = AncApplication.getInstance().getRepository().getReadableDatabase().rawQuery(query, new String[]{})) {
            if (cursor != null && cursor.getCount() > 0) {
                cursor.moveToFirst();
                while (!cursor.isAfterLast()) {

                    int formfields = cursor.getColumnIndex("formfields");
                    formFieldList.add( cursor.isNull(formfields) ? "" : cursor.getString(formfields));

                    cursor.moveToNext();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        JSONObject jsonObject=null;
        if(formFieldList.size()>0){
            try {
                jsonObject = new JSONObject(formFieldList.get(formFieldList.size()-1));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return jsonObject;
    }

    private ContentValues createFormValues(FollowupForm object) {
        ContentValues values = new ContentValues();

        values.put(BASE_ENTITY_ID, object.getBase_entity_id());
        values.put(NAME, object.getForm_name());
        values.put(DATE, object.getDate().toString());
        values.put(FORMFIELDS, object.getFormFields());
        values.put(CREATED_AT, object.getDate().toString());
        return values;
    }
}
