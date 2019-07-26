package org.smartregister.cbhc.provider;

import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import net.sqlcipher.database.SQLiteDatabase;

import org.apache.commons.lang3.text.WordUtils;
import org.smartregister.cbhc.R;
import org.smartregister.cbhc.application.AncApplication;
import org.smartregister.cbhc.fragment.BaseRegisterFragment;
import org.smartregister.cbhc.repository.AncRepository;
import org.smartregister.cbhc.util.DBConstants;
import org.smartregister.cbhc.util.Utils;
import org.smartregister.commonregistry.CommonPersonObject;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.commonregistry.CommonRepository;
import org.smartregister.cursoradapter.RecyclerViewProvider;
import org.smartregister.util.OpenSRPImageLoader;
import org.smartregister.view.activity.DrishtiApplication;
import org.smartregister.view.contract.SmartRegisterClient;
import org.smartregister.view.contract.SmartRegisterClients;
import org.smartregister.view.dialog.FilterOption;
import org.smartregister.view.dialog.ServiceModeOption;
import org.smartregister.view.dialog.SortOption;
import org.smartregister.view.viewholder.OnClickFormLauncher;

import java.text.MessageFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Set;

import static org.smartregister.util.Utils.getName;

/**
 * Created by keyman on 26/06/2018.
 */

public class RegisterProvider implements RecyclerViewProvider<RegisterProvider.RegisterViewHolder> {
    private final LayoutInflater inflater;
    private Set<org.smartregister.configurableviews.model.View> visibleColumns;
    private View.OnClickListener onClickListener;

    private Context context;
    private CommonRepository commonRepository;

    private View.OnClickListener paginationClickListener;


    public RegisterProvider(Context context, CommonRepository commonRepository, Set visibleColumns, View.OnClickListener onClickListener, View.OnClickListener paginationClickListener) {

        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.visibleColumns = visibleColumns;

        this.onClickListener = onClickListener;
        this.paginationClickListener = paginationClickListener;

        this.context = context;
        this.commonRepository = commonRepository;
    }

    @Override
    public void getView(Cursor cursor, SmartRegisterClient client, RegisterViewHolder viewHolder) {
        CommonPersonObjectClient pc = (CommonPersonObjectClient) client;
//        String removeId = org.smartregister.util.Utils.getValue(pc.getColumnmaps(), DBConstants.KEY.BASE_ENTITY_ID, true);
//        if(removeId.equalsIgnoreCase(213214)){
//            return;
//        }

        if (visibleColumns.isEmpty()) {
            populatePatientColumn(pc, client, viewHolder);
            populateIdentifierColumn(pc, viewHolder);
            populateLastColumn(pc, viewHolder);
            populateRejectedViews(pc,viewHolder);
            MemberCount mc = memberCountHashMap.get(pc.entityId());
            if (mc != null) {
                populateMemberCountColumn(mc, viewHolder);
            } else {
                (new MemberCountAsyncTask(pc, viewHolder)).execute();
            }

            return;
        }

       /* for (org.smartregister.configurableviews.model.View columnView : visibleColumns) {
            switch (columnView.getIdentifier()) {
                case ID:
                    populatePatientColumn(pc, client, convertView);
                    break;
                case NAME:
                    populateIdentifierColumn(pc, convertView);
                    break;
                case DOSE:
                    populateDoseColumn(pc, convertView);
                    break;
                default:
            }
        }

        Map<String, Integer> mapping = new HashMap();
        mapping.put(ID, R.id.patient_column);
        mapping.put(DOSE, R.id.identifier_column);
        mapping.put(NAME, R.id.dose_column);
        ConfigurableViewsLibrary.getInstance().getConfigurableViewsHelper().processRegisterColumns(mapping, convertView, visibleColumns, R.id.register_columns);
        */
    }

