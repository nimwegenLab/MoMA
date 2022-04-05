package com.jug.export.measurements;

import com.jug.util.componenttree.ComponentInterface;

import java.util.List;

public class SegmentMeasurementData implements SegmentMeasurementDataInterface {
    private final ComponentInterface componentOfInterest;
    private final List<ComponentInterface> allComponentsAtTimeStep;

    public SegmentMeasurementData(ComponentInterface componentOfInterest, List<ComponentInterface> allComponentsAtTimeStep) {
        this.componentOfInterest = componentOfInterest;
        this.allComponentsAtTimeStep = allComponentsAtTimeStep;
    }

    @Override
    public ComponentInterface getComponentToMeasure() {
        return componentOfInterest;
    }

    @Override
    public List<ComponentInterface> getOptimalComponents() {
        return allComponentsAtTimeStep;
    }
}
