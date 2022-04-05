package com.jug.export.measurements;

import com.jug.datahandling.IImageProvider;
import com.jug.util.componenttree.ComponentInterface;

import java.util.List;

public class SegmentMeasurementData implements SegmentMeasurementDataInterface {
    private final ComponentInterface componentOfInterest;
    private final List<ComponentInterface> allComponentsAtTimeStep;
    private IImageProvider imageProvider;
    private int frameIndex;

    public SegmentMeasurementData(ComponentInterface componentOfInterest, List<ComponentInterface> allComponentsAtTimeStep, IImageProvider imageProvider, int frameIndex) {
        this.componentOfInterest = componentOfInterest;
        this.allComponentsAtTimeStep = allComponentsAtTimeStep;
        this.imageProvider = imageProvider;
        this.frameIndex = frameIndex;
    }

    @Override
    public ComponentInterface getComponentToMeasure() {
        return componentOfInterest;
    }

    @Override
    public List<ComponentInterface> getAllOptimalComponents() {
        return allComponentsAtTimeStep;
    }

    @Override
    public IImageProvider getImageProvider() {
        return imageProvider;
    }

    @Override
    public int getFrameIndex() {return frameIndex; }
}
