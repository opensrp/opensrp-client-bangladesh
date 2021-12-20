package org.smartregister.growplus.activity;

import static org.smartregister.util.Log.logError;

import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import org.smartregister.growplus.R;
import org.smartregister.growplus.application.VaccinatorApplication;
import org.smartregister.growplus.toolbar.LocationSwitcherToolbar;
import org.smartregister.repository.AllSharedPreferences;

import java.util.Locale;

import util.Utils;

public class HomeDashboardActivity extends BaseActivity {
    private Locale myLocale;
    private String currentLanguage;
    private Switch languageSwitch;
    private AllSharedPreferences allSharedPreferences ;
    private TextView language_tv;


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
        allSharedPreferences = new AllSharedPreferences(getApplicationContext().getSharedPreferences("language", android.content.Context.MODE_PRIVATE));
        /*try {
            String preferredLocale = util.Utils.getsSelectedLocale(this);
            Resources res = getOpenSRPContext().applicationContext().getResources();
            // Change locale settings in the app.
            DisplayMetrics dm = res.getDisplayMetrics();
            Configuration conf = res.getConfiguration();
            conf.locale = new Locale(preferredLocale);
            res.updateConfiguration(conf, dm);
        } catch (Exception e) {
            logError("Error onCreate: " + e);

        }*/

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


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_language_settings, menu);
        MenuItem item = menu.findItem(R.id.language_switch);
        item.setActionView(R.layout.language_switch_lay);

        currentLanguage = Utils.getsSelectedLocale(this);

        languageSwitch = item.getActionView().findViewById(R.id.languageSwitch);
        language_tv = item.getActionView().findViewById(R.id.language_tv);

        if(currentLanguage.equalsIgnoreCase("bn")){
            languageSwitch.setChecked(false);
            language_tv.setText("BN");
        }else {
            languageSwitch.setChecked(true);
            language_tv.setText("EN");
        }
        languageSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (languageSwitch.isChecked()) {
                    setLocale("en");
                    language_tv.setText("EN");
                } else {
                    setLocale("bn");
                    language_tv.setText("BN");
                }
            }
        });

        return true;
    }

    public void setLocale(String localeName) {

        allSharedPreferences.saveLanguagePreference(localeName);

        myLocale = new Locale(localeName);
        Resources res = getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        Configuration conf = res.getConfiguration();

        conf.locale = myLocale;
        res.updateConfiguration(conf, dm);
        Intent refresh = new Intent(this, HomeDashboardActivity.class);
        refresh.putExtra("currentLanguage", localeName);
        startActivity(refresh);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        String preferredLocale = util.Utils.getsSelectedLocale(this);
        Resources res = getResources();
        // Change locale settings in the app.
        DisplayMetrics dm = res.getDisplayMetrics();
        Configuration conf = res.getConfiguration();
        conf.locale = new Locale(preferredLocale.toLowerCase());
        res.updateConfiguration(conf, dm);
    }

    /**
     * for setting language to opensrp library
     */
    void setLocalTOLibrary(){
        try {
            String preferredLocale = Utils.getsSelectedLocale(this).toLowerCase();
            Resources res = getOpenSRPContext().applicationContext().getResources();
            // Change locale settings in the app.
            DisplayMetrics dm = res.getDisplayMetrics();
            android.content.res.Configuration conf = res.getConfiguration();
            conf.locale = new Locale(preferredLocale);
            res.updateConfiguration(conf, dm);

            Intent refresh = new Intent(this, HomeDashboardActivity.class);
            refresh.putExtra("currentLanguage", preferredLocale);
            startActivity(refresh);
            finish();
        } catch (Exception e) {
            logError("Error onCreate: " + e);
        }
    }
}