    private void populateRejectedViews(CommonPersonObjectClient pc,RegisterViewHolder viewHolder){
       String detailsStatus =  org.smartregister.util.Utils.getValue(pc.getColumnmaps(),DBConstants.KEY.DETAILSSTATUS,false);
       if("0".equalsIgnoreCase(detailsStatus)){
           viewHolder.register_columns.setBackgroundColor(context.getResources().getColor(android.R.color.holo_orange_light));
       }
    }
    private void populatePatientColumn(CommonPersonObjectClient pc, SmartRegisterClient client, RegisterViewHolder viewHolder) {

        String firstName = org.smartregister.util.Utils.getValue(pc.getColumnmaps(), DBConstants.KEY.FIRST_NAME, true);
        String lastName = org.smartregister.util.Utils.getValue(pc.getColumnmaps(), DBConstants.KEY.LAST_NAME, true);
        String phoneNumber = org.smartregister.util.Utils.getValue(pc.getColumnmaps(), DBConstants.KEY.PHONE_NUMBER, true);
        String para = org.smartregister.util.Utils.getValue(pc.getColumnmaps(), "para", true);
        if (lastName.equalsIgnoreCase("null") || lastName == null) {
            lastName = "";
        }
        String patientName = getName(firstName, lastName);

        fillValue(viewHolder.patientName, WordUtils.capitalize(patientName));

        String dobString = Utils.getDuration(org.smartregister.util.Utils.getValue(pc.getColumnmaps(), DBConstants.KEY.DOB, false));
        dobString = dobString.contains("y") ? dobString.substring(0, dobString.indexOf("y")) : dobString;
//        fillValue((viewHolder.age), String.format(context.getString(R.string.age_text), dobString));
        fillValue(viewHolder.age, phoneNumber);
        attachPatientOnclickListener(viewHolder.registericon, client);
        String last_interacted_with = org.smartregister.util.Utils.getValue(pc.getColumnmaps(), DBConstants.KEY.LAST_INTERACTED_WITH, true);
        try {
            Date last_interacted_date = new Date(Long.parseLong(last_interacted_with));

            last_interacted_with = last_interacted_date.toString();
            String d[] = last_interacted_with.split(" ");
            last_interacted_with = d[1] + " " + d[2] + " " + d[5];

        } catch (Exception e) {

        }

        fillValue(viewHolder.last_interacted_with, last_interacted_with);
        View patient = viewHolder.patientColumn;
        attachPatientOnclickListener(patient, client);
        fillValue(viewHolder.ancId, para);

        View dueButton = viewHolder.dueButton;
        attachDosageOnclickListener(dueButton, client);


        View riskLayout = viewHolder.risk;
        attachRiskLayoutOnclickListener(riskLayout, client);
        viewHolder.registericon.setTag(org.smartregister.R.id.entity_id, pc.entityId());
        //viewHolder.registericon.setImageDrawable(context.getResources().getDrawable(R.drawable.household_cbhc_placeholder));
        if (pc.entityId() != null) { //image already in local storage most likely ):
            //set profile image by passing the client id.If the image doesn't exist in the image repository then download and save locally
            DrishtiApplication.getCachedImageLoaderInstance().getImageByClientId(pc.entityId(), OpenSRPImageLoader.getStaticImageListener(viewHolder.registericon, R.drawable.household_cbhc_placeholder, R.drawable.household_cbhc_placeholder));
//            DrishtiApplication.getCachedImageLoaderInstance().getImageByClientId((String)viewHolder.registericon.getTag(), OpenSRPImageLoader.getStaticImageListener(viewHolder.registericon, 0, 0));
        } else {
            viewHolder.registericon.setImageResource(R.drawable.household_cbhc_placeholder);
        }

    }


    private void populateIdentifierColumn(CommonPersonObjectClient pc, RegisterViewHolder viewHolder) {
        String ancId = org.smartregister.util.Utils.getValue(pc.getColumnmaps(), DBConstants.KEY.ANC_ID, false);
        //fillValue(viewHolder.ancId, String.format(context.getString(R.string.anc_id_text), ancId));
    }


