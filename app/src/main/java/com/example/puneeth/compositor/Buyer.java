package com.example.puneeth.compositor;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.annotation.NonNull;

import com.example.puneeth.compositor.client.controllers.PostController;
import com.example.puneeth.compositor.client.factory.ApiFactory;
import com.example.puneeth.compositor.models.Post;
import com.example.puneeth.compositor.models.PostRequest;
import com.example.puneeth.compositor.models.SimpleResponse;
import com.google.android.material.navigation.NavigationView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;
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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/*
    This class acts as a root class for the buyer which allows the buyer to peform various actions as seen in the menu.
    Related xml:
        1> activity_buyer.xml (main UI for the buyer)
        2> content_buyer.xml (generates a list view which is populated by user posts)
        3> news_items.xml (provides a template for each post that would be placed in content_buyer.xml)

    The AsyncTask DisplayN peforms the following functions:
        1> Adding and Retreving news from the database.
    Associated php files:
        1> dispnews.php
        2> post.php
 */
public class Buyer extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private String PREFERENCE = "date";
    private EditText newsB;
    private Bundle b2, b;
    private Button postB;
    private String user;
    private String post;
    private String date;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ListView listView;
    private Intent intent;
    private SharedPreferences.Editor editor;
    private SharedPreferences sp;
    private static int count = 3;
    private PostController client;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buyer);
        client = ApiFactory.composter.create(PostController.class);
        Toolbar toolbar = findViewById(R.id.toolbar);
        b = new Bundle();
        swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout_buyer);
        sp = getSharedPreferences(PREFERENCE, MODE_PRIVATE);
        editor = sp.edit();
        postB = findViewById(R.id.postB);
        b2 = new Bundle();
        b = getIntent().getExtras();
        newsB = findViewById(R.id.newsB);
        user = b.getString("loginb");
        String userMsg = "Welcome, " + user;
        Log.d("2", userMsg);
        listView = findViewById(R.id.NewsList);

        postB = findViewById(R.id.postB);
        TextView profileName = findViewById(R.id.WelcomeMessage);
        profileName.setText(userMsg);
        try {
            profileName.setText(userMsg);
        } catch (Exception e) {
            e.printStackTrace();
        }
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);//changed set to add
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        editor.putString("currentdate" + user, new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date()));
        editor.apply();
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeRefreshLayout.setRefreshing(true);
                updateUIForPost();
                swipeRefreshLayout.setRefreshing(false);
            }
        });
        updateUIForPost();
        currentDate();
    }

    private void updateUIForPost() {
        Call<List<Post>> postsResponse = client.getPosts();
        postsResponse.enqueue(new Callback<List<Post>>() {
            @Override
            public void onResponse(Call<List<Post>> call, Response<List<Post>> response) {
                if (response != null && response.isSuccessful() && response.body() != null) {
                    List<Post> body = response.body();
                    if (body.isEmpty()) {
                        Toast.makeText(getApplicationContext(), "No news events", Toast.LENGTH_SHORT).show();
                    } else {
                        ListAdapter adapter = new SimpleAdapter(Buyer.this, body.stream().map(news -> {
                            HashMap<String, String> items = new HashMap<>();
                            items.put("news", news.getNews());
                            items.put("postedon", news.getPostedon());
                            return items;
                        }).collect(Collectors.toList()), R.layout.news_items,
                                new String[]{"news", "postedon"}, new int[]{R.id.newsFeed, R.id.postedOnN});
                        listView.setAdapter(adapter);
                        registerForContextMenu(listView);
                    }
                }
            }

            @Override
            public void onFailure(Call<List<Post>> call, Throwable t) {
                t.printStackTrace();
                Toast.makeText(getApplicationContext(), "exception while fetching posts", Toast.LENGTH_LONG).show();
            }
        });
    }

    /*
        This method handles the posting of a news into the database. The user is limited to 3 posts per day in order to avoid spam.
     */
    public void postB(View v) {
        post = newsB.getText().toString();
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        Date dateO = new Date();
        date = dateFormat.format(dateO);
        if (count >= 1) {
            Toast.makeText(this.getApplicationContext(), "Remaining posts: " + count, Toast.LENGTH_SHORT).show();
            Log.d("2", "Remaining posts: " + count);
            if (post.isEmpty()) {
                Toast.makeText(this, "Enter a valid post", Toast.LENGTH_SHORT).show();
            } else {
                count--;
                PostRequest postRequest = new PostRequest(post, date);
                Call<SimpleResponse> postResponse = client.addPost(postRequest);
                postResponse.enqueue(new Callback<SimpleResponse>() {
                    @Override
                    public void onResponse(Call<SimpleResponse> call, Response<SimpleResponse> response) {
                        if (response != null && response.isSuccessful() && response.body() != null) {
                            SimpleResponse body = response.body();
                            Toast.makeText(getApplicationContext(), "news feed updated", Toast.LENGTH_SHORT).show();
                            startActivity(getIntent());
                        }
                    }

                    @Override
                    public void onFailure(Call<SimpleResponse> call, Throwable t) {
                        t.printStackTrace();
                        Toast.makeText(getApplicationContext(), "exception while adding post", Toast.LENGTH_LONG).show();
                    }
                });
            }

        } else {
            Toast.makeText(this.getApplicationContext(), "in order to prevent spam you are limited to 3 posts per day", Toast.LENGTH_SHORT).show();
            postB.setEnabled(false);
            postB.setFocusable(false);
            editor.putString("lockeddate" + user, new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date()));
            editor.apply();
        }

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    public void currentDate() {
        SharedPreferences getter = getSharedPreferences(PREFERENCE, MODE_PRIVATE);
        String currentDate = (getter.getString("currentdate" + user, ""));
        String lockedDate = (getter.getString("lockeddate" + user, ""));
        Log.d("2", "current date: " + currentDate + " locked date: " + lockedDate);
        if (currentDate.isEmpty() && lockedDate.isEmpty()) {
            postB.setEnabled(true);
            postB.setFocusable(true);
            count = 3;
        } else if (currentDate.equals(lockedDate)) {
            postB.setEnabled(false);
            postB.setFocusable(false);
        } else {
            postB.setEnabled(true);
            postB.setFocusable(true);
            count = 3;
        }
    }

    /*
        This is method is overidden to handle the menu clicks
     */
    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        if (id == R.id.nav_store) {
            // Handle the camera action
            intent = new Intent(this.getApplicationContext(), Store.class);
            b2.putString("loginb", user);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.putExtras(b2);
            startActivity(intent);
        } else if (id == R.id.nav_edit) {
            intent = new Intent(this.getApplicationContext(), EditBuyer.class);
            b2.putString("loginb", user);
            b2.putString("type", "buyer");
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.putExtras(b2);
            startActivity(intent);
        } else if (id == R.id.nav_feed) {
            intent = new Intent(this.getApplicationContext(), Buyer.class);
            b2.putString("loginb", user);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.putExtras(b2);
            startActivity(intent);
        } else if (id == R.id.log_out_buyer) {
            toast("logging out");
            startActivity(new Intent(this, Login.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
        } else if (id == R.id.cart_buyer) {
            intent = new Intent(getApplicationContext(), Cart.class);
            b2.putString("loginb", user);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.putExtras(b2);
            startActivity(intent);
        }
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void toast(String s) {
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
    }
}

