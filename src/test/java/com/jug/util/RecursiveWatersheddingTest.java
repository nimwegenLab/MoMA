package com.jug.util;

import com.jug.util.componenttree.ComponentTreeGenerator;
import com.jug.util.componenttree.SimpleComponent;
import com.moma.auxiliary.Plotting;
import net.imagej.ImageJ;
import net.imagej.ops.OpService;
import net.imagej.ops.image.watershed.WatershedSeeded;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.componenttree.ComponentForest;
import net.imglib2.algorithm.labeling.ConnectedComponents;
import net.imglib2.algorithm.neighborhood.RectangleShape;
import net.imglib2.img.Img;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.roi.labeling.LabelRegion;
import net.imglib2.roi.labeling.LabelRegions;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.Views;
import org.junit.Test;
import org.scijava.Context;
import org.scijava.io.IOService;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import static org.junit.Assert.*;

public class RecursiveWatersheddingTest {

    private OpService ops = (new Context()).service(OpService.class);
    private IOService io = (new Context()).service(IOService.class);

    public static void main(String... args) throws IOException, InterruptedException {
        ImageJ ij = new ImageJ();
        ij.ui().showUI();
//        new RecursiveWatersheddingTest().watersheddingTest();
        new RecursiveWatersheddingTest().test();
    }

    @Test
    public void testWatershedding() throws IOException, InterruptedException {
        String imageFile = new File("").getAbsolutePath() + "/src/test/resources/probabilities_watershedding_000.tif";
        assertTrue(new File(imageFile).exists());

        ImageJ ij = new ImageJ();
        Img input = (Img) ij.io().open(imageFile);
        assertNotNull(input);

        RandomAccessibleInterval<FloatType> currentImage = Views.hyperSlice(input, 2, 45);
        assertEquals(2, currentImage.numDimensions());

        ImageJFunctions.show(currentImage);
        ComponentForest<SimpleComponent<FloatType>> tree = new ComponentTreeGenerator().buildIntensityTree(currentImage);
        Plotting.drawComponentTree2(tree, new ArrayList<>());
    }

    @Test
    public void watersheddingTest() throws IOException {
        ImageJ ij = new ImageJ();
        String imageFile = new File("").getAbsolutePath() + "/src/test/resources/parent_watershedding/sourceImage.tif";
        Img sourceImage = (Img) ij.io().open(imageFile);
        imageFile = new File("").getAbsolutePath() + "/src/test/resources/parent_watershedding/mserTreeLevel4.tif";
        Img imageParentTmp = (Img) ij.io().open(imageFile);
        RandomAccessibleInterval<UnsignedByteType> imageParent = Views.hyperSlice(imageParentTmp, 2, 0);
        RandomAccessibleInterval<BitType> imageParent2 = ij.op().convert().bit(Views.iterable(imageParent));
        ImageJFunctions.show(imageParent2);
        imageFile = new File("").getAbsolutePath() + "/src/test/resources/parent_watershedding/mserTreeLevel5.tif";
        Img imageChildTmp = (Img) ij.io().open(imageFile);
        RandomAccessibleInterval<UnsignedByteType> imageChild = Views.hyperSlice(imageChildTmp, 2, 0);
        imageChild = (RandomAccessibleInterval) ops.morphology().erode(imageChild, new RectangleShape(2, false));
        ImgLabeling<Integer, IntType> labels = ij.op().labeling().cca(imageChild, ConnectedComponents.StructuringElement.FOUR_CONNECTED);
        ij.ui().show(labels.getIndexImg());
        ImgLabeling labelsOut = ops.image().watershed(null, sourceImage, labels, false, false, imageParent2);
        ImageJFunctions.show(labelsOut.getIndexImg());
    }

    @Test
    public void test() throws IOException {
        String imageFile = new File("").getAbsolutePath() + "/src/test/resources/parent_watershedding/sourceImage.tif";
        Img sourceImage = (Img) io.open(imageFile);
        imageFile = new File("").getAbsolutePath() + "/src/test/resources/parent_watershedding/mserTreeLevel4.tif";
        Img imageParentTmp = (Img) io.open(imageFile);
        RandomAccessibleInterval<UnsignedByteType> imageParent = Views.hyperSlice(imageParentTmp, 2, 0);
        RandomAccessibleInterval<BitType> mask = ops.convert().bit(Views.iterable(imageParent));
//        ImageJFunctions.show(imageParentTmp);

        imageFile = new File("").getAbsolutePath() + "/src/test/resources/parent_watershedding/mserTreeLevel5.tif";
        Img imageChildTmp = (Img) io.open(imageFile);
        RandomAccessibleInterval<UnsignedByteType> imageChild = Views.hyperSlice(imageChildTmp, 2, 0);
        imageChild = (RandomAccessibleInterval) ops.morphology().erode(imageChild, new RectangleShape(2, false));
        ImgLabeling<Integer, IntType> labeledSeeds = ops.labeling().cca(imageChild, ConnectedComponents.StructuringElement.FOUR_CONNECTED);

        Img<FloatType> input = sourceImage.factory().create(sourceImage);
        ops.image().invert(input, sourceImage);

        testWithMask(input, labeledSeeds, mask);
    }

    private void testWithMask(final RandomAccessibleInterval<FloatType> in, final ImgLabeling<Integer, IntType> seedLabels, RandomAccessibleInterval<BitType> mask) {

        ImgLabeling<Integer, IntType> out = (ImgLabeling<Integer, IntType>) ops.run(WatershedSeeded.class, null, in,
                seedLabels, true, false, mask);

        printRegionSizes(seedLabels);
        System.out.println("------");
        printRegionSizes(out);

//        ImageJFunctions.show(in);
//        ImageJFunctions.show(seedLabels.getIndexImg());
//        ImageJFunctions.show(mask);
        ImageJFunctions.show(out.getIndexImg());
    }

    private void printRegionSizes(ImgLabeling<Integer, IntType> seedLabels) {
        LabelRegions<Integer> regions = new LabelRegions<>(seedLabels);
        Iterator<LabelRegion<Integer>> regionIterator = regions.iterator();
        while (regionIterator.hasNext()) {
            LabelRegion<Integer> region = regionIterator.next();
            System.out.println(String.format("label: %d", region.getLabel()));
            System.out.println(String.format("region size: %d", region.size()));
        }
    }
}
