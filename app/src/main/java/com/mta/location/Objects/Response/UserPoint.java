package com.mta.location.Objects.Response;

import com.google.gson.annotations.SerializedName;

public class UserPoint {

    @SerializedName("line_id")
    private final int lineId;

    private final double position;

    @SerializedName("train_id")
    private final double trainId;

    public UserPoint(int lineId, double position, double trainId) {
        this.lineId = lineId;
        this.position = position;
        this.trainId = trainId;
    }

    public int getLineId() {
        return lineId;
    }

    public double getPosition() {
        return position;
    }

    public double getTrainId() {
        return trainId;
    }
}
