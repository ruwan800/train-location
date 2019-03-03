package com.mta.location.util;

import android.content.Context;
import android.util.Log;

import com.android.volley.VolleyError;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mta.location.Objects.Res;
import com.mta.location.Objects.Station;

import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.List;

import static com.mta.location.util.Config.TAG;

public class StationManager {

    private static StationManager instance;
    private List<Station> stations;

    public static StationManager getInstance() {
        if(instance == null) {
            instance = new StationManager();
        }
        return instance;
    }

    public void getStations(Context context, final ResponseHandler<List<Station>> responseHandler) {

        if(stations != null) {
            responseHandler.onResponse(stations);
        } else {
            final String url = Config.SITE + "api/public/stations";
            Http.sendGetRequest(context, url, new Http.ResponseHandler() {

                @Override
                public void onSuccess(JSONObject response) {
                    Gson gson = Util.getGsonBuilder().create();
                    Type type = new TypeToken<Res<List<Station>>>() {
                    }.getType();
                    Res<List<Station>> res = gson.fromJson(response.toString(), type);
                    stations = res.getData();
                    responseHandler.onResponse(stations);
                }

                @Override
                public void onError(VolleyError error) {
                    Log.e(TAG, "Response Error", error);
                }
            });
        }
    }
}
