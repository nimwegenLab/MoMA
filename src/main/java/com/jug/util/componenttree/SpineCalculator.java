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
            Vector2D orientationVector1 = diffsAtStart.size() > 1 ? GeomUtils.averageVectors(diffsAtStart).multiply(-1.0) : diffsAtStart.get(0); /* average only, if the window-size permits it */
            Vector2D result1 = calculateInterceptWithContour(linkedContour, orientationVector1, basePoint1);
            spine.add(0, result1);
        }

        Vector2D basePoint2 = medialLine.get(medialLine.size() - 1);
        if(Math.round(basePoint2.getY()) != imageLimitsYdirection.getB()) { /* this is catches the situation, where the medial line starts on the image-boundary; this happens for components that sit on the image-boundary */
            List<Vector2D> diffsAtEnd = diffs.subList(diffs.size() - 1 - orientationVectorAveragingWindowSize, diffs.size() - 1);
            Vector2D orientationVector2 = diffsAtEnd.size() > 1 ? GeomUtils.averageVectors(diffsAtEnd) : diffsAtEnd.get(diffsAtEnd.size() - 1); /* average only, if the window-size permits it */
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
}
