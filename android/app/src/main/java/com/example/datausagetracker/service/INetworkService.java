package com.example.datausagetracker.service;

import com.example.datausagetracker.data.remote.model.UsageRequest;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface INetworkService {
    // From Python backend to "/api/usage" endpoint
    @POST("api/usage")
    Call<Void> sendData(@Body UsageRequest request);
}