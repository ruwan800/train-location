package com.mta.location.util;

import android.content.Context;

import com.google.gson.GsonBuilder;

import java.util.Date;

public class Util {

    public static GsonBuilder getGsonBuilder() {
        GsonBuilder gsonBuilder = new GsonBuilder();
        //gsonBuilder.setDateFormat("M/d/yy hh:mm a");
        return gsonBuilder;
    }

    public static int getUserId(Context context) {
        int userId = Cache.getInt(context, Cache.USER_ID, 0);
        if(userId == 0) {
            userId = (int) new Date().getTime();
            Cache.put(context, Cache.USER_ID, userId);
        }
        return userId;
    }
}
