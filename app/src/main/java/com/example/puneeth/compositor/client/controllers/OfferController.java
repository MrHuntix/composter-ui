package com.example.puneeth.compositor.client.controllers;

import com.example.puneeth.compositor.models.OfferRequest;
import com.example.puneeth.compositor.models.SimpleResponse;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface OfferController {
    @GET("/offers/{seller}")
    Call<List<Map<String, String>>> getOffers(@Path("seller") String seller);

    @POST("/offers")
    Call<SimpleResponse> placeOffer(@Body OfferRequest offerRequest);

    @GET("/cart/{id}")
    Call<List<Map<String, String>>> getCart(@Path("id") String id);
}
