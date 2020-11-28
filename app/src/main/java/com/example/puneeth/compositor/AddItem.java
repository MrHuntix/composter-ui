package com.example.puneeth.compositor;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import androidx.annotation.Nullable;

import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.example.puneeth.compositor.client.controllers.ItemController;
import com.example.puneeth.compositor.client.factory.ApiFactory;
import com.example.puneeth.compositor.models.NewItemRequest;
import com.example.puneeth.compositor.models.SimpleResponse;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Date;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/*
    This class acts as an interface that allows the seller to add their composts to the market.
    Related xml:
        1> itementry_activity.xml

    The AsyncTask AddCompost peforms the following functions:
        1> Addition of compost to the market.
    Associated php files:
        1> add.php
 */
public class AddItem extends Activity {
    SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
    String itemsName="",itemsCost="",itemsWeight="",date="",userId="",uploadImage="";
    EditText itemName,itemCost,itemWeight;
    Bundle b,extras;
    double lat,lng;
    private Bitmap bitmap;
    ImageView imageView;
    ListView listView;
    GPSTracker gps;
    static final int REQUEST_IMAGE_CAPTURE=1;
    private ItemController client;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.itementry_activity);
        client = ApiFactory.composter.create(ItemController.class);
        b=getIntent().getExtras();
        gps=new GPSTracker(AddItem.this);
        userId=b.getString("loginb");
        imageView=findViewById(R.id.imageView);
        itemName=findViewById(R.id.ItemName);
        itemCost=findViewById(R.id.ItemCost);
        itemWeight=findViewById(R.id.ItemWeight);
        listView=findViewById(R.id.itemsListB);
    }

    public void goHomeE(View v){
        Intent inent=new Intent(this.getApplicationContext(),Seller.class);
        Bundle b1=new Bundle();
        b1.putString("loginb",userId);
        inent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        inent.putExtras(b1);
        startActivity(inent);
    }

    public void showFileChooser(View v){
        Toast.makeText(this.getApplicationContext(),"in c",Toast.LENGTH_SHORT).show();
        Intent intent=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent,REQUEST_IMAGE_CAPTURE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode==REQUEST_IMAGE_CAPTURE && resultCode==RESULT_OK){
            extras=data.getExtras();
            bitmap=(Bitmap)extras.get("data");
            imageView.setImageBitmap(bitmap);
        }
    }

    //addCom() takes the compost deatails and stores it in the market and also takes an entry of the location of the device which will be used
    //in coloring the heatmap
    public void addCom(View v) throws ParseException {
        itemsName=itemName.getText().toString();
        itemsCost=itemCost.getText().toString();
        itemsWeight=itemWeight.getText().toString();
        lat=gps.getLatitude();
        lng=gps.getLongitude();
        Log.d("2","Latitude: "+lat+" Longitude: "+lng);
        //uploadImage=getStringImage(bitmap);
        Date dateC=new Date();
        date=formatter.format(dateC);
        Log.d("2","Date entered is: "+date);
        if((itemsCost.equals("")||itemsName.isEmpty()||itemsWeight.isEmpty())){
            Toast.makeText(this.getApplicationContext(),"fill all fields",Toast.LENGTH_SHORT).show();
        }else{
            NewItemRequest newItemRequest = new NewItemRequest(userId, itemsName, Long.parseLong(itemsCost), getStringImage(bitmap), itemsWeight, dateC.getTime(), String.valueOf(lat), String.valueOf(lng) );
            Call<SimpleResponse> newItemResponse = client.reqAddNewItem(newItemRequest);
            newItemResponse.enqueue(new Callback<SimpleResponse>() {
                @Override
                public void onResponse(Call<SimpleResponse> call, Response<SimpleResponse> response) {
                    if (response != null && response.isSuccessful() && response.body() != null) {
                        SimpleResponse body = response.body();
                        Toast.makeText(getApplicationContext(),"item added for sale",Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<SimpleResponse> call, Throwable t) {
                    t.printStackTrace();
                    Toast.makeText(getApplicationContext(),"item added for sale",Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    //house keeping function to convert camera image to a format that can be stored in the database
    private byte[] getStringImage(Bitmap bmp){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Log.d("2","aa1");
        try {
            bmp.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        }catch (Exception e){
            Log.d("2","exc: "+e.getMessage());
        }
        //Log.d("2", baos.toByteArray());
        return baos.toByteArray();
//        return imageBytes;//Base64.getEncoder().encodeToString(imageBytes);//Base64.encodeToString(imageBytes, Base64.DEFAULT);
    }
}
