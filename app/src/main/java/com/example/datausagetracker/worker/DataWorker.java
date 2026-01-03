package com.example.datausagetracker.worker;

import android.content.Context;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import com.example.datausagetracker.utils.NetworkHelper;

public class DataWorker extends Worker {

    public DataWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        Context context = getApplicationContext();
        boolean isAnyMeasurementSuccessful = false;

        // 1. WiFi calculation
        long wifiUsage = NetworkHelper.getTotalWifiUsage(context);
        if (wifiUsage != -1) {
            Log.d("Tracker", "WiFi data: " + wifiUsage);
            isAnyMeasurementSuccessful = true;
            // TODO: wifiRepository.insert(wifiUsage);
        }

        // 2. Mobile calculation
        long mobileUsage = NetworkHelper.getTotalMobileUsage(context);
        if (mobileUsage != -1) {
            Log.d("Tracker", "Mobile data: " + mobileUsage);
            isAnyMeasurementSuccessful = true;
            // TODO: mobileRepository.insert(mobileUsage);
        }

        //
        if (isAnyMeasurementSuccessful) {
            return Result.success();
        }

        // Try again if there is no data
        return Result.retry();
    }
}