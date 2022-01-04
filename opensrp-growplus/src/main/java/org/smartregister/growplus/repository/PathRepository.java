package org.smartregister.growplus.repository;

import android.content.Context;
import android.util.Log;

import net.sqlcipher.database.SQLiteDatabase;

import org.apache.commons.lang3.StringUtils;
import org.smartregister.commonregistry.CommonFtsObject;
import org.smartregister.domain.db.Column;
import org.smartregister.growthmonitoring.repository.HeightRepository;
import org.smartregister.growthmonitoring.repository.HeightZScoreRepository;
import org.smartregister.growthmonitoring.repository.MUACRepository;
import org.smartregister.growthmonitoring.repository.WeightRepository;
import org.smartregister.growthmonitoring.repository.ZScoreRepository;
import org.smartregister.immunization.repository.RecurringServiceRecordRepository;
import org.smartregister.immunization.repository.RecurringServiceTypeRepository;
import org.smartregister.immunization.repository.VaccineNameRepository;
import org.smartregister.immunization.repository.VaccineRepository;
import org.smartregister.immunization.repository.VaccineTypeRepository;
import org.smartregister.immunization.util.IMDatabaseUtils;
import org.smartregister.growplus.application.VaccinatorApplication;
import org.smartregister.repository.AlertRepository;
import org.smartregister.repository.EventClientRepository;
import org.smartregister.repository.Repository;
import org.smartregister.util.Utils;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import util.PathConstants;

public class PathRepository extends Repository {

    private static final String TAG = PathRepository.class.getCanonicalName();
    private SQLiteDatabase readableDatabase;
    private SQLiteDatabase writableDatabase;
    private final Context context;

    public PathRepository(Context context, org.smartregister.Context opensrpContext) {
        super(context, PathConstants.DATABASE_NAME, PathConstants.DATABASE_VERSION, opensrpContext.session(), VaccinatorApplication.createCommonFtsObject(), opensrpContext.sharedRepositoriesArray());
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        super.onCreate(database);
        EventClientRepository.createTable(database, EventClientRepository.Table.client, EventClientRepository.client_column.values());
        EventClientRepository.createTable(database, EventClientRepository.Table.address, EventClientRepository.address_column.values());
        EventClientRepository.createTable(database, EventClientRepository.Table.event, EventClientRepository.event_column.values());
        EventClientRepository.createTable(database, EventClientRepository.Table.obs, EventClientRepository.obs_column.values());
        UniqueIdRepository.createTable(database);
        WeightRepository.createTable(database);
        VaccineRepository.createTable(database);
        CounsellingRepository.createTable(database);
        HeightRepository.createTable(database);
        MUACRepository.createTable(database);
        onUpgrade(database, 1, PathConstants.DATABASE_VERSION);

    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(PathRepository.class.getName(),
                "Upgrading database from version " + oldVersion + " to "
                        + newVersion + ", which will destroy all old data");

        int upgradeTo = oldVersion + 1;
        while (upgradeTo <= newVersion) {
            switch (upgradeTo) {
                case 2:
                    upgradeToVersion2(db);
                    break;
                case 3:
                    upgradeToVersion3(db);
                    break;
                case 4:
                    upgradeToVersion4(db);
                    break;
                case 5:
                    upgradeToVersion5(db);
                    break;
                case 6:
                    upgradeToVersion6(db);
                    break;
                case 7:
                    upgradeToVersion7Stock(db);
                    upgradeToVersion7Hia2(db);
                    break;
                case 8:
                    upgradeToVersion8RecurringServiceUpdate(db);
                    upgradeToVersion8ReportDeceased(db);
                    break;
                case 9:
                    upgradeToVersion9(db);
                    break;
                case 10:
                    upgradeToVersion10(db);
                    break;
                case 11:
                    upgradeToVersion11Stock(db);
                    break;
                case 12:
                    upgradeToVersion12(db);
                    break;
                case 13:
                    upgradeToVersion13(db);
                    break;
                case 14:
                    upgradeToVersion14(db);
                    break;
                default:
                    break;
            }
            upgradeTo++;
        }
    }

