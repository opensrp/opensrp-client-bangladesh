package org.smartregister.cbhc.activity;

import android.app.Activity;
import android.database.Cursor;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import net.sqlcipher.database.SQLiteDatabase;

import org.smartregister.cbhc.R;
import org.smartregister.cbhc.adapter.ChildListAdapter;
import org.smartregister.cbhc.application.AncApplication;
import org.smartregister.cbhc.domain.CampaignForm;
import org.smartregister.cbhc.domain.ChildItemData;
import org.smartregister.cbhc.fragment.ChildListFragment;
import org.smartregister.cbhc.fragment.SortFilterFragment;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class ChildListMainActivity extends AppCompatActivity {
    private RecyclerView childListRv;
    public TextView totalChildTv;
    private EditText editSearch;
    ArrayList<ChildItemData> childItemDataArrayList = new ArrayList<>();
    View filter_tv;
    ChildListAdapter adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_child_list);

        getSupportActionBar().hide();
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.frameLayout, new ChildListFragment());
        ft.commit();
    }

}