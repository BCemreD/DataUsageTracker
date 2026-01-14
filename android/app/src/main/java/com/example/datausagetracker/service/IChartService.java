package com.example.datausagetracker.service;

import com.example.datausagetracker.entity.AppUsageSummary;
import com.github.mikephil.charting.charts.HorizontalBarChart;
import java.util.List;

public interface IChartService {
    void setupBarChart(HorizontalBarChart barChart, List<AppUsageSummary> usageList);
    String getAppName(String packageName);
}