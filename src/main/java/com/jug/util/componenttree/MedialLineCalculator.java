package com.jug.util.componenttree;

import com.jug.util.imglib2.Imglib2Utils;
import com.jug.util.math.Vector2DPolygon;
import net.imagej.ops.OpService;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.roi.geom.real.Polygon2D;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.roi.labeling.LabelRegion;
import net.imglib2.roi.labeling.LabelRegions;
import net.imglib2.type.logic.BitType;
import net.imglib2.algorithm.labeling.ConnectedComponents;
import net.imglib2.type.numeric.integer.IntType;
//import net.imglib2.algorithm.interpolation.randomaccess.BSplineInterpolator;


public class MedialLineCalculator {
    private OpService ops;
    private Imglib2Utils imglib2Utils;

    public MedialLineCalculator(OpService ops, Imglib2Utils imglib2Utils) {
        this.ops = ops;
        this.imglib2Utils = imglib2Utils;
    }

    public Vector2DPolygon calculate(RandomAccessibleInterval<BitType> image) {
        RandomAccessibleInterval raiThinned = ops.morphology().thinGuoHall(image);
//        result = ArrayImgFactory.imgFactory(new IntType());
//        new ArrayImgFactory(new IntType());

        ImgLabeling labeling = ops.labeling().cca(raiThinned, ConnectedComponents.StructuringElement.EIGHT_CONNECTED);
        LabelRegions regions = new LabelRegions(labeling);
        LabelRegion skeletonRegion = regions.getLabelRegion(0);
//        Polygon2D poly = imglib2Utils.convertRegionToVector2DPolygon(skeletonRegion);
        Vector2DPolygon polygon = Vector2DPolygon.createFromCursor(skeletonRegion.cursor());
        return polygon;
//        Img<IntType> labeling = utils.createImageWithSameDimension(raiThinned, new IntType());
//        ImgLabeling<Integer, IntType> imglabeling = createLabelingImage(raiThinned);
//
////        labelAllConnectedComponents(RandomAccessible<T> input, ImgLabeling<L, I> labeling, Iterator<L> labelGenerator, ConnectedComponents.StructuringElement se)
//
//        Iterator<L> labelGenerator
//        ConnectedComponents.labelAllConnectedComponents(raiThinned, imglabeling, labelGenerator, ConnectedComponents.StructuringElement.EIGHT_CONNECTED);
////        FloatType res = new FloatType(1.0f);
////        ImgLabeling labeling2 = new ImgLabeling<>(labeling);
//        ConnectedComponents.labelAllConnectedComponents(raiThinned, labeling, ConnectedComponents.StructuringElement.EIGHT_CONNECTED);
////        LabelRegions<Integer> regions = new LabelRegions<>(labeling2);
//        res = regions.getLabelRegion(0);
//        ConnectedComponentAnalysis.connectedComponents(raiThinned);
//        System.out.println("stop");
    }

    private ImgLabeling<Integer, IntType> createLabelingImage(RandomAccessibleInterval sourceImage) {
        long[] dims = new long[sourceImage.numDimensions()];
        sourceImage.dimensions(dims);
        Img<IntType> img = ArrayImgs.ints(dims);
        return new ImgLabeling<>(img);
    }
}
