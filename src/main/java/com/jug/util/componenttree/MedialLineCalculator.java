package com.jug.util.componenttree;

import com.jug.util.imglib2.Imglib2Utils;
import com.jug.util.math.GeomUtils;
import com.jug.util.math.Vector2D;
import com.jug.util.math.Vector2DPolyline;
import net.imagej.ops.OpService;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.RealCursor;
import net.imglib2.algorithm.labeling.ConnectedComponents;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.roi.labeling.LabelRegion;
import net.imglib2.roi.labeling.LabelRegions;
import net.imglib2.type.logic.BitType;

import java.util.Collections;

//import sc.fiji.skeletonize3D.AnalyzeSkeleton_;


public class MedialLineCalculator {
    private OpService ops;
    private Imglib2Utils imglib2Utils;

    public MedialLineCalculator(OpService ops, Imglib2Utils imglib2Utils) {
        this.ops = ops;
        this.imglib2Utils = imglib2Utils;
    }

    public Vector2DPolyline calculate(RandomAccessibleInterval<BitType> image) {
        RandomAccessibleInterval raiThinned = ops.morphology().thinGuoHall(image);
//        RandomAccessibleInterval raiThinned = ops.morphology().thinMorphological(image);
//        RandomAccessibleInterval raiThinned = ops.morphology().thinHilditch(image);
//        RandomAccessibleInterval raiThinned = ops.morphology().thinZhangSuen(image);
//        ImageJFunctions.show(raiThinned);
        ImgLabeling labeling = ops.labeling().cca(raiThinned, ConnectedComponents.StructuringElement.EIGHT_CONNECTED);
        LabelRegions regions = new LabelRegions(labeling);
        LabelRegion skeletonRegion = regions.getLabelRegion(0);
        Vector2DPolyline polygon = createMedialLineFromCursor(skeletonRegion.cursor());
        GeomUtils.filterPolylineAngles(polygon, Math.PI / 2 + 0.01);
        return polygon;
    }

    private static Vector2DPolyline createMedialLineFromCursor(RealCursor<?> cursor) {
        Vector2DPolyline result = new Vector2DPolyline();
        cursor.next();
        result.add(new Vector2D(cursor)); /* always add first point */
        int firstPixelRowIndex = (int) cursor.getDoublePosition(1);
        boolean onFirstPixelRow = true;
        while (cursor.hasNext()) {
            cursor.next();
            if (((int) cursor.getDoublePosition(1)) != firstPixelRowIndex) {
                onFirstPixelRow = false;
                if (GeomUtils.distance(cursor, result.getFirst()) < 2) {
                    result.reverse(); /* if the next skeleton pixel in the next row is closer to the first pixel in the list, then we were moving in the wrong skeleton-direction and we need to reverse the order of skeleton points */
                }
            }
            if (GeomUtils.distance(cursor, result.getLast()) < 2) {
                result.addAtEnd(new Vector2D(cursor));
            }
            else if (GeomUtils.distance(cursor, result.getFirst()) < 2 && onFirstPixelRow) {
                result.addAtStart(new Vector2D(cursor));
            }
        }
        return result;
    }
}
