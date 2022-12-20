package com.jug.util.componenttree;

import com.jug.config.IConfiguration;
import com.jug.lp.costs.ICostFactory;
import com.jug.util.imglib2.Imglib2Utils;
import net.imagej.ops.OpService;
import net.imagej.ops.geom.CentroidPolygon;
import net.imagej.ops.geom.geom2d.DefaultMinimumFeretAngle;
import net.imagej.ops.geom.geom2d.DefaultMinorMajorAxis;
import net.imagej.ops.geom.geom2d.LabelRegionToPolygonConverter;
import net.imglib2.*;
import net.imglib2.roi.geom.real.Polygon2D;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.ValuePair;
import net.imglib2.view.Views;
import org.javatuples.Sextet;
import org.jetbrains.annotations.NotNull;

public class ComponentProperties {
    private final LabelRegionToPolygonConverter regionToPolygonConverter;
    private final OpService ops;
    private Imglib2Utils imglib2Utils;
    private ICostFactory costFactory;
    private IConfiguration configuration;
    private CentralMomentsCalculator polygonMomentsCalculator;

    public ComponentProperties(OpService ops, Imglib2Utils imglib2Utils, ICostFactory costFactory, IConfiguration configuration) {
        this.imglib2Utils = imglib2Utils;
        this.costFactory = costFactory;
        this.configuration = configuration;
        regionToPolygonConverter = new LabelRegionToPolygonConverter();
        regionToPolygonConverter.setContext(ops.context());
        polygonMomentsCalculator = new CentralMomentsCalculator();
        this.ops = ops;
    }

    public synchronized float getCost(AdvancedComponent<?> component) {
        return costFactory.getComponentCost(component);
    }

    /**
     * Returns major and minor axis length.
     * @param component
     * @return
     */
    public synchronized ValuePair<Double, Double> getMinorMajorAxis(AdvancedComponent<?> component){
        final Polygon2D poly = regionToPolygonConverter.convert(component.getRegion(), Polygon2D.class);
        ValuePair<DoubleType, DoubleType> minorMajorAxis = (ValuePair<DoubleType, DoubleType>) ops.run(DefaultMinorMajorAxis.class, poly);
        return new ValuePair<>(minorMajorAxis.getA().get(), minorMajorAxis.getB().get());
    }

    public synchronized Sextet<Double, Double, Double, Double, Double, Double> getCentralMoments(ComponentInterface component) {
        final Polygon2D poly = regionToPolygonConverter.convert(component.getRegion(), Polygon2D.class);
        return polygonMomentsCalculator.calculate(poly);
    }

    /***
     * Return tilt angle against the vertical axis in radians. We use mathematical rotation direction, where positive
     * values indicate tilt to the left and negative values indicate tilt to the right.
     *
     * @param component
     * @return tilte angle in radians.
     */
    public synchronized double getTiltAngle(AdvancedComponent<?> component) {
        final Polygon2D poly = regionToPolygonConverter.convert(component.getRegion(), Polygon2D.class);
        double angle = -((DoubleType) ops.run(DefaultMinimumFeretAngle.class, poly)).get();
        if (angle < -90) angle += 180;
        else if (angle > 90) angle -= 180;
        double angleInRadians = angle;
        return 2 * Math.PI * angleInRadians / 360.0f;
    }

    public synchronized int getArea(AdvancedComponent<?> component){
        return (int) component.getRegion().size();
    }

//    private DefaultConvexHull2D convexHullCalculator = new DefaultConvexHull2D();

    public synchronized double getConvexHullArea(AdvancedComponent<?> component) {
        final Polygon2D poly = regionToPolygonConverter.convert(component.getRegion(), Polygon2D.class);
//        Polygon2D hull = convexHullCalculator.calculate(poly);
//        DoubleType res = (DoubleType) ops.run("geom.size", hull);
        DoubleType res = (DoubleType) ops.run("geom.sizeConvexHull", poly);
        double result = res.getRealDouble();
        return result;
    }

