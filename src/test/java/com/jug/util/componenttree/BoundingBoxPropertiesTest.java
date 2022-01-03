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
        assertEquals(expectedWidth, bboxProps.getWidth(), delta);
        assertEquals(expectedOrientationAngle, bboxProps.getRotationAngle(), delta);
        assertTrue("bbox center is incorrect", bboxProps.getCenterCoordinate().equals(expectedCenterCoordinate, delta));
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
        assertEquals(expectedWidth, bboxProps.getWidth(), delta);
        assertEquals(expectedOrientationAngle, bboxProps.getRotationAngle(), delta);
        assertTrue("bbox center is incorrect", bboxProps.getCenterCoordinate().equals(expectedCenterCoordinate, delta));
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
        assertEquals(expectedWidth, bboxProps.getWidth(), delta);
        assertEquals(expectedOrientationAngle, bboxProps.getRotationAngle(), delta);
        assertTrue("bbox center is incorrect", bboxProps.getCenterCoordinate().equals(expectedCenterCoordinate, delta));
    }
}
