package com.jug.util.componenttree;

import com.jug.util.math.Vector2D;
import com.jug.util.math.Vector2DPolyline;
import net.imagej.ops.OpService;
import net.imagej.ops.geom.geom2d.LabelRegionToPolygonConverter;
import net.imglib2.roi.geom.real.Polygon2D;
import net.imglib2.roi.labeling.LabelRegion;

public class ContourCalculator {
    private final LabelRegionToPolygonConverter regionToPolygonConverter;
    private OpService ops;

    public ContourCalculator(OpService ops) {
        this.ops = ops;
        regionToPolygonConverter = new LabelRegionToPolygonConverter();
        regionToPolygonConverter.setContext(ops.context());
    }

    public Vector2DPolyline calculate(LabelRegion<?> region){
        Polygon2D tmp = regionToPolygonConverter.convert(region, Polygon2D.class);
        Vector2DPolyline result = Vector2DPolyline.createFromVertexList(tmp.vertices());
//        result.shiftMutate(new Vector2D(.5, .5));
        return result;
    }
}
