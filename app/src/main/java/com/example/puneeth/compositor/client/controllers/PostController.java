package com.example.puneeth.compositor.client.controllers;

import com.example.puneeth.compositor.models.Post;
import com.example.puneeth.compositor.models.PostRequest;
import com.example.puneeth.compositor.models.SimpleResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface PostController {
    @GET("/posts")
    Call<List<Post>> getPosts();

    @POST("/posts")
    Call<SimpleResponse> addPost(@Body PostRequest request);
}
