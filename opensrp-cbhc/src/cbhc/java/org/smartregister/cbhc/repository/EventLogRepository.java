package org.smartregister.cbhc.repository;

import android.content.ContentValues;
import net.sqlcipher.Cursor;
import net.sqlcipher.database.SQLiteDatabase;
import net.sqlcipher.database.SQLiteException;

import org.json.JSONObject;
import org.smartregister.repository.BaseRepository;
import org.smartregister.repository.Repository;
import java.util.ArrayList;

public class EventLogRepository extends BaseRepository {

    public static final String EVENT_LOG_TABLE_NAME = "ec_event_log";
    public static final String EVENT_ID = "event_id";
    public static final String BASE_ENTITY_ID = "base_entity_id";
    public static final String FAMILY_ID = "family_id";
    public static final String EVENT_DATE = "event_date";
    public static final String EVENT_TYPE = "event_type";
    public static final String FORM_JSON = "form_json";
    public static final String CLIENT_JSON = "client_json";
    public static final String EVENT_JSON = "event_json";

    public static final String[] TABLE_COLUMNS = {EVENT_ID,FAMILY_ID, BASE_ENTITY_ID, EVENT_DATE,EVENT_TYPE,FORM_JSON};
    private static final String EVENT_LOG_SQL = "CREATE TABLE " +
            "ec_event_log (event_id VARCHAR,event_type VARCHAR," +
            "base_entity_id VARCHAR NOT NULL,family_id VARCHAR NOT NULL," +
            "event_date VARCHAR,form_json TEXT)";

    public EventLogRepository(Repository repository) {
        super(repository);
    }
    public JSONObject getFormJson(String baseEntity_id, String []encounterType){
        String sql = "select form_json from ec_event_log where" +
                " ec_event_log.base_entity_id = '"+baseEntity_id+"'"
                ;
        if(encounterType.length>1){
            sql = sql + " and (";
            for(int i=0;i<encounterType.length;i++){
                sql = sql + " ec_event_log.event_type = '"+encounterType[i]+"' or ";
            }
            sql = sql .substring(0,sql.length()-3)+")";

        }else{
            sql = sql + " and ec_event_log.event_type = '"+encounterType+"'";
        }
        try{
            Cursor cursor = getWritableDatabase().rawQuery(sql,new String[]{});
            if (null != cursor && cursor.getCount() > 0) {
                cursor.moveToFirst();
                String formJson = cursor.getString(0);
                return new JSONObject(formJson);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }
    public static void createTable(SQLiteDatabase database) {
        try{
            database.execSQL(EVENT_LOG_SQL);
        }catch (SQLiteException e){

        }
    }

    public ArrayList<EventLog> getEvents(){
        String sql = "select client.baseEntityId as base_entity_id, client.json as client_json, event.json as event_json, event.eventId as event_id, " +
                "event.eventType as event_type, event.eventDate as event_date from client,event where event.baseEntityId = client.baseEntityId and " +
                "event.eventId not in (select ec_event_log.event_id from ec_event_log)";
        SQLiteDatabase database = getWritableDatabase();
        Cursor cursor = database.rawQuery(sql,null);
        ArrayList<EventLog> eventLogs = new ArrayList<>();

        try {
            if (cursor != null && cursor.getCount() > 0 && cursor.moveToFirst()) {
                while (!cursor.isAfterLast()) {
                    EventLog eventLog = new EventLog();
                    eventLog.setBaseEntityId(cursor.getString(cursor.getColumnIndex(BASE_ENTITY_ID)));
                    eventLog.setClientJson(cursor.getString(cursor.getColumnIndex(CLIENT_JSON)));
                    eventLog.setEventJson(cursor.getString(cursor.getColumnIndex(EVENT_JSON)));
                    eventLog.setEventId(cursor.getString(cursor.getColumnIndex(EVENT_ID)));
                    eventLog.setEventType(cursor.getString(cursor.getColumnIndex(EVENT_TYPE)));
                    eventLog.setEventDate(cursor.getString(cursor.getColumnIndex(EVENT_DATE)));

                    eventLogs.add(eventLog);
                    cursor.moveToNext();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return eventLogs;
    }
    public synchronized void add(EventLog eventLog) {
        if (eventLog == null) {
            return;
        }
        try {
            SQLiteDatabase database = getWritableDatabase();
//            database.beginTransaction();
//            Cursor c = database.rawQuery("select * from ec_event_log where ec_event_log.base_entity_id = '"+eventLog.getBaseEntityId()+"'",null);
//            if(c!=null&&c.getCount()>0){
//                //update
//                database.update(EVENT_LOG_TABLE_NAME,createValuesFor(eventLog),"ec_event_log.base_entity_id = ?",new String[]{eventLog.getBaseEntityId()});
//            }else{
                //insert
                long rowId = database.insert(EVENT_LOG_TABLE_NAME, null, createValuesFor(eventLog));
                System.out.println(rowId);
//            }
//            database.setTransactionSuccessful();
//            if(c!=null)c.close();
//            database.endTransaction();
//            if (eventLog.getBaseEntityId() != null && findUnique(database, eventLog) == null) {
//            }


        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    private ContentValues createValuesFor(EventLog eventLog) {
        ContentValues values = new ContentValues();
        values.put(EVENT_ID, eventLog.getEventId());
        values.put(EVENT_TYPE, eventLog.getEventType());
        values.put(BASE_ENTITY_ID, eventLog.getBaseEntityId());
        values.put(FAMILY_ID, "relationalId");
        values.put(EVENT_DATE, eventLog.getEventDate());
        values.put(FORM_JSON, eventLog.getFormJson());
        return values;
    }


}