package com.mta.location.main;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.transition.ChangeBounds;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.LinearInterpolator;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.mta.location.main.Objects.LiveTrain;
import com.mta.location.main.Objects.Res;
import com.mta.location.main.Objects.Response.UserPoint;
import com.mta.location.main.Objects.Station;
import com.mta.location.main.util.Cache;
import com.mta.location.main.util.Config;
import com.mta.location.main.util.Http;
import com.mta.location.main.util.ViewIdGenerator;

import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.view.View.inflate;
import static com.mta.location.main.StationActivity.STATION_ID;
import static com.mta.location.main.TrainActivity.TRAIN_ID;
import static com.mta.location.main.util.Config.TAG;

public class MainActivity extends AppCompatActivity {

    private static final String TRAINS_LAST_UPDATED = "trains_last_updated";
    private static final String TRAIN_URL = Config.SITE + "api/public/train/live-trains";

    private Gson gson;
    private DrawerLayout mDrawerLayout;
    private ConstraintLayout mainContainerCL;
    private ImageView trackIV;
    private int lineId = 3;
    private List<Station> stations = new ArrayList<>();
    private List<LiveTrain> liveTrains = new ArrayList<>();
    private Map<LiveTrain, View> trainViewMap = new HashMap<>();
    private View userPointView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mDrawerLayout = findViewById(R.id.drawer_layout);
        mainContainerCL = findViewById(R.id.mainContainerCL);
        trackIV = findViewById(R.id.trackIV);

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
                    removeAllViews();
                    updateLineViews();
                }
            }

            @Override
            public void onError(VolleyError error) {
                Log.e(TAG, "Response Error", error);
            }
        });

        startLocationUpdates();

        Spinner spinner = findViewById(R.id.lines_spinner);
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
                    updateLineViews();
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
        final int delay = 1000; //milliseconds

        handler.postDelayed(new Runnable(){
            public void run(){
                final long time = new Date().getTime();
                long prevTime = Cache.getLong(getBaseContext(), TRAINS_LAST_UPDATED, 0);
                if(1000*5 <= time - prevTime) {
                    Log.i("MTA", "5s schedule");
                    Http.sendGetRequest(MainActivity.this, TRAIN_URL, new Http.ResponseHandler() {
                        @Override
                        public void onSuccess(JSONObject response) {
                            Type type = new TypeToken<Res<List<LiveTrain>>>() {}.getType();
                            Res<List<LiveTrain>> res = gson.fromJson(response.toString(), type);
                            liveTrains = res.getData();
                            Cache.put(MainActivity.this, TRAINS_LAST_UPDATED, time);
                        }

                        @Override
                        public void onError(VolleyError error) {
                            Log.e(TAG, "Response Error", error);
                        }
                    });
                }
                updateTrainViews(time);
                updateUserView();
                handler.postDelayed(this, delay);
            }
        }, delay);
    }

    private void updateLineViews() {
        removeAllViews();
        updateStationViews();

    }

    @SuppressLint("SetTextI18n")
    private void updateTrainViews(long time) {
        List<LiveTrain> trainsByLine = new ArrayList<>();
        for (LiveTrain liveTrain : liveTrains) {
            if(liveTrain.getLineId() == lineId) {
                trainsByLine.add(liveTrain);
            }
        }
//        Sample liveTrains
        LiveTrain t1 = new LiveTrain(1, 3, 12, 0, time);
        LiveTrain t2 = new LiveTrain(2, 3, 18, 12, time);
        LiveTrain t3 = new LiveTrain(3, 3, 24, -20, time);

        trainsByLine.add(t1);
        trainsByLine.add(t2);
        trainsByLine.add(t3);


        for (Map.Entry<LiveTrain, View> entry : trainViewMap.entrySet()) {
            if(!trainsByLine.contains(entry.getKey())) {
                mainContainerCL.removeView(entry.getValue());
                trainViewMap.remove(entry.getKey());
            }
        }

        for (final LiveTrain liveTrain : trainsByLine) {
            Log.i(TAG, liveTrain.toString());

            View view = trainViewMap.get(liveTrain);
            if(view == null) {
                int childCount = mainContainerCL.getChildCount();
                view = inflate(this, R.layout.train, null);
                view.setId(ViewIdGenerator.generateViewId());
                mainContainerCL.addView(view, childCount);
                trainViewMap.put(liveTrain, view);
                Button button = view.findViewById(R.id.train_name);
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(getBaseContext(), TrainActivity.class);
                        intent.putExtra(TRAIN_ID, liveTrain.getId());
                        startActivity(intent);
                    }
                });
            }
            ImageView trainIv = view.findViewById(R.id.train);
            ConstraintLayout boxCl = view.findViewById(R.id.box);
            @SuppressLint("CutPasteId")
            Button trainNameBtn = view.findViewById(R.id.train_name);
            TextView trainSpeedTv = view.findViewById(R.id.train_speed);

            if(4 < liveTrain.getVelocity()) {
                trainIv.setImageResource(R.drawable.train_down);
                boxCl.setBackgroundResource(R.drawable.train_bubble_down);
                int color = getResources().getColor(R.color.trainDown);
                trainNameBtn.setTextColor(color);
                trainSpeedTv.setTextColor(color);
            } else if(-4 <= liveTrain.getVelocity()) {
                trainIv.setImageResource(R.drawable.train);
                boxCl.setBackgroundResource(R.drawable.train_bubble);
                int color = getResources().getColor(R.color.train);
                trainNameBtn.setTextColor(color);
                trainSpeedTv.setTextColor(color);
            } else {
                trainIv.setImageResource(R.drawable.train_up);
                boxCl.setBackgroundResource(R.drawable.train_bubble_up);
                int color = getResources().getColor(R.color.trainUp);
                trainNameBtn.setTextColor(color);
                trainSpeedTv.setTextColor(color);
            }

            trainNameBtn.setText(liveTrain.getName() != null ? liveTrain.getName() : "Unnamed Train");
            trainSpeedTv.setText((Math.round(Math.abs(liveTrain.getVelocity())*3.6*100)/100) + "km/h");

            ConstraintSet set = new ConstraintSet();
            set.clone(mainContainerCL);
            double extraDistance = liveTrain.getVelocity() / 1000 * ((time - liveTrain.getTimestamp()) / 1000.0);
            int margin = (int) (100 * (liveTrain.getPosition() + extraDistance));
            set.connect(view.getId(), ConstraintSet.TOP, mainContainerCL.getId(), ConstraintSet.TOP, margin);

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
                ChangeBounds transition = new ChangeBounds();
                transition.setInterpolator(new LinearInterpolator());
                transition.setDuration(1000);
                TransitionManager.beginDelayedTransition(mainContainerCL ,transition);
            }
            set.applyTo(mainContainerCL);
        }
    }

    private void updateStationViews() {
        int max = 0;
        for (final Station station : stations) {
            if(station.getLineId() != lineId) {
                continue;
            }
            Log.i(TAG, station.toString());

            View view = inflate(this, R.layout.station, null);
            Button stationNameButton = view.findViewById(R.id.name);
            stationNameButton.setText(station.getName());
            stationNameButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getBaseContext(), StationActivity.class);
                    intent.putExtra(STATION_ID, station.getId());
                    startActivity(intent);
                }
            });

            view.setId(ViewIdGenerator.generateViewId());
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
        trackIV.setMinimumHeight(max);
    }

    private void removeAllViews() {
        trainViewMap.clear();
        mainContainerCL.removeViews(1, mainContainerCL.getChildCount() - 1);
    }

    private void scrollToTop() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            final ScrollView scrollView = findViewById(R.id.scrollView);
            scrollView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    scrollView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    scrollView.fullScroll(View.FOCUS_UP);
                }
            });
        }
    }

    private void updateUserView() {
        UserPoint userPoint = Location.getInstance(this).getUserPoint();
        if(userPoint != null && userPoint.getLineId() == lineId) {
            if(userPointView == null) {
                userPointView = inflate(this, R.layout.user_point, null);
                userPointView.setId(ViewIdGenerator.generateViewId());
                mainContainerCL.addView(userPointView,1);
//                Button stationNameButton = userPointView.findViewById(R.id.name);
//            stationNameButton.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    Intent intent = new Intent(getBaseContext(), StationActivity.class);
//                    intent.putExtra(STATION_ID, station.getId());
//                    startActivity(intent);
//                }
//            });
            }
//            stationNameButton.setText(station.getName());
            ConstraintSet set = new ConstraintSet();
            set.clone(mainContainerCL);
            int margin = 100 * ((int) userPoint.getPosition());
            set.connect(userPointView.getId(), ConstraintSet.TOP, mainContainerCL.getId(), ConstraintSet.TOP, margin);
            set.applyTo(mainContainerCL);
        } else if(userPointView != null) {
            mainContainerCL.removeView(userPointView);
            userPointView = null;
        }
    }

    private void setLine(int lineId) {
        Cache.put(this, Cache.LINE_ID, lineId);
    }

    private int getLine() {
        return Cache.getInt(this, Cache.LINE_ID, 3);
    }
}
