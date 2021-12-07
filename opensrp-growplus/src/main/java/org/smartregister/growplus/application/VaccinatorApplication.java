package org.smartregister.growplus.application;

import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.util.Pair;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.evernote.android.job.JobManager;

import org.json.JSONArray;
import org.smartregister.Context;
import org.smartregister.CoreLibrary;
import org.smartregister.commonregistry.CommonFtsObject;
import org.smartregister.growplus.job.GrowPlusJobCreator;
import org.smartregister.growplus.repository.CounsellingRepository;
import org.smartregister.growthmonitoring.GrowthMonitoringLibrary;
import org.smartregister.growthmonitoring.repository.WeightRepository;
import org.smartregister.growthmonitoring.repository.ZScoreRepository;
import org.smartregister.immunization.ImmunizationLibrary;
import org.smartregister.immunization.db.VaccineRepo;
import org.smartregister.immunization.domain.VaccineSchedule;
import org.smartregister.immunization.repository.RecurringServiceRecordRepository;
import org.smartregister.immunization.repository.RecurringServiceTypeRepository;
import org.smartregister.immunization.repository.VaccineNameRepository;
import org.smartregister.immunization.repository.VaccineRepository;
import org.smartregister.immunization.repository.VaccineTypeRepository;
import org.smartregister.immunization.util.VaccinateActionUtils;
import org.smartregister.immunization.util.VaccinatorUtils;
import org.smartregister.growplus.BuildConfig;
import org.smartregister.growplus.R;
import org.smartregister.growplus.activity.LoginActivity;
import org.smartregister.growplus.receiver.PathSyncBroadcastReceiver;
import org.smartregister.growplus.receiver.SyncStatusBroadcastReceiver;
import org.smartregister.growplus.repository.PathRepository;
import org.smartregister.growplus.repository.UniqueIdRepository;
import org.smartregister.growplus.sync.PathUpdateActionsTask;
import org.smartregister.repository.EventClientRepository;
import org.smartregister.repository.Repository;
import org.smartregister.sync.DrishtiSyncScheduler;
import org.smartregister.view.activity.DrishtiApplication;
import org.smartregister.view.receiver.TimeChangedBroadcastReceiver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import io.fabric.sdk.android.Fabric;
import util.PathConstants;

import static org.smartregister.util.Log.logError;
import static org.smartregister.util.Log.logInfo;

/**
 * Created by koros on 2/3/16.
 */
