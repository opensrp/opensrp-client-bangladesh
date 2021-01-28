package org.smartregister.growplus.provider;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONObject;
import org.opensrp.api.constants.Gender;
import org.smartregister.commonregistry.CommonPersonObject;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.commonregistry.CommonRepository;
import org.smartregister.cursoradapter.SmartRegisterCLientsProviderForCursorAdapter;
import org.smartregister.domain.Alert;
import org.smartregister.growplus.activity.PathJsonFormActivity;
import org.smartregister.growplus.application.VaccinatorApplication;
import org.smartregister.growplus.fragment.HouseholdMemberAddFragment;
import org.smartregister.growplus.repository.UniqueIdRepository;
import org.smartregister.growplus.view.LocationPickerView;
import org.smartregister.growthmonitoring.domain.Weight;
import org.smartregister.growthmonitoring.repository.WeightRepository;
import org.smartregister.immunization.db.VaccineRepo;
import org.smartregister.immunization.domain.Vaccine;
import org.smartregister.immunization.repository.VaccineRepository;
import org.smartregister.immunization.util.VaccinateActionUtils;
import org.smartregister.growplus.R;
import org.smartregister.growplus.fragment.AdvancedSearchFragment;
import org.smartregister.growplus.wrapper.VaccineViewRecordUpdateWrapper;
import org.smartregister.growplus.wrapper.WeightViewRecordUpdateWrapper;
import org.smartregister.repository.DetailsRepository;
import org.smartregister.service.AlertService;
import org.smartregister.util.DateUtil;
import org.smartregister.util.FormUtils;
import org.smartregister.util.OpenSRPImageLoader;
import org.smartregister.util.Utils;
import org.smartregister.view.activity.DrishtiApplication;
import org.smartregister.view.contract.SmartRegisterClient;
import org.smartregister.view.contract.SmartRegisterClients;
import org.smartregister.view.dialog.FilterOption;
import org.smartregister.view.dialog.ServiceModeOption;
import org.smartregister.view.dialog.SortOption;
import org.smartregister.view.viewholder.OnClickFormLauncher;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import util.ImageUtils;
import util.JsonFormUtils;
import util.PathConstants;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static com.vijay.jsonwizard.utils.FormUtils.DATE_FORMAT;
import static org.smartregister.growplus.activity.LoginActivity.getOpenSRPContext;
import static org.smartregister.growplus.activity.WomanSmartRegisterActivity.REQUEST_CODE_GET_JSON;
import static org.smartregister.growplus.fragment.GrowthFalteringTrendReportFragment.readAllWeights;
import static org.smartregister.immunization.util.VaccinatorUtils.generateScheduleList;
import static org.smartregister.immunization.util.VaccinatorUtils.nextVaccineDue;
import static org.smartregister.immunization.util.VaccinatorUtils.receivedVaccines;
import static org.smartregister.util.Utils.fillValue;
import static org.smartregister.util.Utils.getName;
import static org.smartregister.util.Utils.getValue;

/**
 * Created by Ahmed on 13-Oct-15.
 */
public class ChildSmartClientsProvider implements SmartRegisterCLientsProviderForCursorAdapter {
    private final LayoutInflater inflater;
    private final Context context;
    private final View.OnClickListener onClickListener;
    private final AlertService alertService;
    private final VaccineRepository vaccineRepository;
    private final WeightRepository weightRepository;
    private final AbsListView.LayoutParams clientViewLayoutParams;
    private final CommonRepository commonRepository;

    public ChildSmartClientsProvider(Context context, View.OnClickListener onClickListener,
                                     AlertService alertService, VaccineRepository vaccineRepository, WeightRepository weightRepository, CommonRepository commonRepository) {
        this.onClickListener = onClickListener;
        this.context = context;
        this.alertService = alertService;
        this.vaccineRepository = vaccineRepository;
        this.weightRepository = weightRepository;
        this.commonRepository = commonRepository;
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        clientViewLayoutParams = new AbsListView.LayoutParams(MATCH_PARENT, (int) context.getResources().getDimension(org.smartregister.R.dimen.list_item_height));
    }

    @Override
    public void getView(Cursor cursor, SmartRegisterClient client, final View convertView) {
        final CommonPersonObjectClient pc = (CommonPersonObjectClient) client;

//        fillValue((TextView) convertView.findViewById(R.id.child_zeir_id), getValue(pc.getColumnmaps(), PathConstants.KEY.ZEIR_ID, false));

        String firstName = getValue(pc.getColumnmaps(), PathConstants.KEY.FIRST_NAME, true);
        String lastName = getValue(pc.getColumnmaps(), PathConstants.KEY.LAST_NAME, true).replaceAll(Pattern.quote("."),"");
        String childName = getName(firstName, lastName);

        String motherFirstName = getValue(pc.getColumnmaps(), PathConstants.KEY.MOTHER_FIRST_NAME, true);
        if (StringUtils.isBlank(childName) && StringUtils.isNotBlank(motherFirstName)) {
            childName = "B/o " + motherFirstName.trim();
        }
        fillValue((TextView) convertView.findViewById(R.id.child_name), childName);

        String motherName = getValue(pc.getColumnmaps(), PathConstants.KEY.MOTHER_FIRST_NAME, true).replaceAll(Pattern.quote("."),"") + " " + getValue(pc, PathConstants.KEY.MOTHER_LAST_NAME, true).replaceAll(Pattern.quote("."),"");
        if (!StringUtils.isNotBlank(motherName)) {
            motherName = "M/G: " + motherName.trim();
        }
        fillValue((TextView) convertView.findViewById(R.id.child_mothername), motherName);

        DateTime birthDateTime = null;
        String dobString = getValue(pc.getColumnmaps(), PathConstants.KEY.DOB, false);
        String durationString = "";
        if (StringUtils.isNotBlank(dobString)) {
            try {
                birthDateTime = new DateTime(dobString);
                String duration = DateUtil.getDuration(birthDateTime);
                if (duration != null) {
                    durationString = duration;
                }
            } catch (Exception e) {
                Log.e(getClass().getName(), e.toString(), e);
            }
        }
        fillValue((TextView) convertView.findViewById(R.id.child_age), durationString);

//        fillValue((TextView) convertView.findViewById(R.id.child_card_number), pc.getColumnmaps(), PathConstants.KEY.EPI_CARD_NUMBER, false);

        String gender = getValue(pc.getColumnmaps(), PathConstants.KEY.GENDER, true);



        final ImageView profilePic = (ImageView) convertView.findViewById(R.id.child_profilepic);
        int defaultImageResId = ImageUtils.profileImageResourceByGender(gender);
        profilePic.setImageResource(defaultImageResId);

        convertView.findViewById(R.id.child_profile_info_layout).setTag(client);
        convertView.findViewById(R.id.child_profile_info_layout).setOnClickListener(onClickListener);

        View recordWeight = convertView.findViewById(R.id.record_weight);
        recordWeight.setBackground(context.getResources().getDrawable(R.drawable.record_weight_bg));
        recordWeight.setTag(client);
        TextView record_weight_text = recordWeight.findViewById(R.id.record_weight_text);
//        recordWeight.setOnClickListener(onClickListener);
//        recordWeight.setVisibility(View.INVISIBLE);
        record_weight_text.setText("Follow\nUp");
        record_weight_text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                String metadata = getmetaDataForEditForm(pc);
                Intent intent = new Intent(context, PathJsonFormActivity.class);

                intent.putExtra("json", metadata);

                ((Activity)context).startActivityForResult(intent, REQUEST_CODE_GET_JSON);

            }
        });


