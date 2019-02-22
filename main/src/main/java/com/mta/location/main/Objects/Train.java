package com.mta.location.main.Objects;

import com.google.gson.annotations.SerializedName;

public class Train {

    @SerializedName("_id")
    private double id;

    @SerializedName("line_id")
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

    public Train(double id, int lineId, double position, double velocity) {
        this.id = id;
        this.lineId = lineId;
        this.position = position;
        this.velocity = velocity;
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
