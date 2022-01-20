package com.jug.export.measurements;

import com.jug.export.ResultTable;
import com.jug.export.ResultTableColumn;
import com.jug.util.componenttree.*;
import com.jug.util.math.Vector2D;
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
    private ResultTableColumn<Double> spineStartToEndPointAngleCol;
    private ResultTableColumn<String> spineXcoordsCol;
    private ResultTableColumn<String> spineYcoordsCol;
    private ResultTableColumn<String> medialLineXcoordsCol;
    private ResultTableColumn<String> medialLineYcoordsCol;
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
        spineLengthCalculationSuccessCol = outputTable.addColumn(new ResultTableColumn<>("spine_length_calculation_successful__boolean"));
        spineLengthCol = outputTable.addColumn(new ResultTableColumn<>("spine_length__px", "%.2f"));
        spineSizeCol = outputTable.addColumn(new ResultTableColumn<>("spine_array_size__integer"));
        spineStartToEndPointAngleCol = outputTable.addColumn(new ResultTableColumn<>("spine_start_to_end_point_angle__rad", "%.4f"));
        medialLineSizeCol = outputTable.addColumn(new ResultTableColumn<>("medial_line_array_size__integer"));
        medialLineLengthCol = outputTable.addColumn(new ResultTableColumn<>("medial_line_length__px", "%.2f"));
        spineXcoordsCol = outputTable.addColumn(new ResultTableColumn<>("spine_x_coordinates__px"));
        spineYcoordsCol = outputTable.addColumn(new ResultTableColumn<>("spine_y_coordinates__px"));
        medialLineXcoordsCol = outputTable.addColumn(new ResultTableColumn<>("medial_line_x_coordinates__px"));
        medialLineYcoordsCol = outputTable.addColumn(new ResultTableColumn<>("medial_line_y_coordinates__px"));
    }

    @Override
    public void measure(ComponentInterface component) {
        RandomAccessibleInterval<BitType> image = component.getComponentImage(new BitType(true));
        Vector2DPolyline medialLine = medialLineCalculator.calculate(image);
        component.addComponentFeature("medialline", medialLine);

        LabelRegion<Integer> componentRegion = component.getRegion();
        Vector2DPolyline contour = contourCalculator.calculate(componentRegion);
        component.addComponentFeature("contour", contour);

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
        String medialLineXcoordsString = medialLine.getCoordinatePositionAsString(0, ";", "%.2f");
        medialLineXcoordsCol.addValue(medialLineXcoordsString);
        String medialLineYcoordsString = medialLine.getCoordinatePositionAsString(1, ";", "%.2f");
        medialLineYcoordsCol.addValue(medialLineYcoordsString);

        Vector2DPolyline spine;
        try {
            spine = spineCalculator.calculate(medialLine, contour, new ValuePair<>((int) image.min(1), (int) image.max(1)));
            spineLengthCalculationSuccessCol.addValue(1);
            spineLengthCol.addValue(spine.length());
            spineSizeCol.addValue(spine.size());
            Vector2D orientationVector = spine.getLast().minus(spine.getFirst());
            double orientationAngle = orientationVector.getPolarAngle();
            spineStartToEndPointAngleCol.addValue(orientationAngle);
            String xCoordsString = spine.getCoordinatePositionAsString(0, ";", "%.2f");
            spineXcoordsCol.addValue(xCoordsString);
            String yCoordsString = spine.getCoordinatePositionAsString(1, ";", "%.2f");
            spineYcoordsCol.addValue(yCoordsString);
            spineStartToEndPointAngleCol.addValue(orientationAngle);
        } catch (java.lang.IndexOutOfBoundsException err) {
//            System.out.println("Spine-length measurement FAILED.");
            spineLengthCalculationSuccessCol.addValue(0);
            spineLengthCol.addValue(-1.0); /* if calculation fails, set value to -1 */
            spineSizeCol.addValue(-1);
            spineStartToEndPointAngleCol.addValue(-1.0);
            spineXcoordsCol.addValue("NA");
            spineYcoordsCol.addValue("NA");
        }
        spine = new Vector2DPolyline();
        component.addComponentFeature("spine", spine);
    }
}
