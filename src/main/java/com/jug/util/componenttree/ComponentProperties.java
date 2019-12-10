package com.jug.util.componenttree;

import com.jug.util.ComponentTreeUtils;
import com.jug.util.imglib2.Imglib2Utils;
import net.imagej.ops.OpService;
import net.imagej.ops.geom.CentroidPolygon;
import net.imagej.ops.geom.geom2d.DefaultMinorMajorAxis;
import net.imagej.ops.geom.geom2d.LabelRegionToPolygonConverter;
import net.imglib2.Cursor;
import net.imglib2.FinalInterval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.RealPoint;
import net.imglib2.img.Img;
import net.imglib2.outofbounds.OutOfBoundsConstantValueFactory;
import net.imglib2.roi.Regions;
import net.imglib2.roi.geom.real.Polygon2D;
import net.imglib2.roi.labeling.LabelRegion;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.ValuePair;
import net.imglib2.view.ExtendedRandomAccessibleInterval;
import net.imglib2.view.Views;
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

    public long getBackgroundArea(SimpleComponent<?> component, RandomAccessibleInterval<FloatType> img){
        ValuePair<Integer, Integer> limits = ComponentTreeUtils.getComponentPixelLimits(component, 1);
        FinalInterval roi1 = getBackgroundRoi1(img, limits.getA(), limits.getB());
        FinalInterval roi2 = getBackgroundRoi2(img, limits.getA(), limits.getB());
        return roi1.dimension(0) * roi1.dimension(1) + roi2.dimension(0) * roi2.dimension(1);
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
