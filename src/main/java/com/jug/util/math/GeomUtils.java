package com.jug.util.math;

import net.imglib2.RealLocalizable;
import net.imglib2.util.ValuePair;

import java.util.ArrayList;
import java.util.List;

public class GeomUtils {
    public static Vector2D averageVectors(List<Vector2D> vectors) {
        Vector2D v = new Vector2D(0, 0);
        for (Vector2D vect : vectors) {
            v.plusMutate(vect);
        }
        return v.multiply(1d / vectors.size());
    }

    public static List<Vector2D> differences(List<Vector2D> vectors) {
        List<Vector2D> diffs = new ArrayList<>();
        for (int i = 0; i < vectors.size() - 1; i++) {
            diffs.add(vectors.get(i + 1).minus(vectors.get(i)));
        }
        return diffs;
    }

    /**
     * Smooth the `vectors` by performing a moving average with user-defined 'windowSize'.
     * @param vectors
     * @param windowSize
     * @return
     */
    public static Vector2DPolyline smooth(Vector2DPolyline vectors, int windowSize) {
        if (windowSize % 2 == 0) {
            throw new RuntimeException("windowSize needs to be uneven integer");
        }
        int halfWindow = windowSize / 2;
        Vector2DPolyline smoothedVectors = new Vector2DPolyline();
        for (int i = halfWindow; i < vectors.size() - halfWindow; i++) {
            Vector2D averageOfWindow = GeomUtils.averageVectors(vectors.getVectorList().subList(i - halfWindow, i + halfWindow + 1));
            smoothedVectors.add(averageOfWindow);
        }
        return smoothedVectors;
    }

    /**
     * Symetrically smooth Vector2DPolyline vectors with windows sizes between minWindowSize/2 and maxWindowSize/2.
     * The smoothing will be performed using maxWindowSize, wherever there are enough points to average around the
     * current position. When there are not enough points, the number of averaged points will be reduced.
     * We stop averaging, when the number of points is below minWindowSize/2.
     * Points at the start and end with less neighbors than minWindowSize/2 will not be considered.
     *
     * @param vectors
     * @param minWindowSize
     * @return
     */
    public static Vector2DPolyline smoothWithAdaptiveWindowSize(Vector2DPolyline vectors, int minWindowSize, int maxWindowSize) {
        if (minWindowSize % 2 == 0) {
            throw new RuntimeException("minWindowSize needs to be uneven integer");
        }
        if (maxWindowSize % 2 == 0) {
            throw new RuntimeException("maxWindowSize needs to be uneven integer");
        }
        int minWindowSizeHalfed = minWindowSize / 2;
        int maxWindowSizeHalfed = maxWindowSize / 2;
        Vector2DPolyline smoothedVectors = new Vector2DPolyline();
        for (int i = minWindowSizeHalfed; i < vectors.size() - minWindowSizeHalfed; i++) {
            int currentWindowSizeHalfed = maxWindowSizeHalfed;
            if(i < currentWindowSizeHalfed || (vectors.size() - 1 - i) < currentWindowSizeHalfed){
                currentWindowSizeHalfed = Math.min(i, vectors.size() - 1 - i);
            }
            Vector2D averageOfWindow = GeomUtils.averageVectors(vectors.getVectorList().subList(i - currentWindowSizeHalfed, i + currentWindowSizeHalfed + 1));
            smoothedVectors.add(averageOfWindow);
        }
        return smoothedVectors;
    }

    /**
     * Returns the two contour points firstContourPoint and secondContourPoint, which span the contour segment through
     * which the line startingPoint+t*orientationVector passes.
     * The function uses the fact that we have a radially closed contour. It calculates pairs of vectors
     * startingPoint->firstContourPoint and startingPoint->secondContourPoint and finds the vector pair whose angle
     * encloses the angle of the line startingPoint+t*orientationVector ('targetAngle').
     *
     * @param pointOnMedialLine
     * @param lineOrientationVector
     * @param linkedContour
     * @return
     */
    public static ValuePair<Vector2D, Vector2D> getPointsOfInterceptingContourSegment(Vector2D pointOnMedialLine, Vector2D lineOrientationVector, LinkedItem<Vector2D> linkedContour, int maxSearchIterations){
        double targetAngle = lineOrientationVector.getPolarAngle();
        LinkedItem<Vector2D> currentLinkedItem = linkedContour;
        LinkedItem<Vector2D> nextLinkedItem;
        Vector2D firstContourPoint = null;
        Vector2D secondContourPoint = null;
        double firstAngle;
        double secondAngle;
        boolean successFlag = false;
        for (int counter = 0; counter < maxSearchIterations; counter++) {
            firstContourPoint = currentLinkedItem.getElement();
            Vector2D firstRadialVector = firstContourPoint.minus(pointOnMedialLine);
            firstAngle = firstRadialVector.getPolarAngle();
            nextLinkedItem = currentLinkedItem.next();
            secondContourPoint = nextLinkedItem.getElement();
            Vector2D secondRadialVector = secondContourPoint.minus(pointOnMedialLine);
            secondAngle = secondRadialVector.getPolarAngle();
            currentLinkedItem = nextLinkedItem;
            if(secondAngle >= targetAngle && firstAngle <= targetAngle ){
                successFlag = true;
                break;
            }
        }
        if(!successFlag) {
            throw new RuntimeException("no point pair was found that enclose the target vector 'targetVector'");
        }
        return new ValuePair<>(firstContourPoint, secondContourPoint);
    }

//    public static Vector2D calculateInterceptWithContourNew(Vector2D pointOnMedialLine, Vector2D lineOrientationVector, Vector2DPolyline contour) {
//        return GeomUtils.calculateInterceptWithContourNewSubfunction(pointOnMedialLine, lineOrientationVector, contour);
//    }

