package org.smartregister.path.fragment.mocks;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import org.smartregister.path.fragment.AdvancedSearchFragment;

/**
 * Created by kaderchowdhury on 06/12/17.
 */

public class AdvancedSearchFragmentMock extends AdvancedSearchFragment {
    public AdvancedSearchFragmentMock() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    protected void onCreation() {
        super.onCreation();
    }

    @Override
    protected void onResumption() {
        super.onResumption();
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
    }

    @Override
    public void setupViews(View view) {
        super.setupViews(view);
    }

    @Override
    public void setupSearchView(View view) {
        super.setupSearchView(view);
    }

    @Override
    protected void startRegistration() {
        super.startRegistration();
    }

    @Override
    public void search(View view) {
        super.search(view);
    }

    @Override
    public void CountExecute() {
        super.CountExecute();
    }

    @Override
    public String filterandSortQuery() {
        return super.filterandSortQuery();
    }

    @Override
    public boolean onBackPressed() {
        return super.onBackPressed();
    }

    @Override
    protected void goBack() {
        super.goBack();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return super.onCreateLoader(id, args);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        super.onLoadFinished(loader, cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        super.onLoaderReset(loader);
    }

    @Override
    public void updateFilterCount(int count) {
        super.updateFilterCount(count);
    }

    @Override
    public EditText getZeirId() {
        return super.getZeirId();
    }

    @Override
    public void showProgressView() {
        super.showProgressView();
    }

    @Override
    public void hideProgressView() {
        super.hideProgressView();
    }
}
