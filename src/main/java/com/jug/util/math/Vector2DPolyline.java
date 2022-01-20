package com.jug.util.math;

import org.apache.commons.lang3.StringUtils;

import net.imglib2.RealCursor;
import net.imglib2.RealLocalizable;
import net.imglib2.roi.geom.GeomMasks;
import net.imglib2.roi.geom.real.Polygon2D;
import net.imglib2.roi.geom.real.Polyline;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class Vector2DPolyline {
    public enum PolyshapeType {
        UNDEFINED,
        POLYGON,
        POLYLINE
    }

    List<Vector2D> vectors = new ArrayList<>();

    private PolyshapeType type = PolyshapeType.UNDEFINED;
    public void setType(PolyshapeType type){
        this.type = type;
    }

    public PolyshapeType getType(){
        return type;
    }

    public Vector2DPolyline() {}

    public Vector2DPolyline(List<Vector2D> vectors) {
        this.vectors = vectors;
    }

    public Vector2DPolyline copy(){
        return new Vector2DPolyline(new ArrayList<>(this.vectors));
    }

    public int size(){
        return vectors.size();
    }

    public void remove(Vector2D toRemove) {
        this.vectors.remove(toRemove);
    }

    public Vector2D get(int ind){
        return vectors.get(ind);
    }

    public Vector2D getFirst(){
        return vectors.get(0);
    }

    public Vector2D getLast(){
        return get(this.size() - 1);
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

    public void addAtStart(Vector2D vector) {
        vectors.add(0, vector);
    }

    public void addAtEnd(Vector2D vector) {
        vectors.add(vector);
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

    public Vector2DPolyline shiftMutate(Vector2D shiftVector) {
        for(Vector2D vect : this.getVectorList()) {
            vect.plusMutate(shiftVector);
        }
        return this;
    }

    public boolean isClosedPolygon(){
        return (vectors.get(0).getX() == vectors.get(size()-1).getX()) && (vectors.get(0).getY() == vectors.get(size()-1).getY());
    }

    public void reverse() {
        Collections.reverse(this.vectors);
    }

    public String getCoordinatePositionAsString(int positionIndex, String separator, String valueFormat) {
        String str = this.vectors.stream().map(vect -> String.format(valueFormat, vect.getDoublePosition(positionIndex)) + separator).collect(Collectors.joining());
        str = StringUtils.chop(str);
        return str;
    }

    public boolean isEmpty() {
        return vectors.isEmpty();
    }
}
