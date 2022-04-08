package com.jug.util;

import com.jug.config.ComponentTreeGeneratorConfigurationMock;
import com.jug.datahandling.IImageProvider;
import com.jug.lp.ImageProviderMock;
import com.jug.util.componenttree.*;
import com.jug.util.imglib2.Imglib2Utils;
import com.jug.util.math.Vector2D;
import com.jug.util.math.Vector2DPolyline;
import com.moma.auxiliary.Plotting;
import ij.ImagePlus;
import ij.gui.Overlay;
import net.imagej.ImageJ;
import net.imagej.ops.OpService;
import net.imagej.roi.DefaultROITree;
import net.imagej.roi.ROITree;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.binary.Thresholder;
import net.imglib2.algorithm.componenttree.ComponentForest;
import net.imglib2.img.Img;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.roi.MaskPredicate;
import net.imglib2.type.NativeType;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.ValuePair;
import net.imglib2.view.Views;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class TestUtils {
    private final ImageJ ij;
    private ImageProviderMock imageProviderMock;
    private int frameIndex;

    public TestUtils(ImageJ ij) {
        this.ij = ij;
    }

//    public ValuePair<AdvancedComponent<FloatType>, RandomAccessibleInterval<FloatType>> getComponentWithImageNew(String imageFile, int componentIndex) throws IOException {
//        ComponentForest<AdvancedComponent<FloatType>> tree = getComponentTree(imageFile);
//        ComponentPositionComparator verticalComponentPositionComparator = new ComponentPositionComparator(1);
//        List<AdvancedComponent<FloatType>> roots = new ArrayList<>(tree.roots());
//        roots.sort(verticalComponentPositionComparator);
//        AdvancedComponent<FloatType> component = roots.get(componentIndex);
//        ArrayList<AdvancedComponent<FloatType>> componentList = new ArrayList<>();
//        componentList.add(component);
//        RandomAccessibleInterval<FloatType> image = Plotting.createImageWithComponentsNew(componentList, new ArrayList<>());
//        return new ValuePair<>(component, image);
//    }

    public IImageProvider getImageProvider() {
        return imageProviderMock;
    }

    public Integer getFrameIndex(){
        return frameIndex;
    }

    public void drawComponentTree(String imageFile) throws IOException {
        ComponentForest<AdvancedComponent<FloatType>> tree = getComponentTree(imageFile);
        List<AdvancedComponent<FloatType>> roots = new ArrayList<>(tree.roots());
        AdvancedComponent<FloatType> res = roots.get(0);
        Plotting.drawComponentTree2(tree, new ArrayList<>());
    }

    public <T extends NativeType> ValuePair<AdvancedComponent<FloatType>, RandomAccessibleInterval<T>> getComponentWithImage(String imageFile,
                                                                                                                             int componentIndex,
                                                                                                                             T pixelValue) throws IOException {
        ComponentForest<AdvancedComponent<FloatType>> tree = getComponentTree(imageFile);
        ComponentPositionComparator verticalComponentPositionComparator = new ComponentPositionComparator(1);
        List<AdvancedComponent<FloatType>> roots = new ArrayList<>(tree.roots());
        roots.sort(verticalComponentPositionComparator);
        AdvancedComponent<FloatType> component = roots.get(componentIndex);
        RandomAccessibleInterval<T> image = component.getComponentImage(pixelValue);
        return new ValuePair<>(component, image);
    }

    public ComponentForest<AdvancedComponent<FloatType>> getComponentTree(String imageFile) throws IOException {
        assertTrue(new File(imageFile).exists());
        Img input = (Img) ij.io().open(imageFile);
        assertNotNull(input);
        frameIndex = 0;
        imageProviderMock = new ImageProviderMock(input);
        RandomAccessibleInterval<FloatType> currentImage = Views.hyperSlice(input, 2, frameIndex);
        assertEquals(2, currentImage.numDimensions());
        ComponentTreeGenerator componentTreeGenerator = getComponentTreeGenerator(ij);
        ComponentForest<AdvancedComponent<FloatType>> tree = componentTreeGenerator.buildIntensityTree(imageProviderMock, frameIndex, 1.0f);
        return tree;
    }

    @NotNull
    public ComponentTreeGenerator getComponentTreeGenerator(ImageJ ij) {
        OpService ops = ij.op();
        Imglib2Utils imglib2Utils = new Imglib2Utils(ops);
        ComponentProperties componentProperties = new ComponentProperties(ops, imglib2Utils);
        RecursiveComponentWatershedder recursiveComponentWatershedder = new RecursiveComponentWatershedder(ij.op());
        WatershedMaskGenerator watershedMaskGenerator = new WatershedMaskGenerator(0.5f, 0.5f);
        ComponentTreeGeneratorConfigurationMock config = new ComponentTreeGeneratorConfigurationMock(60, Integer.MIN_VALUE);
        ComponentTreeGenerator componentTreeGenerator = new ComponentTreeGenerator(config, recursiveComponentWatershedder, componentProperties, watershedMaskGenerator, imglib2Utils);
        return componentTreeGenerator;
    }

    public <T extends NumericType<T>> void showImageWithOverlays2(RandomAccessibleInterval<T> image, List<Vector2DPolyline> polylines) {
        List<MaskPredicate<?>> rois = new ArrayList<>();
        polylines.forEach(polyline -> rois.add(polyline.getPolyline()));
        showImageWithOverlays(image, rois);
    }

    public <T extends NumericType<T>> void showImageWithOverlays(RandomAccessibleInterval<T> image, List<MaskPredicate<?>> rois) {
        ROITree roiTree = new DefaultROITree();
        roiTree.addROIs(rois);
        Overlay overlay = ij.convert().convert(roiTree, Overlay.class);
        ImagePlus imagePlus = ImageJFunctions.wrap(image, "image");
        overlay.setStrokeColor(Color.RED);
        overlay.setStrokeWidth(.2);
        imagePlus.setOverlay(overlay);
        ij.ui().show(imagePlus);
//
//        double mag = 5.; // between 0 and 1
//        int newWidth = (int) (imagePlus.getWidth() * mag);
//        int newHeight = (int) ( imagePlus.getHeight() * mag);
//
//        ImageCanvas canvas = imagePlus.getCanvas();
//        canvas.setMagnification(mag);
//
//        ImageWindow win = imagePlus.getWindow();
//        win.setSize(newWidth, newHeight);
    }

    public Img<BitType> readComponentMask(String imageFile) throws IOException {
        Img<UnsignedByteType> input = (Img) ij.io().open(imageFile);
        Img<BitType> componentMask = Thresholder.threshold(input, new UnsignedByteType(128), true, 1); /* BitType images */
        return componentMask;
    }

    public static void assertEqual(Vector2D expected, Vector2D actual, double delta){
        for (int posInd = 0; posInd < 2; posInd++) {
            if (doubleIsDifferent(expected.getDoublePosition(posInd), actual.getDoublePosition(posInd), delta)) {
                throw new AssertionError("Vectors not equal:\nexpected: " + expected + "\nactual: " + actual);
            }
        }
    }

    private static boolean doubleIsDifferent(double d1, double d2, double delta) {
        if (Double.compare(d1, d2) == 0) {
            return false;
        } else {
            return !(Math.abs(d1 - d2) <= delta);
        }
    }
}
