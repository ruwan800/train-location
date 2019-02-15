package com.mta.location.main.Objects;

import com.google.gson.annotations.SerializedName;

public class Train {

    @SerializedName("id")
    private double id;

    @SerializedName("lineId")
    private int lineId;

    private double position;

    private double velocity;


    public double getId() {
        return id;
    }

    public int getLineId() {
        return lineId;
    }

    public double getPosition() {
        return position;
    }

    public double getVelocity() {
        return velocity;
    }

    @Override
    public String toString() {
        return "TRAIN::{id=" + (int)id +
                ", line=" + lineId +
                ", pos=" + round(position)+
                ", v=" + round(velocity) +
                '}';
    }

    private double round(double val) {
        return Math.round(val * 100.0) / 100.0;
    }
}