//        View recordVaccination = convertView.findViewById(R.id.record_vaccination);
//        recordVaccination.setTag(client);
//        recordVaccination.setOnClickListener(onClickListener);
//        recordVaccination.setVisibility(View.INVISIBLE);

        String lostToFollowUp = getValue(pc.getColumnmaps(), PathConstants.KEY.LOST_TO_FOLLOW_UP, false);
        String inactive = getValue(pc.getColumnmaps(), PathConstants.KEY.INACTIVE, false);

        try {
            //Utils.startAsyncTask(new WeightAsyncTask(convertView, pc.entityId(), lostToFollowUp, inactive, client, cursor), null);
//            Utils.startAsyncTask(new VaccinationAsyncTask(convertView, pc.entityId(), dobString, lostToFollowUp, inactive, client, cursor), null);
        } catch (Exception e) {
            Log.e(getClass().getName(), e.getMessage(), e);
        }
        convertView.findViewById(R.id.weightbox).setBackgroundColor(getOpenSRPContext().getColorResource(R.color.status_bar_text_almost_white));
        ((TextView) convertView.findViewById(R.id.weightcaptured)).setTextColor(getOpenSRPContext().getColorResource(R.color.client_list_grey));
        ((TextView) convertView.findViewById(R.id.weightcaptureddate)).setTextColor(getOpenSRPContext().getColorResource(R.color.client_list_grey));

        List<Weight> weightlist = weightRepository.findLast5(pc.entityId());
        if(weightlist.size() >= 1) {

           Double Zscore = weightlist.get(0).getZScore();

            boolean adequate = checkForWeightGainCalc(birthDateTime.toDate(), Gender.valueOf(gender.toUpperCase()), weightlist.get(0), pc, getOpenSRPContext().detailsRepository());
            if (!adequate) {
                convertView.findViewById(R.id.weightbox).setBackgroundColor(getOpenSRPContext().getColorResource(R.color.weightred));
                ((TextView)convertView.findViewById(R.id.weightcaptured)).setTextColor(getOpenSRPContext().getColorResource(R.color.status_bar_text_almost_white));
                ((TextView)convertView.findViewById(R.id.weightcaptureddate)).setTextColor(getOpenSRPContext().getColorResource(R.color.status_bar_text_almost_white));

            }else{
                convertView.findViewById(R.id.weightbox).setBackgroundColor(getOpenSRPContext().getColorResource(R.color.weightgreen));
                ((TextView)convertView.findViewById(R.id.weightcaptured)).setTextColor(getOpenSRPContext().getColorResource(R.color.status_bar_text_almost_white));
                ((TextView)convertView.findViewById(R.id.weightcaptureddate)).setTextColor(getOpenSRPContext().getColorResource(R.color.status_bar_text_almost_white));

            }

            if(Zscore != null) {
                if (Zscore <= -3.0) {
                    convertView.findViewById(R.id.weightbox).setBackgroundColor(getOpenSRPContext().getColorResource(R.color.alert_urgent_red));
                    ((TextView) convertView.findViewById(R.id.weightcaptured)).setTextColor(getOpenSRPContext().getColorResource(R.color.status_bar_text_almost_white));
                    ((TextView) convertView.findViewById(R.id.weightcaptureddate)).setTextColor(getOpenSRPContext().getColorResource(R.color.status_bar_text_almost_white));

                }
            }

            ((TextView)convertView.findViewById(R.id.weightcaptured)).setText(weightlist.get(0).getKg()+"Kg");
            ((TextView)convertView.findViewById(R.id.weightcaptureddate)).setText(Utils.convertDateFormat(new DateTime(weightlist.get(0).getDate().getTime())));

        }else{
            Map<String, String> detailsMap =  getOpenSRPContext().detailsRepository().getAllDetailsForClient(pc.entityId());
            Float birthweight = new Float(getValue(detailsMap, "Birth_Weight", true));

            ((TextView)convertView.findViewById(R.id.weightcaptured)).setText(birthweight+"Kg");
            ((TextView)convertView.findViewById(R.id.weightcaptureddate)).setText(dobString);
            ((TextView)convertView.findViewById(R.id.weightcaptured)).setTextColor(getOpenSRPContext().getColorResource(R.color.client_list_grey));
            ((TextView)convertView.findViewById(R.id.weightcaptureddate)).setTextColor(getOpenSRPContext().getColorResource(R.color.client_list_grey));


        }
    }
    private String getmetaDataForEditForm(CommonPersonObjectClient pc) {
        org.smartregister.Context context = VaccinatorApplication.getInstance().context();
        try {
            JSONObject form = FormUtils.getInstance(this.context).getFormJson("child_followup");
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
                                Date defaultDate = HouseholdMemberAddFragment.DATE_FORMAT.parse(JsonFormUtils.MOTHER_DEFAULT_DOB);
                                long timeDiff = Math.abs(dob.getTime() - defaultDate.getTime());
                                if (timeDiff > 86400000) {// Mother's date of birth occurs more than a day from the default date
                                    jsonObject.put(JsonFormUtils.VALUE, HouseholdMemberAddFragment.DATE_FORMAT.format(dob));
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
    private void updateRecordWeight(WeightViewRecordUpdateWrapper updateWrapper) {

        View recordWeight = updateWrapper.getConvertView().findViewById(R.id.record_weight);
        recordWeight.setVisibility(View.VISIBLE);

        if (updateWrapper.getWeight() != null) {
            TextView recordWeightText = (TextView) updateWrapper.getConvertView().findViewById(R.id.record_weight_text);
            recordWeightText.setText(Utils.kgStringSuffix(updateWrapper.getWeight().getKg()));

            ImageView recordWeightCheck = (ImageView) updateWrapper.getConvertView().findViewById(R.id.record_weight_check);
            recordWeightCheck.setVisibility(View.VISIBLE);

            recordWeight.setClickable(false);
            recordWeight.setBackground(new ColorDrawable(context.getResources()
                    .getColor(android.R.color.transparent)));
        } else {
            TextView recordWeightText = (TextView) updateWrapper.getConvertView().findViewById(R.id.record_weight_text);
            recordWeightText.setText(context.getString(R.string.record_weight_with_nl));

            ImageView recordWeightCheck = (ImageView) updateWrapper.getConvertView().findViewById(R.id.record_weight_check);
            recordWeightCheck.setVisibility(View.GONE);
            recordWeight.setClickable(true);
        }

        // Update active/inactive/lostToFollowup status
        if (updateWrapper.getLostToFollowUp().equals(Boolean.TRUE.toString()) || updateWrapper.getInactive().equals(Boolean.TRUE.toString())) {
            recordWeight.setVisibility(View.INVISIBLE);
        }

        //Update Out of Catchment
        if (updateWrapper.getCursor() instanceof AdvancedSearchFragment.AdvancedMatrixCursor) {
            updateViews(updateWrapper.getConvertView(), updateWrapper.getClient(), true);
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

        List<Map<String, Object>> sch = generateScheduleList(PathConstants.KEY.CHILD, new DateTime(updateWrapper.getDobString()), recievedVaccines, updateWrapper.getAlertList());

        State state = State.FULLY_IMMUNIZED;
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


        // Update active/inactive/lostToFollowup status
        if (updateWrapper.getLostToFollowUp().equals(Boolean.TRUE.toString())) {
            state = State.LOST_TO_FOLLOW_UP;
        }

        if (updateWrapper.getInactive().equals(Boolean.TRUE.toString())) {
            state = State.INACTIVE;
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

        //Update Out of Catchment
        if (updateWrapper.getCursor() instanceof AdvancedSearchFragment.AdvancedMatrixCursor) {
            updateViews(updateWrapper.getConvertView(), updateWrapper.getClient(), false);
        }
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
        return inflater().inflate(R.layout.smart_register_child_client, null);
    }

    public LayoutInflater inflater() {
        return inflater;
    }

    public void updateViews(View convertView, SmartRegisterClient client, boolean isWeightRecord) {

        CommonPersonObjectClient pc = (CommonPersonObjectClient) client;

        if (commonRepository != null) {
            CommonPersonObject commonPersonObject = commonRepository.findByBaseEntityId(pc.entityId());

            View recordVaccination = convertView.findViewById(R.id.record_vaccination);
            recordVaccination.setVisibility(View.VISIBLE);

            View moveToCatchment = convertView.findViewById(R.id.move_to_catchment);
            moveToCatchment.setVisibility(View.GONE);

            if (commonPersonObject == null) { //Out of area -- doesn't exist in local database
                if (isWeightRecord) {
                    TextView recordWeightText = (TextView) convertView.findViewById(R.id.record_weight_text);
                    recordWeightText.setText("Record\nservice");

                    String zeirId = getValue(pc.getColumnmaps(), PathConstants.KEY.ZEIR_ID, false);

                    View recordWeight = convertView.findViewById(R.id.record_weight);
                    recordWeight.setBackground(context.getResources().getDrawable(R.drawable.record_weight_bg));
                    recordWeight.setTag(zeirId);
                    recordWeight.setClickable(true);
                    recordWeight.setEnabled(true);
                    recordWeight.setOnClickListener(onClickListener);
                } else {

                    TextView moveToCatchmentText = (TextView) convertView.findViewById(R.id.move_to_catchment_text);
                    moveToCatchmentText.setText("Move to my\ncatchment");

                    String motherBaseEntityId = getValue(pc.getColumnmaps(), PathConstants.KEY.MOTHER_BASE_ENTITY_ID, false);
                    String entityId = pc.entityId();

                    List<String> ids = new ArrayList<>();
                    ids.add(motherBaseEntityId);
                    ids.add(entityId);

                    moveToCatchment.setBackground(context.getResources().getDrawable(R.drawable.record_weight_bg));
                    moveToCatchment.setTag(ids);
                    moveToCatchment.setClickable(true);
                    moveToCatchment.setEnabled(true);
                    moveToCatchment.setOnClickListener(onClickListener);

                    moveToCatchment.setVisibility(View.VISIBLE);
                    recordVaccination.setVisibility(View.GONE);
                }
            }

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

    private class WeightAsyncTask extends AsyncTask<Void, Void, Void> {
        private final View convertView;
        private final String entityId;
        private final String lostToFollowUp;
        private final String inactive;
        private Weight weight;
        private SmartRegisterClient client;
        private Cursor cursor;

        private WeightAsyncTask(View convertView,
                                String entityId,
                                String lostToFollowUp,
                                String inactive,
                                SmartRegisterClient smartRegisterClient,
                                Cursor cursor) {
            this.convertView = convertView;
            this.entityId = entityId;
            this.lostToFollowUp = lostToFollowUp;
            this.inactive = inactive;
            this.client = smartRegisterClient;
            this.cursor = cursor;
        }


        @Override
        protected Void doInBackground(Void... params) {
            weight = weightRepository.findUnSyncedByEntityId(entityId);
            return null;
        }

        @Override
        protected void onPostExecute(Void param) {
            WeightViewRecordUpdateWrapper wrapper = new WeightViewRecordUpdateWrapper();
            wrapper.setWeight(weight);
            wrapper.setLostToFollowUp(lostToFollowUp);
            wrapper.setInactive(inactive);
            wrapper.setClient(client);
            wrapper.setCursor(cursor);
            wrapper.setConvertView(convertView);
            updateRecordWeight(wrapper);

        }
    }

    private class VaccinationAsyncTask extends AsyncTask<Void, Void, Void> {
        private final View convertView;
        private final String entityId;
        private final String dobString;
        private final String lostToFollowUp;
        private final String inactive;
        private List<Vaccine> vaccines = new ArrayList<>();
        private List<Alert> alerts = new ArrayList<>();
        private SmartRegisterClient client;
        private Cursor cursor;

        private VaccinationAsyncTask(View convertView,
                                     String entityId,
                                     String dobString,
                                     String lostToFollowUp,
                                     String inactive,
                                     SmartRegisterClient smartRegisterClient,
                                     Cursor cursor) {
            this.convertView = convertView;
            this.entityId = entityId;
            this.dobString = dobString;
            this.lostToFollowUp = lostToFollowUp;
            this.inactive = inactive;
            this.client = smartRegisterClient;
            this.cursor = cursor;
        }


        @Override
        protected Void doInBackground(Void... params) {
            vaccines = vaccineRepository.findByEntityId(entityId);
            alerts = alertService.findByEntityIdAndAlertNames(entityId, VaccinateActionUtils.allAlertNames(PathConstants.KEY.CHILD));
            return null;
        }

        @Override
        protected void onPostExecute(Void param) {

            VaccineViewRecordUpdateWrapper wrapper = new VaccineViewRecordUpdateWrapper();
            wrapper.setVaccines(vaccines);
            wrapper.setLostToFollowUp(lostToFollowUp);
            wrapper.setInactive(inactive);
            wrapper.setClient(client);
            wrapper.setCursor(cursor);
            wrapper.setAlertList(alerts);
            wrapper.setConvertView(convertView);
            wrapper.setDobString(dobString);
            updateRecordVaccination(wrapper);

        }
    }


    public static boolean checkForWeightGainCalc(Date dob, Gender gender, Weight weight, CommonPersonObjectClient childDetails, DetailsRepository detailsRepository) {
        String dobString = "";
        String formattedAge = "";
        String formattedDob = "";

        WeightRepository wp = VaccinatorApplication.getInstance().weightRepository();
        List<Weight> weightlist = wp.findLast5(childDetails.entityId());



        formattedDob = DATE_FORMAT.format(dob);
        long timeDiff = Calendar.getInstance().getTimeInMillis() - dob.getTime();

        int age_in_months = (int) Math.floor((float) timeDiff /
                TimeUnit.MILLISECONDS.convert(30, TimeUnit.DAYS));
        DateTime tempweighttime = null;
        Map<String, String> detailsMap = detailsRepository.getAllDetailsForClient(childDetails.entityId());
        Float birthweight = new Float(getValue(detailsMap, "Birth_Weight", true));
        Weight previouseWeight = new Weight();
        previouseWeight.setKg(birthweight);
        previouseWeight.setDate(dob);
        int monthLastWeightTaken = 0;


        CommonRepository commonRepository = VaccinatorApplication.getInstance().context().commonrepository("ec_child");
        Cursor cursor =  commonRepository.rawCustomQueryForAdapter("SELECT * FROM weights where date( date / 1000, 'unixepoch') < date( "+weight.getDate().getTime()+"/1000,'unixepoch') and base_entity_id = '"+weight.getBaseEntityId()+"' group by base_entity_id having date = max(date)");
        List <Weight> previousweightlist = readAllWeights(cursor);

        if(previousweightlist.size()>0){
            previouseWeight = previousweightlist.get(0);
            long timeDiffwhenLastWeightwastaken =  previouseWeight.getDate().getTime() - dob.getTime();
            monthLastWeightTaken = (int) Math.round((float) timeDiffwhenLastWeightwastaken /
                    TimeUnit.MILLISECONDS.convert(30, TimeUnit.DAYS));

        }


        long timeDiffwhenWeightwastaken =  weight.getDate().getTime() - dob.getTime();

       /////////////////////////month last weight was taken calculation needs fixing /////////////////

        int age_when_weight_taken = (int) Math.round((float) timeDiffwhenWeightwastaken /
                TimeUnit.MILLISECONDS.convert(30, TimeUnit.DAYS));
        //////////////////////calculation fix///////////////
        ////////////////////////////////////////////////////////////////
        boolean check = checkWeighGainVelocity(weight,previouseWeight,age_when_weight_taken,monthLastWeightTaken,gender);
        return check;
//        net.sqlcipher.database.SQLiteDatabase db = wp.getPathRepository().getReadableDatabase();


    }



    public static boolean checkWeighGainVelocity(Weight weight, Weight previouseWeight, int age_when_weight_taken, int monthLastWeightTaken, Gender gender) {
        boolean check = true;
        Float weightDifference = weight.getKg() - previouseWeight.getKg();
        Float weightDifferenceInGrams = weightDifference*1000;
        if(age_when_weight_taken == 1 && monthLastWeightTaken == 0 ){
            if(gender.MALE == gender){
                if(weightDifferenceInGrams< 805){
                    check = false;
                }
            }else if (gender.FEMALE == gender){
                if(weightDifferenceInGrams< 697){
                    check = false;
                }
            }
        }
        if(age_when_weight_taken == 2 && monthLastWeightTaken == 0 ){
            if(gender.MALE == gender){
                if(weightDifferenceInGrams< 1890){
                    check = false;
                }
            }else if (gender.FEMALE == gender){
                if(weightDifferenceInGrams< 1604){
                    check = false;
                }
            }
        }
        if(age_when_weight_taken == 2 && monthLastWeightTaken == 1 ){
            if(gender.MALE == gender){
                if(weightDifferenceInGrams< 992){
                    check = false;
                }
            }else if (gender.FEMALE == gender){
                if(weightDifferenceInGrams< 829){
                    check = false;
                }
            }
        }
        if(age_when_weight_taken == 3 && monthLastWeightTaken == 0 ){
            if(gender.MALE == gender){
                if(weightDifferenceInGrams< 2608){
                    check = false;
                }
            }else if (gender.FEMALE == gender){
                if(weightDifferenceInGrams< 2247){
                    check = false;
                }
            }
        }
        if(age_when_weight_taken == 3 && monthLastWeightTaken == 1 ){
            if(gender.MALE == gender){
                if(weightDifferenceInGrams< 1701){
                    check = false;
                }
            }else if (gender.FEMALE == gender){
                if(weightDifferenceInGrams< 1450){
                    check = false;
                }
            }
        }
        if(age_when_weight_taken == 3 && monthLastWeightTaken == 2 ){
            if(gender.MALE == gender){
                if(weightDifferenceInGrams< 658){
                    check = false;
                }
            }else if (gender.FEMALE == gender){
                if(weightDifferenceInGrams< 571){
                    check = false;
                }
            }
        }
        if(age_when_weight_taken == 4 && monthLastWeightTaken == 0 ){
            if(gender.MALE == gender){
                if(weightDifferenceInGrams< 3204){
                    check = false;
                }
            }else if (gender.FEMALE == gender){
                if(weightDifferenceInGrams< 2806){
                    check = false;
                }
            }
        }
        if(age_when_weight_taken == 4 && monthLastWeightTaken == 1 ){
            if(gender.MALE == gender){
                if(weightDifferenceInGrams< 2214){
                    check = false;
                }
            }else if (gender.FEMALE == gender){
                if(weightDifferenceInGrams< 1941){
                    check = false;
                }
            }
        }
        if(age_when_weight_taken == 4 && monthLastWeightTaken == 2 ){
            if(gender.MALE == gender){
                if(weightDifferenceInGrams< 1202){
                    check = false;
                }
            }else if (gender.FEMALE == gender){
                if(weightDifferenceInGrams< 1088){
                    check = false;
                }
            }
        }
        if(age_when_weight_taken == 4 && monthLastWeightTaken == 3 ){
            if(gender.MALE == gender){
                if(weightDifferenceInGrams< 476){
                    check = false;
                }
            }else if (gender.FEMALE == gender){
                if(weightDifferenceInGrams< 448){
                    check = false;
                }
            }
        }
        if(age_when_weight_taken == 5 && monthLastWeightTaken <= 1 ){
            if(gender.MALE == gender){
                if(weightDifferenceInGrams< 2706){
                    check = false;
                }
            }else if (gender.FEMALE == gender){
                if(weightDifferenceInGrams< 2379){
                    check = false;
                }
            }
        }
        if(age_when_weight_taken == 5 && monthLastWeightTaken == 2 ){
            if(gender.MALE == gender){
                if(weightDifferenceInGrams< 1702){
                    check = false;
                }
            }else if (gender.FEMALE == gender){
                if(weightDifferenceInGrams< 1545){
                    check = false;
                }
            }
        }
        if(age_when_weight_taken == 5 && monthLastWeightTaken == 3 ){
            if(gender.MALE == gender){
                if(weightDifferenceInGrams< 930){
                    check = false;
                }
            }else if (gender.FEMALE == gender){
                if(weightDifferenceInGrams< 874){
                    check = false;
                }
            }
        }
        if(age_when_weight_taken == 5 && monthLastWeightTaken == 4 ){
            if(gender.MALE == gender){
                if(weightDifferenceInGrams< 383){
                    check = false;
                }
            }else if (gender.FEMALE == gender){
                if(weightDifferenceInGrams< 355){
                    check = false;
                }
            }
        }
        if(age_when_weight_taken == 6 && monthLastWeightTaken == 0 ){
            if(gender.MALE == gender){
                if(weightDifferenceInGrams< 4072){
                    check = false;
                }
            }else if (gender.FEMALE == gender){
                if(weightDifferenceInGrams< 3620){
                    check = false;
                }
            }
        }
        if(age_when_weight_taken == 6 && monthLastWeightTaken == 2 ){
            if(gender.MALE == gender){
                if(weightDifferenceInGrams< 2038){
                    check = false;
                }
            }else if (gender.FEMALE == gender){
                if(weightDifferenceInGrams< 1883){
                    check = false;
                }
            }
        }
        if(age_when_weight_taken == 6 && monthLastWeightTaken == 3 ){
            if(gender.MALE == gender){
                if(weightDifferenceInGrams< 1307){
                    check = false;
                }
            }else if (gender.FEMALE == gender){
                if(weightDifferenceInGrams< 1229){
                    check = false;
                }
            }
        }
        if(age_when_weight_taken == 6 && monthLastWeightTaken == 4 ){
            if(gender.MALE == gender){
                if(weightDifferenceInGrams< 738){
                    check = false;
                }
            }else if (gender.FEMALE == gender){
                if(weightDifferenceInGrams< 695){
                    check = false;
                }
            }
        }
        if(age_when_weight_taken == 6 && monthLastWeightTaken == 5 ){
            if(gender.MALE == gender){
                if(weightDifferenceInGrams< 287){
                    check = false;
                }
            }else if (gender.FEMALE == gender){
                if(weightDifferenceInGrams< 271){
                    check = false;
                }
            }
        }
        if(age_when_weight_taken == 7 && monthLastWeightTaken == 6 ){
            if(gender.MALE == gender){
                if(weightDifferenceInGrams< 223){
                    check = false;
                }
            }else if (gender.FEMALE == gender){
                if(weightDifferenceInGrams< 214){
                    check = false;
                }
            }
        }
        if(age_when_weight_taken == 7 && monthLastWeightTaken == 5 ){
            if(gender.MALE == gender){
                if(weightDifferenceInGrams< 585){
                    check = false;
                }
            }else if (gender.FEMALE == gender){
                if(weightDifferenceInGrams< 560){
                    check = false;
                }
            }
        }
        if(age_when_weight_taken == 7 && monthLastWeightTaken == 4 ){
            if(gender.MALE == gender){
                if(weightDifferenceInGrams< 1038){
                    check = false;
                }
            }else if (gender.FEMALE == gender){
                if(weightDifferenceInGrams< 995){
                    check = false;
                }
            }
        }
        if(age_when_weight_taken == 7 && monthLastWeightTaken == 3 ){
            if(gender.MALE == gender){
                if(weightDifferenceInGrams< 1602){
                    check = false;
                }
            }else if (gender.FEMALE == gender){
                if(weightDifferenceInGrams< 1522){
                    check = false;
                }
            }
        }
        if(age_when_weight_taken == 7 && monthLastWeightTaken == 1 ){
            if(gender.MALE == gender){
                if(weightDifferenceInGrams< 3406){
                    check = false;
                }
            }else if (gender.FEMALE == gender){
                if(weightDifferenceInGrams< 3033){
                    check = false;
                }
            }
        }
        if(age_when_weight_taken == 8 && monthLastWeightTaken == 7 ){
            if(gender.MALE == gender){
                if(weightDifferenceInGrams< 181){
                    check = false;
                }
            }else if (gender.FEMALE == gender){
                if(weightDifferenceInGrams< 178){
                    check = false;
                }
            }
        }
        if(age_when_weight_taken == 8 && monthLastWeightTaken == 6 ){
            if(gender.MALE == gender){
                if(weightDifferenceInGrams< 486){
                    check = false;
                }
            }else if (gender.FEMALE == gender){
                if(weightDifferenceInGrams< 469){
                    check = false;
                }
            }
        }
        if(age_when_weight_taken == 8 && monthLastWeightTaken == 5 ){
            if(gender.MALE == gender){
                if(weightDifferenceInGrams< 859){
                    check = false;
                }
            }else if (gender.FEMALE == gender){
                if(weightDifferenceInGrams< 825){
                    check = false;
                }
            }
        }
        if(age_when_weight_taken == 8 && monthLastWeightTaken == 4 ){
            if(gender.MALE == gender){
                if(weightDifferenceInGrams< 1312){
                    check = false;
                }
            }else if (gender.FEMALE == gender){
                if(weightDifferenceInGrams< 1248){
                    check = false;
                }
            }
        }
        if(age_when_weight_taken == 8 && monthLastWeightTaken == 2 ){
            if(gender.MALE == gender){
                if(weightDifferenceInGrams< 2662){
                    check = false;
                }
            }else if (gender.FEMALE == gender){
                if(weightDifferenceInGrams< 2480){
                    check = false;
                }
            }
        }
        if(age_when_weight_taken == 9 && monthLastWeightTaken == 8 ){
            if(gender.MALE == gender){
                if(weightDifferenceInGrams< 148){
                    check = false;
                }
            }else if (gender.FEMALE == gender){
                if(weightDifferenceInGrams< 139){
                    check = false;
                }
            }
        }
        if(age_when_weight_taken == 9 && monthLastWeightTaken == 7 ){
            if(gender.MALE == gender){
                if(weightDifferenceInGrams< 417){
                    check = false;
                }
            }else if (gender.FEMALE == gender){
                if(weightDifferenceInGrams< 399){
                    check = false;
                }
            }
        }
        if(age_when_weight_taken == 9 && monthLastWeightTaken == 6 ){
            if(gender.MALE == gender){
                if(weightDifferenceInGrams< 733){
                    check = false;
                }
            }else if (gender.FEMALE == gender){
                if(weightDifferenceInGrams< 697){
                    check = false;
                }
            }
        }
        if(age_when_weight_taken == 9 && monthLastWeightTaken == 5 ){
            if(gender.MALE == gender){
                if(weightDifferenceInGrams< 1097){
                    check = false;
                }
            }else if (gender.FEMALE == gender){
                if(weightDifferenceInGrams< 1042){
                    check = false;
                }
            }
        }
        if(age_when_weight_taken == 9 && monthLastWeightTaken == 3 ){
            if(gender.MALE == gender){
                if(weightDifferenceInGrams< 2141){
                    check = false;
                }
            }else if (gender.FEMALE == gender){
                if(weightDifferenceInGrams< 2030){
                    check = false;
                }
            }
        }
        if(age_when_weight_taken == 10 && monthLastWeightTaken == 9 ){
            if(gender.MALE == gender){
                if(weightDifferenceInGrams< 120){
                    check = false;
                }
            }else if (gender.FEMALE == gender){
                if(weightDifferenceInGrams< 110){
                    check = false;
                }
            }
        }
        if(age_when_weight_taken == 10 && monthLastWeightTaken == 8 ){
            if(gender.MALE == gender){
                if(weightDifferenceInGrams< 360){
                    check = false;
                }
            }else if (gender.FEMALE == gender){
                if(weightDifferenceInGrams< 336){
                    check = false;
                }
            }
        }
        if(age_when_weight_taken == 10 && monthLastWeightTaken == 7 ){
            if(gender.MALE == gender){
                if(weightDifferenceInGrams< 639){
                    check = false;
                }
            }else if (gender.FEMALE == gender){
                if(weightDifferenceInGrams< 598){
                    check = false;
                }
            }
        }
        if(age_when_weight_taken == 10 && monthLastWeightTaken == 6 ){
            if(gender.MALE == gender){
                if(weightDifferenceInGrams< 945){
                    check = false;
                }
            }else if (gender.FEMALE == gender){
                if(weightDifferenceInGrams< 891){
                    check = false;
                }
            }
        }
        if(age_when_weight_taken == 10 && monthLastWeightTaken == 4 ){
            if(gender.MALE == gender){
                if(weightDifferenceInGrams< 1789){
                    check = false;
                }
            }else if (gender.FEMALE == gender){
                if(weightDifferenceInGrams< 1692){
                    check = false;
                }
            }
        }
        if(age_when_weight_taken == 11 && monthLastWeightTaken == 10 ){
            if(gender.MALE == gender){
                if(weightDifferenceInGrams< 100){
                    check = false;
                }
            }else if (gender.FEMALE == gender){
                if(weightDifferenceInGrams< 95){
                    check = false;
                }
            }
        }
        if(age_when_weight_taken == 11 && monthLastWeightTaken == 9 ){
            if(gender.MALE == gender){
                if(weightDifferenceInGrams< 315){
                    check = false;
                }
            }else if (gender.FEMALE == gender){
                if(weightDifferenceInGrams< 297){
                    check = false;
                }
            }
        }
        if(age_when_weight_taken == 11 && monthLastWeightTaken == 8 ){
            if(gender.MALE == gender){
                if(weightDifferenceInGrams< 567){
                    check = false;
                }
            }else if (gender.FEMALE == gender){
                if(weightDifferenceInGrams< 528){
                    check = false;
                }
            }
        }
        if(age_when_weight_taken == 11 && monthLastWeightTaken == 7 ){
            if(gender.MALE == gender){
                if(weightDifferenceInGrams< 832){
                    check = false;
                }
            }else if (gender.FEMALE == gender){
                if(weightDifferenceInGrams< 785){
                    check = false;
                }
            }
        }
        if(age_when_weight_taken == 11 && monthLastWeightTaken == 5 ){
            if(gender.MALE == gender){
                if(weightDifferenceInGrams< 1524){
                    check = false;
                }
            }else if (gender.FEMALE == gender){
                if(weightDifferenceInGrams< 1446){
                    check = false;
                }
            }
        }
        if(age_when_weight_taken == 12 && monthLastWeightTaken == 11 ){
            if(gender.MALE == gender){
                if(weightDifferenceInGrams< 91){
                    check = false;
                }
            }else if (gender.FEMALE == gender){
                if(weightDifferenceInGrams< 88){
                    check = false;
                }
            }
        }
        if(age_when_weight_taken == 12 && monthLastWeightTaken == 10 ){
            if(gender.MALE == gender){
                if(weightDifferenceInGrams< 286){
                    check = false;
                }
            }else if (gender.FEMALE == gender){
                if(weightDifferenceInGrams< 274){
                    check = false;
                }
            }
        }
        if(age_when_weight_taken == 12 && monthLastWeightTaken == 9 ){
            if(gender.MALE == gender){
                if(weightDifferenceInGrams< 509){
                    check = false;
                }
            }else if (gender.FEMALE == gender){
                if(weightDifferenceInGrams< 481){
                    check = false;
                }
            }
        }
        if(age_when_weight_taken == 12 && monthLastWeightTaken == 8 ){
            if(gender.MALE == gender){
                if(weightDifferenceInGrams< 757){
                    check = false;
                }
            }else if (gender.FEMALE == gender){
                if(weightDifferenceInGrams< 712){
                    check = false;
                }
            }
        }
        if(age_when_weight_taken == 12 && monthLastWeightTaken == 6 ){
            if(gender.MALE == gender){
                if(weightDifferenceInGrams< 1346){
                    check = false;
                }
            }else if (gender.FEMALE == gender){
                if(weightDifferenceInGrams< 1271){
                    check = false;
                }
            }
        }
        if(age_when_weight_taken == 13 && monthLastWeightTaken == 11 ){
            if(gender.MALE == gender){
                if(weightDifferenceInGrams< 260){
                    check = false;
                }
            }else if (gender.FEMALE == gender){
                if(weightDifferenceInGrams< 254){
                    check = false;
                }
            }
        }
        if(age_when_weight_taken == 13 && monthLastWeightTaken == 10 ){
            if(gender.MALE == gender){
                if(weightDifferenceInGrams< 465){
                    check = false;
                }
            }else if (gender.FEMALE == gender){
                if(weightDifferenceInGrams< 451){
                    check = false;
                }
            }
        }
        if(age_when_weight_taken == 13 && monthLastWeightTaken == 9 ){
            if(gender.MALE == gender){
                if(weightDifferenceInGrams< 700){
                    check = false;
                }
            }else if (gender.FEMALE == gender){
                if(weightDifferenceInGrams< 659){
                    check = false;
                }
            }
        }
        if(age_when_weight_taken == 13 && monthLastWeightTaken == 7 ){
            if(gender.MALE == gender){
                if(weightDifferenceInGrams< 1212){
                    check = false;
                }
            }else if (gender.FEMALE == gender){
                if(weightDifferenceInGrams< 1147){
                    check = false;
                }
            }
        }
        if(age_when_weight_taken == 14 && monthLastWeightTaken == 12 ){
            if(gender.MALE == gender){
                if(weightDifferenceInGrams< 236){
                    check = false;
                }
            }else if (gender.FEMALE == gender){
                if(weightDifferenceInGrams< 238){
                    check = false;
                }
            }
        }
        if(age_when_weight_taken == 14 && monthLastWeightTaken == 11 ){
            if(gender.MALE == gender){
                if(weightDifferenceInGrams< 430){
                    check = false;
                }
            }else if (gender.FEMALE == gender){
                if(weightDifferenceInGrams< 429){
                    check = false;
                }
            }
        }
        if(age_when_weight_taken == 14 && monthLastWeightTaken == 11 ){
            if(gender.MALE == gender){
                if(weightDifferenceInGrams< 648){
                    check = false;
                }
            }else if (gender.FEMALE == gender){
                if(weightDifferenceInGrams< 624){
                    check = false;
                }
            }
        }
        if(age_when_weight_taken == 14 && monthLastWeightTaken == 8 ){
            if(gender.MALE == gender){
                if(weightDifferenceInGrams< 1106){
                    check = false;
                }
            }else if (gender.FEMALE == gender){
                if(weightDifferenceInGrams< 1063){
                    check = false;
                }
            }
        }
        if(age_when_weight_taken == 15 && monthLastWeightTaken == 13 ){
            if(gender.MALE == gender){
                if(weightDifferenceInGrams< 212){
                    check = false;
                }
            }else if (gender.FEMALE == gender){
                if(weightDifferenceInGrams< 227){
                    check = false;
                }
            }
        }
        if(age_when_weight_taken == 15 && monthLastWeightTaken == 12 ){
            if(gender.MALE == gender){
                if(weightDifferenceInGrams< 404){
                    check = false;
                }
            }else if (gender.FEMALE == gender){
                if(weightDifferenceInGrams< 413){
                    check = false;
                }
            }
        }
        if(age_when_weight_taken == 15 && monthLastWeightTaken == 11 ){
            if(gender.MALE == gender){
                if(weightDifferenceInGrams< 598){
                    check = false;
                }
            }else if (gender.FEMALE == gender){
                if(weightDifferenceInGrams< 601){
                    check = false;
                }
            }
        }
        if(age_when_weight_taken == 15 && monthLastWeightTaken == 9 ){
            if(gender.MALE == gender){
                if(weightDifferenceInGrams< 1024){
                    check = false;
                }
            }else if (gender.FEMALE == gender){
                if(weightDifferenceInGrams< 1009){
                    check = false;
                }
            }
        }
        if(age_when_weight_taken == 16 && monthLastWeightTaken == 14 ){
            if(gender.MALE == gender){
                if(weightDifferenceInGrams< 197){
                    check = false;
                }
            }else if (gender.FEMALE == gender){
                if(weightDifferenceInGrams< 220){
                    check = false;
                }
            }
        }
        if(age_when_weight_taken == 16 && monthLastWeightTaken == 13 ){
            if(gender.MALE == gender){
                if(weightDifferenceInGrams< 385){
                    check = false;
                }
            }else if (gender.FEMALE == gender){
                if(weightDifferenceInGrams< 403){
                    check = false;
                }
            }
        }
        if(age_when_weight_taken == 16 && monthLastWeightTaken == 12 ){
            if(gender.MALE == gender){
                if(weightDifferenceInGrams< 564){
                    check = false;
                }
            }else if (gender.FEMALE == gender){
                if(weightDifferenceInGrams< 586){
                    check = false;
                }
            }
        }
        if(age_when_weight_taken == 16 && monthLastWeightTaken == 10 ){
            if(gender.MALE == gender){
                if(weightDifferenceInGrams< 970){
                    check = false;
                }
            }else if (gender.FEMALE == gender){
                if(weightDifferenceInGrams< 975){
                    check = false;
                }
            }
        }
        if(age_when_weight_taken == 17 && monthLastWeightTaken == 15 ){
            if(gender.MALE == gender){
                if(weightDifferenceInGrams< 193){
                    check = false;
                }
            }else if (gender.FEMALE == gender){
                if(weightDifferenceInGrams< 216){
                    check = false;
                }
            }
        }
        if(age_when_weight_taken == 17 && monthLastWeightTaken == 14 ){
            if(gender.MALE == gender){
                if(weightDifferenceInGrams< 372){
                    check = false;
                }
            }else if (gender.FEMALE == gender){
                if(weightDifferenceInGrams< 397){
                    check = false;
                }
            }
        }
        if(age_when_weight_taken == 17 && monthLastWeightTaken == 13 ){
            if(gender.MALE == gender){
                if(weightDifferenceInGrams< 550){
                    check = false;
                }
            }else if (gender.FEMALE == gender){
                if(weightDifferenceInGrams< 578){
                    check = false;
                }
            }
        }
        if(age_when_weight_taken == 17 && monthLastWeightTaken == 11 ){
            if(gender.MALE == gender){
                if(weightDifferenceInGrams< 937){
                    check = false;
                }
            }else if (gender.FEMALE == gender){
                if(weightDifferenceInGrams< 956){
                    check = false;
                }
            }
        }
        if(age_when_weight_taken == 18 && monthLastWeightTaken == 16 ){
            if(gender.MALE == gender){
                if(weightDifferenceInGrams< 192){
                    check = false;
                }
            }else if (gender.FEMALE == gender){
                if(weightDifferenceInGrams< 212){
                    check = false;
                }
            }
        }
        if(age_when_weight_taken == 18 && monthLastWeightTaken == 15 ){
            if(gender.MALE == gender){
                if(weightDifferenceInGrams< 362){
                    check = false;
                }
            }else if (gender.FEMALE == gender){
                if(weightDifferenceInGrams< 392){
                    check = false;
                }
            }
        }
        if(age_when_weight_taken == 18 && monthLastWeightTaken == 14 ){
            if(gender.MALE == gender){
                if(weightDifferenceInGrams< 543){
                    check = false;
                }
            }else if (gender.FEMALE == gender){
                if(weightDifferenceInGrams< 572){
                    check = false;
                }
            }
        }
        if(age_when_weight_taken == 18 && monthLastWeightTaken == 12 ){
            if(gender.MALE == gender){
                if(weightDifferenceInGrams< 914){
                    check = false;
                }
            }else if (gender.FEMALE == gender){
                if(weightDifferenceInGrams< 942){
                    check = false;
                }
            }
        }
        if(age_when_weight_taken == 19 && monthLastWeightTaken == 17 ){
            if(gender.MALE == gender){
                if(weightDifferenceInGrams< 188){
                    check = false;
                }
            }else if (gender.FEMALE == gender){
                if(weightDifferenceInGrams< 205){
                    check = false;
                }
            }
        }
        if(age_when_weight_taken == 19 && monthLastWeightTaken == 16 ){
            if(gender.MALE == gender){
                if(weightDifferenceInGrams< 355){
                    check = false;
                }
            }else if (gender.FEMALE == gender){
                if(weightDifferenceInGrams< 385){
                    check = false;
                }
            }
        }
        if(age_when_weight_taken == 19 && monthLastWeightTaken == 15 ){
            if(gender.MALE == gender){
                if(weightDifferenceInGrams< 537){
                    check = false;
                }
            }else if (gender.FEMALE == gender){
                if(weightDifferenceInGrams< 565){
                    check = false;
                }
            }
        }
        if(age_when_weight_taken == 19 && monthLastWeightTaken == 13 ){
            if(gender.MALE == gender){
                if(weightDifferenceInGrams< 898){
                    check = false;
                }
            }else if (gender.FEMALE == gender){
                if(weightDifferenceInGrams< 931){
                    check = false;
                }
            }
        }
        if(age_when_weight_taken == 20 && monthLastWeightTaken == 18 ){
            if(gender.MALE == gender){
                if(weightDifferenceInGrams< 182){
                    check = false;
                }
            }else if (gender.FEMALE == gender){
                if(weightDifferenceInGrams< 196){
                    check = false;
                }
            }
        }
        if(age_when_weight_taken == 20 && monthLastWeightTaken == 17 ){
            if(gender.MALE == gender){
                if(weightDifferenceInGrams< 351){
                    check = false;
                }
            }else if (gender.FEMALE == gender){
                if(weightDifferenceInGrams< 376){
                    check = false;
                }
            }
        }
        if(age_when_weight_taken == 20 && monthLastWeightTaken == 16 ){
            if(gender.MALE == gender){
                if(weightDifferenceInGrams< 529){
                    check = false;
                }
            }else if (gender.FEMALE == gender){
                if(weightDifferenceInGrams< 556){
                    check = false;
                }
            }
        }
        if(age_when_weight_taken == 20 && monthLastWeightTaken == 14 ){
            if(gender.MALE == gender){
                if(weightDifferenceInGrams< 887){
                    check = false;
                }
            }else if (gender.FEMALE == gender){
                if(weightDifferenceInGrams< 920){
                    check = false;
                }
            }
        }
        if(age_when_weight_taken == 21 && monthLastWeightTaken == 19 ){
            if(gender.MALE == gender){
                if(weightDifferenceInGrams< 176){
                    check = false;
                }
            }else if (gender.FEMALE == gender){
                if(weightDifferenceInGrams< 188){
                    check = false;
                }
            }
        }
        if(age_when_weight_taken == 21 && monthLastWeightTaken == 18 ){
            if(gender.MALE == gender){
                if(weightDifferenceInGrams< 347){
                    check = false;
                }
            }else if (gender.FEMALE == gender){
                if(weightDifferenceInGrams< 366){
                    check = false;
                }
            }
        }
        if(age_when_weight_taken == 21 && monthLastWeightTaken == 17 ){
            if(gender.MALE == gender){
                if(weightDifferenceInGrams< 519){
                    check = false;
                }
            }else if (gender.FEMALE == gender){
                if(weightDifferenceInGrams< 546){
                    check = false;
                }
            }
        }
        if(age_when_weight_taken == 21 && monthLastWeightTaken == 15 ){
            if(gender.MALE == gender){
                if(weightDifferenceInGrams< 880){
                    check = false;
                }
            }else if (gender.FEMALE == gender){
                if(weightDifferenceInGrams< 908){
                    check = false;
                }
            }
        }
        if(age_when_weight_taken == 22 && monthLastWeightTaken == 20 ){
            if(gender.MALE == gender){
                if(weightDifferenceInGrams< 171){
                    check = false;
                }
            }else if (gender.FEMALE == gender){
                if(weightDifferenceInGrams< 178){
                    check = false;
                }
            }
        }
        if(age_when_weight_taken == 22 && monthLastWeightTaken == 19 ){
            if(gender.MALE == gender){
                if(weightDifferenceInGrams< 342){
                    check = false;
                }
            }else if (gender.FEMALE == gender){
                if(weightDifferenceInGrams< 354){
                    check = false;
                }
            }
        }
        if(age_when_weight_taken == 22 && monthLastWeightTaken == 18 ){
            if(gender.MALE == gender){
                if(weightDifferenceInGrams< 508){
                    check = false;
                }
            }else if (gender.FEMALE == gender){
                if(weightDifferenceInGrams< 532){
                    check = false;
                }
            }
        }
        if(age_when_weight_taken == 22 && monthLastWeightTaken == 16 ){
            if(gender.MALE == gender){
                if(weightDifferenceInGrams< 872){
                    check = false;
                }
            }else if (gender.FEMALE == gender){
                if(weightDifferenceInGrams< 894){
                    check = false;
                }
            }
        }
        if(age_when_weight_taken == 23 && monthLastWeightTaken == 21 ){
            if(gender.MALE == gender){
                if(weightDifferenceInGrams< 167){
                    check = false;
                }
            }else if (gender.FEMALE == gender){
                if(weightDifferenceInGrams< 164){
                    check = false;
                }
            }
        }
        if(age_when_weight_taken == 23 && monthLastWeightTaken == 20 ){
            if(gender.MALE == gender){
                if(weightDifferenceInGrams< 334){
                    check = false;
                }
            }else if (gender.FEMALE == gender){
                if(weightDifferenceInGrams< 340){
                    check = false;
                }
            }
        }
        if(age_when_weight_taken == 23 && monthLastWeightTaken == 19){
            if(gender.MALE == gender){
                if(weightDifferenceInGrams< 501){
                    check = false;
                }
            }else if (gender.FEMALE == gender){
                if(weightDifferenceInGrams< 517){
                    check = false;
                }
            }
        }
        if(age_when_weight_taken == 23 && monthLastWeightTaken == 17 ){
            if(gender.MALE == gender){
                if(weightDifferenceInGrams< 863){
                    check = false;
                }
            }else if (gender.FEMALE == gender){
                if(weightDifferenceInGrams< 876){
                    check = false;
                }
            }
        }
        if(age_when_weight_taken == 24 && monthLastWeightTaken == 22 ){
            if(gender.MALE == gender){
                if(weightDifferenceInGrams< 164){
                    check = false;
                }
            }else if (gender.FEMALE == gender){
                if(weightDifferenceInGrams< 150){
                    check = false;
                }
            }
        }
        if(age_when_weight_taken == 24 && monthLastWeightTaken == 21 ){
            if(gender.MALE == gender){
                if(weightDifferenceInGrams< 322){
                    check = false;
                }
            }else if (gender.FEMALE == gender){
                if(weightDifferenceInGrams< 326){
                    check = false;
                }
            }
        }
        if(age_when_weight_taken == 24 && monthLastWeightTaken == 20 ){
            if(gender.MALE == gender){
                if(weightDifferenceInGrams< 497){
                    check = false;
                }
            }else if (gender.FEMALE == gender){
                if(weightDifferenceInGrams< 503){
                    check = false;
                }
            }
        }
        if(age_when_weight_taken == 24 && monthLastWeightTaken == 18 ){
            if(gender.MALE == gender){
                if(weightDifferenceInGrams< 855){
                    check = false;
                }
            }else if (gender.FEMALE == gender){
                if(weightDifferenceInGrams< 857){
                    check = false;
                }
            }
        }




        return check;
    }

}
