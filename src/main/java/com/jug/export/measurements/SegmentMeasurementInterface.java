package com.jug.export.measurements;

import com.jug.export.ResultTable;
import com.jug.export.SegmentRecord;

public interface SegmentMeasurementInterface {
    void setOutputTable(ResultTable outputTable);

    void measure(SegmentRecord segmentRecord);
}
