package com.jug.util.componenttree;

import com.jug.datahandling.IImageProvider;
import com.jug.lp.ImageProviderMock;
import com.jug.util.imglib2.Imglib2Utils;
import com.moma.auxiliary.Plotting;
import ij.ImagePlus;
import ij.gui.Overlay;
import net.imagej.ImageJ;
import net.imagej.ops.OpService;
import net.imagej.ops.geom.geom2d.DefaultConvexHull2D;
import net.imagej.ops.geom.geom2d.LabelRegionToPolygonConverter;
import net.imagej.roi.DefaultROITree;
import net.imagej.roi.ROITree;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.RealLocalizable;
import net.imglib2.algorithm.componenttree.ComponentForest;
import net.imglib2.img.Img;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.roi.MaskPredicate;
import net.imglib2.roi.geom.GeomMasks;
import net.imglib2.roi.geom.real.Polygon2D;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.ValuePair;
import net.imglib2.view.Views;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

//import net.imagej.ops.geom.geom2d.DefaultBoundingBox; // contains convex hull functions  e.g.: op = net.imagej.ops.geom.geom2d.DefaultVerticesCountConvexHullPolygon.class). public DoubleType boundaryPixelCountConvexHull(final Polygon2D in) {. boundaryPixelCountConvexHull


public class OrientedBoundingBoxCalculatorTest {
    private final ImageJ ij;
    private final OpService ops;

    public OrientedBoundingBoxCalculatorTest() {
        ij = new ImageJ();
        ops = ij.op();
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

        OrientedBoundingBoxCalculator boundingBoxCalculator = new OrientedBoundingBoxCalculator(ops);
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

        OrientedBoundingBoxCalculator boundingBoxCalculator = new OrientedBoundingBoxCalculator(ops);
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
        int componentIndex = 3;
        ValuePair<AdvancedComponent<FloatType>, RandomAccessibleInterval<ARGBType>> componentWithImage = getComponentWithImage(componentIndex);

        AdvancedComponent<FloatType> component = componentWithImage.getA();
        RandomAccessibleInterval<ARGBType> image = componentWithImage.getB();

        OrientedBoundingBoxCalculator boundingBoxCalculator = new OrientedBoundingBoxCalculator(ops);
        Polygon2D orientedBoundingBoxPolygon = boundingBoxCalculator.calculate(component);

        LabelRegionToPolygonConverter regionToPolygonConverter = new LabelRegionToPolygonConverter();
        regionToPolygonConverter.setContext(ops.context());
        final Polygon2D poly = regionToPolygonConverter.convert(component.getRegion(), Polygon2D.class);
        DefaultConvexHull2D convexHullCalculator = new DefaultConvexHull2D();
        Polygon2D polyHull = convexHullCalculator.calculate(poly);

        List<MaskPredicate<?>> rois = Arrays.asList(
//                poly,
//                polyHull,
//                orientedBoundingBoxPolygon
        );
        showImageWithOverlays(image, rois);
    }

    private void showImageWithOverlays(RandomAccessibleInterval<ARGBType> image, List<MaskPredicate<?>> rois) {
        ROITree roiTree = new DefaultROITree();
        roiTree.addROIs(rois);
        Overlay overlay = ij.convert().convert(roiTree, Overlay.class);
        ImagePlus imagePlus = ImageJFunctions.wrap(image, "image");
        imagePlus.setOverlay(overlay);
        ij.ui().show(imagePlus);
    }

    private ValuePair<AdvancedComponent<FloatType>, RandomAccessibleInterval<ARGBType>> getComponentWithImage(int componentIndex) throws IOException {
        ComponentForest<AdvancedComponent<FloatType>> tree = getComponentTree();
        ComponentPositionComparator verticalComponentPositionComparator = new ComponentPositionComparator(1);
        List<AdvancedComponent<FloatType>> roots = new ArrayList<>(tree.roots());
        roots.sort(verticalComponentPositionComparator);
        AdvancedComponent<FloatType> component = roots.get(componentIndex);
        ArrayList<AdvancedComponent<FloatType>> componentList = new ArrayList<>();
        componentList.add(component);
        RandomAccessibleInterval<ARGBType> image = Plotting.createImageWithComponents(componentList, new ArrayList<>());
        return new ValuePair<>(component, image);
    }

    private ComponentForest<AdvancedComponent<FloatType>> getComponentTree() throws IOException {
        String imageFile = new File("").getAbsolutePath() + "/src/test/resources/00_probability_maps/probabilities_watershedding_000.tif";
        assertTrue(new File(imageFile).exists());
        Img input = (Img) ij.io().open(imageFile);
        assertNotNull(input);
        int frameIndex = 10;
        IImageProvider imageProviderMock = new ImageProviderMock(input);
        RandomAccessibleInterval<FloatType> currentImage = Views.hyperSlice(input, 2, frameIndex);
        assertEquals(2, currentImage.numDimensions());
        ComponentTreeGenerator componentTreeGenerator = getComponentTreeGenerator(ij);
        ComponentForest<AdvancedComponent<FloatType>> tree = componentTreeGenerator.buildIntensityTree(imageProviderMock, frameIndex, 1.0f);
        return tree;
    }

    @NotNull
    private ComponentTreeGenerator getComponentTreeGenerator(ImageJ ij) {
        OpService ops = ij.op();
        Imglib2Utils imglib2Utils = new Imglib2Utils(ops);
        ComponentProperties componentProperties = new ComponentProperties(ops, imglib2Utils);
        RecursiveComponentWatershedder recursiveComponentWatershedder = new RecursiveComponentWatershedder(ij.op());
        WatershedMaskGenerator watershedMaskGenerator = new WatershedMaskGenerator(0, 0.5f);
        ComponentTreeGenerator componentTreeGenerator = new ComponentTreeGenerator(recursiveComponentWatershedder, componentProperties, watershedMaskGenerator, imglib2Utils);
        return componentTreeGenerator;
    }
}