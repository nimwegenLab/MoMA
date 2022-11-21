package com.jug.util.math;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Vector2DTest {
    private double delta = 0.01;

    @Test
    public void angle_of_90degrees_between_vectors_is_calculated_correctly() {
        double expectedAngle = Math.PI / 2;
        Vector2D verticalUnitVector = new Vector2D(0, 1);
        Vector2D horizontalUnitVector = new Vector2D(1, 0);
        double angleBetweenVectors = horizontalUnitVector.angleWith(verticalUnitVector);
        assertEquals(expectedAngle, angleBetweenVectors, delta);
    }

    @Test
    public void angle_of_45degrees_between_vectors_is_calculated_correctly() {
        double expectedAngle = Math.PI / 4;
        Vector2D verticalUnitVector = new Vector2D(0, 1);
        Vector2D horizontalUnitVector = new Vector2D(1, 1);
        double angleBetweenVectors = horizontalUnitVector.angleWith(verticalUnitVector);
        assertEquals(expectedAngle, angleBetweenVectors, delta);
    }

    @Test
    public void angle_of_minus_45degrees_between_vectors_is_calculated_correctly() {
        double expectedAngle = Math.PI / 4;
        Vector2D verticalUnitVector = new Vector2D(0, 1);
        Vector2D horizontalUnitVector = new Vector2D(-1, 1);
        double angleBetweenVectors = horizontalUnitVector.angleWith(verticalUnitVector);
        assertEquals(expectedAngle, angleBetweenVectors, delta);
    }

    @Test
    public void polar_angle_is_calculated_correctly_for_unit_vector_in_positive_y_direction(){
        double expectedAngle = Math.PI / 2;
        Vector2D verticalUnitVector = new Vector2D(0, 1);
        assertEquals(expectedAngle, verticalUnitVector.getPolarAngle(), delta);
    }

    @Test
    public void polar_angle_is_calculated_correctly_for_unit_vector_in_negative_y_direction(){
        double expectedAngle = -Math.PI / 2;
        Vector2D verticalUnitVector = new Vector2D(0, -1);
        assertEquals(expectedAngle, verticalUnitVector.getPolarAngle(), delta);
    }

    @Test
    public void polar_angle_is_calculated_correctly_for_unit_vector_in_positive_x_direction(){
        double expectedAngle = 0;
        Vector2D verticalUnitVector = new Vector2D(1, 0);
        assertEquals(expectedAngle, verticalUnitVector.getPolarAngle(), delta);
    }

    @Test
    public void polar_angle_is_calculated_correctly_for_unit_vector_in_negative_x_direction(){
        double expectedAngle = Math.PI;
        Vector2D verticalUnitVector = new Vector2D(-1, 0);
        assertEquals(expectedAngle, verticalUnitVector.getPolarAngle(), delta);
    }

    @Test
    public void polar_angle_is_calculated_correctly_for_unit_vector_in_positive_diagonal_direction_1(){
        double expectedAngle = Math.PI / 4;
        Vector2D verticalUnitVector = new Vector2D(1, 1);
        assertEquals(expectedAngle, verticalUnitVector.getPolarAngle(), delta);
    }

    @Test
    public void polar_angle_is_calculated_correctly_for_unit_vector_in_negative_diagonal_direction_1(){
        double expectedAngle = -Math.PI * 3 / 4;
        Vector2D verticalUnitVector = new Vector2D(-1, -1);
        assertEquals(expectedAngle, verticalUnitVector.getPolarAngle(), delta);
    }

    @Test
    public void polar_angle_is_calculated_correctly_for_unit_vector_in_positive_diagonal_direction_2(){
        double expectedAngle = -Math.PI / 4;
        Vector2D verticalUnitVector = new Vector2D(1, -1);
        assertEquals(expectedAngle, verticalUnitVector.getPolarAngle(), delta);
    }

    @Test
    public void polar_angle_is_calculated_correctly_for_unit_vector_in_negative_diagonal_direction_2(){
        double expectedAngle = Math.PI * 3 / 4;
        Vector2D verticalUnitVector = new Vector2D(-1, 1);
        assertEquals(expectedAngle, verticalUnitVector.getPolarAngle(), delta);
    }
}
