package com.example.datausagetracker.utils;

import android.app.usage.NetworkStats;
import android.app.usage.NetworkStatsManager;
import android.content.Context;
import android.net.ConnectivityManager;

public class NetworkHelper {
    public static long getDataUsageByType(Context context, int networkType){

        NetworkStatsManager networkStatsManager = (NetworkStatsManager)
                context.getSystemService(Context.NETWORK_STATS_SERVICE);

        try {
            android.app.usage.NetworkStats.Bucket bucket = networkStatsManager.querySummaryForDevice(
                    networkType,
                    null,
                    System.currentTimeMillis() - (1000 * 60 * 60 * 24),
                    System.currentTimeMillis());
            return (bucket != null) ? (bucket.getRxBytes() + bucket.getTxBytes()) : 0;
        }catch (Exception e) {
            //for logcat
            android.util.Log.e("NetworkHelper", "Error: " + e.getMessage());

            return -1;
        }
    }

    public static long getTotalWifiUsage(Context context) {
        return getDataUsageByType(context, ConnectivityManager.TYPE_WIFI);
    }

    public static long getTotalMobileUsage(Context context) {
        return getDataUsageByType(context, ConnectivityManager.TYPE_MOBILE);
    }
}
