package com.jug.util.componenttree;

import net.imglib2.Localizable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AdvancedComponentPojo {
    private final String stringId;
    private final int frameNumber;
    private final int label;
    private final String parentStringId;
    private final List<String> childrenStringIds;
    private final double value;
    private final int[] xCoordinates;
    private final int[] yCoordinates;
    private final Float cost;
    private final Integer size; /* This field contains the number of pixels in the component. It is not needed for deserialization, but useful when using the JSON serialization for evaluation purposes. */

    private Map<Integer, Double> maskIntensities;

    private Map<Integer, Double> backgroundIntensities;

    private Map<Integer, Double> backgroundIntensitiesStd;

    private Map<Integer, Double> maskIntensitiesStd;

    public AdvancedComponentPojo(String stringId,
                                 int frameNumber,
                                 int label,
                                 String parentStringId,
                                 List<String> childrenStringIds,
                                 double value,
                                 List<LocalizableImpl> pixelList,
                                 Map<Integer, Double> maskIntensities,
                                 Map<Integer, Double> maskIntensitiesStd,
                                 Map<Integer, Double> backgroundIntensities,
                                 Map<Integer, Double> backgroundIntensitiesStd,
                                 float cost) {
        this.stringId = stringId;
        this.frameNumber = frameNumber;
        this.label = label;
        this.parentStringId = parentStringId;
        this.childrenStringIds = childrenStringIds;
        this.value = value;
        this.maskIntensities = maskIntensities.entrySet().stream().collect(Collectors.toMap(e -> e.getKey(), e -> new Double(e.getValue()))); /* create a copy of HashMap `maskIntensities` */
        this.backgroundIntensities = backgroundIntensities.entrySet().stream().collect(Collectors.toMap(e -> e.getKey(), e -> new Double(e.getValue()))); /* create a copy of HashMap `backgroundIntensities` */
        this.backgroundIntensitiesStd = backgroundIntensitiesStd.entrySet().stream().collect(Collectors.toMap(e -> e.getKey(), e -> new Double(e.getValue()))); /* create a copy of HashMap `backgroundIntensities` */
        this.maskIntensitiesStd = maskIntensitiesStd.entrySet().stream().collect(Collectors.toMap(e -> e.getKey(), e -> new Double(e.getValue()))); /* create a copy of HashMap `backgroundIntensities` */
        this.xCoordinates = new int[pixelList.size()];
        this.yCoordinates = new int[pixelList.size()];
        this.cost = cost;
        this.size = pixelList.size();

        int coordinateIndex = 0;
        for(Localizable loc : pixelList){
            this.xCoordinates[coordinateIndex] = loc.getIntPosition(0);
            this.yCoordinates[coordinateIndex] = loc.getIntPosition(1);
            coordinateIndex++;
        }
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
        List<LocalizableImpl> pixelList = new ArrayList<>();
        for (int coordInd = 0; coordInd < xCoordinates.length; coordInd++) {
            pixelList.add(new LocalizableImpl(xCoordinates[coordInd], yCoordinates[coordInd]));
        }
        return pixelList;
    }

    public Map<Integer, Double> getMaskIntensities() {
        return maskIntensities;
    }

    public Map<Integer, Double> getBackgroundIntensities() {
        return backgroundIntensities;
    }

    public Float getCost() {
        return cost;
    }
}