    private void populateLastColumn(CommonPersonObjectClient pc, RegisterViewHolder viewHolder) {

        if (commonRepository != null) {
            CommonPersonObject commonPersonObject = commonRepository.findByBaseEntityId(pc.entityId());
            if (commonPersonObject != null) {
                viewHolder.sync.setVisibility(View.GONE);
                viewHolder.dueButton.setVisibility(View.VISIBLE);

                //updateDoseButton();
            } else {
                viewHolder.dueButton.setVisibility(View.GONE);
                viewHolder.sync.setVisibility(View.VISIBLE);

                attachSyncOnclickListener(viewHolder.sync, pc);
            }
        }
    }

    /*private void updateDoseButton(){
        DoseStatus doseStatus = Utils.getCurrentDoseStatus(pc);

        Button patient = (Button) view.findViewById(R.id.dose_button);

        LinearLayout completeView = (LinearLayout) view.findViewById(R.id.completedView);

        if (StringUtils.isNotBlank(doseStatus.getDateDoseTwoGiven())) {
            patient.setVisibility(View.GONE);
            completeView.setVisibility(View.VISIBLE);
        } else {

            patient.setVisibility(View.VISIBLE);
            completeView.setVisibility(View.GONE);
            patient.setText(getDoseButtonText(doseStatus));
            patient.setBackground(Utils.getDoseButtonBackground(context, Utils.getRegisterViewButtonStatus(doseStatus)));
            patient.setTextColor(Utils.getDoseButtonTextColor(context, Utils.getRegisterViewButtonStatus(doseStatus)));
            attachDosageOnclickListener(patient, pc);
        }
    }*/

    private void attachSyncOnclickListener(View view, SmartRegisterClient client) {
        view.setOnClickListener(onClickListener);
        view.setTag(client);
        view.setTag(R.id.VIEW_ID, BaseRegisterFragment.CLICK_VIEW_SYNC);
    }

    private void attachPatientOnclickListener(View view, SmartRegisterClient client) {
        view.setOnClickListener(onClickListener);
        view.setTag(client);
        view.setTag(R.id.VIEW_ID, BaseRegisterFragment.CLICK_VIEW_NORMAL);
    }

    private void attachRiskLayoutOnclickListener(View view, SmartRegisterClient client) {
        view.setOnClickListener(onClickListener);
        view.setTag(client);
        view.setTag(R.id.VIEW_ID, BaseRegisterFragment.CLICK_VIEW_ATTENTION_FLAG);
    }

    /*
    private void adjustLayoutParams(View view, TextView details) {
        ViewGroup.LayoutParams params = view.getLayoutParams();
        params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        view.setLayoutParams(params);

        params = details.getLayoutParams();
        params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        details.setLayoutParams(params);
    }
*/

    @Override
    public void getFooterView(RecyclerView.ViewHolder viewHolder, int currentPageCount, int totalPageCount, boolean hasNext, boolean hasPrevious) {
        FooterViewHolder footerViewHolder = (FooterViewHolder) viewHolder;
        footerViewHolder.pageInfoView.setText(
                MessageFormat.format(context.getString(R.string.str_page_info), currentPageCount,
                        totalPageCount));

        footerViewHolder.nextPageView.setVisibility(hasNext ? View.VISIBLE : View.INVISIBLE);
        footerViewHolder.previousPageView.setVisibility(hasPrevious ? View.VISIBLE : View.INVISIBLE);

        footerViewHolder.nextPageView.setOnClickListener(paginationClickListener);
        footerViewHolder.previousPageView.setOnClickListener(paginationClickListener);
    }

    private void attachDosageOnclickListener(View view, SmartRegisterClient client) {
        view.setOnClickListener(onClickListener);
        view.setTag(client);
        view.setTag(R.id.VIEW_ID, BaseRegisterFragment.CLICK_VIEW_DOSAGE_STATUS);
    }

    @Override
    public SmartRegisterClients updateClients(FilterOption villageFilter, ServiceModeOption serviceModeOption, FilterOption searchFilter, SortOption sortOption) {
        return null;
    }

