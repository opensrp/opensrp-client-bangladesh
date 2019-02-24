package org.smartregister.cbhc.contract;

import android.app.Activity;
import android.content.Intent;

import org.apache.commons.lang3.tuple.Triple;
import org.json.JSONObject;
import org.smartregister.cbhc.activity.BaseProfileActivity;
import org.smartregister.cbhc.activity.ProfileActivity;
import org.smartregister.repository.AllSharedPreferences;

import java.util.Map;

/**
 * Created by ndegwamartin on 13/07/2018.
 */
public interface ProfileContract {

    interface Presenter {

        ProfileContract.View getProfileView();

        void onDestroy(boolean isChangingConfiguration);

        void refreshProfileView(String baseEntityId);

        void processFormDetailsSave(Intent data, AllSharedPreferences allSharedPreferences);

        void refreshProfileTopSection(Map<String, String> client);
        void saveForm(String jsonString, boolean isEditMode);
        void setProfileActivity(Activity profileActivity);
        void startMemberRegistrationForm(String formName, String entityId, String metadata, String currentLocationId,String householdID) throws Exception;
    }

    interface View {

        void setProfileName(String fullName);

        void setProfileID(String ancId);

        void setProfileAge(String age);

        void setProfileGestationAge(String gestationAge);

        void setProfileImage(String baseEntityId);

        void showProgressDialog(int messageStringIdentifier);

        void hideProgressDialog();

        void displayToast(int resourceId);

        String getIntentString(String intentKey);

        void setWomanPhoneNumber(String phoneNumber);
        void startFormActivity(JSONObject form);
        ProfileContract.View getView();

    }

    interface Interactor {

        void onDestroy(boolean isChangingConfiguration);

        void refreshProfileView(String baseEntityId);
    }
}
