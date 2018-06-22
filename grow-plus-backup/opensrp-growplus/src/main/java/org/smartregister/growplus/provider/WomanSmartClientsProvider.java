package org.smartregister.growplus.provider;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.commons.lang3.StringUtils;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.cursoradapter.SmartRegisterCLientsProviderForCursorAdapter;
import org.smartregister.domain.Alert;
import org.smartregister.growthmonitoring.repository.WeightRepository;
import org.smartregister.immunization.db.VaccineRepo;
import org.smartregister.immunization.domain.Vaccine;
import org.smartregister.immunization.domain.VaccineSchedule;
import org.smartregister.immunization.repository.VaccineRepository;
import org.smartregister.immunization.util.VaccinateActionUtils;
import org.smartregister.growplus.R;
import org.smartregister.growplus.activity.PathJsonFormActivity;
import org.smartregister.growplus.application.VaccinatorApplication;
import org.smartregister.growplus.repository.UniqueIdRepository;
import org.smartregister.growplus.view.LocationPickerView;
import org.smartregister.growplus.wrapper.VaccineViewRecordUpdateWrapper;
import org.smartregister.repository.DetailsRepository;
import org.smartregister.service.AlertService;
import org.smartregister.util.DateUtil;
import org.smartregister.util.FormUtils;
import org.smartregister.util.OpenSRPImageLoader;
import org.smartregister.view.activity.DrishtiApplication;
import org.smartregister.view.contract.SmartRegisterClient;
import org.smartregister.view.contract.SmartRegisterClients;
import org.smartregister.view.dialog.FilterOption;
import org.smartregister.view.dialog.ServiceModeOption;
import org.smartregister.view.dialog.SortOption;
import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import org.smartregister.view.viewholder.OnClickFormLauncher;

import util.JsonFormUtils;
import util.PathConstants;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;
import static org.smartregister.immunization.util.VaccinatorUtils.generateScheduleList;
import static org.smartregister.immunization.util.VaccinatorUtils.nextVaccineDue;

import static org.smartregister.immunization.util.VaccinatorUtils.receivedVaccines;
import static org.smartregister.growplus.activity.WomanSmartRegisterActivity.REQUEST_CODE_GET_JSON;
import static org.smartregister.growplus.fragment.HouseholdMemberAddFragment.DATE_FORMAT;
import static org.smartregister.util.Utils.fillValue;
import static org.smartregister.util.Utils.getValue;
import static org.smartregister.util.Utils.startAsyncTask;

/**
 * Created by Ahmed on 13-Oct-15.
 */
public class WomanSmartClientsProvider implements SmartRegisterCLientsProviderForCursorAdapter {
    private final LayoutInflater inflater;
    private final Context context;
    private final View.OnClickListener onClickListener;
    AlertService alertService;
    VaccineRepository vaccineRepository;
    WeightRepository weightRepository;
    private final AbsListView.LayoutParams clientViewLayoutParams;
    private static final String VACCINES_FILE = "vaccines.json";
    private DetailsRepository detailsRepository = null;

    public WomanSmartClientsProvider(Context context, View.OnClickListener onClickListener,
                                     AlertService alertService, VaccineRepository vaccineRepository, WeightRepository weightRepository) {
        this.onClickListener = onClickListener;
        this.context = context;
        this.alertService = alertService;
        this.vaccineRepository = vaccineRepository;
        this.weightRepository = weightRepository;
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        clientViewLayoutParams = new AbsListView.LayoutParams(MATCH_PARENT, WRAP_CONTENT);
    }

