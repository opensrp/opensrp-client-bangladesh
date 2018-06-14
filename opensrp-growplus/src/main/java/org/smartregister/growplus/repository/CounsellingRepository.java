package org.smartregister.growplus.repository;

import android.content.ContentValues;
import android.database.Cursor;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import net.sqlcipher.database.SQLiteDatabase;

import org.apache.commons.lang3.StringUtils;
import org.ei.drishti.dto.AlertStatus;
import org.smartregister.commonregistry.CommonFtsObject;
import org.smartregister.domain.Alert;
import org.smartregister.growplus.domain.Counselling;
import org.smartregister.immunization.ImmunizationLibrary;
import org.smartregister.immunization.domain.Vaccine;
import org.smartregister.immunization.repository.VaccineRepository;
import org.smartregister.repository.BaseRepository;
import org.smartregister.repository.EventClientRepository;
import org.smartregister.repository.Repository;
import org.smartregister.service.AlertService;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class CounsellingRepository extends BaseRepository {
    public static DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private static final String TAG = CounsellingRepository.class.getCanonicalName();
    private static final String VACCINE_SQL = "CREATE TABLE counselling (_id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,base_entity_id VARCHAR NOT NULL,name VARCHAR NOT NULL,date DATETIME NOT NULL,anmid VARCHAR NULL,location_id VARCHAR NULL,event_id VARCHAR NULL,formSubmissionId VARCHAR,sync_status VARCHAR,updated_at INTEGER NULL,formfields VARCHAR,created_at DATETIME NOT NULL)";
    public static final String COUNSELLING_TABLE_NAME = "counselling";
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

    public static final String[] COUNSELLING_TABLE_COLUMNS = {ID_COLUMN, BASE_ENTITY_ID, NAME, DATE, ANMID, LOCATIONID, SYNC_STATUS, UPDATED_AT_COLUMN, EVENT_ID, FORMSUBMISSION_ID, CREATED_AT,FORMFIELDS};

    private static final String BASE_ENTITY_ID_INDEX = "CREATE INDEX " + COUNSELLING_TABLE_NAME + "_" + BASE_ENTITY_ID + "_index ON " + COUNSELLING_TABLE_NAME + "(" + BASE_ENTITY_ID + " COLLATE NOCASE);";
    private static final String UPDATED_AT_INDEX = "CREATE INDEX " + COUNSELLING_TABLE_NAME + "_" + UPDATED_AT_COLUMN + "_index ON " + COUNSELLING_TABLE_NAME + "(" + UPDATED_AT_COLUMN + ");";
//
//    public static final String UPDATE_TABLE_ADD_EVENT_ID_COL = "ALTER TABLE " + COUNSELLING_TABLE_NAME + " ADD COLUMN " + EVENT_ID + " VARCHAR;";
//    public static final String EVENT_ID_INDEX = "CREATE INDEX " + COUNSELLING_TABLE_NAME + "_" + EVENT_ID + "_index ON " + COUNSELLING_TABLE_NAME + "(" + EVENT_ID + " COLLATE NOCASE);";
//
//    public static final String UPDATE_TABLE_ADD_FORMSUBMISSION_ID_COL = "ALTER TABLE " + COUNSELLING_TABLE_NAME + " ADD COLUMN " + FORMSUBMISSION_ID + " VARCHAR;";
//    public static final String FORMSUBMISSION_INDEX = "CREATE INDEX " + COUNSELLING_TABLE_NAME + "_" + FORMSUBMISSION_ID + "_index ON " + COUNSELLING_TABLE_NAME + "(" + FORMSUBMISSION_ID + " COLLATE NOCASE);";
//


    private CommonFtsObject commonFtsObject;
    private AlertService alertService;

    public CounsellingRepository(Repository repository, CommonFtsObject commonFtsObject, AlertService alertService) {
        super(repository);
        this.commonFtsObject = commonFtsObject;
        this.alertService = alertService;
    }

    public static void createTable(SQLiteDatabase database) {
        database.execSQL(VACCINE_SQL);
        database.execSQL(BASE_ENTITY_ID_INDEX);
        database.execSQL(UPDATED_AT_INDEX);
    }

    public void add(Counselling counselling) {
        if (counselling == null) {
            return;
        }

        try {


            if (StringUtils.isBlank(counselling.getSyncStatus())) {
                counselling.setSyncStatus(TYPE_Unsynced);
            }
            if (StringUtils.isBlank(counselling.getFormSubmissionId())) {
                counselling.setFormSubmissionId(generateRandomUUIDString());
            }

            if (counselling.getUpdatedAt() == null) {
                counselling.setUpdatedAt(Calendar.getInstance().getTimeInMillis());
            }

            SQLiteDatabase database = getWritableDatabase();
            if (counselling.getId() == null) {
                Counselling sameCounselling = findUnique(database, counselling);
                if (sameCounselling != null) {
                    counselling.setUpdatedAt(sameCounselling.getUpdatedAt());
                    counselling.setId(sameCounselling.getId());
                    update(database, counselling);
                } else {
                    if(counselling.getCreatedAt() == null){
                        counselling.setCreatedAt(new Date());
                    }
                    Long id = database.insert(COUNSELLING_TABLE_NAME, null, createValuesFor(counselling));
                    counselling.setId(id);
                }
            } else {
                //mark the vaccine as unsynced for processing as an updated event
                counselling.setSyncStatus(TYPE_Unsynced);
                update(database, counselling);
            }
        } catch (Exception e) {
            Log.e(TAG, Log.getStackTraceString(e));
        }
        updateFtsSearch(counselling);
    }

    public void update(SQLiteDatabase database, Counselling counselling) {
        if (counselling == null || counselling.getId() == null) {
            return;
        }

        try {
            String idSelection = ID_COLUMN + " = ?";
            database.update(COUNSELLING_TABLE_NAME, createValuesFor(counselling), idSelection, new String[]{counselling.getId().toString()});
        } catch (Exception e) {
            Log.e(TAG, Log.getStackTraceString(e));
        }
    }

    public List<Counselling> findUnSyncedBeforeTime(int hours) {
        List<Counselling> counsellings = new ArrayList<Counselling>();
        Cursor cursor = null;
        try {
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.HOUR_OF_DAY, -hours);

            Long time = calendar.getTimeInMillis();

            cursor = getReadableDatabase().query(COUNSELLING_TABLE_NAME, COUNSELLING_TABLE_COLUMNS, UPDATED_AT_COLUMN + " < ? AND " + SYNC_STATUS + " = ? ", new String[]{time.toString(), TYPE_Unsynced}, null, null, null, null);
            counsellings = readAllCounsellings(cursor);
        } catch (Exception e) {
            Log.e(TAG, Log.getStackTraceString(e));
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return counsellings;
    }


    public List<Counselling> findByEntityId(String entityId) {
        SQLiteDatabase database = getReadableDatabase();
        Cursor cursor = database.query(COUNSELLING_TABLE_NAME, COUNSELLING_TABLE_COLUMNS, BASE_ENTITY_ID + " = ? " + COLLATE_NOCASE + " ORDER BY " + UPDATED_AT_COLUMN, new String[]{entityId}, null, null, null, null);
        return readAllCounsellings(cursor);
    }

    public Counselling find(Long caseId) {
        Counselling counselling = null;
        Cursor cursor = null;
        try {
            cursor = getReadableDatabase().query(COUNSELLING_TABLE_NAME, COUNSELLING_TABLE_COLUMNS, ID_COLUMN + " = ?", new String[]{caseId.toString()}, null, null, null, null);
            List<Counselling> vaccines = readAllCounsellings(cursor);
            if (!vaccines.isEmpty()) {
                counselling = vaccines.get(0);
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return counselling;
    }

    public Counselling findUnique(SQLiteDatabase database, Counselling counselling) {
        if (counselling == null || (StringUtils.isBlank(counselling.getFormSubmissionId()) && StringUtils.isBlank(counselling.getEventId()))) {
            return null;
        }

        try {
            if (database == null) {
                database = getReadableDatabase();
            }

            String selection = null;
            String[] selectionArgs = null;
            if (StringUtils.isNotBlank(counselling.getFormSubmissionId()) && StringUtils.isNotBlank(counselling.getEventId())) {
                selection = FORMSUBMISSION_ID + " = ? " + COLLATE_NOCASE + " OR " + EVENT_ID + " = ? " + COLLATE_NOCASE;
                selectionArgs = new String[]{counselling.getFormSubmissionId(), counselling.getEventId()};
            } else if (StringUtils.isNotBlank(counselling.getEventId())) {
                selection = EVENT_ID + " = ? " + COLLATE_NOCASE;
                selectionArgs = new String[]{counselling.getEventId()};
            } else if (StringUtils.isNotBlank(counselling.getFormSubmissionId())) {
                selection = FORMSUBMISSION_ID + " = ? " + COLLATE_NOCASE;
                selectionArgs = new String[]{counselling.getFormSubmissionId()};
            }

            Cursor cursor = database.query(COUNSELLING_TABLE_NAME, COUNSELLING_TABLE_COLUMNS, selection, selectionArgs, null, null, ID_COLUMN + " DESC ", null);
            List<Counselling> counsellings = readAllCounsellings(cursor);
            if (!counsellings.isEmpty()) {
                return counsellings.get(0);
            }
        } catch (Exception e) {
            Log.e(TAG, Log.getStackTraceString(e));
        }
        return null;

    }



    public void deleteCounselling(Long caseId) {
        try {
            Counselling counselling = find(caseId);
            if (counselling != null) {
                getWritableDatabase().delete(COUNSELLING_TABLE_NAME, ID_COLUMN + "= ?", new String[]{caseId.toString()});

                updateFtsSearch(counselling.getBaseEntityId(), counselling.getName());
            }
        } catch (Exception e) {
            Log.e(TAG, Log.getStackTraceString(e));
        }
    }

    public void close(Long caseId) {
        try {
            ContentValues values = new ContentValues();
            values.put(SYNC_STATUS, TYPE_Synced);
            getWritableDatabase().update(COUNSELLING_TABLE_NAME, values, ID_COLUMN + " = ?", new String[]{caseId.toString()});
        } catch (Exception e) {
            Log.e(TAG, Log.getStackTraceString(e));
        }
    }


    private List<Counselling> readAllCounsellings(Cursor cursor) {
        List<Counselling> counsellings = new ArrayList<Counselling>();

        try {

            if (cursor != null && cursor.getCount() > 0 && cursor.moveToFirst()) {
                while (!cursor.isAfterLast()) {
                    String vaccineName = cursor.getString(cursor.getColumnIndex(NAME));
                    if (vaccineName != null) {
                        vaccineName = removeHyphen(vaccineName);
                    }

                    Date createdAt = null;
                    String dateCreatedString = cursor.getString(cursor.getColumnIndex(CREATED_AT));
                    if (StringUtils.isNotBlank(dateCreatedString)) {
                        try {
                            createdAt = dateFormat.parse(dateCreatedString);
                        } catch (ParseException e) {
                            Log.e(TAG, Log.getStackTraceString(e));
                        }
                    }
                    Counselling counselling = new Counselling(cursor.getLong(cursor.getColumnIndex(ID_COLUMN)),
                            cursor.getString(cursor.getColumnIndex(BASE_ENTITY_ID)),
                            vaccineName,
                            new Date(cursor.getLong(cursor.getColumnIndex(DATE))),
                            cursor.getString(cursor.getColumnIndex(ANMID)),
                            cursor.getString(cursor.getColumnIndex(LOCATIONID)),
                            cursor.getString(cursor.getColumnIndex(SYNC_STATUS)),
                            cursor.getLong(cursor.getColumnIndex(UPDATED_AT_COLUMN)),
                            cursor.getString(cursor.getColumnIndex(EVENT_ID)),
                            cursor.getString(cursor.getColumnIndex(FORMSUBMISSION_ID)),
                            createdAt
                    );
                    int columnindex = cursor.getColumnIndex(FORMFIELDS);
                    counselling.setFormfields(new Gson().<Map<String, String>>fromJson(cursor.getString(cursor.getColumnIndex(FORMFIELDS)),
                            new TypeToken<Map<String, String>>() {
                            }.getType()));
                    counsellings.add(counselling);

                    cursor.moveToNext();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            cursor.close();
        }
        return counsellings;
    }


    private ContentValues createValuesFor(Counselling counselling) {
        ContentValues values = new ContentValues();
        values.put(ID_COLUMN, counselling.getId());
        values.put(BASE_ENTITY_ID, counselling.getBaseEntityId());
        values.put(NAME, counselling.getName() != null ? addHyphen(counselling.getName().toLowerCase()) : null);
        values.put(DATE, counselling.getDate() != null ? counselling.getDate().getTime() : null);
        values.put(ANMID, counselling.getAnmId());
        values.put(LOCATIONID, counselling.getLocationId());
        values.put(SYNC_STATUS, counselling.getSyncStatus());
        values.put(UPDATED_AT_COLUMN, counselling.getUpdatedAt() != null ? counselling.getUpdatedAt() : null);
        values.put(EVENT_ID, counselling.getEventId() != null ? counselling.getEventId() : null);
        values.put(FORMSUBMISSION_ID, counselling.getFormSubmissionId() != null ? counselling.getFormSubmissionId() : null);
        values.put(CREATED_AT, counselling.getCreatedAt() != null ? dateFormat.format(counselling.getCreatedAt()) : null);
        values.put(FORMFIELDS,new Gson().toJson(counselling.getFormfields()));
        return values;
    }

    //-----------------------
    // FTS methods
    public void updateFtsSearch(Counselling counselling) {
        try {
            if (commonFtsObject != null && alertService() != null) {
                String entityId = counselling.getBaseEntityId();
                String vaccineName = counselling.getName();
                if (vaccineName != null) {
                    vaccineName = removeHyphen(vaccineName);
                }
                String scheduleName = commonFtsObject.getAlertScheduleName(vaccineName);

                String bindType = commonFtsObject.getAlertBindType(scheduleName);

                if (StringUtils.isNotBlank(bindType) && StringUtils.isNotBlank(scheduleName) && StringUtils.isNotBlank(entityId)) {
                    String field = addHyphen(scheduleName);
                    // update vaccine status
                    alertService().updateFtsSearchInACR(bindType, entityId, field, AlertStatus.complete.value());
                }
            }
        } catch (Exception e) {
            Log.e(TAG, Log.getStackTraceString(e));
        }

    }

    public void updateFtsSearch(String entityId, String vaccineName_) {
        String vaccineName = vaccineName_;
        try {
            if (commonFtsObject != null && alertService() != null) {
                if (vaccineName != null) {
                    vaccineName = removeHyphen(vaccineName);
                }

                String scheduleName = commonFtsObject.getAlertScheduleName(vaccineName);
                if (StringUtils.isNotBlank(entityId) && StringUtils.isNotBlank(scheduleName)) {
                    Alert alert = alertService().findByEntityIdAndScheduleName(entityId, scheduleName);
                    alertService().updateFtsSearch(alert, true);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, Log.getStackTraceString(e));
        }
    }

    public AlertService alertService() {
        if (alertService == null) {
            alertService = ImmunizationLibrary.getInstance().context().alertService();
        }
        return alertService;
    }

    public static String addHyphen(String s) {
        if (StringUtils.isNotBlank(s)) {
            return s.replace(" ", "_");
        }
        return s;
    }

    public static String removeHyphen(String s) {
        if (StringUtils.isNotBlank(s)) {
            return s.replace("_", " ");
        }
        return s;
    }

    public static void migrateCreatedAt(SQLiteDatabase database) {
        try {
            String sql = "UPDATE " + COUNSELLING_TABLE_NAME +
                    " SET " + CREATED_AT + " = " +
                    " ( SELECT " + EventClientRepository.event_column.dateCreated.name() +
                    "   FROM " + EventClientRepository.Table.event.name() +
                    "   WHERE " + EventClientRepository.event_column.eventId.name() + " = " + COUNSELLING_TABLE_NAME + "." + EVENT_ID +
                    "   OR " + EventClientRepository.event_column.formSubmissionId.name() + " = " + COUNSELLING_TABLE_NAME + "." + FORMSUBMISSION_ID +
                    " ) " +
                    " WHERE " + CREATED_AT + " is null ";
            database.execSQL(sql);
        } catch (Exception e) {
            Log.e(TAG, Log.getStackTraceString(e));
        }
    }
}
