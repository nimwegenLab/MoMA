package com.jug.util.componenttree;

import com.jug.util.TestUtils;
import ij.ImagePlus;
import net.imagej.ImageJ;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.ValuePair;
import org.apache.commons.lang.NotImplementedException;
import sc.fiji.skeletonize3D.Skeletonize3D_;

import java.io.File;
import java.io.IOException;

public class MedialLineCalculatorTest {
    private final ImageJ ij;
    private final TestUtils testUtils;

    public MedialLineCalculatorTest() {
        ij = new ImageJ();
        testUtils = new TestUtils(ij);
    }

    public static void main(String... args) throws IOException {
        ImageJ ij = new ImageJ();
        ij.ui().showUI();
        new MedialLineCalculatorTest().exploreMedialLineCalculator();
    }

    /**
     * Add test for gerating the component tree on a sample image and displaying it.
     *
     * @throws IOException
     * @throws InterruptedException
     */
    public void exploreMedialLineCalculator() throws IOException {
        String imageFile = new File("").getAbsolutePath() + "/src/test/resources/00_probability_maps/probabilities_watershedding_000.tif";
        int componentIndex = 3;

        ValuePair<AdvancedComponent<FloatType>, RandomAccessibleInterval<ARGBType>> componentAndImage = testUtils.getComponentWithImage(imageFile,
                componentIndex,
                new ARGBType(ARGBType.rgba(255, 255, 255, 255)));

        AdvancedComponent<FloatType> component = componentAndImage.getA();
        RandomAccessibleInterval<ARGBType> image = componentAndImage.getB();


        MedialLineCalculator sut = new MedialLineCalculator(ij.context());

        ImagePlus imp = ImageJFunctions.wrap(image, "name");


//        RandomAccessibleInterval<BitType>

        Skeletonize3D_ skProc = new Skeletonize3D_();
        skProc.setup("", imp);
        skProc.run(imp.getProcessor());

        imp.show();

        if(true) return;

        skProc.setup("", imp);
        skProc.run(imp.getProcessor());

        if(true) throw new NotImplementedException("This test is not yet implemented");
//        LabelRegionToPolygonConverter regionToPolygonConverter = new LabelRegionToPolygonConverter();
//        regionToPolygonConverter.setContext(ij.context());
//        final Polygon2D poly = regionToPolygonConverter.convert(component.getRegion(), Polygon2D.class);
////        Polygon2D orientedBoundingBoxPolygon = boundingBoxCalculator.calculate(poly);
//
//        DefaultConvexHull2D convexHullCalculator = new DefaultConvexHull2D();
//        Polygon2D polyHull = convexHullCalculator.calculate(poly);
//
//        List<MaskPredicate<?>> rois = Arrays.asList(
//                poly,
//                polyHull
//        );
//        testUtils.showImageWithOverlays(image, rois);
    }
}