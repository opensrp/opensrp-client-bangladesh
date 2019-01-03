package org.smartregister.cbhc.presenter;

import android.content.Intent;
import android.util.Log;
import android.util.Pair;

import org.apache.commons.lang3.tuple.Triple;
import org.json.JSONObject;
import org.smartregister.cbhc.R;
import org.smartregister.cbhc.activity.ProfileActivity;
import org.smartregister.cbhc.application.AncApplication;
import org.smartregister.cbhc.contract.ProfileContract;
import org.smartregister.cbhc.contract.RegisterContract;
import org.smartregister.cbhc.helper.ECSyncHelper;
import org.smartregister.cbhc.interactor.ProfileInteractor;
import org.smartregister.cbhc.interactor.RegisterInteractor;
import org.smartregister.cbhc.receiver.SyncStatusBroadcastReceiver;
import org.smartregister.cbhc.sync.AncClientProcessorForJava;
import org.smartregister.cbhc.util.Constants;
import org.smartregister.cbhc.util.DBConstants;
import org.smartregister.cbhc.util.JsonFormUtils;
import org.smartregister.cbhc.util.Utils;
import org.smartregister.clientandeventmodel.Client;
import org.smartregister.clientandeventmodel.Event;
import org.smartregister.domain.FetchStatus;
import org.smartregister.domain.db.EventClient;
import org.smartregister.repository.AllSharedPreferences;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Map;

/**
 * Created by ndegwamartin on 13/07/2018.
 */
public class ProfilePresenter implements ProfileContract.Presenter, RegisterContract.InteractorCallBack {

    private static final String TAG = ProfilePresenter.class.getCanonicalName();

    private WeakReference<ProfileContract.View> mProfileView;
    private ProfileContract.Interactor mProfileInteractor;
    private RegisterContract.Interactor mRegisterInteractor;
    ProfileActivity profileActivity;

    public ProfilePresenter(ProfileContract.View loginView) {
        mProfileView = new WeakReference<>(loginView);
        mProfileInteractor = new ProfileInteractor(this);
        mRegisterInteractor = new RegisterInteractor();
    }

    @Override
    public void onDestroy(boolean isChangingConfiguration) {

        mProfileView = null;//set to null on destroy

        // Inform interactor
        mProfileInteractor.onDestroy(isChangingConfiguration);
        mRegisterInteractor.onDestroy(isChangingConfiguration);

        // Activity destroyed set interactor to null
        if (!isChangingConfiguration) {
            mProfileInteractor = null;
            mRegisterInteractor = null;
        }

    }

    @Override
    public void refreshProfileView(String baseEntityId) {
        mProfileInteractor.refreshProfileView(baseEntityId);
    }

    @Override
    public ProfileContract.View getProfileView() {
        if (mProfileView != null) {
            return mProfileView.get();
        } else {
            return null;
        }
    }

