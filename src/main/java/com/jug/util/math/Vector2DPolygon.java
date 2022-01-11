package com.jug.util.math;

import net.imglib2.RealCursor;
import net.imglib2.roi.geom.GeomMasks;
import net.imglib2.roi.geom.real.Polygon2D;
import net.imglib2.roi.geom.real.Polyline;
import net.imglib2.roi.labeling.LabelRegion;
import net.imglib2.roi.labeling.LabelRegionCursor;

import java.util.ArrayList;
import java.util.List;

public class Vector2DPolygon {
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

//    public static Vector2DPolygon createFromCursor(LabelRegion region){
    public static Vector2DPolygon createFromCursor(RealCursor<?> cursor){
//        LabelRegionCursor cursor = region.cursor();
//        double[] xPos = new double[(int)region.size()];
//        double[] yPos = new double[(int)region.size()];
//        List<float[]> coords = new ArrayList<>();
        Vector2DPolygon result = new Vector2DPolygon();
        float[] res = new float[2];
        int ind = 0;
        while (cursor.hasNext()){
            cursor.next();
            cursor.localize(res);
//            coords.add(res);
//            xPos[ind] = res[0];
//            yPos[ind] = res[1];
            result.add(new Vector2D(cursor));
//            ind++;
        }
        return result;
    }

    public void add(Vector2D vector){
        vectors.add(vector);
    }
}
