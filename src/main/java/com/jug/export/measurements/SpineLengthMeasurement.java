package com.jug.export.measurements;

import com.jug.export.ResultTable;
import com.jug.export.ResultTableColumn;
import com.jug.util.componenttree.ComponentInterface;

public class SpineLengthMeasurement implements SegmentMeasurementInterface {
    private ResultTableColumn<Double> spineLength;

    @Override
    public void setOutputTable(ResultTable outputTable) {
        spineLength = outputTable.addColumn(new ResultTableColumn<>("spine_length_px", "%.2f"));
    }

    @Override
    public void measure(ComponentInterface component) {

    }
}
