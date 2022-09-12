package com.jug.util;

import static java.util.Objects.isNull;

public class Timer {
    Long startTime;
    Long stopTime;

    public void start() {
        startTime = System.currentTimeMillis();
    }

    public void stop() {
        stopTime = System.currentTimeMillis();
    }

    public double getExecutionTimeInSeconds() {
        if (isNull(startTime)) {
            throw new RuntimeException("startTime is null");
        }
        if (isNull(stopTime)) {
            throw new RuntimeException("endTime is null");
        }
        return (startTime - stopTime) / 1000.0;
    }
}