    public static Vector2D calculateInterceptWithContourNew(Vector2D pointOnMedialLine, Vector2D lineOrientationVector, Vector2DPolyline contour){
        int maxSearchIterations = contour.size() + 1; /* number of iterations is increased by +1, because we iterate over the closed contour, so we need to extend the iteration to include the segment between the last and first points in the contour */
        LinkedItem<Vector2D> linkedContour = contour.toCircularLinkedList();
        double targetAngle = lineOrientationVector.getPolarAngle();
        LinkedItem<Vector2D> currentLinkedItem = linkedContour;
        LinkedItem<Vector2D> nextLinkedItem;
        Vector2D firstContourPoint = null;
        Vector2D secondContourPoint = null;
        double firstAngle;
        double secondAngle;
        for (int counter = 0; counter < maxSearchIterations; counter++) {
            firstContourPoint = currentLinkedItem.getElement();
            Vector2D firstRadialVector = firstContourPoint.minus(pointOnMedialLine);
            firstAngle = firstRadialVector.getPolarAngle();
            nextLinkedItem = currentLinkedItem.next();
            secondContourPoint = nextLinkedItem.getElement();
            Vector2D secondRadialVector = secondContourPoint.minus(pointOnMedialLine);
            secondAngle = secondRadialVector.getPolarAngle();
            currentLinkedItem = nextLinkedItem;
            if(secondAngle >= targetAngle && firstAngle <= targetAngle ){
                Vector2D basePoint2 = firstContourPoint;
                Vector2D orientationVector2 = secondContourPoint.minus(basePoint2);
                return calculateLineLineIntercept(pointOnMedialLine, lineOrientationVector, basePoint2, orientationVector2);
            }
        }
        throw new RuntimeException("no point pair was found that enclose the target vector 'targetVector'");
    }

    public static Vector2D calculateInterceptWithContour(LinkedItem<Vector2D> linkedContour, Vector2D orientationVector1, Vector2D basePoint1) {
        ValuePair<Vector2D, Vector2D> pointsOfInterceptingContourSegment = GeomUtils.getPointsOfInterceptingContourSegment(basePoint1, orientationVector1, linkedContour);
        Vector2D basePoint2 = pointsOfInterceptingContourSegment.getA();
        Vector2D tmp = pointsOfInterceptingContourSegment.getB();
        Vector2D orientationVector2 = tmp.minus(basePoint2);
        return calculateLineLineIntercept(basePoint1, orientationVector1, basePoint2, orientationVector2);
    }

    public static ValuePair<Vector2D, Vector2D> getPointsOfInterceptingContourSegment(Vector2D startingPoint, Vector2D orientationVector, LinkedItem<Vector2D> linkedContour) {
        return getPointsOfInterceptingContourSegment(startingPoint, orientationVector, linkedContour, 10000);
    }

    /**
     * Check that the determinant of the two vectors vec1 and vec2 is zero to within tolerance.
     * @param vec1
     * @param vec2
     * @param tolerance
     * @return
     */
    public static boolean vectorsAreColinear(Vector2D vec1, Vector2D vec2, double tolerance){
        double determinant = Math.abs(vec1.getX() * vec2.getY() - vec1.getY() * vec2.getX());
        return determinant < tolerance;
    }

