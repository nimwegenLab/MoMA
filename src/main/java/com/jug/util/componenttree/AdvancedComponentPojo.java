package com.jug.util.componenttree;

public class AdvancedComponentPojo {
    private String stringId;
    private int frameNumber;
    private int label;
    private String parentStringId;

    public AdvancedComponentPojo(String stringId, int frameNumber, int label, String parentStringId) {
        this.stringId = stringId;
        this.frameNumber = frameNumber;
        this.label = label;
        this.parentStringId = parentStringId;
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

    public String getParentStringId() {
        return parentStringId;
    }
}
