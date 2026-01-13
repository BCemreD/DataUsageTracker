package com.example.datausagetracker;


import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.example.datausagetracker.entity.AppUsageSummary;
import com.example.datausagetracker.utils.NetworkHelper;
import com.example.datausagetracker.worker.DataWorker;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    // 1. New IDs
    TextView tvWifi;
    TextView tvMobile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        tvWifi = findViewById(R.id.tvWifi);
        tvMobile = findViewById(R.id.tvMobile);


        // 2. Permission Control
        if (!isAccessGranted()) {
            android.content.Intent intent = new android.content.Intent(android.provider.Settings.ACTION_USAGE_ACCESS_SETTINGS);
            startActivity(intent);
            tvWifi.setText("Permission is required...");
        } else {
            // 3. Calculate data usage
            long wifiBytes = NetworkHelper.getTotalWifiUsage(this);
            long mobileBytes = NetworkHelper.getTotalMobileUsage(this);

            double wifiMB = wifiBytes / (1024.0 * 1024.0);
            double mobileMB = mobileBytes / (1024.0 * 1024.0);

            tvWifi.setText(String.format("WiFi: %.2f MB", wifiMB));
            tvMobile.setText(String.format("Mobile: %.2f MB", mobileMB));

            // Background operations
            startBackgroundWork();
        }

        // Window Insets (Padding)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void startBackgroundWork() {
        PeriodicWorkRequest trackingRequest =
                new PeriodicWorkRequest.Builder(DataWorker.class, 15, TimeUnit.MINUTES)
                        .build();

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                "DataTrackingWork",
                ExistingPeriodicWorkPolicy.KEEP,
                trackingRequest
        );
    }

    //check permissions
    private boolean isAccessGranted() {
        try {
            //access packet manager
            android.content.pm.PackageManager packageManager = getPackageManager();
            //id infos of the app (UID).
            android.content.pm.ApplicationInfo applicationInfo = packageManager.getApplicationInfo(getPackageName(), 0);
            //system permissions (different from cam, contacts... permissions)
            android.app.AppOpsManager appOpsManager = (android.app.AppOpsManager) getSystemService(android.content.Context.APP_OPS_SERVICE);
            //access permission for usage stats
            int mode = appOpsManager.checkOpNoThrow(android.app.AppOpsManager.OPSTR_GET_USAGE_STATS,
                    applicationInfo.uid, applicationInfo.packageName);
            return (mode == android.app.AppOpsManager.MODE_ALLOWED);
        } catch (android.content.pm.PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        // 1. Permission control for usage access
        if (!isAccessGranted()) {
            tvWifi.setText("Permission is required...");
            // settings
            Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
            startActivity(intent);
        } else {
            loadDataAndRefreshUI();
        }
    }

    private void setupBarChart(List<AppUsageSummary> usageList) {
        com.github.mikephil.charting.charts.HorizontalBarChart barChart = findViewById(R.id.barChart);
        int mainColor = Color.parseColor("#18181d");
        android.graphics.Typeface boldTypeface = android.graphics.Typeface.DEFAULT_BOLD;

        //if no data, no chart
        barChart.setNoDataText("No chart data available");
        barChart.setNoDataTextColor(mainColor);

        List<BarEntry> entries = new ArrayList<>();
        List<String> labels = new ArrayList<>();

        // 1. Apps that consumes the highest data are on top
        int index = 0;
        for (int i = usageList.size() - 1; i >= 0; i--) {
            AppUsageSummary summary = usageList.get(i);
            float totalMB = (summary.totalWifi + summary.totalMobile) / (1024f * 1024f);

            if (totalMB > 0.1f) {
                entries.add(new BarEntry(index, totalMB));

                String name = getAppName(summary.packageName);

                // shorten names
                if (name.length() > 15) {
                    name = name.substring(0, 11) + "...";
                }

                labels.add(name);

                index++;
            }
        }

        if (entries.isEmpty()) {
            barChart.setNoDataText("No Sufficient Data.");
            barChart.invalidate();
            return;
        }

        // 2. Color and stile
        barChart.setExtraLeftOffset(50f);

        BarDataSet dataSet = new BarDataSet(entries, "Data Usage (MB)");
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS); // Color palette
        dataSet.setValueTextSize(12f);
        dataSet.setValueTextColor(mainColor);
        dataSet.setValueTypeface(android.graphics.Typeface.DEFAULT_BOLD);

        BarData data = new BarData(dataSet);
        barChart.setData(data);

        //X axis
        com.github.mikephil.charting.components.XAxis xAxis = barChart.getXAxis();
        xAxis.setValueFormatter(new com.github.mikephil.charting.formatter.IndexAxisValueFormatter(labels));
        xAxis.setPosition(com.github.mikephil.charting.components.XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
        xAxis.setXOffset(15f);
        xAxis.setTextColor(mainColor);
        xAxis.setTypeface(boldTypeface);
        xAxis.setLabelCount(labels.size());


        //legend settings
        barChart.getLegend().setEnabled(false);

        barChart.getAxisLeft().setEnabled(false); // top value line closed
        barChart.getAxisRight().setEnabled(false);
        barChart.getLegend().setEnabled(false);
        barChart.getDescription().setEnabled(false);
        barChart.setFitBars(true);
        barChart.animateY(1000);
        barChart.invalidate();


        barChart.getDescription().setEnabled(false);
        barChart.animateY(1400); // animation
        barChart.invalidate(); // renew
    }

    private void loadDataAndRefreshUI() {
        // Sum data
        long wifiBytes = NetworkHelper.getTotalWifiUsage(this);
        long mobileBytes = NetworkHelper.getTotalMobileUsage(this);

        tvWifi.setText(String.format("WiFi: %.2f MB", wifiBytes / (1024.0 * 1024.0)));
        tvMobile.setText(String.format("Mobil: %.2f MB", mobileBytes / (1024.0 * 1024.0)));

        // 3.Graph from db
        new Thread(() -> {
            com.example.datausagetracker.data.local.db.AppDatabase db =
                    com.example.datausagetracker.data.local.db.AppDatabase.getDatabase(this);
            List<AppUsageSummary> topApps = db.dataUsageDao().getTopUsageApps();

            runOnUiThread(() -> {
                setupBarChart(topApps);
            });
        }).start();

        // background starter
        startBackgroundWork();
    }

    private String getAppName(String packageName) {
        try {
            android.content.pm.PackageManager pm = getPackageManager();
            android.content.pm.ApplicationInfo ai = pm.getApplicationInfo(packageName, 0);
            return pm.getApplicationLabel(ai).toString();
        } catch (Exception e) {
            return packageName;
        }
}
}