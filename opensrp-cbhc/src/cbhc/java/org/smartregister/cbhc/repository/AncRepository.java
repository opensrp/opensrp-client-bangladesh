package org.smartregister.cbhc.repository;

import android.content.Context;
import android.util.Log;

import net.sqlcipher.database.SQLiteDatabase;

import org.smartregister.AllConstants;
import org.smartregister.cbhc.BuildConfig;
import org.smartregister.cbhc.application.AncApplication;
import org.smartregister.cbhc.util.Utils;
import org.smartregister.configurableviews.repository.ConfigurableViewsRepository;
import org.smartregister.domain.db.Column;
import org.smartregister.repository.EventClientRepository;
import org.smartregister.repository.Repository;


/**
 * Created by ndegwamartin on 09/04/2018.
 */

public class AncRepository extends Repository {

    private static final String TAG = AncRepository.class.getCanonicalName();
    protected SQLiteDatabase readableDatabase;
    protected SQLiteDatabase writableDatabase;

    private Context context;

    public AncRepository(Context context, org.smartregister.Context openSRPContext) {
        super(context, AllConstants.DATABASE_NAME, BuildConfig.DATABASE_VERSION, openSRPContext.session(), AncApplication.createCommonFtsObject(), openSRPContext.sharedRepositoriesArray());
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        super.onCreate(database);
        ConfigurableViewsRepository.createTable(database);
        EventLogRepository.createTable(database);
        EventClientRepository.createTable(database, EventClientRepository.Table.client, EventClientRepository.client_column.values());
        EventClientRepository.createTable(database, EventClientRepository.Table.event, EventClientRepository.event_column.values());
//        EventClientRepository.createTable(database, EventClientRepository.Table.path_reports, EventClientRepository.report_column.values());


        //////////////////DraftFormRepository////////////////
        DraftFormRepository.createTable(database);
        ////////////////////////////////////////////////////

        //////////////////FollowupRepository////////////////
        FollowupRepository.createTable(database);

        HealthIdRepository.createTable(database);
        UniqueIdRepository.createTable(database);

        //onUpgrade(database, 1, 2);

        onUpgrade(database, 1, 6);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(AncRepository.class.getName(),
                "Upgrading database from version " + oldVersion + " to "
                        + newVersion + ", which will destroy all old data");
//        if(oldVersion<newVersion&&newVersion==6){
//            upgradeToVersion6(db);
//        }
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
                    upgradeToVersion7(db);
                    break;
                default:
                    break;
            }
            upgradeTo++;
        }
    }



    @Override
    public SQLiteDatabase getReadableDatabase() {
        return getReadableDatabase(AncApplication.getInstance().getPassword());
    }

    @Override
    public SQLiteDatabase getWritableDatabase() {
        return getWritableDatabase(AncApplication.getInstance().getPassword());
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
            Utils.appendLog(getClass().getName(), e);
            Log.e(TAG, "Database Error. " + e.getMessage());
            return null;
        }

    }

    @Override
    public synchronized SQLiteDatabase getWritableDatabase(String password) {

        try {
            if (writableDatabase == null || !writableDatabase.isOpen()) {
                if (writableDatabase != null) {
                    writableDatabase.close();
                }
                writableDatabase = super.getWritableDatabase(password);
            }
            return writableDatabase;
        } catch (Exception e) {
            Utils.appendLog(getClass().getName(), e);
            Log.e(TAG, "Database Error. " + e.getMessage());
            return null;
        }
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

    private void upgradeToVersion2(SQLiteDatabase db) {
        try {


//            EventClientRepository.createTable(db, EventClientRepository.Table.path_reports, EventClientRepository.report_column.values());


        } catch (Exception e) {
            Utils.appendLog(getClass().getName(), e);
            Log.e(TAG, "upgradeToVersion2 " + Log.getStackTraceString(e));
        }

    }

    private void upgradeToVersion3(SQLiteDatabase db) {
        try {
            Column[] columns = {EventClientRepository.event_column.formSubmissionId};
            EventClientRepository.createIndex(db, EventClientRepository.Table.event, columns);

        } catch (Exception e) {
            Utils.appendLog(getClass().getName(), e);
            Log.e(TAG, "upgradeToVersion3 " + Log.getStackTraceString(e));
        }
        try {
            Column[] columns = {EventClientRepository.event_column.formSubmissionId};
            EventClientRepository.createIndex(db, EventClientRepository.Table.event, columns);


        } catch (Exception e) {
            Utils.appendLog(getClass().getName(), e);
            Log.e(TAG, "upgradeToVersion3 " + e.getMessage());
        }
    }

    private void upgradeToVersion4(SQLiteDatabase db) {


    }

    private void upgradeToVersion6(SQLiteDatabase db) {
        try {
            db.execSQL("ALTER TABLE ec_member ADD COLUMN dataApprovalStatus INTEGER DEFAULT 1");
            db.execSQL("ALTER TABLE ec_member ADD COLUMN dataApprovalComments VARCHAR DEFAULT 1");
            db.execSQL("ALTER TABLE ec_woman ADD COLUMN dataApprovalStatus INTEGER DEFAULT 1");
            db.execSQL("ALTER TABLE ec_woman ADD COLUMN dataApprovalComments VARCHAR DEFAULT 1");
            db.execSQL("ALTER TABLE ec_child ADD COLUMN dataApprovalStatus INTEGER DEFAULT 1");
            db.execSQL("ALTER TABLE ec_child ADD COLUMN dataApprovalComments VARCHAR DEFAULT 1");
            db.execSQL("ALTER TABLE ec_household ADD COLUMN dataApprovalStatus INTEGER DEFAULT 1");

        } catch (Exception e) {
            Utils.appendLog(getClass().getName(), e);

        }

    }
    private void upgradeToVersion7(SQLiteDatabase db) {
        try {
            db.execSQL("ALTER TABLE ec_household ADD COLUMN householdCode INTEGER DEFAULT 1");
            db.execSQL("ALTER TABLE ec_household ADD COLUMN phoneNumber VARCHAR");
            db.execSQL("ALTER TABLE ec_household ADD COLUMN postOfficePermanent VARCHAR");
            db.execSQL("ALTER TABLE ec_household ADD COLUMN postOfficePresent VARCHAR");
            db.execSQL("ALTER TABLE ec_household ADD COLUMN financial_status VARCHAR");
            db.execSQL("ALTER TABLE ec_household ADD COLUMN Monthly_Expenditure VARCHAR");
            db.execSQL("ALTER TABLE ec_household ADD COLUMN Nearby_Health_Facility VARCHAR");
            db.execSQL("ALTER TABLE ec_household ADD COLUMN Health_Facility_Distance VARCHAR");
            db.execSQL("ALTER TABLE ec_household ADD COLUMN info_provider_name VARCHAR");
            db.execSQL("ALTER TABLE ec_household ADD COLUMN permanentAddress VARCHAR");
            db.execSQL("ALTER TABLE ec_household ADD COLUMN village VARCHAR");
            db.execSQL("ALTER TABLE ec_household ADD COLUMN Health_Care_Center VARCHAR");
            db.execSQL("ALTER TABLE ec_household ADD COLUMN ADDRESS_LINE VARCHAR");
            db.execSQL("ALTER TABLE ec_household ADD COLUMN HIE_FACILITIES VARCHAR");
            db.execSQL("ALTER TABLE ec_household ADD COLUMN Clinic_Distance VARCHAR");
            db.execSQL("ALTER TABLE ec_household ADD COLUMN is_permanent_address VARCHAR");
            db.execSQL("ALTER TABLE ec_household ADD COLUMN member_count VARCHAR");
            db.execSQL("ALTER TABLE ec_household ADD COLUMN Community_Clinic VARCHAR");

            db.execSQL("ALTER TABLE ec_woman ADD COLUMN givenNameLocal VARCHAR");
            db.execSQL("ALTER TABLE ec_woman ADD COLUMN motherNameEnglish VARCHAR");
            db.execSQL("ALTER TABLE ec_woman ADD COLUMN motherNameBangla VARCHAR");
            db.execSQL("ALTER TABLE ec_woman ADD COLUMN fatherNameEnglish VARCHAR");
            db.execSQL("ALTER TABLE ec_woman ADD COLUMN fathernameBangla VARCHAR");
            db.execSQL("ALTER TABLE ec_woman ADD COLUMN Child_birth_weight VARCHAR");
            db.execSQL("ALTER TABLE ec_woman ADD COLUMN Birth_weight VARCHAR");
            db.execSQL("ALTER TABLE ec_woman ADD COLUMN use_chlorohexidin VARCHAR");
            db.execSQL("ALTER TABLE ec_woman ADD COLUMN MaritalStatus VARCHAR");
            db.execSQL("ALTER TABLE ec_woman ADD COLUMN spouseNameEnglish VARCHAR");
            db.execSQL("ALTER TABLE ec_woman ADD COLUMN spouseNameBangla VARCHAR");
            db.execSQL("ALTER TABLE ec_woman ADD COLUMN birthPlace VARCHAR");
            db.execSQL("ALTER TABLE ec_woman ADD COLUMN disable VARCHAR");
            db.execSQL("ALTER TABLE ec_woman ADD COLUMN Disability_Type VARCHAR");
            db.execSQL("ALTER TABLE ec_woman ADD COLUMN ethnicity VARCHAR");
            db.execSQL("ALTER TABLE ec_woman ADD COLUMN education VARCHAR");
            db.execSQL("ALTER TABLE ec_woman ADD COLUMN Occupation_Category VARCHAR");
            db.execSQL("ALTER TABLE ec_woman ADD COLUMN technical_professionals VARCHAR");
            db.execSQL("ALTER TABLE ec_woman ADD COLUMN labor_service VARCHAR");
            db.execSQL("ALTER TABLE ec_woman ADD COLUMN Unskilled_labor VARCHAR");
            db.execSQL("ALTER TABLE ec_woman ADD COLUMN blue_collar_service VARCHAR");
            db.execSQL("ALTER TABLE ec_woman ADD COLUMN Home_based_manufacturing VARCHAR");
            db.execSQL("ALTER TABLE ec_woman ADD COLUMN Business VARCHAR");
            db.execSQL("ALTER TABLE ec_woman ADD COLUMN Domestic_Servant VARCHAR");
            db.execSQL("ALTER TABLE ec_woman ADD COLUMN Religion VARCHAR");
            db.execSQL("ALTER TABLE ec_woman ADD COLUMN bloodgroup VARCHAR");
            db.execSQL("ALTER TABLE ec_woman ADD COLUMN has_disease VARCHAR");
            db.execSQL("ALTER TABLE ec_woman ADD COLUMN lmp_date VARCHAR");
            db.execSQL("ALTER TABLE ec_woman ADD COLUMN delivery_date VARCHAR");
            db.execSQL("ALTER TABLE ec_woman ADD COLUMN Disease_Below_2Month_Age VARCHAR");
            db.execSQL("ALTER TABLE ec_woman ADD COLUMN Disease_2Month_5Years VARCHAR");
            db.execSQL("ALTER TABLE ec_woman ADD COLUMN familyplanning VARCHAR");
            db.execSQL("ALTER TABLE ec_woman ADD COLUMN family_diseases_details VARCHAR");
            db.execSQL("ALTER TABLE ec_woman ADD COLUMN RiskyHabit VARCHAR");
            db.execSQL("ALTER TABLE ec_woman ADD COLUMN phoneNumber VARCHAR");
            db.execSQL("ALTER TABLE ec_woman ADD COLUMN Realtion_With_Household_Head VARCHAR");
            db.execSQL("ALTER TABLE ec_woman ADD COLUMN idtype VARCHAR");
            db.execSQL("ALTER TABLE ec_woman ADD COLUMN DiseaseType VARCHAR");
            db.execSQL("ALTER TABLE ec_woman ADD COLUMN Comm_Disease VARCHAR");
            db.execSQL("ALTER TABLE ec_woman ADD COLUMN NonComnta_Disease VARCHAR");

            db.execSQL("ALTER TABLE ec_child ADD COLUMN givenNameLocal VARCHAR");
            db.execSQL("ALTER TABLE ec_child ADD COLUMN motherNameEnglish VARCHAR");
            db.execSQL("ALTER TABLE ec_child ADD COLUMN motherNameBangla VARCHAR");
            db.execSQL("ALTER TABLE ec_child ADD COLUMN fatherNameEnglish VARCHAR");
            db.execSQL("ALTER TABLE ec_child ADD COLUMN fathernameBangla VARCHAR");
            db.execSQL("ALTER TABLE ec_child ADD COLUMN Child_birth_weight VARCHAR");
            db.execSQL("ALTER TABLE ec_child ADD COLUMN Birth_weight VARCHAR");
            db.execSQL("ALTER TABLE ec_child ADD COLUMN use_chlorohexidin VARCHAR");
            db.execSQL("ALTER TABLE ec_child ADD COLUMN MaritalStatus VARCHAR");
            db.execSQL("ALTER TABLE ec_child ADD COLUMN spouseNameEnglish VARCHAR");
            db.execSQL("ALTER TABLE ec_child ADD COLUMN spouseNameBangla VARCHAR");
            db.execSQL("ALTER TABLE ec_child ADD COLUMN birthPlace VARCHAR");
            db.execSQL("ALTER TABLE ec_child ADD COLUMN disable VARCHAR");
            db.execSQL("ALTER TABLE ec_child ADD COLUMN Disability_Type VARCHAR");
            db.execSQL("ALTER TABLE ec_child ADD COLUMN ethnicity VARCHAR");
            db.execSQL("ALTER TABLE ec_child ADD COLUMN education VARCHAR");
            db.execSQL("ALTER TABLE ec_child ADD COLUMN Occupation_Category VARCHAR");
            db.execSQL("ALTER TABLE ec_child ADD COLUMN technical_professionals VARCHAR");
            db.execSQL("ALTER TABLE ec_child ADD COLUMN labor_service VARCHAR");
            db.execSQL("ALTER TABLE ec_child ADD COLUMN Unskilled_labor VARCHAR");
            db.execSQL("ALTER TABLE ec_child ADD COLUMN blue_collar_service VARCHAR");
            db.execSQL("ALTER TABLE ec_child ADD COLUMN Home_based_manufacturing VARCHAR");
            db.execSQL("ALTER TABLE ec_child ADD COLUMN Business VARCHAR");
            db.execSQL("ALTER TABLE ec_child ADD COLUMN Domestic_Servant VARCHAR");
            db.execSQL("ALTER TABLE ec_child ADD COLUMN Religion VARCHAR");
            db.execSQL("ALTER TABLE ec_child ADD COLUMN bloodgroup VARCHAR");
            db.execSQL("ALTER TABLE ec_child ADD COLUMN has_disease VARCHAR");
            db.execSQL("ALTER TABLE ec_child ADD COLUMN lmp_date VARCHAR");
            db.execSQL("ALTER TABLE ec_child ADD COLUMN delivery_date VARCHAR");
            db.execSQL("ALTER TABLE ec_child ADD COLUMN Disease_Below_2Month_Age VARCHAR");
            db.execSQL("ALTER TABLE ec_child ADD COLUMN Disease_2Month_5Years VARCHAR");
            db.execSQL("ALTER TABLE ec_child ADD COLUMN familyplanning VARCHAR");
            db.execSQL("ALTER TABLE ec_child ADD COLUMN family_diseases_details VARCHAR");
            db.execSQL("ALTER TABLE ec_child ADD COLUMN RiskyHabit VARCHAR");
            db.execSQL("ALTER TABLE ec_child ADD COLUMN phoneNumber VARCHAR");
            db.execSQL("ALTER TABLE ec_child ADD COLUMN Realtion_With_Household_Head VARCHAR");
            db.execSQL("ALTER TABLE ec_child ADD COLUMN idtype VARCHAR");
            db.execSQL("ALTER TABLE ec_child ADD COLUMN DiseaseType VARCHAR");
            db.execSQL("ALTER TABLE ec_child ADD COLUMN Comm_Disease VARCHAR");
            db.execSQL("ALTER TABLE ec_child ADD COLUMN NonComnta_Disease VARCHAR");

            db.execSQL("ALTER TABLE ec_member ADD COLUMN givenNameLocal VARCHAR");
            db.execSQL("ALTER TABLE ec_member ADD COLUMN motherNameEnglish VARCHAR");
            db.execSQL("ALTER TABLE ec_member ADD COLUMN motherNameBangla VARCHAR");
            db.execSQL("ALTER TABLE ec_member ADD COLUMN fatherNameEnglish VARCHAR");
            db.execSQL("ALTER TABLE ec_member ADD COLUMN fathernameBangla VARCHAR");
            db.execSQL("ALTER TABLE ec_member ADD COLUMN Child_birth_weight VARCHAR");
            db.execSQL("ALTER TABLE ec_member ADD COLUMN Birth_weight VARCHAR");
            db.execSQL("ALTER TABLE ec_member ADD COLUMN use_chlorohexidin VARCHAR");
            db.execSQL("ALTER TABLE ec_member ADD COLUMN MaritalStatus VARCHAR");
            db.execSQL("ALTER TABLE ec_member ADD COLUMN spouseNameEnglish VARCHAR");
            db.execSQL("ALTER TABLE ec_member ADD COLUMN spouseNameBangla VARCHAR");
            db.execSQL("ALTER TABLE ec_member ADD COLUMN birthPlace VARCHAR");
            db.execSQL("ALTER TABLE ec_member ADD COLUMN disable VARCHAR");
            db.execSQL("ALTER TABLE ec_member ADD COLUMN Disability_Type VARCHAR");
            db.execSQL("ALTER TABLE ec_member ADD COLUMN ethnicity VARCHAR");
            db.execSQL("ALTER TABLE ec_member ADD COLUMN education VARCHAR");
            db.execSQL("ALTER TABLE ec_member ADD COLUMN Occupation_Category VARCHAR");
            db.execSQL("ALTER TABLE ec_member ADD COLUMN technical_professionals VARCHAR");
            db.execSQL("ALTER TABLE ec_member ADD COLUMN labor_service VARCHAR");
            db.execSQL("ALTER TABLE ec_member ADD COLUMN Unskilled_labor VARCHAR");
            db.execSQL("ALTER TABLE ec_member ADD COLUMN blue_collar_service VARCHAR");
            db.execSQL("ALTER TABLE ec_member ADD COLUMN Home_based_manufacturing VARCHAR");
            db.execSQL("ALTER TABLE ec_member ADD COLUMN Business VARCHAR");
            db.execSQL("ALTER TABLE ec_member ADD COLUMN Domestic_Servant VARCHAR");
            db.execSQL("ALTER TABLE ec_member ADD COLUMN Religion VARCHAR");
            db.execSQL("ALTER TABLE ec_member ADD COLUMN bloodgroup VARCHAR");
            db.execSQL("ALTER TABLE ec_member ADD COLUMN has_disease VARCHAR");
            db.execSQL("ALTER TABLE ec_member ADD COLUMN lmp_date VARCHAR");
            db.execSQL("ALTER TABLE ec_member ADD COLUMN delivery_date VARCHAR");
            db.execSQL("ALTER TABLE ec_member ADD COLUMN Disease_Below_2Month_Age VARCHAR");
            db.execSQL("ALTER TABLE ec_member ADD COLUMN Disease_2Month_5Years VARCHAR");
            db.execSQL("ALTER TABLE ec_member ADD COLUMN familyplanning VARCHAR");
            db.execSQL("ALTER TABLE ec_member ADD COLUMN family_diseases_details VARCHAR");
            db.execSQL("ALTER TABLE ec_member ADD COLUMN RiskyHabit VARCHAR");
            db.execSQL("ALTER TABLE ec_member ADD COLUMN phoneNumber VARCHAR");
            db.execSQL("ALTER TABLE ec_member ADD COLUMN Realtion_With_Household_Head VARCHAR");
            db.execSQL("ALTER TABLE ec_member ADD COLUMN idtype VARCHAR");
            db.execSQL("ALTER TABLE ec_member ADD COLUMN DiseaseType VARCHAR");
            db.execSQL("ALTER TABLE ec_member ADD COLUMN Comm_Disease VARCHAR");
            db.execSQL("ALTER TABLE ec_member ADD COLUMN NonComnta_Disease VARCHAR");

        } catch (Exception e) {
//            Utils.appendLog(getClass().getName(), e);
            e.printStackTrace();

        }

    }

    private void upgradeToVersion5(SQLiteDatabase db) {


    }
}

