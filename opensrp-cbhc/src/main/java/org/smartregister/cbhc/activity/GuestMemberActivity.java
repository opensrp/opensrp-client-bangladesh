/*
package org.smartregister.cbhc.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.smartregister.growplus.R;
import org.smartregister.growplus.adapter.GuestMemberAdapter;
import org.smartregister.growplus.application.VaccinatorApplication;
import org.smartregister.growplus.contract.GuestMemberContract;
import org.smartregister.growplus.domain.GuestMemberData;
import org.smartregister.growplus.presenter.GuestMemberPresenter;
import org.smartregister.growplus.repository.UniqueIdRepository;
import org.smartregister.util.FormUtils;

import util.JsonFormUtils;

public class GuestMemberActivity extends AppCompatActivity implements GuestMemberContract.View,View.OnClickListener {
    private RecyclerView recyclerView;
    private RecyclerView navRecyclerView;
    private ProgressBar progressBar;
    private GuestMemberAdapter adapter;
    private Spinner ssSpinner;
    private GuestMemberPresenter presenter;
    private String ssName="";
    private String query ="";
    private EditText editTextSearch;
    private DrawerLayout drawer;
    private NavigationView navigationView;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    //private AppBarConfiguration mAppBarConfiguration;
    private Toolbar toolbar;
    private LinearLayout logoutLay;

    public static void startGuestMemberProfileActivity(Activity activity , String baseEntityId){
        Intent intent = new Intent(activity,GuestMemberActivity.class);
        //intent.putExtra(BASE_ENTITY_ID,baseEntityId);
        activity.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guest_member);

        //drawer = (DrawerLayout) findViewById(R.id.drawer_lay_guest);
        //navigationView = (NavigationView) findViewById(R.id.nav_bar_guest);

       // actionBarDrawerToggle = new ActionBarDrawerToggle(this,drawer,R.string.app_name,R.string.app_name);
       // drawer.addDrawerListener(actionBarDrawerToggle);
       // actionBarDrawerToggle.syncState();
        presenter = new GuestMemberPresenter(this);
        presenter.fetchData();

        editTextSearch = findViewById(R.id.search_edit_text);
        recyclerView = findViewById(R.id.guest_recycler_view);
        //navRecyclerView = findViewById(R.id.nav_recycler_view_guest);
        progressBar = findViewById(R.id.progress_bar);
       // logoutLay = findViewById(R.id.log_out_lay);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        //navRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        findViewById(R.id.fab_add_member).setOnClickListener(this);

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

       */
/* logoutLay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                logOutDialog();
            }
        });*//*


        if(adapter == null){
            adapter = new GuestMemberAdapter(this, new GuestMemberAdapter.OnClickAdapter() {
                @Override
                public void onClick(int position, GuestMemberData content) {
                    openProfile(content.getBaseEntityId());
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
*/
/*
    @Override
    protected void initializePresenter() {
        presenter = new GuestMemberPresenter(this);
        fetchProfileData();
        fetchMemberData();
    }*//*


    @Override
    protected void onResume() {
        super.onResume();
        presenter.fetchData();
        fetchMemberData();
    }

    private void fetchMemberData() {
        presenter.fetchMemberData();
    }

 */
/*   @Override
    protected ViewPager setupViewPager(ViewPager viewPager) {
        return null;
    }*//*


   */
/* @Override
    protected void fetchProfileData() {
        presenter.fetchData();
    }*//*


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
                    openProfile(content.getBaseEntityId());
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
    public void updateNavAdapter() {
        */
/*if(guestNavAdapter == null){
            guestNavAdapter = new GuestNavAdapter(context());
            guestNavAdapter.setData(getPresenter().getNAvData());
            navRecyclerView.setAdapter(guestNavAdapter);
        }else{
            guestNavAdapter.setData(getPresenter().getNAvData());
            guestNavAdapter.notifyDataSetChanged();
        }*//*

    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            */
/*case R.id.backBtn:
                finish();
                break;*//*

            case R.id.fab_add_member:
                */
/*HnppConstants.getGPSLocation(this, new OnPostDataWithGps() {
                    @Override
                    public void onPost(double latitude, double longitude) {
                        try{
                            Intent intent = new Intent(GuestMemberActivity.this, GuestAddMemberJsonFormActivity.class);
                            JSONObject jsonForm = FormUtils.getInstance(GuestMemberActivity.this).getFormJson(HnppConstants.JSON_FORMS.GUEST_MEMBER_FORM);
                            HnppJsonFormUtils.updateFormWithSSName(jsonForm, SSLocationHelper.getInstance().getSsModels());
                            HnppJsonFormUtils.updateLatitudeLongitude(jsonForm,latitude,longitude);
                            intent.putExtra(Constants.JSON_FORM_EXTRA.JSON, jsonForm.toString());
                            Form form = new Form();
                            form.setWizard(false);
                            if(!HnppConstants.isReleaseBuild()){
                                form.setActionBarBackground(R.color.test_app_color);

                            }else{
                                form.setActionBarBackground(org.smartregister.family.R.color.customAppThemeBlue);

                            }

                            intent.putExtra(JsonFormConstants.JSON_FORM_KEY.FORM, form);

                            startActivityForResult(intent, Constants.REQUEST_CODE_GET_JSON);
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                });*//*


                String entityId = null;
                try {
                    JSONObject form = FormUtils.getInstance(this).getFormJson("guest_member_register");

                    if(form!=null){

                        if (StringUtils.isBlank(entityId)) {
                            UniqueIdRepository uniqueIdRepo = VaccinatorApplication.getInstance().uniqueIdRepository();
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
                                    .equalsIgnoreCase(JsonFormUtils.OpenMRS_ID)) {
                                jsonObject.remove(JsonFormUtils.VALUE);
                                jsonObject.put(JsonFormUtils.VALUE, entityId);
                                continue;
                            }
                        }

                        Intent intent = new Intent(this, PathJsonFormActivity.class);


                        intent.putExtra("json", form.toString());
                        startActivityForResult(intent, */
/*Constant.REQUEST_CODE_GET_JSON*//*
3432);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
        }
    }
    protected FormUtils getFormUtils() {
       */
/* if (formUtils == null) {
            try {
                formUtils = FormUtils.getInstance(Utils.context().applicationContext());
            } catch (Exception e) {
                Timber.e(e);
            }
        }*//*

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
        if (requestCode == 3432 && resultCode == RESULT_OK) {
            try {
                String jsonString = data.getStringExtra("json");
                presenter.saveMember(jsonString);


            } catch (Exception e) {
            }
        }
    }

    public void opeDrawerBtClick(View view) {
        drawer.openDrawer(Gravity.LEFT);
    }

}
*/