    private void upgradeToVersion7Stock(SQLiteDatabase db) {
        try {
//            db.execSQL("DROP TABLE IF EXISTS  ");
//            StockRepository.createTable(db);
            VaccineNameRepository.createTable(db);
            VaccineTypeRepository.createTable(db);
        } catch (Exception e) {
            Log.e(TAG, "upgradeToVersion7Stock " + e.getMessage());
        }
    }

    @Override
    public SQLiteDatabase getReadableDatabase() {
        return getReadableDatabase(VaccinatorApplication.getInstance().getPassword());
    }

    @Override
    public SQLiteDatabase getWritableDatabase() {
        return getWritableDatabase(VaccinatorApplication.getInstance().getPassword());
    }

    @Override
    public synchronized SQLiteDatabase getReadableDatabase(String password) {
        try {
            if (readableDatabase == null || !readableDatabase.isOpen()) {
                if (readableDatabase != null) {
                    readableDatabase.close();
                }
                readableDatabase = super.getReadableDatabase(password);
            }
            return readableDatabase;
        } catch (Exception e) {
            Log.e(TAG, "Database Error. " + e.getMessage());
            return null;
        }

    }

    @Override
    public synchronized SQLiteDatabase getWritableDatabase(String password) {
        if (writableDatabase == null || !writableDatabase.isOpen()) {
            if (writableDatabase != null) {
                writableDatabase.close();
            }
            writableDatabase = super.getWritableDatabase(password);
        }
        return writableDatabase;
    }

    @Override
    public synchronized void close() {
        if (readableDatabase != null) {
            readableDatabase.close();
        }

        if (writableDatabase != null) {
            writableDatabase.close();
        }
        super.close();
    }

    /**
     * Version 2 added some columns to the ec_child table
     *
     * @param database
     */
    private void upgradeToVersion2(SQLiteDatabase database) {
        try {
            // Run insert query
            ArrayList<String> newlyAddedFields = new ArrayList<>();
            newlyAddedFields.add("BCG_2");
            newlyAddedFields.add("inactive");
            newlyAddedFields.add("lost_to_follow_up");

            addFieldsToFTSTable(database, PathConstants.CHILD_TABLE_NAME, newlyAddedFields);
        } catch (Exception e) {
            Log.e(TAG, "upgradeToVersion2 " + Log.getStackTraceString(e));
        }
    }

    private void upgradeToVersion3(SQLiteDatabase db) {
        try {
            db.execSQL(VaccineRepository.UPDATE_TABLE_ADD_EVENT_ID_COL);
            db.execSQL(VaccineRepository.EVENT_ID_INDEX);
            db.execSQL(WeightRepository.UPDATE_TABLE_ADD_EVENT_ID_COL);
            db.execSQL(WeightRepository.EVENT_ID_INDEX);
            db.execSQL(VaccineRepository.UPDATE_TABLE_ADD_FORMSUBMISSION_ID_COL);
            db.execSQL(VaccineRepository.FORMSUBMISSION_INDEX);
            db.execSQL(WeightRepository.UPDATE_TABLE_ADD_FORMSUBMISSION_ID_COL);
            db.execSQL(WeightRepository.FORMSUBMISSION_INDEX);
        } catch (Exception e) {
            Log.e(TAG, "upgradeToVersion3 " + Log.getStackTraceString(e));
        }
    }

    private void upgradeToVersion4(SQLiteDatabase db) {
        try {
            db.execSQL(AlertRepository.ALTER_ADD_OFFLINE_COLUMN);
            db.execSQL(AlertRepository.OFFLINE_INDEX);
        } catch (Exception e) {
            Log.e(TAG, "upgradeToVersion4" + Log.getStackTraceString(e));
        }
    }

