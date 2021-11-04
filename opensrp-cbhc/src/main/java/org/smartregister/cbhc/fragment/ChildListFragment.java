package org.smartregister.cbhc.fragment;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
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
import org.smartregister.cbhc.domain.ChildItemData;
import org.smartregister.cbhc.util.Constants;
import org.smartregister.commonregistry.CommonPersonObjectClient;

import java.util.ArrayList;

public class ChildListFragment extends Fragment {

    private RecyclerView childListRv;
    public TextView totalChildTv;
    private EditText editSearch;
    ArrayList<ChildItemData> childItemDataArrayList = new ArrayList<>();
    View filter_tv;
    ChildListAdapter adapter;
    public ChildListFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.fragment_child_list, container, false);

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

        childListRv = view.findViewById(R.id.childListRv);
        totalChildTv = view.findViewById(R.id.total_child_tv);
        editSearch = view.findViewById(R.id.edt_search);
        filter_tv = view.findViewById(R.id.filter_text_view);

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
                FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
                ft.replace(R.id.frameLayout, new SortFilterFragment());
                ft.commit();
            }
        });

        getAllChildList();


        return view;
    }



    private void getAllChildList() {
        childItemDataArrayList.clear();
        SQLiteDatabase sqLiteDatabase = AncApplication.getInstance().getRepository().getWritableDatabase();
        Cursor cursor = sqLiteDatabase.rawQuery( "select * from ec_child order by last_interacted_with DESC", null);
        if (cursor != null && cursor.getCount() > 0 && cursor.moveToFirst()) {
            while (!cursor.isAfterLast()) {
                childItemDataArrayList.add(new ChildItemData(
                        cursor.getString(cursor.getColumnIndex("base_entity_id")),
                        cursor.getString(cursor.getColumnIndex("first_name")),
                        cursor.getString(cursor.getColumnIndex("last_name")),
                        cursor.getString(cursor.getColumnIndex("dob")),
                        cursor.getString(cursor.getColumnIndex("gender")),
                        ""));
                cursor.moveToNext();
            }
        }
        adapter = new ChildListAdapter(getActivity(), childItemDataArrayList, new ChildListAdapter.TotalChild() {
            @Override
            public void onChange(String totalStr) {
                totalChildTv.setText(totalStr);
            }
        }, new ChildListAdapter.OnClickHandler() {
            @Override
            public void onClick(int position, ChildItemData childItemData) {
             /*   CommonPersonObjectClient memberclient = (CommonPersonObjectClient) view.getTag(R.id.clientformemberprofile);
                memberclient.getColumnmaps().put("relational_id", householdDetails.getCaseId());
                String clienttype = (String) view.getTag(R.id.typeofclientformemberprofile);*/
                Intent intent = new Intent(getActivity(), MemberProfileActivity.class);
                intent.putExtra(Constants.INTENT_KEY.BASE_ENTITY_ID, childItemData.getBaseEntityId());
                /*intent.putExtra(ProfileOverviewFragment.EXTRA_HOUSEHOLD_DETAILS, memberclient);
                intent.putExtra("type_of_member", clienttype);*/
                startActivityForResult(intent, 1002);
            }
        });
        childListRv.setLayoutManager(new LinearLayoutManager(getActivity()));
        childListRv.setAdapter(adapter);
    }
}