package org.smartregister.cbhc.repository;

import android.content.ContentValues;
import android.database.Cursor;
import android.util.Log;

import net.sqlcipher.database.SQLiteDatabase;

import org.apache.commons.lang3.StringUtils;
import org.opensrp.api.constants.Gender;
import org.smartregister.cbhc.domain.MUAC;
import org.smartregister.cbhc.util.Utils;
import org.smartregister.growthmonitoring.GrowthMonitoringLibrary;
import org.smartregister.location.helper.LocationHelper;
import org.smartregister.repository.AllSharedPreferences;
import org.smartregister.repository.BaseRepository;
import org.smartregister.repository.EventClientRepository;
import org.smartregister.repository.Repository;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class MuacRepository extends BaseRepository {
    public static final String MUAC_TABLE_NAME = "muacs";
    public static final String ID_COLUMN = "_id";
    public static final String BASE_ENTITY_ID = "base_entity_id";
    public static final String EVENT_ID = "event_id";
    public static final String PROGRAM_CLIENT_ID = "program_client_id";// ID to be used to identify entity when base_entity_id is unavailable
    public static final String FORMSUBMISSION_ID = "formSubmissionId";
    public static final String OUT_OF_AREA = "out_of_area";
    public static final String KG = "kg";
    public static final String DATE = "date";
    public static final String ANMID = "anmid";
    public static final String LOCATIONID = "location_id";
    public static final String CHILD_LOCATION_ID = "child_location_id";
    public static final String SYNC_STATUS = "sync_status";
    public static final String UPDATED_AT_COLUMN = "updated_at";
    public static final String Z_SCORE = "z_score";
    public static final double DEFAULT_Z_SCORE = 999999d;
    public static final String CREATED_AT = "created_at";
    public static final String TEAM_ID = "team_id";
    public static final String TEAM = "team";
    public static final String[] MUAC_TABLE_COLUMNS = {
            ID_COLUMN, BASE_ENTITY_ID, PROGRAM_CLIENT_ID, KG, DATE, ANMID, LOCATIONID, CHILD_LOCATION_ID, TEAM, TEAM_ID,
            SYNC_STATUS, UPDATED_AT_COLUMN, EVENT_ID, FORMSUBMISSION_ID, Z_SCORE, OUT_OF_AREA, CREATED_AT};
    public static final String UPDATE_TABLE_ADD_EVENT_ID_COL = "ALTER TABLE " + MUAC_TABLE_NAME + " ADD COLUMN " + EVENT_ID + " VARCHAR;";
    public static final String EVENT_ID_INDEX = "CREATE INDEX " + MUAC_TABLE_NAME + "_" + EVENT_ID + "_index ON " + MUAC_TABLE_NAME + "(" + EVENT_ID + " COLLATE NOCASE);";
    public static final String UPDATE_TABLE_ADD_FORMSUBMISSION_ID_COL = "ALTER TABLE " + MUAC_TABLE_NAME + " ADD COLUMN " + FORMSUBMISSION_ID + " VARCHAR;";
    public static final String FORMSUBMISSION_INDEX = "CREATE INDEX " + MUAC_TABLE_NAME + "_" + FORMSUBMISSION_ID + "_index ON " + MUAC_TABLE_NAME + "(" + FORMSUBMISSION_ID + " COLLATE NOCASE);";
    public static final String UPDATE_TABLE_ADD_OUT_OF_AREA_COL = "ALTER TABLE " + MUAC_TABLE_NAME + " ADD COLUMN " + OUT_OF_AREA + " VARCHAR;";
    public static final String UPDATE_TABLE_ADD_OUT_OF_AREA_COL_INDEX = "CREATE INDEX " + MUAC_TABLE_NAME + "_" + OUT_OF_AREA + "_index ON " + MUAC_TABLE_NAME + "(" + OUT_OF_AREA + " COLLATE NOCASE);";
    public static final String ALTER_ADD_Z_SCORE_COLUMN = "ALTER TABLE " + MUAC_TABLE_NAME + " ADD COLUMN " + Z_SCORE + " REAL NOT NULL DEFAULT " + DEFAULT_Z_SCORE;
    public static final String ALTER_ADD_CREATED_AT_COLUMN = "ALTER TABLE " + MUAC_TABLE_NAME + " ADD COLUMN " + CREATED_AT + " DATETIME NULL ";
    public static final String UPDATE_TABLE_ADD_TEAM_COL = "ALTER TABLE " + MUAC_TABLE_NAME + " ADD COLUMN " + TEAM + " VARCHAR;";
    public static final String UPDATE_TABLE_ADD_TEAM_ID_COL = "ALTER TABLE " + MUAC_TABLE_NAME + " ADD COLUMN " + TEAM_ID + " VARCHAR;";
    public static final String UPDATE_TABLE_ADD_CHILD_LOCATION_ID_COL = "ALTER TABLE " + MUAC_TABLE_NAME + " ADD COLUMN " + CHILD_LOCATION_ID + " VARCHAR;";
    private static final String TAG = MuacRepository.class.getCanonicalName();
    private static final String MUAC_SQL = "CREATE TABLE muacs (_id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT," +
            "base_entity_id VARCHAR NOT NULL,program_client_id VARCHAR NULL,kg REAL NOT NULL,date DATETIME NOT NULL,anmid " +
            "VARCHAR NULL,location_id VARCHAR NULL,sync_status VARCHAR,updated_at INTEGER NULL)";
    private static final String BASE_ENTITY_ID_INDEX = "CREATE INDEX " + MUAC_TABLE_NAME + "_" + BASE_ENTITY_ID + "_index ON " + MUAC_TABLE_NAME + "(" + BASE_ENTITY_ID + " COLLATE NOCASE);";
    private static final String SYNC_STATUS_INDEX = "CREATE INDEX " + MUAC_TABLE_NAME + "_" + SYNC_STATUS + "_index ON " + MUAC_TABLE_NAME + "(" + SYNC_STATUS + " COLLATE NOCASE);";
    private static final String UPDATED_AT_INDEX = "CREATE INDEX " + MUAC_TABLE_NAME + "_" + UPDATED_AT_COLUMN + "_index ON " + MUAC_TABLE_NAME + "(" + UPDATED_AT_COLUMN + ");";


    public MuacRepository(Repository repository) {
        super(repository);
    }

    public static void createTable(SQLiteDatabase database) {
        database.execSQL(MUAC_SQL);
        database.execSQL(BASE_ENTITY_ID_INDEX);
        database.execSQL(SYNC_STATUS_INDEX);
        database.execSQL(UPDATED_AT_INDEX);
    }

    public static void migrateCreatedAt(SQLiteDatabase database) {
        try {
            String sql = "UPDATE " + MUAC_TABLE_NAME +
                    " SET " + CREATED_AT + " = " +
                    " ( SELECT " + EventClientRepository.event_column.dateCreated.name() +
                    "   FROM " + EventClientRepository.Table.event.name() +
                    "   WHERE " + EventClientRepository.event_column.eventId
                    .name() + " = " + MUAC_TABLE_NAME + "." + EVENT_ID +
                    "   OR " + EventClientRepository.event_column.formSubmissionId
                    .name() + " = " + MUAC_TABLE_NAME + "." + FORMSUBMISSION_ID +
                    " ) " +
                    " WHERE " + CREATED_AT + " is null ";
            database.execSQL(sql);
        } catch (Exception e) {
            Utils.appendLog(MuacRepository.class.getName(), e);
            Log.e(TAG, Log.getStackTraceString(e));
        }
    }

    /**
     * This method sets the muac's z-score, before adding it to the database
     *
     * @param dateOfBirth
     * @param gender
     * @param muac
     */
    public void add(Date dateOfBirth, Gender gender, MUAC muac) {

    }

    public void add(MUAC muac) {
        try {
            if (muac == null) {
                return;
            }

            AllSharedPreferences allSharedPreferences = GrowthMonitoringLibrary.getInstance().context()
                    .allSharedPreferences();
            String providerId = allSharedPreferences.fetchRegisteredANM();
            muac.setTeam(allSharedPreferences.fetchDefaultTeam(providerId));
            muac.setTeamId(allSharedPreferences.fetchDefaultTeamId(providerId));
            muac.setLocationId(allSharedPreferences.fetchDefaultLocalityId(providerId));
            muac.setChildLocationId(LocationHelper.getInstance().getChildLocationId());


            if (StringUtils.isBlank(muac.getSyncStatus())) {
                muac.setSyncStatus(TYPE_Unsynced);
            }
            if (StringUtils.isBlank(muac.getFormSubmissionId())) {
                muac.setFormSubmissionId(generateRandomUUIDString());
            }


            if (muac.getUpdatedAt() == null) {
                muac.setUpdatedAt(Calendar.getInstance().getTimeInMillis());
            }

            SQLiteDatabase database = getRepository().getWritableDatabase();
            if (muac.getId() == null) {
                MUAC sameMuac = findUnique(database, muac);
                if (sameMuac != null) {
                    muac.setUpdatedAt(sameMuac.getUpdatedAt());
                    muac.setId(sameMuac.getId());
                    update(database, muac);
                } else {
                    if (muac.getCreatedAt() == null) {
                        muac.setCreatedAt(new Date());
                    }
                    muac.setId(database.insert(MUAC_TABLE_NAME, null, createValuesFor(muac)));
                }
            } else {
                muac.setSyncStatus(TYPE_Unsynced);
                update(database, muac);
            }
        } catch (Exception e) {
            Utils.appendLog(MuacRepository.class.getName(), e);
            Log.e(TAG, Log.getStackTraceString(e));
        }
    }

    public MUAC findUnique(SQLiteDatabase db, MUAC muac) {

        if (muac == null || (StringUtils.isBlank(muac.getFormSubmissionId()) && StringUtils
                .isBlank(muac.getEventId()))) {
            return null;
        }

        try {
            SQLiteDatabase database = db;
            if (database == null) {
                database = getRepository().getReadableDatabase();
            }

            String selection = null;
            String[] selectionArgs = null;
            if (StringUtils.isNotBlank(muac.getFormSubmissionId()) && StringUtils.isNotBlank(muac.getEventId())) {
                selection = FORMSUBMISSION_ID + " = ? " + COLLATE_NOCASE + " OR " + EVENT_ID + " = ? " + COLLATE_NOCASE;
                selectionArgs = new String[]{muac.getFormSubmissionId(), muac.getEventId()};
            } else if (StringUtils.isNotBlank(muac.getEventId())) {
                selection = EVENT_ID + " = ? " + COLLATE_NOCASE;
                selectionArgs = new String[]{muac.getEventId()};
            } else if (StringUtils.isNotBlank(muac.getFormSubmissionId())) {
                selection = FORMSUBMISSION_ID + " = ? " + COLLATE_NOCASE;
                selectionArgs = new String[]{muac.getFormSubmissionId()};
            }

            Cursor cursor = database.query(MUAC_TABLE_NAME, MUAC_TABLE_COLUMNS, selection, selectionArgs, null, null,
                    ID_COLUMN + " DESC ", null);
            List<MUAC> muacList = readAllMuacs(cursor);
            if (!muacList.isEmpty()) {
                return muacList.get(0);
            }
        } catch (Exception e) {
            Utils.appendLog(MuacRepository.class.getName(), e);
            Log.e(TAG, Log.getStackTraceString(e));
        }

        return null;
    }

    public void update(SQLiteDatabase database, MUAC muac) {
        if (muac == null || muac.getId() == null) {
            return;
        }

        try {
            SQLiteDatabase db;
            if (database == null) {
                db = getRepository().getWritableDatabase();
            } else {
                db = database;
            }

            String idSelection = ID_COLUMN + " = ?";
            db.update(MUAC_TABLE_NAME, createValuesFor(muac), idSelection, new String[]{muac.getId().toString()});
        } catch (Exception e) {
            Utils.appendLog(MuacRepository.class.getName(), e);
            Log.e(TAG, Log.getStackTraceString(e));
        }
    }

    private ContentValues createValuesFor(MUAC muac) {
        ContentValues values = new ContentValues();
        values.put(ID_COLUMN, muac.getId());
        values.put(BASE_ENTITY_ID, muac.getBaseEntityId());
        values.put(PROGRAM_CLIENT_ID, muac.getProgramClientId());
        values.put(KG, muac.getKg());
        values.put(DATE, muac.getDate() != null ? muac.getDate().getTime() : null);
        values.put(ANMID, muac.getAnmId());
        values.put(LOCATIONID, muac.getLocationId());
        values.put(CHILD_LOCATION_ID, muac.getChildLocationId());
        values.put(TEAM, muac.getTeam());
        values.put(TEAM_ID, muac.getTeamId());
        values.put(SYNC_STATUS, muac.getSyncStatus());
        values.put(UPDATED_AT_COLUMN, muac.getUpdatedAt());
        values.put(EVENT_ID, muac.getEventId());
        values.put(FORMSUBMISSION_ID, muac.getFormSubmissionId());
        values.put(OUT_OF_AREA, muac.getOutOfCatchment());
        values.put(Z_SCORE, muac.getZScore() == null ? DEFAULT_Z_SCORE : muac.getZScore());
        values.put(CREATED_AT,
                muac.getCreatedAt() != null ? EventClientRepository.dateFormat.format(muac.getCreatedAt()) : null);
        return values;
    }

    private List<MUAC> readAllMuacs(Cursor cursor) {
        List<MUAC> muacs = new ArrayList<>();
        try {
            if (cursor != null && cursor.getCount() > 0 && cursor.moveToFirst()) {
                while (!cursor.isAfterLast()) {
                    Double zScore = cursor.getDouble(cursor.getColumnIndex(Z_SCORE));
                    if (zScore != null && zScore.equals(new Double(DEFAULT_Z_SCORE))) {
                        zScore = null;
                    }

                    Date createdAt = null;
                    String dateCreatedString = cursor.getString(cursor.getColumnIndex(CREATED_AT));
                    if (StringUtils.isNotBlank(dateCreatedString)) {
                        try {
                            createdAt = EventClientRepository.dateFormat.parse(dateCreatedString);
                        } catch (ParseException e) {
                            Utils.appendLog(MuacRepository.class.getName(), e);
                            Log.e(TAG, Log.getStackTraceString(e));
                        }
                    }

                    MUAC muac = new MUAC(cursor.getLong(cursor.getColumnIndex(ID_COLUMN)),
                            cursor.getString(cursor.getColumnIndex(BASE_ENTITY_ID)),
                            cursor.getString(cursor.getColumnIndex(PROGRAM_CLIENT_ID)),
                            cursor.getFloat(cursor.getColumnIndex(KG)),
                            new Date(cursor.getLong(cursor.getColumnIndex(DATE))),
                            cursor.getString(cursor.getColumnIndex(ANMID)),
                            cursor.getString(cursor.getColumnIndex(LOCATIONID)),
                            cursor.getString(cursor.getColumnIndex(SYNC_STATUS)),
                            cursor.getLong(cursor.getColumnIndex(UPDATED_AT_COLUMN)),
                            cursor.getString(cursor.getColumnIndex(EVENT_ID)),
                            cursor.getString(cursor.getColumnIndex(FORMSUBMISSION_ID)),
                            zScore,
                            cursor.getInt(cursor.getColumnIndex(OUT_OF_AREA)),
                            createdAt);

                    muac.setTeam(cursor.getString(cursor.getColumnIndex(TEAM)));
                    muac.setTeamId(cursor.getString(cursor.getColumnIndex(TEAM_ID)));
                    muac.setChildLocationId(cursor.getString(cursor.getColumnIndex(CHILD_LOCATION_ID)));

                    muacs.add(muac);

                    cursor.moveToNext();
                }
            }
        } catch (Exception e) {
            Utils.appendLog(MuacRepository.class.getName(), e);
            Log.e(TAG, e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return muacs;

    }

    public List<MUAC> findUnSyncedBeforeTime(int hours) {
        List<MUAC> muacs = new ArrayList<>();
        Cursor cursor = null;
        try {

            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.HOUR_OF_DAY, -hours);

            Long time = calendar.getTimeInMillis();

            cursor = getRepository().getReadableDatabase().query(MUAC_TABLE_NAME, MUAC_TABLE_COLUMNS,
                    UPDATED_AT_COLUMN + " < ? " + COLLATE_NOCASE + " AND " + SYNC_STATUS + " = ? " + COLLATE_NOCASE,
                    new String[]{time.toString(), TYPE_Unsynced}, null, null, null, null);
            muacs = readAllMuacs(cursor);
        } catch (Exception e) {
            Utils.appendLog(MuacRepository.class.getName(), e);
            Log.e(TAG, Log.getStackTraceString(e));
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return muacs;
    }

    public MUAC findUnSyncedByEntityId(String entityId) {
        MUAC muac = null;
        Cursor cursor = null;
        try {

            cursor = getRepository().getReadableDatabase().query(MUAC_TABLE_NAME, MUAC_TABLE_COLUMNS,
                    BASE_ENTITY_ID + " = ? " + COLLATE_NOCASE + " AND " + SYNC_STATUS + " = ? ",
                    new String[]{entityId, TYPE_Unsynced}, null, null, UPDATED_AT_COLUMN + COLLATE_NOCASE + " DESC", null);
            List<MUAC> muacs = readAllMuacs(cursor);
            if (!muacs.isEmpty()) {
                muac = muacs.get(0);
            }
        } catch (Exception e) {
            Utils.appendLog(MuacRepository.class.getName(), e);
            Log.e(TAG, Log.getStackTraceString(e));
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return muac;
    }

    public List<MUAC> findByEntityId(String entityId) {
        List<MUAC> muacs = null;
        Cursor cursor = null;
        try {
            cursor = getRepository().getReadableDatabase()
                    .query(MUAC_TABLE_NAME, MUAC_TABLE_COLUMNS, BASE_ENTITY_ID + " = ? " + COLLATE_NOCASE,
                            new String[]{entityId}, null, null, null, null);
            muacs = readAllMuacs(cursor);
        } catch (Exception e) {
            Utils.appendLog(MuacRepository.class.getName(), e);
            Log.e(TAG, Log.getStackTraceString(e));
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return muacs;
    }

    public List<MUAC> findWithNoZScore() {
        List<MUAC> result = new ArrayList<>();
        Cursor cursor = null;
        try {
            cursor = getRepository().getReadableDatabase().query(MUAC_TABLE_NAME,
                    MUAC_TABLE_COLUMNS, Z_SCORE + " = " + DEFAULT_Z_SCORE, null, null, null, null, null);
            result = readAllMuacs(cursor);
        } catch (Exception e) {
            Utils.appendLog(MuacRepository.class.getName(), e);
            Log.e(TAG, Log.getStackTraceString(e));
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return result;
    }

    public MUAC find(Long caseId) {
        MUAC muac = null;
        Cursor cursor = null;
        try {
            cursor = getRepository().getReadableDatabase()
                    .query(MUAC_TABLE_NAME, MUAC_TABLE_COLUMNS, ID_COLUMN + " = ?", new String[]{caseId.toString()},
                            null, null, null, null);
            List<MUAC> muacs = readAllMuacs(cursor);
            if (!muacs.isEmpty()) {
                muac = muacs.get(0);
            }
        } catch (Exception e) {
            Utils.appendLog(MuacRepository.class.getName(), e);
            Log.e(TAG, Log.getStackTraceString(e));
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return muac;
    }

    public List<MUAC> findLast5(String entityid) {
        List<MUAC> muacList = new ArrayList<>();
        Cursor cursor = null;
        try {
            cursor = getRepository().getReadableDatabase()
                    .query(MUAC_TABLE_NAME, MUAC_TABLE_COLUMNS, BASE_ENTITY_ID + " = ? " + COLLATE_NOCASE,
                            new String[]{entityid}, null, null, UPDATED_AT_COLUMN + COLLATE_NOCASE + " DESC", null);
            muacList = readAllMuacs(cursor);
        } catch (Exception e) {
            Utils.appendLog(MuacRepository.class.getName(), e);
            Log.e(TAG, Log.getStackTraceString(e));
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return muacList;
    }

    public void delete(String id) {
        try {
            getRepository().getWritableDatabase()
                    .delete(MUAC_TABLE_NAME, ID_COLUMN + " = ? " + COLLATE_NOCASE + " AND " + SYNC_STATUS + " = ? ",
                            new String[]{id, TYPE_Unsynced});
        } catch (Exception e) {
            Utils.appendLog(MuacRepository.class.getName(), e);
            Log.e(TAG, Log.getStackTraceString(e));
        }
    }

    public void close(Long caseId) {
        try {
            ContentValues values = new ContentValues();
            values.put(SYNC_STATUS, TYPE_Synced);
            getRepository().getWritableDatabase()
                    .update(MUAC_TABLE_NAME, values, ID_COLUMN + " = ?", new String[]{caseId.toString()});
        } catch (Exception e) {
            Utils.appendLog(MuacRepository.class.getName(), e);
            Log.e(TAG, Log.getStackTraceString(e));
        }
    }
}
