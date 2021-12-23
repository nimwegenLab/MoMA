package com.jug.util.math;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class Vector2DTest {
    @Test
    public void angle_of_90degrees_between_vectors_is_calculated_correctly() {
        double expectedAngle = Math.PI / 2;
        Vector2D verticalUnitVector = new Vector2D(0, 1);
        Vector2D horizontalUnitVector = new Vector2D(1, 0);
        double angleBetweenVectors = horizontalUnitVector.angleWith(verticalUnitVector);
        assertEquals(expectedAngle, angleBetweenVectors, 0.1);
    }

    @Test
    public void angle_of_45degrees_between_vectors_is_calculated_correctly() {
        double expectedAngle = Math.PI / 4;
        Vector2D verticalUnitVector = new Vector2D(0, 1);
        Vector2D horizontalUnitVector = new Vector2D(1, 1);
        double scalarProduct = verticalUnitVector.scalarProduct(horizontalUnitVector);
        double angleBetweenVectors = horizontalUnitVector.angleWith(verticalUnitVector);
        assertEquals(expectedAngle, angleBetweenVectors, 0.1);
    }

    @Test
    public void angle_of_minus_45degrees_between_vectors_is_calculated_correctly() {
        double tmp1 = Math.acos(1);
        double tmp2 = Math.acos(-1);
        double expectedAngle = Math.PI / 4;
        Vector2D verticalUnitVector = new Vector2D(0, 1);
        Vector2D horizontalUnitVector = new Vector2D(-1, 1);
        double scalarProduct = verticalUnitVector.scalarProduct(horizontalUnitVector);
        double angleBetweenVectors = horizontalUnitVector.angleWith(verticalUnitVector);
        assertEquals(expectedAngle, angleBetweenVectors, 0.1);
    }
}
