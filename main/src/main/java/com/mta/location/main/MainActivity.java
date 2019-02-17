package com.mta.location.main;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.android.volley.VolleyError;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.mta.location.main.Objects.Res;
import com.mta.location.main.Objects.Station;
import com.mta.location.main.Objects.Train;
import com.mta.location.main.util.Cache;
import com.mta.location.main.util.Config;
import com.mta.location.main.util.Http;

import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.Date;
import java.util.List;

import static android.view.View.inflate;
import static com.mta.location.main.util.Config.TAG;

public class MainActivity extends AppCompatActivity {

    private Gson gson;
    private DrawerLayout mDrawerLayout;
    private ConstraintLayout mainContainerCL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mDrawerLayout = findViewById(R.id.drawer_layout);
        mainContainerCL = findViewById(R.id.mainContainerCL);

        GsonBuilder gsonBuilder = new GsonBuilder();
        //gsonBuilder.setDateFormat("M/d/yy hh:mm a");
        gson = gsonBuilder.create();

        int userId = Cache.getInt(this, Cache.USER_ID, 0);
        if(userId == 0) {
            userId = (int) new Date().getTime();
            Cache.put(this, Cache.USER_ID, userId);
        }

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                        // set item as selected to persist highlight
                        menuItem.setChecked(true);
                        // close drawer when item is tapped
                        mDrawerLayout.closeDrawers();

                        // Add code here to update the UI based on the item selected
                        // For example, swap UI fragments here

                        return true;
                    }
                });

        final String url = Config.SITE + "api/public/stations";
        Http.sendGetRequest(this, url, new Http.ResponseHandler() {
            @Override
            public void onSuccess(JSONObject response) {
                Type type = new TypeToken<Res<List<Station>>>() {}.getType();
                Res<List<Station>> res = gson.fromJson(response.toString(), type);
                List<Station> stations = res.getData();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                    updateUiOnStations(stations);
                }
            }

            @Override
            public void onError(VolleyError error) {
                Log.e(TAG, error.getMessage());
            }
        });
        startLocationUpdates();
    }

    @Override
    protected void onResume() {
        super.onResume();
        runUpdateTask();
        Location.getInstance(this).setLogging(false);
        //start map update task
    }

    @Override
    protected void onPause() {
        super.onPause();
        //stop map update task
    }

    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 0);
                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 0);
            }
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                LocationUpdateJob.scheduleJob(this);
            }
        }
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (0 < grantResults.length && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                LocationUpdateJob.scheduleJob(this);
            }
        }
    }


    private void runUpdateTask() {
        final Handler handler = new Handler();
        final int delay = 5000; //milliseconds
        final String url = Config.SITE + "api/public/trains";

        handler.postDelayed(new Runnable(){
            public void run(){
                //do something
                Http.sendGetRequest(MainActivity.this, url, new Http.ResponseHandler() {
                    @Override
                    public void onSuccess(JSONObject response) {
                        Type type = new TypeToken<Res<List<Train>>>() {}.getType();
                        Res<List<Train>> res = gson.fromJson(response.toString(), type);
                    }

                    @Override
                    public void onError(VolleyError error) {
                        Log.e(TAG, error.getMessage());
                    }
                });
                updateUI();
                Log.i("MTA", "5s schedule");
                handler.postDelayed(this, delay);
            }
        }, delay);
    }

    private void updateUI() {
    }



    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    private void updateUiOnStations(List<Station> stations) {
        ImageView tv = findViewById(R.id.trackIV);
        int max = 0;
        for (Station station : stations) {
            if(station.getLineId() != 3) {
                continue;
            }
            Log.i(TAG, station.toString());

            View view = inflate(this, R.layout.station, null);
            Button stationNameButton = view.findViewById(R.id.name);
            stationNameButton.setText(station.getName());
            view.setId(View.generateViewId());
            //ConstraintLayout.LayoutParams lp = new ConstraintLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT);
            //lp.topToTop = (int) station.getPosition();
            //lp.leftToLeft = 0;
            //mainContainerCL.addView(view, lp);


            ConstraintSet set = new ConstraintSet();

            //ImageView view = new ImageView(this);
            mainContainerCL.addView(view,1);
            set.clone(mainContainerCL);
            int margin = 100 * ((int) station.getPosition());
            if(max < margin) {
                max = margin;
            }
            set.connect(view.getId(), ConstraintSet.TOP, mainContainerCL.getId(), ConstraintSet.TOP, margin);
            set.applyTo(mainContainerCL);
        }
        tv.setMinimumHeight(max);
    }
}
