package com.mta.location.util;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

public class HttpQueue {

    private static HttpQueue mInstance;
    private RequestQueue mHttpQueue;

    private HttpQueue(Context context) {
        mHttpQueue = getRequestQueue(context);
    }

    public static synchronized HttpQueue getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new HttpQueue(context);
        }
        return mInstance;
    }

    private RequestQueue getRequestQueue(Context context) {
        if (mHttpQueue == null) {
            // getApplicationContext() is key, it keeps you from leaking the
            // Activity or BroadcastReceiver if someone passes one in.
            mHttpQueue = Volley.newRequestQueue(context.getApplicationContext());
        }
        return mHttpQueue;
    }

    public <T> void addToRequestQueue(Request<T> req) {
        mHttpQueue.add(req);
    }
}