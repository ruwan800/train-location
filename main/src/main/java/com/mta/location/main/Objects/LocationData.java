package com.mta.location.main.Objects;

import android.location.Location;

public class LocationData {
    private float accuracy;
    private double altitude;
    private double latitude;
    private double longitude;
    private float speed;
    private float speedAccuracy;
    //TODO add date

    public LocationData(Location location) {
        this.accuracy = location.getAccuracy();
        this.altitude = location.getAltitude();
        this.latitude = location.getLatitude();
        this.longitude = location.getLongitude();
        this.speed = location.getSpeed();
        this.speedAccuracy = location.getAccuracy();
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
}
