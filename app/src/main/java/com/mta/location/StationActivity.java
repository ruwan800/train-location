package com.mta.location;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class StationActivity extends AppCompatActivity {

    public static final String STATION_ID = "station_id";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_station);
    }
}
