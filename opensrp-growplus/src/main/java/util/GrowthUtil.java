package util;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.support.v4.app.FragmentActivity;
import android.util.Pair;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.domain.Photo;
import org.smartregister.growplus.R;
import org.smartregister.growthmonitoring.GrowthMonitoringLibrary;
import org.smartregister.growthmonitoring.domain.Height;
import org.smartregister.growthmonitoring.domain.HeightWrapper;
import org.smartregister.growthmonitoring.domain.MUAC;
import org.smartregister.growthmonitoring.domain.MUACWrapper;
import org.smartregister.growthmonitoring.domain.WeightWrapper;
import org.smartregister.growthmonitoring.fragment.RecordHeightDialogFragment;
import org.smartregister.growthmonitoring.fragment.RecordMUACDialogFragment;
import org.smartregister.growthmonitoring.fragment.RecordWeightDialogFragment;
import org.smartregister.growthmonitoring.repository.HeightRepository;
import org.smartregister.growthmonitoring.repository.MUACRepository;
import org.smartregister.growthmonitoring.util.ImageUtils;
import org.smartregister.util.DateUtil;
import org.smartregister.util.Utils;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.smartregister.util.Utils.getName;
import static org.smartregister.util.Utils.getValue;

public class GrowthUtil {
    public static String DOB_STRING = "2012-01-01T00:00:00.000Z";
    private static String CM_FORMAT = "%s cm";
    public static void createHeightWidget(Activity context,HashMap<Long, Pair<String, String>> last_five_weight_map,
                                          ArrayList<View.OnClickListener> listeners, ArrayList<Boolean> editenabled,LinearLayout tableLayout) {

        tableLayout.removeAllViews();

        int i = 0;
        for (Map.Entry<Long, Pair<String, String>> entry : last_five_weight_map.entrySet()) {
            Pair<String, String> pair = entry.getValue();
            View view = createTableRowForHeight(context, tableLayout, pair.first, pair.second, editenabled.get(i),
                    listeners.get(i));

            tableLayout.addView(view);
            i++;
        }
    }
    public static View createTableRowForHeight(Activity context, ViewGroup container, String labelString, String valueString,
                                               boolean editenabled, View.OnClickListener listener) {
        View rows = context.getLayoutInflater().inflate(R.layout.tablerows_weight, container, false);
        TextView label = rows.findViewById(R.id.label);
        TextView value = rows.findViewById(R.id.value);
        Button edit = rows.findViewById(R.id.edit);
        if (editenabled) {
            edit.setVisibility(View.VISIBLE);
            edit.setOnClickListener(listener);
        } else {
            edit.setVisibility(View.INVISIBLE);
        }
        label.setText(labelString);
        value.setText(valueString);
        return rows;
    }
    private static HeightWrapper getHeightWrapper(int position, CommonPersonObjectClient childDetails, String childName,
                                                  String gender, String zeirId, String duration, Photo photo) {
        HeightWrapper heightWrapper = new HeightWrapper();
        heightWrapper.setId(childDetails.entityId());
        HeightRepository wp = GrowthMonitoringLibrary.getInstance().getHeightRepository();
//        List<Height> heightList = wp.findLast5(childDetails.entityId());
//        if (!heightList.isEmpty()) {
//            heightWrapper.setHeight(heightList.get(position).getCm());
//            heightWrapper.setUpdatedHeightDate(new DateTime(heightList.get(position).getDate()), false);
//            heightWrapper.setDbKey(heightList.get(position).getId());
//        }

        heightWrapper.setGender(gender);
        heightWrapper.setPatientName(childName);
        heightWrapper.setPatientNumber(zeirId);
        heightWrapper.setPatientAge(duration);
        heightWrapper.setPhoto(photo);
        heightWrapper.setPmtctStatus(getValue(childDetails.getColumnmaps(), "pmtct_status", false));
        return heightWrapper;
    }
    private static WeightWrapper getWeightWrapper(int position, CommonPersonObjectClient childDetails, String childName,
                                                  String gender, String zeirId, String duration, Photo photo) {
        WeightWrapper heightWrapper = new WeightWrapper();
        heightWrapper.setId(childDetails.entityId());
        HeightRepository wp = GrowthMonitoringLibrary.getInstance().getHeightRepository();
//        List<Height> heightList = wp.findLast5(childDetails.entityId());
//        if (!heightList.isEmpty()) {
//            heightWrapper.setHeight(heightList.get(position).getCm());
//            heightWrapper.setUpdatedHeightDate(new DateTime(heightList.get(position).getDate()), false);
//            heightWrapper.setDbKey(heightList.get(position).getId());
//        }

        heightWrapper.setGender(gender);
        heightWrapper.setPatientName(childName);
        heightWrapper.setPatientNumber(zeirId);
        heightWrapper.setPatientAge(duration);
        heightWrapper.setPhoto(photo);
        heightWrapper.setPmtctStatus(getValue(childDetails.getColumnmaps(), "pmtct_status", false));
        return heightWrapper;
    }
    private static MUACWrapper getMUACWrapper(int position, CommonPersonObjectClient childDetails, String childName,
                                                String gender, String zeirId, String duration, Photo photo) {
        MUACWrapper heightWrapper = new MUACWrapper();
        heightWrapper.setId(childDetails.entityId());
        MUACRepository wp = GrowthMonitoringLibrary.getInstance().getMuacRepository();
        List<MUAC> heightList = wp.findLast5(childDetails.entityId());
        if (!heightList.isEmpty()) {
            heightWrapper.setHeight(heightList.get(position).getCm());
            heightWrapper.setUpdatedHeightDate(new DateTime(heightList.get(position).getDate()), false);
            heightWrapper.setDbKey(heightList.get(position).getId());
        }

        heightWrapper.setGender(gender);
        heightWrapper.setPatientName(childName);
        heightWrapper.setPatientNumber(zeirId);
        heightWrapper.setPatientAge(duration);
        heightWrapper.setPhoto(photo);
        heightWrapper.setPmtctStatus(getValue(childDetails.getColumnmaps(), "pmtct_status", false));
        return heightWrapper;
    }
    public static void showMuacRecordDialog(FragmentActivity context, CommonPersonObjectClient childDetails, String tag){
        String firstName = org.smartregister.util.Utils.getValue(childDetails.getColumnmaps(), "first_name", true);
        String lastName = Utils.getValue(childDetails.getColumnmaps(), "last_name", true);
        String childName = getName(firstName, lastName).trim();

        String gender = getValue(childDetails.getColumnmaps(), "gender", true);

        String zeirId = getValue(childDetails.getColumnmaps(), "zeir_id", false);
        String duration = "";
        String dobString = getValue(childDetails.getColumnmaps(), "dob", false);
        DateTime dobDateTime = new DateTime();
        if (StringUtils.isNotBlank(dobString)) {
            dobDateTime = new DateTime(getValue(childDetails.getColumnmaps(), "dob", false));
            duration = DateUtil.getDuration(dobDateTime);
        }

        Photo photo = ImageUtils.profilePhotoByClient(childDetails);

        MUACWrapper heightWrapper = getMUACWrapper(0, childDetails, childName, gender, zeirId, duration, photo);
        RecordMUACDialogFragment heightDialogFragment = RecordMUACDialogFragment.newInstance(dobDateTime.toDate(),heightWrapper);
        heightDialogFragment.show(initFragmentTransaction(context,tag),tag);

    }
    public static void showHeightRecordDialog(FragmentActivity context, CommonPersonObjectClient childDetails, int position, String tag) {

        String firstName = org.smartregister.util.Utils.getValue(childDetails.getColumnmaps(), "first_name", true);
        String lastName = Utils.getValue(childDetails.getColumnmaps(), "last_name", true);
        String childName = getName(firstName, lastName).trim();

        String gender = getValue(childDetails.getColumnmaps(), "gender", true);

        String zeirId = getValue(childDetails.getColumnmaps(), "zeir_id", false);
        String duration = "";
        String dobString = getValue(childDetails.getColumnmaps(), "dob", false);
        DateTime dobDateTime = new DateTime();
        if (StringUtils.isNotBlank(dobString)) {
            dobDateTime = new DateTime(getValue(childDetails.getColumnmaps(), "dob", false));
            duration = DateUtil.getDuration(dobDateTime);
        }

        Photo photo = ImageUtils.profilePhotoByClient(childDetails);

        HeightWrapper heightWrapper = getHeightWrapper(position, childDetails, childName, gender, zeirId, duration, photo);
        RecordHeightDialogFragment heightDialogFragment = RecordHeightDialogFragment.newInstance(dobDateTime.toDate(),heightWrapper);
        heightDialogFragment.show(initFragmentTransaction(context,tag),tag);

    }
    public static void showWeightRecordDialog(FragmentActivity context, CommonPersonObjectClient childDetails, int position, String tag) {

        String firstName = org.smartregister.util.Utils.getValue(childDetails.getColumnmaps(), "first_name", true);
        String lastName = Utils.getValue(childDetails.getColumnmaps(), "last_name", true);
        String childName = getName(firstName, lastName).trim();

        String gender = getValue(childDetails.getColumnmaps(), "gender", true);

        String zeirId = getValue(childDetails.getColumnmaps(), "zeir_id", false);
        String duration = "";
        String dobString = getValue(childDetails.getColumnmaps(), "dob", false);
        DateTime dobDateTime = new DateTime();
        if (StringUtils.isNotBlank(dobString)) {
            dobDateTime = new DateTime(getValue(childDetails.getColumnmaps(), "dob", false));
            duration = DateUtil.getDuration(dobDateTime);
        }

        Photo photo = ImageUtils.profilePhotoByClient(childDetails);

        WeightWrapper heightWrapper = getWeightWrapper(position, childDetails, childName, gender, zeirId, duration, photo);
        RecordWeightDialogFragment heightDialogFragment = RecordWeightDialogFragment.newInstance(dobDateTime.toDate(),heightWrapper);
        heightDialogFragment.show(initFragmentTransaction(context,tag),tag);

    }
    public static FragmentTransaction initFragmentTransaction(FragmentActivity context, String tag) {
        FragmentTransaction ft = context.getFragmentManager().beginTransaction();
        Fragment prev = context.getFragmentManager().findFragmentByTag(tag);
        if (prev != null) {
            ft.remove(prev);
        }
        return ft;
    }
    public static String cmStringSuffix(Float height) {
        return String.format(CM_FORMAT, height);
    }
    public static Date getDateOfBirth() {
        LocalDate localDate = new LocalDate();
        //DOB for sample app needs to ba dynamic
        DateTime dateTime = localDate.minusYears(5).plusMonths(2).toDateTime(LocalTime.now());
        dateTime = new DateTime(DOB_STRING);
        Date dob = dateTime.toDate();

        return dob;
    }
    public static boolean lessThanThreeMonths(Height height) {
        ////////////////////////check 3 months///////////////////////////////
        return height == null || height.getCreatedAt() == null || !DateUtil
                .checkIfDateThreeMonthsOlder(height.getCreatedAt());
        ///////////////////////////////////////////////////////////////////////
    }
    public static boolean lessThanThreeMonths(MUAC height) {
        ////////////////////////check 3 months///////////////////////////////
        return height == null || height.getCreatedAt() == null || !DateUtil
                .checkIfDateThreeMonthsOlder(height.getCreatedAt());
        ///////////////////////////////////////////////////////////////////////
    }
}
