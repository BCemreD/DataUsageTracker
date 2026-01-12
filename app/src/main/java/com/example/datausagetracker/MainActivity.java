package com.example.datausagetracker;

import android.app.AppOpsManager;
import android.content.Context;
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
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
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
            tvWifi.setText("Permission needed...");
        } else {
            // 3. Calculate data usage
            long wifiBytes = NetworkHelper.getTotalWifiUsage(this);
            long mobileBytes = NetworkHelper.getTotalMobileUsage(this);

            double wifiMB = wifiBytes / (1024.0 * 1024.0);
            double mobileMB = mobileBytes / (1024.0 * 1024.0);

            tvWifi.setText(String.format("WiFi: %.2f MB", wifiMB));
            tvMobile.setText(String.format("Mobil: %.2f MB", mobileMB));

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
            tvWifi.setText("İzin bekleniyor...");
            // settings
            Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
            startActivity(intent);
        } else {
            loadDataAndRefreshUI();
        }
    }

    private void setupPieChart(List<AppUsageSummary> usageList) {
        PieChart pieChart = findViewById(R.id.pieChart);
        List<PieEntry> entries = new ArrayList<>();

        // 1. Compromise data for the graph
        for (AppUsageSummary summary : usageList) {

            long totalBytes = summary.totalWifi + summary.totalMobile;
            float totalMB = totalBytes / (1024f * 1024f);
            if (totalMB > 0.1f) { // rounding
                String friendlyName = getAppName(summary.packageName);
                entries.add(new PieEntry(totalMB, friendlyName));
            }
        }

        if (entries.isEmpty()) {
            pieChart.setNoDataText("No Sufficient Data.");
            pieChart.invalidate();
            return;
        }

        // 2. Color and stile
        PieDataSet dataSet = new PieDataSet(entries, "App Usage");
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS); // Color palette
        dataSet.setValueTextSize(12f);
        dataSet.setValueTextColor(Color.BLACK);

        PieData data = new PieData(dataSet);
        pieChart.setData(data);


        pieChart.getDescription().setEnabled(false);
        pieChart.setCenterText("Data range ");
        pieChart.setHoleRadius(40f); // middle gap
        pieChart.animateY(1400); // animation
        pieChart.invalidate(); // renew
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
                setupPieChart(topApps);
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
            return packageName; // Bulamazsa eski halini bırakır
        }
}
}