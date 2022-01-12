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
        int counter= 0;
        boolean successFlag = false;
        while(counter < maxSearchIterations) {
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
            counter++;
        }
        if(!successFlag) {throw new RuntimeException("no point pair was found that enclose the target vector 'targetVector'");}
        return new ValuePair<>(firstContourPoint, secondContourPoint);
    }

    public static ValuePair<Vector2D, Vector2D> getPointsOfInterceptingContourSegment(Vector2D startingPoint, Vector2D orientationVector, LinkedItem<Vector2D> linkedContour) {
        return getPointsOfInterceptingContourSegment(startingPoint, orientationVector, linkedContour, 10000);
    }
}
