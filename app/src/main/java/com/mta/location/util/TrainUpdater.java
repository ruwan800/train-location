package com.mta.location.util;

import android.content.Context;
import android.util.Log;

import com.android.volley.VolleyError;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mta.location.Objects.LiveTrain;
import com.mta.location.Objects.Res;

import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.List;

import static com.mta.location.util.Config.TAG;

public class TrainUpdater implements TrainUpdatable {

    private static final String TRAIN_URL = Config.SITE + "api/public/train/live-trains";
    private Gson gson;

    public TrainUpdater() {
        gson = Util.getGsonBuilder().create();
    }

    @Override
    public void onUpdate(final Context context, final ResponseHandler<List<LiveTrain>> handler) {

        Http.sendGetRequest(context, TRAIN_URL, new Http.ResponseHandler() {
            @Override
            public void onSuccess(JSONObject response) {
                Type type = new TypeToken<Res<List<LiveTrain>>>() {}.getType();
                Res<List<LiveTrain>> res = gson.fromJson(response.toString(), type);
                handler.onResponse(res.getData());
            }

            @Override
            public void onError(VolleyError error) {
                Log.e(TAG, "Response Error", error);
            }
        });
    }
}
