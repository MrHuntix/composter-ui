package com.example.puneeth.compositor;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.annotation.Nullable;

import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.example.puneeth.compositor.client.controllers.UserController;
import com.example.puneeth.compositor.client.factory.ApiFactory;
import com.example.puneeth.compositor.models.LoginRequest;
import com.example.puneeth.compositor.models.RegisterRequest;
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
    This class handles the login and registration of a user who can either be a buyer or an seller.
    Related xml:
        1> activity_main.xml(UI for login and registration).
    A global variable which is used to identify the user in all activities is passed through the intents as loginb which stored the contact number.
    AsyncTask class register runs a background thread that
        1> Talks to the database to verify the entered details.
        2> Delets all items from the store whose weight has reached 0.
    The server side scripting is done with php and associated php files for this class is:
        1> deletezero.php
        2> login.php
        3> addbuyer.php
        4> addseller.php
 */

public class Login extends Activity {
    private UserController client;
    private EditText nameR,contactR,passwordR,contactL,passwordL;
    private CheckBox buyerR,sellerR,buyerL,sellerL;
    private String loginContact,pass,loginChoice,username,contactnumber,registerPass,option;
    public Intent intent;
    private String PREFERENCE="date";
    private SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    private Bundle b;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        client = ApiFactory.composter.create(UserController.class);

        b=new Bundle();

        nameR=findViewById(R.id.userName);
        contactR=findViewById(R.id.contactNumber);
        passwordR=findViewById(R.id.RegisterPassword);
        contactL=findViewById(R.id.ContactB);
        passwordL=findViewById(R.id.LoginPassword);
        sharedPreferences=getSharedPreferences(PREFERENCE,MODE_PRIVATE);

        editor=sharedPreferences.edit();

