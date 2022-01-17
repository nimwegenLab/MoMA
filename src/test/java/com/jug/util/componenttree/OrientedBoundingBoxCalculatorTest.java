package com.jug.util.componenttree;

import com.jug.util.TestUtils;
import net.imagej.ImageJ;
import net.imagej.ops.geom.geom2d.DefaultConvexHull2D;
import net.imagej.ops.geom.geom2d.LabelRegionToPolygonConverter;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.RealLocalizable;
import net.imglib2.roi.MaskPredicate;
import net.imglib2.roi.geom.GeomMasks;
import net.imglib2.roi.geom.real.Polygon2D;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.ValuePair;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

//import net.imagej.ops.geom.geom2d.DefaultBoundingBox; // contains convex hull functions  e.g.: op = net.imagej.ops.geom.geom2d.DefaultVerticesCountConvexHullPolygon.class). public DoubleType boundaryPixelCountConvexHull(final Polygon2D in) {. boundaryPixelCountConvexHull


public class OrientedBoundingBoxCalculatorTest {
    private final ImageJ ij;
    private final TestUtils testUtils;

    public OrientedBoundingBoxCalculatorTest() {
        ij = new ImageJ();
        testUtils = new TestUtils(ij);
    }

    public static void main(String... args) throws IOException {
        ImageJ ij = new ImageJ();
        ij.ui().showUI();
        new OrientedBoundingBoxCalculatorTest().exploreOrientedBoundingBox();
//        new OrientedBoundingBoxCalculatorTest().test_GetOrientedBoundingBoxCoordinates_returns_correct_value();
    }

    @Test
    public void test_GetOrientedBoundingBoxCoordinates_returns_correct_value_1() {
        double[] xp = new double[]{53, 50, 49, 48, 47, 46, 46, 47, 49, 54, 56, 57, 58, 60, 60, 59, 56};
        double[] yp = new double[]{311, 314, 316, 323, 331, 341, 342, 346, 348, 348, 347, 345, 338, 320, 316, 314, 311};

        OrientedBoundingBoxCalculator boundingBoxCalculator = new OrientedBoundingBoxCalculator();
        ValuePair<double[], double[]> res = boundingBoxCalculator.getOrientedBoundingBoxCoordinatesOfConvexHull(xp, yp);
        Polygon2D orientedBoundingBoxPolygon = GeomMasks.polygon2D(res.getA(), res.getB());
        List<RealLocalizable> vertices = orientedBoundingBoxPolygon.vertices();
        double[] xCoordsExpected = new double[]{60.939026, 49.30488, 45.158535, 56.792683};
        double[] yCoordsExpected = new double[]{311.54877, 310.2561, 347.57318, 348.86584};
        int index = 0;
        for (RealLocalizable pos : vertices) {
            assertEquals(xCoordsExpected[index], pos.getFloatPosition(0), 1e-4);
            assertEquals(yCoordsExpected[index], pos.getFloatPosition(1), 1e-4);
            index++;
        }
    }

    @Test
    public void test_GetOrientedBoundingBoxCoordinates_returns_correct_value_2() {
        double[] xp = new double[]{49, 47, 47, 50, 52, 53, 54, 56, 60, 61, 61, 59, 58, 56, 54};
        double[] yp = new double[]{352, 354, 361, 375, 381, 383, 384, 385, 385, 384, 376, 362, 357, 353, 352};

        OrientedBoundingBoxCalculator boundingBoxCalculator = new OrientedBoundingBoxCalculator();
        ValuePair<double[], double[]> res = boundingBoxCalculator.getOrientedBoundingBoxCoordinatesOfConvexHull(xp, yp);
        Polygon2D orientedBoundingBoxPolygon = GeomMasks.polygon2D(res.getA(), res.getB());
        List<RealLocalizable> vertices = orientedBoundingBoxPolygon.vertices();
        double[] xCoordsExpected = new double[]{56.692307, 45.346153, 52.115383, 63.46154};
        double[] yCoordsExpected = new double[]{350.46155, 352.73077, 386.57693, 384.30768};
        int index = 0;
        for (RealLocalizable pos : vertices) {
            assertEquals(xCoordsExpected[index], pos.getFloatPosition(0), 1e-4);
            assertEquals(yCoordsExpected[index], pos.getFloatPosition(1), 1e-4);
            index++;
        }
    }

    /**
     * Add test for gerating the component tree on a sample image and displaying it.
     *
     * @throws IOException
     * @throws InterruptedException
     */
    public void exploreOrientedBoundingBox() throws IOException {
        String imageFile = new File("").getAbsolutePath() + "/src/test/resources/00_probability_maps/probabilities_watershedding_000.tif";
        int componentIndex = 3;

        ValuePair<AdvancedComponent<FloatType>, RandomAccessibleInterval<ARGBType>> componentWithImage =
                testUtils.getComponentWithImage(imageFile,
                        componentIndex,
                        new ARGBType(ARGBType.rgba(255, 255, 255, 255)));

        AdvancedComponent<FloatType> component = componentWithImage.getA();
        RandomAccessibleInterval<ARGBType> image = componentWithImage.getB();

        OrientedBoundingBoxCalculator boundingBoxCalculator = new OrientedBoundingBoxCalculator();

        LabelRegionToPolygonConverter regionToPolygonConverter = new LabelRegionToPolygonConverter();
        regionToPolygonConverter.setContext(ij.context());
        final Polygon2D poly = regionToPolygonConverter.convert(component.getRegion(), Polygon2D.class);
        Polygon2D orientedBoundingBoxPolygon = boundingBoxCalculator.calculate(poly);

        DefaultConvexHull2D convexHullCalculator = new DefaultConvexHull2D();
        Polygon2D polyHull = convexHullCalculator.calculate(poly);

        List<MaskPredicate<?>> rois = Arrays.asList(
                poly,
                polyHull,
                orientedBoundingBoxPolygon
        );
        testUtils.showImageWithOverlays(image, rois);
    }
}