package com.jug.util.componenttree;

public class AdvancedComponentPojo {
    private String stringId;
    private int frameNumber;

    public AdvancedComponentPojo(String stringId, int frameNumber) {
        this.stringId = stringId;
        this.frameNumber = frameNumber;
    }

    public String getStringId() {
        return stringId;
    }

    public int getFrameNumber() {
        return frameNumber;
    }
}
