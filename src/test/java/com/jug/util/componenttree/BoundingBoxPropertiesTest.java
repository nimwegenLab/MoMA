package com.jug.util.componenttree;

import com.jug.util.math.Vector2D;
import net.imglib2.roi.geom.GeomMasks;
import net.imglib2.roi.geom.real.Polygon2D;
import org.junit.Test;
import static org.junit.Assert.*;

public class BoundingBoxPropertiesTest {
    double delta = 0.01;
    @Test
    public void test_returned_properties_for_vertically_aligned_bbox(){
        double expectedArea = 5000.0;
        double expectedHeight = 100.0;
        double expectedWidth = 50.0;
        double expectedOrientationAngle = Math.PI / 2;
        Vector2D expectedCenterCoordinate = new Vector2D(25, 50);
        double[] xCoordsExpected = new double[]{0, 50, 50, 0};
        double[] yCoordsExpected = new double[]{0, 0, 100, 100};
        Polygon2D orientedBoundingBoxPolygon = GeomMasks.polygon2D(xCoordsExpected, yCoordsExpected);
        BoundingBoxProperties bboxProps = new BoundingBoxProperties(orientedBoundingBoxPolygon);
        assertEquals(expectedArea, bboxProps.getArea(), delta);
        assertEquals(expectedHeight, bboxProps.getHeight(), delta);
        assertEquals(expectedWidth, bboxProps.getWidth(), delta);
        assertEquals(expectedOrientationAngle, bboxProps.getRotationAngle(), delta);
        assertVectorsEqual(expectedCenterCoordinate, bboxProps);
    }

    @Test
    public void test_returned_properties_for_horizontally_aligned_bbox(){
        double expectedArea = 5000.0;
        double expectedHeight = 100.0;
        double expectedWidth = 50.0;
        double expectedOrientationAngle = 0.0;
        Vector2D expectedCenterCoordinate = new Vector2D(50,25);
        double[] xCoordsExpected = new double[]{0, 100, 100, 0};
        double[] yCoordsExpected = new double[]{0, 0, 50, 50};
        Polygon2D orientedBoundingBoxPolygon = GeomMasks.polygon2D(xCoordsExpected, yCoordsExpected);
        BoundingBoxProperties bboxProps = new BoundingBoxProperties(orientedBoundingBoxPolygon);
        assertEquals(expectedArea, bboxProps.getArea(), delta);
        assertEquals(expectedHeight, bboxProps.getHeight(), delta);
        assertEquals(expectedWidth, bboxProps.getWidth(), delta);
        assertEquals(expectedOrientationAngle, bboxProps.getRotationAngle(), delta);
        assertVectorsEqual(expectedCenterCoordinate, bboxProps);
    }

    @Test
    public void test_returned_properties_for_horizontally_aligned_bbox_oriented_in_negative_x_direction(){
        double expectedArea = 5000.0;
        double expectedHeight = 100.0;
        double expectedWidth = 50.0;
        double expectedOrientationAngle = 0.0;
        Vector2D expectedCenterCoordinate = new Vector2D(-50,25);
        double[] xCoordsExpected = new double[]{0, -100, -100, 0};
        double[] yCoordsExpected = new double[]{0, 0, 50, 50};
        Polygon2D orientedBoundingBoxPolygon = GeomMasks.polygon2D(xCoordsExpected, yCoordsExpected);
        BoundingBoxProperties bboxProps = new BoundingBoxProperties(orientedBoundingBoxPolygon);
        assertEquals(expectedArea, bboxProps.getArea(), delta);
        assertEquals(expectedHeight, bboxProps.getHeight(), delta);
        assertEquals(expectedWidth, bboxProps.getWidth(), delta);
        assertEquals(expectedOrientationAngle, bboxProps.getRotationAngle(), delta);
        assertVectorsEqual(expectedCenterCoordinate, bboxProps);
    }

    @Test
    public void test_returned_properties_for_45_degrees_right_tilted_bbox_are_correct(){
        double expectedHeight = 3 * 10.0 * Math.sqrt(2);
        double expectedWidth = 1 * 10.0 * Math.sqrt(2);
        double expectedArea = expectedWidth * expectedHeight;
        double expectedOrientationAngle = Math.PI / 4;
        Vector2D expectedCenterCoordinate = new Vector2D(0,0);
        double[] xCoordsExpected = new double[]{-10, 20, 10, -20};
        double[] yCoordsExpected = new double[]{-20, 10, 20, -10};
        Polygon2D orientedBoundingBoxPolygon = GeomMasks.polygon2D(xCoordsExpected, yCoordsExpected);
        BoundingBoxProperties bboxProps = new BoundingBoxProperties(orientedBoundingBoxPolygon);
        assertEquals(expectedArea, bboxProps.getArea(), delta);
        assertEquals(expectedHeight, bboxProps.getHeight(), delta);
        assertEquals(expectedWidth, bboxProps.getWidth(), delta);
        assertEquals(expectedOrientationAngle, bboxProps.getRotationAngle(), delta);
        assertVectorsEqual(expectedCenterCoordinate, bboxProps);
    }

    @Test
    public void test_returned_properties_for_135_degrees_left_tilted_bbox_are_correct(){
        double expectedHeight = 3 * 10.0 * Math.sqrt(2);
        double expectedWidth = 1 * 10.0 * Math.sqrt(2);
        double expectedArea = expectedWidth * expectedHeight;
        double expectedOrientationAngle = Math.PI * 3 / 4;
        Vector2D expectedCenterCoordinate = new Vector2D(0,0);
        double[] xCoordsExpected = new double[]{10, -20, -10, 20};
        double[] yCoordsExpected = new double[]{-20, 10, 20, -10};
        Polygon2D orientedBoundingBoxPolygon = GeomMasks.polygon2D(xCoordsExpected, yCoordsExpected);
        BoundingBoxProperties bboxProps = new BoundingBoxProperties(orientedBoundingBoxPolygon);
        assertEquals(expectedArea, bboxProps.getArea(), delta);
        assertEquals(expectedHeight, bboxProps.getHeight(), delta);
        assertEquals(expectedWidth, bboxProps.getWidth(), delta);
        assertEquals(expectedOrientationAngle, bboxProps.getRotationAngle(), delta);
        assertVectorsEqual(expectedCenterCoordinate, bboxProps);
    }

    private void assertVectorsEqual(Vector2D expectedCenterCoordinate, BoundingBoxProperties bboxProps) {
        assertTrue("bbox center is incorrect\nexpected: " + expectedCenterCoordinate + "\nactual: " + bboxProps.getCenterCoordinate(), bboxProps.getCenterCoordinate().equals(expectedCenterCoordinate, delta));
    }
}
