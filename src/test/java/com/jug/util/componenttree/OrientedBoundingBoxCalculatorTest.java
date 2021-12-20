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
import net.imglib2.algorithm.componenttree.ComponentForest;
import net.imglib2.img.Img;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.roi.MaskPredicate;
import net.imglib2.roi.geom.real.Polygon2D;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.real.FloatType;
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

    public static void main(String... args) throws IOException {
        ImageJ ij = new ImageJ();
        ij.ui().showUI();
        new OrientedBoundingBoxCalculatorTest().testOrientedBoundingBox();
    }

    public OrientedBoundingBoxCalculatorTest() {
        ij = new ImageJ();
    }

    /**
     * Add test for gerating the component tree on a sample image and displaying it.
     * @throws IOException
     * @throws InterruptedException
     */
    @Test
    public void testOrientedBoundingBox() throws IOException {
        ComponentForest<AdvancedComponent<FloatType>> tree = getComponentTree();

        ComponentPositionComparator verticalComponentPositionComparator = new ComponentPositionComparator(1);
        List<AdvancedComponent<FloatType>> roots = new ArrayList<>(tree.roots());
        roots.sort(verticalComponentPositionComparator);

        ComponentProperties props = new ComponentProperties(ij.op(), new Imglib2Utils(ij.op()));

        int componentIndex = 2;
        AdvancedComponent<FloatType> component = roots.get(componentIndex);
//        Plotting.drawComponentTree2(tree, new ArrayList<>());
        OpService ops = ij.op();

        OrientedBoundingBoxCalculator boundingBoxCalculator = new OrientedBoundingBoxCalculator(ops);
        Polygon2D orientedBoundingBoxPolygon = boundingBoxCalculator.calculate(component);
        
//        double minorAxis = props.getMinorMajorAxis(component).getA();
        LabelRegionToPolygonConverter regionToPolygonConverter = new LabelRegionToPolygonConverter();
        regionToPolygonConverter.setContext(ops.context());
        final Polygon2D poly = regionToPolygonConverter.convert(component.getRegion(), Polygon2D.class);
        DefaultConvexHull2D convexHullCalculator = new DefaultConvexHull2D();
        Polygon2D polyHull = convexHullCalculator.calculate(poly);

        ArrayList<AdvancedComponent<FloatType>> componentList = new ArrayList<>();
        componentList.add(component);
        RandomAccessibleInterval<ARGBType> image = Plotting.createImageWithComponents(componentList, new ArrayList<>());
        List<MaskPredicate< ? >> rois = Arrays.asList(
                poly,
                polyHull,
                orientedBoundingBoxPolygon
        );
        ROITree roiTree = new DefaultROITree();
        roiTree.addROIs(rois);
        Overlay overlay = ij.convert().convert(roiTree, Overlay.class);

        ImagePlus imagePlus = ImageJFunctions.wrap((Img) image, "image");
        imagePlus.setOverlay(overlay);
        ij.ui().show(imagePlus);
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

//        ImageJFunctions.show(currentImage);

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