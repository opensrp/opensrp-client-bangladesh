package org.smartregister.cbhc.activity;

import android.app.DatePickerDialog;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import com.mobsandgeeks.saripaar.ValidationError;
import com.mobsandgeeks.saripaar.Validator;
import com.mobsandgeeks.saripaar.annotation.NotEmpty;

import java.time.LocalDateTime;

import org.joda.time.DateTime;
import org.smartregister.cbhc.R;
import org.smartregister.cbhc.application.AncApplication;
import org.smartregister.cbhc.domain.CampaignForm;
import org.smartregister.cbhc.repository.CampaignRepository;
import org.smartregister.cbhc.repository.FollowupRepository;

import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class AddCampaignActivity extends AppCompatActivity implements Validator.ValidationListener {
    @NotEmpty
    private TextInputEditText campaign_name_et,campaign_type_et,target_et;
    private Button add_campaign_bt;
    private Calendar calendar;
    private Validator validator;
    private CampaignRepository campaignRepository;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_campaign);
        getSupportActionBar().setTitle("Add Campaign");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        initView();

        calendar = Calendar.getInstance();
        validator = new Validator(this);
        validator.setValidationListener(this);

        campaignRepository = new CampaignRepository(AncApplication.getInstance().getRepository());

        target_et.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openDatePicker();
            }
        });

        add_campaign_bt.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View view) {
                validator.validate();
            }
        });

    }

    /**
     * adding campaign to sqlite db
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void campaignAddOperations() {
        String base_entity_id = UUID.randomUUID().toString();
       DateTimeFormatter dateFormat =DateTimeFormatter.ofPattern("dd/MM/yyyy");
       Date date = new Date();
        Toast.makeText(this, "date    "+date, Toast.LENGTH_SHORT).show();
        long status = campaignRepository.saveData(new CampaignForm(campaign_name_et.getText().toString(),campaign_type_et.getText().toString(),base_entity_id,target_et.getText().toString(), date,date));
        if(status>=0){
            Toast.makeText(AddCampaignActivity.this, "Campaign Added Successfully", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    /**
     * date picker dialog
     */
    private void openDatePicker() {
        DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                calendar.set(Calendar.YEAR, year);
                calendar.set(Calendar.MONTH, monthOfYear);
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                target_et.setText(new SimpleDateFormat("dd/MM/yyyy").format(calendar.getTime()));
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
     * saripaar validation success listeners
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onValidationSucceeded() {
        campaignAddOperations();
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