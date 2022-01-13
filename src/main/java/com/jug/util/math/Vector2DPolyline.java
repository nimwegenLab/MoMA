package com.jug.util.math;

import edu.mines.jtk.opt.Vect;
import net.imglib2.RealCursor;
import net.imglib2.RealLocalizable;
import net.imglib2.roi.geom.GeomMasks;
import net.imglib2.roi.geom.real.Polygon2D;
import net.imglib2.roi.geom.real.Polyline;

import java.util.ArrayList;
import java.util.List;

public class Vector2DPolyline {
    List<Vector2D> vectors = new ArrayList<>();

    public Vector2DPolyline() {}

    public Vector2DPolyline(List<Vector2D> vectors) {
        this.vectors = vectors;
    }

    public Vector2DPolyline copy(){
        return new Vector2DPolyline(this.vectors);
    }

    public int size(){
        return vectors.size();
    }

    public Vector2D get(int ind){
        return vectors.get(ind);
    }

    public List<Vector2D> getVectorList() {
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

    public static Vector2DPolyline createFromVertexList(List<RealLocalizable> vertices) {
        Vector2DPolyline result = new Vector2DPolyline();
        float[] res = new float[2];
        for (RealLocalizable vertex: vertices){
            result.add(new Vector2D(vertex));
        }
        return result;
    }

    public void add(Vector2D vector){
        vectors.add(vector);
    }

    public void add(int index, Vector2D vector){
        vectors.add(index, vector);
    }

    public LinkedItem<Vector2D> toCircularLinkedList() {
        return LinkedItem.toCircularLinkedList(this.getVectorList());
    }

    public LinkedItem<Vector2D> toLinkedList() {
        return LinkedItem.toLinkedList(this.getVectorList());
    }

    public double length(){
        double result = this.asVectorTrain().stream().mapToDouble(vector2D -> vector2D.getLength()).sum();
        return result;
    }

    public List<Vector2D> asVectorTrain(){
        return GeomUtils.differences(this.getVectorList());
    }

    public void shiftMutate(Vector2D shiftVector) {
        for(Vector2D vect : this.getVectorList()) {
            vect.plusMutate(shiftVector);
        }
    }
}
