package org.smartregister.growplus.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import org.smartregister.growplus.R;
import org.smartregister.growplus.adapter.CampaignListRvAdapter;
import org.smartregister.growplus.application.VaccinatorApplication;
import org.smartregister.growplus.domain.CampaignForm;
import org.smartregister.growplus.repository.CampaignRepository;

import java.util.ArrayList;

public class CampaignActivity extends AppCompatActivity {
    private ArrayList<CampaignForm> campaignFormList;
    private RecyclerView campaign_list_rv;
    private TextView not_fount_tv;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_campaign);
        getSupportActionBar().setTitle("Campaign");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        initView();
        campaignFormList = new ArrayList<>();

        findViewById(R.id.add_campaign_bt).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(CampaignActivity.this,AddCampaignActivity.class).putExtra("from","add"));
            }
        });
    }

    private void initView() {
        campaign_list_rv = findViewById(R.id.campaign_list_rv);
        not_fount_tv = findViewById(R.id.not_fount_tv);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        campaignFormList = new CampaignRepository(VaccinatorApplication.getInstance().getRepository()).getAllCampaign();
        campaign_list_rv.setLayoutManager(new LinearLayoutManager(this));
        campaign_list_rv.setAdapter(new CampaignListRvAdapter(this, campaignFormList, new CampaignListRvAdapter.OnClickAdapter() {
            @Override
            public void onClick(int position, CampaignForm content) {
                startActivity(new Intent(CampaignActivity.this,AddCampaignActivity.class)
                        .putExtra("from","update")
                        .putExtra("content", content));
            }
        }));

        if(campaignFormList.size()<=0) not_fount_tv.setVisibility(View.VISIBLE);
        else not_fount_tv.setVisibility(View.GONE);
    }
}