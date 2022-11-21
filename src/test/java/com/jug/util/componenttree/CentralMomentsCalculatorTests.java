package com.jug.util.componenttree;

import net.imglib2.roi.geom.GeomMasks;
import net.imglib2.roi.geom.real.Polygon2D;
import org.javatuples.Quartet;
import org.javatuples.Sextet;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CentralMomentsCalculatorTests {

    /**
     * The test values for the rectangle below are derived analytically in this publication:
     *
     * @see "On  Calculation of Arbitrary Moments of Polygon2Ds, Carsten Steger, October 1996"
     **/
    @Test
    public void moments_for_a_rectangle_are_correct(){
        CentralMomentsCalculator polygonMomentsCalculator = new CentralMomentsCalculator();
        double[] xCoords = {2, 10, 8, 0, 2};
        double[] yCoords = {0, 4, 8, 4, 0};
        Polygon2D polyRect = GeomMasks.polygon2D(xCoords, yCoords);
        Sextet<Double, Double, Double, Double, Double, Double> moments = polygonMomentsCalculator.calculate(polyRect);

        assertEquals(40.0, moments.getValue0(), 1e-5, "area is incorrect");
        assertEquals(5, moments.getValue1(), 1e-5, "X coordinate of centroid is incorrect");
        assertEquals(4, moments.getValue2(), 1e-5, "Y coordinate of centroid is incorrect");  //
        assertEquals(17./3., moments.getValue3(), 1e-5, "X variance is incorrect"); // X variance is correct
        assertEquals(8./3., moments.getValue4(), 1e-5, "Y variance is incorrect"); // Y variance is correct
        assertEquals(2, moments.getValue5(), 1e-5, "covariance is incorrect"); // covariance is correct
    }
}
