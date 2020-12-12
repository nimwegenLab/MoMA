package com.jug.util.componenttree;

import com.jug.util.ComponentTreeUtils;
import com.jug.util.imglib2.Imglib2Utils;
import net.imagej.ops.OpService;
import net.imagej.ops.geom.CentroidPolygon;
import net.imagej.ops.geom.geom2d.DefaultMinimumFeretAngle;
import net.imagej.ops.geom.geom2d.DefaultMinorMajorAxis;
import net.imagej.ops.geom.geom2d.LabelRegionToPolygonConverter;
import net.imglib2.FinalInterval;
import net.imglib2.Localizable;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.RealPoint;
import net.imglib2.roi.geom.real.Polygon2D;
import net.imglib2.roi.labeling.LabelRegion;
import net.imglib2.roi.labeling.LabelRegionRandomAccess;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.ValuePair;
import org.apache.commons.lang.NotImplementedException;
import org.jetbrains.annotations.NotNull;
import org.scijava.Context;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ComponentProperties {
    private OpService ops = (new Context()).service(OpService.class);
    private final LabelRegionToPolygonConverter regionToPolygonConverter;

    public ComponentProperties() {
        regionToPolygonConverter = new LabelRegionToPolygonConverter();
        regionToPolygonConverter.setContext(ops.context());
    }

    RowsFirstPixelPositionComparator rowsFirstPixelPositionComparator = new RowsFirstPixelPositionComparator();

    public double getSkeletonLength(SimpleComponent<?> component) {
        List<double[]> positions = getSkeletonPositions(component);
        double tmp = calculateSkeletonLength(positions);
//        System.out.println("tmp: " + tmp);
        return tmp;
    }

    public List<double[]>  getSkeletonPositions(SimpleComponent<?> component) {
        Iterator<Localizable> iterator = component.sortedIterator(rowsFirstPixelPositionComparator);

        List<double[]> skeletonPositions = new ArrayList<>();

        Localizable loc = iterator.next();
        double xMin = loc.getDoublePosition(0);
        double xMax = loc.getDoublePosition(0);
        double yCurrent = loc.getDoublePosition(1);
        double x;
        double y;

        while(iterator.hasNext()){
//            System.out.println("x: " + x + "; y: " + y);
            loc = iterator.next();
            x = loc.getDoublePosition(0);
            y = loc.getDoublePosition(1);

            if (y > yCurrent) { /* we are starting work on a new row */
                double xCenter = (xMin + xMax) / 2;
                skeletonPositions.add(new double[]{xCenter, yCurrent}); /* add center point for the new-finished row */
//                System.out.println("xMin: " + xMin + "; xMax: " + xMax);
//                System.out.println("xCenter: " + xCenter + "; yCurrent: " + yCurrent);
                yCurrent = y; /* set yCurrent for the new row */
                xMin = loc.getDoublePosition(0); /* set xMin value for new row */
            }
            xMax = x;
        }
        return skeletonPositions;
    }

    private double calculateSkeletonLength(List<double[]> centerPositions) {
        double skeletonLength = 0;
        for (int ind = 0; ind < centerPositions.size() - 1; ind++) {
            double[] pos1 = centerPositions.get(ind);
            double[] pos2 = centerPositions.get(ind + 1);
            double deltaX = pos1[0] - pos2[0];
            double deltaY = pos1[1] - pos2[1];
            skeletonLength += Math.sqrt(deltaX * deltaX + deltaY * deltaY);
        }
        return skeletonLength;
    }

    public ValuePair<Double, Double> getMinorMajorAxis(SimpleComponent<?> component){
        final Polygon2D poly = regionToPolygonConverter.convert(component.getRegion(), Polygon2D.class);
        ValuePair<DoubleType, DoubleType> minorMajorAxis = (ValuePair<DoubleType, DoubleType>) ops.run(DefaultMinorMajorAxis.class, poly);
        return new ValuePair<>(minorMajorAxis.getA().get(), minorMajorAxis.getB().get());
    }

    /***
     * Return tilt angle against the vertical axis in radians. We use mathematical rotation direction, where positive
     * values indicate tilt to the left and negative values indicate tilt to the right.
     *
     * @param component
     * @return tilte angle in radians.
     */
    public double getTiltAngle(SimpleComponent<?> component) {
        final Polygon2D poly = regionToPolygonConverter.convert(component.getRegion(), Polygon2D.class);
        double angle = -((DoubleType) ops.run(DefaultMinimumFeretAngle.class, poly)).get();
        if (angle < -90) angle += 180;
        else if (angle > 90) angle -= 180;
        double angleInRadians = angle;
        return 2 * Math.PI * angleInRadians / 360.0f;
    }

    public int getArea(SimpleComponent<?> component){
        return (int) component.getRegion().size();
    }

    public ValuePair<Double, Double> getCentroid(SimpleComponent<?> component) {
        final Polygon2D poly = regionToPolygonConverter.convert(component.getRegion(), Polygon2D.class);
        RealPoint tmp = (RealPoint) ops.run(CentroidPolygon.class, poly);
        return new ValuePair<>(tmp.getDoublePosition(0), tmp.getDoublePosition(1));
    }

    public double getTotalIntensity(SimpleComponent<?> component, RandomAccessibleInterval<FloatType> img){
        return Imglib2Utils.getTotalIntensity(component.getRegion(), img);
    }

    public double getTotalBackgroundIntensity(SimpleComponent<?> component, RandomAccessibleInterval<FloatType> img){
        ValuePair<Integer, Integer> limits = ComponentTreeUtils.getComponentPixelLimits(component, 1);
        FinalInterval roi1 = getBackgroundRoi1(img, limits.getA(), limits.getB());
        double intensity1 = Imglib2Utils.getTotalIntensity(roi1, img);
        FinalInterval roi2 = getBackgroundRoi2(img, limits.getA(), limits.getB());
        double intensity2 = Imglib2Utils.getTotalIntensity(roi2, img);
        return intensity1 + intensity2;
    }

    public int getBackgroundArea(SimpleComponent<?> component, RandomAccessibleInterval<FloatType> img){
        ValuePair<Integer, Integer> limits = ComponentTreeUtils.getComponentPixelLimits(component, 1);
        FinalInterval roi1 = getBackgroundRoi1(img, limits.getA(), limits.getB());
        FinalInterval roi2 = getBackgroundRoi2(img, limits.getA(), limits.getB());
        return (int) (roi1.dimension(0) * roi1.dimension(1) + roi2.dimension(0) * roi2.dimension(1));
    }


    long background_roi_width = 5; /* ROI width in pixels*/

    @NotNull
    private FinalInterval getBackgroundRoi1(RandomAccessibleInterval<FloatType> img, long vert_start, long vert_stop) {
        FinalInterval tmp = new FinalInterval(
                new long[]{0, vert_start},
                new long[]{background_roi_width - 1, vert_stop}
        );
        return tmp;
    }

    @NotNull
    private FinalInterval getBackgroundRoi2(RandomAccessibleInterval<FloatType> img, long vert_start, long vert_stop) {
        FinalInterval tmp = new FinalInterval(
                new long[]{img.max(0) - (background_roi_width - 1), vert_start},
                new long[]{img.max(0), vert_stop}
        );
        return tmp;
    }
}
