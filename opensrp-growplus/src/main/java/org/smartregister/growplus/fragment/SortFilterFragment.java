package org.smartregister.growplus.fragment;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.TextView;


import org.apache.commons.lang3.StringUtils;

import org.smartregister.growplus.R;
import org.smartregister.growplus.activity.ChildSmartRegisterActivity;
import org.smartregister.growplus.activity.HouseholdSmartRegisterActivity;
import org.smartregister.growplus.activity.WomanSmartRegisterActivity;
import org.smartregister.growplus.view.Field;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import util.SortFilterUtil;


public class SortFilterFragment extends BaseSmartRegisterFragment {

    private FilterDialogClickListener actionHandler = new FilterDialogClickListener();



    private FilterAdapter filterAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initializePresenter();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(
                R.layout.fragment_sort_filter,
                container, false);

        updateFilterList(view,SortFilterUtil.getFields(SortFilterUtil.FieldType.FILTER));
        updateSortLabel("");
        View sortLayout = view.findViewById(R.id.sort_layout);
        sortLayout.setOnClickListener(actionHandler);

        View apply = view.findViewById(R.id.apply_layout);
        apply.setOnClickListener(actionHandler);

        View buttonApply = view.findViewById(R.id.button_apply);
        buttonApply.setOnClickListener(actionHandler);

        View clear = view.findViewById(R.id.clear_filter);
        clear.setOnClickListener(actionHandler);

