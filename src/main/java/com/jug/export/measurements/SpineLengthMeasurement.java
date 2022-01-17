package com.jug.export.measurements;

import com.jug.export.ResultTable;
import com.jug.export.ResultTableColumn;
import com.jug.util.componenttree.AdvancedComponent;
import com.jug.util.componenttree.ContourCalculator;
import com.jug.util.componenttree.MedialLineCalculator;
import com.jug.util.componenttree.SpineCalculator;
import com.jug.util.math.Vector2DPolyline;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.roi.labeling.LabelRegion;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.ValuePair;

public class SpineLengthMeasurement implements SegmentMeasurementInterface {
    private ResultTableColumn<Double> spineLengthCol;
    private ResultTableColumn<Integer> spineLengthCalculationSuccessCol;
    private ResultTableColumn<Integer> spineSizeCol;
    private ResultTableColumn<Integer> medialLineSizeCol;
    private ResultTableColumn<Double> medialLineLengthCol;
    private final SpineCalculator spineCalculator;
    private final MedialLineCalculator medialLineCalculator;
    private final ContourCalculator contourCalculator;

    public SpineLengthMeasurement(MedialLineCalculator medialLineCalculator, SpineCalculator spineCalculator, ContourCalculator contourCalculator) {
        this.medialLineCalculator = medialLineCalculator;
        this.spineCalculator = spineCalculator;
        this.contourCalculator = contourCalculator;
    }

    @Override
    public void setOutputTable(ResultTable outputTable) {
        spineLengthCol = outputTable.addColumn(new ResultTableColumn<>("spine_length__px", "%.2f"));
        spineLengthCalculationSuccessCol = outputTable.addColumn(new ResultTableColumn<>("spine_length_calculation_successful__boolean"));
        spineSizeCol = outputTable.addColumn(new ResultTableColumn<>("spine_array_size__integer"));
        medialLineSizeCol = outputTable.addColumn(new ResultTableColumn<>("medial_line_array_size__integer"));
        medialLineLengthCol = outputTable.addColumn(new ResultTableColumn<>("medial_line_length__px", "%.2f"));
    }

    @Override
    public void measure(AdvancedComponent<FloatType> component) {
        RandomAccessibleInterval<BitType> image = component.getComponentImage(new BitType(true));
        Vector2DPolyline medialLine = medialLineCalculator.calculate(image);

        LabelRegion<Integer> componentRegion = component.getRegion();
        Vector2DPolyline contour = contourCalculator.calculate(componentRegion);

//        if(component.firstMomentPixelCoordinates()[0] == 53.71328671328671){
//        if(component.firstMomentPixelCoordinates()[0] == 53.30769230769231){
//        if(component.firstMomentPixelCoordinates()[0] == 53.574380165289256){
//            IJ.saveAsTiff(ImageJFunctions.wrap(image, "componentImage"), "/home/micha/Documents/01_work/git/MoMA/src/test/resources/ComponentMasks/component_image_3.tiff");
////        xExpected = ;
////        yExpected = 134.1981351981352;
//            System.out.println("stop");
//            List<MaskPredicate<?>> rois = Arrays.asList(
//                    contour.getPolygon2D(),
//                medialLine.getPolyline()
////                    spine.getPolyline()
//            );
//            Plotting.showImageWithOverlays(image, rois);
//        }

//        if(medialLine.size() == 0){
//            System.out.println("stop");
//            List<MaskPredicate<?>> rois = Arrays.asList(
//                    contour.getPolygon2D(),
//                medialLine.getPolyline()
////                    spine.getPolyline()
//            );
//            Plotting.showImageWithOverlays(image, rois);
//        }

//        if(medialLine.size() < 20){ /* method for calculating spine will fail for less than 20 pixels */
////            System.out.println("Skipped spine-length measurement.");
//            spineLength.addValue(-1.0);
//            return;
//        }

        medialLineSizeCol.addValue(medialLine.size());
        medialLineLengthCol.addValue(medialLine.length());

        try {
            Vector2DPolyline spine = spineCalculator.calculate(medialLine, contour, new ValuePair<>((int) image.min(1), (int) image.max(1)));
            spineLengthCol.addValue(spine.length());
            spineSizeCol.addValue(spine.size());
            spineLengthCalculationSuccessCol.addValue(1);
        } catch (java.lang.IndexOutOfBoundsException err) {
//            System.out.println("Spine-length measurement FAILED.");
            spineLengthCol.addValue(-1.0); /* if calculation fails, set value to -1 */
            spineSizeCol.addValue(-1);
            spineLengthCalculationSuccessCol.addValue(0);
        }
    }
}