public class VaccinatorApplication extends DrishtiApplication
        implements TimeChangedBroadcastReceiver.OnTimeChangedListener {

    private static final String TAG = "VaccinatorApplication";
    private static CommonFtsObject commonFtsObject;
    private UniqueIdRepository uniqueIdRepository;
    private EventClientRepository eventClientRepository;
    private boolean lastModified;
    private CounsellingRepository counsellingRepository;

    @Override
    public void onCreate() {
        super.onCreate();

        mInstance = this;

        context = Context.getInstance();
        context.updateApplicationContext(getApplicationContext());
        context.updateCommonFtsObject(createCommonFtsObject());

        if (!BuildConfig.DEBUG) {
            Fabric.with(this, new Crashlytics());
        }
        DrishtiSyncScheduler.setReceiverClass(PathSyncBroadcastReceiver.class);

        SyncStatusBroadcastReceiver.init(this);
        TimeChangedBroadcastReceiver.init(this);
        TimeChangedBroadcastReceiver.getInstance().addOnTimeChangedListener(this);

        //applyUserLanguagePreference();
        cleanUpSyncState();
        initOfflineSchedules();
//        setCrashlyticsUser(context);
        PathUpdateActionsTask.setAlarms(this);

        //Initialize Modules
        CoreLibrary.init(context());
        GrowthMonitoringLibrary.init(context(), getRepository(), BuildConfig.VERSION_CODE, BuildConfig.DATABASE_VERSION);
        ImmunizationLibrary.init(context(), getRepository(), createCommonFtsObject(), BuildConfig.VERSION_CODE, BuildConfig.DATABASE_VERSION);
        JobManager.create(this).addJobCreator(new GrowPlusJobCreator());
    }

    public static synchronized VaccinatorApplication getInstance() {
        return (VaccinatorApplication) mInstance;
    }

    @Override
    public void logoutCurrentUser() {

        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        getApplicationContext().startActivity(intent);
        context.userService().logoutSession();
    }

    protected void cleanUpSyncState() {
        DrishtiSyncScheduler.stop(getApplicationContext());
        context.allSharedPreferences().saveIsSyncInProgress(false);
    }


    @Override
    public void onTerminate() {
        logInfo("Application is terminating. Stopping Bidan Sync scheduler and resetting isSyncInProgress setting.");
        cleanUpSyncState();
        SyncStatusBroadcastReceiver.destroy(this);
        TimeChangedBroadcastReceiver.destroy(this);
        super.onTerminate();
    }

    protected void applyUserLanguagePreference() {
        Configuration config = getBaseContext().getResources().getConfiguration();

        String lang = context.allSharedPreferences().fetchLanguagePreference();
        if (!"".equals(lang) && !config.locale.getLanguage().equals(lang)) {
            locale = new Locale(lang);
            updateConfiguration(config);
        }
    }

    private void updateConfiguration(Configuration config) {
        config.locale = locale;
        Locale.setDefault(locale);
        getBaseContext().getResources().updateConfiguration(config,
                getBaseContext().getResources().getDisplayMetrics());
    }

    private static String[] getFtsSearchFields(String tableName) {
        if (tableName.equals(PathConstants.CHILD_TABLE_NAME)) {
            return new String[]{"zeir_id", "epi_card_number", "first_name", "last_name","block"};
        } else if (tableName.equals(PathConstants.MOTHER_TABLE_NAME)) {
            return new String[]{"zeir_id", "epi_card_number", "first_name", "last_name", "father_name", "husband_name", "contact_phone_number","block"};
        }
        return null;
    }

    private static String[] getFtsSortFields(String tableName) {


        if (tableName.equals(PathConstants.CHILD_TABLE_NAME)) {
            ArrayList<VaccineRepo.Vaccine> vaccines = VaccineRepo.getVaccines("child");
            List<String> names = new ArrayList<>();
            names.add("first_name");
            names.add("dob");
            names.add("zeir_id");
            names.add("last_interacted_with");
            names.add("inactive");
            names.add("lost_to_follow_up");
            names.add("block");
            names.add(PathConstants.EC_CHILD_TABLE.DOD);

            for (VaccineRepo.Vaccine vaccine : vaccines) {
                names.add("alerts." + VaccinateActionUtils.addHyphen(vaccine.display()));
            }

            return names.toArray(new String[names.size()]);
        } else if (tableName.equals(PathConstants.MOTHER_TABLE_NAME)) {
            return new String[]{"first_name", "dob", "zeir_id", "last_interacted_with","block"};
        }
        return null;
    }

    private static String[] getFtsTables() {
        return new String[]{PathConstants.CHILD_TABLE_NAME, PathConstants.MOTHER_TABLE_NAME};
    }

    private static Map<String, Pair<String, Boolean>> getAlertScheduleMap() {
        ArrayList<VaccineRepo.Vaccine> vaccines = VaccineRepo.getVaccines("child");
        Map<String, Pair<String, Boolean>> map = new HashMap<>();
        for (VaccineRepo.Vaccine vaccine : vaccines) {
            map.put(vaccine.display(), Pair.create(PathConstants.CHILD_TABLE_NAME, false));
        }
        return map;
    }

    public static CommonFtsObject createCommonFtsObject() {
        if (commonFtsObject == null) {
            commonFtsObject = new CommonFtsObject(getFtsTables());
            for (String ftsTable : commonFtsObject.getTables()) {
                commonFtsObject.updateSearchFields(ftsTable, getFtsSearchFields(ftsTable));
                commonFtsObject.updateSortFields(ftsTable, getFtsSortFields(ftsTable));
            }
        }
        commonFtsObject.updateAlertScheduleMap(getAlertScheduleMap());
        return commonFtsObject;
    }

    /**
     * This method sets the Crashlytics user to whichever username was used to log in last. It only
     * does so if the app is not built for debugging
     *
     * @param context The user's context
     */
    public static void setCrashlyticsUser(Context context) {
        if (!BuildConfig.DEBUG
                && context != null && context.userService() != null
                && context.userService().getAllSharedPreferences() != null) {
            Crashlytics.setUserName(context.userService().getAllSharedPreferences().fetchRegisteredANM());
        }
    }

    private void grantPhotoDirectoryAccess() {
        Uri uri = FileProvider.getUriForFile(this,
                "com.vijay.jsonwizard.fileprovider",
                getExternalFilesDir(Environment.DIRECTORY_PICTURES));
        grantUriPermission("com.vijay.jsonwizard", uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
    }

    @Override
    public Repository getRepository() {
        try {
            if (repository == null) {
                repository = new PathRepository(getInstance().getApplicationContext(), context());
                uniqueIdRepository();
                eventClientRepository();

            }
        } catch (UnsatisfiedLinkError e) {
            logError("Error on getRepository: " + e);

        }
        return repository;
    }


    public WeightRepository weightRepository() {
        return GrowthMonitoringLibrary.getInstance().weightRepository();
    }

    public Context context() {
        return context;
    }

    public VaccineRepository vaccineRepository() {
        return ImmunizationLibrary.getInstance().vaccineRepository();
    }

    public ZScoreRepository zScoreRepository() {
        return GrowthMonitoringLibrary.getInstance().zScoreRepository();
    }

    public CounsellingRepository counsellingRepository() {
        if (counsellingRepository == null) {
            counsellingRepository = new CounsellingRepository((PathRepository) getRepository(),this.commonFtsObject,context().alertService());
        }
        return counsellingRepository;
    }

    public UniqueIdRepository uniqueIdRepository() {
        if (uniqueIdRepository == null) {
            uniqueIdRepository = new UniqueIdRepository((PathRepository) getRepository());
        }
        return uniqueIdRepository;
    }

    public RecurringServiceTypeRepository recurringServiceTypeRepository() {
        return ImmunizationLibrary.getInstance().recurringServiceTypeRepository();
    }

    public RecurringServiceRecordRepository recurringServiceRecordRepository() {
        return ImmunizationLibrary.getInstance().recurringServiceRecordRepository();
    }

    public EventClientRepository eventClientRepository() {
        if (eventClientRepository == null) {
            eventClientRepository = new EventClientRepository(getRepository());
        }
        return eventClientRepository;
    }



    public VaccineTypeRepository vaccineTypeRepository() {
        return ImmunizationLibrary.getInstance().vaccineTypeRepository();
    }

    public VaccineNameRepository vaccineNameRepository() {
        return ImmunizationLibrary.getInstance().vaccineNameRepository();
    }

    public boolean isLastModified() {
        return lastModified;
    }

    public void setLastModified(boolean lastModified) {
        this.lastModified = lastModified;
    }

    @Override
    public void onTimeChanged() {
//        Toast.makeText(this, R.string.device_time_changed, Toast.LENGTH_LONG).show();
//        context.userService().forceRemoteLogin();
//        logoutCurrentUser();
    }

    @Override
    public void onTimeZoneChanged() {
//        Toast.makeText(this, R.string.device_timezone_changed, Toast.LENGTH_LONG).show();
//        context.userService().forceRemoteLogin();
//        logoutCurrentUser();
    }

    private void initOfflineSchedules() {
        try {
            JSONArray childVaccines = new JSONArray(VaccinatorUtils.getSupportedVaccines(this));
            JSONArray specialVaccines = new JSONArray(VaccinatorUtils.getSpecialVaccines(this));
            JSONArray womanVaccines = new JSONArray(VaccinatorUtils.getSupportedWomanVaccines(this));
//            VaccineSchedule.init(childVaccines, specialVaccines, "child");
//            VaccineSchedule.init(womanVaccines, null, "woman");
        } catch (Exception e) {
            Log.e(TAG, Log.getStackTraceString(e));
        }
    }

}
