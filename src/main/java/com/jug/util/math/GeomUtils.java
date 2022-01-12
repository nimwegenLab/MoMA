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
     * Returns the two contour points firstContourPoint and secondContourPoint, which span the contour segment through
     * which the line startingPoint+t*orientationVector passes.
     * The function uses the fact that we have a radially closed contour. It calculates pairs of vectors
     * startingPoint->firstContourPoint and startingPoint->secondContourPoint and finds the vector pair whose angle
     * encloses the angle of the line startingPoint+t*orientationVector ('targetAngle').
     *
     * @param startingPoint
     * @param orientationVector
     * @param linkedContour
     * @return
     */
    public static ValuePair<Vector2D, Vector2D> getPointsOfInterceptingContourSegment(Vector2D startingPoint, Vector2D orientationVector, LinkedItem<Vector2D> linkedContour, int maxSearchIterations){
        double targetAngle = orientationVector.getPolarAngle();

        LinkedItem<Vector2D> currentLinkedItem = linkedContour;
        LinkedItem<Vector2D> nextLinkedItem;
        Vector2D firstContourPoint = null;
        Vector2D secondContourPoint = null;
        double angleCurr;
        double angleNext;
        boolean successFlag = false;
        for (int counter = 0; counter < maxSearchIterations; counter++) {
            firstContourPoint = currentLinkedItem.getElement();
            Vector2D currentRadialVector = firstContourPoint.minus(startingPoint);
            angleCurr = currentRadialVector.getPolarAngle();
            nextLinkedItem = currentLinkedItem.next();
            secondContourPoint = nextLinkedItem.getElement();
            Vector2D nextRadialVector = secondContourPoint.minus(startingPoint);
            angleNext = nextRadialVector.getPolarAngle();
            currentLinkedItem = nextLinkedItem;
            if(angleNext >= targetAngle && angleCurr <= targetAngle ){
                successFlag = true;
                break;
            }
        }
        if(!successFlag) {throw new RuntimeException("no point pair was found that enclose the target vector 'targetVector'");}
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
}