    @Override
    public void onServiceModeSelected(ServiceModeOption serviceModeOption) {//Implement Abstract Method
    }

    @Override
    public OnClickFormLauncher newFormLauncher(String formName, String entityId, String metaData) {
        return null;
    }

    @Override
    public LayoutInflater inflater() {
        return inflater;
    }

    @Override
    public RegisterViewHolder createViewHolder(ViewGroup parent) {
        View view;
        view = inflater.inflate(R.layout.register_home_list_row, parent, false);

        /*
        ConfigurableViewsHelper helper = ConfigurableViewsLibrary.getInstance().getConfigurableViewsHelper();
        if (helper.isJsonViewsEnabled()) {

            ViewConfiguration viewConfiguration = helper.getViewConfiguration(Constants.CONFIGURATION.HOME_REGISTER_ROW);
            ViewConfiguration commonConfiguration = helper.getViewConfiguration(COMMON_REGISTER_ROW);

            if (viewConfiguration != null) {
                return helper.inflateDynamicView(viewConfiguration, commonConfiguration, view, R.id.register_columns, false);
            }
        }*/

        return new RegisterViewHolder(view);
    }

    public static void fillValue(TextView v, String value) {
        if (v != null)
            v.setText(value);

    }

    class MemberCount {
        int memberCount;
        int femaleChildCount;
        int maleChildCount;
        int pregnantCount;
    }

    private void populateMemberCountColumn(MemberCount mc, RegisterViewHolder viewHolder) {

        TextView countView = viewHolder.memberCount;
        TextView femalechildcount = viewHolder.femalechild;
        TextView malechildcount = viewHolder.malechild;
        TextView pregnantcountView = viewHolder.pregnantcount;

        ImageView femalechildpresent = viewHolder.femalepresent;
        ImageView malechildpresent = viewHolder.malepresent;
        ImageView pregnantpresent = viewHolder.pregnantpresent;

        countView.setText("Members: " + mc.memberCount);
        if (mc.femaleChildCount > 0) {
            femalechildcount.setVisibility(View.VISIBLE);
            femalechildpresent.setVisibility(View.VISIBLE);
            femalechildcount.setText("" + mc.femaleChildCount);
        } else {
            femalechildcount.setVisibility(View.GONE);
            femalechildpresent.setVisibility(View.GONE);
        }
        if (mc.maleChildCount > 0) {
            malechildcount.setVisibility(View.VISIBLE);
            malechildpresent.setVisibility(View.VISIBLE);
            malechildcount.setText("" + mc.maleChildCount);
        } else {
            malechildcount.setVisibility(View.GONE);
            malechildpresent.setVisibility(View.GONE);
        }
        if (mc.pregnantCount > 0) {
            pregnantcountView.setVisibility(View.VISIBLE);
            pregnantpresent.setVisibility(View.VISIBLE);
            pregnantcountView.setText("" + mc.pregnantCount);
        } else {
            pregnantcountView.setVisibility(View.GONE);
            pregnantpresent.setVisibility(View.GONE);
        }

    }

    public static final HashMap<String, MemberCount> memberCountHashMap = new HashMap<>();

    class MemberCountAsyncTask extends AsyncTask {
        CommonPersonObjectClient pc;
        int count = 0;
        int femalechild = 0;
        int malechild = 0;
        int pregnantcount = 0;
        TextView countView;
        TextView femalechildcount;
        TextView malechildcount;
        TextView pregnantcountView;

        ImageView femalechildpresent;
        ImageView malechildpresent;
        ImageView pregnantpresent;

