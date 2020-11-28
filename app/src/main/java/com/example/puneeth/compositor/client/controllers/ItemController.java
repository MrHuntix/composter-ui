package com.example.puneeth.compositor.client.controllers;

import com.example.puneeth.compositor.models.ItemResponse;
import com.example.puneeth.compositor.models.Items;
import com.example.puneeth.compositor.models.NewItemRequest;
import com.example.puneeth.compositor.models.SimpleResponse;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface ItemController {

    @GET("/item/seller/{sellerContact}")
    Call<List<HashMap<String, String>>> reqItemForUser(@Path("sellerContact") String sellerContact);

    @GET("/item/all")
    Call<List<ItemResponse>> reqAllItems();

    @GET("/item/disp")
    Call<List<Map<String, String>>> dispItems();

    @POST("/item/add")
    Call<SimpleResponse> reqAddNewItem(@Body NewItemRequest item);

    @PUT("/item/{id}/weight/{weight}")
    Call<SimpleResponse> reqUpdateItem(@Path("id") String id, @Path("weight") String weight);

    @DELETE("/item/{id}")
    Call<SimpleResponse> reqDeleteItemById(@Path("id") String id);

    @DELETE("/item/zero")
    Call<SimpleResponse> reqDeleteZeroItem();
}
