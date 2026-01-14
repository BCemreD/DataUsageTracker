package com.example.datausagetracker.utils;

import com.example.datausagetracker.service.INetworkService;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    //Backend Ip (local IP for now)
    private static final String BASE_URL = "http://10.0.2.2:8000/"; // PC access IP for emulator
    private static Retrofit retrofit = null;

    public static INetworkService getService() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit.create(INetworkService.class);
    }
}