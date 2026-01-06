package com.example.datausagetracker.data.local.db;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.datausagetracker.data.local.dao.DataUsageDao;
import com.example.datausagetracker.entity.DataUsageRecord;

@Database(entities = {DataUsageRecord.class}, version = 1) //all tables and version on the project
public abstract class AppDatabase extends RoomDatabase {

    public abstract DataUsageDao dataUsageDao(); //like Spring Autowired

    private static volatile AppDatabase INSTANCE;

    //Singleton. One db
    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "data_usage_db")
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
