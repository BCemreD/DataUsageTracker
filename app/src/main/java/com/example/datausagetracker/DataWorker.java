package com.example.datausagetracker;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

public class DataWorker extends Worker{

    public DataWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super (context, params);
    }

    @NonNull
    @Override
    public Result doWork(){
        return Result.success();
    }
}
