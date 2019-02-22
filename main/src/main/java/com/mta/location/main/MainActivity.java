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
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.Switch;

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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static android.view.View.inflate;
import static com.mta.location.main.util.Config.TAG;

public class MainActivity extends AppCompatActivity {

    private Gson gson;
    private DrawerLayout mDrawerLayout;
    private ConstraintLayout mainContainerCL;
    private int lineId = 3;
    private List<Station> stations = new ArrayList<>();
    private List<Train> trains = new ArrayList<>();

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
                    MainActivity.this.stations = stations;
                    updateUiOnStations();
                }
            }

            @Override
            public void onError(VolleyError error) {
                Log.e(TAG, error.getMessage());
            }
        });

        startLocationUpdates();

        Spinner spinner = (Spinner) findViewById(R.id.lines_spinner);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.line_array, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);
        spinner.setSelection(getLine() - 1);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                lineId = i +1;
                setLine(i +1);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                    updateUiOnStations();
                    scrollToTop();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });


        Switch trackSwitch = findViewById(R.id.trackSwitch);
        trackSwitch.setChecked(true);
        trackSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b) {
                    startLocationUpdates();
                } else {
                    Location.getInstance(MainActivity.this).stopLocationUpdates(MainActivity.this);
                }
            }
        });
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
        final int delay = 10000; //milliseconds
        final String url = Config.SITE + "api/public/trains";

        handler.postDelayed(new Runnable(){
            public void run(){
                //do something
                Http.sendGetRequest(MainActivity.this, url, new Http.ResponseHandler() {
                    @Override
                    public void onSuccess(JSONObject response) {
                        Type type = new TypeToken<Res<List<Train>>>() {}.getType();
                        Res<List<Train>> res = gson.fromJson(response.toString(), type);
                        trains = res.getData();
                    }

                    @Override
                    public void onError(VolleyError error) {
                        Log.e(TAG, error.getMessage());
                    }
                });
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                    updateUI();
                }
                Log.i("MTA", "5s schedule");
                handler.postDelayed(this, delay);
            }
        }, delay);
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    private void updateUI() {
        mainContainerCL.removeViews(1, mainContainerCL.getChildCount() - 1);
        updateUiOnStations();
        int childCount = mainContainerCL.getChildCount();
//        trains.clear();
//        trains.add(new Train(1, 3, 20, 30));
//        trains.add(new Train(1, 3, 10, -30));
//        trains.add(new Train(1, 3, 30, -10));
//        trains.add(new Train(1, 3, 40, 20));
//        trains.add(new Train(1, 3, 50, 40));
        for (Train train : trains) {
            if(train.getLineId() != lineId) {
                continue;
            }
            Log.i(TAG, train.toString());

            ImageView imageView = new ImageView(this);
            if(4 < train.getVelocity()) {
                imageView.setImageResource(R.drawable.train_down);
            } else if(train.getVelocity() < -4) {
                imageView.setImageResource(R.drawable.train_up);
            } else {
                imageView.setImageResource(R.drawable.train);
            }
            imageView.setId(View.generateViewId());
            //ConstraintLayout.LayoutParams lp = new ConstraintLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT);
            //lp.topToTop = (int) train.getPosition();
            //lp.leftToLeft = 0;
            //mainContainerCL.addView(view, lp);


            ConstraintSet set = new ConstraintSet();

            //ImageView view = new ImageView(this);
            mainContainerCL.addView(imageView, childCount);
            set.clone(mainContainerCL);
            int margin = 100 * ((int) train.getPosition());
            set.connect(imageView.getId(), ConstraintSet.TOP, mainContainerCL.getId(), ConstraintSet.TOP, margin);
            set.applyTo(mainContainerCL);
        }
    }



    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    private void updateUiOnStations() {
        mainContainerCL.removeViews(1, mainContainerCL.getChildCount() - 1);
        ImageView tv = findViewById(R.id.trackIV);
        int max = 0;
        for (Station station : stations) {
            if(station.getLineId() != lineId) {
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

    private void scrollToTop() {
        final ScrollView scrollView = findViewById(R.id.scrollView);
        scrollView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void onGlobalLayout() {
                scrollView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                scrollView.fullScroll(View.FOCUS_UP);
            }
        });
    }

    private void setLine(int lineId) {
        Cache.put(this, Cache.LINE_ID, lineId);
    }

    private int getLine() {
        return Cache.getInt(this, Cache.LINE_ID, 3);
    }
}
