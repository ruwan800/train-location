package com.mta.location;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.support.v4.app.Fragment;
import android.transition.ChangeBounds;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.LinearInterpolator;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

import com.mta.location.Objects.LiveTrain;
import com.mta.location.Objects.Response.UserPoint;
import com.mta.location.Objects.Station;
import com.mta.location.util.Cache;
import com.mta.location.util.ResponseHandler;
import com.mta.location.util.StationManager;
import com.mta.location.util.TrainUpdatable;
import com.mta.location.util.TrainUpdater;
import com.mta.location.util.ViewIdGenerator;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static android.view.View.inflate;
import static com.mta.location.StationActivity.STATION_ID;
import static com.mta.location.TrainActivity.TRAIN_ID;
import static com.mta.location.util.Config.TAG;

public class MapFragment extends Fragment {

    private static final String TRAINS_LAST_UPDATED = "trains_last_updated";

    private ConstraintLayout mapCl;
    private Spinner spinner;
    private ScrollView scrollView;
    private ImageView trackIv;

    private int lineId = 3;
    private List<Station> stations = new ArrayList<>();
    private List<LiveTrain> liveTrains = new ArrayList<>();
    private Map<LiveTrain, View> trainViewMap = new HashMap<>();
    private View userPointView;
    private TrainUpdatable trainUpdater;

    public MapFragment() {
        // Required empty public constructor
    }

    public void setTrainUpdater(TrainUpdatable trainUpdater) {
        this.trainUpdater = trainUpdater;
    }

    public int getLineId() {
        return lineId;
    }

    public void setLineId(int lineId) {
        this.lineId = lineId;
        updateStationViews();
        spinner.setSelection(lineId - 1);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        final View layout = inflater.inflate(R.layout.fragment_map, container, false);
        spinner = layout.findViewById(R.id.lines_spinner);
        scrollView = layout.findViewById(R.id.scrollView);
        mapCl = layout.findViewById(R.id.mainContainerCL);
        trackIv = layout.findViewById(R.id.trackIV);
        init(getContext());
        return layout;
    }