        buyerR=findViewById(R.id.buyer);
        sellerR= findViewById(R.id.seller);
        buyerL=findViewById(R.id.LoginBoxBuyer);
        sellerL=findViewById(R.id.LoginBoxSeller);
    }

    /*
        The login() method is used to handle the logging in of a user and handles login for either buyer or seller
     */
    public void login(View v){
        loginContact=contactL.getText().toString();
        pass=passwordL.getText().toString();
        if(buyerL.isChecked()&&!sellerL.isChecked()){
            loginChoice="buyer";
        }else if(!buyerL.isChecked()&&sellerL.isChecked()){
            loginChoice="seller";
        }else {
            Toast.makeText(getApplicationContext(),"please select one of the checkboxes",Toast.LENGTH_SHORT).show();
        }
        buyerL.setChecked(false);
        sellerL.setChecked(false);
        if(!loginContact.isEmpty()&&!pass.equals("")){

            LoginRequest request = new LoginRequest(loginContact, pass, loginChoice);
            Call<SimpleResponse> response = client.login(request);
            response.enqueue(new Callback<SimpleResponse>() {
                @Override
                public void onResponse(Call<SimpleResponse> call, Response<SimpleResponse> response) {
                    if(response!=null && response.isSuccessful() && response.body()!=null) {
                        SimpleResponse loginResponse = response.body();
                        Toast.makeText(getApplicationContext(), loginResponse.getResponse(), Toast.LENGTH_LONG).show();
                        contactL.setText("");
                        passwordL.setText("");
                        postProcess(loginChoice, loginContact, loginChoice,  getApplicationContext());
                    }
                }

                @Override
                public void onFailure(Call<SimpleResponse> call, Throwable t) {
                    t.printStackTrace();
                    Toast.makeText(getApplicationContext(), "exception while logging in", Toast.LENGTH_LONG).show();
                }
            });
        }else{
            Toast.makeText(this.getApplicationContext(),"Fill all fields",Toast.LENGTH_SHORT).show();
            contactL.setError("contact field cannot be empty");
            passwordL.setError("password field cannot be empty");
        }
    }

    /*
        The register() method handles registration for both buyer and seller
     */
    public void register(View v){
        username=nameR.getText().toString();
        contactnumber=contactR.getText().toString();
        registerPass=passwordR.getText().toString();
        if(username.isEmpty()||contactnumber.isEmpty()||registerPass.isEmpty()){
            if(username.isEmpty()){
                nameR.setError("username field is empty");
            }if(contactnumber.isEmpty()){
                contactR.setError("phone number cannot be empty");
            }if(registerPass.isEmpty()){
                passwordR.setError("password field cannot be empty");
            }
            Toast.makeText(this.getApplicationContext(), "fill all fields", Toast.LENGTH_SHORT).show();
        }else {
            if (!(username.isEmpty() && contactnumber.isEmpty() && registerPass.isEmpty()) && contactnumber.length() == 10) {
                if (buyerR.isChecked() && !sellerR.isChecked()) {
                    buyerR.setChecked(false);
                    sellerR.setChecked(false);
                    option = "buyer";
                    RegisterRequest registerRequest = new RegisterRequest(username, contactnumber, registerPass);
                    Call<SimpleResponse> response = client.buyer(registerRequest);
                    response.enqueue(new Callback<SimpleResponse>() {
                        @Override
                        public void onResponse(Call<SimpleResponse> call, Response<SimpleResponse> response) {
                            if(response!=null && response.isSuccessful() && response.body()!=null) {
                                SimpleResponse body = response.body();
                                Toast.makeText(getApplicationContext(), body.getResponse(), Toast.LENGTH_SHORT).show();
                                nameR.setText("");
                                contactR.setText("");
                                passwordR.setText("");
                                postProcess(option, contactnumber, registerPass, getApplicationContext());
                            }
                        }

                        @Override
                        public void onFailure(Call<SimpleResponse> call, Throwable t) {
                            t.printStackTrace();
                            Toast.makeText(getApplicationContext(), "exception while registering", Toast.LENGTH_LONG).show();
                        }
                    });



                } else if (!buyerR.isChecked() && sellerR.isChecked()) {
                    buyerR.setChecked(false);
                    sellerR.setChecked(false);
                    option = "seller";
                    RegisterRequest registerRequest = new RegisterRequest(username, contactnumber, registerPass);
                    Call<SimpleResponse> response = client.seller(registerRequest);
                    response.enqueue(new Callback<SimpleResponse>() {
                        @Override
                        public void onResponse(Call<SimpleResponse> call, Response<SimpleResponse> response) {
                            if(response!=null && response.isSuccessful() && response.body()!=null) {
                                SimpleResponse body = response.body();
                                Toast.makeText(getApplicationContext(), body.getResponse(), Toast.LENGTH_SHORT).show();
                                nameR.setText("");
                                contactR.setText("");
                                passwordR.setText("");
                                postProcess(option, contactnumber, registerPass, getApplicationContext());
                            }
                        }

                        @Override
                        public void onFailure(Call<SimpleResponse> call, Throwable t) {
                            t.printStackTrace();
                            Toast.makeText(getApplicationContext(), "exception while registering", Toast.LENGTH_LONG).show();
                        }
                    });

                } else {
                    Toast.makeText(this.getApplicationContext(), "select one of the two", Toast.LENGTH_SHORT).show();
                }
            } else {
                contactR.setError("entered phone number is of a invalid length");
            }
        }
    }

    private void postProcess(String result, String contact, String password, Context context) {
        switch (result) {
            case "deleted":
                break;
            case "successfull":
                Toast.makeText(this.getApplicationContext(), "Successfull registration", Toast.LENGTH_SHORT).show();
                break;
            case "buyer":
                Toast.makeText(context, "Successfull buyer login", Toast.LENGTH_SHORT).show();
                intent = new Intent(context, Buyer.class);
                editor.putString("username",contact);
                editor.putString("password",password);
                editor.apply();
                b.putString("loginb", contact);
                intent.putExtras(b);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                break;
            case "seller":
                intent = new Intent(context, Seller.class);
                editor.putString("username",contact);
                editor.putString("password",password);
                editor.apply();
                b.putString("loginb", contact);
                intent.putExtras(b);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                break;
            case "nouser":
                Toast.makeText(context, "Wrong username/password", Toast.LENGTH_SHORT).show();
                break;
            case "exists":
                Toast.makeText(context, "user with contact number already exists", Toast.LENGTH_SHORT).show();
                break;
        }
    }
}
