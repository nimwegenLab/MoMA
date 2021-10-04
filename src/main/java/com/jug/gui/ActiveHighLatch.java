package com.jug.gui;

public class ActiveHighLatch {
    private boolean isActive = false;

    public synchronized void set() {
        if (!isActive) {
            isActive = true;
        }
    }

    public synchronized void reset() {
        if (isActive) {
            isActive = false;
        }
    }

    public synchronized boolean isActive() {
        return isActive;
    }
}
