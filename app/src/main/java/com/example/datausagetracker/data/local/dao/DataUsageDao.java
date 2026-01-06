package com.example.datausagetracker.data.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.datausagetracker.entity.DataUsageRecord;

import java.util.List;

@Dao
public interface DataUsageDao {
        @Insert
        void insert(DataUsageRecord record);

        @Query("SELECT * FROM data_usage_records ORDER BY timestamp DESC")
        List<DataUsageRecord> getAllRecords();
}
