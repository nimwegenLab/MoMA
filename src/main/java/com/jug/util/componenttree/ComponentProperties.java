package com.jug.util.componenttree;

import net.imagej.ops.OpService;
import net.imagej.ops.geom.CentroidPolygon;
import net.imagej.ops.geom.geom2d.DefaultMajorAxis;
import net.imagej.ops.geom.geom2d.DefaultMinorMajorAxis;
import net.imagej.ops.geom.geom2d.DefaultSizePolygon;
import net.imagej.ops.geom.geom2d.LabelRegionToPolygonConverter;
import net.imglib2.RealPoint;
import net.imglib2.roi.geom.real.Polygon2D;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.util.ValuePair;
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

    public RealPoint getCentroid(SimpleComponent<?> component){
        final Polygon2D poly = regionToPolygonConverter.convert(component.getRegion(), Polygon2D.class);
        return (RealPoint) ops.run(CentroidPolygon.class, poly);
    }
}