        public MemberCountAsyncTask(CommonPersonObjectClient pc, RegisterViewHolder viewHolder) {
            this.pc = pc;
            this.countView = viewHolder.memberCount;
            this.femalechildcount = viewHolder.femalechild;
            this.malechildcount = viewHolder.malechild;
            this.pregnantcountView = viewHolder.pregnantcount;

            this.femalechildpresent = viewHolder.femalepresent;
            this.malechildpresent = viewHolder.malepresent;
            this.pregnantpresent = viewHolder.pregnantpresent;
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            Cursor cursor;
//            try{
//                cursor = AncApplication.getInstance().getContext().commonrepository("ec_member").rawCustomQueryForAdapter("Select Count(*) from ec_member where relational_id = '"+pc.getCaseId()+"' and date_removed IS NULL");
//                cursor.moveToFirst();
//                count = count+Integer.parseInt(cursor.getString(0));
//                cursor.close();
//            }catch (Exception e){
//                Log.e("member error",e.getMessage());
//            }
//            try{
//                cursor = AncApplication.getInstance().getContext().commonrepository("ec_child").rawCustomQueryForAdapter("Select Count(*) from ec_child where relational_id = '"+pc.getCaseId()+"' and date_removed IS NULL");
//                cursor.moveToFirst();
//                count = count+Integer.parseInt(cursor.getString(0));
//                cursor.close();
//            }catch (Exception e){
//
//            }
//            try{
//                cursor = AncApplication.getInstance().getContext().commonrepository("ec_woman").rawCustomQueryForAdapter("Select Count(*) from ec_woman where relational_id = '"+pc.getCaseId()+"' and date_removed IS NULL");
//                cursor.moveToFirst();
//                count = count+Integer.parseInt(cursor.getString(0));
//                cursor.close();
//            }catch (Exception e){
//
//            }
            try {
                AncRepository repo = (AncRepository) AncApplication.getInstance().getRepository();
                SQLiteDatabase db = repo.getReadableDatabase();
                String sql = "Select base_entity_id from (Select base_entity_id from ec_member where relational_id='" + pc.getCaseId() + "' and date_removed IS NULL UNION Select base_entity_id from ec_woman where relational_id='" + pc.getCaseId() + "' and date_removed IS NULL UNION Select base_entity_id from ec_child where relational_id='" + pc.getCaseId() + "' and date_removed IS NULL " + ") group by base_entity_id";
                cursor = db.rawQuery(sql, new String[]{});
//                cursor.moveToFirst();
                count = cursor.getCount();
                cursor.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                cursor = AncApplication.getInstance().getContext().commonrepository("ec_child").rawCustomQueryForAdapter("Select Count(*) from ec_child where relational_id = '" + pc.getCaseId() + "'"
                        + " and ec_child.id in (Select base_entity_id from ec_details where key = 'gender' and value = 'F') and date_removed IS NULL;"
                );
                cursor.moveToFirst();
                femalechild = femalechild + Integer.parseInt(cursor.getString(0));
                cursor.close();
            } catch (Exception e) {

            }
            try {
                cursor = AncApplication.getInstance().getContext().commonrepository("ec_child").rawCustomQueryForAdapter("Select Count(*) from ec_child where relational_id = '" + pc.getCaseId() + "'"
                        + " and ec_child.id in (Select base_entity_id from ec_details where key = 'gender' and value = 'M') and date_removed IS NULL;"
                );
                cursor.moveToFirst();
                malechild = malechild + Integer.parseInt(cursor.getString(0));
                cursor.close();
            } catch (Exception e) {

            }
            try {
                cursor = AncApplication.getInstance().getContext().commonrepository("ec_woman").rawCustomQueryForAdapter("Select Count(*) from ec_woman where relational_id = '" + pc.getCaseId() + "'"
                        + " and ec_woman.id in (Select base_entity_id from ec_details where key = 'PregnancyStatus' and (value like '%Antenatal Period%' or value like '%প্রসব পূর্ব%')) and date_removed IS NULL;"
                );
                cursor.moveToFirst();
                pregnantcount = pregnantcount + Integer.parseInt(cursor.getString(0));
                cursor.close();
            } catch (Exception e) {

            }

            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            countView.setText("Members: " + count);
            if (femalechild > 0) {
                femalechildcount.setVisibility(View.VISIBLE);
                femalechildpresent.setVisibility(View.VISIBLE);
                femalechildcount.setText("" + femalechild);
            } else {
                femalechildcount.setVisibility(View.GONE);
                femalechildpresent.setVisibility(View.GONE);
            }
            if (malechild > 0) {
                malechildcount.setVisibility(View.VISIBLE);
                malechildpresent.setVisibility(View.VISIBLE);
                malechildcount.setText("" + malechild);
            } else {
                malechildcount.setVisibility(View.GONE);
                malechildpresent.setVisibility(View.GONE);
            }
            if (pregnantcount > 0) {
                pregnantcountView.setVisibility(View.VISIBLE);
                pregnantpresent.setVisibility(View.VISIBLE);
                pregnantcountView.setText("" + pregnantcount);
            } else {
                pregnantcountView.setVisibility(View.GONE);
                pregnantpresent.setVisibility(View.GONE);
            }
            MemberCount mc = new MemberCount();
            mc.memberCount = count;
            mc.pregnantCount = pregnantcount;
            mc.maleChildCount = malechild;
            mc.femaleChildCount = femalechild;
            memberCountHashMap.put(pc.entityId(), mc);
        }
    }

