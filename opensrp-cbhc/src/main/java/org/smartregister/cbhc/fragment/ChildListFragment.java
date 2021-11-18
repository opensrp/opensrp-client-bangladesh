package org.smartregister.cbhc.fragment;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import net.sqlcipher.database.SQLiteDatabase;

import org.smartregister.cbhc.R;
import org.smartregister.cbhc.activity.MemberProfileActivity;
import org.smartregister.cbhc.adapter.ChildListAdapter;
import org.smartregister.cbhc.application.AncApplication;
import org.smartregister.cbhc.contract.ChildListContract;
import org.smartregister.cbhc.domain.ChildItemData;
import org.smartregister.cbhc.presenter.ChildListPresenter;
import org.smartregister.cbhc.util.Constants;
import org.smartregister.cbhc.util.DBConstants;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.configurableviews.model.Field;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ChildListFragment extends Fragment implements ChildListContract.View {

    private static ChildListFragment instance=null;
    private RecyclerView childListRv;
    public TextView totalChildTv;
    private EditText editSearch;
    private View filter_tv;
    private ChildListAdapter adapter;
    private FragmentTransaction ft;
    private  FragmentManager fragmentManager;
    private boolean fromFilter=false;
    ChildListPresenter presenter;
    private String selectQuery = "Select child.id as _id , child.relationalid , child.Patient_Identifier, child.first_name , child.last_name , child.dob ,child.gender, child.PregnancyStatus, child.tasks, child.relation_with_household as relation, child.age as age, NULL as MaritalStatus, child.camp_type, child.child_status FROM ec_child as child";
    //private Childl

    public ChildListFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        presenter = new ChildListPresenter(this);
        presenter.fetchChildList(selectQuery);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.fragment_child_list, container, false);
        ft = getActivity().getSupportFragmentManager().beginTransaction();
        instance = this;

        if (fragmentManager==null) fragmentManager = getFragmentManager();

        //toolbar view comp
        ImageView backImage = view.findViewById(R.id.back_arrow_im);
        TextView toolbarText = view.findViewById(R.id.toolbar_text);
        toolbarText.setText(R.string.child_list);

        backImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().finish();
            }
        });


        totalChildTv = view.findViewById(R.id.total_child_tv);
        editSearch = view.findViewById(R.id.edt_search);
        filter_tv = view.findViewById(R.id.filter_text_view);
        childListRv = view.findViewById(R.id.childListRv);

        editSearch.setHint("Find Name");
        editSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                adapter.getFilter().filter(charSequence);
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        filter_tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ft.replace(R.id.frameLayout, new ChildSortFilterFragment()).addToBackStack("child_filter_frag");
                ft.commit();
            }
        });

        return view;
    }

    /**
     * this method will be call when close button will be call from filter dialog
     */
    public void removeFromBackStack(){
        if (fragmentManager==null) fragmentManager = getFragmentManager();
        presenter.fetchChildList(selectQuery);
        fragmentManager.popBackStack();
    }

    /**
     * passing instance
     * @return
     */
    public static ChildListFragment getInstance() {
        return instance;
    }

    /**
     * child filter query producer
     * @param filterList
     * @param sortField
     * @param selectedType
     */
    public void filter(List<Field> filterList, Field sortField, String selectedType) {
        String selectedTypeTemp="";
        fromFilter = true;
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        String TWO_MONTHS = format.format( new Date(new Date().getTime()-2l*32l*24l*60l*60l*1000l));
        String FIVE_YEAR = format.format( new Date(new Date().getTime()-5l*12l*30l*24l*60l*60l*1000l));


        if(selectedType.isEmpty())selectedTypeTemp = "";
        else selectedTypeTemp = " AND child.camp_type = '"+selectedType+"'";

        String sortQuery = " order by child."+sortField.getDbAlias();



        if(!filterList.isEmpty()){
            if(filterList.get(0).getDbAlias().equals("infant")){
                presenter.fetchChildList(selectQuery+" where child.dob > DATE('"+TWO_MONTHS+"')"+selectedTypeTemp+sortQuery);
                fragmentManager.popBackStack();
            }else if(filterList.get(0).getDbAlias().equals("toddler")){
                presenter.fetchChildList(selectQuery+" where child.dob < DATE('"+TWO_MONTHS+"') and child.dob > DATE('"+FIVE_YEAR+"')"+selectedTypeTemp+sortQuery);
                fragmentManager.popBackStack();
            }
        }else{
            if(!selectedType.isEmpty()){
                presenter.fetchChildList(selectQuery+" where child.camp_type = '"+selectedType+"'"+sortQuery);
            }else{
                presenter.fetchChildList(selectQuery+" "+sortQuery);
            }
            fragmentManager.popBackStack();
        }

    }

    @Override
    public ChildListContract.Presenter getPresenter() {
        return null;
    }

    /**
     * recyclerview updating here
     */
    @Override
    public void updateChildList() {
         if(adapter==null){
        adapter = new ChildListAdapter(getActivity(), presenter.getChildList(), new ChildListAdapter.TotalChild() {
            @Override
            public void onChange(String totalStr) {
                totalChildTv.setText(totalStr);
            }
        }, new ChildListAdapter.OnClickHandler() {
            @Override
            public void onClick(int position, ChildItemData childItemData) {
               /* if (childItemData.get().equals("") && membersData.getDob().equals("")) {
                    Toast.makeText(getActivity(), "Age not found.", Toast.LENGTH_SHORT).show();
                    return;
                }*/

                CommonPersonObjectClient memberclient = childItemData.getpClient();
                //memberclient.getColumnmaps().put("relational_id", householdDetails.getCaseId());
                String clienttype = childItemData.getGender().equalsIgnoreCase("m")?"malechild":"femalechild";
                Intent intent = new Intent(getActivity(), MemberProfileActivity.class);
                intent.putExtra(Constants.INTENT_KEY.BASE_ENTITY_ID, memberclient.getCaseId());
                intent.putExtra(ProfileOverviewFragment.EXTRA_HOUSEHOLD_DETAILS, memberclient);
                intent.putExtra("type_of_member", clienttype);
                startActivityForResult(intent, 1002);
            }
        });

        }else{
            adapter.setData(presenter.getChildList());
        }
        childListRv.setLayoutManager(new LinearLayoutManager(getActivity()));
        childListRv.setAdapter(adapter);
    }

    public void clearFilter() {
        presenter.fetchChildList(selectQuery);
        fragmentManager.popBackStack();
    }
}