    private void upgradeToVersion5(SQLiteDatabase db) {
        try {
            RecurringServiceTypeRepository.createTable(db);
            RecurringServiceRecordRepository.createTable(db);

            RecurringServiceTypeRepository recurringServiceTypeRepository = VaccinatorApplication.getInstance().recurringServiceTypeRepository();
            IMDatabaseUtils.populateRecurringServices(context, db, recurringServiceTypeRepository);
        } catch (Exception e) {
            Log.e(TAG, "upgradeToVersion5 " + Log.getStackTraceString(e));
        }
    }
    private void upgradeToVersion14(SQLiteDatabase db) {
        try {
            HeightZScoreRepository.createTable(db);
        } catch (Exception e) {
            Log.e(TAG, "upgradeToVersion6" + Log.getStackTraceString(e));
        }
    }
    private void upgradeToVersion6(SQLiteDatabase db) {
        try {
            ZScoreRepository.createTable(db);
            db.execSQL(WeightRepository.ALTER_ADD_Z_SCORE_COLUMN);
        } catch (Exception e) {
            Log.e(TAG, "upgradeToVersion6" + Log.getStackTraceString(e));
        }
    }

    private void upgradeToVersion7Hia2(SQLiteDatabase db) {
        try {
            db.execSQL(VaccineRepository.UPDATE_TABLE_ADD_OUT_OF_AREA_COL);
            db.execSQL(VaccineRepository.UPDATE_TABLE_ADD_OUT_OF_AREA_COL_INDEX);
            db.execSQL(WeightRepository.UPDATE_TABLE_ADD_OUT_OF_AREA_COL);
//            db.execSQL(WeightRepository.UPDATE_TABLE_ADD_OUT_OF_AREA_COL_INDEX);
            db.execSQL(WeightRepository.UPDATE_TABLE_ADD_OUT_OF_AREA_COL_INDEX);
//            DailyTalliesRepository.createTable(db);
//            MonthlyTalliesRepository.createTable(db);
            EventClientRepository.createTable(db, EventClientRepository.Table.path_reports, EventClientRepository.report_column.values());
//            HIA2IndicatorsRepository.createTable(db);
            db.execSQL(VaccineRepository.UPDATE_TABLE_ADD_HIA2_STATUS_COL);

            dumpHIA2IndicatorsCSV(db);
        } catch (Exception e) {
            Log.e(TAG, "upgradeToVersion7Hia2 " + e.getMessage());
        }
    }

    private void upgradeToVersion8RecurringServiceUpdate(SQLiteDatabase db) {
        try {
//            db.execSQL(MonthlyTalliesRepository.INDEX_UNIQUE);
            dumpHIA2IndicatorsCSV(db);

            // Recurring service json changed. update
            RecurringServiceTypeRepository recurringServiceTypeRepository = VaccinatorApplication.getInstance().recurringServiceTypeRepository();
            IMDatabaseUtils.populateRecurringServices(context, db, recurringServiceTypeRepository);

        } catch (Exception e) {
            Log.e(TAG, "upgradeToVersion8RecurringServiceUpdate " + Log.getStackTraceString(e));
        }
    }

    private void upgradeToVersion8ReportDeceased(SQLiteDatabase database) {
        try {

//            String ALTER_ADD_DEATHDATE_COLUMN = "ALTER TABLE " + PathConstants.CHILD_TABLE_NAME + " ADD COLUMN " + PathConstants.EC_CHILD_TABLE.DOD + " VARCHAR";
//            database.execSQL(ALTER_ADD_DEATHDATE_COLUMN);

            ArrayList<String> newlyAddedFields = new ArrayList<>();
            newlyAddedFields.add(PathConstants.EC_CHILD_TABLE.DOD);

            addFieldsToFTSTable(database, PathConstants.CHILD_TABLE_NAME, newlyAddedFields);
        } catch (Exception e) {
            Log.e(TAG, "upgradeToVersion8ReportDeceased " + e.getMessage());
        }
    }

