package com.jug.util;

import static java.util.Objects.isNull;

public class Timer {
    Long startTime;
    Long stopTime;
    private boolean isRunning;
    private final boolean isHeadless;
    private final boolean isTrackOnly;

    public Timer(boolean isHeadless, boolean isTrackOnly) {
        this.isHeadless = isHeadless;
        this.isTrackOnly = isTrackOnly;
    }

    public void start() {
        if (!isRunning) {
            isRunning = true;
            startTime = System.currentTimeMillis();
        }
    }

    public void stop() {
        if(isRunning){
            stopTime = System.currentTimeMillis();
            isRunning = false;
        }        
    }

    public double getExecutionTimeInSeconds() {
        if (isNull(startTime)) {
            throw new RuntimeException("startTime is null");
        }
        if (isNull(stopTime)) {
            throw new RuntimeException("endTime is null");
        }
        return (stopTime - startTime) / 1000.0;
    }

    public void printExecutionTime(String prependString) {
        System.out.println(prependString + " (IsTrackOnly: " + isTrackOnly + ", IsHeadless:" + isHeadless + "): " + getExecutionTimeInSeconds());
    }
}
