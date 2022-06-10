package com.jug.util.componenttree;

import com.google.common.collect.Lists;
import com.jug.config.ComponentTreeGeneratorConfigurationMock;
import com.jug.datahandling.IImageProvider;
import com.jug.lp.ImageProviderMock;
import com.jug.util.imglib2.Imglib2Utils;
import com.moma.auxiliary.Plotting;
import ij.ImagePlus;
import ij.gui.TextRoi;
import net.imagej.ImageJ;
import net.imagej.ops.OpService;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.componenttree.ComponentForest;
import net.imglib2.img.Img;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.Views;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;

import static org.junit.Assert.*;

public class ComponentTreeGeneratorTests {
    public static void main(String... args) throws IOException, InterruptedException {
//        new ComponentTreeGeneratorTests().testWatershedding();
//        new ComponentTreeGeneratorTests().testSegmentAreaCalculationOfChildren();
//        new ComponentTreeGeneratorTests().testPrintRankOfSegment();
//        new ComponentTreeGeneratorTests().root_components__return__correct_hash_code();
        new ComponentTreeGeneratorTests().debugThreeWayComponentSegmentation();
    }

    /**
     * Add test for generating the component tree on a sample image and displaying it.
     *
     * @throws IOException
     * @throws InterruptedException
     */
    public void debugThreeWayComponentSegmentation() throws IOException {
        String imageFile = new File("").getAbsolutePath() + "/src/test/resources/00_probability_maps/20211026_VNG1040_AB6min_2h_1_MMStack_Pos7_GL12/frames_494-495__20211026_VNG1040_AB6min_2h_1_MMStack_Pos7_GL12__model_9e5727e4ed18802f4ab04c7494ef8992d798f4d64d5fd75e285b9a3d83b13ac9.tif";
        int frameIndex = 1;

        SimpleComponentTree<FloatType, AdvancedComponent<FloatType>> tree = (SimpleComponentTree<FloatType, AdvancedComponent<FloatType>>) getComponentTreeFromProbabilityImage(imageFile, frameIndex, 1.0f);

//        for (AdvancedComponent component2 : tree.getAllComponents()) {
//            ImageJFunctions.show(Plotting.createImageWithComponent(component2));
//        }
        Plotting.drawComponentTree2(tree, new ArrayList<>());
    }

    /**
     * Add test for generating the component tree on a sample image and displaying it.
     *
     * @throws IOException
     * @throws InterruptedException
     */
    @Test
    public void root_components__return__correct_hash_code() throws IOException {
        String imageFile = new File("").getAbsolutePath() + "/src/test/resources/00_probability_maps/20211026_VNG1040_AB6min_2h_1_MMStack_Pos7_GL12/frames_494-495__20211026_VNG1040_AB6min_2h_1_MMStack_Pos7_GL12__model_9e5727e4ed18802f4ab04c7494ef8992d798f4d64d5fd75e285b9a3d83b13ac9.tif";
        int frameIndex = 1;

        SimpleComponentTree<FloatType, AdvancedComponent<FloatType>> tree = (SimpleComponentTree<FloatType, AdvancedComponent<FloatType>>) getComponentTreeFromProbabilityImage(imageFile, frameIndex, 1.0f);
        List<AdvancedComponent<FloatType>> rootComponents = tree.rootsSorted();

        Set<Integer> expectedRootHashCodes = new HashSet<>(Arrays.asList(new Integer[]{557155091, 2026295260, 911273390, 356038541, 1017538091, 796074954, 592650667, 548763902, 1886319597, 1117842004, -2000971763, -643329487}));

        for (AdvancedComponent root : rootComponents){
            assertTrue("hash code note found in list of expected hash codes", expectedRootHashCodes.contains(root.hashCode()));
            expectedRootHashCodes.remove(root.hashCode());
        }
        assertTrue("not all expected hash codes were found", expectedRootHashCodes.isEmpty());
    }

