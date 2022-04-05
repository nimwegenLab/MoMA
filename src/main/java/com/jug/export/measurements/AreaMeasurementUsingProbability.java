package com.jug.export.measurements;

import com.jug.export.ResultTable;
import com.jug.export.ResultTableColumn;

public class AreaMeasurementUsingProbability implements SegmentMeasurementInterface {
    private ResultTableColumn<Double> areaCol;

    @Override
    public void setOutputTable(ResultTable outputTable) {
        areaCol = outputTable.addColumn(new ResultTableColumn<Double>("spine_length_calculation_successful__boolean"));
    }

    @Override
    public void measure(SegmentMeasurementDataInterface data) {
        System.out.println();
    }
}
