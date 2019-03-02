package com.mta.location.Objects;

import com.google.gson.annotations.SerializedName;

public class Station {

    @SerializedName("_id")
    private double id;

    @SerializedName("line_id")
    private int lineId;

    private double position;

    private String name;


    public double getId() {
        return id;
    }

    public int getLineId() {
        return lineId;
    }

    public double getPosition() {
        return position;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return lineId + ":" + name +
                ", pos=" + round(position);
    }

    private double round(double val) {
        return Math.round(val * 100.0) / 100.0;
    }
}