    @Override
    public void processFormDetailsSave(Intent data, AllSharedPreferences allSharedPreferences) {
        try {
            String jsonString = data.getStringExtra(Constants.INTENT_KEY.JSON);
            Log.d("JSONResult", jsonString);

            JSONObject form = new JSONObject(jsonString);

            getProfileView().showProgressDialog(form.getString(JsonFormUtils.ENCOUNTER_TYPE).equals(Constants.EventType.CLOSE) ? R.string.removing_dialog_title : R.string.saving_dialog_title);

            if (form.getString(JsonFormUtils.ENCOUNTER_TYPE).equals(Constants.EventType.UPDATE_REGISTRATION)) {

                Pair<Client, Event> values = JsonFormUtils.processRegistrationForm(allSharedPreferences, jsonString);
                mRegisterInteractor.saveRegistration(values, jsonString, true, this);

            } else if (form.getString(JsonFormUtils.ENCOUNTER_TYPE).equals(Constants.EventType.UPDATE_Household_REGISTRATION)) {

                Pair<Client, Event> values = JsonFormUtils.processRegistrationForm(allSharedPreferences, jsonString);
                mRegisterInteractor.saveRegistration(values, jsonString, true, this);

            }else if (form.getString(JsonFormUtils.ENCOUNTER_TYPE).equals(Constants.EventType.HouseholdREGISTRATION)) {
                Pair<Client, Event> values = JsonFormUtils.processRegistrationForm(allSharedPreferences, jsonString);
                mRegisterInteractor.saveRegistration(values, jsonString, true, this);

            }else if (form.getString(JsonFormUtils.ENCOUNTER_TYPE).equals(Constants.EventType.MemberREGISTRATION)) {
                Pair<Client, Event> values = JsonFormUtils.processRegistrationForm(allSharedPreferences, jsonString);
                mRegisterInteractor.saveRegistration(values, jsonString, true, this);

            }else if (form.getString(JsonFormUtils.ENCOUNTER_TYPE).equals(Constants.EventType.WomanMemberREGISTRATION)) {
                Pair<Client, Event> values = JsonFormUtils.processRegistrationForm(allSharedPreferences, jsonString);
                mRegisterInteractor.saveRegistration(values, jsonString, true, this);

            }else if (form.getString(JsonFormUtils.ENCOUNTER_TYPE).equals(Constants.EventType.Child_REGISTRATION)) {
                Pair<Client, Event> values = JsonFormUtils.processRegistrationForm(allSharedPreferences, jsonString);
                mRegisterInteractor.saveRegistration(values, jsonString, true, this);

            }else if (form.getString(JsonFormUtils.ENCOUNTER_TYPE).equals(Constants.EventType.CLOSE)) {

                mRegisterInteractor.removeWomanFromANCRegister(jsonString, allSharedPreferences.fetchRegisteredANM());

            }else if (form.getString(JsonFormUtils.ENCOUNTER_TYPE).equals(Constants.EventType.MARITAL_STATUS)) {
            Pair<Client, Event> values = JsonFormUtils.processRegistrationForm(allSharedPreferences, jsonString);
            mRegisterInteractor.saveRegistration(values, jsonString, true, this);
            }else if (form.getString(JsonFormUtils.ENCOUNTER_TYPE).equals(Constants.EventType.PREGNANT_STATUS)) {
                Pair<Client, Event> values = JsonFormUtils.processRegistrationForm(allSharedPreferences, jsonString);
                mRegisterInteractor.saveRegistration(values, jsonString, true, this);
            } else {
                getProfileView().hideProgressDialog();
            }
        } catch (Exception e) {
            Log.e(TAG, Log.getStackTraceString(e));
        }
    }

    @Override
    public void onUniqueIdFetched(Triple<String, String, String> triple, String entityId) {
        //Overriden
    }

    @Override
    public void onUniqueIdFetched(String formName, String metadata, String currentLocationId, String householdID, String entityId) {

    }


    @Override
    public void onNoUniqueId() {
        getProfileView().displayToast(R.string.no_openmrs_id);

    }

    @Override
    public void onRegistrationSaved(boolean isEdit) {


        ////////////////////////////////////////////////////////////////

        this.refreshProfileView(getProfileView().getIntentString(Constants.INTENT_KEY.BASE_ENTITY_ID));
        if(profileActivity!=null)
            profileActivity.refreshProfileViews();
        getProfileView().hideProgressDialog();

        getProfileView().displayToast(isEdit ? R.string.registration_info_updated : R.string.new_registration_saved);
        if(profileActivity!=null)
            profileActivity.profileOverviewFragment.refreshadapter(profileActivity.profileOverviewFragment.getFragmentView());

    }

    @Override
    public void refreshProfileTopSection(Map<String, String> client) {

//        getProfileView().setProfileName(client.get(DBConstants.KEY.FIRST_NAME) + " " + client.get(DBConstants.KEY.LAST_NAME));
//        getProfileView().setProfileAge(String.valueOf(Utils.getAgeFromDate(client.get(DBConstants.KEY.DOB))));
//        getProfileView().setProfileGestationAge(client.containsKey(DBConstants.KEY.EDD) ? String.valueOf(Utils.getGestationAgeFromDate(client.get(DBConstants.KEY.EDD))) : null);
//        getProfileView().setProfileID(client.get(DBConstants.KEY.ANC_ID));
//        getProfileView().setProfileImage(client.get(DBConstants.KEY.BASE_ENTITY_ID));
//        getProfileView().setWomanPhoneNumber(client.get(DBConstants.KEY.PHONE_NUMBER));
    }

    @Override
    public void setProfileActivity(ProfileActivity profileActivity) {
        this.profileActivity = profileActivity;
    }
}
