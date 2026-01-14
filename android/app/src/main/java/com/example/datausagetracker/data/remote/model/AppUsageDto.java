package com.example.datausagetracker.data.remote.model;

public  class AppUsageDto {
    private String packageName;
    private double wifiMB;
    private double mobileMB;

    public AppUsageDto(String packageName, double wifiMB, double mobileMB) {
        this.packageName = packageName;
        this.wifiMB = wifiMB;
        this.mobileMB = mobileMB;
    }
}