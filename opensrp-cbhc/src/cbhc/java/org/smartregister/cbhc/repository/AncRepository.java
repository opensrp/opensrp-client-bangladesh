package org.smartregister.cbhc.repository;

import android.content.Context;
import android.util.Log;
import net.sqlcipher.database.SQLiteDatabase;
import org.smartregister.AllConstants;
import org.smartregister.cbhc.BuildConfig;
import org.smartregister.cbhc.application.AncApplication;
import org.smartregister.configurableviews.repository.ConfigurableViewsRepository;
import org.smartregister.domain.db.Column;
import org.smartregister.immunization.repository.RecurringServiceRecordRepository;
import org.smartregister.immunization.repository.RecurringServiceTypeRepository;
import org.smartregister.immunization.repository.VaccineRepository;
import org.smartregister.immunization.util.IMDatabaseUtils;
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
        if(oldVersion<newVersion&&newVersion==6){
            upgradeToVersion6(db);
        }
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
            Log.e(TAG, "Database Error. " + e.getMessage());
            return null;
        }

    }

    @Override
    public synchronized SQLiteDatabase getWritableDatabase(String password) {

        try{
            if (writableDatabase == null || !writableDatabase.isOpen()) {
                if (writableDatabase != null) {
                    writableDatabase.close();
                }
                writableDatabase = super.getWritableDatabase(password);
            }
            return writableDatabase;
        } catch (Exception e) {
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
            Log.e(TAG, "upgradeToVersion2 " + Log.getStackTraceString(e));
        }

    }

    private void upgradeToVersion3(SQLiteDatabase db) {
        try {
            Column[] columns = {EventClientRepository.event_column.formSubmissionId};
            EventClientRepository.createIndex(db, EventClientRepository.Table.event, columns);

        } catch (Exception e) {
            Log.e(TAG, "upgradeToVersion3 " + Log.getStackTraceString(e));
        }
        try {
            Column[] columns = {EventClientRepository.event_column.formSubmissionId};
            EventClientRepository.createIndex(db, EventClientRepository.Table.event, columns);


        } catch (Exception e) {
            Log.e(TAG, "upgradeToVersion3 " + e.getMessage());
        }
    }

    private void upgradeToVersion4(SQLiteDatabase db) {


    }

    private void upgradeToVersion6(SQLiteDatabase db) {
        try{
            db.execSQL("ALTER TABLE ec_member ADD COLUMN dataApprovalStatus INTEGER DEFAULT 1");
            db.execSQL("ALTER TABLE ec_member ADD COLUMN dataApprovalComments VARCHAR DEFAULT 1");
            db.execSQL("ALTER TABLE ec_woman ADD COLUMN dataApprovalStatus INTEGER DEFAULT 1");
            db.execSQL("ALTER TABLE ec_woman ADD COLUMN dataApprovalComments VARCHAR DEFAULT 1");
            db.execSQL("ALTER TABLE ec_child ADD COLUMN dataApprovalStatus INTEGER DEFAULT 1");
            db.execSQL("ALTER TABLE ec_child ADD COLUMN dataApprovalComments VARCHAR DEFAULT 1");
            db.execSQL("ALTER TABLE ec_household ADD COLUMN dataApprovalStatus INTEGER DEFAULT 1");

        }catch(Exception e){

        }

    }
    private void upgradeToVersion5(SQLiteDatabase db) {


    }
}

