package com.jug.util.componenttree;

import com.jug.util.ComponentTreeUtils;
import com.jug.util.imglib2.Imglib2Utils;
import net.imagej.ops.OpService;
import net.imagej.ops.geom.CentroidPolygon;
import net.imagej.ops.geom.geom2d.DefaultMinimumFeretAngle;
import net.imagej.ops.geom.geom2d.DefaultMinorMajorAxis;
import net.imagej.ops.geom.geom2d.LabelRegionToPolygonConverter;
import net.imglib2.FinalInterval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.RealPoint;
import net.imglib2.roi.geom.real.Polygon2D;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.ValuePair;
import org.jetbrains.annotations.NotNull;
import org.scijava.Context;

public class ComponentProperties {
    private OpService ops = (new Context()).service(OpService.class);
    private final LabelRegionToPolygonConverter regionToPolygonConverter;

    public ComponentProperties() {
        regionToPolygonConverter = new LabelRegionToPolygonConverter();
        regionToPolygonConverter.setContext(ops.context());
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
        FinalInterval leftBackgroundRoi = getLeftBackgroundRoi(img, limits.getA(), limits.getB());
        double intensity1 = Imglib2Utils.getTotalIntensity(leftBackgroundRoi, img);
        FinalInterval rightBackgroundRoi = getRightBackgroundRoi(img, limits.getA(), limits.getB());
        double intensity2 = Imglib2Utils.getTotalIntensity(rightBackgroundRoi, img);
        return intensity1 + intensity2;
    }

    public int getBackgroundArea(SimpleComponent<?> component, RandomAccessibleInterval<FloatType> img){
        ValuePair<Integer, Integer> limits = ComponentTreeUtils.getComponentPixelLimits(component, 1);
        FinalInterval roi1 = getLeftBackgroundRoi(img, limits.getA(), limits.getB());
        FinalInterval roi2 = getRightBackgroundRoi(img, limits.getA(), limits.getB());
        return (int) (roi1.dimension(0) * roi1.dimension(1) + roi2.dimension(0) * roi2.dimension(1));
    }


    long background_roi_width = 5; /* ROI width in pixels*/

    /**
     * Returns a ROI interval for calculating the background intensity of the segment under consideration.
     * This ROI is situated at the left image edge, has width background_roi_width and the same vertical position as
     * the region being considered.
     *
     * @param img: that will be accessed; necessary for calculating the horizontal position
     * @param vert_start: vertical start position of the segment under consideration
     * @param vert_stop: vertical stop position of the segment under consideration
     * @return
     */
    @NotNull
    private FinalInterval getLeftBackgroundRoi(RandomAccessibleInterval<FloatType> img, long vert_start, long vert_stop) {
        FinalInterval tmp = new FinalInterval(
                new long[]{0, vert_start},
                new long[]{background_roi_width - 1, vert_stop}
        );
        return tmp;
    }

    /**
     * Returns a ROI interval for calculating the background intensity of the segment under consideration.
     * This ROI is situated at the right image edge, has width background_roi_width and the same vertical position as
     * the region being considered.
     *
     * @param img: that will be accessed; necessary for calculating the horizontal position
     * @param vert_start: vertical start position of the segment under consideration
     * @param vert_stop: vertical stop position of the segment under consideration
     * @return
     */
    @NotNull
    private FinalInterval getRightBackgroundRoi(RandomAccessibleInterval<FloatType> img, long vert_start, long vert_stop) {
        FinalInterval tmp = new FinalInterval(
                new long[]{img.max(0) - (background_roi_width - 1), vert_start},
                new long[]{img.max(0), vert_stop}
        );
        return tmp;
    }
}