    private void upgradeToVersion9(SQLiteDatabase database) {
        try {
//            String ALTER_EVENT_TABLE_VALIDATE_COLUMN = "ALTER TABLE " + EventClientRepository.Table.event + " ADD COLUMN " + EventClientRepository.event_column.validationStatus + " VARCHAR";
//            database.execSQL(ALTER_EVENT_TABLE_VALIDATE_COLUMN);

            String ALTER_CLIENT_TABLE_VALIDATE_COLUMN = "ALTER TABLE " + EventClientRepository.Table.client + " ADD COLUMN " + EventClientRepository.client_column.validationStatus + " VARCHAR";
            database.execSQL(ALTER_CLIENT_TABLE_VALIDATE_COLUMN);

            String ALTER_REPORT_TABLE_VALIDATE_COLUMN = "ALTER TABLE " + EventClientRepository.Table.path_reports + " ADD COLUMN " + EventClientRepository.report_column.validationStatus + " VARCHAR";
            database.execSQL(ALTER_REPORT_TABLE_VALIDATE_COLUMN);

            EventClientRepository.createIndex(database, EventClientRepository.Table.event, EventClientRepository.event_column.values());
            EventClientRepository.createIndex(database, EventClientRepository.Table.client, EventClientRepository.client_column.values());
            EventClientRepository.createIndex(database, EventClientRepository.Table.path_reports, EventClientRepository.report_column.values());
            EventClientRepository.createIndex(database, EventClientRepository.Table.address, EventClientRepository.address_column.values());
            EventClientRepository.createIndex(database, EventClientRepository.Table.obs, EventClientRepository.obs_column.values());

        } catch (Exception e) {
            Log.e(TAG, "upgradeToVersion9 " + e.getMessage());
        }
    }

    private void upgradeToVersion10(SQLiteDatabase database) {
        try {

//            CohortRepository.createTable(database);
//            CohortIndicatorRepository.createTable(database);
//            CohortPatientRepository.createTable(database);
//
//            CumulativeRepository.createTable(database);
//            CumulativeIndicatorRepository.createTable(database);
//            CumulativePatientRepository.createTable(database);

            dumpHIA2IndicatorsCSV(database);

        } catch (Exception e) {
            Log.e(TAG, "upgradeToVersion10 " + e.getMessage());
        }
    }

    private void upgradeToVersion11Stock(SQLiteDatabase db) {
        try {
            db.execSQL("DROP TABLE IF EXISTS " + VaccineTypeRepository.VACCINE_Types_TABLE_NAME);
//            StockTypeRepository.createTable(db);
//            StockUtils.populateStockTypesFromAssets(context, StockLibrary.getInstance().getStockTypeRepository(), db);
//            StockRepository.migrateFromOldStockRepository(db, "Stocks");

        } catch (Exception e) {
            Log.e(TAG, "upgradeToVersion11Stock " + e.getMessage());
        }
    }

    private void upgradeToVersion12(SQLiteDatabase db) {
        try {
            Column[] columns = {EventClientRepository.event_column.formSubmissionId};
            EventClientRepository.createIndex(db, EventClientRepository.Table.event, columns);

            db.execSQL(WeightRepository.ALTER_ADD_CREATED_AT_COLUMN);
            WeightRepository.migrateCreatedAt(db);

            db.execSQL(VaccineRepository.ALTER_ADD_CREATED_AT_COLUMN);
            VaccineRepository.migrateCreatedAt(db);

            db.execSQL(RecurringServiceRecordRepository.ALTER_ADD_CREATED_AT_COLUMN);
            RecurringServiceRecordRepository.migrateCreatedAt(db);

        } catch (Exception e) {
            Log.e(TAG, "upgradeToVersion12 " + e.getMessage());
        }
    }

    private void upgradeToVersion13(SQLiteDatabase db){
        try {
            db.execSQL(VaccineRepository.UPDATE_TABLE_ADD_TEAM_ID_COL);
            db.execSQL(VaccineRepository.UPDATE_TABLE_ADD_TEAM_COL);
//            db.execSQL(WeightRepository.ALTER_ADD_Z_SCORE_COLUMN);
            db.execSQL(WeightRepository.UPDATE_TABLE_ADD_TEAM_COL);
            db.execSQL(WeightRepository.UPDATE_TABLE_ADD_TEAM_ID_COL);
            db.execSQL(WeightRepository.UPDATE_TABLE_ADD_CHILD_LOCATION_ID_COL);
            db.execSQL(VaccineRepository.UPDATE_TABLE_ADD_CHILD_LOCATION_ID_COL);
            db.execSQL(RecurringServiceRecordRepository.UPDATE_TABLE_ADD_CHILD_LOCATION_ID_COL);

            WeightRepository.migrateCreatedAt(db);

            db.execSQL(RecurringServiceRecordRepository.UPDATE_TABLE_ADD_TEAM_ID_COL);
            db.execSQL(RecurringServiceRecordRepository.UPDATE_TABLE_ADD_TEAM_COL);
        } catch (Exception e) {
            Log.e(TAG, "upgradeToVersion13 " + e.getMessage());
        }
    }

