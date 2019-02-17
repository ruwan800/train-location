package com.mta.location.main;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.mta.location.main.Objects.Res;
import com.mta.location.main.Objects.Train;
import com.mta.location.main.util.Config;
import com.mta.location.main.util.Http;
import com.mta.location.main.util.MessageQueue;

import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.List;

public class LoggingActivity extends AppCompatActivity {

    private TextView logView;
    private MessageQueue messageQueue;
    private Gson gson;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logging);
        logView = findViewById(R.id.logView);
        messageQueue = MessageQueue.getInstance();


        GsonBuilder gsonBuilder = new GsonBuilder();
        //gsonBuilder.setDateFormat("M/d/yy hh:mm a");
        gson = gsonBuilder.create();
        Location.getInstance(this).setLogging(true);
    }

    @Override
    protected void onResume() {
        super.onResume();
        runUpdateTask();
        //start map update task
    }

    @Override
    protected void onPause() {
        super.onPause();
        //stop map update task
    }


    private void runUpdateTask() {
        final Handler handler = new Handler();
        final int delay = 5000; //milliseconds
        final String url = Config.SITE + "api/public/trains";

        handler.postDelayed(new Runnable(){
            public void run(){
                //do something
                Http.sendGetRequest(getBaseContext(), url, new Http.ResponseHandler() {
                    @Override
                    public void onSuccess(JSONObject response) {
                        Type type = new TypeToken<Res<List<Train>>>() {}.getType();
                        Res<List<Train>> res = gson.fromJson(response.toString(), type);
                        messageQueue.putMessage(2, res.getData().toString());
                    }

                    @Override
                    public void onError(VolleyError error) {
                        messageQueue.putMessage(0, error.getMessage());
                    }
                });
                updateUI();
                Log.i("MTA", "5s schedule");
                handler.postDelayed(this, delay);
            }
        }, delay);
    }

    private void updateUI() {
        messageQueue = MessageQueue.getInstance();
        List<MessageQueue.Message> messages = messageQueue.getMessages();
        StringBuilder stringBuilder = new StringBuilder();
        int max = 1000;
        for (MessageQueue.Message message : (messages.size() < max ? messages : messages.subList(0 ,max))) {
            stringBuilder.append(message.getValue()).append("\n");
        }
        logView.setText(stringBuilder.toString());
    }

}
