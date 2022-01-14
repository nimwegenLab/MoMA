package com.jug.util.componenttree;

import com.jug.util.imglib2.Imglib2Utils;
import com.jug.util.math.Vector2DPolyline;
import net.imagej.ops.OpService;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.roi.labeling.LabelRegion;
import net.imglib2.roi.labeling.LabelRegions;
import net.imglib2.type.logic.BitType;
import net.imglib2.algorithm.labeling.ConnectedComponents;
import net.imglib2.type.numeric.integer.IntType;
//import net.imglib2.algorithm.interpolation.randomaccess.BSplineInterpolator;
import sc.fiji.skeletonize3D.Skeletonize3D_;
//import sc.fiji.skeletonize3D.AnalyzeSkeleton_;


public class MedialLineCalculator {
    private OpService ops;
    private Imglib2Utils imglib2Utils;

    public MedialLineCalculator(OpService ops, Imglib2Utils imglib2Utils) {
        this.ops = ops;
        this.imglib2Utils = imglib2Utils;
    }

    public Vector2DPolyline calculate(RandomAccessibleInterval<BitType> image) {
//        RandomAccessibleInterval raiThinned = ops.morphology().thinGuoHall(image);
        RandomAccessibleInterval raiThinned = ops.morphology().thinMorphological(image);
//        RandomAccessibleInterval raiThinned = ops.morphology().thinHilditch(image);
//        RandomAccessibleInterval raiThinned = ops.morphology().thinZhangSuen(image);
        ImgLabeling labeling = ops.labeling().cca(raiThinned, ConnectedComponents.StructuringElement.EIGHT_CONNECTED);
        LabelRegions regions = new LabelRegions(labeling);
        LabelRegion skeletonRegion = regions.getLabelRegion(0);
        Vector2DPolyline polygon = Vector2DPolyline.createFromCursor(skeletonRegion.cursor());
        return polygon;
    }
}
