package com.jug.util.componenttree;

import com.jug.util.math.Vector2D;
import net.imglib2.roi.geom.real.Polygon2D;

public class BoundingBoxProperties {
    private final Vector2D centerCoordinate;
    private double rotationAngle;
    private double area;
    private double height;
    private double width;

    public BoundingBoxProperties(Polygon2D orientedBoundingBoxPolygon) {
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
        height = longEdge.getLength();
        width = shortEdge.getLength();
        area = width * height;
        Vector2D halfDiagonal = vertex2vec.minus(vertex0vec).multiply(0.5);
        centerCoordinate = vertex0vec.plus(halfDiagonal);

        rotationAngle = longEdge.getPolarAngle();
        if (rotationAngle == Math.PI){
            rotationAngle = 0; /* a vertically aligned, rectangular bbox that is wider than tall, will always return the length-angle in positive x-direction giving thus an angle 0 */
        }
        if (rotationAngle < 0) {
            rotationAngle = rotationAngle + Math.PI; /* the orientation angle of the bounding box to always be positive; i.e. in positive y-direction */
        }
    }

    public double getArea() {
        return area;
    }

    public double getWidth() {
        return width;
    }

    public double getHeight() {
        return height;
    }

    public Vector2D getCenterCoordinate() {
        return centerCoordinate;
    }

    public double getRotationAngle() {
        return rotationAngle;
    }
}
