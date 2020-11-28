package com.example.puneeth.compositor;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.annotation.Nullable;

import android.view.View;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.example.puneeth.compositor.client.controllers.OfferController;
import com.example.puneeth.compositor.client.factory.ApiFactory;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/*
    This class is used to make a cart that allows the buyers to keep track of the composts that have been brought.
    Related xml:
        1> activity_cart.xml (generates a list view that will act as a cart)
        2> cart_items.xml (provides a template for each compost product that would be placed in the cart)

    The AsyncTask DisplayCart peforms the following functions:
        1> Retrive composts from the database on which an offer has been placed and display them in the cart.
    Associated php files:
        1> displaycart.php
 */
public class Cart extends Activity {
    private TextView nameC,costC,weightC;
    private ListView listView;
    private ArrayList<HashMap<String,String>> itemsList;
    private String userid;
    private OfferController client;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);
        client = ApiFactory.composter.create(OfferController.class);
        itemsList=new ArrayList<>();
        listView=findViewById(R.id.cartList);
        nameC=findViewById(R.id.compostNameCart);
        costC=findViewById(R.id.compostCostCart);
        weightC=findViewById(R.id.compostWeightCart);
        Bundle b=getIntent().getExtras();
        userid=b.getString("loginb");
        Call<List<Map<String, String>>> cartResponse = client.getCart(userid);
        cartResponse.enqueue(new Callback<List<Map<String, String>>>() {
            @Override
            public void onResponse(Call<List<Map<String, String>>> call, Response<List<Map<String, String>>> response) {
                if (response != null && response.isSuccessful() && response.body() != null) {
                    List<Map<String, String>> body = response.body();
                    ListAdapter adapter = new SimpleAdapter(getApplicationContext(), body, R.layout.cart_items,
                            new String[]{"compostNameCart", "compostCostCart", "compostWeightCart"},
                            new int[]{R.id.compostNameCart, R.id.compostCostCart, R.id.compostWeightCart}
                    );
                    listView.setAdapter(adapter);
                    registerForContextMenu(listView);
                }
            }

            @Override
            public void onFailure(Call<List<Map<String, String>>> call, Throwable t) {
                t.printStackTrace();
                Toast.makeText(getApplicationContext(), "exception while loading cart", Toast.LENGTH_LONG).show();
            }
        });
    }

    public void goBackC(View v){
        Intent intent=new Intent(getApplicationContext(),Buyer.class);
        Bundle b=new Bundle();
        b.putString("loginb",userid);
        intent.putExtras(b);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    public void refreshC(View v){
        startActivity(getIntent());
    }
}
