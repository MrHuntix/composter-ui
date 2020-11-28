package com.example.puneeth.compositor;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.example.puneeth.compositor.client.controllers.ItemController;
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

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/*
    This class acts as an inventory management of compost that is present in the market and each compost item can be editted or deleted from the market.
    Related xml files:
        1> activity_unsold.xml (provides a list view to hold the unsold compost)
        2> unsold_items.xml (provides a template for the compost that does not have any offers placed on it)

    AsyncTask InteractUnsold peforms the following functions:
        1> Display all the compost items that has been posted by the seller.
    Associated php files:
        1> displaycommon.php
 */
public class Unsold extends Activity {
    private Bundle b1;
    private TextView item;
    private ListView listView;
    private ArrayList<HashMap<String,String>> itemsList;
    private SwipeRefreshLayout swipeRefreshLayout;
    String userId,selectedId;
    private ItemController client;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_unsold);
        client = ApiFactory.composter.create(ItemController.class);
        itemsList = new ArrayList<>();
        listView =  findViewById(R.id.unsoldItemsS);
        swipeRefreshLayout =  findViewById(R.id.swipe_refresh_layout_seller);
        Bundle b = getIntent().getExtras();
        b1 = new Bundle();
        userId = b.getString("loginb");
        Call<List<HashMap<String, String>>> reqItemResponse = client.reqItemForUser(userId);
        reqItemResponse.enqueue(new Callback<List<HashMap<String, String>>>() {
            @Override
            public void onResponse(Call<List<HashMap<String, String>>> call, Response<List<HashMap<String, String>>> response) {
                if (response != null && response.isSuccessful() && response.body() != null) {
                    List<HashMap<String, String>> body = response.body();
                    ListAdapter listAdapter=new SimpleAdapter(
                            getApplicationContext(),body,
                            R.layout.unsold_items,
                            new String[]{"ItemId","Cost","ItemName","ItemWeight"},
                            new int[]{R.id.ItemIdB,R.id.CostB,R.id.ItemNameB,R.id.ItemWeightB}
                    );
                    listView.setAdapter(listAdapter);
                    registerForContextMenu(listView);
                }
            }

            @Override
            public void onFailure(Call<List<HashMap<String, String>>> call, Throwable t) {
                t.printStackTrace();
                Toast.makeText(getApplicationContext(), "exception while fetching items", Toast.LENGTH_LONG).show();
            }
        });
        listView.setOnItemClickListener((parent, view, position, id) -> {
            item = view.findViewById(R.id.ItemIdB);
            TextView name = view.findViewById(R.id.ItemNameB);
            TextView weight = view.findViewById(R.id.ItemWeightB);
            TextView cost = view.findViewById(R.id.CostB);
            selectedId = item.getText().toString();
            Intent inventory = new Intent(Unsold.this, Inventory.class);
            b1.putString("id", selectedId);
            b1.putString("loginb",userId);
            b1.putString("name", name.getText().toString());
            b1.putString("weight", weight.getText().toString());
            b1.putString("cost", cost.getText().toString());
            inventory.putExtras(b1);
            inventory.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(inventory);
        });

    }

    public void goHomeU(View v){
        Intent inent=new Intent(this.getApplicationContext(),Seller.class);
        Bundle b1=new Bundle();
        b1.putString("loginb",userId);
        inent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        inent.putExtras(b1);
        startActivity(inent);
    }

    public void refreshInventory(View v){
        startActivity(getIntent());
    }
}
