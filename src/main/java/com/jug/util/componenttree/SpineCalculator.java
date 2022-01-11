package com.jug.util.componenttree;

import com.jug.util.math.LinkedItem;
import com.jug.util.math.Vector2D;
import com.jug.util.math.Vector2DPolyline;
import org.apache.commons.lang.NotImplementedException;

public class SpineCalculator {
    public Vector2DPolyline calculate(Vector2DPolyline medialLine, Vector2DPolyline contour) {
        LinkedItem<Vector2D> linkedContour = contour.toCircularLinkedList();
        LinkedItem<Vector2D> linkedMedialLine = medialLine.toCircularLinkedList();

        throw new NotImplementedException();
    }

//    Vector2D calculateInterceptWithCounter(Vector2D startingPoint, Vector2D direction, Vector2DPolyline contour) {
//
//    }
//
//    Vector2D (LinkedItem<Vector2D> linkedMedialLine, LinkedItem<Vector2D> linkedContour){
//
//    }
}
