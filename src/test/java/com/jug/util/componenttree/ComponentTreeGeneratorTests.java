package com.jug.util.componenttree;

import com.google.common.collect.Lists;
import com.moma.auxiliary.Plotting;
import ij.ImagePlus;
import ij.gui.TextRoi;
import net.imagej.ImageJ;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.componenttree.ComponentForest;
import net.imglib2.img.Img;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.Views;
import org.junit.Test;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class ComponentTreeGeneratorTests {
    public static void main(String... args) throws IOException, InterruptedException {
        ImageJ ij = new ImageJ();
//        new ComponentTreeGeneratorTests().testWatershedding();
        new ComponentTreeGeneratorTests().testSegmentAreaCalculationOfChildren();
    }

    /**
     * Add test for generating the component tree on a sample image and displaying it.
     *
     * @throws IOException
     * @throws InterruptedException
     */
    @Test
    public void testWatershedding() throws IOException, InterruptedException {
        String imageFile = new File("").getAbsolutePath() + "/src/test/resources/00_probability_maps/probabilities_watershedding_000.tif";
        int frameIndex = 10;
//        String imageFile = new File("").getAbsolutePath() + "/src/test/resources/00_probability_maps/cropped_20180711_glyc_lactuloseTMG20uM_1_MMStack_Pos3_preproc_GL16__model_6a24d4567cae96f9a0469d872dfd2ecb2abb4d0a9d0464e561d2dbc7dd0c0411.tif";
//        int frameIndex = 30;
        assertTrue(new File(imageFile).exists());

        ImageJ ij = new ImageJ();
        Img input = (Img) ij.io().open(imageFile);
        assertNotNull(input);

        RandomAccessibleInterval<FloatType> currentImage = Views.hyperSlice(input, 2, frameIndex);
        assertEquals(2, currentImage.numDimensions());

        ImageJFunctions.show(currentImage);
        ComponentForest<SimpleComponent<FloatType>> tree = new ComponentTreeGenerator().buildIntensityTree(currentImage);
        Plotting.drawComponentTree2(tree, new ArrayList<>());
    }

    /**
     * Add test for methods that calculate the total component area above and below a component.
     *
     * @throws IOException
     * @throws InterruptedException
     */
    @Test
    public void testSegmentAreaCalculation() throws IOException, InterruptedException {
        String imageFile = new File("").getAbsolutePath() + "/src/test/resources/00_probability_maps/probabilities_watershedding_000.tif";
        int frameIndex = 10;
        assertTrue(new File(imageFile).exists());

        ImageJ ij = new ImageJ();
        Img input = (Img) ij.io().open(imageFile);
        assertNotNull(input);

        RandomAccessibleInterval<FloatType> currentImage = Views.hyperSlice(input, 2, frameIndex);
        assertEquals(2, currentImage.numDimensions());

        SimpleComponentTree<FloatType, SimpleComponent<FloatType>> tree = (SimpleComponentTree<FloatType, SimpleComponent<FloatType>>) new ComponentTreeGenerator().buildIntensityTree(currentImage);

        List<SimpleComponent<FloatType>> roots = tree.rootsSorted();

        /* test that each returned area above is equal to the summed area of roots above */
        int totalSizeOfComponentsSoFar = 0;
        for (SimpleComponent<FloatType> root : roots) {
            assertEquals(totalSizeOfComponentsSoFar, root.getTotalAreaOfComponentsAbove());
            totalSizeOfComponentsSoFar += root.size();
        }

        /* test that each returned area above is equal to the summed area of roots below */
        totalSizeOfComponentsSoFar = 0;
        for (SimpleComponent<FloatType> root : Lists.reverse(roots)) {
            assertEquals(totalSizeOfComponentsSoFar, root.getTotalAreaOfComponentsBelow());
            totalSizeOfComponentsSoFar += root.size();
        }

        /* test that each returned area above is equal to the summed area of roots below */
        totalSizeOfComponentsSoFar = 0;
        for (SimpleComponent<FloatType> root : roots) {
            totalSizeOfComponentsSoFar += root.size();
        }
        assertEquals(totalSizeOfComponentsSoFar, roots.get(0).getTotalAreaOfRootComponents());
    }

    /**
     * Add test for methods that calculate the total component area above and below a component.
     *
     * @throws IOException
     * @throws InterruptedException
     */
    @Test
    public void testSegmentAreaCalculationOfChildren() throws IOException, InterruptedException {
        String imageFile = new File("").getAbsolutePath() + "/src/test/resources/00_probability_maps/probabilities_watershedding_000.tif";
        int frameIndex = 10;
        assertTrue(new File(imageFile).exists());

        ImageJ ij = new ImageJ();
        Img input = (Img) ij.io().open(imageFile);
        assertNotNull(input);

        RandomAccessibleInterval<FloatType> currentImage = Views.hyperSlice(input, 2, frameIndex);
        assertEquals(2, currentImage.numDimensions());

        SimpleComponentTree<FloatType, SimpleComponent<FloatType>> tree = (SimpleComponentTree<FloatType, SimpleComponent<FloatType>>) new ComponentTreeGenerator().buildIntensityTree(currentImage);

        List<SimpleComponent<FloatType>> roots = tree.rootsSorted();

        int counter = 0;
        for (SimpleComponent<FloatType> root : roots) {
            List<SimpleComponent<FloatType>> components = root.getComponentsBelowClosestToRoot();
//            List<SimpleComponent<FloatType>> components = new ArrayList<>();
//            components.add(root);
            ImagePlus imp = ImageJFunctions.show(Plotting.createImageWithComponents(components, new ArrayList<>()));
//            TextRoi text = new TextRoi(0, 0, String.format("y_center=%d", (int) root.firstMomentPixelCoordinates()[1]));
            TextRoi text = new TextRoi(0, 0, String.format("i=%d", counter));
            imp.setOverlay(text, Color.white, 0, Color.black);
            counter ++;
        }
    }
}
