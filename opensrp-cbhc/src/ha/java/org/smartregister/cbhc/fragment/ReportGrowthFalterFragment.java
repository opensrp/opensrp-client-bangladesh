package org.smartregister.cbhc.fragment;

import static org.smartregister.growthmonitoring.domain.ZScore.getMuacText;
import static org.smartregister.growthmonitoring.domain.ZScore.getZScoreText;

import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.Toast;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import org.smartregister.cbhc.R;
import org.smartregister.cbhc.adapters.AppSegmentAdapter;
import org.smartregister.cbhc.application.AncApplication;
import org.smartregister.cbhc.domain.ChildData;
import org.smartregister.cbhc.domain.ReportData;
import org.smartregister.commonregistry.CommonRepository;

import java.util.ArrayList;

import segmented_control.widget.custom.android.com.segmentedcontrol.SegmentedControl;
import segmented_control.widget.custom.android.com.segmentedcontrol.item_row_column.SegmentViewHolder;
import segmented_control.widget.custom.android.com.segmentedcontrol.listeners.OnSegmentClickListener;

public class ReportGrowthFalterFragment extends Fragment implements SeekBar.OnSeekBarChangeListener, OnChartValueSelectedListener {
    private CommonRepository commonRepository;
    private ArrayList<ChildData> childDataList;
    private int totalGmpChild=0;
    private BarChart chart;


