package com.example.datausagetracker.utils;

import android.app.usage.NetworkStats;
import android.app.usage.NetworkStatsManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;

import java.util.List;

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

    public static List<ApplicationInfo> getInstalledApps(Context context) {
        PackageManager pm = context.getPackageManager();

        return pm.getInstalledApplications(PackageManager.GET_META_DATA);
    }

    public static long getUsageForUid(Context context, int networkType, int uid, long startTime, long endTime) {
        NetworkStatsManager nsm = (NetworkStatsManager) context.getSystemService(Context.NETWORK_STATS_SERVICE);
        long totalBytes = 0;

        try {
            NetworkStats stats = nsm.queryDetailsForUid(networkType, null, startTime, endTime, uid);
            NetworkStats.Bucket bucket = new NetworkStats.Bucket();

            while (stats.hasNextBucket()) {
                stats.getNextBucket(bucket);
                totalBytes += bucket.getRxBytes() + bucket.getTxBytes();
            }
            stats.close();

            if (totalBytes > 0) {
                android.util.Log.d("Debug", "UID: " + uid + " Data: " + totalBytes);
            }

            return totalBytes;
        } catch (Exception e) {
            android.util.Log.e("NetworkHelper", "UID Query Error: " + e.getMessage());
            return 0;
        }
    }
}
