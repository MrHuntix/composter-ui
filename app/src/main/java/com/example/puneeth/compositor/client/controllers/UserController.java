package com.example.puneeth.compositor.client.controllers;

import com.example.puneeth.compositor.models.LoginRequest;
import com.example.puneeth.compositor.models.RegisterRequest;
import com.example.puneeth.compositor.models.SimpleResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface UserController {
    @POST("/login")
    Call<SimpleResponse> login(@Body LoginRequest request);

    @POST("/buyer")
    Call<SimpleResponse> buyer(@Body RegisterRequest request);

    @POST("/seller")
    Call<SimpleResponse> seller(@Body RegisterRequest request);

    @GET("/seller/{id}")
    Call<SimpleResponse> getById(@Path(value = "id") String id);
}
