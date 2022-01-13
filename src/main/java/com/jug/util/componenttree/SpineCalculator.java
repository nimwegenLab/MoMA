package com.jug.util.componenttree;

import com.jug.util.math.GeomUtils;
import com.jug.util.math.LinkedItem;
import com.jug.util.math.Vector2D;
import com.jug.util.math.Vector2DPolyline;
import net.imglib2.util.ValuePair;
import org.apache.commons.lang.NotImplementedException;

import java.util.List;
import java.util.stream.Collectors;

import static com.jug.util.math.GeomUtils.calculateLineLineIntercept;

public class SpineCalculator {
    public Vector2DPolyline calculate(Vector2DPolyline medialLine, Vector2DPolyline contour, int positionAveragingWindowSize, int orientationVectorAveragingWindowSize) {
        if (medialLine.size() < orientationVectorAveragingWindowSize) {
            orientationVectorAveragingWindowSize = 1;
        }

        medialLine = GeomUtils.smooth(medialLine, positionAveragingWindowSize);
//        if(true){return medialLine; }
        LinkedItem<Vector2D> linkedContour = contour.toCircularLinkedList();

        List<Vector2D> diffs = GeomUtils.differences(medialLine.getVectorList());
        List<Vector2D> diffsAtStart = diffs.stream().skip(0).limit(orientationVectorAveragingWindowSize).collect(Collectors.toList());

//        medialLine.getVectorList().stream()
//        List<Vector2D> res = medialLine.getVectorList().stream().skip(0).limit(pointsToAverage).collect(Collectors.toList());
        Vector2D orientationVector1 = GeomUtils.averageVectors(diffsAtStart).multiply(-1.0); /* multiply(-1.0): we invert direction because diffs will point towards the center of the medial line */
        Vector2D basePoint1 = medialLine.get(0);
        Vector2D result1 = calculateInterceptWithContour(linkedContour, orientationVector1, basePoint1);

//             medialLine.getVectorList().stream().skip(medialLine.size()-pointsToAverage).limit(pointsToAverage) // do this at end of contour
//        List<Vector2D> vectorsAtEnd = medialLine.getVectorList().stream().skip(medialLine.size()-pointsToAverage).limit(pointsToAverage).collect(Collectors.toList());
//        List<Vector2D> diffsAtEnd = diffs.stream().skip(medialLine.size()-pointsToAverage).limit(pointsToAverage).collect(Collectors.toList());
//        Vector2D avgDirectionAtEnd = GeomUtils.averageVectors(diffsAtEnd);
//        ValuePair<Vector2D, Vector2D> pointsOfInterceptingContourSegmentAtEnd = GeomUtils.getPointsOfInterceptingContourSegment(medialLine.get(medialLine.size()-1), avgDirectionAtEnd, linkedContour);
        List<Vector2D> diffsAtEnd = diffs.stream().skip(medialLine.size()-orientationVectorAveragingWindowSize).limit(orientationVectorAveragingWindowSize).collect(Collectors.toList());
        Vector2D basePoint2 = medialLine.get(medialLine.size() - 1);
        Vector2D orientationVector2 = GeomUtils.averageVectors(diffsAtEnd);
        Vector2D result2 = calculateInterceptWithContour(linkedContour, orientationVector2, basePoint2);

        Vector2DPolyline spine = medialLine.copy();
        spine.add(0, result1);
        spine.add(result2);
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
