package org.smartregister.cbhc.activity;

import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.MenuItem;

import org.smartregister.cbhc.application.AncApplication;
import org.smartregister.cbhc.domain.draft_form_object;
import org.smartregister.cbhc.fragment.AdvancedSearchFragment;
import org.smartregister.cbhc.fragment.BaseRegisterFragment;
import org.smartregister.cbhc.fragment.DraftFormSelectorFragment;
import org.smartregister.cbhc.fragment.HomeRegisterFragment;
import org.smartregister.cbhc.fragment.LibraryFragment;
import org.smartregister.cbhc.fragment.MeFragment;
import org.smartregister.cbhc.fragment.SortFilterFragment;
import org.smartregister.cbhc.presenter.RegisterPresenter;
import org.smartregister.cbhc.repository.DraftFormRepository;
import org.smartregister.cbhc.util.Constants;
import org.smartregister.cbhc.util.DBConstants;
import org.smartregister.cbhc.util.Utils;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.configurableviews.model.Field;

import java.util.Arrays;
import java.util.List;

/**
 * Created by keyman on 26/06/2018.
 */

public class HomeRegisterActivity extends BaseRegisterActivity {

    public static final int ADVANCED_SEARCH_POSITION = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
        } catch (Exception e) {
            Utils.appendLog(getClass().getName(), e);
            Log.e(TAG, e.getMessage());
        }

//        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
//        if(preferences.getString(Constants.LOCATION_UPDATED,null)==null){
//            startActivity(new Intent(this,BlocksDialog.class));
//        }

    }

    @Override
    public BaseRegisterFragment getRegisterFragment() {
        return new HomeRegisterFragment();
    }

    @Override
    protected Fragment[] getOtherFragments() {
        return new Fragment[]{new AdvancedSearchFragment(), new SortFilterFragment(), new MeFragment(), new LibraryFragment()};
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return superOnOptionsItemsSelected(item);

    }

    @Override
    protected void initializePresenter() {
        presenter = new RegisterPresenter(this);
    }

    @Override
    public List<String> getViewIdentifiers() {
        return Arrays.asList(Constants.CONFIGURATION.HOME_REGISTER);
    }

    protected boolean superOnOptionsItemsSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    public void updateSortAndFilter(List<Field> filterList, Field sortField,String camp_type) {
        mBaseFragment.updateSortAndFilter(filterList, sortField,camp_type);
        switchToBaseFragment();
    }

    public void clearFilter() {
        mBaseFragment.clearSortAndFilter();
        switchToBaseFragment();
    }

    public void startAdvancedSearch() {
        try {
            mPager.setCurrentItem(ADVANCED_SEARCH_POSITION, false);
        } catch (Exception e) {
            Utils.appendLog(getClass().getName(), e);
            e.printStackTrace();
        }

    }

    @Override
    public void startRegistration() {
        List<draft_form_object> draftFormObjects = checkForDraft();
        if (draftFormObjects.size() > 0) {
            try {
//                JSONObject form = new JSONObject(draftFormObjects.get(0).getDraftFormJson());
//                startFormActivity(form);

                FragmentTransaction ft = ((HomeRegisterActivity) getContext()).getFragmentManager().beginTransaction();
                DraftFormSelectorFragment draftFormSelectorFragment = DraftFormSelectorFragment.newInstance();
                draftFormSelectorFragment.setContext(getContext());
                draftFormSelectorFragment.setDraftForms(draftFormObjects);
                draftFormSelectorFragment.show(((HomeRegisterActivity) getContext()).getFragmentManager(), DIALOG_TAG);


            } catch (Exception e) {
                Utils.appendLog(getClass().getName(), e);
                e.printStackTrace();
            }
        } else {
            startFormActivity(Constants.JSON_FORM.Household_REGISTER, null, null);
        }
    }

    public List<draft_form_object> checkForDraft() {
        DraftFormRepository draftFormRepository = new DraftFormRepository(AncApplication.getInstance().getRepository());
        List<draft_form_object> draftFormObjects = draftFormRepository.findUnusedDraftWithoutEntityID(0);
        return draftFormObjects;
    }

    public List<draft_form_object> checkForDraftWithEntityId(String entityID) {
        DraftFormRepository draftFormRepository = new DraftFormRepository(AncApplication.getInstance().getRepository());
        List<draft_form_object> draftFormObjects = draftFormRepository.findByEntityId(entityID);
        return draftFormObjects;
    }

    @Override
    public void showRecordBirthPopUp(CommonPersonObjectClient client) {
        getIntent().putExtra(Constants.INTENT_KEY.BASE_ENTITY_ID, client.getColumnmaps().get(DBConstants.KEY.BASE_ENTITY_ID));

        List<draft_form_object> draftFormObjects = checkForDraftWithEntityId(client.getColumnmaps().get(DBConstants.KEY.BASE_ENTITY_ID));
        if (draftFormObjects.size() > 0) {
            try {
//                JSONObject form = new JSONObject(draftFormObjects.get(0).getDraftFormJson());
//                startFormActivity(form);

                FragmentTransaction ft = ((HomeRegisterActivity) getContext()).getFragmentManager().beginTransaction();
                DraftFormSelectorFragment draftFormSelectorFragment = DraftFormSelectorFragment.newInstance();
                draftFormSelectorFragment.setContext(getContext());
                draftFormSelectorFragment.setFamilyBaseEntityId(client.getColumnmaps().get(DBConstants.KEY.BASE_ENTITY_ID));
                draftFormSelectorFragment.setDraftForms(draftFormObjects);
                draftFormSelectorFragment.setCampType(client.getColumnmaps().get(DBConstants.KEY.CHAMP_TYPE));
                draftFormSelectorFragment.show(((HomeRegisterActivity) getContext()).getFragmentManager(), DIALOG_TAG);


            } catch (Exception e) {
                Utils.appendLog(getClass().getName(), e);
                e.printStackTrace();
            }
        } else {
            //
            startMemberRegistrationForm(client.getColumnmaps().get(DBConstants.KEY.BASE_ENTITY_ID));

        }

    }

    public void startMemberRegistrationForm(String householdEntityID) {
        try {
            getPresenter().startMemberRegistrationForm(Constants.JSON_FORM.MEMBER_REGISTER, null, null, null, householdEntityID);
        } catch (Exception e) {
            Utils.appendLog(getClass().getName(), e);
            e.printStackTrace();
        }
    }
    public void startMemberRegistrationForm(String householdEntityID,String campType) {
        try {
            getPresenter().startMemberRegistrationForm(Constants.JSON_FORM.MEMBER_REGISTER, null, null, null, householdEntityID,campType);
        } catch (Exception e) {
            Utils.appendLog(getClass().getName(), e);
            e.printStackTrace();
        }
    }

    @Override
    public void onBackPressed() {


        android.support.v7.app.AlertDialog alertDialog = new android.support.v7.app.AlertDialog.Builder(this).create();
        alertDialog.setTitle("Do you want to exit?");
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.setButton(android.support.v7.app.AlertDialog.BUTTON_NEGATIVE, "NO",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.setButton(android.support.v7.app.AlertDialog.BUTTON_POSITIVE, "EXIT",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            finish();


                        } catch (Exception e) {
                            Utils.appendLog(getClass().getName(), e);
                            e.printStackTrace();
                        }
                    }
                });
        alertDialog.show();
    }


}
