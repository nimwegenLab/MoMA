package com.jug.util.componenttree;

import net.imagej.ImageJ;
import net.imglib2.Localizable;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.Views;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.junit.Assert.*;

public class SimpleComponentTests {
    public static void main(String... args) throws IOException {
        new SimpleComponentTests().testGetParentWatershedLineValues();
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
        for (SimpleComponent<FloatType> root: roots) {
            List<Localizable> watershedProbabilityValues = root.getWatershedLinePixelPositions();
            int bla = 1;
        }
    }
}
