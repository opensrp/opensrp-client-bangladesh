package org.smartregister.growplus.activity;

import android.app.FragmentTransaction;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.opensrp.api.constants.Gender;
import org.smartregister.Context;
import org.smartregister.clientandeventmodel.Event;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.domain.Photo;
import org.smartregister.growplus.R;
import org.smartregister.growplus.application.VaccinatorApplication;
import org.smartregister.growplus.fragment.GrowthFalteringTrendReportFragment;
import org.smartregister.growplus.fragment.ReportGeoMapFragment;
import org.smartregister.growplus.fragment.StatusEditDialogFragment;
import org.smartregister.growplus.listener.StatusChangeListener;
import org.smartregister.growplus.sync.ECSyncUpdater;
import org.smartregister.growplus.sync.PathClientProcessor;
import org.smartregister.growplus.tabfragments.ChildRegistrationDataFragment;
import org.smartregister.growplus.tabfragments.ChildUnderFiveFragment;
import org.smartregister.growplus.toolbar.ChildDetailsToolbar;
import org.smartregister.growplus.toolbar.LocationSwitcherToolbar;
import org.smartregister.growthmonitoring.domain.Weight;
import org.smartregister.growthmonitoring.domain.WeightWrapper;
import org.smartregister.growthmonitoring.listener.WeightActionListener;
import org.smartregister.growthmonitoring.repository.WeightRepository;
import org.smartregister.immunization.domain.ServiceRecord;
import org.smartregister.immunization.domain.Vaccine;
import org.smartregister.immunization.domain.VaccineSchedule;
import org.smartregister.immunization.domain.VaccineWrapper;
import org.smartregister.immunization.listener.VaccinationActionListener;
import org.smartregister.immunization.repository.RecurringServiceRecordRepository;
import org.smartregister.immunization.repository.VaccineRepository;
import org.smartregister.immunization.util.VaccinatorUtils;
import org.smartregister.immunization.view.ImmunizationRowGroup;
import org.smartregister.repository.AllSharedPreferences;
import org.smartregister.repository.BaseRepository;
import org.smartregister.repository.DetailsRepository;
import org.smartregister.repository.EventClientRepository;
import org.smartregister.util.FormUtils;
import org.smartregister.util.OpenSRPImageLoader;
import org.smartregister.view.activity.DrishtiApplication;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import util.ImageUtils;
import util.JsonFormUtils;
import util.PathConstants;

import static org.smartregister.util.DateUtil.getDuration;
import static org.smartregister.util.Utils.dobToDateTime;
import static org.smartregister.util.Utils.getName;
import static org.smartregister.util.Utils.getValue;
import static org.smartregister.util.Utils.startAsyncTask;


/**
 * Created by raihan on 1/03/2017.
 */

public class GrowthReportActivity extends BaseActivity {

    private TabLayout tabLayout;
    private ViewPager viewPager;
    public static Gender gender;
    //////////////////////////////////////////////////
    public static final String EXTRA_CHILD_DETAILS = "child_details";
    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd-MM-yyyy");
     public static final String DIALOG_TAG = "ChildDetailActivity_DIALOG_TAG";


      private ViewPagerAdapter adapter;

    public ViewPagerAdapter getViewPagerAdapter() {
        return adapter;
    }

    // Data

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.report_detail_activity_simple_tabs);
        ((TextView)findViewById(R.id.title)).setText("Report");


        tabLayout = (TabLayout) findViewById(R.id.tabs);
//        getSupportActionBar().

        viewPager = (ViewPager) findViewById(R.id.viewpager);
//        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
//            @Override
//            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
//
//            }
//
//            @Override
//            public void onPageSelected(int position) {
//
//            }
//
//            @Override
//            public void onPageScrollStateChanged(int state) {
//
//            }
//        });

        setupViewPager(viewPager);



        tabLayout.setupWithViewPager(viewPager);
        createTabIcons();


    }
    private void createTabIcons() {
        TextView tabOne = new TextView(this);
        tabOne.setText("Key"+ System.getProperty("line.separator") +"Achievement");
        tabOne.setCompoundDrawablesWithIntrinsicBounds(0, R.mipmap.bargraph, 0, 0);
        tabOne.setGravity(Gravity.CENTER);
        tabOne.setTextColor(getResources().getColor(R.color.status_bar_text_almost_white));
        tabLayout.getTabAt(0).setCustomView(tabOne);
//        tabLayout.getTabAt(0).setIcon(R.mipmap.bargraph);
        TextView tabtwo = new TextView(this);
        tabtwo.setText("Geo"+ System.getProperty("line.separator") +"Map");
        tabtwo.setCompoundDrawablesWithIntrinsicBounds(0, R.mipmap.ic_geo_loc, 0, 0);
        tabtwo.setGravity(Gravity.CENTER);
        tabtwo.setTextColor(getResources().getColor(R.color.status_bar_text_almost_white));
        tabLayout.getTabAt(1).setCustomView(tabtwo);

        TextView tabthree = new TextView(this);
        tabthree.setText("Growth"+ System.getProperty("line.separator") +"Faltering Trend");
        tabthree.setCompoundDrawablesWithIntrinsicBounds(0, R.mipmap.ic_chart, 0, 0);
        tabthree.setGravity(Gravity.CENTER);
        tabthree.setTextColor(getResources().getColor(R.color.status_bar_text_almost_white));
        tabLayout.getTabAt(2).setCustomView(tabthree);
    }



    @Override
    protected int getContentView() {
        return R.layout.report_detail_activity_simple_tabs;
    }

    @Override
    protected int getDrawerLayoutId() {
        return R.id.drawer_layout;
    }

    @Override
    protected int getToolbarId() {
        return R.id.child_detail_toolbar;
    }

    @Override
    protected Class onBackActivity() {
        return ChildImmunizationActivity.class;
    }



    private void setupViewPager(ViewPager viewPager) {
        adapter = new ViewPagerAdapter(getSupportFragmentManager());

        adapter.addFragment(new Fragment(), "Key Achievement");
        adapter.addFragment(new ReportGeoMapFragment(), "Geo Map");
        adapter.addFragment(new GrowthFalteringTrendReportFragment(), "Growth Faltering Trend");
        viewPager.setAdapter(adapter);
    }






    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }


        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }


}
