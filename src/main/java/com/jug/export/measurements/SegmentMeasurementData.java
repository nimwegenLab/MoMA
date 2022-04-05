package com.jug.export.measurements;

import com.jug.lp.Hypothesis;
import com.jug.util.componenttree.AdvancedComponent;
import com.jug.util.componenttree.ComponentInterface;
import net.imglib2.type.numeric.real.FloatType;

import java.util.List;

public class SegmentMeasurementData implements SegmentMeasurementDataInterface {
    private final ComponentInterface componentOfInterest;
    private final List<Hypothesis<AdvancedComponent<FloatType>>> allComponentsAtTimeStep;

    public SegmentMeasurementData(ComponentInterface componentOfInterest, List<Hypothesis<AdvancedComponent<FloatType>>> allComponentsAtTimeStep) {
        this.componentOfInterest = componentOfInterest;
        this.allComponentsAtTimeStep = allComponentsAtTimeStep;
    }

    @Override
    public ComponentInterface getComponentToMeasure() {
        return componentOfInterest;
    }

    @Override
    public List<Hypothesis<AdvancedComponent<FloatType>>> getOptimalSegments() {
        return allComponentsAtTimeStep;
    }
}
