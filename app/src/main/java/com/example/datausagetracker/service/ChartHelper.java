package com.example.datausagetracker.service;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import com.example.datausagetracker.entity.AppUsageSummary;
import com.github.mikephil.charting.charts.HorizontalBarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import java.util.ArrayList;
import java.util.List;

public class ChartHelper implements IChartService {
    private final Context context;

    public ChartHelper(Context context) {
        this.context = context;
    }

    @Override
    public void setupBarChart(HorizontalBarChart barChart, List<AppUsageSummary> usageList) {
        int mainColor = Color.parseColor("#18181d");
        Typeface boldTypeface = Typeface.DEFAULT_BOLD;

        barChart.setNoDataText("No Sufficient Data.");
        barChart.setNoDataTextColor(mainColor);

        List<BarEntry> entries = new ArrayList<>();
        List<String> labels = new ArrayList<>();

        int index = 0;
        for (int i = usageList.size() - 1; i >= 0; i--) {
            AppUsageSummary summary = usageList.get(i);
            float totalMB = (summary.totalWifi + summary.totalMobile) / (1024f * 1024f);

            if (totalMB > 0.1f) {
                entries.add(new BarEntry(index, totalMB));
                String name = getAppName(summary.packageName);
                if (name.length() > 15) name = name.substring(0, 12) + "...";
                labels.add(name);
                index++;
            }
        }

        if (entries.isEmpty()) {
            barChart.invalidate();
            return;
        }

        BarDataSet dataSet = new BarDataSet(entries, "Data Usage (MB)");
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        dataSet.setValueTextSize(12f);
        dataSet.setValueTextColor(mainColor);
        dataSet.setValueTypeface(boldTypeface);

        barChart.setData(new BarData(dataSet));
        barChart.setExtraLeftOffset(50f);

        XAxis xAxis = barChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
        xAxis.setXOffset(15f);
        xAxis.setTextColor(mainColor);
        xAxis.setTypeface(boldTypeface);
        xAxis.setLabelCount(labels.size());

        barChart.getAxisLeft().setEnabled(false);
        barChart.getAxisRight().setEnabled(false);
        barChart.getLegend().setEnabled(false);
        barChart.getDescription().setEnabled(false);
        barChart.setFitBars(true);
        barChart.animateY(1000);
        barChart.invalidate();
    }

    @Override
    public String getAppName(String packageName) {
        try {
            android.content.pm.PackageManager pm = context.getPackageManager();
            android.content.pm.ApplicationInfo ai = pm.getApplicationInfo(packageName, 0);
            return pm.getApplicationLabel(ai).toString();
        } catch (Exception e) {
            return packageName;
        }
    }
}