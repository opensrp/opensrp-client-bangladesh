package org.smartregister.growplus.provider;

import android.app.FragmentTransaction;
import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import org.smartregister.commonregistry.CommonPersonObject;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.cursoradapter.SmartRegisterCLientsProviderForCursorAdapter;
import org.smartregister.cursoradapter.SmartRegisterQueryBuilder;
import org.smartregister.growplus.activity.HouseholdDetailActivity;
import org.smartregister.growplus.application.VaccinatorApplication;
import org.smartregister.growplus.repository.PathRepository;
import org.smartregister.growthmonitoring.repository.WeightRepository;
import org.smartregister.immunization.repository.VaccineRepository;
import org.smartregister.growplus.R;
import org.smartregister.growplus.activity.HouseholdSmartRegisterActivity;
import org.smartregister.growplus.fragment.HouseholdMemberAddFragment;
import org.smartregister.growplus.fragment.HouseholdSmartRegisterFragment;
import org.smartregister.growplus.view.LocationPickerView;
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

import java.util.Date;
import java.util.List;
import java.util.Map;

import util.JsonFormUtils;
import util.PathConstants;

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

        fillValue((TextView) convertView.findViewById(R.id.id),findmemberDetails(pc));
        Date date = new Date(Long.parseLong(getValue(pc.getColumnmaps(), "last_interacted_with", false)));
        String d[] = date.toString().split(" ");
        String date_string = d[1]+" "+d[2]+" "+d[d.length-1];
        fillValue((TextView) convertView.findViewById(R.id.registrationdate), date_string);
//        fillValue((TextView) convertView.findViewById(R.id.address), getValue(pc.getColumnmaps(), "address1", false));
//        fillValue((TextView) convertView.findViewById(R.id.householdprimarytext), getValue(pc.getColumnmaps(), "block", false));


        DetailsRepository detailsRepository;
        detailsRepository = org.smartregister.Context.getInstance().detailsRepository();


        Map<String, String> details = detailsRepository.getAllDetailsForClient(pc.entityId());


        fillValue((TextView) convertView.findViewById(R.id.householdprimarytext), getValue(details, "address3", false).substring( getValue(details, "address3", false).lastIndexOf(":")+1));
        fillValue((TextView) convertView.findViewById(R.id.housholdsecondarytext), getValue(details, "address2", false).substring( getValue(details, "address2", false).lastIndexOf(":")+1));


        ImageView addmember = (ImageView) convertView.findViewById(R.id.add_member);
        LocationPickerView locationPickerView = ((HouseholdSmartRegisterFragment) mBaseFragment).getLocationPickerView();

        try {
            locationId = JsonFormUtils.getOpenMrsLocationId(context(),getValue(details, "address4", false) );
        } catch (JSONException e) {
            e.printStackTrace();
        }

        convertView.findViewById(R.id.child_profile_info_layout).setTag(client);
        convertView.findViewById(R.id.child_profile_info_layout).setOnClickListener(onClickListener);

        ImageView profileImageIV = (ImageView) convertView.findViewById(R.id.profilepic);


        addmember.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentTransaction ft = ((HouseholdSmartRegisterActivity)context).getFragmentManager().beginTransaction();
                android.app.Fragment prev = ((HouseholdSmartRegisterActivity)context).getFragmentManager().findFragmentByTag(HouseholdMemberAddFragment.DIALOG_TAG);
                if (prev != null) {
                    ft.remove(prev);
                }
                ft.addToBackStack(null);
               boolean isMotherExist = isMotherExist(pc);
                HouseholdMemberAddFragment addmemberFragment = HouseholdMemberAddFragment.newInstance(context,locationId,pc.entityId(),context(), isMotherExist);
                    addmemberFragment.show(ft, HouseholdMemberAddFragment.DIALOG_TAG);
            }
        });
//


    }
    public String findmemberDetails(CommonPersonObjectClient pc){
        DetailsRepository detailsRepository = org.smartregister.Context.getInstance().detailsRepository();

        List<CommonPersonObject> mothers = context().commonrepository(PathConstants.MOTHER_TABLE_NAME)
                .findByRelational_IDs(pc.entityId());
        int children = 0;
        int pregnantwoman = 0;
        int mother = mothers.size();
        for(int i= 0;i<mothers.size();i++){
            List<CommonPersonObject> childrennumber = context().commonrepository(PathConstants.CHILD_TABLE_NAME)
                    .findByRelational_IDs(mothers.get(i).getCaseId());
            children = children + childrennumber.size();
            Map<String, String> detailmaps = detailsRepository.getAllDetailsForClient(mothers.get(i).getCaseId());
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
            if(pregnant && !lactating){
                pregnantwoman++;
            }
        }
        return "Woman: "+mother+ "\n"+"Child: "+children+"\n"+"Pregnant: "+pregnantwoman;
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

    private boolean isMotherExist(CommonPersonObjectClient householdObject) {
        PathRepository repo = (PathRepository) VaccinatorApplication.getInstance().getRepository();
        net.sqlcipher.database.SQLiteDatabase db = repo.getReadableDatabase();
        String mother_id = householdObject.getDetails().get("_id");

        String tableName = PathConstants.MOTHER_TABLE_NAME;
        SmartRegisterQueryBuilder queryBUilder = new SmartRegisterQueryBuilder();
        queryBUilder.SelectInitiateMainTable(tableName, new String[]{
                tableName + ".relational_id",
        });
        Cursor cursor = db.rawQuery(queryBUilder.mainCondition("relational_id = ?"),new String[]{mother_id});

        return (cursor!=null && cursor.getCount()>0) ? true : false ;
    }
}