package com.jug.export.measurements;

import com.jug.export.ResultTable;
import com.jug.export.ResultTableColumn;
import com.jug.util.componenttree.*;
import com.jug.util.imglib2.Imglib2Utils;
import com.jug.util.math.Vector2DPolyline;
import net.imagej.ops.OpService;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.roi.labeling.LabelRegion;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.ValuePair;

public class SpineLengthMeasurement implements SegmentMeasurementInterface {
    private ResultTableColumn<Double> spineLength;
    private OpService ops;
    private Imglib2Utils imglib2Utils;
    private SpineCalculator spineCalculator;
    private MedialLineCalculator medialLineCalculator;
    private ContourCalculator contourCalculator;

    public SpineLengthMeasurement(MedialLineCalculator medialLineCalculator, SpineCalculator spineCalculator, ContourCalculator contourCalculator) {
        this.medialLineCalculator = medialLineCalculator;
        this.spineCalculator = spineCalculator;
        this.contourCalculator = contourCalculator;
    }

    @Override
    public void setOutputTable(ResultTable outputTable) {
        spineLength = outputTable.addColumn(new ResultTableColumn<>("spine_length_px", "%.2f"));
    }

    @Override
    public void measure(AdvancedComponent<FloatType> component) {
        RandomAccessibleInterval<BitType> image = component.getComponentImage(new BitType(true));

        Vector2DPolyline medialLine = medialLineCalculator.calculate(image);

        LabelRegion<Integer> componentRegion = component.getRegion();
        Vector2DPolyline contour = contourCalculator.calculate(componentRegion);

        Vector2DPolyline spine = spineCalculator.calculate(medialLine, contour, 0, 2, new ValuePair<>((int) image.min(1), (int) image.max(1)));

        spineLength.addValue(spine.length());
    }
}
