package com.jug.util;
import com.jug.util.componenttree.*;
import com.jug.util.filteredcomponents.FilteredComponent;
import com.moma.auxiliary.Plotting;
import net.imagej.ImageJ;
import net.imagej.ops.image.watershed.WatershedSeeded;
import net.imglib2.Cursor;
import net.imglib2.IterableInterval;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.componenttree.ComponentForest;
import net.imglib2.algorithm.componenttree.mser.MserTree;
import net.imglib2.algorithm.labeling.ConnectedComponents;
import net.imglib2.algorithm.neighborhood.RectangleShape;
import net.imglib2.algorithm.neighborhood.Shape;
import net.imglib2.converter.Converter;
import net.imglib2.converter.Converters;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImg;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.img.basictypeaccess.array.IntArray;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.roi.IterableRegion;
import net.imglib2.roi.Regions;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.roi.labeling.LabelRegion;
import net.imglib2.roi.labeling.LabelRegions;
import net.imglib2.roi.labeling.LabelingType;
import net.imglib2.type.BooleanType;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;
import org.junit.Assert;
import org.junit.Test;
import org.scijava.Context;
import org.scijava.io.IOService;
import org.scijava.util.MersenneTwisterFast;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;
import net.imagej.ops.OpService;
import static java.lang.Thread.sleep;
import static org.junit.Assert.*;

public class RecursiveWatersheddingTest {

    @Test
    public void testWatershedding() throws IOException, InterruptedException {
        String imageFile = new File("").getAbsolutePath() + "/src/test/resources/probabilities_watershedding_000.tif";
        assertTrue(new File(imageFile).exists());

        ImageJ ij = new ImageJ();
        Img input = (Img) ij.io().open(imageFile);
        assertNotNull(input);

        RandomAccessibleInterval<FloatType> currentImage = Views.hyperSlice(input, 2, 45);
        assertEquals(2, currentImage.numDimensions());

        float threshold = 0.1f;
        currentImage = setZero(currentImage, threshold);
        ImageJFunctions.show(currentImage);
        ComponentForest<FilteredComponent<FloatType>> tree = buildIntensityTree(currentImage);
        Plotting.drawComponentTree2(tree, new ArrayList<>());
    }

    private static final RandomAccessibleInterval< FloatType > setZero(final RandomAccessibleInterval< FloatType > image, float threshold){
        IterableInterval< FloatType > iterableSource = Views.iterable(image);
        Cursor<FloatType> cursor = iterableSource.cursor();
        while(cursor.hasNext()){
            cursor.next();
            float val = cursor.get().getRealFloat();
            if(val > 0.0f && val < threshold){
                cursor.get().set(0);
            }
        }
        return image;
    }

    protected ComponentForest< FilteredComponent< FloatType >> buildIntensityTree(final RandomAccessibleInterval< FloatType > raiFkt ) {
        float threshold = 0.1f;
        IterableInterval< FloatType > iterableSource = Views.iterable(raiFkt);
        Cursor<FloatType> cursor = iterableSource.cursor();
        while(cursor.hasNext()){
            cursor.next();
            float val = cursor.get().getRealFloat();
            if(val > 0.0f && val < threshold){
                cursor.get().set(0);
            }
        }

		final double delta = 0.0001;
//        final double delta = 0.02;
        final int minSize = 50; // minSize=50px seems safe, assuming pixel-area of a round cell with radius of have the bacterial width: 3.141*0.35**2/0.065**2, where pixelSize=0.065mu and width/2=0.35mu
        final long maxSize = Long.MAX_VALUE;
        final double maxVar = 1.0;
        final double minDiversity = 0.5;
        final boolean darkToBright = false;
        MserTree<FloatType> componentTree = MserTree.buildMserTree(raiFkt, delta, minSize, maxSize, maxVar, minDiversity, darkToBright);

        Predicate<Integer> widthCondition = (width) -> (width <= 20);
        ILocationTester ctester = new ComponentExtentTester(0, widthCondition);
//        Predicate<Integer> condition = (pos) -> (pos >= GL_OFFSET_TOP && pos <= raiFkt.dimension(1) - GL_OFFSET_BOTTOM);
//        ILocationTester boundaryTester = new PixelPositionTester(1, condition);
        ArrayList<ILocationTester> testers = new ArrayList<>();
        testers.add(ctester);
//        testers.add(boundaryTester);
        ComponentTester<FloatType, FilteredComponent<FloatType>> tester = new ComponentTester<>(testers);

        SimpleComponentTree tree = new SimpleComponentTree(componentTree, raiFkt, tester);
//        HasSiblingTester<?,?> tester2 = new HasSiblingTester<>();
//        SimpleComponentTree tree2 = new SimpleComponentTree(tree, raiFkt, tester2);
        return tree;
    }

    private OpService ops = ( new Context()).service(OpService.class);
    private IOService io = ( new Context()).service(IOService.class);