        View cancel = view.findViewById(R.id.cancel_filter);
        cancel.setOnClickListener(actionHandler);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

    }

    private void initializePresenter()
    {
        SortFilterUtil.init();
        ArrayList<HashMap<String, String>> wardlist = context().commonrepository("ec_mother").rawQuery("select * from ec_details where ec_details.key='address2' group by ec_details.value");
        String[]wards = new String[wardlist.size()];
        for (HashMap<String, String>ward:wardlist){
                wards[wardlist.indexOf(ward)] = ward.get("value");
        }
        SortFilterUtil.initFilterFields(wards);
        boolean household = getActivity() instanceof HouseholdSmartRegisterActivity;
        SortFilterUtil.initSortFields(household);
    }

    private void switchToRegister() {
        if (getActivity() != null) {
            if(getActivity() instanceof HouseholdSmartRegisterActivity){
                ((HouseholdSmartRegisterActivity) getActivity()).switchToBaseFragment();
            }
            if(getActivity() instanceof WomanSmartRegisterActivity){
                ((WomanSmartRegisterActivity) getActivity()).switchToBaseFragment();
            }
            if(getActivity() instanceof ChildSmartRegisterActivity){
                ((ChildSmartRegisterActivity) getActivity()).switchToBaseFragment();
            }

        }
    }

    public void updateSortLabel(String sortText) {

        int currentChecked = -1;
            currentChecked = SortFilterUtil.currentChecked(SortFilterUtil.FieldType.SORT);
        if(currentChecked!=-1)
            sortText = SortFilterUtil.getFields(SortFilterUtil.FieldType.SORT).get(currentChecked).getDisplayName();
        if (getView() != null && StringUtils.isNotBlank(sortText)) {
            TextView sortLabel = getView().findViewById(R.id.sort_label);
            sortLabel.setText(Html.fromHtml(sortText));
        }
    }

    protected void updateFilterList(final View view, final List<Field> filterList) {

        if (filterList == null) {
            return;
        }

        RecyclerView recyclerView = view.findViewById(R.id.filter_recycler_view);
        recyclerView.setHasFixedSize(true);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);

        DividerItemDecoration itemDecor = new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL);
        recyclerView.addItemDecoration(itemDecor);

        filterAdapter = new FilterAdapter(filterList);
        recyclerView.setAdapter(filterAdapter);
    }

    private void updateSortList(final List<Field> sortFields) {


        int currentChecked = -1;
        currentChecked = SortFilterUtil.currentChecked(SortFilterUtil.FieldType.SORT);
        if(currentChecked!=-1)
            updateSortLabel(SortFilterUtil.getFields(SortFilterUtil.FieldType.SORT).get(currentChecked).getDisplayName());
        final SortArrayAdapter arrayAdapter = new SortArrayAdapter(getActivity(), sortFields);
        AlertDialog.Builder builderSingle = new AlertDialog.Builder(getContext(), R.style.AncAlertDialog);
        builderSingle.setSingleChoiceItems(arrayAdapter, currentChecked, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                SortFilterUtil.setChecked(SortFilterUtil.FieldType.SORT,which);
                updateSortLabel(SortFilterUtil.getFields(SortFilterUtil.FieldType.SORT).get(which).getDisplayName());
            }
        });

        builderSingle.setNegativeButton(getString(R.string.done), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });


        builderSingle.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {

            }
        });

        builderSingle.show();
    }


    ////////////////////////////////////////////////////////////////
    // Inner classes
    ////////////////////////////////////////////////////////////////
    private class FilterDialogClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {

            switch (v.getId()) {
                case R.id.cancel_filter:
                    switchToRegister();
                    break;
                case R.id.apply_layout:
                    v.findViewById(R.id.button_apply).performClick();
                    break;
                case R.id.button_apply:
                    applySortAndFilter();
                    break;
                case R.id.clear_filter:
                    filterAdapter.clear();
                    break;
                case R.id.sort_layout:
                    updateSortList(SortFilterUtil.getFields(SortFilterUtil.FieldType.SORT));
                    break;
                default:
                    break;
            }
        }
    }

    public void applySortAndFilter(){
        if (getActivity() instanceof HouseholdSmartRegisterActivity){
            ((HouseholdSmartRegisterActivity) getActivity()).applySortAndFilter();
        }
        if (getActivity() instanceof WomanSmartRegisterActivity){
            ((WomanSmartRegisterActivity) getActivity()).applySortAndFilter();
        }
        if (getActivity() instanceof ChildSmartRegisterActivity){
            ((ChildSmartRegisterActivity) getActivity()).applySortAndFilter();
        }

    }

    private class FilterAdapter extends RecyclerView.Adapter<FilterAdapter.ViewHolder> {
        private List<Field> filterList;

        public class ViewHolder extends RecyclerView.ViewHolder {
            public CheckedTextView checkedTextView;

            public ViewHolder(CheckedTextView v) {
                super(v);
                checkedTextView = v;
            }
        }

        public FilterAdapter(List<Field> filterList) {
            this.filterList = filterList;
        }

        @Override
        public FilterAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                           int viewType) {
            CheckedTextView v = (CheckedTextView) LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.register_filter_item, parent, false);

            ViewHolder vh = new ViewHolder(v);
            return vh;
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, final int position) {
            Field field = filterList.get(position);
            holder.checkedTextView.setText(field.getDisplayName());
            holder.checkedTextView.setTag(field);

            holder.checkedTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((CheckedTextView) v).toggle();
                    boolean isChecked = ((CheckedTextView) v).isChecked();
                    Object tag = v.getTag();
                    if (tag != null && tag instanceof Field) {
                        Field currentField = (Field) tag;
                        SortFilterUtil.setChecked(SortFilterUtil.FieldType.FILTER,position);
                    }
                }
            });
            holder.checkedTextView.setChecked(SortFilterUtil.isChecked(SortFilterUtil.FieldType.FILTER,position));

        }

        @Override
        public int getItemCount() {
            return filterList.size();
        }

        public void clear() {
//            presenter.getFilterList().clear();
            notifyDataSetChanged();
        }
    }

    private class SortArrayAdapter extends ArrayAdapter<Field> {

        private Context context;
        private List<Field> list = new ArrayList<>();

        private SortArrayAdapter(@NonNull Context context, @NonNull List<Field> list) {
            super(context, 0, list);
            this.context = context;
            this.list = list;
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            View view = convertView;
            if (view == null) {
                view = LayoutInflater.from(context).inflate(R.layout.register_sort_item, parent, false);
            }

            Field field = list.get(position);

            TextView text = (TextView) view;
            text.setTag(field);
            text.setText(field.getDisplayName());

            return view;
        }
    }
}
