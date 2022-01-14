package com.jug.util.math;

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
        for (int i = halfWindow; i < vectors.size() - 1 - halfWindow; i++) {
            double x = 0, y = 0;
            for (int j = i - halfWindow; j < i + halfWindow + 1; j++) {
                x += vectors.get(j).getX();
                y += vectors.get(j).getY();
            }
            smoothedVectors.add(new Vector2D(x / windowSize, y / windowSize));
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

    public static ValuePair<Vector2D, Vector2D> getPointsOfInterceptingContourSegment(Vector2D startingPoint, Vector2D orientationVector, LinkedItem<Vector2D> linkedContour) {
        return getPointsOfInterceptingContourSegment(startingPoint, orientationVector, linkedContour, 10000);
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
}
