package com.example.puneeth.compositor;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.annotation.Nullable;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.widget.EditText;
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
import java.util.ArrayList;

/*
    This class generates an optimal list of items that gives maximum profit that a seller can transport as transportation vehicles can carry
    upto a certain weight.
    Associated xml files:
        1> activity_optimise.xml (displays an optimal list of compost that would maximise the profit)

    AsyncTask WeightVal peforms the function of retreiving the compost on which offers has been made.
    Associated php files:
        1> viewoffers.php

 */
public class Optimiser extends Activity {
    EditText cost;
    Bundle b;
    String userId;
    TextView optimalCost,mainText;
    ArrayList<Integer> wt;
    ArrayList<Integer> val;
    String items="";
    SparseArray itemLink;
    int optW;
    @SuppressLint("UseSparseArrays")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        wt=new ArrayList<>();
        val=new ArrayList<>();
        setContentView(R.layout.activity_optimise);
        itemLink=new SparseArray();
        cost = (EditText) findViewById(R.id.optimisedCost);
        optimalCost = (TextView) findViewById(R.id.optimalCost);
        mainText=(TextView)findViewById(R.id.mainMsg);
        mainText.setVisibility(View.INVISIBLE);
        b = getIntent().getExtras();
        userId = b.getString("loginb");
    }

    public int max(int a, int b) {
        return (a > b) ? a : b;
    }

    public void optimise(View v){
        items="";
        wt.clear();
        val.clear();
        optW=Integer.valueOf(cost.getText().toString());
        new WeightVal(this).execute("optimise");
    }

    int optimalCost(int W, ArrayList wt, ArrayList val, int n) {
        int i, w;
        int K[][] = new int[n + 1][W + 1];
        for (i = 0; i <= n; i++) {
            for (w = 0; w <= W; w++) {
                if (i == 0 || w == 0)
                    K[i][w] = 0;
                else if ((int)wt.get(i-1)<= w)
                    K[i][w] = max((int)val.get(i - 1) + K[i - 1][w - (int)wt.get(i - 1)], K[i - 1][w]);
                else
                    K[i][w] = K[i - 1][w];
            }
        }
        int N=n;
        int i1=n,k1=W,j=1;
        while(i1>0&&k1>0){
            if(K[i1][k1]!= K[i1-1][k1])
            {
                n=i1;
                items+=j+"> "+itemLink.get(i1)+"\n";
                j++;
                i1=i1-1;
                Log.d("2","value of n: "+n);
                k1=k1-(int)wt.get(n-1);

            }
            else
                i1=i1-1;
        }
        return K[N][W];
    }

    private class WeightVal extends AsyncTask<String, String, String> {
        Context context;

        WeightVal(Context ctx) {
            this.context = ctx;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
            StringBuilder response = new StringBuilder();
            String reg_url, option = params[0];
            if (option.equals("optimise")) {
                try {
                    reg_url = "http://"+new IP().getIp()+"/examp/android/viewoffers.php";
                    URL url = new URL(reg_url);
                    HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                    httpURLConnection.setRequestMethod("POST");
                    httpURLConnection.setDoInput(true);
                    httpURLConnection.setDoOutput(true);
                    BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(httpURLConnection.getOutputStream(), "UTF-8"));
                    String data = URLEncoder.encode("sellerid", "UTF-8") + "=" + URLEncoder.encode(userId, "UTF-8");
                    bufferedWriter.write(data);
                    bufferedWriter.flush();
                    bufferedWriter.close();
                    InputStream inputStream = httpURLConnection.getInputStream();
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "iso-8859-1"));
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        response.append(line).append("\n");
                    }
                    bufferedReader.close();
                    inputStream.close();
                    httpURLConnection.disconnect();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return response.toString().trim();
        }

        @Override
        protected void onPostExecute(String aVoid) {
            super.onPostExecute(aVoid);
            if (aVoid.isEmpty()) {
                Toast.makeText(context, "you dont have any compost in store", Toast.LENGTH_SHORT).show();
            } else {
                try {
                    JSONArray jsonArray = new JSONArray(aVoid);
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject c = jsonArray.getJSONObject(i);
                        String ItemWeight = c.getString("weight");
                        String ItemName=c.getString("ItemName");
                        wt.add(Integer.parseInt(ItemWeight));
                        String Cost = c.getString("cost");
                        val.add(Integer.parseInt(Cost));
                        String BuyerName=c.getString("Name");
                        itemLink.append(i+1,"buyer name: "+BuyerName+" compost name: "+ItemName+" weight: "+ItemWeight+" cost: "+Cost);
                    }
                    int totalProfit=optimalCost(optW,wt,val,wt.size());
                    String message;
                    if(totalProfit==0){
                        message="weight entered is very less";
                    }else{
                        mainText.setVisibility(View.VISIBLE);
                        message=items+"\ngainable profit: "+String.valueOf(totalProfit);
                    }
                    optimalCost.setText(message);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