    /**
     * Add test for generating the component tree on a sample image and displaying it.
     *
     * @throws IOException
     * @throws InterruptedException
     */
    public void testWatershedding() throws IOException {
        String imageFile = new File("").getAbsolutePath() + "/src/test/resources/00_probability_maps/probabilities_watershedding_000.tif";
        int frameIndex = 10;

        ComponentForest<AdvancedComponent<FloatType>> tree = getComponentTreeFromProbabilityImage(imageFile, frameIndex, 0.5f);
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

        SimpleComponentTree<FloatType, AdvancedComponent<FloatType>> tree = (SimpleComponentTree<FloatType, AdvancedComponent<FloatType>>) getComponentTreeFromProbabilityImage(imageFile, frameIndex, 1.0f);

        List<AdvancedComponent<FloatType>> roots = tree.rootsSorted();

        /* test that each returned area above is equal to the summed area of roots above */
        int totalSizeOfComponentsSoFar = 0;
        for (AdvancedComponent<FloatType> root : roots) {
            assertEquals(totalSizeOfComponentsSoFar, root.getTotalAreaOfComponentsAbove());
            totalSizeOfComponentsSoFar += root.size();
        }

        /* test that each returned area above is equal to the summed area of roots below */
        totalSizeOfComponentsSoFar = 0;
        for (AdvancedComponent<FloatType> root : Lists.reverse(roots)) {
            assertEquals(totalSizeOfComponentsSoFar, root.getTotalAreaOfComponentsBelow());
            totalSizeOfComponentsSoFar += root.size();
        }

        /* test that each returned area above is equal to the summed area of roots below */
        totalSizeOfComponentsSoFar = 0;
        for (AdvancedComponent<FloatType> root : roots) {
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
    public void testSegmentAreaCalculationOfChildren() throws IOException, InterruptedException {
        String imageFile = new File("").getAbsolutePath() + "/src/test/resources/00_probability_maps/cropped__20200922_M9glc_VNG1040-hi2_AB_1_MMStack_Pos0_GL30__probability_map_frame_126_duplicated_frame__20210812.tif";
        int frameIndex = 0;

        SimpleComponentTree<FloatType, AdvancedComponent<FloatType>> tree = (SimpleComponentTree<FloatType, AdvancedComponent<FloatType>>) getComponentTreeFromProbabilityImage(imageFile, frameIndex, 1.0f);

        List<AdvancedComponent<FloatType>> roots = tree.rootsSorted();

        int counter = 0;
        for (AdvancedComponent<FloatType> root : roots) {
            List<AdvancedComponent<FloatType>> components = root.getComponentsBelowClosestToRoot();
            if (!components.isEmpty()){
                ImagePlus imp = ImageJFunctions.show(Plotting.createImageWithComponents(components, new ArrayList<>()));
                TextRoi text = new TextRoi(0, 0, String.format("i=%d", counter));
                imp.setOverlay(text, Color.white, 0, Color.black);
            }
            counter++;
        }
    }

    public void testPrintRankOfSegment() throws IOException {
        String imageFile = new File("").getAbsolutePath() + "/src/test/resources/00_probability_maps/cropped__20200922_M9glc_VNG1040-hi2_AB_1_MMStack_Pos0_GL30__probability_map_frame_126_duplicated_frame__20210812.tif";
        int frameIndex = 0;

        SimpleComponentTree<FloatType, AdvancedComponent<FloatType>> tree = (SimpleComponentTree<FloatType, AdvancedComponent<FloatType>>) getComponentTreeFromProbabilityImage(imageFile, frameIndex, 1.0f);

        List<AdvancedComponent<FloatType>> roots = tree.rootsSorted();

        for (AdvancedComponent<FloatType> root : roots) {
            ArrayList<AdvancedComponent<FloatType>> componentsToDraw = new ArrayList<>();
            componentsToDraw.add(root);
            ImagePlus imp = ImageJFunctions.show(Plotting.createImageWithComponents(componentsToDraw, new ArrayList<>()));
            int rank = root.getRankRelativeToComponentsClosestToRoot();
            TextRoi text = new TextRoi(0, 0, String.format("rank: %d", rank));
            imp.setOverlay(text, Color.white, 0, Color.black);
        }
    }

    @NotNull
    private ComponentTreeGenerator getComponentTreeGenerator(ImageJ ij) {
        OpService ops = ij.op();
        Imglib2Utils imglib2Utils = new Imglib2Utils(ops);
        ComponentProperties componentProperties = new ComponentProperties(ops, imglib2Utils);
        RecursiveComponentWatershedder recursiveComponentWatershedder = new RecursiveComponentWatershedder(ij.op());
        WatershedMaskGenerator watershedMaskGenerator = new WatershedMaskGenerator(0.5f, 0.5f);
        ComponentTreeGeneratorConfigurationMock config = new ComponentTreeGeneratorConfigurationMock(60, Integer.MIN_VALUE);
        ComponentTreeGenerator componentTreeGenerator = new ComponentTreeGenerator(config, recursiveComponentWatershedder, componentProperties, watershedMaskGenerator, imglib2Utils);
        return componentTreeGenerator;
    }

    private ComponentForest<AdvancedComponent<FloatType>> getComponentTreeFromProbabilityImage(String imageFile, int frameIndex, float componentSplittingThreshold) throws IOException {
        assertTrue(new File(imageFile).exists());

        ImageJ ij = new ImageJ();
        Img input = (Img) ij.io().open(imageFile);
        assertNotNull(input);

        IImageProvider imageProviderMock = new ImageProviderMock(input);
        RandomAccessibleInterval<FloatType> currentImage = Views.hyperSlice(input, 2, frameIndex);
        assertEquals(2, currentImage.numDimensions());

//        ImageJFunctions.show(currentImage);

        ComponentTreeGenerator componentTreeGenerator = getComponentTreeGenerator(ij);

        ComponentForest<AdvancedComponent<FloatType>> tree = componentTreeGenerator.buildIntensityTree(imageProviderMock, frameIndex, componentSplittingThreshold);
        return tree;
    }
}
