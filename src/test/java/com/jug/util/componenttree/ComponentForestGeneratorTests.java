package com.jug.util.componenttree;

import com.google.common.collect.Lists;
import com.jug.datahandling.IImageProvider;
import com.jug.util.PseudoDic;
import com.jug.util.TestUtils;
import com.jug.util.imglib2.Imglib2Utils;
import com.moma.auxiliary.Plotting;
import ij.ImagePlus;
import ij.gui.TextRoi;
import net.imglib2.algorithm.binary.Thresholder;
import net.imglib2.algorithm.componenttree.ComponentForest;
import net.imglib2.algorithm.componenttree.mser.MserTree;
import net.imglib2.img.Img;
import net.imglib2.img.ImgView;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.real.FloatType;
import org.junit.Test;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ComponentForestGeneratorTests {
    private final TestUtils testUtils;

    public ComponentForestGeneratorTests() {
        testUtils = new TestUtils();
    }

    public static void main(String... args) throws IOException, InterruptedException {
//        new ComponentForestGeneratorTests().testWatershedding();
//        new ComponentForestGeneratorTests().testSegmentAreaCalculationOfChildren();
//        new ComponentForestGeneratorTests().testPrintRankOfSegment();
//        new ComponentForestGeneratorTests().root_components__return__correct_hash_code();
        new ComponentForestGeneratorTests().debugThreeWayComponentSegmentation();
//        new ComponentForestGeneratorTests().debugThreeWayComponentSegmentation__check_MserTree();
//        new ComponentForestGeneratorTests().debugThreeWayComponentSegmentation__test_new_tree_generation_method();
    }

    public void debugThreeWayComponentSegmentation__test_new_tree_generation_method() throws IOException {
        String imageFile = new File("").getAbsolutePath() + "/src/test/resources/00_probability_maps/20201119_VNG1040_AB2h_2h_1__Pos5_GL17/cropped__20201119_VNG1040_AB2h_2h_1_MMStack_Pos5_GL17__model_9e5727e4ed18802f4ab04c7494ef8992d798f4d64d5fd75e285b9a3d83b13ac9.tif";
        int frameIndex = 0;

        IImageProvider imageProvider = testUtils.getImageProvider(imageFile);

        PseudoDic dic = new PseudoDic();
        WatershedMaskGenerator watershedMaskGenerator = dic.getWatershedMaskGenerator();
        watershedMaskGenerator.setThreshold(0.5f);
        watershedMaskGenerator.setThresholdForComponentMerging(0.5f);
        Imglib2Utils imglib2Utils = dic.getImglib2utils();
        float componentSplittingThreshold = 1.0f;

        Img<FloatType> raiFkt = imageProvider.getImgProbsAt(frameIndex);

        /* generate image mask for component generation; watershedMaskGenerator.generateMask(...) also merges adjacent connected components, if values between do fall below a given cutoff (see implementation) */
        Img<BitType> mask = watershedMaskGenerator.generateMask(ImgView.wrap(raiFkt));

        /* fill holes in water shedding mask to avoid components from having holes */
        mask = ImgView.wrap(imglib2Utils.fillHoles(mask));

        raiFkt = imglib2Utils.maskImage(raiFkt, mask, new FloatType(.0f));

        /* set values >componentSplittingThreshold to 1; this avoids over segmentation during component generation */
        Img<BitType> mask2 = Thresholder.threshold(raiFkt, new FloatType(componentSplittingThreshold), false, 1);
        raiFkt = imglib2Utils.maskImage(raiFkt, mask2, new FloatType(1.0f));


        final double delta = 0.0001;
//        final double delta = 0.02;
        final int minSize = 5; // this sets the minimum size of components during component generation for root components as well as child components. We set this to a low value to ensure a deep segmentation of our components. The minimum size of root and child components is then filtered using LeafComponentSizeTester and RootComponentSizeTester (see below).
        final long maxSize = Long.MAX_VALUE;
        final double maxVar = 1.0;
        final double minDiversity = 0.2;
        final boolean darkToBright = false;

        // generate MSER tree
        MserTree<FloatType> componentTree = MserTree.buildMserTree(raiFkt, delta, minSize, maxSize, maxVar, minDiversity, darkToBright);


//        for (AdvancedComponent component2 : tree.getAllComponents()) {
//            ImageJFunctions.show(Plotting.createImageWithComponent(component2));
//        }

        Plotting.drawComponentTree2(componentTree, new ArrayList<>(), raiFkt);
    }

    public void debugThreeWayComponentSegmentation__check_MserTree() throws IOException {
        String imageFile = new File("").getAbsolutePath() + "/src/test/resources/00_probability_maps/20201119_VNG1040_AB2h_2h_1__Pos5_GL17/cropped__20201119_VNG1040_AB2h_2h_1_MMStack_Pos5_GL17__model_9e5727e4ed18802f4ab04c7494ef8992d798f4d64d5fd75e285b9a3d83b13ac9.tif";
        int frameIndex = 0;

        IImageProvider imageProvider = testUtils.getImageProvider(imageFile);

        PseudoDic dic = new PseudoDic();
        WatershedMaskGenerator watershedMaskGenerator = dic.getWatershedMaskGenerator();
        watershedMaskGenerator.setThreshold(0.5f);
        watershedMaskGenerator.setThresholdForComponentMerging(0.5f);
        Imglib2Utils imglib2Utils = dic.getImglib2utils();
        float componentSplittingThreshold = 1.0f;

        Img<FloatType> raiFkt = imageProvider.getImgProbsAt(frameIndex);

        /* generate image mask for component generation; watershedMaskGenerator.generateMask(...) also merges adjacent connected components, if values between do fall below a given cutoff (see implementation) */
        Img<BitType> mask = watershedMaskGenerator.generateMask(ImgView.wrap(raiFkt));

        /* fill holes in water shedding mask to avoid components from having holes */
        mask = ImgView.wrap(imglib2Utils.fillHoles(mask));

        raiFkt = imglib2Utils.maskImage(raiFkt, mask, new FloatType(.0f));

        /* set values >componentSplittingThreshold to 1; this avoids over segmentation during component generation */
        Img<BitType> mask2 = Thresholder.threshold(raiFkt, new FloatType(componentSplittingThreshold), false, 1);
        raiFkt = imglib2Utils.maskImage(raiFkt, mask2, new FloatType(1.0f));


        final double delta = 0.0001;
//        final double delta = 0.02;
        final int minSize = 5; // this sets the minimum size of components during component generation for root components as well as child components. We set this to a low value to ensure a deep segmentation of our components. The minimum size of root and child components is then filtered using LeafComponentSizeTester and RootComponentSizeTester (see below).
        final long maxSize = Long.MAX_VALUE;
        final double maxVar = 1.0;
        final double minDiversity = 0.2;
        final boolean darkToBright = false;

        // generate MSER tree
        MserTree<FloatType> componentTree = MserTree.buildMserTree(raiFkt, delta, minSize, maxSize, maxVar, minDiversity, darkToBright);


//        for (AdvancedComponent component2 : tree.getAllComponents()) {
//            ImageJFunctions.show(Plotting.createImageWithComponent(component2));
//        }

        Plotting.drawComponentTree2(componentTree, new ArrayList<>(), raiFkt);
    }

    public void debugThreeWayComponentSegmentation() throws IOException {
        String imageFile = new File("").getAbsolutePath() + "/src/test/resources/00_probability_maps/20201119_VNG1040_AB2h_2h_1__Pos5_GL17/cropped__20201119_VNG1040_AB2h_2h_1_MMStack_Pos5_GL17__model_9e5727e4ed18802f4ab04c7494ef8992d798f4d64d5fd75e285b9a3d83b13ac9.tif";
        int frameIndex = 0;

        AdvancedComponentForest<FloatType, AdvancedComponent<FloatType>> tree = testUtils.getComponentTreeFromProbabilityImage(Paths.get(imageFile), frameIndex, 1.0f);

//        for (AdvancedComponent component2 : tree.getAllComponents()) {
//            ImageJFunctions.show(Plotting.createImageWithComponent(component2));
//        }
        Plotting.drawComponentTree2(tree, new ArrayList<>(), tree.getSourceImage());
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

        AdvancedComponentForest<FloatType, AdvancedComponent<FloatType>> tree = testUtils.getComponentTreeFromProbabilityImage(Paths.get(imageFile), frameIndex, 1.0f);
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

        ComponentForest<AdvancedComponent<FloatType>> tree = testUtils.getComponentTreeFromProbabilityImage(Paths.get(imageFile), frameIndex, 1.0f);
        Plotting.drawComponentTree2(tree, new ArrayList<>(), tree.roots().iterator().next().getSourceImage());
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

        AdvancedComponentForest<FloatType, AdvancedComponent<FloatType>> tree = testUtils.getComponentTreeFromProbabilityImage(Paths.get(imageFile), frameIndex, 1.0f);

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

        AdvancedComponentForest<FloatType, AdvancedComponent<FloatType>> tree = testUtils.getComponentTreeFromProbabilityImage(Paths.get(imageFile), frameIndex, 1.0f);

        List<AdvancedComponent<FloatType>> roots = tree.rootsSorted();

        int counter = 0;
        for (AdvancedComponent<FloatType> root : roots) {
            List<AdvancedComponent<FloatType>> components = root.getComponentsBelowClosestToRoot();
            if (!components.isEmpty()){
                ImagePlus imp = ImageJFunctions.show(Plotting.createImageWithComponents(components, new ArrayList<>(), root.getSourceImage()));
                TextRoi text = new TextRoi(0, 0, String.format("i=%d", counter));
                imp.setOverlay(text, Color.white, 0, Color.black);
            }
            counter++;
        }
    }

    public void testPrintRankOfSegment() throws IOException {
        String imageFile = new File("").getAbsolutePath() + "/src/test/resources/00_probability_maps/cropped__20200922_M9glc_VNG1040-hi2_AB_1_MMStack_Pos0_GL30__probability_map_frame_126_duplicated_frame__20210812.tif";
        int frameIndex = 0;

        AdvancedComponentForest<FloatType, AdvancedComponent<FloatType>> tree = testUtils.getComponentTreeFromProbabilityImage(Paths.get(imageFile), frameIndex, 1.0f);

        List<AdvancedComponent<FloatType>> roots = tree.rootsSorted();

        for (AdvancedComponent<FloatType> root : roots) {
            ArrayList<AdvancedComponent<FloatType>> componentsToDraw = new ArrayList<>();
            componentsToDraw.add(root);
            ImagePlus imp = ImageJFunctions.show(Plotting.createImageWithComponents(componentsToDraw, new ArrayList<>(), root.getSourceImage()));
            int rank = root.getRankRelativeToComponentsClosestToRoot();
            TextRoi text = new TextRoi(0, 0, String.format("rank: %d", rank));
            imp.setOverlay(text, Color.white, 0, Color.black);
        }
    }
}
