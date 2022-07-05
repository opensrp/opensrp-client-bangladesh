package org.smartregister.cbhc.interactor;

import android.content.ContentValues;
import android.support.annotation.VisibleForTesting;
import android.util.Log;
import android.util.Pair;

import org.smartregister.cbhc.sync.AncClientProcessorForJava;
import org.smartregister.cbhc.util.JsonFormUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.json.JSONObject;
import org.smartregister.cbhc.application.AncApplication;
import org.smartregister.cbhc.contract.RegisterContract;
import org.smartregister.cbhc.domain.FollowupForm;
import org.smartregister.cbhc.domain.UniqueId;
import org.smartregister.cbhc.event.PatientRemovedEvent;
import org.smartregister.cbhc.helper.ECSyncHelper;
import org.smartregister.cbhc.repository.FollowupRepository;
import org.smartregister.cbhc.repository.HealthIdRepository;
import org.smartregister.cbhc.repository.UniqueIdRepository;
import org.smartregister.cbhc.util.AppExecutors;
import org.smartregister.cbhc.util.Constants;
import org.smartregister.cbhc.util.DBConstants;
import org.smartregister.cbhc.util.Utils;
import org.smartregister.clientandeventmodel.Client;
import org.smartregister.clientandeventmodel.Event;
import org.smartregister.commonregistry.AllCommonsRepository;
import org.smartregister.repository.AllSharedPreferences;
import org.smartregister.repository.BaseRepository;
import org.smartregister.sync.ClientProcessorForJava;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;


/**
 * Created by keyman 27/06/2018.
 */
public class RegisterInteractor implements RegisterContract.Interactor {


    public static final String TAG = RegisterInteractor.class.getName();
    private AppExecutors appExecutors;
    private UniqueIdRepository uniqueIdRepository;
    private HealthIdRepository healthIdRepository;
    private ECSyncHelper syncHelper;
    private AllSharedPreferences allSharedPreferences;
    private AncClientProcessorForJava clientProcessorForJava;
    private AllCommonsRepository allCommonsRepository;

    @VisibleForTesting
    RegisterInteractor(AppExecutors appExecutors) {
        this.appExecutors = appExecutors;
    }

    public RegisterInteractor() {
        this(new AppExecutors());
    }

