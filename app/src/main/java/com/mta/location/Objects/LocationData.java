package com.mta.location.Objects;

import android.location.Location;

import com.google.gson.annotations.SerializedName;

public class LocationData {

    @SerializedName("user_id")
    private double userId;
    private float accuracy;
    private double altitude;

    @SerializedName("lat")
    private double latitude;

    @SerializedName("lon")
    private double longitude;
    private float speed;
    private float speedAccuracy;
    private double timestamp;

    public LocationData(Location location, double userId) {
        this.userId = userId;
        this.accuracy = location.getAccuracy();
        this.altitude = location.getAltitude();
        this.latitude = location.getLatitude();
        this.longitude = location.getLongitude();
        this.speed = location.getSpeed();
        this.speedAccuracy = location.getAccuracy();
        this.timestamp = location.getTime();
    }

    public double getUserId() {
        return userId;
    }

    public float getAccuracy() {
        return accuracy;
    }

    public double getAltitude() {
        return altitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public float getSpeed() {
        return speed;
    }

    public float getSpeedAccuracy() {
        return speedAccuracy;
    }

    public double getTimestamp() {
        return timestamp;
    }
}
