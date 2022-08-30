package com.amupys.testright2.fragments;

import android.graphics.Color;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.amupys.testright2.ChartModel;
import com.amupys.testright2.R;
import com.github.mikephil.charting.charts.ScatterChart;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.ScatterData;
import com.github.mikephil.charting.data.ScatterDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;

public class ConcSecondFragment extends Fragment {

    ArrayList scatterEntries;
    private ArrayList<ChartModel> data;

    public ConcSecondFragment() {
        // Required empty public constructor
    }

    public void setData(ArrayList<ChartModel> data) {
        this.data = data;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_conc_second, container, false);

        ScatterChart scatterChart = view.findViewById(R.id.scatterChart);
        getEntries();
        ScatterDataSet scatterDataSet = new ScatterDataSet(scatterEntries, "");
        ScatterData scatterData = new ScatterData(scatterDataSet);
        scatterChart.setData(scatterData);
        scatterDataSet.setColors(ColorTemplate.JOYFUL_COLORS);
        scatterDataSet.setValueTextColor(Color.BLACK);
        scatterDataSet.setValueTextSize(18f);

        return view;
    }

    private void getEntries() {
        scatterEntries = new ArrayList<>();
        scatterEntries.add(new BarEntry(2f, 0));
        scatterEntries.add(new BarEntry(4f, 1));
        scatterEntries.add(new BarEntry(6f, 1));
        scatterEntries.add(new BarEntry(8f, 3));
        scatterEntries.add(new BarEntry(7f, 4));
        scatterEntries.add(new BarEntry(3f, 3));
    }
}