    @Override
    public void getView(Cursor cursor, SmartRegisterClient client, final View convertView) {
        final CommonPersonObjectClient pc = (CommonPersonObjectClient) client;
//        Logger.largeLog("-----------",pc.getDetails().toString());
//        Logger.largeLog("-----------",pc.getColumnmaps().toString());

        String name = getValue(pc.getColumnmaps(), "first_name", false)+ " " + getValue(pc.getColumnmaps(), "last_name", false).replaceAll(Pattern.quote("."),"");
        ((TextView) convertView.findViewById(R.id.name)).setText(name);

        ImageView profileImageIV = (ImageView) convertView.findViewById(R.id.profilepic);
//        if (pc.entityId() != null) {//image already in local storage most likey ):
//            //set profile image by passing the client id.If the image doesn't exist in the image repository then download and save locally
//            profileImageIV.setTag(org.smartregister.R.id.entity_id, pc.entityId());
//            DrishtiApplication.getCachedImageLoaderInstance().getImageByClientId(pc.entityId(), OpenSRPImageLoader.getStaticImageListener((ImageView) profileImageIV, R.drawable.woman_path_register_logo, R.drawable.woman_path_register_logo));
//
//        }


        View profileview = convertView.findViewById(R.id.profile_info_layout);
        profileview.setTag(pc);
        profileview.setOnClickListener(onClickListener);

        String dobString = getValue(pc.getColumnmaps(), "dob", false);
        String durationString = "";
        if (StringUtils.isNotBlank(dobString)) {
            try {
                DateTime birthDateTime = new DateTime(dobString);
                String duration = DateUtil.getDuration(birthDateTime);
                if (duration != null) {
                    durationString = duration;
                }
            } catch (Exception e) {
                Log.e(getClass().getName(), e.toString(), e);
            }
        }
        fillValue((TextView) convertView.findViewById(R.id.age), "age :"+durationString);


        String address1 = getValue(pc.getColumnmaps(), "address1", false);
        fillValue((TextView) convertView.findViewById(R.id.address), address1);

        detailsRepository = detailsRepository == null ? org.smartregister.Context.getInstance().updateApplicationContext(context.getApplicationContext()).detailsRepository() : detailsRepository;
        Map<String, String> detailmaps = detailsRepository.getAllDetailsForClient(pc.entityId());
        pc.getColumnmaps().putAll(detailmaps);
        String husbandname = getValue(detailmaps, "spouseName", false);
        fillValue((TextView) convertView.findViewById(R.id.spousename), husbandname);

        fillValue((TextView) convertView.findViewById(R.id.nid), "LMP: "+getValue(detailmaps, "nationalId", false));

        final String lmpstring = getValue(pc.getColumnmaps(), "lmp", false);
        final String eddstring = getValue(pc.getColumnmaps(), "edd", false);
        final String gastring = getValue(pc.getColumnmaps(), "ultrasound_weeks", false);

        fillValue((TextView) convertView.findViewById(R.id.nid), "LMP: "+lmpstring);
        fillValue((TextView) convertView.findViewById(R.id.zeir_id), "EDD: "+eddstring);
        fillValue((TextView) convertView.findViewById(R.id.brid), "GA: "+gastring);


        fillValue((TextView) convertView.findViewById(R.id.followup_date), getValue(detailmaps, "Date_Of_next_appointment", false));


        Button add_child = (Button)convertView.findViewById(R.id.add_member);
        add_child.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                String metadata = getmetaDataForEditForm(pc);
                Intent intent = new Intent(context, PathJsonFormActivity.class);

                intent.putExtra("json", metadata);

                ((Activity)context).startActivityForResult(intent, REQUEST_CODE_GET_JSON);

            }
        });

        boolean pregnant = false;
        boolean lactating = false;
        if(detailmaps.get("pregnant")!=null){
            if(detailmaps.get("pregnant").equalsIgnoreCase("Yes")){
                pregnant = true;

            }
        }
        if(detailmaps.get("lactating_woman")!=null){
            if(detailmaps.get("lactating_woman").equalsIgnoreCase("Yes")){
                lactating = true;
            }
        }
        if(!pregnant&&!lactating){
            add_child.setText("Follow Up");
            add_child.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {


                    String metadata = getmetaDataForFollowUpForm(pc);
                    Intent intent = new Intent(context, PathJsonFormActivity.class);

                    intent.putExtra("json", metadata);

                    ((Activity)context).startActivityForResult(intent, REQUEST_CODE_GET_JSON);

                }
            });
        }else if(pregnant&&!lactating){
            add_child.setText("Follow Up");

            add_child.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {


                    String metadata = getmetaDataForFollowUpFormPregnantWoman(pc);
                    Intent intent = new Intent(context, PathJsonFormActivity.class);

                    intent.putExtra("json", metadata);

                    ((Activity)context).startActivityForResult(intent, REQUEST_CODE_GET_JSON);

                }
            });
        }else if(lactating){
            add_child.setText("IYCF Counselling");

            add_child.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {


                    String metadata = getmetaDataForLactatingCounsellingForm(pc);
                    Intent intent = new Intent(context, PathJsonFormActivity.class);

                    intent.putExtra("json", metadata);

                    ((Activity)context).startActivityForResult(intent, REQUEST_CODE_GET_JSON);

                }
            });
        }
        if(pregnant){
            profileImageIV.setImageDrawable(context.getResources().getDrawable(R.drawable.pregnant_woman));
        }
        if(lactating){
            profileImageIV.setImageDrawable(context.getResources().getDrawable(R.drawable.lactating_woman));
            fillValue((TextView) convertView.findViewById(R.id.nid), "");
            fillValue((TextView) convertView.findViewById(R.id.zeir_id), "");
            fillValue((TextView) convertView.findViewById(R.id.brid), "");
        }else if(!lactating && !pregnant){
            profileImageIV.setImageDrawable(context.getResources().getDrawable(R.drawable.women));
            fillValue((TextView) convertView.findViewById(R.id.nid), "");
            fillValue((TextView) convertView.findViewById(R.id.zeir_id), "");
            fillValue((TextView) convertView.findViewById(R.id.brid), "");

        }

