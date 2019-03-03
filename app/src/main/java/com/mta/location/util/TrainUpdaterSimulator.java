package com.mta.location.util;

import android.content.Context;

import com.mta.location.Objects.LiveTrain;

import java.util.Date;
import java.util.ArrayList;
import java.util.List;

public class TrainUpdaterSimulator implements TrainUpdatable {

    private List<LiveTrain> trains;

    public TrainUpdaterSimulator() {
        trains = new ArrayList<>();
        long time = new Date().getTime();
        LiveTrain t1 = new LiveTrain(-1, 3, 12, 0, time);
        LiveTrain t2 = new LiveTrain(-2, 3, 4, 12, time);
        LiveTrain t3 = new LiveTrain(-3, 3, 20, -20, time);

        trains.add(t1);
        trains.add(t2);
        trains.add(t3);
    }

    @Override
    public void onUpdate(final Context context, final ResponseHandler<List<LiveTrain>> handler) {

        handler.onResponse(trains);
    }
}
