package com.jug.util.componenttree;

import com.jug.util.math.GeomUtils;
import com.jug.util.math.LinkedItem;
import com.jug.util.math.Vector2D;
import com.jug.util.math.Vector2DPolyline;
import org.apache.commons.lang.NotImplementedException;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

//        calculateInterceptWithCounter(medialLine.get(0), Vector2D direction, Vector2DPolyline contour)

//        medialLine.getVectorList().stream().skip(medialLine.size()-pointsToAverage).limit(pointsToAverage) // do this at end of contour

        throw new NotImplementedException();
    }

    Vector2D calculateInterceptWithCounter(Vector2D startingPoint, Vector2D direction, Vector2DPolyline contour) {

        throw new NotImplementedException();
    }
//
//    Vector2D (LinkedItem<Vector2D> linkedMedialLine, LinkedItem<Vector2D> linkedContour){
//
//    }
}
