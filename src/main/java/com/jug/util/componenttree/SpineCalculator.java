package com.jug.util.componenttree;

import com.jug.util.math.GeomUtils;
import com.jug.util.math.LinkedItem;
import com.jug.util.math.Vector2D;
import com.jug.util.math.Vector2DPolyline;
import net.imglib2.util.ValuePair;
import org.apache.commons.lang.NotImplementedException;

import java.util.List;
import java.util.stream.Collectors;

public class SpineCalculator {
    public Vector2DPolyline calculate(Vector2DPolyline medialLine, Vector2DPolyline contour, int pointsToAverage) {
        if (medialLine.size() < pointsToAverage) {
            pointsToAverage = 1;
        }
        LinkedItem<Vector2D> linkedContour = contour.toCircularLinkedList();
        LinkedItem<Vector2D> linkedMedialLine = medialLine.toCircularLinkedList();

        List<Vector2D> diffs = GeomUtils.differences(medialLine.getVectorList());
        List<Vector2D> diffsAtStart = diffs.stream().skip(0).limit(pointsToAverage).collect(Collectors.toList());

//        medialLine.getVectorList().stream()
//        List<Vector2D> res = medialLine.getVectorList().stream().skip(0).limit(pointsToAverage).collect(Collectors.toList());
        Vector2D avgDirectionAtStart = GeomUtils.averageVectors(diffsAtStart).multiply(-1.0);

        ValuePair<Vector2D, Vector2D> interceptLinePoints = getPointsOfContourLineToIntercept(medialLine.get(0), avgDirectionAtStart, linkedContour);

//        medialLine.getVectorList().stream().skip(medialLine.size()-pointsToAverage).limit(pointsToAverage) // do this at end of contour

        throw new NotImplementedException();
    }

    Vector2D calculateInterceptWithCounter(Vector2D startingPoint, Vector2D direction, LinkedItem<Vector2D> linkedContour) {

        throw new NotImplementedException();
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
    ValuePair<Vector2D, Vector2D> getPointsOfContourLineToIntercept(Vector2D startingPoint, Vector2D orientationVector, LinkedItem<Vector2D> linkedContour){
        double targetAngle = orientationVector.getPolarAngle();

        LinkedItem<Vector2D> currentLinkedItem = linkedContour;
        LinkedItem<Vector2D> nextLinkedItem;
        Vector2D firstContourPoint = null;
        Vector2D secondContourPoint = null;
        double angleCurr;
        double angleNext;
        int counter= 0;
        boolean successFlag = false;
        while(counter < 100) {
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

//    List<Vector2D> getPointsOfContourLineToIntercept(Vector2D startingPoint, Vector2D direction, Vector2DPolyline contour){
//        double targetAngle = direction.getPolarAngle();
//
////        List<Double> angles = new ArrayList();
//        List<Vector2D> vectList = contour.getVectorList();
//        int nrVects = vectList.size();
//        int index1 = 0;
//        int index2 = nrVects;
//        while (true) {
////            Vector2D vect = vectList.get(index);
//            double angle1 = vectList.get(index1).minus(startingPoint).getPolarAngle();
//            double absDiff1 = Math.abs(targetAngle - angle1);
//            double angle2 = vectList.get(index2).minus(startingPoint).getPolarAngle();
//            double absDiff2 = Math.abs(targetAngle - angle2);
//            if (absDiff1 <= absDiff2) {
//                absDiff1
//            }
////            angles.add(vect.getPolarAngle());
//            break;
//        }
////        Object res = contour.getVectorList().stream().forEach(vect -> vect.getPolarAngle()).collect(Collectors.toList());
//
//        throw new NotImplementedException();
//    }

//
//    Vector2D (LinkedItem<Vector2D> linkedMedialLine, LinkedItem<Vector2D> linkedContour){
//
//    }
}