    private void init(final Context context) {
        trainUpdater = new TrainUpdater();

        StationManager.getInstance().getStations(context, new ResponseHandler<List<Station>>() {
            @Override
            public void onResponse(List<Station> stations) {
                MapFragment.this.stations = stations;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                    updateStationViews();
                }
            }
        });

        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(context,
                R.array.line_array, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);
        spinner.setSelection(getLine(context) - 1);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                lineId = i + 1;
                setLine(context, i + 1);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                    updateStationViews();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        runUpdateTask(getContext());
        Location.getInstance(getContext()).setLogging(false);
        //start map update task
    }

    @Override
    public void onPause() {
        super.onPause();
        //stop map update task
    }

    private void runUpdateTask(final Context context) {
        final Handler handler = new Handler();
        final int delay = 1000; //milliseconds

        handler.postDelayed(new Runnable() {
            public void run() {
                final long time = new Date().getTime();
                long prevTime = Cache.getLong(context, TRAINS_LAST_UPDATED, 0);
                if (1000 * 5 <= time - prevTime) {
                    Log.i("MTA", "5s schedule");
                    trainUpdater.onUpdate(context, new ResponseHandler<List<LiveTrain>>() {
                        @Override
                        public void onResponse(List<LiveTrain> data) {
                            liveTrains = data;
                            Cache.put(context, TRAINS_LAST_UPDATED, time);
                        }
                    });
                }
                updateTrainViews(time);
                updateUserView();
                handler.postDelayed(this, delay);
            }
        }, delay);
    }

    @SuppressLint("SetTextI18n")
    private void updateTrainViews(long time) {
        List<LiveTrain> trainsByLine = new ArrayList<>();
        for (LiveTrain liveTrain : liveTrains) {
            if (liveTrain.getLineId() == lineId) {
                trainsByLine.add(liveTrain);
            }
        }

        for(Iterator<Map.Entry<LiveTrain, View>> it = trainViewMap.entrySet().iterator(); it.hasNext();) {
            Map.Entry<LiveTrain, View> entry = it.next();
            if (!trainsByLine.contains(entry.getKey())) {
                mapCl.removeView(entry.getValue());
                it.remove();
            }
        }

        for (final LiveTrain liveTrain : trainsByLine) {
            Log.i(TAG, liveTrain.toString());

            View view = trainViewMap.get(liveTrain);
            if (view == null) {
                int childCount = mapCl.getChildCount();
                view = inflate(getContext(), R.layout.train, null);
                view.setId(ViewIdGenerator.generateViewId());
                mapCl.addView(view, childCount);
                trainViewMap.put(liveTrain, view);
                Button button = view.findViewById(R.id.train_name);
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(getContext(), TrainActivity.class);
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

            if (4 < liveTrain.getVelocity()) {
                trainIv.setImageResource(R.drawable.train_down);
                boxCl.setBackgroundResource(R.drawable.train_bubble_down);
                int color = getResources().getColor(R.color.trainDown);
                trainNameBtn.setTextColor(color);
                trainSpeedTv.setTextColor(color);
            } else if (-4 <= liveTrain.getVelocity()) {
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
            trainSpeedTv.setText((Math.round(Math.abs(liveTrain.getVelocity()) * 3.6 * 100) / 100) + "km/h");

            ConstraintSet set = new ConstraintSet();
            set.clone(mapCl);
            double extraDistance = liveTrain.getVelocity() / 1000 * ((time - liveTrain.getTimestamp()) / 1000.0);
            int margin = (int) (100 * (liveTrain.getPosition() + extraDistance));
            set.connect(view.getId(), ConstraintSet.TOP, mapCl.getId(), ConstraintSet.TOP, margin);

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
                ChangeBounds transition = new ChangeBounds();
                transition.setInterpolator(new LinearInterpolator());
                transition.setDuration(1000);
                TransitionManager.beginDelayedTransition(mapCl, transition);
            }
            set.applyTo(mapCl);
        }
    }

    private void updateStationViews() {
        removeAllViews();
        int max = 0;
        for (final Station station : stations) {
            if (station.getLineId() != lineId) {
                continue;
            }
            Log.i(TAG, station.toString());

            View view = inflate(getContext(), R.layout.station, null);
            Button stationNameButton = view.findViewById(R.id.name);
            stationNameButton.setText(station.getName());
            stationNameButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getContext(), StationActivity.class);
                    intent.putExtra(STATION_ID, station.getId());
                    startActivity(intent);
                }
            });

            view.setId(ViewIdGenerator.generateViewId());
            //ConstraintLayout.LayoutParams lp = new ConstraintLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT);
            //lp.topToTop = (int) station.getPosition();
            //lp.leftToLeft = 0;
            //mapCl.addView(view, lp);


            ConstraintSet set = new ConstraintSet();

            //ImageView view = new ImageView(this);
            mapCl.addView(view, 1);
            set.clone(mapCl);
            int margin = 100 * ((int) station.getPosition());
            if (max < margin) {
                max = margin;
            }
            set.connect(view.getId(), ConstraintSet.TOP, mapCl.getId(), ConstraintSet.TOP, margin);
            set.applyTo(mapCl);
        }
        trackIv.setMinimumHeight(max);
        scrollToTop();
    }

    private void removeAllViews() {
        trainViewMap.clear();
        mapCl.removeViews(1, mapCl.getChildCount() - 1);
    }

    private void scrollToTop() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
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
        UserPoint userPoint = Location.getInstance(getContext()).getUserPoint();
        if (userPoint != null && userPoint.getLineId() == lineId) {
            if (userPointView == null) {
                userPointView = inflate(getContext(), R.layout.user_point, null);
                userPointView.setId(ViewIdGenerator.generateViewId());
                mapCl.addView(userPointView, 1);
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
            set.clone(mapCl);
            int margin = 100 * ((int) userPoint.getPosition());
            set.connect(userPointView.getId(), ConstraintSet.TOP, mapCl.getId(), ConstraintSet.TOP, margin);
            set.applyTo(mapCl);
        } else if (userPointView != null) {
            mapCl.removeView(userPointView);
            userPointView = null;
        }
    }

    private int getLine(Context context) {
        return Cache.getInt(context, Cache.LINE_ID, 3);
    }

    private void setLine(Context context, int lineId) {
        Cache.put(context, Cache.LINE_ID, lineId);
    }

}
