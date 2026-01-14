package com.example.datausagetracker.data.remote.model;

import java.util.List;

public class UsageRequest {
    private String deviceId;
    private List<AppUsageDto> usages;

    public UsageRequest(String deviceId, List<AppUsageDto> usages) {
        this.deviceId = deviceId;
        this.usages = usages;
    }

}