package com.example.datausagetracker.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "data_usage_records")
public class DataUsageRecord {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public long timestamp;
    public long wifiBytes;
    public long mobileBytes;

    public DataUsageRecord(long timestamp, long wifiBytes, long mobileBytes) {
        this.timestamp = timestamp;
        this.wifiBytes = wifiBytes;
        this.mobileBytes = mobileBytes;
    }
}
