package org.smartregister.growplus.activity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.mobsandgeeks.saripaar.ValidationError;
import com.mobsandgeeks.saripaar.Validator;
import com.mobsandgeeks.saripaar.annotation.NotEmpty;

import org.smartregister.growplus.R;
import org.smartregister.growplus.adapter.ScheduleListTvAdapter;
import org.smartregister.growplus.application.VaccinatorApplication;
import org.smartregister.growplus.domain.CampaignForm;
import org.smartregister.growplus.domain.ScheduleData;
import org.smartregister.growplus.repository.CampaignRepository;
import org.smartregister.growplus.repository.ScheduleRepository;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class AddCampaignActivity extends AppCompatActivity implements Validator.ValidationListener {
    @NotEmpty
    private TextInputEditText campaign_name_et,campaign_type_et,target_et;
    private Button add_campaign_bt;
    private RecyclerView schedule_list_rv;
    private Calendar calendar;
    private Validator validator;
    private CampaignRepository campaignRepository;
    private ScheduleRepository scheduleRepository;
    private Date targetDate;
    private CampaignForm campaignForm;
    private String type;
    private ArrayList<ScheduleData> scheduleDataArrayList;
    private TextInputEditText dialog_target_et;


    @SuppressLint("SimpleDateFormat")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_campaign);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        calendar = Calendar.getInstance();

        campaignRepository = new CampaignRepository(VaccinatorApplication.getInstance().getRepository());
        scheduleRepository = new ScheduleRepository(VaccinatorApplication.getInstance().getRepository());

        validator = new Validator(this);
        validator.setValidationListener(this);
        scheduleDataArrayList = new ArrayList<>();

        initView();

        type = getIntent().getStringExtra("from");

        //update view according to view type
        if(type.equals("update")){
            getSupportActionBar().setTitle("Update Campaign");
            add_campaign_bt.setText(R.string.update);
            campaignForm = (CampaignForm) getIntent().getSerializableExtra("content");

            targetDate = campaignForm.getTargetDate();
            target_et.setText(new SimpleDateFormat("dd/MM/yyyy").format(targetDate));
            campaign_name_et.setText(campaignForm.getName());
            campaign_type_et.setText(campaignForm.getType());
            setupScheduleList(campaignForm.getBaseEntityId());

        }else{
            getSupportActionBar().setTitle("Add Campaign");
            add_campaign_bt.setText(R.string.add);
        }

        //target edittext click listeners
        target_et.setOnClickListener(view -> openDatePicker("add",targetDate));

        //schedule add and update button listeners
        add_campaign_bt.setOnClickListener(view -> validator.validate());


    }

    /**
     * adding campaign to sqlite db
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void campaignAddOperations() {
        String base_entity_id = UUID.randomUUID().toString();
       Date date = new Date();
        long status = campaignRepository.saveData(new CampaignForm(Objects.requireNonNull(campaign_name_et.getText()).toString(),campaign_type_et.getText().toString(),base_entity_id,targetDate, date,date));
        if(status>=0){
            addSchedule(base_entity_id);
            finish();
        }
    }

    /**
     * schedule add logic
     * @param base_entity_id
     */
    private void addSchedule(String base_entity_id) {
        Date date = new Date();
        Calendar scheduleCalendar = calendar;
        scheduleRepository.removeScheduleFromToday(calendar.getTime(),base_entity_id);
        for(int i=0;i<(52*7);i+=28){
            scheduleCalendar.add(Calendar.DATE,28);
            scheduleRepository.saveData(new ScheduleData(base_entity_id,calendar.getTime(), date,date,"true"));
        }
        Toast.makeText(AddCampaignActivity.this, "Campaign Added Successfully", Toast.LENGTH_SHORT).show();
    }


    /**
     * schedule list setup
     * @param baseEntityId
     */
    private void setupScheduleList(String baseEntityId) {
        scheduleDataArrayList = scheduleRepository.getAllScheduleData(baseEntityId);
        schedule_list_rv.setLayoutManager(new LinearLayoutManager(this));
        schedule_list_rv.setAdapter(new ScheduleListTvAdapter(this, scheduleDataArrayList, new ScheduleListTvAdapter.OnClickAdapter() {
            @Override
            public void onClick(int position, ScheduleData content) {
                showUpdateScheduleDialog(content.getTargetDate());
            }
        }));
    }


    /**
     * specific schedule update dialog
     * @param targetDt
     */
    @SuppressLint("DefaultLocale")
    private void showUpdateScheduleDialog(Date targetDt) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(targetDt);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_update_schedule,null);
        AlertDialog dialog = new AlertDialog.Builder(this).create();
        dialog.setView(dialogView);
        dialog_target_et = dialogView.findViewById(R.id.target_date_et);

        dialog_target_et.setText(String.format("%d/%d/%d",
                calendar.get(Calendar.DAY_OF_MONTH),
                calendar.get(Calendar.MONTH) + 1,
                calendar.get(Calendar.YEAR)));
        dialog_target_et.setOnClickListener(view -> {
            Toast.makeText(AddCampaignActivity.this, "clicked", Toast.LENGTH_SHORT).show();
            openDatePicker("dialog",targetDt);
        });

        dialogView.findViewById(R.id.cancel_bt)
                .setOnClickListener(view -> dialog.dismiss());
        dialog.show();
    }

    /**
     * date picker dialog
     */
    private void openDatePicker(String from,Date targetDt) {
        if(targetDt!=null){
            calendar.setTime(targetDt);
        }

        DatePickerDialog.OnDateSetListener date = (view, year, monthOfYear, dayOfMonth) -> {
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, monthOfYear);
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            if(from.equals("add")){
                target_et.setText(new SimpleDateFormat("dd/MM/yyyy").format(calendar.getTime()));
                targetDate = calendar.getTime();
            }else if(from.equals("dialog")){
                dialog_target_et.setText(new SimpleDateFormat("dd/MM/yyyy").format(calendar.getTime()));
            }
        };

        new DatePickerDialog(AddCampaignActivity.this, date, calendar
                .get(Calendar.YEAR), calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)).show();

    }

    /**
     * all view initialization
     */
    private void initView() {
        campaign_name_et = findViewById(R.id.campaign_name_et);
        campaign_type_et = findViewById(R.id.campaign_type_et);
        target_et = findViewById(R.id.target_et);

        add_campaign_bt = findViewById(R.id.add_campaign_bt);

        schedule_list_rv = findViewById(R.id.schedule_list_rv);
    }

    /**
     * top back arrow button handling
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * campaign update operation
     */
    private void campaignUpdateOperation() {
        Date date = new Date();
        long status = campaignRepository.updateData(new CampaignForm(Objects.requireNonNull(campaign_name_et.getText()).toString(),campaign_type_et.getText().toString(),campaignForm.getBaseEntityId(),targetDate, date,date));
        if(status>=0){
            addSchedule(campaignForm.getBaseEntityId());
            Toast.makeText(AddCampaignActivity.this, "Successfully Campaign Updated", Toast.LENGTH_SHORT).show();
            finish();
        }
    }


    /**
     * saripaar validation success listeners
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onValidationSucceeded() {
        if(type.equals("add")){
            campaignAddOperations();
        }else{
            campaignUpdateOperation();
        }

    }

    /**
     * saripaar validation failed listeners
     * @param errors
     */
    @Override
    public void onValidationFailed(List<ValidationError> errors) {
        for (ValidationError error : errors) {
            View view = error.getView();
            String message = error.getCollatedErrorMessage(this);

            if (view instanceof EditText) {
                ((EditText) view).setError(message);
            } else {
                Toast.makeText(this, message, Toast.LENGTH_LONG).show();
            }
        }
    }
    }