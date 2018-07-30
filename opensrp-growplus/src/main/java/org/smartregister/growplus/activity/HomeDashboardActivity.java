package org.smartregister.growplus.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.LinearLayout;

import org.smartregister.growplus.R;

public class HomeDashboardActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_dashboard);
        final Bundle extras = this.getIntent().getExtras();
        LinearLayout household = (LinearLayout)findViewById(R.id.household_dashboard_button);
        household.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeDashboardActivity.this, HouseholdSmartRegisterActivity.class);

                intent.putExtras(extras);
                startActivity(intent);
            }
        });
        LinearLayout mother = (LinearLayout)findViewById(R.id.woman_dashboard_button);
        mother.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeDashboardActivity.this, WomanSmartRegisterActivity.class);

                intent.putExtras(extras);
                startActivity(intent);
            }
        });
        LinearLayout child = (LinearLayout)findViewById(R.id.child_dashboard_button);
        child.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeDashboardActivity.this, ChildSmartRegisterActivity.class);

                intent.putExtras(extras);
                startActivity(intent);
            }
        });
        LinearLayout report = (LinearLayout)findViewById(R.id.report_dashboard_button);
        report.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeDashboardActivity.this, GrowthReportActivity.class);

                intent.putExtras(extras);
                startActivity(intent);
            }
        });

    }

}
