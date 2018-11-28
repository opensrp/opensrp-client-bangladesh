package org.smartregister.growplus.provider;

import android.content.Context;
import android.database.Cursor;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.opensrp.api.constants.Gender;
import org.smartregister.commonregistry.CommonPersonObject;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.commonregistry.CommonRepository;
import org.smartregister.cursoradapter.SmartRegisterCLientsProviderForCursorAdapter;
import org.smartregister.domain.Alert;
import org.smartregister.growplus.application.VaccinatorApplication;
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
import util.PathConstants;
import util.WeightVelocityUtils;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static com.vijay.jsonwizard.utils.FormUtils.DATE_FORMAT;
import static org.smartregister.growplus.activity.LoginActivity.getOpenSRPContext;
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
    TextView textViewWeightCaptured,textViewWeightCaptureDate,textViewChildAge,textViewMontherName,textViewChildName;
    TextView textViewRecordWeight;
    LinearLayout backgroundLayout,weightBoxLayout,recordWeightLayout;
    ImageView profileImage,recordWeightCheck;

    @Override
    public void getView(Cursor cursor, SmartRegisterClient client, final View convertView) {
            textViewChildName=(TextView) convertView.findViewById(R.id.child_name);
            textViewMontherName=(TextView) convertView.findViewById(R.id.child_mothername);
            textViewChildAge=(TextView) convertView.findViewById(R.id.child_age);
            textViewWeightCaptured=(TextView) convertView.findViewById(R.id.weightcaptured);
            textViewRecordWeight=(TextView) convertView.findViewById(R.id.record_weight_text);
            profileImage=(ImageView) convertView.findViewById(R.id.child_profilepic);
            recordWeightCheck=(ImageView) convertView.findViewById(R.id.record_weight_check);
            backgroundLayout=convertView.findViewById(R.id.child_profile_info_layout);
            recordWeightLayout=convertView.findViewById(R.id.record_weight);
            weightBoxLayout=convertView.findViewById(R.id.weightbox);
            textViewWeightCaptureDate=(TextView) convertView.findViewById(R.id.weightcaptureddate);

        CommonPersonObjectClient pc = (CommonPersonObjectClient) client;

//        fillValue((TextView) convertView.findViewById(R.id.child_zeir_id), getValue(pc.getColumnmaps(), PathConstants.KEY.ZEIR_ID, false));

        String firstName = getValue(pc.getColumnmaps(), PathConstants.KEY.FIRST_NAME, true);
        String lastName = getValue(pc.getColumnmaps(), PathConstants.KEY.LAST_NAME, true).replaceAll(Pattern.quote("."),"");
        String childName = getName(firstName, lastName);

        String motherFirstName = getValue(pc.getColumnmaps(), PathConstants.KEY.MOTHER_FIRST_NAME, true);
        if (StringUtils.isBlank(childName) && StringUtils.isNotBlank(motherFirstName)) {
            childName = "B/o " + motherFirstName.trim();
        }
        fillValue(textViewChildName, childName);

        String motherName = getValue(pc.getColumnmaps(), PathConstants.KEY.MOTHER_FIRST_NAME, true).replaceAll(Pattern.quote("."),"") + " " + getValue(pc, PathConstants.KEY.MOTHER_LAST_NAME, true).replaceAll(Pattern.quote("."),"");
        if (!StringUtils.isNotBlank(motherName)) {
            motherName = "M/G: " + motherName.trim();
        }
        fillValue(textViewMontherName, motherName);

        DateTime birthDateTime = null;
        String dobString = getValue(pc.getColumnmaps(), PathConstants.KEY.DOB, false);
        String durationString = "";
        if (StringUtils.isNotBlank(dobString)) {
            try {
                birthDateTime = new DateTime(dobString);
                durationString = DateUtil.getDuration(birthDateTime);
            } catch (Exception e) {
                Log.e(getClass().getName(), e.toString(), e);
            }
        }
        fillValue(textViewChildAge, durationString);

        String gender = getValue(pc.getColumnmaps(), PathConstants.KEY.GENDER, true);
        profileImage.setImageResource(ImageUtils.profileImageResourceByGender(gender));

        backgroundLayout.setTag(client);
        backgroundLayout.setOnClickListener(onClickListener);

        recordWeightLayout.setBackground(context.getResources().getDrawable(R.drawable.record_weight_bg));
        recordWeightLayout.setTag(client);
        recordWeightLayout.setOnClickListener(onClickListener);
        recordWeightLayout.setVisibility(View.INVISIBLE);


        String lostToFollowUp = getValue(pc.getColumnmaps(), PathConstants.KEY.LOST_TO_FOLLOW_UP, false);
        String inactive = getValue(pc.getColumnmaps(), PathConstants.KEY.INACTIVE, false);

        try {
            Utils.startAsyncTask(new WeightAsyncTask(convertView, pc.entityId(), lostToFollowUp, inactive, client, cursor), null);
//            Utils.startAsyncTask(new VaccinationAsyncTask(convertView, pc.entityId(), dobString, lostToFollowUp, inactive, client, cursor), null);
        } catch (Exception e) {
            Log.e(getClass().getName(), e.getMessage(), e);
        }
        weightBoxLayout.setBackgroundColor(getOpenSRPContext().getColorResource(R.color.status_bar_text_almost_white));
        textViewWeightCaptured.setTextColor(getOpenSRPContext().getColorResource(R.color.client_list_grey));
        textViewWeightCaptureDate.setTextColor(getOpenSRPContext().getColorResource(R.color.client_list_grey));

        List<Weight> weightlist = weightRepository.findLast5(pc.entityId());
        if(weightlist.size() >= 1) {

           Double Zscore = weightlist.get(0).getZScore();

            Boolean adequate = WeightVelocityUtils.checkForWeightGainCalc(birthDateTime.toDate(), Gender.valueOf(gender.toUpperCase()), weightlist.get(0), pc.entityId(), getOpenSRPContext().detailsRepository());
            if(adequate==null){
                weightBoxLayout.setBackgroundColor(getOpenSRPContext().getColorResource(R.color.status_bar_text_almost_white));
                textViewWeightCaptured.setTextColor(getOpenSRPContext().getColorResource(R.color.client_list_grey));
                textViewWeightCaptureDate.setTextColor(getOpenSRPContext().getColorResource(R.color.client_list_grey));

            }else if(adequate){
                weightBoxLayout.setBackgroundColor(getOpenSRPContext().getColorResource(R.color.weightgreen));
                textViewWeightCaptured.setTextColor(getOpenSRPContext().getColorResource(R.color.status_bar_text_almost_white));
                textViewWeightCaptureDate.setTextColor(getOpenSRPContext().getColorResource(R.color.status_bar_text_almost_white));

            }else {
                weightBoxLayout.setBackgroundColor(getOpenSRPContext().getColorResource(R.color.weightred));
                textViewWeightCaptured.setTextColor(getOpenSRPContext().getColorResource(R.color.status_bar_text_almost_white));
                textViewWeightCaptureDate.setTextColor(getOpenSRPContext().getColorResource(R.color.status_bar_text_almost_white));

            }
//            if(Zscore != null) {
//                if (Zscore <= -3.0) {
//                    weightBoxLayout.setBackgroundColor(getOpenSRPContext().getColorResource(R.color.alert_urgent_red));
//                    textViewWeightCaptured.setTextColor(getOpenSRPContext().getColorResource(R.color.status_bar_text_almost_white));
//                    textViewWeightCaptureDate.setTextColor(getOpenSRPContext().getColorResource(R.color.status_bar_text_almost_white));
//
//                }
//            }

            textViewWeightCaptured.setText(weightlist.get(0).getKg()+"Kg");
            textViewWeightCaptureDate.setText(Utils.convertDateFormat(new DateTime(weightlist.get(0).getDate().getTime())));

        }else{
            Map<String, String> detailsMap =  getOpenSRPContext().detailsRepository().getAllDetailsForClient(pc.entityId());
            Float birthweight = new Float(getValue(detailsMap, "Birth_Weight", true));

            textViewWeightCaptured.setText(birthweight+"Kg");
            textViewWeightCaptureDate.setText(dobString);
            textViewWeightCaptured.setTextColor(getOpenSRPContext().getColorResource(R.color.client_list_grey));
            textViewWeightCaptureDate.setTextColor(getOpenSRPContext().getColorResource(R.color.client_list_grey));


        }
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
//        if (updateWrapper.getCursor() instanceof AdvancedSearchFragment.AdvancedMatrixCursor) {
//            updateViews(updateWrapper.getConvertView(), updateWrapper.getClient(), true);
//        }
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
        View view=inflater().inflate(R.layout.smart_register_child_client, null);
        return view;
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



}
