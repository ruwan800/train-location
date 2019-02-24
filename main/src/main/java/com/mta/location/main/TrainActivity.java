package com.mta.location.main;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.mta.location.main.Objects.Res;
import com.mta.location.main.Objects.Request.TrainUpdate;
import com.mta.location.main.util.Config;
import com.mta.location.main.util.Http;

import org.json.JSONObject;

import java.lang.reflect.Type;

import static com.mta.location.main.util.Config.TAG;

public class TrainActivity extends AppCompatActivity {

    private static final String TRAIN_UPDATE_URL = Config.SITE + "api/public/train/update";
    public static final String TRAIN_ID = "train_id";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_train);


        Intent intent = getIntent();
        final double trainId = intent.getDoubleExtra(TRAIN_ID, 0);
        Button submitButton = findViewById(R.id.submitBtn);
        final EditText trainName = findViewById(R.id.trainNameEt);
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = trainName.getText().toString();
                if(!name.isEmpty()) {
                    TrainUpdate train = new TrainUpdate(trainId, name);
                    Http.sendPostRequest(getBaseContext(), TRAIN_UPDATE_URL, train, new Http.ResponseHandler() {
                        @Override
                        public void onSuccess(JSONObject response) {
                            GsonBuilder gsonBuilder = new GsonBuilder();
                            //gsonBuilder.setDateFormat("M/d/yy hh:mm a");
                            Gson gson = gsonBuilder.create();
                            Type type = new TypeToken<Res<Void>>() {}.getType();
                            Res<Void> res = gson.fromJson(response.toString(), type);
                            if(0 < res.getStatus()) {
                                Toast.makeText(getBaseContext(), "Train name saved",
                                        Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(getBaseContext(), "Failed to save name",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onError(VolleyError error) {
                            Log.e(TAG, "Response Error", error);
                            Toast.makeText(getBaseContext(), "Failed to save name",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });


    }

}
