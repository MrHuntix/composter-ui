package com.example.puneeth.compositor;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.Nullable;

import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.puneeth.compositor.client.controllers.OfferController;
import com.example.puneeth.compositor.client.factory.ApiFactory;
import com.example.puneeth.compositor.models.OfferRequest;
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
    This class handles the process of placing an offer on a compost which can be viewed by the seller who owns that compost.
    Related xml:
        1> offer_interface.xml (provides a UI for placing the offer)

    The AsyncTask PostOffer peforms the following functions:
        1> Adds the offer to the database.
    Associated php files:
        1> placeOffer.php
 */
public class PlaceOffer extends Activity {
    EditText weight, offer;
    TextView finmsg;
    Bundle b;
    String message, Itemid;
    private OfferController client;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.offer_interface);
        client = ApiFactory.composter.create(OfferController.class);
        weight = findViewById(R.id.requiredWeight);
        offer = findViewById(R.id.offerPrice);
        finmsg = findViewById(R.id.finalMessage);
        b = getIntent().getExtras();
        Itemid = b.getString("itemid");
        message = "For selected item '" + b.getString("name") + "' posted by '" + b.getString("sellername") + "' having a cost per kg as '" + b.getString("cost") + "' and available weight is: '" + b.getString("weight") + "'";
        finmsg.setText(message);
    }

    // offer() thakes the weight and entered cost and stores into the database which would be displayed to the seller.
    public void offer(View v) {
        String weightS = weight.getText().toString();
        String costS = offer.getText().toString();
        if (weightS.isEmpty() || costS.isEmpty()) {
            Toast.makeText(this, "fill all fields", Toast.LENGTH_SHORT).show();
        } else if (Integer.valueOf(weightS) > Integer.valueOf(b.getString("weight"))) {
            Toast.makeText(this, "required weight is more than available weight", Toast.LENGTH_SHORT).show();
        } else {
            OfferRequest offerRequest = new OfferRequest(Itemid, b.getString("loginb"), b.getString("sellerContact"), weightS, costS);
            Call<SimpleResponse> offerResponse = client.placeOffer(offerRequest);
            offerResponse.enqueue(new Callback<SimpleResponse>() {
                @Override
                public void onResponse(Call<SimpleResponse> call, Response<SimpleResponse> response) {
                    if (response != null && response.isSuccessful() && response.body() != null) {
                        SimpleResponse body = response.body();
                        if ("exists".equals(body.getResponse())) {
                            Toast.makeText(getApplicationContext(), "offer already placed for the compost", Toast.LENGTH_SHORT).show();
                            viewStore();
                        }
                        if ("offered".equals(body.getResponse())) {
                            Toast.makeText(getApplicationContext(), "seller will contact you shortly", Toast.LENGTH_LONG).show();
                            viewStore();
                        }
                    }
                }

                @Override
                public void onFailure(Call<SimpleResponse> call, Throwable t) {
                    t.printStackTrace();
                    Toast.makeText(getApplicationContext(), "exception while posting offer", Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    // viewStore() takes the user back to the store
    public void viewStore() {
        Intent store = new Intent(this, Store.class);
        Bundle b1 = new Bundle();
        b1.putString("loginb", b.getString("loginb"));
        store.putExtras(b1);
        store.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(store);
    }
}
