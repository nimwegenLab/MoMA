package com.jug.export.measurements;

import com.jug.datahandling.IImageProvider;
import com.jug.util.componenttree.ComponentInterface;

import java.util.List;

public interface SegmentMeasurementDataInterface {
    ComponentInterface getComponentToMeasure();
    List<ComponentInterface> getAllOptimalComponents();
    IImageProvider getImageProvider();
    int getFrameIndex();
}
