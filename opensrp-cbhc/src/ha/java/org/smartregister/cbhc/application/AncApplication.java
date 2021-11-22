package org.smartregister.cbhc.application;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.evernote.android.job.JobManager;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.smartregister.Context;
import org.smartregister.CoreLibrary;
import org.smartregister.cbhc.BuildConfig;
import org.smartregister.cbhc.activity.LoginActivity;
import org.smartregister.cbhc.event.TriggerSyncEvent;
import org.smartregister.cbhc.event.ViewConfigurationSyncCompleteEvent;
import org.smartregister.cbhc.helper.ECSyncHelper;
import org.smartregister.cbhc.job.AncJobCreator;
import org.smartregister.cbhc.job.ViewConfigurationsServiceJob;
import org.smartregister.cbhc.receiver.SyncStatusBroadcastReceiver;
import org.smartregister.cbhc.repository.AncRepository;
import org.smartregister.cbhc.repository.HealthIdRepository;
import org.smartregister.cbhc.repository.UniqueIdRepository;
import org.smartregister.cbhc.service.intent.PullHealthIdsIntentService;
import org.smartregister.cbhc.service.intent.PullUniqueIdsIntentService;
import org.smartregister.cbhc.sync.AncSyncConfiguration;
import org.smartregister.cbhc.util.Constants;
import org.smartregister.cbhc.util.DBConstants;
import org.smartregister.cbhc.util.Utils;
import org.smartregister.commonregistry.CommonFtsObject;
import org.smartregister.configurableviews.ConfigurableViewsLibrary;
import org.smartregister.configurableviews.helper.ConfigurableViewsHelper;
import org.smartregister.configurableviews.helper.JsonSpecHelper;
import org.smartregister.configurableviews.repository.ConfigurableViewsRepository;
import org.smartregister.configurableviews.service.PullConfigurableViewsIntentService;
import org.smartregister.growthmonitoring.GrowthMonitoringLibrary;
import org.smartregister.growthmonitoring.repository.WeightRepository;
import org.smartregister.growthmonitoring.service.intent.ZScoreRefreshIntentService;
import org.smartregister.immunization.ImmunizationLibrary;
import org.smartregister.immunization.domain.VaccineSchedule;
import org.smartregister.immunization.domain.jsonmapping.Vaccine;
import org.smartregister.immunization.domain.jsonmapping.VaccineGroup;
import org.smartregister.immunization.util.VaccinatorUtils;
import org.smartregister.location.helper.LocationHelper;
import org.smartregister.repository.AllSharedPreferences;
import org.smartregister.repository.EventClientRepository;
import org.smartregister.repository.Repository;
import org.smartregister.sync.ClientProcessorForJava;
import org.smartregister.sync.DrishtiSyncScheduler;
import org.smartregister.view.activity.DrishtiApplication;
import org.smartregister.view.receiver.TimeChangedBroadcastReceiver;

import java.util.ArrayList;
import java.util.List;

import id.zelory.compressor.Compressor;

import static org.smartregister.util.Log.logError;
import static org.smartregister.util.Log.logInfo;

/**
 * Created by ndegwamartin on 21/06/2018.
 */
public class AncApplication extends DrishtiApplication implements TimeChangedBroadcastReceiver.OnTimeChangedListener {


    public static final ArrayList<String> ALLOWED_LEVELS;
    public static final String DEFAULT_LOCATION_LEVEL = "Health Facility";
    public static final String FACILITY = "Dispensary";
    private static final String TAG = AncApplication.class.getCanonicalName();
    private static JsonSpecHelper jsonSpecHelper;
    private static CommonFtsObject commonFtsObject;

    static {
        ALLOWED_LEVELS = new ArrayList<>();
        ALLOWED_LEVELS.add(DEFAULT_LOCATION_LEVEL);
        ALLOWED_LEVELS.add(FACILITY);
    }

