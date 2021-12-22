package com.jug.util.componenttree;

import com.jug.util.Vector2D;
import net.imglib2.roi.geom.real.Polygon2D;

public class BoundingBoxProperties {
    private final Vector2D centerCoordinate;
    private double area;
    private double height;
    private double width;

    public BoundingBoxProperties(Polygon2D orientedBoundingBoxPolygon) {
        Vector2D vertex0vec = new Vector2D(orientedBoundingBoxPolygon.vertices().get(0));
        Vector2D vertex1vec = new Vector2D(orientedBoundingBoxPolygon.vertices().get(1));
        Vector2D vertex2vec = new Vector2D(orientedBoundingBoxPolygon.vertices().get(2));

        Vector2D shortEdge = vertex1vec.minus(vertex0vec);
        Vector2D longEdge = vertex2vec.minus(vertex1vec);

        if(shortEdge.length() > longEdge.length()) { /* switch edges according to length */
            Vector2D tmp = longEdge;
            longEdge = shortEdge;
            shortEdge = tmp;
        }
        height = longEdge.length();
        width = shortEdge.length();
        area = width * height;
        Vector2D halfDiagonal = vertex2vec.minus(vertex0vec).multiply(0.5);
        centerCoordinate = vertex0vec.plus(halfDiagonal);
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
}
