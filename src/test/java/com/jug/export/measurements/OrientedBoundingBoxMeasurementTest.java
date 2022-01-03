package com.jug.export.measurements;

import com.jug.export.ResultTable;
import net.imagej.ImageJ;
import org.junit.Test;

public class OrientedBoundingBoxMeasurementTest {
    @Test
    public void measurement_works() {
        ImageJ ij = new ImageJ();

        ResultTable outputTable = new ResultTable(",");
        OrientedBoundingBoxMeasurement measurement = new OrientedBoundingBoxMeasurement(outputTable, ij.context());
    }
}
