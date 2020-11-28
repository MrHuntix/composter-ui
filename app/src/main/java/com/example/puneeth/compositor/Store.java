package com.example.puneeth.compositor;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import android.util.Base64;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.puneeth.compositor.client.controllers.ItemController;
import com.example.puneeth.compositor.client.factory.ApiFactory;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/*
    This class handles the various operations that can be peformed on an item is the store. On selecting a compost the PlaceOffer.java class is loaded.
    Related xml:
        1> activity_store_list.xml (generates a list view to hold the composts that is available)
        2> item_interface.xml (provides a template for each of the compost that is currently available in the market)

    AsyncTask Display peforms the following functions:
        1> Retrieve the compost from the database to fill the market.
    Associated php files:
        1> displayitems.php
 */

public class Store extends Activity {
    private ListView listView;
    private Bundle b1;
    private String userid,Itemid,sellerContact;
    private ArrayList<HashMap<String, Object>> itemsList;

    private ItemController client;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_store_list);
        client = ApiFactory.composter.create(ItemController.class);
        itemsList=new ArrayList<>();
        listView=findViewById(R.id.itemsListB);
        b1=new Bundle();
        Bundle b = getIntent().getExtras();
        userid= b.getString("loginb");
        dispItems();

        listView.setOnItemClickListener((parent, view, position, id) -> {
            Toast.makeText(Store.this,"clicked",Toast.LENGTH_SHORT).show();
            TextView textView=view.findViewById(R.id.BuyerItemId);
            TextView name= view.findViewById(R.id.BuyerItemName);
            TextView weight= view.findViewById(R.id.ItemWeightBuyer);
            TextView cost= view.findViewById(R.id.ItemCostBuyer) ;
            TextView sellersContact= view.findViewById(R.id.sellerContact);
            TextView sellerName= view.findViewById(R.id.ItemSellernameBuyer);
            Itemid=textView.getText().toString();
            sellerContact=sellersContact.getText().toString();
            Intent intent=new Intent(Store.this,PlaceOffer.class);
            b1.putString("itemid",textView.getText().toString());
            b1.putString("name",name.getText().toString());
            b1.putString("weight",weight.getText().toString());
            b1.putString("cost",cost.getText().toString());
            b1.putString("sellername",sellerName.getText().toString());
            b1.putString("loginb",userid);
            b1.putString("sellerContact",sellerContact);
            intent.putExtras(b1);
            startActivity(intent);
        });
        final SwipeRefreshLayout swipeRefreshLayout= findViewById(R.id.swipe_refresh_layout_buyer_store);
        swipeRefreshLayout.setOnRefreshListener(() -> {
            swipeRefreshLayout.setRefreshing(true);
            itemsList.clear();
            dispItems();
            swipeRefreshLayout.setRefreshing(false);
        });
    }

    // goes to homescreen.
    public void goHomeS(View v){
        Intent inent=new Intent(this.getApplicationContext(),Buyer.class);
        Bundle b1=new Bundle();
        b1.putString("loginb",userid);
        inent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        inent.putExtras(b1);
        startActivity(inent);
    }

    private void dispItems() {
        Call<List<Map<String, String>>> dispResponse = client.dispItems();
        dispResponse.enqueue(new Callback<List<Map<String, String>>>() {
            @Override
            public void onResponse(Call<List<Map<String, String>>> call, Response<List<Map<String, String>>> response) {
                if (response != null && response.isSuccessful() && response.body() != null) {
                    List<Map<String, String>> body = response.body();
                    ArrayList<RowItem> result = new ArrayList<>();
                    body.forEach(item->{
                        RowItem rowItem=new RowItem();
                        rowItem.setCompostId(item.get("ItemId"));
                        rowItem.setSellerName(item.get("Name"));
                        rowItem.setContact(item.get("Contact"));
                        rowItem.setCompostName(item.get("ItemName"));
                        rowItem.setCost(item.get("Cost"));
                        rowItem.setDate(item.get("DayPosted"));
                        rowItem.setWeight(item.get("ItemWeight"));
                        String StoreImage=item.get("Image");
                        byte[] decodedString= java.util.Base64.getDecoder().decode(StoreImage);
                                //Base64.decode(StoreImage);
                        Bitmap decodedByte= BitmapFactory.decodeByteArray(decodedString,0,decodedString.length);
                        rowItem.setImage(decodedByte);
                        result.add(rowItem);
                    });
                    listView.setAdapter(new CustomBaseAdapter(getApplicationContext(), result));
                    registerForContextMenu(listView);
                }
            }

            @Override
            public void onFailure(Call<List<Map<String, String>>> call, Throwable t) {
                t.printStackTrace();
                Toast.makeText(getApplicationContext(), "exception while fetching items", Toast.LENGTH_LONG).show();
            }
        });
    }

    //refreshes the market
    public void refreshStore(View v){
        startActivity(getIntent());
    }
}
