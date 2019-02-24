package com.mta.location.main;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.android.volley.VolleyError;
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
import com.mta.location.main.Objects.Response.UserPoint;
import com.mta.location.main.util.Cache;
import com.mta.location.main.util.Config;
import com.mta.location.main.util.Http;
import com.mta.location.main.util.MessageQueue;

import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.Date;

public class Location {

    private static Location instance;
    private FusedLocationProviderClient mFusedLocationClient;
    private LocationCallback mLocationCallback;
    private LocationRequest mLocationRequest;
    private static final String PREVIOUS_LOCATION = "previous_location";
    private boolean isLogging = false;
    private boolean isRunning = false;
    private int userId;
    private UserPoint userPoint;

    private Location() {}

    public static Location getInstance(Context context) {
        if(instance == null) {
            instance = new Location();
            instance.init(context);
        }
        return instance;
    }

    private void init(final Context context) {
        userId = Cache.getInt(context, Cache.USER_ID, (int) new Date().getTime());

        GsonBuilder gsonBuilder = new GsonBuilder();
        //gsonBuilder.setDateFormat("M/d/yy hh:mm a");
        final Gson gson = gsonBuilder.create();

        final String updateUrl = Config.SITE + "api/public/location-update";

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                Log.e("MTA","Location result");
                for (android.location.Location location : locationResult.getLocations()) {
                    LocationData locationData = new LocationData(location, userId);
                    Cache.put(context, PREVIOUS_LOCATION, location);
                    Http.sendPostRequest(context, updateUrl, locationData, new Http.ResponseHandler() {
                        @Override
                        public void onSuccess(JSONObject response) {
                            Type type = new TypeToken<Res<UserPoint>>() {}.getType();
                            Res<UserPoint> res = gson.fromJson(response.toString(), type);
                            UserPoint userPoint = res.getData();
                            Location.this.userPoint = (userPoint != null && userPoint.getTrainId() == 0) ? userPoint: null;
                            if(isLogging) {
                                MessageQueue messageQueue = MessageQueue.getInstance();
                                messageQueue.putMessage(1, res.getMessage());
                            }
                            if(0 < res.getStatus()) {
                                setLastUpdated(context, new Date().getTime());
                            }
                        }

                        @Override
                        public void onError(VolleyError error) {
                            if(isLogging) {
                                MessageQueue messageQueue = MessageQueue.getInstance();
                                messageQueue.putMessage(1, error.getMessage());
                            }
                        }
                    });
                    //Toast.makeText(context, location.toString(), Toast.LENGTH_SHORT).show();
                }
            }
        };
        createLocationRequest();
    }

    private void createLocationRequest() {
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    public void startLocationUpdates(Context context) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, null /* Looper */);
            isRunning = true;
        }
    }

    public void stopLocationUpdates(Context context) {
        isRunning = false;
        mFusedLocationClient.removeLocationUpdates(mLocationCallback);
        Cache.put(context, Cache.IS_LOCATION_UPDATING, false);
    }

    public void setLogging(boolean logging) {
        isLogging = logging;
    }

    public boolean isRunning() {
        return isRunning;
    }

    public UserPoint getUserPoint() {
        return userPoint;
    }

    public static boolean isLocationUpdating(Context context) {
        boolean updating = Cache.getBoolean(context, Cache.IS_LOCATION_UPDATING, false);
        long lastUpdated = Cache.getLong(context, Cache.LAST_SUCCESSFUL_UPDATE, 0);
        return (updating && new Date().getTime() - 10*60*1000 < lastUpdated);
    }

    public static void setLastUpdated(Context context, long timestamp) {
        Cache.put(context, Cache.LAST_SUCCESSFUL_UPDATE, timestamp);
    }
}