    public ReportGrowthFalterFragment(){
        commonRepository = AncApplication.getInstance().getContext().commonrepository("ec_child");
    }
    private SegmentedControl segmentedControl;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.report_growth_fragment, container, false);
        childDataList = new ArrayList<>();

        initUi(view);
        getChildData();
        setupGraph(0,view);
        return view;
    }

    private void getChildData() {
        childDataList.clear();
        String query = "select * from ec_child where child_status is not NULL";
        Cursor cursor = commonRepository.rawCustomQueryForAdapter(query);

        if(cursor!=null && cursor.getCount()>0){
            cursor.moveToFirst();
            while (!cursor.isAfterLast()){
                int child_height = cursor.getColumnIndex("child_height");
                int child_weight = cursor.getColumnIndex("child_weight");
                int child_muac = cursor.getColumnIndex("child_muac");
                int child_status = cursor.getColumnIndex("child_status");
                int has_edema = cursor.getColumnIndex("has_edema");

                childDataList.add(new ChildData(
                        cursor.isNull(child_height)?"":cursor.getString(child_height),
                        cursor.isNull(child_weight)?"":cursor.getString(child_weight),
                        cursor.isNull(child_muac)?"":cursor.getString(child_muac),
                        cursor.isNull(child_status)?"":cursor.getString(child_status),
                        cursor.isNull(has_edema)?"":cursor.getString(has_edema)

                ));
                cursor.moveToNext();
            }

            totalGmpChild = cursor.getCount();

        }
    }

    private void setupGraph(int pos,View view) {
        chart.setDrawBarShadow(false);
        chart.setDrawValueAboveBar(true);
        chart.getDescription().setEnabled(false);
        chart.setMaxVisibleValueCount(60);
        chart.setPinchZoom(false);
        chart.setDrawGridBackground(false);



        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
        xAxis.setLabelCount(7);


        if(pos == 0){
            final ArrayList<String> axiss = new ArrayList<>();
            axiss.add("Normal Growth");
            axiss.add("Normal Growth");
            axiss.add("MAM");
            axiss.add("SAM");
            axiss.add("Edema");
            xAxis.setValueFormatter(new IndexAxisValueFormatter(axiss));
        }else if(pos == 1){
            final ArrayList<String> axiss = new ArrayList<>();
            axiss.add("Healthy");
            axiss.add("Healthy");
            axiss.add("Overweight");
            axiss.add("Moderately Underweight");
            axiss.add("Severly Underweight");
            xAxis.setValueFormatter(new IndexAxisValueFormatter(axiss));
        }else if(pos == 2){
            final ArrayList<String> axiss = new ArrayList<>();
            axiss.add("Normal");
            axiss.add("Normal");
            axiss.add("Moderately Stunted");
            axiss.add("Severly Stunted");
            xAxis.setValueFormatter(new IndexAxisValueFormatter(axiss));
        }



        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.setLabelCount(5, false);
        leftAxis.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART);
        leftAxis.setSpaceTop(5f);
        leftAxis.setAxisMinimum(0f);

        YAxis rightAxis = chart.getAxisRight();
        rightAxis.resetAxisMaximum();
        rightAxis.setDrawGridLines(false);
        rightAxis.setLabelCount(0, true);
        rightAxis.setSpaceTop(0);
        rightAxis.setAxisMinimum(0f);

        Legend l = chart.getLegend();
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.LEFT);
        l.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        l.setDrawInside(false);
        l.setForm(Legend.LegendForm.SQUARE);
        l.setFormSize(9f);
        l.setTextSize(11f);
        l.setXEntrySpace(4f);

        setData(4, 100,pos,chart);
    }

    @Override
    public void onValueSelected(Entry entry, Highlight highlight) {

    }

    @Override
    public void onNothingSelected() {

    }



    private void setData(int count, float range,int pos,BarChart chart) {
         int normalChildMuac=0;
         int samChildMuac=0;
         int mamChildMuac=0;
         int hasEdemaMuac=0;

         int healthyWeight = 0;
         int overWeight = 0;
         int moderatelyWeight = 0;
         int severlyWeight = 0;

        int normalStunting = 0;
        int moderatelyStunting = 0;
        int severlyStunting = 0;


        ArrayList<BarEntry> values = new ArrayList<>();

        BarDataSet barDataSet;
        int[] colors = null;

        int totalGmpChild = childDataList.size();
        for(ChildData childData : childDataList){
           if(pos == 0){
               if(!childData.getChild_muac().isEmpty()){
                   if(getMuacText(Double.valueOf(childData.getChild_muac())).equalsIgnoreCase("normal")){
                       normalChildMuac++;
                   }else if(getMuacText(Double.valueOf(childData.getChild_muac())).equalsIgnoreCase("sam")){
                       samChildMuac++;
                   }else if(getMuacText(Double.valueOf(childData.getChild_muac())).equalsIgnoreCase("mam")){
                       mamChildMuac++;
                   }
               }

               if(childData.getHas_edema().equals("true")){
                   hasEdemaMuac++;
               }
           }
           else if(pos == 1){
               if(!childData.getChild_weight().isEmpty()){
                   if(getZScoreText(Double.valueOf(childData.getChild_weight())).equalsIgnoreCase("normal")){
                       healthyWeight++;
                   }else if(getZScoreText(Double.valueOf(childData.getChild_weight())).equalsIgnoreCase("OVER WEIGHT")){
                       overWeight++;
                   }else if(getMuacText(Double.valueOf(childData.getChild_weight())).equalsIgnoreCase("DARK YELLOW")){
                       moderatelyWeight++;
                   }else if(getZScoreText(Double.valueOf(childData.getChild_weight())).equalsIgnoreCase("YELLOW")){
                       severlyWeight++;
                   }
               }
           }
           else if(pos == 2){
               if(!childData.getChild_height().isEmpty()){
                   Log.d("ttttHeiZSc",getZScoreText(Double.valueOf(childData.getChild_height())));
                   if(getZScoreText(Double.valueOf(childData.getChild_height())).equalsIgnoreCase("normal")){
                       normalStunting++;
                   }else if(getZScoreText(Double.valueOf(childData.getChild_height())).equalsIgnoreCase("DARK YELLOW")){
                       moderatelyStunting++;
                   }else if(getZScoreText(Double.valueOf(childData.getChild_height())).equalsIgnoreCase("YELLOW")){
                       severlyStunting++;
                   }
               }
           }
        }
        if(totalGmpChild==0) totalGmpChild = 1;


        if(pos==0){
            values.add(new BarEntry(1, (normalChildMuac*100)/totalGmpChild));
            values.add(new BarEntry(2, (mamChildMuac*100)/totalGmpChild));
            values.add(new BarEntry(3, (samChildMuac*100)/totalGmpChild));
            values.add(new BarEntry(4, (hasEdemaMuac*100)/totalGmpChild));

            colors = new int[]{
                    ContextCompat.getColor(getActivity(), android.R.color.holo_green_dark),
                    ContextCompat.getColor(getActivity(), R.color.yellow),
                    ContextCompat.getColor(getActivity(), android.R.color.holo_red_dark),
                    ContextCompat.getColor(getActivity(), R.color.quick_check_red)};

        }
        else if(pos==1){
            values.add(new BarEntry(1, (healthyWeight*100)/totalGmpChild));
            values.add(new BarEntry(2, (overWeight*100)/totalGmpChild));
            values.add(new BarEntry(3, (moderatelyWeight*100)/totalGmpChild));
            values.add(new BarEntry(4, (severlyWeight*100)/totalGmpChild));

            colors = new int[]{
                    ContextCompat.getColor(getActivity(), android.R.color.holo_green_dark),
                    ContextCompat.getColor(getActivity(), R.color.red),
                    ContextCompat.getColor(getActivity(), R.color.dark_yellow),
                    ContextCompat.getColor(getActivity(), R.color.quick_check_red)};
        }
        else if(pos==2){
            values.add(new BarEntry(1, (normalStunting*100)/totalGmpChild));
            values.add(new BarEntry(2, (moderatelyStunting*100)/totalGmpChild));
            values.add(new BarEntry(3, (severlyStunting*100)/totalGmpChild));

            colors = new int[]{
                    ContextCompat.getColor(getActivity(), android.R.color.holo_green_dark),
                    ContextCompat.getColor(getActivity(), R.color.quick_check_red),
                    ContextCompat.getColor(getActivity(), R.color.vaccine_red_bg_end)};
        }

        if (chart.getData() != null &&
                chart.getData().getDataSetCount() > 0) {
            barDataSet = (BarDataSet) chart.getData().getDataSetByIndex(0);
            barDataSet.setColors(colors);
            barDataSet.setValues(values);
            chart.getData().notifyDataChanged();
            chart.notifyDataSetChanged();
            chart.invalidate();

        } else {
            barDataSet = new BarDataSet(values,"");

            barDataSet.setDrawIcons(false);
            barDataSet.setColors(colors);
            ArrayList<IBarDataSet> dataSets = new ArrayList<>();
            dataSets.add(barDataSet);

            BarData data = new BarData(dataSets);
            data.setValueTextSize(10f);
            data.setBarWidth(0.9f);

            chart.setData(data);
            chart.getData().notifyDataChanged();
            chart.notifyDataSetChanged();
            chart.invalidate();
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
       // tvX.setText(String.valueOf(seekBarX.getProgress()));
        //tvY.setText(String.valueOf(seekBarY.getProgress()));

        //setData(10, 5,0);
       // chart.invalidate();
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }





    private void initUi(View view) {
        chart = view.findViewById(R.id.idBarChart);

        segmentedControl = (SegmentedControl) view.findViewById(R.id.segmented_control);
        segmentedControl.setAdapter(new AppSegmentAdapter());
        segmentedControl.setSelectedSegment(0);

        segmentedControl.addOnSegmentClickListener(new OnSegmentClickListener() {
            @Override
            public void onSegmentClick(SegmentViewHolder segmentViewHolder) {
                setupGraph(segmentViewHolder.getAbsolutePosition(),view);
            }
        });
    }
}
