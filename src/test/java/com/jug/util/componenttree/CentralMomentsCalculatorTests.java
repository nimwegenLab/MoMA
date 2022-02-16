package com.jug.util.componenttree;

import net.imglib2.roi.geom.GeomMasks;
import net.imglib2.roi.geom.real.Polygon2D;
import org.javatuples.Quartet;
import org.javatuples.Sextet;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class CentralMomentsCalculatorTests {
    @Test
    public void moments_for_a_rectangle_are_correct(){
        CentralMomentsCalculator polygonMomentsCalculator = new CentralMomentsCalculator();
        double[] xCoords = {2, 10, 8, 0, 2};
        double[] yCoords = {0, 4, 8, 4, 0};
        Polygon2D polyRect = GeomMasks.polygon2D(xCoords, yCoords);
        Sextet<Double, Double, Double, Double, Double, Double> moments = polygonMomentsCalculator.calculate(polyRect);

        assertEquals("area is incorrect", 40.0, moments.getValue0(), 1e-5);
        assertEquals("X coordinate of centroid is incorrect", 5, moments.getValue1(), 1e-5);
        assertEquals("Y coordinate of centroid is incorrect", 4, moments.getValue2(), 1e-5);  //
        assertEquals("X variance is incorrect", 17./3., moments.getValue3(), 1e-5); // X variance is correct
        assertEquals("covariance is incorrect", 2, moments.getValue4(), 1e-5); // covariance is correct
        assertEquals("Y variance is incorrect", 8./3., moments.getValue5(), 1e-5); // Y variance is correct
    }
}
