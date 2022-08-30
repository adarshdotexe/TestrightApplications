package com.amupys.testright2.fragments;

import android.graphics.Color;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.amupys.testright2.R;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;

import java.util.ArrayList;
import java.util.List;

public class SpectraFragment extends Fragment {

    private ArrayList<Float> values;
    LineChart lineChart;
    TextView textView;

    public SpectraFragment() {
        // Required empty public constructor
    }

    public void receiveData(ArrayList<Float> values){
//        Log.e("list size spectra", String.valueOf(values.size()));
        this.values = values;
        if(values.size() > 290){
            try{
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        drawLineChart();
                    }
                });
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_spectra, container, false);
        lineChart = v.findViewById(R.id.reportingChart);
        textView = v.findViewById(R.id.txt_spectra);
        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        if(values != null) drawLineChart();
    }

    private void drawLineChart() {
        List<Entry> lineEntries = getDataSet();
        LineDataSet lineDataSet = new LineDataSet(lineEntries, null);
        lineDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
        lineDataSet.setLineWidth(2);
        lineDataSet.setDrawValues(false);
        lineDataSet.setColor(getResources().getColor(R.color.colorAccent));

        lineDataSet.setDrawCircles(false);
        lineDataSet.setDrawHighlightIndicators(true);
        lineDataSet.setHighlightEnabled(true);
        lineDataSet.setHighLightColor(getResources().getColor(R.color.colorAccent));
        lineDataSet.setValueTextSize(12);
        lineDataSet.setValueTextColor(Color.DKGRAY);
        lineDataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        lineDataSet.setCubicIntensity(0.2f);

        Description description = new Description();
        description.setText("Wavelength (nm)");

        LineData lineData = new LineData(lineDataSet);
        lineChart.getLegend().setEnabled(true);
        lineChart.getDescription().setTextSize(12);
        lineChart.setDescription(description);
//        lineChart.getDescription().setEnabled(false);
        lineChart.animateY(500);

        lineChart.getAxisRight().setEnabled(false);
        lineChart.setDrawGridBackground(false);

//        // Setup X Axis
        XAxis xAxis = lineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
//        xAxis.setGranularityEnabled(true);
//        xAxis.setGranularity(1.0f);
//        xAxis.setXOffset(1f);
//        xAxis.setLabelCount(values.size());
//        xAxis.setAxisMinimum(0);


        lineChart.setData(lineData);

        lineChart.invalidate();

        lineChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
//                lineChart.getXAxis().getValueFormatter().getFormattedValue(e.getX(), lineChart.getXAxis());
                textView.setText(String.format("x: %s, y: %s", e.getX(), e.getY()));
            }

            @Override
            public void onNothingSelected() {

            }
        });
    }

    private List<Entry> getDataSet() {
        List<Entry> lineEntries = new ArrayList<>();
        int x = 401;
        for (int i=0;i<values.size();i++){
            try{
                lineEntries.add(new Entry(x++, values.get(i)));
            }catch (Exception e){
                try{
                    lineEntries.add(new Entry(x++, values.get(i - 1)));
                }catch (Exception e1){
                    lineEntries.add(new Entry(x++, values.get(i - 2)));
                }
            }
//            Log.e("chart y", String.valueOf(values.get(i)));
            if(x > 700)
                break;
        }

        return lineEntries;
    }
}