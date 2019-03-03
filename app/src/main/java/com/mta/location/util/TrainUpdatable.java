package com.mta.location.util;

import android.content.Context;

import com.mta.location.Objects.LiveTrain;

import java.util.List;

public interface TrainUpdatable {

    void onUpdate(Context context, final ResponseHandler<List<LiveTrain>> handler);
}
