package com.jug.export.measurements;

import com.jug.lp.Hypothesis;
import com.jug.util.componenttree.AdvancedComponent;
import com.jug.util.componenttree.ComponentInterface;
import net.imglib2.type.numeric.real.FloatType;

import java.util.List;

public interface SegmentMeasurementDataInterface {
    ComponentInterface getComponentToMeasure();
    List<Hypothesis<AdvancedComponent<FloatType>>> getOptimalSegments();
}
