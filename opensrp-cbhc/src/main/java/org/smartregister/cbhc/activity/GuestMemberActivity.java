package org.smartregister.cbhc.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.vijay.jsonwizard.activities.JsonFormActivity;
import com.vijay.jsonwizard.constants.JsonFormConstants;
import com.vijay.jsonwizard.domain.Form;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.joda.time.format.FormatUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import org.smartregister.cbhc.R;
import org.smartregister.cbhc.adapter.GuestMemberAdapter;
import org.smartregister.cbhc.application.AncApplication;
import org.smartregister.cbhc.contract.GuestMemberContract;
import org.smartregister.cbhc.contract.ProfileContract;
import org.smartregister.cbhc.contract.RegisterContract;
import org.smartregister.cbhc.domain.AttentionFlag;
import org.smartregister.cbhc.domain.GuestMemberData;
import org.smartregister.cbhc.presenter.GuestMemberPresenter;
import org.smartregister.cbhc.presenter.RegisterPresenter;
import org.smartregister.cbhc.repository.UniqueIdRepository;
import org.smartregister.cbhc.util.Constants;
import org.smartregister.cbhc.util.CustomFormUtils;
import org.smartregister.cbhc.util.DBConstants;
import org.smartregister.cbhc.util.JsonFormUtils;
import org.smartregister.cbhc.util.LookUpUtils;
import org.smartregister.cbhc.util.Utils;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.domain.FetchStatus;
import org.smartregister.util.FormUtils;

import java.util.List;

import timber.log.Timber;

public class GuestMemberActivity extends AppCompatActivity implements GuestMemberContract.View,View.OnClickListener,ProfileContract.View, RegisterContract.View {
    private RecyclerView recyclerView;
    private RecyclerView navRecyclerView;
    private ProgressBar progressBar;
    private GuestMemberAdapter adapter;
    private Spinner ssSpinner;
    private GuestMemberPresenter presenter;
    RegisterPresenter registerPresenter;
    private String ssName="";
    private String query ="";
    private EditText editTextSearch;

    public static void startGuestMemberProfileActivity(Activity activity , String baseEntityId){
        Intent intent = new Intent(activity,GuestMemberActivity.class);
        //intent.putExtra(BASE_ENTITY_ID,baseEntityId);
        activity.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guest_member);

        presenter = new GuestMemberPresenter(this);
        presenter.fetchData();

        editTextSearch = findViewById(R.id.search_edit_text);
        recyclerView = findViewById(R.id.guest_recycler_view);
        progressBar = findViewById(R.id.progress_bar);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        findViewById(R.id.fab_add_member).setOnClickListener(this);

        registerPresenter = new RegisterPresenter(GuestMemberActivity.this);

        fetchProfileData();

        editTextSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                query = s.toString();
                filterData();

            }
        });

        if(adapter == null){
            adapter = new GuestMemberAdapter(this, new GuestMemberAdapter.OnClickAdapter() {
                @Override
                public void onClick(int position, GuestMemberData content) {
                    openProfile(content.getBaseEntity());
                }
            });

            adapter.setData(getPresenter().getData());
            recyclerView.setAdapter(adapter);
        }else{
            adapter.setData(getPresenter().getData());
            adapter.notifyDataSetChanged();
        }
    }

    private void logOutDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Alert!")
                .setMessage("Are you sure to log out?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();;
                    }
                }).show();

    }

    private void filterData() {
        presenter.filterData(query,ssName);

    }
    protected void initializePresenter() {
        presenter = new GuestMemberPresenter(this);
        fetchProfileData();
        fetchMemberData();
    }


    @Override
    protected void onResume() {
        super.onResume();
        Log.d("tttt","onresume called");
        presenter.fetchData();
    }

    private void fetchMemberData() {
        presenter.fetchMemberData();
    }

    protected ViewPager setupViewPager(ViewPager viewPager) {
        return null;
    }



    protected void fetchProfileData() {
        presenter.fetchData();
    }


    @Override
    public void showProgressBar() {

    }

    @Override
    public void hideProgressBar() {

    }

    @Override
    public void updateAdapter() {
        if(adapter == null){
            adapter = new GuestMemberAdapter(this, new GuestMemberAdapter.OnClickAdapter() {
                @Override
                public void onClick(int position, GuestMemberData content) {
                    openProfile(content.getBaseEntity());
                }
            });

            adapter.setData(getPresenter().getData());
            recyclerView.setAdapter(adapter);
        }else{
            adapter.setData(getPresenter().getData());
            adapter.notifyDataSetChanged();
        }
    }

    private void openProfile(String baseEntityId){

        GuestMemberActivity.startGuestMemberProfileActivity(this,baseEntityId);

    }

    @Override
    public void updateSuccessfullyFetchMessage() {
        presenter.fetchData();

    }

    @Override
    public GuestMemberContract.Presenter getPresenter() {
        return  presenter;
    }

    @Override
    public Context getContext() {
        return getApplicationContext();
    }

    @Override
    public void displaySyncNotification() {

    }

    @Override
    public void updateNavAdapter() {
/*if(guestNavAdapter == null){
            guestNavAdapter = new GuestNavAdapter(context());
            guestNavAdapter.setData(getPresenter().getNAvData());
            navRecyclerView.setAdapter(guestNavAdapter);
        }else{
            guestNavAdapter.setData(getPresenter().getNAvData());
            guestNavAdapter.notifyDataSetChanged();
        }*/

    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.fab_add_member:

                String entityId = null;
                try {
                    JSONObject form = FormUtils.getInstance(this).getFormJson("guest_member_register");

                    if(form!=null){

                        if (StringUtils.isBlank(entityId)) {
                            UniqueIdRepository uniqueIdRepo = AncApplication.getInstance().getUniqueIdRepository();
                            entityId = uniqueIdRepo.getNextUniqueId() != null ? uniqueIdRepo.getNextUniqueId().getOpenmrsId() : "";
                            if (entityId.isEmpty()) {
                                Toast.makeText(this, this.getString(R.string.no_openmrs_id), Toast.LENGTH_SHORT).show();
                                return;
                            }
                        }
                        JSONObject stepOne = form.getJSONObject(JsonFormUtils.STEP1);
                        JSONArray jsonArray = stepOne.getJSONArray(JsonFormUtils.FIELDS);
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            if (jsonObject.getString(JsonFormUtils.KEY)
                                    .equalsIgnoreCase("Patient_Identifier")) {
                                jsonObject.remove(JsonFormUtils.VALUE);
                                jsonObject.put(JsonFormUtils.VALUE, entityId);
                                continue;
                            }
                        }

                        Intent intent = new Intent(this,AncJsonFormActivity.class);


                        intent.putExtra("json", form.toString());
                        startActivityForResult(intent,  JsonFormUtils.REQUEST_CODE_GET_JSON);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
        }
    }
    protected FormUtils getFormUtils() {

        FormUtils formUtils=null;
        try {
            formUtils = FormUtils.getInstance(getApplicationContext());
        } catch (Exception e) {
            //Timber.e(e);
        }
        return formUtils;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == JsonFormUtils.REQUEST_CODE_GET_JSON && resultCode == RESULT_OK) {
            try {
                String jsonString = data.getStringExtra("json");
                registerPresenter.saveForm(jsonString,false);

            } catch (Exception e) {
            }
        }
    }

    @Override
    public void setProfileName(String fullName) {

    }

    @Override
    public void setProfileID(String ancId) {

    }

    @Override
    public void setProfileAge(String age) {

    }

    @Override
    public void setProfileGestationAge(String gestationAge) {

    }

    @Override
    public void setProfileImage(String baseEntityId) {

    }

    @Override
    public void showProgressDialog(int messageStringIdentifier) {

    }

    @Override
    public void hideProgressDialog() {

    }

    @Override
    public void showAttentionFlagsDialog(List<AttentionFlag> attentionFlags) {

    }

    @Override
    public void updateInitialsText(String initials) {

    }

    @Override
    public void displayToast(int resourceId) {

    }

    @Override
    public void displayToast(String message) {

    }

    @Override
    public void displayShortToast(int resourceId) {

    }

    @Override
    public void showLanguageDialog(List<String> displayValues) {

    }

    @Override
    public String getIntentString(String intentKey) {
        return null;
    }

    @Override
    public void setWomanPhoneNumber(String phoneNumber) {

    }

    @Override
    public void startFormActivity(JSONObject form) {
        try {
            Intent intent = new Intent(this, AncJsonFormActivity.class);
            intent.putExtra("json", form.toString());
            startActivityForResult(intent, JsonFormUtils.REQUEST_CODE_GET_JSON);

        } catch (Exception e) {
            Utils.appendLog(getClass().getName(),e);

        }
    }

    @Override
    public void refreshList(FetchStatus fetchStatus) {
        presenter.fetchData();
    }

    @Override
    public ProfileContract.View getView() {
        return this;
    }

    public void back(View view) {
        finish();
    }
}
