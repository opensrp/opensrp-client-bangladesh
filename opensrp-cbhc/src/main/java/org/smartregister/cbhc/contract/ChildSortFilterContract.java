package org.smartregister.cbhc.contract;

import android.content.Context;

import org.smartregister.configurableviews.model.Field;
import org.smartregister.configurableviews.model.RegisterConfiguration;

import java.util.List;

public interface ChildSortFilterContract {

    interface View {

        Context getContext();

        void updateSortAndFilter(List<Field> filterList, Field sortField);

        void updateSortLabel(String sortText);

        void clearFilter();
    }

    interface Presenter {

        void updateSortAndFilter();

        void updateSort();

        RegisterConfiguration getConfig();

        List<Field> getFilterList();

        Field getSortField();

        void setSortField(Field sortField);

        void clearSortAndFilter();
    }
}
