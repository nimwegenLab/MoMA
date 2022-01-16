package com.jug.export.measurements;

import com.jug.export.ResultTable;
import com.jug.export.ResultTableColumn;
import com.jug.util.componenttree.*;
import com.jug.util.imglib2.Imglib2Utils;
import com.jug.util.math.Vector2DPolyline;
import com.moma.auxiliary.Plotting;
import ij.IJ;
import net.imagej.ops.OpService;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.display.imagej.ImageJFunctions;
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
//        if(component.firstMomentPixelCoordinates()[0] == 53.30769230769231){
        if(component.firstMomentPixelCoordinates()[0] == 53.574380165289256){
            IJ.saveAsTiff(ImageJFunctions.wrap(image, "componentImage"), "/home/micha/Documents/01_work/git/MoMA/src/test/resources/ComponentMasks/component_image_3.tiff");
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

        if(medialLine.size() == 0){
            System.out.println("stop");
            List<MaskPredicate<?>> rois = Arrays.asList(
                    contour.getPolygon2D(),
                medialLine.getPolyline()
//                    spine.getPolyline()
            );
            Plotting.showImageWithOverlays(image, rois);
        }

        Vector2DPolyline spine = spineCalculator.calculate(medialLine, contour, new ValuePair<>((int) image.min(1), (int) image.max(1)));

        spineLength.addValue(spine.length());
    }
}
