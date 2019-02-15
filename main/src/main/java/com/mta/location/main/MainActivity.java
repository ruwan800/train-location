package com.mta.location.main;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.mta.location.main.Objects.LocationData;
import com.mta.location.main.Objects.Res;
import com.mta.location.main.Objects.Train;
import com.mta.location.main.util.Cache;
import com.mta.location.main.util.HttpQueue;
import com.mta.location.main.util.MessageQueue;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private FusedLocationProviderClient mFusedLocationClient;
    private LocationCallback mLocationCallback;
    private boolean mRequestingLocationUpdates;
    private LocationRequest mLocationRequest;
    public static final String PREVIOUS_LOCATION = "previous_location";
    private Gson gson;
    private static final String SITE = "http://157.230.166.94:3000/";
    private TextView logView;
    private MessageQueue messageQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        final long time = new Date().getTime();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_log);
        logView = findViewById(R.id.logView);
        messageQueue = MessageQueue.getInstance();

        mRequestingLocationUpdates = true;
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        GsonBuilder gsonBuilder = new GsonBuilder();
        //gsonBuilder.setDateFormat("M/d/yy hh:mm a");
        gson = gsonBuilder.create();

        final String url = SITE + "api/public/location-update";
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    //Location previousLocation = Cache.getLocation(MainActivity.this, PREVIOUS_LOCATION);
                    //if(previousLocation != null || true) {
                        //float distance = location.distanceTo(previousLocation);
                        //if (200 < distance || true) {
                            LocationData locationData = new LocationData(location, time);
                            Cache.put(MainActivity.this, PREVIOUS_LOCATION, location);
                            try {
                                sendPostRequest(url, locationData);
                            } catch (JSONException e) {
                                Log.e("MTA", e.getMessage());
                            }
                        //}
                    //}
                    Toast.makeText(MainActivity.this, location.toString(), Toast.LENGTH_SHORT).show();
                }
            }
        };
        createLocationRequest();
        runUpdateTask();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mRequestingLocationUpdates) {
            startLocationUpdates();
        }
    }

    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 0);
                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 0);
            }
        } else {
            mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, null /* Looper */);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();
    }

    private void stopLocationUpdates() {
        mFusedLocationClient.removeLocationUpdates(mLocationCallback);
    }

    protected void createLocationRequest() {
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (0 < grantResults.length && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, null /* Looper */);
        }
    }

    private void sendPostRequest(String url, LocationData data) throws JSONException {
        Log.w("MTA", "POST:" + url);
        String json = gson.toJson(data);
        JSONObject jsonObject = new JSONObject(json);


        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.POST, url, jsonObject, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        Type type = new TypeToken<Res<Void>>() {}.getType();
                        Res<Void> res = gson.fromJson(response.toString(), type);
                        messageQueue.putMessage(1, res.getMessage());
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        messageQueue.putMessage(1, error.getMessage());

                    }
                });

        // Access the RequestQueue through your singleton class.
        HttpQueue.getInstance(this).addToRequestQueue(jsonObjectRequest);
    }

    private void sendGetRequest(String url) {

        Log.w("MTA", "GET:" + url);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        Type type = new TypeToken<Res<List<Train>>>() {}.getType();
                        Res<List<Train>> res = gson.fromJson(response.toString(), type);
                        messageQueue.putMessage(2, res.getData().toString());
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        messageQueue.putMessage(0, error.getMessage());

                    }
                });

        // Access the RequestQueue through your singleton class.
        HttpQueue.getInstance(this).addToRequestQueue(jsonObjectRequest);
    }


    private void runUpdateTask() {
        final Handler handler = new Handler();
        final int delay = 5000; //milliseconds
        final String url = SITE + "api/public/trains";

        handler.postDelayed(new Runnable(){
            public void run(){
                //do something
                sendGetRequest(url);
                updateUI();
                Log.i("MTA", "5s schedule");
                handler.postDelayed(this, delay);
            }
        }, delay);
    }

    private void updateUI() {
        messageQueue = MessageQueue.getInstance();
        List<MessageQueue.Message> messages = messageQueue.getMessages();
        StringBuilder stringBuilder = new StringBuilder();
        int max = 1000;
        for (MessageQueue.Message message : (messages.size() < max ? messages : messages.subList(messages.size() -max, messages.size()))) {
            stringBuilder.append(message.getValue()).append("\n");
        }
        logView.setText(stringBuilder.toString());
    }
}
