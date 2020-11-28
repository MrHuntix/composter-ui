package com.example.puneeth.compositor.client.factory;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiFactory {
    private static final String BASE_URL = "https://composter-b.herokuapp.com/";
//    private static final String LOCAL = "http://127.0.0.1:13000/";

    private static final OkHttpClient .Builder httpClient = new OkHttpClient.Builder()
            .connectTimeout(30000, TimeUnit.SECONDS)
            .readTimeout(30000,TimeUnit.SECONDS)
            .writeTimeout(30000, TimeUnit.SECONDS);


    public static Retrofit composter = new Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(httpClient.build())
            .addConverterFactory(GsonConverterFactory.create())
            .build();
}