    private ConfigurableViewsRepository configurableViewsRepository;
    private EventClientRepository eventClientRepository;
    private ConfigurableViewsHelper configurableViewsHelper;
    private UniqueIdRepository uniqueIdRepository;
    private HealthIdRepository healthIdRepository;
    private ECSyncHelper ecSyncHelper;
    private Compressor compressor;
    private ClientProcessorForJava clientProcessorForJava;
    private String password;
    private String DEFAULT_PASSWORD = "1e815e13-f6ca-42ef-97c8-83394c201a47";
    // This Broadcast Receiver is the handler called whenever an Intent with an action named PullConfigurableViewsIntentService.EVENT_SYNC_COMPLETE is broadcast.
    private BroadcastReceiver syncCompleteMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(android.content.Context context, Intent intent) {
            // Retrieve the extra data included in the Intent

            int recordsRetrievedCount = intent.getIntExtra(org.smartregister.configurableviews.util.Constants.INTENT_KEY.SYNC_TOTAL_RECORDS, 0);
            if (recordsRetrievedCount > 0) {
                Log.d(TAG, "Total records retrieved " + recordsRetrievedCount);
            }

            Utils.postEvent(new ViewConfigurationSyncCompleteEvent());

            String lastSyncTime = intent.getStringExtra(org.smartregister.configurableviews.util.Constants.INTENT_KEY.LAST_SYNC_TIME_STRING);

            Utils.writePrefString(context, org.smartregister.configurableviews.util.Constants.INTENT_KEY.LAST_SYNC_TIME_STRING, lastSyncTime);

        }
    };

    public static synchronized AncApplication getInstance() {
        return (AncApplication) mInstance;
    }
    public WeightRepository weightRepository() {
        return GrowthMonitoringLibrary.getInstance().weightRepository();
    }
    public static JsonSpecHelper getJsonSpecHelper() {
        return jsonSpecHelper;
    }

    public static CommonFtsObject createCommonFtsObject() {
        if (commonFtsObject == null) {
            commonFtsObject = new CommonFtsObject(getFtsTables());
            for (String ftsTable : commonFtsObject.getTables()) {
                commonFtsObject.updateSearchFields(ftsTable, getFtsSearchFields());
                commonFtsObject.updateSortFields(ftsTable, getFtsSortFields());
            }
        }
        return commonFtsObject;
    }

    private static String[] getFtsTables() {
        return new String[]{DBConstants.HOUSEHOLD_TABLE_NAME, DBConstants.WOMAN_TABLE_NAME, DBConstants.MEMBER_TABLE_NAME, DBConstants.CHILD_TABLE_NAME};
    }

    private static String[] getFtsSearchFields() {
//        return new String[]{DBConstants.KEY.BASE_ENTITY_ID, DBConstants.KEY.FIRST_NAME, DBConstants.KEY.LAST_NAME, DBConstants.KEY.ANC_ID, DBConstants.KEY.DATE_REMOVED, DBConstants.KEY.PHONE_NUMBER};
        return new String[]{DBConstants.KEY.FIRST_NAME, DBConstants.KEY.LAST_NAME, DBConstants.KEY.PHONE_NUMBER, DBConstants.KEY.DATE_REMOVED, "person_nid", "person_brid", "person_epi", "person_address"};

    }

    private static String[] getFtsSortFields() {
        return new String[]{DBConstants.KEY.BASE_ENTITY_ID, DBConstants.KEY.FIRST_NAME, DBConstants.KEY.LAST_NAME, DBConstants.KEY.LAST_INTERACTED_WITH, DBConstants.KEY.DATE_REMOVED};
//        return new String[]{DBConstants.KEY.FIRST_NAME, DBConstants.KEY.LAST_NAME, DBConstants.KEY.PHONE_NUMBER,"person_nid","person_brid","person_epi","person_address",DBConstants.KEY.LAST_INTERACTED_WITH};
    }

    @Override
    public void onCreate() {

        super.onCreate();

        mInstance = this;
        context = Context.getInstance();
        context.updateApplicationContext(getApplicationContext());
        context.updateCommonFtsObject(createCommonFtsObject());

        //Initialize Modules
        CoreLibrary.init(context, new AncSyncConfiguration(), BuildConfig.BUILD_TIMESTAMP, null);
        CoreLibrary.getInstance().setEcClientFieldsFile(Constants.EC_CLIENT_FIELDS);

        LocationHelper.init(ALLOWED_LEVELS, DEFAULT_LOCATION_LEVEL);

        SyncStatusBroadcastReceiver.init(this);
        TimeChangedBroadcastReceiver.init(this);
        TimeChangedBroadcastReceiver.getInstance().addOnTimeChangedListener(this);

        JobManager.create(this).addJobCreator(new AncJobCreator());
        try {
            Utils.saveLanguage("en");
        } catch (Exception e) {
            Utils.appendLog(getClass().getName(), e);
            Log.e(TAG, e.getMessage());
        }
        initLibraries();
        initOfflineSchedules();
// Initialize JsonSpec Helper
        jsonSpecHelper = new JsonSpecHelper(this);

        setUpEventHandling();
        String groupId = getPassword();
//        if (groupId != null && !groupId.isEmpty()) {
//            initLibraries();
//            initOfflineSchedules();
//        }


    }

    public void initLibraries() {
        ConfigurableViewsLibrary.init(context, getRepository());
        ImmunizationLibrary.init(context, getRepository(), null, BuildConfig.VERSION_CODE, BuildConfig.DATABASE_VERSION);

        GrowthMonitoringLibrary.init(context, getRepository(), BuildConfig.VERSION_CODE, BuildConfig.DATABASE_VERSION);
        startZscoreRefreshService();
        startPullConfigurableViewsIntentService(this);
    }

    private AllSharedPreferences getSharedPreferences() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        AllSharedPreferences allSharedPreferences = new AllSharedPreferences(preferences);
        return allSharedPreferences;
    }

    public void initOfflineSchedules() {
        try {
            List<VaccineGroup> childVaccines = VaccinatorUtils.getSupportedVaccines(this);
            List<VaccineGroup> womanVaccines = VaccinatorUtils.getSupportedWomanVaccines(this);
            List<Vaccine> specialVaccines = VaccinatorUtils.getSpecialVaccines(this);
            VaccineSchedule.init(childVaccines, specialVaccines, "child");
            VaccineSchedule.init(womanVaccines, null, "woman");

        } catch (Exception e) {
            Utils.appendLog(getClass().getName(), e);
            Log.e(TAG, Log.getStackTraceString(e));
        }
    }

    @Override
    public Repository getRepository() {
        try {
            if (repository == null) {
                repository = new AncRepository(getInstance().getApplicationContext(), context);
                getConfigurableViewsRepository();

            }
        } catch (UnsatisfiedLinkError e) {
            logError("Error on getRepository: " + e);

        }
        return repository;
    }

    public String getPassword() {
        if (password == null) {
            String username = getContext().userService().getAllSharedPreferences().fetchRegisteredANM();
            password = getContext().userService().getGroupId(username);
        }
//        return "";
        return password;

    }

    @Override
    public void logoutCurrentUser() {

    }
    public void logoutUser(){
        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        getApplicationContext().startActivity(intent);
        context.userService().logoutSession();
    }

    public Context getContext() {
        return context;
    }

    protected void cleanUpSyncState() {
        try {
            DrishtiSyncScheduler.stop(getApplicationContext());
            context.allSharedPreferences().saveIsSyncInProgress(false);
        } catch (Exception e) {
            Utils.appendLog(getClass().getName(), e);
            Log.e(TAG, e.getMessage());
        }
    }

    @Override
    public void onTerminate() {
        logInfo("Application is terminating. Stopping Sync scheduler and resetting isSyncInProgress setting.");
        cleanUpSyncState();
        TimeChangedBroadcastReceiver.destroy(this);
        super.onTerminate();
    }

    public void startPullConfigurableViewsIntentService(android.content.Context context) {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            context.startForegroundService(new Intent(context, PullConfigurableViewsIntentService.class));
//        } else {
//            context.startService(new Intent(context, PullConfigurableViewsIntentService.class));
//        }
        ViewConfigurationsServiceJob.scheduleJobImmediately(ViewConfigurationsServiceJob.TAG);
    }

    public ConfigurableViewsRepository getConfigurableViewsRepository() {
        if (configurableViewsRepository == null)
            configurableViewsRepository = new ConfigurableViewsRepository(getRepository());
        return configurableViewsRepository;
    }

    public EventClientRepository getEventClientRepository() {
        if (eventClientRepository == null) {
            eventClientRepository = new EventClientRepository(getRepository());
        }
        return eventClientRepository;
    }

    public UniqueIdRepository getUniqueIdRepository() {
        if (uniqueIdRepository == null) {
            uniqueIdRepository = new UniqueIdRepository((AncRepository) getRepository());
        }
        return uniqueIdRepository;
    }

    public HealthIdRepository getHealthIdRepository() {
        if (healthIdRepository == null) {
            healthIdRepository = new HealthIdRepository((AncRepository) getRepository());
        }
        return healthIdRepository;
    }

    public ConfigurableViewsHelper getConfigurableViewsHelper() {
        if (configurableViewsHelper == null) {
            configurableViewsHelper = new ConfigurableViewsHelper(getConfigurableViewsRepository(),
                    getJsonSpecHelper(), getApplicationContext());
        }
        return configurableViewsHelper;
    }

    public ECSyncHelper getEcSyncHelper() {
        if (ecSyncHelper == null) {
            ecSyncHelper = ECSyncHelper.getInstance(getApplicationContext());
        }
        return ecSyncHelper;
    }

    public Compressor getCompressor() {
        if (compressor == null) {
            compressor = Compressor.getDefault(getApplicationContext());
        }
        return compressor;
    }

    public ClientProcessorForJava getClientProcessorForJava() {
        if (clientProcessorForJava == null) {
            clientProcessorForJava = ClientProcessorForJava.getInstance(getApplicationContext());
        }
        return clientProcessorForJava;
    }

    private void setUpEventHandling() {

        try {

            LocalBroadcastManager.getInstance(this).registerReceiver(syncCompleteMessageReceiver, new IntentFilter(PullConfigurableViewsIntentService.EVENT_SYNC_COMPLETE));

        } catch
        (Exception e) {
            Utils.appendLog(getClass().getName(), e);
            Log.e(TAG, e.getMessage());
        }

        EventBus.getDefault().register(this);


    }

    @Subscribe(threadMode = ThreadMode.POSTING)
    public void triggerSync(TriggerSyncEvent event) {
        if (event != null) {
            startPullConfigurableViewsIntentService(this);
            //startSyncService(); call to trigger sync
        }

    }

    public void startPullHealthIdsService() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            getApplicationContext().startForegroundService(new Intent(getApplicationContext(), PullHealthIdsIntentService.class));
        } else {
            getApplicationContext().startService(new Intent(getApplicationContext(), PullHealthIdsIntentService.class));
        }
    }

    public void startPullUniqueIdsService() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            getApplicationContext().startForegroundService(new Intent(getApplicationContext(), PullUniqueIdsIntentService.class));
        } else {
            getApplicationContext().startService(new Intent(getApplicationContext(), PullUniqueIdsIntentService.class));
        }
    }

    @Override
    public void onTimeChanged() {
//        Utils.showToast(this, this.getString(R.string.device_time_changed));
//        context.userService().forceRemoteLogin();
//        logoutCurrentUser();
    }

    @Override
    public void onTimeZoneChanged() {
//        Utils.showToast(this, this.getString(R.string.device_timezone_changed));
//        context.userService().forceRemoteLogin();
//        logoutCurrentUser();
    }


    public void startZscoreRefreshService() {
        Intent intent = new Intent(getInstance().getApplicationContext(), ZScoreRefreshIntentService.class);
        this.getApplicationContext().startService(intent);
    }
    public void forcelogoutCurrentUser() {
        getContext().userService().getAllSharedPreferences().saveForceRemoteLogin(true);
        org.smartregister.cbhc.helper.LocationHelper.setInstance(null);
        logoutCurrentUser();
        System.exit(0);
    }
}
