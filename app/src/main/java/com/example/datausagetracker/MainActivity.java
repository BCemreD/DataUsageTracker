package com.example.datausagetracker;

import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.example.datausagetracker.utils.NetworkHelper;
import com.example.datausagetracker.worker.DataWorker;

import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        //if no permission
        if (!isAccessGranted()) {
            android.content.Intent intent = new android.content.Intent(android.provider.Settings.ACTION_USAGE_ACCESS_SETTINGS);
            startActivity(intent);
        }
        TextView myText = findViewById(R.id.textView); // like XML's ID
        long wifiBytes = NetworkHelper.getTotalWifiUsage(this);
        long mobileBytes = NetworkHelper.getTotalMobileUsage(this);

        double wifiMB = wifiBytes / (1024.0 * 1024.0);
        double mobileMB = mobileBytes / (1024.0 * 1024.0);

        myText.setText(String.format("WiFi: %.2f MB\nMobil: %.2f MB", wifiMB, mobileMB));

        startBackgroundWork();

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
    //print total usage
    private long getTotalDataUsage() {
        // 1. NetworkStatsManager: Android web stat service.
        android.app.usage.NetworkStatsManager networkStatsManager =
                (android.app.usage.NetworkStatsManager) getSystemService(android.content.Context.NETWORK_STATS_SERVICE);

        try {
            // Total data
            // ConnectivityManager.TYPE_MOBILE: cell data
            // System.currentTimeMillis() - 1000 * 60 * 60: last 1 hour
            android.app.usage.NetworkStats.Bucket bucket = networkStatsManager.querySummaryForDevice(
                    android.net.ConnectivityManager.TYPE_WIFI,
                    null,
                    System.currentTimeMillis() - (1000 * 60 * 60 * 24), // last 24 hours
                    System.currentTimeMillis());

            // 3. Calculate: (Rx) (Tx)
            return bucket.getRxBytes() + bucket.getTxBytes();
        } catch (Exception e) {
            return -1;
        }
    }
}