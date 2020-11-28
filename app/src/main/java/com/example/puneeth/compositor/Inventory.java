package com.example.puneeth.compositor;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.puneeth.compositor.client.controllers.ItemController;
import com.example.puneeth.compositor.client.factory.ApiFactory;
import com.example.puneeth.compositor.models.SimpleResponse;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/*
    This class handles the inventory management buy allowing the user to either the weight of the compost or to completely delete a compost.
    Associated xml files:
        1> activity_inventory.xml (Provides a general UI for managing the compost)

    AsyncTask class updateWeight peforms the following functions:
        1> delete the compost from the market.
        2> update the weight of the compost once a certain amount of weight is sold.

    Associated php files:
        1> updateweight.php
        2> deleteInventory.php
 */
public class Inventory extends Activity {
    public Bundle b;
    private String weight;
    public String m;
    private String id;
    public TextView msg;
    private EditText differenceW;
    int newWeight;
    private ItemController client;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory);
        client = ApiFactory.composter.create(ItemController.class);
        b=getIntent().getExtras();
        String name = b.getString("name");
        weight=b.getString("weight");
        String cost = b.getString("cost");
        id=b.getString("id");
        msg=findViewById(R.id.messageInventory);
        differenceW=findViewById(R.id.differenceWeight);
        m="for item "+ name +" currently having a weight "+weight+" costing "+ cost;
        msg.setText(m);
    }

    //update() updates the weight of the compost when a certain weight is sold to the buyer and is deleted automatically when available weight becomes 0
    public void update(View v){
        Log.d("2","original weight: "+Integer.parseInt(weight)+" sold weight: "+Integer.parseInt(differenceW.getText().toString()));
        newWeight=Integer.parseInt(weight)-(Integer.parseInt(differenceW.getText().toString()));
        Call<SimpleResponse> weightResponse = client.reqUpdateItem(id, String.valueOf(newWeight));
        weightResponse.enqueue(new Callback<SimpleResponse>() {
            @Override
            public void onResponse(Call<SimpleResponse> call, Response<SimpleResponse> response) {
                if (response != null && response.isSuccessful() && response.body() != null) {
                    SimpleResponse body = response.body();
                    Toast.makeText(getApplicationContext(), "successfully updated data", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<SimpleResponse> call, Throwable t) {
                t.printStackTrace();
                Toast.makeText(getApplicationContext(), "exception while updating weight", Toast.LENGTH_LONG).show();
            }
        });
    }

    //deleteItem() deletes the selected compost
    public void deleteItem(View v){
        Log.d("2","in delete item function");
        Call<SimpleResponse> itemDeleteResponse = client.reqDeleteItemById(id);
        itemDeleteResponse.enqueue(new Callback<SimpleResponse>() {
            @Override
            public void onResponse(Call<SimpleResponse> call, Response<SimpleResponse> response) {
                if (response != null && response.isSuccessful() && response.body() != null) {
                    SimpleResponse body = response.body();
                    Toast.makeText(getApplicationContext(), "deleted successfully", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(Inventory.this, Unsold.class);
                    Bundle go = new Bundle();
                    go.putString("loginb", b.getString("loginb"));
                    intent.putExtras(go);
                    getApplicationContext().startActivity(intent);
                }
            }

            @Override
            public void onFailure(Call<SimpleResponse> call, Throwable t) {
                t.printStackTrace();
                Toast.makeText(getApplicationContext(), "exception while deleting item", Toast.LENGTH_LONG).show();
            }
        });
    }
}
