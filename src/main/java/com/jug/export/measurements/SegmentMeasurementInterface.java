package com.jug.export.measurements;

import com.jug.export.ResultTable;
import com.jug.export.SegmentRecord;
import com.jug.util.componenttree.AdvancedComponent;
import com.jug.util.componenttree.ComponentInterface;
import net.imglib2.type.numeric.real.FloatType;

public interface SegmentMeasurementInterface {
    void setOutputTable(ResultTable outputTable);

    void measure(AdvancedComponent<FloatType> component);
}
