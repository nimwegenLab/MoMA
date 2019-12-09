package com.jug.util.componenttree;

import net.imagej.ops.OpService;
import net.imagej.ops.geom.geom2d.DefaultMajorAxis;
import net.imagej.ops.geom.geom2d.LabelRegionToPolygonConverter;
import net.imglib2.roi.geom.real.Polygon2D;
import net.imglib2.type.Type;
import net.imglib2.type.numeric.real.DoubleType;
import org.scijava.Context;

public class ComponentProperties {
    private OpService ops = (new Context()).service(OpService.class);
    private final LabelRegionToPolygonConverter regionToPolygonConverter;

    public ComponentProperties() {
        regionToPolygonConverter = new LabelRegionToPolygonConverter();
        regionToPolygonConverter.setContext(ops.context());
    }

    public <T extends Type<T>> double getMajorAxis(SimpleComponent<T> component){
        final Polygon2D poly = regionToPolygonConverter.convert(component.getRegion(), Polygon2D.class);
        return ((DoubleType) ops.run(DefaultMajorAxis.class, poly)).get();
    }
}
