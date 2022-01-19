package com.jug.util.componenttree;

import com.jug.util.math.GeomUtils;
import com.jug.util.math.LinkedItem;
import com.jug.util.math.Vector2D;
import com.jug.util.math.Vector2DPolyline;
import net.imagej.ops.geom.geom2d.DefaultBoundingBox;
import net.imglib2.roi.geom.real.Polygon2D;
import net.imglib2.util.ValuePair;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class SpineCalculator {
    private DefaultBoundingBox boundingBoxCalculator;

    int orientationVectorAveragingWindowSize;
    double minimalVerticalDistanceFromStartAndEnd;
    private Function<Vector2DPolyline, Vector2DPolyline> medialLinePreprocesser;

    public SpineCalculator(int orientationVectorAveragingWindowSize, double minimalVerticalDistanceFromStartAndEnd, Function<Vector2DPolyline, Vector2DPolyline> medialLinePreprocesser) {
        this.medialLinePreprocesser = medialLinePreprocesser;
        boundingBoxCalculator = new DefaultBoundingBox();
        this.orientationVectorAveragingWindowSize = orientationVectorAveragingWindowSize;
        this.minimalVerticalDistanceFromStartAndEnd = minimalVerticalDistanceFromStartAndEnd;
    }

    public SpineCalculator(int positionAveragingWindowSize, int orientationVectorAveragingWindowSize, double minimalVerticalDistanceFromStartAndEnd) {
        this(orientationVectorAveragingWindowSize, minimalVerticalDistanceFromStartAndEnd, getDefaultMedialLinePreprocessor(positionAveragingWindowSize));
    }

    public static Function<Vector2DPolyline, Vector2DPolyline> getDefaultMedialLinePreprocessor(int positionAveragingWindowSize) {
        return (medialLine) -> GeomUtils.smooth(medialLine, positionAveragingWindowSize);
    }

    public Vector2DPolyline calculate(Vector2DPolyline medialLine, Vector2DPolyline contour, ValuePair<Integer, Integer> imageLimitsYdirection) {
        if(orientationVectorAveragingWindowSize<2){
            throw new IllegalArgumentException("orientationVectorAveragingWindowSize must be >2");
        }

//        System.out.println("medialLine.size: " + medialLine.size());

        Vector2DPolyline spine = medialLine.copy();

        removeMedialLinePointsAtStartAndEnd(spine, contour, minimalVerticalDistanceFromStartAndEnd);

//        System.out.println("spine.size after removal: " + spine.size());
//        System.out.println("orientationVectorAveragingWindowSize: " + orientationVectorAveragingWindowSize);

//        if (spine.size() - positionAveragingWindowSize < 10) {
//            positionAveragingWindowSizeCurrent = 0;
//        }

//            spine = GeomUtils.smooth(spine, positionAveragingWindowSizeCurrent);
//            spine = GeomUtils.smoothWithAdaptiveWindowSize(spine, positionAveragingWindowSizeCurrent, positionAveragingWindowSizeCurrent);
        spine = medialLinePreprocesser.apply(spine);

//        System.out.println("spine.size after smoothing: " + spine.size());

        LinkedItem<Vector2D> linkedContour = contour.toCircularLinkedList();

        List<Vector2D> diffs = GeomUtils.differences(spine.getVectorList());

//        System.out.println("diffs.size: " + diffs.size());
//        if(spine.size() == 0){
//            System.out.println("stop");
//        }

        int orientationVectorAveragingWindowSizeCurrent = orientationVectorAveragingWindowSize;
//        if (spine.size() - orientationVectorAveragingWindowSize < 10) {
//            orientationVectorAveragingWindowSizeCurrent = 1;
//        }

        Vector2D basePoint1 = spine.get(0);
        if(Math.round(basePoint1.getY()) != imageLimitsYdirection.getA()){ /* this is catches the situation, where the medial line starts on the image-boundary; this happens for components that sit on the image-boundary */
            List<Vector2D> diffsAtStart = diffs.subList(0, orientationVectorAveragingWindowSizeCurrent);
            Vector2D orientationVector1 = diffsAtStart.size() > 1 ? GeomUtils.averageVectors(diffsAtStart).multiply(-1.0) : diffsAtStart.get(0); /* average only, if the window-size permits it */
            Vector2D result1 = GeomUtils.calculateInterceptWithContour(linkedContour, orientationVector1, basePoint1);
            spine.add(0, result1);
        }

        Vector2D basePoint2 = spine.get(spine.size() - 1);
        if(Math.round(basePoint2.getY()) != imageLimitsYdirection.getB()) { /* this is catches the situation, where the medial line starts on the image-boundary; this happens for components that sit on the image-boundary */
            List<Vector2D> diffsAtEnd = diffs.subList(diffs.size() - 1 - orientationVectorAveragingWindowSizeCurrent, diffs.size() - 1);
            Vector2D orientationVector2 = diffsAtEnd.size() > 1 ? GeomUtils.averageVectors(diffsAtEnd) : diffsAtEnd.get(diffsAtEnd.size() - 1); /* average only, if the window-size permits it */
            Vector2D result2 = GeomUtils.calculateInterceptWithContour(linkedContour, orientationVector2, basePoint2);
            spine.add(result2);
        }

        return spine;
    }

    private void removeMedialLinePointsAtStartAndEnd(Vector2DPolyline medialLine, Vector2DPolyline contour, double maxAllowedVerticalDistance){
        Polygon2D bbox = boundingBoxCalculator.calculate(contour.getPolygon2D());
        double min_y = bbox.vertices().get(0).getDoublePosition(1);
        double max_y = bbox.vertices().get(1).getDoublePosition(1);
        ArrayList<Vector2D> pointsToRemove = new ArrayList<>();
        for (Vector2D vect : medialLine.getVectorList()) {
            double distanceFromTop = Math.abs(vect.getY() - min_y);
            double distanceFromBottom = Math.abs(max_y - vect.getY());
            if ((distanceFromTop < maxAllowedVerticalDistance) ||
                (distanceFromBottom < maxAllowedVerticalDistance)) {
                pointsToRemove.add(vect);
            }
        }
        for (Vector2D vect : pointsToRemove) {
            medialLine.remove(vect);
        }
    }
}
