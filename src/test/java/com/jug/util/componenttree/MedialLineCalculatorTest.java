package com.jug.util.componenttree;

import com.jug.util.TestUtils;
import ij.ImagePlus;
import net.imagej.ImageJ;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.roi.MaskPredicate;
import net.imglib2.roi.geom.GeomMasks;
import net.imglib2.roi.geom.real.Polygon2D;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.ValuePair;
import sc.fiji.analyzeSkeleton.AnalyzeSkeleton_;
import sc.fiji.analyzeSkeleton.Point;
import sc.fiji.analyzeSkeleton.SkeletonResult;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

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
//        String imageFile = new File("").getAbsolutePath() + "/src/test/resources/00_probability_maps/probabilities_watershedding_000.tif";
        String imageFile = new File("").getAbsolutePath() + "/src/test/resources/00_probability_maps/20201119_VNG1040_AB2h_2h_1_MMStack_Pos6_GL6/frame90_repeated__cropped__20201119_VNG1040_AB2h_2h_1_MMStack_Pos6_GL6__model_9e5727e4ed18802f4ab04c7494ef8992d798f4d64d5fd75e285b9a3d83b13ac9.tif";

//        testUtils.drawComponentTree(imageFile);
//        if(true) return;

        int componentIndex = 4;

        ValuePair<AdvancedComponent<FloatType>, RandomAccessibleInterval<BitType>> componentAndImage = testUtils.getComponentWithImage(imageFile,
                componentIndex,
                new BitType(true));
        RandomAccessibleInterval<BitType> image = componentAndImage.getB();

        RandomAccessibleInterval raiThinned = ij.op().morphology().thinGuoHall(image);

        ImagePlus imp_thinned = ImageJFunctions.wrap(raiThinned, "thinned_image");

        AnalyzeSkeleton_ skel = new AnalyzeSkeleton_();
        skel.calculateShortestPath = true;
        skel.setup("", imp_thinned);
        long t1 = System.currentTimeMillis();
        SkeletonResult skelResult = skel.run(AnalyzeSkeleton_.NONE, false, true, null, true, false);
        long t2 = System.currentTimeMillis();
        double exectime = (double) (t2 - t1) / 1000.0;
        System.out.println("run time in seconds: " + exectime);

        List<Point>[] sppoints = skel.getShortestPathPoints();

        Polygon2D poly = convertToPolygon(sppoints[0]);
        List<MaskPredicate<?>> rois = Arrays.asList(poly);
        testUtils.showImageWithOverlays(raiThinned, rois);

        System.out.println("");

//        MedialLineCalculator sut = new MedialLineCalculator(ij.context());
//
//        ImagePlus imp = ImageJFunctions.wrap(image, "name");
//
//        imp.show();

//        Skeletonize3D_ skProc = new Skeletonize3D_();
//        skProc.setup("", imp);
//        skProc.run(imp.getProcessor());
//
//        imp.show();

//
//        if(true) return;
//
//        skProc.setup("", imp);
//        skProc.run(imp.getProcessor());
//
//        if(true) throw new NotImplementedException("This test is not yet implemented");
////        LabelRegionToPolygonConverter regionToPolygonConverter = new LabelRegionToPolygonConverter();
////        regionToPolygonConverter.setContext(ij.context());
////        final Polygon2D poly = regionToPolygonConverter.convert(component.getRegion(), Polygon2D.class);
//////        Polygon2D orientedBoundingBoxPolygon = boundingBoxCalculator.calculate(poly);
////
////        DefaultConvexHull2D convexHullCalculator = new DefaultConvexHull2D();
////        Polygon2D polyHull = convexHullCalculator.calculate(poly);
////
////        List<MaskPredicate<?>> rois = Arrays.asList(
////                poly,
////                polyHull
////        );
////        testUtils.showImageWithOverlays(image, rois);
    }

    public Polygon2D convertToPolygon(List<Point> points) {
//        double[] xp = new double[]{49, 47, 47, 50, 52, 53, 54, 56, 60, 61, 61, 59, 58, 56, 54};
//        double[] yp = new double[]{352, 354, 361, 375, 381, 383, 384, 385, 385, 384, 376, 362, 357, 353, 352};
//        xp = new Array
        double[] xp = new double[points.size()];
        double[] yp = new double[points.size()];
        int index = 0;
        for(Point point : points){
            xp[index] = point.x;
            yp[index] = point.y;
            index++;
        }
        return GeomMasks.polygon2D(xp, yp);
    }
}