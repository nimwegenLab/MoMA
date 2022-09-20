package com.jug.util.componenttree;

public class AdvancedComponentPojo {
    private String stringId;
    private int frameNumber;
    private int label;

    public AdvancedComponentPojo(String stringId, int frameNumber, int label) {
        this.stringId = stringId;
        this.frameNumber = frameNumber;
        this.label = label;
    }

    public String getStringId() {
        return stringId;
    }

    public int getFrameNumber() {
        return frameNumber;
    }

    public int getLabel() {
        return label;
    }
}
