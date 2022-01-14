package com.jug.export.measurements;

import com.jug.export.ResultTable;
import com.jug.export.ResultTableColumn;
import com.jug.util.componenttree.*;
import com.jug.util.imglib2.Imglib2Utils;
import com.jug.util.math.Vector2DPolyline;
import com.moma.auxiliary.Plotting;
import net.imagej.ops.OpService;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.roi.MaskPredicate;
import net.imglib2.roi.labeling.LabelRegion;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.ValuePair;

import java.util.Arrays;
import java.util.List;

public class SpineLengthMeasurement implements SegmentMeasurementInterface {
    private ResultTableColumn<Double> spineLength;
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

//        if(component.firstMomentPixelCoordinates()[0] == 53.71328671328671){
        if(component.firstMomentPixelCoordinates()[0] == 53.30769230769231){
//        xExpected = ;
//        yExpected = 134.1981351981352;
            System.out.println("stop");
            List<MaskPredicate<?>> rois = Arrays.asList(
                    contour.getPolygon2D(),
                medialLine.getPolyline()
//                    spine.getPolyline()
            );
            Plotting.showImageWithOverlays(image, rois);
        }

        Vector2DPolyline spine = spineCalculator.calculate(medialLine, contour, 0, 2, new ValuePair<>((int) image.min(1), (int) image.max(1)));

        spineLength.addValue(spine.length());
    }
}
