package com.jug.util.componenttree;

import java.util.List;

public class AdvancedComponentPojo {
    private String stringId;
    private int frameNumber;
    private int label;
    private String parentStringId;
    private List<String> childrenStringIds;
    private double value;

    public AdvancedComponentPojo(String stringId, int frameNumber, int label, String parentStringId, List<String> childrenStringIds, double value) {
        this.stringId = stringId;
        this.frameNumber = frameNumber;
        this.label = label;
        this.parentStringId = parentStringId;
        this.childrenStringIds = childrenStringIds;
        this.value = value;
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

    public List<String> getChildrenStringIds() {
        return childrenStringIds;
    }

    public double getValue() {
        return value;
    }
}
