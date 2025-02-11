package com.jug.util.componenttree;

import com.jug.util.math.GeomUtils;
import net.imagej.ops.geom.geom2d.DefaultConvexHull2D;
import net.imglib2.roi.geom.GeomMasks;
import net.imglib2.roi.geom.real.Polygon2D;
import net.imglib2.util.ValuePair;
import org.apache.commons.lang.ArrayUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 *  Fits a rectangle minimum area to a polygon.
 *
 * 	It searches the for the minimum area bounding rectangles among the ones that have a side
 * 	which is collinear with an edge of the convex hull.
 *
 * 	Concept based on:
 * 	H. Freeman and R. Shapira. 1975. Determining the minimum-area encasing rectangle for an arbitrary
 * 	closed curve. Commun. ACM 18, 7 (July 1975), 409–413. DOI:https://doi.org/10.1145/360881.360919
 *
 * 	Many thanks to @mountain_man (https://forum.image.sc/u/mountain_man) for helping me correct
 * 	my initial (wrong) intuition about the right way to tackle this problem!
 *
 *  Code was adapted from: https://raw.githubusercontent.com/ndefrancesco/macro-frenzy/master/geometry/fitting/fitMinRectangle.ijm
 */

public class OrientedBoundingBoxCalculator {
    private final DefaultConvexHull2D convexHullCalculator = new DefaultConvexHull2D();

    /**
     * This function calculates the convex hull of a polygon and returns its oriented bounding box as a polygon.
     *
     * @param polygon polygon for which the oriented bounding box will be calculated
     * @return oriented bounding box as a polygon
     */
    public Polygon2D calculate(Polygon2D polygon) {
        return getOrientedRectangleWithMinimalArea(polygon);
    }

    /**
     * This function calculates the convex hull of a polygon and returns its oriented bounding box as a polygon.
     *
     * @param polygon polygon for which the oriented bounding box will be calculated
     * @return oriented bounding box as a polygon
     */
    public Polygon2D getOrientedRectangleWithMinimalArea(Polygon2D polygon) {
        Polygon2D polyHull = convexHullCalculator.calculate(polygon);
        List<Double> xList = polyHull.vertices().stream().map(entry -> entry.getDoublePosition(0)).collect(Collectors.toList());
        List<Double> yList = polyHull.vertices().stream().map(entry -> entry.getDoublePosition(1)).collect(Collectors.toList());
        double[] x = ArrayUtils.toPrimitive(xList.toArray(new Double[0]));
        double[] y = ArrayUtils.toPrimitive(yList.toArray(new Double[0]));
        ValuePair<double[], double[]> res = getOrientedBoundingBoxCoordinatesOfConvexHull(x, y);
        Polygon2D orientedBoundingBoxPoly = GeomMasks.polygon2D(res.getA(), res.getB());
        return orientedBoundingBoxPoly;
    }

    /**
     * Determine the index of the coordinate with maximal perpendicular distance from the edge-line being considered:
     * (x[indEdgeSource], y[indEdgeSource]) -> (x[indEdgeTarget], y[indEdgeTarget])
     * Return both its index and distance from the edge.
     *
     * @param x
     * @param y
     * @param indEdgeSource
     * @param indEdgeTarget
     * @return index and distance of the coordinate that has maximal distance from the considered edge.
     */
    public ValuePair<Integer, Double> calculateDistanceAndIndexOfCoordWithMaximalPerpendicularDistance(double[] x, double[] y, int indEdgeSource, int indEdgeTarget) {
        int indOfCoordWithMaxDistanceToEdge = -1;
        int numberOfCoords = x.length;
        double distanceOfCoordWithMaxDistanceToEdge = 0.0;
        for (int coordIndex = 0; coordIndex < numberOfCoords; coordIndex++) {
            double distanceOfCoordToEdge = Math.abs(GeomUtils.perpDist(x[indEdgeSource], y[indEdgeSource], x[indEdgeTarget], y[indEdgeTarget], x[coordIndex], y[coordIndex]));
            if (distanceOfCoordWithMaxDistanceToEdge < distanceOfCoordToEdge) {
                distanceOfCoordWithMaxDistanceToEdge = distanceOfCoordToEdge;
                indOfCoordWithMaxDistanceToEdge = coordIndex;
            }
        }
        return new ValuePair<>(indOfCoordWithMaxDistanceToEdge, distanceOfCoordWithMaxDistanceToEdge);
    }

    /**
     * Calculate the projections of extremal coordinates in direction of polygon edge being considered:
     * (x[indEdgeSource], y[indEdgeSource]) -> (x[indEdgeTarget], y[indEdgeTarget])
     * From this we can calculate the size of the oriented bounding box in direction of this edge.
     *
     * @param x
     * @param y
     * @param indEdgeSource
     * @param indEdgeTarget
     * @return minimal and maximal bounding-box extent in direction of the considered edge.
     */
    public ValuePair<Double, Double> calculateExtentsInDirectionOfEdge(double[] x, double[] y, int indEdgeSource, int indEdgeTarget) {
        int numberOfCoords = x.length;
        double minBboxExtentInEdgeDirection = 0.0;
        double maxBboxExtentInEdgeDirection = 0.0;
        for (int k = 0; k < numberOfCoords; k++) { /* perform rotating calipers */
            double bboxExtent = GeomUtils.parDist(x[indEdgeSource], y[indEdgeSource], x[indEdgeTarget], y[indEdgeTarget], x[k], y[k]);
            minBboxExtentInEdgeDirection = Math.min(minBboxExtentInEdgeDirection, bboxExtent);
            maxBboxExtentInEdgeDirection = Math.max(maxBboxExtentInEdgeDirection, bboxExtent);
        }
        return new ValuePair<>(minBboxExtentInEdgeDirection, maxBboxExtentInEdgeDirection);
    }

    /**
     * Calculate the vertices of the oriented bounding box with minimal area. The algorithm for doing this is the
     * following:
     * <p>
     * We iterate through all edges of the convex hull and calculate the area of the bounding rectangle whose side
     * coincides with the edge. This is done by:
     * 1. Determining the vertex of the convex hull with maximal distance from the edge. This gives the extent of the
     * bounding box in one direction.
     * 2. Calculating the extent of the bounding box in direction of the considered edge. This gives the extent in the
     * second dimension.
     * We keep index of the edge and perpendicular vertex that yield the minimal area as well as its extents. This is
     * the oriented bounding box that we are looking for. From this, the bounding box coordinates are calculated.
     *
     * @param x x coordinates of the convex hull
     * @param y y coordinates fo the convex hull
     * @return ValuePair of lists containing the x- and y-values of vertices of the oriented bounding box
     */
    public ValuePair<double[], double[]> getOrientedBoundingBoxCoordinatesOfConvexHull(double[] x, double[] y) {
        int numberOfCoords = x.length;

        double minimalArea = Double.MAX_VALUE;
        int IndEdgeSourceFinal = -1;
        int indEdgeTargetFinal = -1;
        int indOfCoordWithMaxDistanceToEdgeFinal = -1;

        double minBboxExtentInEdgeDirectionFinal = 0.0;
        double maxBboxExtentInEdgeDirectionFinal = 0.0;

        for (int indEdgeSource = 0; indEdgeSource < numberOfCoords; indEdgeSource++) {
            int indEdgeTarget;
            if (indEdgeSource < numberOfCoords - 1)
                indEdgeTarget = indEdgeSource + 1; /* handle last coordinate pair in (xp[np-1], yp[np-1]) */
            else indEdgeTarget = 0;

            ValuePair<Integer, Double> coordIndexAndDistance = calculateDistanceAndIndexOfCoordWithMaximalPerpendicularDistance(x, y, indEdgeSource, indEdgeTarget);
            int indOfCoordWithMaxDistanceToEdge = coordIndexAndDistance.getA();
            double distanceOfCoordWithMaxDistanceToEdge = coordIndexAndDistance.getB();

            ValuePair<Double, Double> extentsInDirectionOfEdge = calculateExtentsInDirectionOfEdge(x, y, indEdgeSource, indEdgeTarget);
            double minBboxExtentInEdgeDirection = extentsInDirectionOfEdge.getA();
            double maxBboxExtentInEdgeDirection = extentsInDirectionOfEdge.getB();

            double area = distanceOfCoordWithMaxDistanceToEdge * (maxBboxExtentInEdgeDirection - minBboxExtentInEdgeDirection);

            if (minimalArea > area) {
                minimalArea = area;
                minBboxExtentInEdgeDirectionFinal = minBboxExtentInEdgeDirection;
                maxBboxExtentInEdgeDirectionFinal = maxBboxExtentInEdgeDirection;
                IndEdgeSourceFinal = indEdgeSource;
                indEdgeTargetFinal = indEdgeTarget;
                indOfCoordWithMaxDistanceToEdgeFinal = indOfCoordWithMaxDistanceToEdge;
            }
        }

        double sizePerpendicularToEdge = GeomUtils.perpDist(x[IndEdgeSourceFinal], y[IndEdgeSourceFinal], x[indEdgeTargetFinal], y[indEdgeTargetFinal], x[indOfCoordWithMaxDistanceToEdgeFinal], y[indOfCoordWithMaxDistanceToEdgeFinal]); // signed feret diameter
        double edgeAngle = Math.atan2(y[indEdgeTargetFinal] - y[IndEdgeSourceFinal], x[indEdgeTargetFinal] - x[IndEdgeSourceFinal]);
        double minAngle = edgeAngle + Math.PI / 2;
        double sizeInDirectionOfEdge = minBboxExtentInEdgeDirectionFinal - maxBboxExtentInEdgeDirectionFinal;

        double[] nxp = new double[4];
        double[] nyp = new double[4];

        nxp[0] = x[IndEdgeSourceFinal] + Math.cos(edgeAngle) * maxBboxExtentInEdgeDirectionFinal;
        nyp[0] = y[IndEdgeSourceFinal] + Math.sin(edgeAngle) * maxBboxExtentInEdgeDirectionFinal;

        nxp[1] = nxp[0] + Math.cos(minAngle) * sizePerpendicularToEdge;
        nyp[1] = nyp[0] + Math.sin(minAngle) * sizePerpendicularToEdge;

        nxp[2] = nxp[1] + Math.cos(edgeAngle) * sizeInDirectionOfEdge;
        nyp[2] = nyp[1] + Math.sin(edgeAngle) * sizeInDirectionOfEdge;

        nxp[3] = nxp[2] + Math.cos(minAngle) * -sizePerpendicularToEdge;
        nyp[3] = nyp[2] + Math.sin(minAngle) * -sizePerpendicularToEdge;

        return new ValuePair<>(nxp, nyp);
    }
}


