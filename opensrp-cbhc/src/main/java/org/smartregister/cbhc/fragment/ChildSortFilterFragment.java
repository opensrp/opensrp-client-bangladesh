package org.smartregister.cbhc.fragment;

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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.Spinner;
import android.widget.TextView;

import org.apache.commons.lang3.StringUtils;
import org.smartregister.cbhc.R;
import org.smartregister.cbhc.activity.HomeRegisterActivity;
import org.smartregister.cbhc.contract.ChildSortFilterContract;
import org.smartregister.cbhc.contract.SortFilterContract;
import org.smartregister.cbhc.presenter.ChildSortFilterPresenter;
import org.smartregister.cbhc.presenter.SortFilterPresenter;
import org.smartregister.configurableviews.model.Field;

import java.util.ArrayList;
import java.util.List;


public class ChildSortFilterFragment extends Fragment implements ChildSortFilterContract.View {

    private FilterDialogClickListener actionHandler = new FilterDialogClickListener();
    private ChildSortFilterPresenter presenter;

    private FilterAdapter filterAdapter;
    private String[] typeArr = new String[]{"Select Type", "Type 1", "Type 2"};
    private String selectedType="";

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
                R.layout.fragment_child_sort_filter,
                container, false);

        updateFilterList(view, presenter.getConfig().getFilterFields());

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

        Spinner type_sp = view.findViewById(R.id.type_sp);
        type_sp.setAdapter(new ArrayAdapter(getActivity(),android.R.layout.simple_spinner_item,typeArr));

        type_sp.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if(i!=0){
                    selectedType = typeArr[i];
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        presenter.updateSort();
    }

    private void initializePresenter() {
        presenter = new ChildSortFilterPresenter(this);
    }

    @Override
    public void updateSortAndFilter(List<Field> filterList, Field sortField) {
        /*if (getActivity() != null) {
                ((HomeRegisterActivity) getActivity()).updateSortAndFilter(filterList, sortField,selectedType);

        }*/

        ChildListFragment.getInstance().filter(filterList, sortField,selectedType);
    }

    private void switchToChidList() {
        ChildListFragment.getInstance().removeFromBackStack();
    }

    @Override
    public void clearFilter() {
      /*  if (getActivity() != null) {
            ((HomeRegisterActivity) getActivity()).clearFilter();
        }*/

        ChildListFragment.getInstance().clearFilter();
    }

    public void updateSortLabel(String sortText) {
        if (getView() != null && StringUtils.isNotBlank(sortText)) {
            TextView sortLabel = getView().findViewById(R.id.sort_label);
            sortLabel.setText(Html.fromHtml(sortText));
        }
    }

    protected void updateFilterList(final View view, final List<Field> filterList) {

        if (filterList == null) {
            return;
        }
        //removed pregnant, adult and above 50 item from list
        filterList.remove(0);
        filterList.remove(2);
        filterList.remove(2);

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
        if (presenter.getSortField() != null) {
            currentChecked = sortFields.indexOf(presenter.getSortField());
        }
        final SortArrayAdapter arrayAdapter = new SortArrayAdapter(getActivity(), sortFields);
        AlertDialog.Builder builderSingle = new AlertDialog.Builder(getContext(), R.style.AncAlertDialog);
        builderSingle.setSingleChoiceItems(arrayAdapter, currentChecked, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                presenter.setSortField(sortFields.get(which));
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
                presenter.updateSort();
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
                    switchToChidList();
                    break;
                case R.id.apply_layout:
                    v.findViewById(R.id.button_apply).performClick();
                    break;
                case R.id.button_apply:
                    presenter.updateSortAndFilter();
                    break;
                case R.id.clear_filter:

//                    filterAdapter.clear();
                    presenter.getFilterList().clear();
                    filterAdapter.notifyDataSetChanged();
                    presenter.clearSortAndFilter();
                    break;
                case R.id.sort_layout:
                    presenter.getConfig().getSortFields();
                    updateSortList(presenter.getConfig().getSortFields());
                    break;
                default:
                    break;
            }
        }
    }

    private class FilterAdapter extends RecyclerView.Adapter<FilterAdapter.ViewHolder> {
        private List<Field> filterList;

        public FilterAdapter(List<Field> filterList) {
            this.filterList = filterList;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent,
                                                           int viewType) {
            CheckedTextView v = (CheckedTextView) LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.register_filter_item, parent, false);

            ViewHolder vh = new ViewHolder(v);
            return vh;
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
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
                        if (isChecked) {
                            if (!presenter.getFilterList().contains(currentField)) {
                                clear();
                                presenter.getFilterList().add(currentField);
                            }
                        } else {
                            presenter.getFilterList().remove(currentField);
                        }
                    }
                }
            });

            if (presenter.getFilterList().contains(field) && !holder.checkedTextView.isChecked()) {
                holder.checkedTextView.setChecked(true);
            } else if (!presenter.getFilterList().contains(field) && holder.checkedTextView.isChecked()) {
                holder.checkedTextView.setChecked(false);
            }
        }

        @Override
        public int getItemCount() {
            return filterList.size();
        }

        public void clear() {
            presenter.getFilterList().clear();
            notifyDataSetChanged();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            public CheckedTextView checkedTextView;

            public ViewHolder(CheckedTextView v) {
                super(v);
                checkedTextView = v;
            }
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
