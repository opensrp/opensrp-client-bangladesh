package org.smartregister.cbhc.repository;

import android.content.ContentValues;
import android.database.Cursor;
import android.util.Log;

import net.sqlcipher.database.SQLiteDatabase;

import org.apache.commons.lang3.StringUtils;
import org.ei.drishti.dto.AlertStatus;
import org.smartregister.commonregistry.CommonFtsObject;
import org.smartregister.domain.Alert;
import org.smartregister.immunization.ImmunizationLibrary;
import org.smartregister.immunization.domain.Vaccine;
import org.smartregister.repository.BaseRepository;
import org.smartregister.repository.EventClientRepository;
import org.smartregister.repository.Repository;
import org.smartregister.service.AlertService;
import org.smartregister.cbhc.domain.draft_form_object;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class DraftFormRepository extends BaseRepository {
    private static final String TAG = DraftFormRepository.class.getCanonicalName();
    private static final String DraftForm_SQL = "CREATE TABLE Draft_Form (_id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,household_base_entity_id VARCHAR NOT NULL,formName VARCHAR NOT NULL,draftformJson TEXT,date DATETIME NOT NULL,draft_status VARCHAR, updated_at INTEGER NULL)";
    public static final String DraftForm_TABLE_NAME = "Draft_Form";
    public static final String ID_COLUMN = "_id";
    public static final String household_BASE_ENTITY_ID = "household_base_entity_id";
    public static final String DraftFormJson = "draftformJson";
    public static final String FormNAME = "formName";
    public static final String DATE = "date";
    public static final String draft_STATUS = "draft_status";
    public static final String UPDATED_AT_COLUMN = "updated_at";

    public static String TYPE_draft_closed = "draft_closed";
    public static String TYPE_draft_open = "draft_open";

    public static final String[] DraftForm_TABLE_COLUMNS = {ID_COLUMN, household_BASE_ENTITY_ID, FormNAME,DraftFormJson, DATE, draft_STATUS, UPDATED_AT_COLUMN};


    public DraftFormRepository(Repository repository) {
        super(repository);
    }

    public static void createTable(SQLiteDatabase database) {
        database.execSQL(DraftForm_SQL);
    }

    public void add(draft_form_object draftFormObject) {
        if (draftFormObject == null) {
            return;
        }

        try {



            if (draftFormObject.getUPDATED_AT_COLUMN() == null) {
                draftFormObject.setUPDATED_AT_COLUMN(""+Calendar.getInstance().getTimeInMillis());
            }

            SQLiteDatabase database = getWritableDatabase();
            if (draftFormObject.getID_COLUMN() == null) {

                    if (draftFormObject.getDATE() == null) {
                        draftFormObject.setDATE(new Date().toString());
                    }
                    draftFormObject.setDraft_STATUS(TYPE_draft_open);
                    draftFormObject.setID_COLUMN(""+database.insert(DraftForm_TABLE_NAME, null, createValuesFor(draftFormObject)));
                }

        } catch (Exception e) {
            Log.e(TAG, Log.getStackTraceString(e));
        }

    }

    public List<draft_form_object> findUnusedDraftWithoutEntityID(int hours) {
        List<draft_form_object> draftFormObjects = new ArrayList<draft_form_object>();
        Cursor cursor = null;
        try {
            cursor = getReadableDatabase().query(DraftForm_TABLE_NAME, DraftForm_TABLE_COLUMNS, household_BASE_ENTITY_ID +" = ?  AND "+ draft_STATUS + " = ? ", new String[]{"",TYPE_draft_open}, null, null, null, null);
            draftFormObjects = readAllDraftForms(cursor);
        } catch (Exception e) {
            Log.e(TAG, Log.getStackTraceString(e));
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return draftFormObjects;
    }


    public List<draft_form_object> findByEntityId(String entityId) {
        SQLiteDatabase database = getReadableDatabase();
        Cursor cursor = database.query(DraftForm_TABLE_NAME, DraftForm_TABLE_COLUMNS, household_BASE_ENTITY_ID + " = ? " + COLLATE_NOCASE + " ORDER BY " + UPDATED_AT_COLUMN, new String[]{entityId}, null, null, null, null);
        return readAllDraftForms(cursor);
    }

    public draft_form_object find(String caseId) {
        draft_form_object draftFormObject = null;
        Cursor cursor = null;
        try {
            cursor = getReadableDatabase().query(DraftForm_TABLE_NAME, DraftForm_TABLE_COLUMNS, household_BASE_ENTITY_ID + " = ?", new String[]{caseId.toString()}, null, null, null, null);
            List<draft_form_object> draft_form_objects = readAllDraftForms(cursor);
            if (!draft_form_objects.isEmpty()) {
                draftFormObject = draft_form_objects.get(0);
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return draftFormObject;
    }

    public draft_form_object findById(String caseId) {
        draft_form_object draftFormObject = null;
        Cursor cursor = null;
        try {
            cursor = getReadableDatabase().query(DraftForm_TABLE_NAME, DraftForm_TABLE_COLUMNS, ID_COLUMN + " = ?", new String[]{caseId.toString()}, null, null, null, null);
            List<draft_form_object> draft_form_objects = readAllDraftForms(cursor);
            if (!draft_form_objects.isEmpty()) {
                draftFormObject = draft_form_objects.get(0);
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return draftFormObject;
    }

    public void deleteDraftForms(String caseId) {
        try {
            draft_form_object draftFormObject = findById(caseId);
            if (draftFormObject != null) {
                getWritableDatabase().delete(DraftForm_TABLE_NAME, ID_COLUMN + "= ?", new String[]{caseId.toString()});
            }
        } catch (Exception e) {
            Log.e(TAG, Log.getStackTraceString(e));
        }
    }

    public void close(String caseId) {
        try {
            ContentValues values = new ContentValues();
            values.put(draft_STATUS, TYPE_draft_closed);
            getWritableDatabase().update(DraftForm_TABLE_NAME, values, ID_COLUMN + " = ?", new String[]{caseId.toString()});
            deleteDraftForms(caseId);
        } catch (Exception e) {
            Log.e(TAG, Log.getStackTraceString(e));
        }
    }


    private List<draft_form_object> readAllDraftForms(Cursor cursor) {
        List<draft_form_object> draft_form_objects = new ArrayList<draft_form_object>();

        try {

            if (cursor != null && cursor.getCount() > 0 && cursor.moveToFirst()) {
                while (!cursor.isAfterLast()) {
                    int formjsoncolumnindex = cursor.getColumnIndex(DraftFormJson);
                    draft_form_object draftFormObject = new draft_form_object();
                    draftFormObject.setFormNAME(cursor.getString(cursor.getColumnIndex(FormNAME)));
                    draftFormObject.setDATE(cursor.getString(cursor.getColumnIndex(DATE)));
                    draftFormObject.setDraft_STATUS(cursor.getString(cursor.getColumnIndex(draft_STATUS)));
                    draftFormObject.setID_COLUMN(cursor.getString(cursor.getColumnIndex(ID_COLUMN)));
                    draftFormObject.setHousehold_BASE_ENTITY_ID(cursor.getString(cursor.getColumnIndex(household_BASE_ENTITY_ID)));
                    draftFormObject.setDraftFormJson(cursor.getString(cursor.getColumnIndex(DraftFormJson)));
                    draftFormObject.setUPDATED_AT_COLUMN(cursor.getString(cursor.getColumnIndex(UPDATED_AT_COLUMN)));
                    draft_form_objects.add(draftFormObject);
                    cursor.moveToNext();
                }
            }
        } catch (Exception e) {
            Log.e(TAG, Log.getStackTraceString(e));
        } finally {
            cursor.close();
        }
        return draft_form_objects;
    }


    private ContentValues createValuesFor(draft_form_object object) {
        ContentValues values = new ContentValues();
        values.put(ID_COLUMN, object.getID_COLUMN());
        values.put(household_BASE_ENTITY_ID, object.getHousehold_BASE_ENTITY_ID());
        values.put(FormNAME, object.getFormNAME());
        values.put(DATE, object.getDATE());
        values.put(draft_STATUS, object.getDraft_STATUS());
        values.put(UPDATED_AT_COLUMN, object.getUPDATED_AT_COLUMN());
        values.put(DraftFormJson,object.getDraftFormJson());
        return values;
    }




}
