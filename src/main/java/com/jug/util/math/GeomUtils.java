package com.jug.util.math;

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
}