    @Test
    public void watersheddingTest() throws IOException {
        ImageJ ij = new ImageJ();
//        ij.launch();
        String imageFile = new File("").getAbsolutePath() + "/src/test/resources/parent_watershedding/sourceImage.tif";
        Img sourceImage = (Img) ij.io().open(imageFile);
//        RandomAccessibleInterval<FloatType>  sourceImage = Views.hyperSlice(sourceImageTmp, 2, 0);
        imageFile = new File("").getAbsolutePath() + "/src/test/resources/parent_watershedding/mserTreeLevel4.tif";
        Img imageParentTmp = (Img) ij.io().open(imageFile);
        RandomAccessibleInterval<UnsignedByteType>  imageParent = Views.hyperSlice(imageParentTmp, 2, 0);
        RandomAccessibleInterval<BitType>  imageParent2 = ij.op().convert().bit(Views.iterable(imageParent));
        ImageJFunctions.show(imageParent2);
        imageFile = new File("").getAbsolutePath() + "/src/test/resources/parent_watershedding/mserTreeLevel5.tif";
        Img imageChildTmp = (Img) ij.io().open(imageFile);
        RandomAccessibleInterval<UnsignedByteType>  imageChild = Views.hyperSlice(imageChildTmp, 2, 0);
//        ImgLabeling labels = new ImgLabeling(imageChild);
        imageChild = (RandomAccessibleInterval) ops.morphology().erode(imageChild, new RectangleShape(2,false));
        ImgLabeling<Integer, IntType> labels = ij.op().labeling().cca(imageChild, ConnectedComponents.StructuringElement.FOUR_CONNECTED);
        ij.ui().show(labels.getIndexImg());
//        ArrayImg<IntType, IntArray> backing = ArrayImgs.ints(sourceImage.dimension(0), sourceImage.dimension(1));
//        ImgLabeling<Integer, IntType> labelsOut = new ImgLabeling<>(backing);
//        ImgLabeling labelsOut = new ImgLabeling(imageChild);
//        ImageJFunctions.show(imageParent);
//        ImageJFunctions.show(imageChild);
//        ImageJFunctions.show(sourceImage);
//        ij.ops
        ImgLabeling labelsOut = ops.image().watershed(null, sourceImage, labels, false, false, imageParent2);
        ImageJFunctions.show(labelsOut.getIndexImg());
    }


    public static void main(String ... args) throws IOException, InterruptedException {
        new RecursiveWatersheddingTest().watersheddingTest();
//        new RecursiveWatersheddingTest().test();
    }

    @Test
    public void test() throws IOException {
        ImageJ ij = new ImageJ();
        String imageFile = new File("").getAbsolutePath() + "/src/test/resources/parent_watershedding/sourceImage.tif";
        Img sourceImage = (Img) io.open(imageFile);
        imageFile = new File("").getAbsolutePath() + "/src/test/resources/parent_watershedding/mserTreeLevel4.tif";
        Img imageParentTmp = (Img) io.open(imageFile);
        RandomAccessibleInterval<UnsignedByteType> imageParent = Views.hyperSlice(imageParentTmp, 2, 0);
        RandomAccessibleInterval<BitType> mask = ij.op().convert().bit(Views.iterable(imageParent));
        ImageJFunctions.show(imageParentTmp);

        imageFile = new File("").getAbsolutePath() + "/src/test/resources/parent_watershedding/mserTreeLevel5.tif";
        Img imageChildTmp = (Img) io.open(imageFile);
        RandomAccessibleInterval<UnsignedByteType>  imageChild = Views.hyperSlice(imageChildTmp, 2, 0);
        imageChild = (RandomAccessibleInterval) ops.morphology().erode(imageChild, new RectangleShape(2,false));
        ImgLabeling<Integer, IntType> labeledSeeds = ij.op().labeling().cca(imageChild, ConnectedComponents.StructuringElement.FOUR_CONNECTED);

        Img<FloatType> input = sourceImage.factory().create(sourceImage);
        ops.image().invert(input, sourceImage);

        testWithMask(input, labeledSeeds, mask);
    }

    private void testWithMask(final RandomAccessibleInterval<FloatType> in, final ImgLabeling<Integer, IntType> seedLabels, RandomAccessibleInterval<BitType> mask) {
//        LabelRegions regions = new LabelRegions<>(seedLabels);
//        for (LabelRegion region : regions){
//
//        }

        ImgLabeling<Integer, IntType> out = (ImgLabeling<Integer, IntType>) ops.run(WatershedSeeded.class, null, in,
                seedLabels, true, false, mask);

        ImageJFunctions.show(in);
        ImageJFunctions.show(seedLabels.getIndexImg());
        ImageJFunctions.show(mask);
        ImageJFunctions.show(out.getIndexImg());
    }
}
