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
        Vector2DPolyline polygon = createMedialLineFromCursor(skeletonRegion.cursor());
        GeomUtils.filterPolylineAngles(polygon, Math.PI / 2 + 0.01);
        return polygon;
    }

    private static Vector2DPolyline createMedialLineFromCursor(RealCursor<?> cursor) {
        Vector2DPolyline result = new Vector2DPolyline();
        cursor.next();
        Vector2D lastAdded = new Vector2D(cursor); /* always add first point */
        result.add(lastAdded);
        while (cursor.hasNext()) {
            cursor.next();
            if (GeomUtils.distance(cursor, lastAdded) < 2) {
                lastAdded = new Vector2D(cursor); /* only add new point, if its distance from the last contour position is less than 2 pixels; i.e. we accept the diagonal pixel distance */
                result.add(lastAdded);
            }
        }
        return result;
    }
}