    private void addFieldsToFTSTable(SQLiteDatabase database, String originalTableName, List<String> newlyAddedFields) {

        // Create the new ec_child table

        String newTableNameSuffix = "_v2";

        Set<String> searchColumns = new LinkedHashSet<>();
        searchColumns.add(CommonFtsObject.idColumn);
        searchColumns.add(CommonFtsObject.relationalIdColumn);
        searchColumns.add(CommonFtsObject.phraseColumn);
        searchColumns.add(CommonFtsObject.isClosedColumn);

        String[] mainConditions = this.commonFtsObject.getMainConditions(originalTableName);
        if (mainConditions != null)
            for (String mainCondition : mainConditions) {
                if (!mainCondition.equals(CommonFtsObject.isClosedColumnName))
                    searchColumns.add(mainCondition);
            }

        String[] sortFields = this.commonFtsObject.getSortFields(originalTableName);
        if (sortFields != null) {
            for (String sortValue : sortFields) {
                if (sortValue.startsWith("alerts.")) {
                    sortValue = sortValue.split("\\.")[1];
                }
                searchColumns.add(sortValue);
            }
        }

        String joinedSearchColumns = StringUtils.join(searchColumns, ",");

        String searchSql = "create virtual table "
                + CommonFtsObject.searchTableName(originalTableName) + newTableNameSuffix
                + " using fts4 (" + joinedSearchColumns + ");";
        Log.d(TAG, "Create query is\n---------------------------\n" + searchSql);

        database.execSQL(searchSql);

        ArrayList<String> oldFields = new ArrayList<>();

        for (String curColumn : searchColumns) {
            curColumn = curColumn.trim();
            if (curColumn.contains(" ")) {
                String[] curColumnParts = curColumn.split(" ");
                curColumn = curColumnParts[0];
            }

            if (!newlyAddedFields.contains(curColumn)) {
                oldFields.add(curColumn);
            } else {
                Log.d(TAG, "Skipping field " + curColumn + " from the select query");
            }
        }

        String insertQuery = "insert into "
                + CommonFtsObject.searchTableName(originalTableName) + newTableNameSuffix
                + " (" + StringUtils.join(oldFields, ", ") + ")"
                + " select " + StringUtils.join(oldFields, ", ") + " from "
                + CommonFtsObject.searchTableName(originalTableName);

        Log.d(TAG, "Insert query is\n---------------------------\n" + insertQuery);
        database.execSQL(insertQuery);

        // Run the drop query
        String dropQuery = "drop table " + CommonFtsObject.searchTableName(originalTableName);
        Log.d(TAG, "Drop query is\n---------------------------\n" + dropQuery);
        database.execSQL(dropQuery);

        // Run rename query
        String renameQuery = "alter table "
                + CommonFtsObject.searchTableName(originalTableName) + newTableNameSuffix
                + " rename to " + CommonFtsObject.searchTableName(originalTableName);
        Log.d(TAG, "Rename query is\n---------------------------\n" + renameQuery);
        database.execSQL(renameQuery);

    }

    private void dumpHIA2IndicatorsCSV(SQLiteDatabase db) {
//        List<Map<String, String>> csvData = Utils.populateTableFromCSV(
//                context,
//                HIA2IndicatorsRepository.INDICATORS_CSV_FILE,
//                HIA2IndicatorsRepository.CSV_COLUMN_MAPPING);
//        HIA2IndicatorsRepository hIA2IndicatorsRepository = VaccinatorApplication.getInstance()
//                .hIA2IndicatorsRepository();
//        hIA2IndicatorsRepository.save(db, csvData);
    }

}
