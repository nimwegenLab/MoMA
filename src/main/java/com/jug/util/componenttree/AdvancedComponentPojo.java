package com.jug.util.componenttree;

import java.util.List;

public class AdvancedComponentPojo {
    private String stringId;
    private int frameNumber;
    private int label;
    private String parentStringId;
    private List<String> childrenStringIds;

    public AdvancedComponentPojo(String stringId, int frameNumber, int label, String parentStringId, List<String> childrenStringIds) {
        this.stringId = stringId;
        this.frameNumber = frameNumber;
        this.label = label;
        this.parentStringId = parentStringId;
        this.childrenStringIds = childrenStringIds;
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
}
