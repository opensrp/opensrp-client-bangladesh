package org.smartregister.path.provider;

import android.app.FragmentTransaction;
import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.cursoradapter.SmartRegisterCLientsProviderForCursorAdapter;
import org.smartregister.growthmonitoring.repository.WeightRepository;
import org.smartregister.immunization.repository.VaccineRepository;
import org.smartregister.path.R;
import org.smartregister.path.activity.HouseholdSmartRegisterActivity;
import org.smartregister.path.fragment.HouseholdMemberAddFragment;
import org.smartregister.path.fragment.HouseholdSmartRegisterFragment;
import org.smartregister.path.view.LocationPickerView;
import org.smartregister.repository.DetailsRepository;
import org.smartregister.service.AlertService;
import org.smartregister.util.OpenSRPImageLoader;
import org.smartregister.view.activity.DrishtiApplication;
import org.smartregister.view.contract.SmartRegisterClient;
import org.smartregister.view.contract.SmartRegisterClients;
import org.smartregister.view.dialog.FilterOption;
import org.smartregister.view.dialog.ServiceModeOption;
import org.smartregister.view.dialog.SortOption;
import org.json.JSONException;
import org.smartregister.view.viewholder.OnClickFormLauncher;

import java.util.Map;

import org.smartregister.cbhc.util.JsonFormUtils;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;
import static org.smartregister.util.Utils.fillValue;
import static org.smartregister.util.Utils.getValue;

/**
 * Created by Ahmed on 13-Oct-15.
 */
public class HouseholdSmartClientsProvider implements SmartRegisterCLientsProviderForCursorAdapter {
    private final LayoutInflater inflater;
    private final Context context;
    private final View.OnClickListener onClickListener;
    AlertService alertService;
    VaccineRepository vaccineRepository;
    WeightRepository weightRepository;
    private final AbsListView.LayoutParams clientViewLayoutParams;
    private static final String VACCINES_FILE = "vaccines.json";
    String locationId = "";
    private HouseholdSmartRegisterFragment mBaseFragment;
    private static final int REQUEST_CODE_GET_JSON = 3432;

    public HouseholdSmartClientsProvider(Context context, View.OnClickListener onClickListener,
                                         AlertService alertService, VaccineRepository vaccineRepository, WeightRepository weightRepository,HouseholdSmartRegisterFragment mBaseFragment) {
        this.onClickListener = onClickListener;
        this.context = context;
        this.alertService = alertService;
        this.vaccineRepository = vaccineRepository;
        this.weightRepository = weightRepository;
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.mBaseFragment = mBaseFragment;

        clientViewLayoutParams = new AbsListView.LayoutParams(MATCH_PARENT, WRAP_CONTENT);
    }

