package com.jug.util.componenttree;

import com.jug.util.math.GeomUtils;
import com.jug.util.math.LinkedItem;
import com.jug.util.math.Vector2D;
import com.jug.util.math.Vector2DPolyline;
import net.imglib2.util.ValuePair;
import org.apache.commons.lang.NotImplementedException;

import java.util.List;

import static com.jug.util.math.GeomUtils.calculateLineLineIntercept;

public class SpineCalculator {
    public Vector2DPolyline calculate(Vector2DPolyline medialLine, Vector2DPolyline contour, int positionAveragingWindowSize, int orientationVectorAveragingWindowSize, ValuePair<Integer, Integer> imageLimitsYdirection) {
        if(orientationVectorAveragingWindowSize<2){
            throw new IllegalArgumentException("orientationVectorAveragingWindowSize must be >2");
        }

        if (medialLine.size() < orientationVectorAveragingWindowSize) {
            orientationVectorAveragingWindowSize = 1;
        }
        if (positionAveragingWindowSize > 0) {
            medialLine = GeomUtils.smooth(medialLine, positionAveragingWindowSize);
        }
        LinkedItem<Vector2D> linkedContour = contour.toCircularLinkedList();

        List<Vector2D> diffs = GeomUtils.differences(medialLine.getVectorList());

        Vector2DPolyline spine = medialLine.copy();

        Vector2D basePoint1 = medialLine.get(0);
        if(Math.round(basePoint1.getY()) != imageLimitsYdirection.getA()){ /* this is catches the situation, where the medial line starts on the image-boundary; this happens for components that sit on the image-boundary */
            List<Vector2D> diffsAtStart = diffs.subList(0, orientationVectorAveragingWindowSize);
            Vector2D orientationVector1 = GeomUtils.averageVectors(diffsAtStart).multiply(-1.0); /* multiply(-1.0): we invert direction because diffs will point towards the center of the medial line */
            Vector2D result1 = calculateInterceptWithContour(linkedContour, orientationVector1, basePoint1);
            spine.add(0, result1);
        }

        Vector2D basePoint2 = medialLine.get(medialLine.size() - 1);
        if(Math.round(basePoint2.getY()) != imageLimitsYdirection.getB()) { /* this is catches the situation, where the medial line starts on the image-boundary; this happens for components that sit on the image-boundary */
            List<Vector2D> diffsAtEnd = diffs.subList(diffs.size() - 1 - orientationVectorAveragingWindowSize, diffs.size() - 1);
            Vector2D orientationVector2 = GeomUtils.averageVectors(diffsAtEnd);
            Vector2D result2 = calculateInterceptWithContour(linkedContour, orientationVector2, basePoint2);
            spine.add(result2);
        }

        return spine;
    }

    private Vector2D calculateInterceptWithContour(LinkedItem<Vector2D> linkedContour, Vector2D orientationVector1, Vector2D basePoint1) {
        ValuePair<Vector2D, Vector2D> pointsOfInterceptingContourSegment = GeomUtils.getPointsOfInterceptingContourSegment(basePoint1, orientationVector1, linkedContour);
        Vector2D basePoint2 = pointsOfInterceptingContourSegment.getA();
        Vector2D tmp = pointsOfInterceptingContourSegment.getB();
        Vector2D orientationVector2 = tmp.minus(basePoint2);
        return calculateLineLineIntercept(basePoint1, orientationVector1, basePoint2, orientationVector2);
    }


    Vector2D calculateInterceptWithLineSegment(Vector2D segmentPoint1, Vector2D segmentPoint2, Vector2D startingPoint, Vector2D direction) {
        throw new NotImplementedException();
    }

//    List<Vector2D> getPointsOfInterceptingContourSegment(Vector2D startingPoint, Vector2D direction, Vector2DPolyline contour){
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