    @Override
    public void getNextUniqueId(final Triple<String, String, String> triple, final RegisterContract.InteractorCallBack callBack) {

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                UniqueId uniqueId = getUniqueIdRepository().getNextUniqueId();
                //final String entityId = uniqueId != null ? uniqueId.getOpenmrsId() : "";
                final String entityId = generateIds(20);
                appExecutors.mainThread().execute(new Runnable() {
                    @Override
                    public void run() {
                        if (StringUtils.isBlank(entityId)) {
                            callBack.onNoUniqueId();
                        } else {
                            callBack.onUniqueIdFetched(triple, entityId);
                        }
                    }
                });
            }
        };

        appExecutors.diskIO().execute(runnable);
    }

    private String generateIds(int size) {
        List<String> ids = new ArrayList<>();
        Random r = new Random();

        /*for (int i = 0; i < size; i++) {
            Integer randomInt = r.nextInt(1000) + 1;
            ids.add(randomInt.toString());
        }*/
        Integer randomInt = r.nextInt(1000) + 1;
        return randomInt.toString();
    }

    @Override
    public void getNextUniqueId(final String formName, final String metadata, final String currentLocationId, final String householdID, final RegisterContract.InteractorCallBack callBack) {

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                UniqueId uniqueId = getUniqueIdRepository().getNextUniqueId();
                final String entityId = /*uniqueId != null ? uniqueId.getOpenmrsId() : ""*/generateIds(20);
                appExecutors.mainThread().execute(new Runnable() {
                    @Override
                    public void run() {
                        if (StringUtils.isBlank(entityId)) {
                            callBack.onNoUniqueId();
                        } else {
                            callBack.onUniqueIdFetched(formName, metadata, currentLocationId, householdID, entityId);
                        }
                    }
                });
            }
        };

        appExecutors.diskIO().execute(runnable);
    }

    @Override
    public void getNextHealthId(final String formName, final String metadata, final String currentLocationId, final String householdID, final RegisterContract.InteractorCallBack callBack) {

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
//                UniqueId uniqueId = getHealthIdRepository().getNextUniqueId();
//                final String entityId = uniqueId != null ? uniqueId.getOpenmrsId() : "";
                final boolean uniqueIdFound = getHealthIdRepository().countUnUsedIds() >= 0;//actual comp is >, >= for testing
                appExecutors.mainThread().execute(new Runnable() {
                    @Override
                    public void run() {
                        if (!uniqueIdFound) {
                            callBack.onNoUniqueId();
                        } else {
                            callBack.onUniqueIdFetched(formName, metadata, currentLocationId, householdID, Utils.DEFAULT_IDENTIFIER);
                        }
                    }
                });
            }
        };

        appExecutors.diskIO().execute(runnable);
    }

    /**
     * for passing extra camp type
     * @param formName
     * @param metadata
     * @param currentLocationId
     * @param householdID
     * @param callBack
     * @param campType
     */
    @Override
    public void getNextHealthId(final String formName, final String metadata, final String currentLocationId, final String householdID, final RegisterContract.InteractorCallBack callBack,String campType) {

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
//                UniqueId uniqueId = getHealthIdRepository().getNextUniqueId();
//                final String entityId = uniqueId != null ? uniqueId.getOpenmrsId() : "";
                final boolean uniqueIdFound = getHealthIdRepository().countUnUsedIds() >= 0;//actual comp is >, >= for testing
                appExecutors.mainThread().execute(new Runnable() {
                    @Override
                    public void run() {
                        if (!uniqueIdFound) {
                            callBack.onNoUniqueId();
                        } else {
                            callBack.onUniqueIdFetched(formName, metadata, currentLocationId, householdID, Utils.DEFAULT_IDENTIFIER,campType);
                        }
                    }
                });
            }
        };

        appExecutors.diskIO().execute(runnable);
    }

    @Override
    public void saveRegistration(final Pair<Client, Event> pair, final String jsonString, final boolean isEditMode, final RegisterContract.InteractorCallBack callBack) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                saveRegistration(pair, jsonString, isEditMode);
                appExecutors.mainThread().execute(new Runnable() {
                    @Override
                    public void run() {
                        callBack.onRegistrationSaved(isEditMode);
                    }
                });
            }
        };

        appExecutors.diskIO().execute(runnable);
    }

    @Override
    public void removeWomanFromANCRegister(final String closeFormJsonString, final String providerId) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {

                try {

                    Triple<Boolean, Event, Event> triple = JsonFormUtils.saveRemovedFromANCRegister(getAllSharedPreferences(), closeFormJsonString, providerId);

                    if (triple == null) {
                        return;
                    }

                    boolean isDeath = triple.getLeft();
                    Event event = triple.getMiddle();
                    Event updateChildDetailsEvent = triple.getRight();

                    String baseEntityId = event.getBaseEntityId();

                    //Update client to deceased
                    JSONObject client = getSyncHelper().getClient(baseEntityId);
                    if (isDeath) {
                        client.put(Constants.JSON_FORM_KEY.DEATH_DATE, Utils.getTodaysDate());
                        client.put(Constants.JSON_FORM_KEY.DEATH_DATE_APPROX, false);
                    }
                    JSONObject attributes = client.getJSONObject(Constants.JSON_FORM_KEY.ATTRIBUTES);
                    attributes.put(DBConstants.KEY.DATE_REMOVED, Utils.getTodaysDate());
                    client.put(Constants.JSON_FORM_KEY.ATTRIBUTES, attributes);
                    getSyncHelper().addClient(baseEntityId, client);

                    //Add Remove Event for child to flag for Server delete
                    JSONObject eventJson = new JSONObject(JsonFormUtils.gson.toJson(event));
                    getSyncHelper().addEvent(event.getBaseEntityId(), eventJson);

                    //Update Child Entity to include death date
                    JSONObject eventJsonUpdateChildEvent = new JSONObject(JsonFormUtils.gson.toJson(updateChildDetailsEvent));
                    getSyncHelper().addEvent(baseEntityId, eventJsonUpdateChildEvent); //Add event to flag server update

                    //Update REGISTER and FTS Tables
                    if (getAllCommonsRepository() != null) {
                        ContentValues values = new ContentValues();
                        values.put(DBConstants.KEY.DATE_REMOVED, Utils.getTodaysDate());
                        getAllCommonsRepository().update(DBConstants.WOMAN_TABLE_NAME, values, baseEntityId);
                        getAllCommonsRepository().updateSearch(baseEntityId);

                    }
                } catch (Exception e) {
                    Utils.appendLog(getClass().getName(), e);
                    Log.e(TAG, "", e);
                } finally {
                    Utils.postStickyEvent(new PatientRemovedEvent());
                }
            }
        };

        appExecutors.diskIO().execute(runnable);
    }

    private void saveRegistration(Pair<Client, Event> pair, String jsonString, boolean isEditMode) {

        try {
            Thread.sleep(2000);
            Client baseClient = pair.first;
            Event baseEvent = pair.second;
//            if(jsonString.contains("Followup HH Transfer")){
//                isEditMode = false;
//            }
            if (baseClient != null) {
                JSONObject clientJson = new JSONObject(JsonFormUtils.gson.toJson(baseClient));
                if (isEditMode) {
                    JsonFormUtils.mergeAndSaveClient(getSyncHelper(), baseClient);
                } else {
                    getSyncHelper().addClient(baseClient.getBaseEntityId(), clientJson);
                }
            }

            if (baseEvent != null) {
                JSONObject eventJson = new JSONObject(JsonFormUtils.gson.toJson(baseEvent));
                getSyncHelper().addEvent(baseEvent.getBaseEntityId(), eventJson);
            }

            if (Utils.notFollowUp(baseEvent.getEventType())) {

                if (isEditMode) {
                    // Unassign current OPENSRP ID
                    if (baseClient != null) {
                        String newOpenSRPId = baseClient.getIdentifier(DBConstants.KEY.Patient_Identifier).replace("-", "");
                        String currentOpenSRPId = JsonFormUtils.getString(jsonString, JsonFormUtils.CURRENT_OPENSRP_ID).replace("-", "");
                        if (!newOpenSRPId.equals(currentOpenSRPId)) {
                            //OPENSRP ID was changed
                            getUniqueIdRepository().close(currentOpenSRPId);
                            getHealthIdRepository().close(currentOpenSRPId);
                        }
                    }

                } else {
                    if (baseClient != null) {
                        String opensrpId = baseClient.getIdentifier(DBConstants.KEY.Patient_Identifier);
                        //mark OPENSRP ID as used
                        getUniqueIdRepository().close(opensrpId);
                        getHealthIdRepository().close(opensrpId);
                    }
                }


                if (baseClient != null || baseEvent != null) {
                    String imageLocation = JsonFormUtils.getFieldValue(jsonString, Constants.KEY.PHOTO);
                    JsonFormUtils.saveImage(baseEvent.getProviderId(), baseClient.getBaseEntityId(), imageLocation);
                }
            }

            long lastSyncTimeStamp = getAllSharedPreferences().fetchLastUpdatedAtDate(0);
            Date lastSyncDate = new Date(lastSyncTimeStamp);

            getClientProcessorForJava().processClient(getSyncHelper().getEvents(lastSyncDate, BaseRepository.TYPE_Unprocessed));
            getAllSharedPreferences().saveLastUpdatedAtDate(lastSyncDate.getTime());

            /////////////////////////try this out////////////////
//            ECSyncHelper ecUpdater = ECSyncHelper.getInstance(AncApplication.getInstance().getApplicationContext());
//            List<EventClient> events = ecUpdater.allEventClients(lastSyncTimeStamp-1, lastSyncTimeStamp);
//            AncClientProcessorForJava.getInstance(AncApplication.getInstance().getApplicationContext()).processClient(events);

            ////////////////////////////////////////////////////////////////
            //now save followup form here
            try {
                JSONObject formObject = new JSONObject(jsonString);
                String encounter_type = formObject.getString("encounter_type");
                String base_entity_id = formObject.getString("entity_id");
                if (encounter_type != null && !Utils.notFollowUp(encounter_type)) {
                    //time to save followup jinish
                    FollowupForm followupForm = new FollowupForm();
                    followupForm.setBase_entity_id(base_entity_id);
                    followupForm.setForm_name(encounter_type);
                    followupForm.setDate(new Date());
                    followupForm.setFormFields(jsonString);
                    FollowupRepository followupFormRepository = new FollowupRepository(AncApplication.getInstance().getRepository());
                    followupFormRepository.saveForm(followupForm);
                }
            } catch (Exception e) {
                Utils.appendLog(getClass().getName(), e);

            }

        } catch (Exception e) {
            Utils.appendLog(getClass().getName(), e);
            Log.e(TAG, Log.getStackTraceString(e));
        }
    }

    @Override
    public void onDestroy(boolean isChangingConfiguration) {
        //TODO set presenter or model to null
    }

    public UniqueIdRepository getUniqueIdRepository() {
        if (uniqueIdRepository == null) {
            uniqueIdRepository = AncApplication.getInstance().getUniqueIdRepository();
        }
        return uniqueIdRepository;
    }

    public void setUniqueIdRepository(UniqueIdRepository uniqueIdRepository) {
        this.uniqueIdRepository = uniqueIdRepository;
    }

    public HealthIdRepository getHealthIdRepository() {
        if (healthIdRepository == null) {
            healthIdRepository = AncApplication.getInstance().getHealthIdRepository();
        }
        return healthIdRepository;
    }

    public ECSyncHelper getSyncHelper() {
        if (syncHelper == null) {
            syncHelper = AncApplication.getInstance().getEcSyncHelper();
        }
        return syncHelper;
    }

    public void setSyncHelper(ECSyncHelper syncHelper) {
        this.syncHelper = syncHelper;
    }

    public AllSharedPreferences getAllSharedPreferences() {
        if (allSharedPreferences == null) {
            allSharedPreferences = AncApplication.getInstance().getContext().allSharedPreferences();
        }
        return allSharedPreferences;
    }

    public void setAllSharedPreferences(AllSharedPreferences allSharedPreferences) {
        this.allSharedPreferences = allSharedPreferences;
    }

    public ClientProcessorForJava getClientProcessorForJava() {
        if (clientProcessorForJava == null) {
            clientProcessorForJava = AncApplication.getInstance().getClientProcessorForJava();
        }
        return clientProcessorForJava;
    }

    public void setClientProcessorForJava(AncClientProcessorForJava clientProcessorForJava) {
        this.clientProcessorForJava = clientProcessorForJava;
    }

    public AllCommonsRepository getAllCommonsRepository() {
        if (allCommonsRepository == null) {
            allCommonsRepository = AncApplication.getInstance().getContext().allCommonsRepositoryobjects(DBConstants.WOMAN_TABLE_NAME);
        }
        return allCommonsRepository;
    }

    public void setAllCommonsRepository(AllCommonsRepository allCommonsRepository) {
        this.allCommonsRepository = allCommonsRepository;
    }

    public enum type {SAVED, UPDATED}
}
