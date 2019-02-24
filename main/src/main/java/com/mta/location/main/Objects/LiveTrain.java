package com.mta.location.main.Objects;

import com.google.gson.annotations.SerializedName;

public class LiveTrain {

    @SerializedName("_id")
    private final double id;

    @SerializedName("line_id")
    private final int lineId;

    private final double position;

    private final double velocity;

    private final long timestamp;

    private final String name;


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

    public String getName() {
        return name;
    }

    public LiveTrain(double id, int lineId, double position, double velocity, long timestamp) {
        this(id, lineId, position, velocity, timestamp, "Unnamed Train");
    }

    public LiveTrain(double id, int lineId, double position, double velocity, long timestamp, String name) {
        this.id = id;
        this.lineId = lineId;
        this.position = position;
        this.velocity = velocity;
        this.timestamp = timestamp;
        this.name = name;
    }

    @Override
    public String toString() {
        return "TRAIN::"+name+" {id=" + (int)id +
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
        LiveTrain liveTrain = (LiveTrain) o;
        return Double.compare(liveTrain.id, id) == 0;
    }

    @Override
    public int hashCode() {
        return Double.valueOf(id).hashCode();
    }

    private double round(double val) {
        return Math.round(val * 100.0) / 100.0;
    }
}
