package com.example.puneeth.compositor;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.example.puneeth.compositor.client.controllers.OfferController;
import com.example.puneeth.compositor.client.controllers.UserController;
import com.example.puneeth.compositor.client.factory.ApiFactory;
import com.example.puneeth.compositor.models.SimpleResponse;

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
    This class is used to display composts on which offers are made and also provides a mean for the seller to communicate with the buyer if an
    offer for an item is approved. An sms is sent to buyer if the seller approves on an offer.
    Related xml files:
        1> offers_activity.xml (provides a listview to display the compost)
        2> offers_list.xml (acts as a template for the compost on which an offer is placed)

    AsyncTask OffersL peforms the following functions:
        1> Display all the compost which has an offer placed on it.
        2> Get name of the buyer who has placed and offer on the compost item.
    Associated php files:
        1> viewoffers.php
        2> getsellersname.php

 */
public class Offers extends Activity {
    ListView listView;
    ArrayList<HashMap<String, String>> itemsList;
    Bundle b;
    String sellerid, sellerName;
    SwipeRefreshLayout swipeRefreshLayout;
    private UserController userApi;
    private OfferController offerApi;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.offers_activity);
        userApi = ApiFactory.composter.create(UserController.class);
        offerApi = ApiFactory.composter.create(OfferController.class);
        swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout_offers);
        itemsList = new ArrayList<>();
        listView = findViewById(R.id.OffersListView);
        b = getIntent().getExtras();
        sellerid = b.getString("loginb");
        buidOfferList(sellerid);
        listView.setOnItemClickListener((parent, view, position, id) -> {
            TextView contactBuyer = view.findViewById(R.id.buyerContactO);
            TextView compostName = view.findViewById(R.id.compostNameO);
            TextView buyerName = view.findViewById(R.id.buyerNameO);
            TextView Weight = view.findViewById(R.id.weightO);
            TextView Cost = view.findViewById(R.id.costO);
            String buyersName = buyerName.getText().toString();
            String compostsName = compostName.getText().toString();
            String contactsBuyer = contactBuyer.getText().toString();
            Call<SimpleResponse> nameResponse = userApi.getById(sellerid);
            nameResponse.enqueue(new Callback<SimpleResponse>() {
                @Override
                public void onResponse(Call<SimpleResponse> call, Response<SimpleResponse> response) {
                    if (response != null && response.isSuccessful() && response.body() != null) {
                        SimpleResponse body = response.body();
                        String message = "Hello, " + buyersName + " this is " + body.getResponse() + " with regard to the offer placed on " + compostsName + ".I am fine with your offer price of Rs " + Cost.getText() + " for " + Weight.getText() + " kg's";
                        SmsManager smsManager = SmsManager.getDefault();
                        try {
                            Toast.makeText(Offers.this, "sending message to buyer: " + body.getResponse(), Toast.LENGTH_SHORT).show();
                            ActivityCompat.requestPermissions(Offers.this, new String[]{Manifest.permission.SEND_SMS}, 1);
                            smsManager.sendTextMessage("+91" + contactsBuyer, null, message, null, null);
                            Toast.makeText(Offers.this, "update your inventory in case compost is sold", Toast.LENGTH_SHORT).show();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }

                @Override
                public void onFailure(Call<SimpleResponse> call, Throwable t) {
                    t.printStackTrace();
                    Toast.makeText(getApplicationContext(), "exception while fetching user by id", Toast.LENGTH_LONG).show();
                }
            });

        });
        swipeRefreshLayout.setOnRefreshListener(() -> {
            startActivity(getIntent());
            swipeRefreshLayout.setRefreshing(false);
        });
    }

    public void goHome(View v) {
        Intent inent = new Intent(this.getApplicationContext(), Seller.class);
        Bundle b1 = new Bundle();
        b1.putString("loginb", sellerid);
        inent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        inent.putExtras(b1);
        startActivity(inent);
    }

    private void buidOfferList(String id) {
        Call<List<Map<String, String>>> offerResponse = offerApi.getOffers(id);
        offerResponse.enqueue(new Callback<List<Map<String, String>>>() {
            @Override
            public void onResponse(Call<List<Map<String, String>>> call, Response<List<Map<String, String>>> response) {
                if (response != null && response.isSuccessful() && response.body() != null) {
                    List<Map<String, String>> body = response.body();
                    ListAdapter adapter = new SimpleAdapter(
                            Offers.this, body, R.layout.offers_list,
                            new String[]{"OfferId", "ItemName", "Cost", "BuyerName", "ItemWeight", "weight", "cost", "BuyerContact"},
                            new int[]{R.id.offerIdO, R.id.compostNameO, R.id.compostCostO, R.id.buyerNameO, R.id.availableWeightO, R.id.weightO, R.id.costO, R.id.buyerContactO}
                    );
                    listView.setAdapter(adapter);
                    registerForContextMenu(listView);
                }
            }

            @Override
            public void onFailure(Call<List<Map<String, String>>> call, Throwable t) {
                t.printStackTrace();
                Toast.makeText(getApplicationContext(), "exception while fetching offers", Toast.LENGTH_LONG).show();
            }
        });
    }
}
