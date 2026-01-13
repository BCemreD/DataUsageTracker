package com.example.datausagetracker;

import android.content.Intent;
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

import com.example.datausagetracker.R;
import com.example.datausagetracker.entity.AppUsageSummary;
import com.example.datausagetracker.service.ChartHelper;
import com.example.datausagetracker.service.IChartService;
import com.example.datausagetracker.service.IPermissionService;
import com.example.datausagetracker.service.PermissionManager;
import com.example.datausagetracker.utils.NetworkHelper;
import com.example.datausagetracker.worker.DataWorker;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {
    private TextView tvWifi, tvMobile;
    private com.github.mikephil.charting.charts.HorizontalBarChart barChart;

    private IPermissionService permissionService;
    private IChartService chartService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Servisleri başlat
        permissionService = new PermissionManager(this);
        chartService = new ChartHelper(this);

        tvWifi = findViewById(R.id.tvWifi);
        tvMobile = findViewById(R.id.tvMobile);
        barChart = findViewById(R.id.barChart);

        setupWindowInsets();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!permissionService.isAccessGranted()) {
            startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS));
        } else {
            loadDataAndRefreshUI();
        }
    }

    private void loadDataAndRefreshUI() {
        // Data summary
        long wifiBytes = NetworkHelper.getTotalWifiUsage(this);
        long mobileBytes = NetworkHelper.getTotalMobileUsage(this);
        tvWifi.setText(String.format("WiFi: %.2f MB", wifiBytes / (1024.0 * 1024.0)));
        tvMobile.setText(String.format("Mobil: %.2f MB", mobileBytes / (1024.0 * 1024.0)));

        // Background operations
        new Thread(() -> {
            com.example.datausagetracker.data.local.db.AppDatabase db =
                    com.example.datausagetracker.data.local.db.AppDatabase.getDatabase(this);
            List<AppUsageSummary> topApps = db.dataUsageDao().getTopUsageApps();

            runOnUiThread(() -> chartService.setupBarChart(barChart, topApps));
        }).start();

        startBackgroundWork();
    }

    private void startBackgroundWork() {
        PeriodicWorkRequest trackingRequest =
                new PeriodicWorkRequest.Builder(DataWorker.class, 15, TimeUnit.MINUTES).build();
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                "DataTrackingWork", ExistingPeriodicWorkPolicy.KEEP, trackingRequest);
    }

    private void setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}