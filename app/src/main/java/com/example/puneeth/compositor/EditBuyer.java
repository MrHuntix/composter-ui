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
import android.widget.Toast;

import com.example.puneeth.compositor.client.controllers.UserController;
import com.example.puneeth.compositor.client.factory.ApiFactory;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

/*
    This class handles the task of editing the user information for both the buyer and seller.
    Related xml:
        1> activity_edit_nuyer.xml (provides a UI to enter new details)

    The AsnycTask Change peforms the following functions:
        1> Updates the database with the new information.
    Associated php files:
        1> editbuyer.php
 */
public class EditBuyer extends Activity {
    private EditText newName, newContact, newPassword;
    private String name, contact, password;
    public Bundle b;
    private String userid, user, type;
    private UserController client;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_buyer);
        client = ApiFactory.composter.create(UserController.class);
        b = getIntent().getExtras();
        userid = b.getString("loginb");
        user = b.getString("user");
        type = b.getString("type");
        newName = findViewById(R.id.NewName);
        newContact = findViewById(R.id.NewContact);
        newPassword = findViewById(R.id.NewPassword);
    }

    // THe change() takes the new details and updates the data base
    public void change(View v) {
        name = newName.getText().toString();
        contact = newContact.getText().toString();
        password = newPassword.getText().toString();
        if ((name.isEmpty() && contact.isEmpty() && password.isEmpty())) {
            Toast.makeText(this.getApplicationContext(), "Fill up all fields", Toast.LENGTH_SHORT).show();
        } else {
            //new Change(this).execute();
            Toast.makeText(getApplicationContext(), "update feature coming soon", Toast.LENGTH_LONG).show();
            //TODO: add update api
        }
    }

    public void goHomeE(View v) {
        Intent inent;
        if (type.equals("buyer")) {
            inent = new Intent(this.getApplicationContext(), Buyer.class);
        } else {
            inent = new Intent(this.getApplicationContext(), Seller.class);
        }
        Bundle b1 = new Bundle();
        b1.putString("loginb", userid);
        inent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        inent.putExtras(b1);
        startActivity(inent);
    }
}
