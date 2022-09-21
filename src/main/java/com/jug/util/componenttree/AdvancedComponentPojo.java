package com.jug.util.componenttree;

import net.imglib2.Localizable;

import java.util.List;

public class AdvancedComponentPojo {
    private final String stringId;
    private final int frameNumber;
    private final int label;
    private final String parentStringId;
    private final List<String> childrenStringIds;
    private final double value;
    private final List<LocalizableImpl> pixelList;

    public AdvancedComponentPojo(String stringId,
                                 int frameNumber,
                                 int label,
                                 String parentStringId,
                                 List<String> childrenStringIds,
                                 double value,
                                 List<LocalizableImpl> pixelList) {
        this.stringId = stringId;
        this.frameNumber = frameNumber;
        this.label = label;
        this.parentStringId = parentStringId;
        this.childrenStringIds = childrenStringIds;
        this.value = value;
        this.pixelList = pixelList;
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

    public List<LocalizableImpl> getPixelList() {
        return pixelList;
    }
}