    @Override
    public void getView(Cursor cursor, SmartRegisterClient client, final View convertView) {
        convertView.setLayoutParams(clientViewLayoutParams);
        final CommonPersonObjectClient pc = (CommonPersonObjectClient) client;
        fillValue((TextView) convertView.findViewById(R.id.householdheadname), getValue(pc.getColumnmaps(), "first_name", false));

        fillValue((TextView) convertView.findViewById(R.id.id), getValue(pc.getColumnmaps(), "HHID", false));
        fillValue((TextView) convertView.findViewById(R.id.registrationdate), getValue(pc.getColumnmaps(), "Date_Of_Reg", false));
//        fillValue((TextView) convertView.findViewById(R.id.address), getValue(pc.getColumnmaps(), "address1", false));
//        fillValue((TextView) convertView.findViewById(R.id.householdprimarytext), getValue(pc.getColumnmaps(), "block", false));


        DetailsRepository detailsRepository;
        detailsRepository = org.smartregister.Context.getInstance().detailsRepository();
        Map<String, String> details = detailsRepository.getAllDetailsForClient(pc.entityId());
        fillValue((TextView) convertView.findViewById(R.id.householdprimarytext), getValue(details, "address3", false));
        fillValue((TextView) convertView.findViewById(R.id.housholdsecondarytext), getValue(details, "address2", false));
        fillValue((TextView) convertView.findViewById(R.id.address), getValue(details, "address1", false));

        Button addmember = (Button)convertView.findViewById(R.id.add_member);
        LocationPickerView locationPickerView = ((HouseholdSmartRegisterFragment) mBaseFragment).getLocationPickerView();

        try {
            locationId = JsonFormUtils.getOpenMrsLocationId(context(),getValue(details, "address4", false) );
        } catch (JSONException e) {
            e.printStackTrace();
        }

        convertView.findViewById(R.id.child_profile_info_layout).setTag(client);
        convertView.findViewById(R.id.child_profile_info_layout).setOnClickListener(onClickListener);

        ImageView profileImageIV = (ImageView) convertView.findViewById(R.id.profilepic);
        if (pc.entityId() != null) {//image already in local storage most likey ):
            //set profile image by passing the client id.If the image doesn't exist in the image org.smartregister.cbhc.repository then download and save locally
            profileImageIV.setTag(org.smartregister.R.id.entity_id, pc.entityId());
            DrishtiApplication.getCachedImageLoaderInstance().getImageByClientId(pc.entityId(), OpenSRPImageLoader.getStaticImageListener((ImageView) profileImageIV, R.drawable.houshold_register_placeholder, R.drawable.houshold_register_placeholder));

        }

        addmember.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentTransaction ft = ((HouseholdSmartRegisterActivity)context).getFragmentManager().beginTransaction();
                android.app.Fragment prev = ((HouseholdSmartRegisterActivity)context).getFragmentManager().findFragmentByTag(HouseholdMemberAddFragment.DIALOG_TAG);
                if (prev != null) {
                    ft.remove(prev);
                }
                ft.addToBackStack(null);

                HouseholdMemberAddFragment addmemberFragment = HouseholdMemberAddFragment.newInstance(context,locationId,pc.entityId(),context());
                    addmemberFragment.show(ft, HouseholdMemberAddFragment.DIALOG_TAG);
//

            }
        });
//
//        String firstName = getValue(pc.getColumnmaps(), "first_name", true);
//        String lastName = getValue(pc.getColumnmaps(), "last_name", true);
//        String childName = getName(firstName, lastName);
//
//        String motherFirstName = getValue(pc.getColumnmaps(), "mother_first_name", true);
//        if (StringUtils.isBlank(childName) && StringUtils.isNotBlank(motherFirstName)) {
//            childName = "B/o " + motherFirstName.trim();
//        }
//        fillValue((TextView) convertView.findViewById(R.id.child_name), childName);
//
//        String motherName = getValue(pc.getColumnmaps(), "mother_first_name", true) + " " + getValue(pc, "mother_last_name", true);
//        if (!StringUtils.isNotBlank(motherName)) {
//            motherName = "M/G: " + motherName.trim();
//        }
//        fillValue((TextView) convertView.findViewById(R.id.child_mothername), motherName);
//
//        DateTime birthDateTime = new DateTime((new Date()).getTime());
//        String dobString = getValue(pc.getColumnmaps(), "dob", false);
//        String durationString = "";
//        if (StringUtils.isNotBlank(dobString)) {
//            try {
//                birthDateTime = new DateTime(dobString);
//                String duration = DateUtils.getDuration(birthDateTime);
//                if (duration != null) {
//                    durationString = duration;
//                }
//            } catch (Exception e) {
//                Log.e(getClass().getName(), e.toString(), e);
//            }
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
        ViewGroup view = (ViewGroup) inflater().inflate(R.layout.smart_register_household_client, null);
        return view;
    }

    public LayoutInflater inflater() {
        return inflater;
    }

      protected org.smartregister.Context context() {
        return org.smartregister.Context.getInstance().updateApplicationContext(context);
    }
}