package com.jug.util.componenttree;

import net.imagej.ops.OpService;
import net.imagej.ops.geom.geom2d.DefaultConvexHull2D;
import net.imagej.ops.geom.geom2d.LabelRegionToPolygonConverter;
import net.imglib2.roi.geom.GeomMasks;
import net.imglib2.roi.geom.real.Polygon2D;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.ValuePair;
import org.apache.commons.lang.ArrayUtils;

import java.util.List;
import java.util.stream.Collectors;

/*	Fits a minimum area rectangle into a ROI.
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
    private final OpService ops;

    public OrientedBoundingBoxCalculator(OpService ops) {
        this.ops = ops;
    }

    public Polygon2D calculate(AdvancedComponent<FloatType> component) {
        return getOrientedRectangleWithMinimalArea(component);
    }

    public Polygon2D getOrientedRectangleWithMinimalArea(AdvancedComponent<FloatType> component) {
        LabelRegionToPolygonConverter regionToPolygonConverter = new LabelRegionToPolygonConverter();
        regionToPolygonConverter.setContext(ops.context());
        final Polygon2D poly = regionToPolygonConverter.convert(component.getRegion(), Polygon2D.class);
        DefaultConvexHull2D convexHullCalculator = new DefaultConvexHull2D();
        Polygon2D polyHull = convexHullCalculator.calculate(poly);
        List<Double> xList = polyHull.vertices().stream().map(entry -> entry.getDoublePosition(0)).collect(Collectors.toList());
        List<Double> yList = polyHull.vertices().stream().map(entry -> entry.getDoublePosition(1)).collect(Collectors.toList());
        double[] x = ArrayUtils.toPrimitive(xList.toArray(new Double[0]));
        double[] y = ArrayUtils.toPrimitive(yList.toArray(new Double[0]));

        ValuePair<double[], double[]> res = getOrientedBoundingBoxCoordinates(x, y);
        Polygon2D orientedBoundingBoxPoly = GeomMasks.polygon2D(res.getA(), res.getB());
        return orientedBoundingBoxPoly;
    }

    public ValuePair<double[], double[]> getOrientedBoundingBoxCoordinates(double[] x, double[] y) {
        int numberOfCoords = x.length;

        double minimalArea = Double.MAX_VALUE;
        int indEdgeSourceMin = -1;
        int indEdgeTargetMin = -1;
        int indPerpendicularPointMin = -1;

        double min_hmin = 0.0;
        double min_hmax = 0.0;

        for (int indEdgeSource = 0; indEdgeSource < numberOfCoords; indEdgeSource++) {
            double distanceOfPerpendicularPointMax = 0.0;
            int indEdgeSourceMax = -1;
            int indEdgeTargetMax = -1;
            int indOfCoordWithMaxDistanceToEdge = -1;
            int indEdgeTarget;
            if (indEdgeSource < numberOfCoords - 1) indEdgeTarget = indEdgeSource + 1; /* handle last coordinate pair in (xp[np-1], yp[np-1]) */
            else indEdgeTarget = 0;

            for (int coordIndex = 0; coordIndex < numberOfCoords; coordIndex++) {
                double distanceOfCoordToEdge = Math.abs(perpDist(x[indEdgeSource], y[indEdgeSource], x[indEdgeTarget], y[indEdgeTarget], x[coordIndex], y[coordIndex]));
                if (distanceOfPerpendicularPointMax < distanceOfCoordToEdge) {
                    distanceOfPerpendicularPointMax = distanceOfCoordToEdge;
                    indOfCoordWithMaxDistanceToEdge = coordIndex; /* coordinate index of the coordinate with smallest perpendicular distance from the edge (xp[i], yp[i]) -> (xp[i2], yp[i2]) */
                    indEdgeSourceMax = indEdgeSource;
                    indEdgeTargetMax = indEdgeTarget;
                }
            }

            double hmin = 0.0;
            double hmax = 0.0;

            for (int k = 0; k < numberOfCoords; k++) { /* perform rotating calipers */
                double hd = parDist(x[indEdgeSourceMax], y[indEdgeSourceMax], x[indEdgeTargetMax], y[indEdgeTargetMax], x[k], y[k]);
                hmin = Math.min(hmin, hd);
                hmax = Math.max(hmax, hd);
            }

            double area = distanceOfPerpendicularPointMax * (hmax - hmin);

            if (minimalArea > area) {
                minimalArea = area;
                min_hmin = hmin;
                min_hmax = hmax;

                indEdgeSourceMin = indEdgeSourceMax;
                indEdgeTargetMin = indEdgeTargetMax;
                indPerpendicularPointMin = indOfCoordWithMaxDistanceToEdge;
            }
        }

        double pd = perpDist(x[indEdgeSourceMin], y[indEdgeSourceMin], x[indEdgeTargetMin], y[indEdgeTargetMin], x[indPerpendicularPointMin], y[indPerpendicularPointMin]); // signed feret diameter
        double pairAngle = Math.atan2(y[indEdgeTargetMin] - y[indEdgeSourceMin], x[indEdgeTargetMin] - x[indEdgeSourceMin]);
        double minAngle = pairAngle + Math.PI / 2;

        double[] nxp = new double[4];
        double[] nyp = new double[4];

        nxp[0] = x[indEdgeSourceMin] + Math.cos(pairAngle) * min_hmax;
        nyp[0] = y[indEdgeSourceMin] + Math.sin(pairAngle) * min_hmax;

        nxp[1] = nxp[0] + Math.cos(minAngle) * pd;
        nyp[1] = nyp[0] + Math.sin(minAngle) * pd;

        nxp[2] = nxp[1] + Math.cos(pairAngle) * (min_hmin - min_hmax);
        nyp[2] = nyp[1] + Math.sin(pairAngle) * (min_hmin - min_hmax);

        nxp[3] = nxp[2] + Math.cos(minAngle) * -pd;
        nyp[3] = nyp[2] + Math.sin(minAngle) * -pd;

        return new ValuePair<>(nxp, nyp);
    }

    private double distance(double x1, double y1, double x2, double y2) {
        return Math.sqrt(Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2));
    }

    private double perpDist(double p1x, double p1y, double p2x, double p2y, double x, double y) {
        // signed distance from a point (x,y) to a line passing through p1 and p2
        return ((p2x - p1x) * (y - p1y) - (x - p1x) * (p2y - p1y)) / distance(p1x, p1y, p2x, p2y);
    }

    private double parDist(double p1x, double p1y, double p2x, double p2y, double x, double y) {
        // signed projection of vector (x,y)-p1 into a line passing through p1 and p2
        return ((p2x - p1x) * (x - p1x) + (y - p1y) * (p2y - p1y)) / distance(p1x, p1y, p2x, p2y);
    }
}


