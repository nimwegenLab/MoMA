package com.jug.util.math;

import net.imglib2.RealCursor;
import net.imglib2.roi.geom.GeomMasks;
import net.imglib2.roi.geom.real.Polygon2D;
import net.imglib2.roi.geom.real.Polyline;

import java.util.ArrayList;
import java.util.List;

public class Vector2DPolyline {
    List<Vector2D> vectors = new ArrayList<>();

    List<Vector2D> getVectorList() {
        return vectors; // this should return a clone
    }

    public Polygon2D getPolygon2D(){
        return GeomMasks.polygon2D(vectors);
    }

    public Polyline getPolyline(){
        return GeomMasks.polyline(vectors);
    }

    public static Vector2DPolyline createFromCursor(RealCursor<?> cursor){
        Vector2DPolyline result = new Vector2DPolyline();
        float[] res = new float[2];
        while (cursor.hasNext()){
            cursor.next();
            cursor.localize(res);
            result.add(new Vector2D(cursor));
        }
        return result;
    }

    public void add(Vector2D vector){
        vectors.add(vector);
    }
}