    public synchronized ValuePair<Double, Double> getCentroid(AdvancedComponent<?> component) {
        final Polygon2D poly = regionToPolygonConverter.convert(component.getRegion(), Polygon2D.class);
        RealPoint tmp = (RealPoint) ops.run(CentroidPolygon.class, poly);
        return new ValuePair<>(tmp.getDoublePosition(0), tmp.getDoublePosition(1));
    }

    public synchronized double getIntensityStd(AdvancedComponent<?> component, RandomAccessibleInterval<FloatType> img){
        return imglib2Utils.getIntensityStDev(component.getRegion(), img);
    }

    public synchronized double getIntensityTotal(AdvancedComponent<?> component, RandomAccessibleInterval<FloatType> img){
        return imglib2Utils.getIntensityTotal(component.getRegion(), img);
    }

    public synchronized double getIntensityPercentile(AdvancedComponent<?> component, RandomAccessibleInterval<FloatType> img, double percent){
        IterableInterval<FloatType> region = Views.interval(img, component.getRegion());
        return ops.stats().percentile(region, percent).getRealDouble();
    }

    public synchronized double getIntensityCoefficientOfVariation(AdvancedComponent<?> component, RandomAccessibleInterval<FloatType> img){
        return imglib2Utils.getIntensityCoeffVariation(component.getRegion(), img);
    }

    public synchronized double getBackgroundIntensityTotal(AdvancedComponent<?> component, RandomAccessibleInterval<FloatType> img){
        ValuePair<Integer, Integer> limits = component.getVerticalComponentLimits();;
        FinalInterval leftBackgroundRoi = getLeftBackgroundRoi(img, limits.getA(), limits.getB());
        double intensity1 = imglib2Utils.getIntensityTotal(leftBackgroundRoi, img);
        FinalInterval rightBackgroundRoi = getRightBackgroundRoi(img, limits.getA(), limits.getB());
        double intensity2 = imglib2Utils.getIntensityTotal(rightBackgroundRoi, img);
        return intensity1 + intensity2;
    }

    public synchronized double getBackgroundIntensityStd(AdvancedComponent<?> component, RandomAccessibleInterval<FloatType> img) {
        ValuePair<Integer, Integer> limits = component.getVerticalComponentLimits();
        FinalInterval leftBackgroundRoi = getLeftBackgroundRoi(img, limits.getA(), limits.getB());
        long leftRoiSize = getRoiSize(leftBackgroundRoi);
        double leftRoiIntensity = imglib2Utils.getIntensityStDev(leftBackgroundRoi, img);
        FinalInterval rightBackgroundRoi = getRightBackgroundRoi(img, limits.getA(), limits.getB());
        long rightRoiSize = getRoiSize(rightBackgroundRoi);
        double rightRoiIntensity = imglib2Utils.getIntensityStDev(rightBackgroundRoi, img);
        return (leftRoiSize * leftRoiIntensity + rightRoiSize * rightRoiIntensity) / (leftRoiSize + rightRoiSize);
    }

    private long getRoiSize(Interval rightBackgroundRoi) {
        long size = 0;
        int numDims = rightBackgroundRoi.numDimensions();
        for (int d = 0; d < numDims; d++) {
            size += rightBackgroundRoi.dimension(d);
        }
        return size;
    }

    public synchronized int getBackgroundArea(AdvancedComponent<?> component, RandomAccessibleInterval<FloatType> img){
        ValuePair<Integer, Integer> limits = component.getVerticalComponentLimits();;
        FinalInterval roi1 = getLeftBackgroundRoi(img, limits.getA(), limits.getB());
        FinalInterval roi2 = getRightBackgroundRoi(img, limits.getA(), limits.getB());
        return (int) (roi1.dimension(0) * roi1.dimension(1) + roi2.dimension(0) * roi2.dimension(1));
    }


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
                new long[]{configuration.getBackgroundRoiWidth() - 1, vert_stop}
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
                new long[]{img.max(0) - (configuration.getBackgroundRoiWidth() - 1), vert_start},
                new long[]{img.max(0), vert_stop}
        );
        return tmp;
    }
}