    ////////////////////////////////////////////////////////////////
    // Inner classes
    ////////////////////////////////////////////////////////////////

    public class RegisterViewHolder extends RecyclerView.ViewHolder {
        public ImageView registericon;
        public TextView patientName;
        public TextView age;
        public TextView ga;
        public TextView ancId;
        public TextView memberCount;
        public TextView femalechild;
        public TextView malechild;
        public TextView pregnantcount;
        public ImageView femalepresent;
        public ImageView malepresent;
        public ImageView pregnantpresent;
        public TextView last_interacted_with;
        public TextView risk;
        public ImageView dueButton;
        public Button sync;
        public View patientColumn;
        public View register_columns;
        public RegisterViewHolder(View itemView) {
            super(itemView);
            registericon = itemView.findViewById(R.id.imageViewregistericon);
            patientName = itemView.findViewById(R.id.patient_name);
            age = itemView.findViewById(R.id.age);
            ga = itemView.findViewById(R.id.ga);
            ancId = itemView.findViewById(R.id.anc_id);
            risk = itemView.findViewById(R.id.risk);
            dueButton = itemView.findViewById(R.id.due_button);
            sync = itemView.findViewById(R.id.sync);
            memberCount = itemView.findViewById(R.id.member_count);
            femalechild = itemView.findViewById(R.id.child_girl_count);
            malechild = itemView.findViewById(R.id.child_boy_count);
            pregnantcount = itemView.findViewById(R.id.pregnant_woman_count);

            malepresent = itemView.findViewById(R.id.male_child_present);
            femalepresent = itemView.findViewById(R.id.female_child_present);
            pregnantpresent = itemView.findViewById(R.id.pregnant_woman_present);

            patientColumn = itemView.findViewById(R.id.patient_column);
            last_interacted_with = itemView.findViewById(R.id.last_interacted_with);
            register_columns = itemView.findViewById(R.id.register_columns);
        }
    }

    @Override
    public RecyclerView.ViewHolder createFooterHolder(ViewGroup parent) {
        View view = inflater.inflate(R.layout.smart_register_pagination, parent, false);
        return new FooterViewHolder(view);
    }

    @Override
    public boolean isFooterViewHolder(RecyclerView.ViewHolder viewHolder) {
        return FooterViewHolder.class.isInstance(viewHolder);
    }

    public class FooterViewHolder extends RecyclerView.ViewHolder {
        public TextView pageInfoView;
        public Button nextPageView;
        public Button previousPageView;

        public FooterViewHolder(View view) {
            super(view);

            nextPageView = view.findViewById(org.smartregister.R.id.btn_next_page);
            previousPageView = view.findViewById(org.smartregister.R.id.btn_previous_page);
            pageInfoView = view.findViewById(org.smartregister.R.id.txt_page_info);
        }
    }


}