//        Intent intent = new Intent(context, PathJsonFormActivity.class);
//
//        intent.putExtra("json", metadata);


    }

    @Override
    public SmartRegisterClients updateClients(FilterOption villageFilter, ServiceModeOption
            serviceModeOption, FilterOption searchFilter, SortOption sortOption) {
        throw new UnsupportedOperationException("Operation not supported");
    }

    @Override
    public void onServiceModeSelected(ServiceModeOption serviceModeOption) {

    }

    @Override
    public OnClickFormLauncher newFormLauncher(String formName, String entityId, String
            metaData) {
        throw new UnsupportedOperationException("Operation not supported");
    }

    @Override
    public View inflatelayoutForCursorAdapter() {
        ViewGroup view = (ViewGroup) inflater().inflate(R.layout.smart_register_woman_client, null);
        return view;
    }

    public LayoutInflater inflater() {
        return inflater;
    }



    private class VaccinationAsyncTask extends AsyncTask<Void, Void, Void> {
        private final View convertView;
        private final String entityId;
        private final String dobString;
        private List<Vaccine> vaccines = new ArrayList<>();
        private List<Alert> alerts = new ArrayList<>();
        private SmartRegisterClient client;
        private Cursor cursor;

        private VaccinationAsyncTask(View convertView,
                                     String entityId,
                                     String dobString,
                                     SmartRegisterClient smartRegisterClient,
                                     Cursor cursor) {
            this.convertView = convertView;
            this.entityId = entityId;
            this.dobString = dobString;
            this.client = smartRegisterClient;
            this.cursor = cursor;
        }


        @Override
        protected Void doInBackground(Void... params) {
            if (!TextUtils.isEmpty(dobString)) {
                SimpleDateFormat lmp_DATE_FORMAT = new SimpleDateFormat("dd-MM-yyyy");
                Date dateTime = null;
                try {
                    dateTime = lmp_DATE_FORMAT.parse(dobString);
                    VaccineSchedule.updateOfflineAlerts(entityId, new DateTime(dateTime.getTime()), "woman");
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
            vaccines = vaccineRepository.findByEntityId(entityId);
            alerts = alertService.findByEntityIdAndAlertNames(entityId, VaccinateActionUtils.allAlertNames(PathConstants.KEY.WOMAN));
            return null;
        }

        @Override
        protected void onPostExecute(Void param) {

            VaccineViewRecordUpdateWrapper wrapper = new VaccineViewRecordUpdateWrapper();
            wrapper.setVaccines(vaccines);
            wrapper.setClient(client);
            wrapper.setCursor(cursor);
            wrapper.setAlertList(alerts);
            wrapper.setConvertView(convertView);
            wrapper.setDobString(dobString);
            updateRecordVaccination(wrapper);

        }
    }
    private void updateRecordVaccination(VaccineViewRecordUpdateWrapper updateWrapper) {
        View recordVaccination = updateWrapper.getConvertView().findViewById(R.id.record_vaccination);
        recordVaccination.setVisibility(View.VISIBLE);

        TextView recordVaccinationText = (TextView) updateWrapper.getConvertView().findViewById(R.id.record_vaccination_text);
        ImageView recordVaccinationCheck = (ImageView) updateWrapper.getConvertView().findViewById(R.id.record_vaccination_check);
        recordVaccinationCheck.setVisibility(View.GONE);

        updateWrapper.getConvertView().setLayoutParams(clientViewLayoutParams);

        // Alerts
        Map<String, Date> recievedVaccines = receivedVaccines(updateWrapper.getVaccines());
        Date dateTime = null;
        DateTime dateTimetoSend = null;

        if (!TextUtils.isEmpty(updateWrapper.getDobString())) {
            SimpleDateFormat lmp_DATE_FORMAT = new SimpleDateFormat("dd-MM-yyyy");
            try {
                dateTime = lmp_DATE_FORMAT.parse(updateWrapper.getDobString());
           } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        if(dateTime != null){
            dateTimetoSend = new DateTime(dateTime.getTime());
        }else{
            dateTimetoSend = new DateTime();
        }

        List<Map<String, Object>> sch = generateScheduleList(PathConstants.KEY.WOMAN, dateTimetoSend, recievedVaccines, updateWrapper.getAlertList());

        WomanSmartClientsProvider.State state = WomanSmartClientsProvider.State.FULLY_IMMUNIZED;
        String stateKey = null;

        Map<String, Object> nv = null;
        if (updateWrapper.getVaccines().isEmpty()) {
            List<VaccineRepo.Vaccine> vList = Arrays.asList(VaccineRepo.Vaccine.values());
            nv = nextVaccineDue(sch, vList);
        }

        if (nv == null) {
            Date lastVaccine = null;
            if (!updateWrapper.getVaccines().isEmpty()) {
                Vaccine vaccine = updateWrapper.getVaccines().get(updateWrapper.getVaccines().size() - 1);
                lastVaccine = vaccine.getDate();
            }

            nv = nextVaccineDue(sch, lastVaccine);
        }

        if (nv != null) {
            DateTime dueDate = (DateTime) nv.get(PathConstants.KEY.DATE);
            VaccineRepo.Vaccine vaccine = (VaccineRepo.Vaccine) nv.get(PathConstants.KEY.VACCINE);
            stateKey = VaccinateActionUtils.stateKey(vaccine);
            if (nv.get(PathConstants.KEY.ALERT) == null) {
                state = State.NO_ALERT;
            } else if (((Alert) nv.get(PathConstants.KEY.ALERT)).status().value().equalsIgnoreCase(PathConstants.KEY.NORMAL)) {
                state = State.DUE;
            } else if (((Alert) nv.get(PathConstants.KEY.ALERT)).status().value().equalsIgnoreCase(PathConstants.KEY.UPCOMING)) {
                Calendar today = Calendar.getInstance();
                today.set(Calendar.HOUR_OF_DAY, 0);
                today.set(Calendar.MINUTE, 0);
                today.set(Calendar.SECOND, 0);
                today.set(Calendar.MILLISECOND, 0);

                if (dueDate.getMillis() >= (today.getTimeInMillis() + TimeUnit.MILLISECONDS.convert(1, TimeUnit.DAYS)) && dueDate.getMillis() < (today.getTimeInMillis() + TimeUnit.MILLISECONDS.convert(7, TimeUnit.DAYS))) {
                    state = State.UPCOMING_NEXT_7_DAYS;
                } else {
                    state = State.UPCOMING;
                }
            } else if (((Alert) nv.get(PathConstants.KEY.ALERT)).status().value().equalsIgnoreCase(PathConstants.KEY.URGENT)) {
                state = State.OVERDUE;
            } else if (((Alert) nv.get(PathConstants.KEY.ALERT)).status().value().equalsIgnoreCase(PathConstants.KEY.EXPIRED)) {
                state = State.EXPIRED;
            }
        } else {
            state = State.WAITING;
        }



        if (state.equals(State.FULLY_IMMUNIZED)) {
            recordVaccinationText.setText("Fully\nimmunized");
            recordVaccinationText.setTextColor(context.getResources().getColor(R.color.client_list_grey));

            recordVaccinationCheck.setImageResource(R.drawable.ic_action_check);
            recordVaccinationCheck.setVisibility(View.VISIBLE);

            recordVaccination.setBackgroundColor(context.getResources().getColor(R.color.white));
            recordVaccination.setEnabled(false);

        } else if (state.equals(State.INACTIVE)) {
            recordVaccinationText.setText("Inactive");
            recordVaccinationText.setTextColor(context.getResources().getColor(R.color.client_list_grey));

            recordVaccinationCheck.setImageResource(R.drawable.ic_icon_status_inactive);
            recordVaccinationCheck.setVisibility(View.VISIBLE);

            recordVaccination.setBackgroundColor(context.getResources().getColor(R.color.white));
            recordVaccination.setEnabled(false);


        } else if (state.equals(State.LOST_TO_FOLLOW_UP)) {
            recordVaccinationText.setText("Lost to\nFollow-Up");
            recordVaccinationText.setTextColor(context.getResources().getColor(R.color.client_list_grey));

            recordVaccinationCheck.setImageResource(R.drawable.ic_icon_status_losttofollowup);
            recordVaccinationCheck.setVisibility(View.VISIBLE);

            recordVaccination.setBackgroundColor(context.getResources().getColor(R.color.white));
            recordVaccination.setEnabled(false);

        } else if (state.equals(State.WAITING)) {
            recordVaccinationText.setText("Waiting");
            recordVaccinationText.setTextColor(context.getResources().getColor(R.color.client_list_grey));

            recordVaccination.setBackgroundColor(context.getResources().getColor(R.color.white));
            recordVaccination.setEnabled(false);
        } else if (state.equals(State.EXPIRED)) {
            recordVaccinationText.setText("Expired");
            recordVaccinationText.setTextColor(context.getResources().getColor(R.color.client_list_grey));

            recordVaccination.setBackgroundColor(context.getResources().getColor(R.color.white));
            recordVaccination.setEnabled(false);
        } else if (state.equals(State.UPCOMING)) {
            recordVaccinationText.setText("Due\n" + stateKey);
            recordVaccinationText.setTextColor(context.getResources().getColor(R.color.client_list_grey));

            recordVaccination.setBackgroundColor(context.getResources().getColor(R.color.white));
            recordVaccination.setEnabled(false);
        } else if (state.equals(State.UPCOMING_NEXT_7_DAYS)) {
            recordVaccinationText.setText("Record\n" + stateKey);
            recordVaccinationText.setTextColor(context.getResources().getColor(R.color.client_list_grey));

            recordVaccination.setBackground(context.getResources().getDrawable(R.drawable.due_vaccine_light_blue_bg));
            recordVaccination.setEnabled(true);
        } else if (state.equals(State.DUE)) {
            recordVaccinationText.setText("Record\n" + stateKey);
            recordVaccinationText.setTextColor(context.getResources().getColor(R.color.status_bar_text_almost_white));

            recordVaccination.setBackground(context.getResources().getDrawable(R.drawable.due_vaccine_blue_bg));
            recordVaccination.setEnabled(true);
        } else if (state.equals(State.OVERDUE)) {
            recordVaccinationText.setText("Record\n" + stateKey);
            recordVaccinationText.setTextColor(context.getResources().getColor(R.color.status_bar_text_almost_white));

            recordVaccination.setBackground(context.getResources().getDrawable(R.drawable.due_vaccine_red_bg));
            recordVaccination.setEnabled(true);
        } else if (state.equals(State.NO_ALERT)) {
            if (StringUtils.isNotBlank(stateKey) && (StringUtils.containsIgnoreCase(stateKey, PathConstants.KEY.WEEK) || StringUtils.containsIgnoreCase(stateKey, PathConstants.KEY.MONTH)) && !updateWrapper.getVaccines().isEmpty()) {
                Vaccine vaccine = updateWrapper.getVaccines().isEmpty() ? null : updateWrapper.getVaccines().get(updateWrapper.getVaccines().size() - 1);
                String previousStateKey = VaccinateActionUtils.previousStateKey(PathConstants.KEY.CHILD, vaccine);
                if (previousStateKey != null) {
                    recordVaccinationText.setText(previousStateKey);
                } else {
                    recordVaccinationText.setText(stateKey);
                }
                recordVaccinationCheck.setImageResource(R.drawable.ic_action_check);
                recordVaccinationCheck.setVisibility(View.VISIBLE);
            } else {
                recordVaccinationText.setText("Due\n" + stateKey);
            }
            recordVaccinationText.setTextColor(context.getResources().getColor(R.color.client_list_grey));

            recordVaccination.setBackgroundColor(context.getResources().getColor(R.color.white));
            recordVaccination.setEnabled(false);
        } else {
            recordVaccinationText.setText("");
            recordVaccinationText.setTextColor(context.getResources().getColor(R.color.client_list_grey));

            recordVaccination.setBackgroundColor(context.getResources().getColor(R.color.white));
            recordVaccination.setEnabled(false);
        }


    }
    public enum State {
        DUE,
        OVERDUE,
        UPCOMING_NEXT_7_DAYS,
        UPCOMING,
        INACTIVE,
        LOST_TO_FOLLOW_UP,
        EXPIRED,
        WAITING,
        NO_ALERT,
        FULLY_IMMUNIZED
    }


    private String getmetaDataForEditForm(CommonPersonObjectClient pc) {
        org.smartregister.Context context = VaccinatorApplication.getInstance().context();
        try {
            JSONObject form = FormUtils.getInstance(this.context).getFormJson("child_enrollment");
            LocationPickerView lpv = new LocationPickerView(this.context);
            lpv.init(context);
            JsonFormUtils.addHouseholdRegLocHierarchyQuestions(form, context);
            Log.d("add child form", "Form is " + form.toString());
            if (form != null) {
                JSONObject metaDataJson = form.getJSONObject("metadata");
                JSONObject lookup = metaDataJson.getJSONObject("look_up");
                lookup.put("entity_id", "mother");
                lookup.put("value", pc.entityId());

                UniqueIdRepository uniqueIdRepo = VaccinatorApplication.getInstance().uniqueIdRepository();
                String entityId = uniqueIdRepo.getNextUniqueId() != null ? uniqueIdRepo.getNextUniqueId().getOpenmrsId() : "";
                if (entityId.isEmpty()) {
                    Toast.makeText(context.applicationContext(), context.getInstance().applicationContext().getString(R.string.no_openmrs_id), Toast.LENGTH_SHORT).show();
                }
//
//                JSONObject stepOne = form.getJSONObject(JsonFormUtils.STEP1);
//                JSONArray jsonArray = stepOne.getJSONArray(JsonFormUtils.FIELDS);
//                for (int i = 0; i < jsonArray.length(); i++) {
//                    JSONObject jsonObject = jsonArray.getJSONObject(i);
//                    if (jsonObject.getString(JsonFormUtils.KEY)
//                            .equalsIgnoreCase(JsonFormUtils.OpenMRS_ID)) {
//                        jsonObject.remove(JsonFormUtils.VALUE);
//                        jsonObject.put(JsonFormUtils.VALUE, entityId);
//                        continue;
//                    }
//                }
                String locationid = "";
                DetailsRepository detailsRepository;
                detailsRepository = org.smartregister.Context.getInstance().detailsRepository();
                Map<String, String> details = detailsRepository.getAllDetailsForClient(pc.entityId());
                locationid = JsonFormUtils.getOpenMrsLocationId(context,getValue(details, "address3", false) );

                String birthFacilityHierarchy = JsonFormUtils.getOpenMrsLocationHierarchy(
                        context,locationid ).toString();
                //inject zeir id into the form
                JSONObject stepOne = form.getJSONObject(JsonFormUtils.STEP1);
                JSONArray jsonArray = stepOne.getJSONArray(JsonFormUtils.FIELDS);
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    if (jsonObject.getString(JsonFormUtils.KEY).equalsIgnoreCase(JsonFormUtils.OpenMRS_ID)) {
                        jsonObject.remove(JsonFormUtils.VALUE);
                        jsonObject.put(JsonFormUtils.VALUE, entityId);
                    }
                    if (jsonObject.getString(JsonFormUtils.KEY).equalsIgnoreCase("HIE_FACILITIES")) {
                        jsonObject.put(JsonFormUtils.VALUE, birthFacilityHierarchy);

                    }
                    if (jsonObject.getString(JsonFormUtils.KEY).equalsIgnoreCase("Mother_Guardian_First_Name")) {
                        jsonObject.put(JsonFormUtils.READ_ONLY, true);
                        jsonObject.put(JsonFormUtils.VALUE, (getValue(pc.getDetails(), "first_name", true).isEmpty() ? getValue(pc.getDetails(), "first_name", true) : getValue(pc.getDetails(), "first_name", true)));

                    }
                    if (jsonObject.getString(JsonFormUtils.KEY).equalsIgnoreCase("Mother_Guardian_Last_Name")) {
                        jsonObject.put(JsonFormUtils.READ_ONLY, true);
                        jsonObject.put(JsonFormUtils.VALUE, (getValue(pc.getDetails(), "last_name", true).isEmpty() ? getValue(pc.getDetails(), "last_name", true) : getValue(pc.getDetails(), "last_name", true)));
                    }
                    if (jsonObject.getString(JsonFormUtils.KEY).equalsIgnoreCase("Mother_Guardian_Date_Birth")) {
                        jsonObject.put(JsonFormUtils.READ_ONLY, true);
                        if (!TextUtils.isEmpty(getValue(pc.getDetails(), "dob", true))) {
                            try {
                                DateTime dateTime = new DateTime(getValue(pc.getDetails(), "dob", true));
                                Date dob = dateTime.toDate();
                                Date defaultDate = DATE_FORMAT.parse(JsonFormUtils.MOTHER_DEFAULT_DOB);
                                long timeDiff = Math.abs(dob.getTime() - defaultDate.getTime());
                                if (timeDiff > 86400000) {// Mother's date of birth occurs more than a day from the default date
                                    jsonObject.put(JsonFormUtils.VALUE, DATE_FORMAT.format(dob));
                                }
                            } catch (Exception e) {
                            }
                        }
                    }





                }
//            intent.putExtra("json", form.toString());
//            startActivityForResult(intent, REQUEST_CODE_GET_JSON);
                return form.toString();
            }
        } catch (Exception e) {
            Log.e("exception in addchild", e.getMessage());
        }

        return "";
    }
    private String getmetaDataForLactatingCounsellingForm(CommonPersonObjectClient pc) {
        org.smartregister.Context context = VaccinatorApplication.getInstance().context();
        try {
            JSONObject form = FormUtils.getInstance(this.context).getFormJson("iycf_counselling_form_lactating_woman");

            if (form != null) {


                JSONObject jsonObject = form;
                if (jsonObject.getString(JsonFormUtils.ENTITY_ID) != null) {
                    jsonObject.remove(JsonFormUtils.ENTITY_ID);
                    jsonObject.put(JsonFormUtils.ENTITY_ID, pc.entityId());
                }

//            intent.putExtra("json", form.toString());
//            startActivityForResult(intent, REQUEST_CODE_GET_JSON);
                return form.toString();
            }
        } catch (Exception e) {
            Log.e("exception counselling", e.getMessage());
        }

        return "";
    }

    private String getmetaDataForPregnantCounsellingForm(CommonPersonObjectClient pc) {
        org.smartregister.Context context = VaccinatorApplication.getInstance().context();
        try {
            JSONObject form = FormUtils.getInstance(this.context).getFormJson("iycf_counselling_form_pregnant_woman");

            if (form != null) {


                    JSONObject jsonObject = form;
                    if (jsonObject.getString(JsonFormUtils.ENTITY_ID) != null) {
                        jsonObject.remove(JsonFormUtils.ENTITY_ID);
                        jsonObject.put(JsonFormUtils.ENTITY_ID, pc.entityId());
                    }

//            intent.putExtra("json", form.toString());
//            startActivityForResult(intent, REQUEST_CODE_GET_JSON);
                return form.toString();
            }
        } catch (Exception e) {
            Log.e("exception counselling", e.getMessage());
        }

        return "";
    }
    private String getmetaDataForFollowUpForm(CommonPersonObjectClient pc) {
        org.smartregister.Context context = VaccinatorApplication.getInstance().context();
        try {
            JSONObject form = FormUtils.getInstance(this.context).getFormJson("woman_followup");

            if (form != null) {


                JSONObject jsonObject = form;
                if (jsonObject.getString(JsonFormUtils.ENTITY_ID) != null) {
                    jsonObject.remove(JsonFormUtils.ENTITY_ID);
                    jsonObject.put(JsonFormUtils.ENTITY_ID, pc.entityId());
                }

//            intent.putExtra("json", form.toString());
//            startActivityForResult(intent, REQUEST_CODE_GET_JSON);
                return form.toString();
            }
        } catch (Exception e) {
            Log.e("exception followup", e.getMessage());
        }

        return "";
    }
    private String getmetaDataForFollowUpFormPregnantWoman(CommonPersonObjectClient pc) {
        org.smartregister.Context context = VaccinatorApplication.getInstance().context();
        try {
            JSONObject form = FormUtils.getInstance(this.context).getFormJson("woman_followup");

            if (form != null) {


                JSONObject jsonObject = form;
                if (jsonObject.getString(JsonFormUtils.ENTITY_ID) != null) {
                    jsonObject.remove(JsonFormUtils.ENTITY_ID);
                    jsonObject.put(JsonFormUtils.ENTITY_ID, pc.entityId());
                }
                JSONObject stepOne = form.getJSONObject(JsonFormUtils.STEP1);
                JSONArray jsonArray = stepOne.getJSONArray(JsonFormUtils.FIELDS);
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject fieldjsonObject = jsonArray.getJSONObject(i);
                    if (fieldjsonObject.getString(JsonFormUtils.KEY)
                            .equalsIgnoreCase("is_pregnant")) {
                        fieldjsonObject.remove(JsonFormUtils.VALUE);
                        fieldjsonObject.put(JsonFormUtils.VALUE, "Yes");
                        continue;
                    }
                }

//            intent.putExtra("json", form.toString());
//            startActivityForResult(intent, REQUEST_CODE_GET_JSON);
                return form.toString();
            }
        } catch (Exception e) {
            Log.e("exception followup", e.getMessage());
        }

        return "";
    }
}