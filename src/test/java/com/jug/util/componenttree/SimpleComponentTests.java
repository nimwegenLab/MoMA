package com.jug.util.componenttree;

import com.moma.auxiliary.Plotting;
import ij.ImagePlus;
import net.imagej.ImageJ;
import net.imglib2.Localizable;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.logic.NativeBoolType;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.Views;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class SimpleComponentTests {
    public static void main(String... args) throws IOException {
//        new SimpleComponentTests().testGetParentWatershedLineValues();
        new SimpleComponentTests().testGetParentWatershedLineCoordinates();
    }

    @Test
    public void testGetParentWatershedLineValues() throws IOException {
        String imageFile = new File("").getAbsolutePath() + "/src/test/resources/00_probability_maps/probabilities_watershedding_000.tif";
        assertTrue(new File(imageFile).exists());

        ImageJ ij = new ImageJ();
        Img input = (Img) ij.io().open(imageFile);
        assertNotNull(input);
        int frameIndex = 10;
        RandomAccessibleInterval<FloatType> currentImage = Views.hyperSlice(input, 2, frameIndex);
        assertEquals(2, currentImage.numDimensions());

        SimpleComponentTree<FloatType, SimpleComponent<FloatType>> tree = (SimpleComponentTree<FloatType, SimpleComponent<FloatType>>) new ComponentTreeGenerator().buildIntensityTree(currentImage);

        List<SimpleComponent<FloatType>> roots = tree.rootsSorted();
        for (SimpleComponent<FloatType> root: roots) {
            List<FloatType> watershedProbabilityValues = root.getWatershedLineValues();
        }
    }

    @Test
    public void testGetParentWatershedLineCoordinates() throws IOException {
        String imageFile = new File("").getAbsolutePath() + "/src/test/resources/00_probability_maps/probabilities_watershedding_000.tif";
        assertTrue(new File(imageFile).exists());

        ImageJ ij = new ImageJ();
        Img input = (Img) ij.io().open(imageFile);
        assertNotNull(input);
        int frameIndex = 10;
        RandomAccessibleInterval<FloatType> currentImage = Views.hyperSlice(input, 2, frameIndex);
        assertEquals(2, currentImage.numDimensions());

        SimpleComponentTree<FloatType, SimpleComponent<FloatType>> tree = (SimpleComponentTree<FloatType, SimpleComponent<FloatType>>) new ComponentTreeGenerator().buildIntensityTree(currentImage);

        List<SimpleComponent<FloatType>> roots = tree.rootsSorted();
        for (SimpleComponent<FloatType> root : roots) {
            if (root.getChildren().size() == 0) {
                return;
            }
            ArrayList<SimpleComponent<FloatType>> componentsToDraw = new ArrayList<>();
            componentsToDraw.add(root);
            RandomAccessibleInterval<ARGBType> parentComponentImage = Plotting.createImageWithComponents(componentsToDraw, new ArrayList<>());
            RandomAccessibleInterval<ARGBType> childComponentsImage = Plotting.createImageWithComponents(root.getChildren(), new ArrayList<>());
            ImagePlus impParent = ImageJFunctions.show(parentComponentImage);
            ImagePlus impChildren = ImageJFunctions.show(childComponentsImage);
            List<Localizable> watershedProbabilityValues = root.getWatershedLinePixelPositions();
            Img<NativeBoolType> img = root.createImage(root.getSourceImage());
            RandomAccess<NativeBoolType> rndAcc = img.randomAccess();
            for (Localizable loc : watershedProbabilityValues) {
                rndAcc.setPosition(loc);
                rndAcc.get().set(true);
            }
            ImagePlus imp = ImageJFunctions.show(img);
        }
    }
}
