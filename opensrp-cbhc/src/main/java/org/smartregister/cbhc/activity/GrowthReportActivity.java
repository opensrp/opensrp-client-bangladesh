package org.smartregister.cbhc.activity;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.widget.TextView;

import org.opensrp.api.constants.Gender;
import org.smartregister.cbhc.R;
import org.smartregister.cbhc.fragment.GrowthFalteringTrendReportFragment;
import org.smartregister.cbhc.fragment.KeyAchievementFragment;
import org.smartregister.cbhc.fragment.ReportFragment;
import org.smartregister.cbhc.fragment.ReportGrowthFalterFragment;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by raihan on 1/03/2017.
 */

public class GrowthReportActivity extends AppCompatActivity {

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
        ((TextView)findViewById(R.id.title)).setText(R.string.report);


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
        tabOne.setText("Report");
        tabOne.setCompoundDrawablesWithIntrinsicBounds(0, R.mipmap.bargraph, 0, 0);
        tabOne.setGravity(Gravity.CENTER);
        tabOne.setTextColor(getResources().getColor(R.color.status_bar_text_almost_white));
        tabLayout.getTabAt(0).setCustomView(tabOne);
//        tabLayout.getTabAt(0).setIcon(R.mipmap.bargraph);
//        TextView tabtwo = new TextView(this);
//        tabtwo.setText("Geo"+ System.getProperty("line.separator") +"Map");
//        tabtwo.setCompoundDrawablesWithIntrinsicBounds(0, R.mipmap.ic_geo_loc, 0, 0);
//        tabtwo.setGravity(Gravity.CENTER);
//        tabtwo.setTextColor(getResources().getColor(R.color.status_bar_text_almost_white));
//        tabLayout.getTabAt(1).setCustomView(tabtwo);

        TextView tabthree = new TextView(this);
        tabthree.setText("Growth"+ System.getProperty("line.separator") +"Faltering Trend");
        tabthree.setCompoundDrawablesWithIntrinsicBounds(0, R.mipmap.ic_chart, 0, 0);
        tabthree.setGravity(Gravity.CENTER);
        tabthree.setTextColor(getResources().getColor(R.color.status_bar_text_almost_white));
        tabLayout.getTabAt(1).setCustomView(tabthree);
    }



/*    @Override
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
    }*/



    private void setupViewPager(ViewPager viewPager) {
        adapter = new ViewPagerAdapter(getSupportFragmentManager());

       // adapter.addFragment(new KeyAchievementFragment(), "Key Achievement");
        adapter.addFragment(new ReportFragment(), "Report");
       // adapter.addFragment(new ReportGeoMapFragment(), "Geo Map");
        adapter.addFragment(new ReportGrowthFalterFragment(), "Growth Faltering Trend");
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
