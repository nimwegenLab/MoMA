package com.jug.util.componenttree;

import com.jug.util.ComponentTreeUtils;
import com.jug.util.math.Vector2D;
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
import org.apache.commons.lang.NotImplementedException;
import org.jetbrains.annotations.NotNull;

public class ComponentProperties {
    private final LabelRegionToPolygonConverter regionToPolygonConverter;
    private final OpService ops;
    private Imglib2Utils imglib2Utils;
    private final OrientedBoundingBoxCalculator boundingBoxCalculator;

    public ComponentProperties(OpService ops, Imglib2Utils imglib2Utils) {
        this.imglib2Utils = imglib2Utils;
        regionToPolygonConverter = new LabelRegionToPolygonConverter();
        regionToPolygonConverter.setContext(ops.context());
        this.ops = ops;
        boundingBoxCalculator = new OrientedBoundingBoxCalculator();
    }

    public ValuePair<Double, Double> getMinorMajorAxis(AdvancedComponent<?> component){
        final Polygon2D poly = regionToPolygonConverter.convert(component.getRegion(), Polygon2D.class);
        ValuePair<DoubleType, DoubleType> minorMajorAxis = (ValuePair<DoubleType, DoubleType>) ops.run(DefaultMinorMajorAxis.class, poly);
        return new ValuePair<>(minorMajorAxis.getA().get(), minorMajorAxis.getB().get());
    }

    public ValuePair<Double, Double> getOrientedBoundingBoxWidthAndHeight(AdvancedComponent<?> component){
        final Polygon2D poly = regionToPolygonConverter.convert(component.getRegion(), Polygon2D.class);
        Polygon2D orientedBoundingBoxPolygon = boundingBoxCalculator.calculate(poly);
//        ValuePair<double[], double[]>
        getWidthAndHeightRelativeToCoordinateSystem(orientedBoundingBoxPolygon);
        return new ValuePair<>(1.0, 1.0);
//        throw new NotImplementedException();
//        ValuePair<DoubleType, DoubleType> minorMajorAxis = (ValuePair<DoubleType, DoubleType>) ops.run(DefaultMinorMajorAxis.class, poly);

//        return new ValuePair<>(minorMajorAxis.getA().get(), minorMajorAxis.getB().get());
    }

    public ValuePair<Double, Double> getWidthAndHeightRelativeToCoordinateSystem(Polygon2D orientedBoundingBoxPolygon) {
        Vector2D vertex0vec = new Vector2D(orientedBoundingBoxPolygon.vertices().get(0));
        Vector2D vertex1vec = new Vector2D(orientedBoundingBoxPolygon.vertices().get(1));
        Vector2D vertex2vec = new Vector2D(orientedBoundingBoxPolygon.vertices().get(2));

        Vector2D shortEdge = vertex1vec.minus(vertex0vec);
        Vector2D longEdge = vertex2vec.minus(vertex1vec);

        if(shortEdge.getLength() > longEdge.getLength()) { /* switch edges according to length */
            Vector2D tmp = longEdge;
            longEdge = shortEdge;
            shortEdge = tmp;
        }



//        RealLocalizable vertex0 = orientedBoundingBoxPolygon.vertices().get(0);
//        RealLocalizable vertex1 = orientedBoundingBoxPolygon.vertices().get(1);
//        RealLocalizable vertex2 = orientedBoundingBoxPolygon.vertices().get(2);

//        double edge1lengthAlongY = Math.abs(vertex0.getDoublePosition(1) - vertex1.getDoublePosition(1));
//        double edge1lengthX = Math.abs(vertex0.getDoublePosition(0) - vertex1.getDoublePosition(0));
//        double edge1length = eucledianDistance(vertex0.getDoublePosition(0), vertex0.getDoublePosition(1), vertex1.getDoublePosition(0), vertex1.getDoublePosition(1));
//
//        double edge2lengthY = Math.abs(vertex1.getDoublePosition(1) - vertex2.getDoublePosition(1));
//        double edge2lengthX = Math.abs(vertex1.getDoublePosition(0) - vertex2.getDoublePosition(0));
//        double edge2length = eucledianDistance(vertex0.getDoublePosition(0), vertex0.getDoublePosition(1), vertex1.getDoublePosition(0), vertex1.getDoublePosition(1));

        throw new NotImplementedException();
//        ValuePair<double[], double[]>
    }

//    private double eucledianDistance(double x1, double y1, double x2, double y2) {
//        return Math.sqrt(Math.pow(x2 - x1, 2.0) + Math.pow(y2 - y1, 2.0));
//    }

    /***
     * Return tilt angle against the vertical axis in radians. We use mathematical rotation direction, where positive
     * values indicate tilt to the left and negative values indicate tilt to the right.
     *
     * @param component
     * @return tilte angle in radians.
     */
    public double getTiltAngle(AdvancedComponent<?> component) {
        final Polygon2D poly = regionToPolygonConverter.convert(component.getRegion(), Polygon2D.class);
        double angle = -((DoubleType) ops.run(DefaultMinimumFeretAngle.class, poly)).get();
        if (angle < -90) angle += 180;
        else if (angle > 90) angle -= 180;
        double angleInRadians = angle;
        return 2 * Math.PI * angleInRadians / 360.0f;
    }

    public int getArea(AdvancedComponent<?> component){
        return (int) component.getRegion().size();
    }

//    private DefaultConvexHull2D convexHullCalculator = new DefaultConvexHull2D();

    public double getConvexHullArea(AdvancedComponent<?> component) {
        final Polygon2D poly = regionToPolygonConverter.convert(component.getRegion(), Polygon2D.class);
//        Polygon2D hull = convexHullCalculator.calculate(poly);
//        DoubleType res = (DoubleType) ops.run("geom.size", hull);
        DoubleType res = (DoubleType) ops.run("geom.sizeConvexHull", poly);
        double result = res.getRealDouble();
        return result;
    }

    public ValuePair<Double, Double> getCentroid(AdvancedComponent<?> component) {
        final Polygon2D poly = regionToPolygonConverter.convert(component.getRegion(), Polygon2D.class);
        RealPoint tmp = (RealPoint) ops.run(CentroidPolygon.class, poly);
        return new ValuePair<>(tmp.getDoublePosition(0), tmp.getDoublePosition(1));
    }

    public double getTotalIntensity(AdvancedComponent<?> component, RandomAccessibleInterval<FloatType> img){
        return imglib2Utils.getTotalIntensity(component.getRegion(), img);
    }

    public double getIntensityCoefficientOfVariation(AdvancedComponent<?> component, RandomAccessibleInterval<FloatType> img){
        return imglib2Utils.getIntensityCoeffVariation(component.getRegion(), img);
    }

    public double getTotalBackgroundIntensity(AdvancedComponent<?> component, RandomAccessibleInterval<FloatType> img){
        ValuePair<Integer, Integer> limits = ComponentTreeUtils.getComponentPixelLimits(component, 1);
        FinalInterval leftBackgroundRoi = getLeftBackgroundRoi(img, limits.getA(), limits.getB());
        double intensity1 = imglib2Utils.getTotalIntensity(leftBackgroundRoi, img);
        FinalInterval rightBackgroundRoi = getRightBackgroundRoi(img, limits.getA(), limits.getB());
        double intensity2 = imglib2Utils.getTotalIntensity(rightBackgroundRoi, img);
        return intensity1 + intensity2;
    }

    public int getBackgroundArea(AdvancedComponent<?> component, RandomAccessibleInterval<FloatType> img){
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
