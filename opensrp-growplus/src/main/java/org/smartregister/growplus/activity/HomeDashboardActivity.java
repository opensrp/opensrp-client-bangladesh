package org.smartregister.growplus.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import org.smartregister.growplus.R;
import org.smartregister.growplus.application.VaccinatorApplication;
import org.smartregister.growplus.toolbar.LocationSwitcherToolbar;

public class HomeDashboardActivity extends BaseActivity {

    private SharedPreferences languagePrefs;

    @Override
    protected int getContentView() {
        return  R.layout.home_dashboard;
    }
    @Override
    protected int getToolbarId() {
        return LocationSwitcherToolbar.TOOLBAR_ID;
    }

    @Override
    protected Class onBackActivity() {
        return ChildSmartRegisterActivity.class;
    }

    @Override
    protected int getDrawerLayoutId() {
        return  R.id.drawer_layout;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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

        TextView initialsTV = (TextView) findViewById(R.id.name_inits);
        String preferredName = VaccinatorApplication.getInstance().context().allSharedPreferences().getANMPreferredName(
                VaccinatorApplication.getInstance().context().allSharedPreferences().fetchRegisteredANM());
        if (!TextUtils.isEmpty(preferredName)) {
            String[] initialsArray = preferredName.split(" ");
            String initials = "";
            if (initialsArray.length > 0) {
                initials = initialsArray[0].substring(0, 1);
                if (initialsArray.length > 1) {
                    initials = initials + initialsArray[1].substring(0, 1);
                }
            }

            initialsTV.setText(initials.toUpperCase());
        }

        TextView nameTV = (TextView) findViewById(R.id.provider_name);
        nameTV.setText(capitalize(preferredName));

        ((ToggleButton)findViewById(R.id.language_switcher)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                languagePrefs.edit().putString("locale",  b ? "bn" : "en" ).apply();
            }
        });
     languagePrefs = getSharedPreferences("language",Context.MODE_PRIVATE);
    }

    public static String capitalize(@NonNull String input) {
if(input==null||"".equals(input))return "";
        String[] words = input.toLowerCase().split(" ");
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < words.length; i++) {
            String word = words[i];

            if (i > 0 && word.length() > 0) {
                builder.append(" ");
            }

            String cap = word.substring(0, 1).toUpperCase() + word.substring(1);
            builder.append(cap);
        }
        return builder.toString();
    }

}