    /** Solve intersect of two lines:
     * l_i = /x_i\ + a * /u_i\
     *       \y_i/       \v_i/
     * setting up linear equation system and solving using Cramers rule:
     * /u1  -u2\ /a\ = / x2 - x1\ := /b1\
     * \v1  -v2/ \b/   \ y2 - x2/    \b2/
     */
    public static Vector2D calculateLineLineIntercept(Vector2D basePointLine1, Vector2D orientationLine1, Vector2D basePointLine2, Vector2D orientationLine2) {
        double tol = 1E-5;
        boolean vectorsAreCollinear = Math.abs(orientationLine1.getX() / orientationLine1.getY() - orientationLine2.getX() / orientationLine2.getY()) < tol;
//        boolean vectorsAreCollinear2 = vectorsAreColinear(orientationLine1, orientationLine2, tol);
        if (vectorsAreCollinear) {
            throw new RuntimeException("cannot calculate intercept for collinear vectors");
        }

        double b1 = basePointLine2.getX() - basePointLine1.getX();
        double b2 = basePointLine2.getY() - basePointLine1.getY();
        double u1 = orientationLine1.getX();
        double v1 = orientationLine1.getY();
        double u2 = orientationLine2.getX();
        double v2 = orientationLine2.getY();

        double a = (-b1 * v2 + u2 * b2) / (-u1 * v2 + u2 * v1); /* use Cramers rule to determine factor for rescaling orientationLine1 */

        orientationLine1.multiplyMutate(a);
        return basePointLine1.plus(orientationLine1);
    }

    /**
     * Checks if {point} lies inside {polygon} using ray-casting.
     * This code was adapted from here: https://stackoverflow.com/a/16391873/653770
     *
     * @param point
     * @param polygon
     * @return
     */
    public boolean IsPointInPolygon(Vector2D point, Vector2DPolyline polygon, boolean checkBoundingBoxFirst) {
        if (checkBoundingBoxFirst) {
            /* First check if point is inside bounding-box of polygon */
            double minX = polygon.get(0).getX();
            double maxX = polygon.get(0).getX();
            double minY = polygon.get(0).getY();
            double maxY = polygon.get(0).getY();
            for (int i = 1; i < polygon.size(); i++) {
                Vector2D q = polygon.get(i);
                minX = Math.min(q.getX(), minX);
                maxX = Math.max(q.getX(), maxX);
                minY = Math.min(q.getY(), minY);
                maxY = Math.max(q.getY(), maxY);
            }
            if (point.getX() < minX || point.getX() > maxX || point.getY() < minY || point.getY() > maxY) {
                return false;
            }
        }

        /* now check, if point is actually inside the polygon using ray-casting */
        // https://wrf.ecse.rpi.edu/Research/Short_Notes/pnpoly.html
        boolean inside = false;
        for (int i = 0, j = polygon.size() - 1; i < polygon.size(); j = i++) {
            if ((polygon.get(i).getY() > point.getY()) != (polygon.get(j).getY() > point.getY()) &&
                    point.getX() < (polygon.get(j).getX() - polygon.get(i).getX()) * (point.getY() - polygon.get(i).getY()) / (polygon.get(j).getY() - polygon.get(i).getY()) + polygon.get(i).getX()) {
                inside = !inside;
            }
        }

        return inside;
    }

    public static double distance(RealLocalizable point1, RealLocalizable point2) {
        return distance(point1.getDoublePosition(0), point1.getDoublePosition(1), point2.getDoublePosition(0), point2.getDoublePosition(1));
    }

    public static double distance(double x1, double y1, double x2, double y2) {
        return Math.sqrt(Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2));
    }

    public static double perpDist(double p1x, double p1y, double p2x, double p2y, double x, double y) {
        // signed distance from a point (x,y) to a line passing through p1 and p2
        return ((p2x - p1x) * (y - p1y) - (x - p1x) * (p2y - p1y)) / distance(p1x, p1y, p2x, p2y);
    }

    public static double parDist(double p1x, double p1y, double p2x, double p2y, double x, double y) {
        // signed projection of vector (x,y)-p1 into a line passing through p1 and p2
        return ((p2x - p1x) * (x - p1x) + (y - p1y) * (p2y - p1y)) / distance(p1x, p1y, p2x, p2y);
    }

    public static void filterPolylineAngles(Vector2DPolyline polyline, double minAllowedAngle) {
        boolean pointWasRemoved = true;
        while (pointWasRemoved) {
            pointWasRemoved = removeVerticesWithStrongAngles(polyline, minAllowedAngle); /* when we remove a point, the resulting polyline could then contain angles, which are not allowed. We repeat the process in this case. */
        }
    }

    public static boolean removeVerticesWithStrongAngles(Vector2DPolyline polyline, double minAllowedAngle) {
        boolean pointWasRemoved = false;
        for (int ind = 1; ind < polyline.size() - 2; ind++) {
            Vector2D pointBefore = polyline.get(ind - 1);
            Vector2D pointCurrent = polyline.get(ind);
            Vector2D pointNext = polyline.get(ind + 1);
            Vector2D firstVector = pointBefore.minus(pointCurrent);
            Vector2D secondVector = pointNext.minus(pointCurrent);
            double currentAngle = firstVector.angleWith(secondVector);
//            System.out.println("currentAngle: " + currentAngle);
            if (currentAngle <= minAllowedAngle) {
                polyline.remove(pointCurrent);
                pointWasRemoved = true;
            }
//              System.out.println("----");
        }
//        System.out.println("stop");
        return pointWasRemoved;
    }
}
