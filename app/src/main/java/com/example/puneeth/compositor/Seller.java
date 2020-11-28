package com.example.puneeth.compositor;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

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
import java.util.stream.Collectors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/*
    This class acts a root for the seller which allows the user to peform various operations as seen in the menu
    Related xml:
        1> activity_seller.xml (main UI for the seller)
        2> content_seller.xml (generates a list view which is populated by user posts)
        3> news_items.xml (provides a template for each post that would be placed in content_buyer.xml)

    The AsyncTask DisplayNseller peforms the same functions of the AsyncTask in Buyer.class
 */
public class Seller extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private Bundle b1;
    private String PREFERENCE="date";
    private SharedPreferences sp;
    private TextView profileName;
    private EditText newsB;
    private Button postB;
    private ListView listView;
    private String user="";
    private String post="";
    private String date="";
    private SharedPreferences.Editor editor;
    private static int count=3;
    private SwipeRefreshLayout swipeRefreshLayout;
    private Bundle b;

    private PostController client;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seller);
        client = ApiFactory.composter.create(PostController.class);
        Toolbar toolbar=findViewById(R.id.toolbar_seller);
        swipeRefreshLayout=findViewById(R.id.swipe_refresh_layout_seller);
        sp=getSharedPreferences(PREFERENCE,MODE_PRIVATE);
        editor=sp.edit();
        DrawerLayout drawer=findViewById(R.id.drawer_layout_seller);
        b= new Bundle();
        b1=new Bundle();
        b =getIntent().getExtras();
        newsB=findViewById(R.id.newsB_seller);
        postB=findViewById(R.id.postB_seller);
        profileName =  findViewById(R.id.WelcomeMessageSeller);
        listView=findViewById(R.id.NewsList_seller);
        user= b.getString("loginb");
        String userMsg = "Welcome, " + user;
        editor.putString("currentdates"+user,new SimpleDateFormat("dd/MM/yyyy",Locale.getDefault()).format(new Date()));
        editor.apply();
        profileName.setText(userMsg);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);//changed set to add
        toggle.syncState();
        NavigationView navigationView =  findViewById(R.id.nav_view_seller);
        navigationView.setNavigationItemSelectedListener(this);
        swipeRefreshLayout.setOnRefreshListener(() -> swipeRefreshLayout.setRefreshing(false));
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
                        ListAdapter adapter = new SimpleAdapter(Seller.this, body.stream().map(news -> {
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

    public void currentDate(){
        SharedPreferences getter=getSharedPreferences(PREFERENCE,MODE_PRIVATE);
        String currentDate=(getter.getString("currentdates"+user,""));
        String lockedDate=(getter.getString("lockeddates"+user,""));
        if(currentDate.isEmpty()&&lockedDate.isEmpty()){
            postB.setEnabled(true);
            postB.setFocusable(true);
            count=3;
        }else if(currentDate.equals(lockedDate)){
            postB.setEnabled(false);
            postB.setFocusable(false);
        }else{
            postB.setEnabled(true);
            postB.setFocusable(true);
            count=3;
        }
    }

    // this method takes the event entered by the user and adds it to the news feed
    public void postBseller(View v){
        post=newsB.getText().toString();
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy",Locale.getDefault());
        Date dateO = new Date();
        date = dateFormat.format(dateO);
        if(count>=1){
            Toast.makeText(this.getApplicationContext(),"Remaining posts: "+count,Toast.LENGTH_SHORT).show();
            if(post.isEmpty()){
                Toast.makeText(this,"Enter a valid post",Toast.LENGTH_SHORT).show();
            }else{
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

        }else{
            Toast.makeText(this.getApplicationContext(),"in order to prevent spam you are limited to 3 posts per day",Toast.LENGTH_SHORT).show();
            postB.setEnabled(false);
            postB.setFocusable(false);
            editor.putString("lockeddates"+user,date);
            editor.apply();
        }

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer =  findViewById(R.id.drawer_layout_seller);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    public void toast(String s){
        Toast.makeText(this,s,Toast.LENGTH_SHORT).show();
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id=item.getItemId();
        Intent intent;
        if(id==R.id.nav_add_seller){
            toast("adding item to market");
            intent =new Intent(this,AddItem.class);
            b1.putString("loginb",user);
            intent.putExtras(b1);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }else if(id==R.id.nav_edit_seller){
            toast("editing information");
            intent =new Intent(this.getApplicationContext(), EditBuyer.class);
            b1.putString("loginb",user);
            b1.putString("type","seller");
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.putExtras(b1);
            startActivity(intent);
        }else if(id==R.id.nav_feed_seller){
            toast("news feed");
            intent =new Intent(this,Seller.class);
            b1.putString("loginb",user);
            intent.putExtras(b1);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }else if(id==R.id.nav_unsold_seller){
            toast("displaying unsold items");
            intent =new Intent(this,Unsold.class);
            b1.putString("loginb",user);
            intent.putExtras(b1);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }else if(id==R.id.nav_offers_seller){
            toast("showing offers");
            intent =new Intent(this,Offers.class);
            b1.putString("loginb",user);
            intent.putExtras(b1);
            startActivity(intent);
        }else if(id==R.id.log_out_seller){
            toast("logging out");
            startActivity(new Intent(this,Login.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
        }else if(id==R.id.nav_optimise_seller){
            intent =new Intent(this,Optimiser.class);
            b1.putString("loginb",user);
            intent.putExtras(b1);
            startActivity(intent);
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout_seller);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
