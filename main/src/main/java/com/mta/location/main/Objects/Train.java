package com.mta.location.main.Objects;

import com.google.gson.annotations.SerializedName;

public class Train {

    @SerializedName("_id")
    private final double id;

    @SerializedName("line_id")
    private final int lineId;

    private final double position;

    private final double velocity;

    private final long timestamp;


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

    public long getTimestamp() {
        return timestamp;
    }

    public Train(double id, int lineId, double position, double velocity, long timestamp) {
        this.id = id;
        this.lineId = lineId;
        this.position = position;
        this.velocity = velocity;
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "TRAIN::{id=" + (int)id +
                ", line=" + lineId +
                ", pos=" + round(position)+
                ", v=" + round(velocity) +
                ", t=" + timestamp +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Train train = (Train) o;
        return Double.compare(train.id, id) == 0;
    }

    @Override
    public int hashCode() {
        return Double.valueOf(id).hashCode();
    }

    private double round(double val) {
        return Math.round(val * 100.0) / 100.0;
    }
}
