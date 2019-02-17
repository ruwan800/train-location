package com.mta.location.main.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;

import java.util.Set;

import static android.content.Context.MODE_PRIVATE;

public class Cache {

    private static final String LOCATION_LAT = "_LOCATION_LAT";
    private static final String LOCATION_LON = "_LOCATION_LON";
    private static final String LOCATION_PROVIDER = "_LOCATION_PROVIDER";
    public static final String USER_ID = "USER_ID";

    public static void put(Context context, String key, String value) {
        SharedPreferences.Editor editor = getEditor(context, key);
        editor.putString(key, value);
        editor.apply();
    }

    public static void put(Context context, String key, int value) {
        SharedPreferences.Editor editor = getEditor(context, key);
        editor.putInt(key, value);
        editor.apply();
    }

    public static void put(Context context, String key, boolean value) {
        SharedPreferences.Editor editor = getEditor(context, key);
        editor.putBoolean(key, value);
        editor.apply();
    }

    public static void put(Context context, String key, float value) {
        SharedPreferences.Editor editor = getEditor(context, key);
        editor.putFloat(key, value);
        editor.apply();
    }

    public static void put(Context context, String key, Long value) {
        SharedPreferences.Editor editor = getEditor(context, key);
        editor.putLong(key, value);
        editor.apply();
    }

    public static void put(Context context, String key, Set<String> value) {
        SharedPreferences.Editor editor = getEditor(context, key);
        editor.putStringSet(key, value);
        editor.apply();
    }

    public static void put(Context context, String key, Location location) {
        SharedPreferences.Editor editor = getEditor(context, key);
        if (location == null) {
            editor.remove(key + LOCATION_LAT);
            editor.remove(key + LOCATION_LON);
            editor.remove(key + LOCATION_PROVIDER);
        } else {
            editor.putString(key + LOCATION_LAT, String.valueOf(location.getLatitude()));
            editor.putString(key + LOCATION_LON, String.valueOf(location.getLongitude()));
            editor.putString(key + LOCATION_PROVIDER, location.getProvider());
        }
        editor.apply();
    }

    public static String getString(Context context, String key, String defVal) {
        SharedPreferences prefs = context.getSharedPreferences(key, MODE_PRIVATE);
        return prefs.getString(key, defVal);
    }

    public static int getInt(Context context, String key, int defVal) {
        SharedPreferences prefs = context.getSharedPreferences(key, MODE_PRIVATE);
        return prefs.getInt(key, defVal);
    }

    public static Location getLocation(Context context, String key) {
        SharedPreferences prefs = context.getSharedPreferences(key, MODE_PRIVATE);
        String lat = prefs.getString(key + LOCATION_LAT, null);
        String lon = prefs.getString(key + LOCATION_LON, null);
        Location location = null;
        if (lat != null && lon != null) {
            String provider = prefs.getString(key + LOCATION_PROVIDER, null);
            location = new Location(provider);
            location.setLatitude(Double.parseDouble(lat));
            location.setLongitude(Double.parseDouble(lon));
        }
        return location;
    }

    private static SharedPreferences.Editor getEditor(Context context, String key) {
        return context.getSharedPreferences(key, MODE_PRIVATE).edit();
    }
}