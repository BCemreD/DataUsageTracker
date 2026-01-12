package com.example.datausagetracker.worker;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.net.ConnectivityManager;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.datausagetracker.data.local.db.AppDatabase;
import com.example.datausagetracker.entity.DataUsageRecord;
import com.example.datausagetracker.utils.NetworkHelper;

import java.util.List;

public class DataWorker extends Worker {

    public DataWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        Context context = getApplicationContext();
        AppDatabase db = AppDatabase.getDatabase(context);

        long endTime = System.currentTimeMillis();
        long startTime = endTime - (15*60*1000);

        try{
            //List all apps
            List<ApplicationInfo> installedApps = NetworkHelper.getInstalledApps(context);

            for (ApplicationInfo app : installedApps) {
                int uid = app.uid;
                String packageName = app.packageName;

                // 1. WiFi calculation
                long wifiUsage = NetworkHelper.getUsageForUid(
                context,
                        ConnectivityManager.TYPE_WIFI,
                uid,
                startTime,
                endTime
                );

                //2. Mobile calculation
                long mobileUsage = NetworkHelper.getUsageForUid(
                        context,
                        ConnectivityManager.TYPE_MOBILE,
                        uid,
                        startTime,
                        endTime
                );

                //3. Only used apps will be kept in db
                if (wifiUsage > 0 || mobileUsage < 0) {
                    DataUsageRecord record = new DataUsageRecord(
                            packageName,
                            endTime,
                            wifiUsage,
                            mobileUsage
                    );
                    db.dataUsageDao().insert(record);
                    Log.d("DataWorker", "Saved usage for: " + packageName + " (WiFi: " + wifiUsage + ")");

                }

            }
            return Result.success();
        }catch (Exception e){
            Log.e("DataWorker", "Error in background task: " + e.getMessage());
            return Result.failure();
        }

    }
}