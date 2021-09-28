package org.smartregister.cbhc.notification;

import android.content.ContentValues;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.sqlcipher.Cursor;
import net.sqlcipher.database.SQLiteDatabase;

import org.joda.time.DateTime;
import org.smartregister.repository.BaseRepository;
import org.smartregister.repository.LocationRepository;
import org.smartregister.repository.Repository;
import org.smartregister.util.DateTimeTypeConverter;

import java.util.ArrayList;

public class NotificationRepository extends BaseRepository {


    protected static final String ID = "_id";

    public static final String NOTIFICATION_TABLE = "notification_table";
    public static final String NOTIFICATION_SENDDATE = "notification_senddate";
    public static final String NOTIFICATION_TITLE = "notification_title";
    public static final String NOTIFICATION_TIMESTAMP = "notification_timestamp";



    private static final String CREATE_NOTIFICATION_TABLE =
            "CREATE TABLE " + NOTIFICATION_TABLE + " (" +
                    ID + " INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT," +
                    NOTIFICATION_SENDDATE + " VARCHAR, " + NOTIFICATION_TITLE+ " VARCHAR, "+NOTIFICATION_TIMESTAMP+" VARCHAR ) ";




    public NotificationRepository(Repository repository) {
        super(repository);
    }

    protected String getLocationTableName() {
        return NOTIFICATION_TABLE;
    }

    public static void createTable(SQLiteDatabase database) {
        database.execSQL(CREATE_NOTIFICATION_TABLE);
    }
    public void dropTable(){
        getWritableDatabase().execSQL("delete from "+getLocationTableName());
    }

    public void addOrUpdate(NotificationDTO notification) {
            ContentValues contentValues = new ContentValues();
            contentValues.put(NOTIFICATION_SENDDATE, notification.getDate());
            contentValues.put(NOTIFICATION_TITLE, notification.getText());
            contentValues.put(NOTIFICATION_TIMESTAMP, notification.getTimestamp());
            long inserted = getWritableDatabase().insert(getLocationTableName(), null, contentValues);
            Log.v("TARGET_FETCH","inserterd:"+inserted);



    }

    public NotificationDTO getNotification() {
        Cursor cursor = null;
        NotificationDTO notification = new NotificationDTO();
        try {
            cursor = getReadableDatabase().rawQuery("SELECT * FROM " + getLocationTableName()+"", null);
            while (cursor.moveToNext()) {
                notification = readCursor(cursor);
            }
            cursor.close();
        } catch (Exception e) {
            Log.e(LocationRepository.class.getCanonicalName(), e.getMessage(), e);
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return notification;
    }

    public ArrayList<NotificationDTO> getAllNotification() {
        Cursor cursor = null;
        ArrayList<NotificationDTO> notificationArrayList = new ArrayList<>();
        try {
            cursor = getReadableDatabase().rawQuery("SELECT * FROM " + getLocationTableName()+" order by "+NOTIFICATION_TIMESTAMP+" desc", null);
            while (cursor.moveToNext()) {
                notificationArrayList.add(readCursor(cursor));
            }
            cursor.close();
        } catch (Exception e) {
            Log.e(LocationRepository.class.getCanonicalName(), e.getMessage(), e);
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return notificationArrayList;
    }
    protected NotificationDTO readCursor(Cursor cursor) {
        String notificationSendDate = cursor.getString(cursor.getColumnIndex(NOTIFICATION_SENDDATE));
        String notificationTitle = cursor.getString(cursor.getColumnIndex(NOTIFICATION_TITLE));
        String notificationTimestamp = cursor.getString(cursor.getColumnIndex(NOTIFICATION_TIMESTAMP));

        NotificationDTO notification = new NotificationDTO();
        notification.setDate(notificationSendDate+"");
        notification.setText(notificationTitle+"");
        notification.setTimestamp(Long.parseLong(notificationTimestamp));

        return notification;
    }

}
