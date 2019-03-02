package com.mta.location.util;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONException;
import org.json.JSONObject;

public class Http {


    public static <T> void sendPostRequest(Context context, String url, T data, final ResponseHandler handler) {
        Log.w("MTA", "POST:" + url);


        GsonBuilder gsonBuilder = new GsonBuilder();
        //gsonBuilder.setDateFormat("M/d/yy hh:mm a");
        Gson gson = gsonBuilder.create();
        String json = gson.toJson(data);
        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject(json);
        } catch (JSONException e) {
            Log.e("MTA", "HTTP::POST:" + e.getMessage());
        }
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.POST, url, jsonObject, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        if(handler != null) {
                            handler.onSuccess(response);
                        }
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        handler.onError(error);

                    }
                });

        // Access the RequestQueue through your singleton class.
        HttpQueue.getInstance(context).addToRequestQueue(jsonObjectRequest);
    }

    public static void sendGetRequest(Context context, String url, final ResponseHandler handler) {

        Log.w("MTA", "GET:" + url);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        handler.onSuccess(response);
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        handler.onError(error);
                    }
                });

        // Access the RequestQueue through your singleton class.
        HttpQueue.getInstance(context).addToRequestQueue(jsonObjectRequest);
    }

    public interface ResponseHandler {

        void onSuccess(JSONObject response);
        void onError(VolleyError error);
    }
}
