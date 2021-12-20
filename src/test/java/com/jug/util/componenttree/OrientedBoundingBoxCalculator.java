package com.jug.util.componenttree;

import net.imagej.ops.OpService;
import net.imagej.ops.geom.geom2d.DefaultConvexHull2D;
import net.imagej.ops.geom.geom2d.LabelRegionToPolygonConverter;
import net.imglib2.roi.geom.real.Polygon2D;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.ValuePair;
import org.apache.commons.lang.NotImplementedException;

/*	Fits a minimum area rectangle into a ROI.
 *
 * 	It searches the for the minimum area bounding rectangles among the ones that have a side
 * 	which is collinear with an edge of the convex hull.
 *
 * 	Concept based on:
 * 	H. Freeman and R. Shapira. 1975. Determining the minimum-area encasing rectangle for an arbitrary
 * 	closed curve. Commun. ACM 18, 7 (July 1975), 409–413. DOI:https://doi.org/10.1145/360881.360919
 *
 * 	Many thanks to @mountain_man (https://forum.image.sc/u/mountain_man) for helping me correct
 * 	my initial (wrong) intuition about the right way to tackle this problem!
 *
 * Code was adapted from here: https://raw.githubusercontent.com/ndefrancesco/macro-frenzy/master/geometry/fitting/fitMinRectangle.ijm
 */

public class OrientedBoundingBoxCalculator {
    private OpService ops;

    public OrientedBoundingBoxCalculator(OpService ops) {
        this.ops = ops;
    }

    public Polygon2D calculate(AdvancedComponent<FloatType> component){
        return findOrientedRectangleWithMinimalArea(component);
    }

    public Polygon2D findOrientedRectangleWithMinimalArea(AdvancedComponent<FloatType> component){
        /*	Fits a minimum area rectangle into a ROI.
         *
         * 	It searches the for the minimum area bounding rectangles among the ones that have a side
         * 	which is collinear with an edge of the convex hull.
         *
         * 	Concept based on:
         * 	H. Freeman and R. Shapira. 1975. Determining the minimum-area encasing rectangle for an arbitrary
         * 	closed curve. Commun. ACM 18, 7 (July 1975), 409–413. DOI:https://doi.org/10.1145/360881.360919
         *
         * 	Many thanks to @mountain_man (https://forum.image.sc/u/mountain_man) for helping me correct
         * 	my initial (wrong) intuition about the right way to tackle this problem!
         *
         */
        LabelRegionToPolygonConverter regionToPolygonConverter = new LabelRegionToPolygonConverter();
        regionToPolygonConverter.setContext(ops.context());
        final Polygon2D poly = regionToPolygonConverter.convert(component.getRegion(), Polygon2D.class);
        DefaultConvexHull2D convexHullCalculator = new DefaultConvexHull2D();
        Polygon2D polyHull = convexHullCalculator.calculate(poly);

//        GetOrientedBoundingBoxCoordinates(Integer[] xp, Integer[] yp);
        throw new NotImplementedException();
    }

    public ValuePair<Double[], Double[]> GetOrientedBoundingBoxCoordinates(Integer[] xp, Integer[] yp){
//        run("Convex Hull");
//        getSelectionCoordinates(xp, yp);
//        run("Undo"); // until run("Restore Selection"); for convex hull gets into production (ver >= 1.52u18)

        Integer np = xp.length;

        // Double minArea = 2 * getValue("Width") * getValue("Height"): width and height ob bounding box
        Double minArea = Double.MAX_VALUE;
//        Double minFD = getValue("Width") + getValue("Height"); // FD now stands for first diameter :)
        Double minFD = Double.MAX_VALUE; // FD now stands for first diameter :)
        Integer imin = -1;
        Integer i2min = -1;
        Integer jmin = -1;

        Double min_hmin = 0.0;
        Double min_hmax = 0.0;

        for (Integer i = 0; i < np; i++) {
            Double maxLD = 0.0;
            Integer imax = -1;
            Integer i2max = -1;
            Integer jmax = -1;
            Integer i2;
            if(i<np-1) i2 = i + 1; else i2 = 0;

            for (Integer j = 0; j < np; j++) {
                Double d = Math.abs(perpDist(xp[i], yp[i], xp[i2], yp[i2], xp[j], yp[j]));
                if (maxLD < d) {
                    maxLD = d;
                    imax = i;
                    jmax = j;
                    i2max = i2;
                }
            }

            Double hmin = 0.0;
            Double hmax = 0.0;

            for (Integer k = 0; k < np; k++) { // rotating calipers
                Double hd = parDist(xp[imax], yp[imax], xp[i2max], yp[i2max], xp[k], yp[k]);
//                hmin = minOf(hmin, hd);
                hmin = (hmin < hd) ? hmin : hd;
//                hmax = maxOf(hmax, hd);
                hmax = (hmax > hd) ? hmax : hd;
            }

            Double area = maxLD * (hmax - hmin);

            if (minArea > area){

                minArea = area;
                minFD = maxLD;
                min_hmin = hmin;
                min_hmax = hmax;

                imin = imax;
                i2min = i2max;
                jmin = jmax;
            }
        }

        Double pd = perpDist(xp[imin], yp[imin], xp[i2min], yp[i2min], xp[jmin], yp[jmin]); // signed feret diameter
        Double pairAngle = Math.atan2( yp[i2min]- yp[imin], xp[i2min]- xp[imin]);
        Double minAngle = pairAngle + Math.PI/2;



//        nxp=newArray(4);
//        nyp=newArray(4);
        Double[] nxp= new Double[4];
        Double[] nyp= new Double[4];

        nxp[0] = xp[imin] + Math.cos(pairAngle) * min_hmax;
        nyp[0] = yp[imin] + Math.sin(pairAngle) * min_hmax;

        nxp[1] = nxp[0] + Math.cos(minAngle) * pd;
        nyp[1] = nyp[0] + Math.sin(minAngle) * pd;

        nxp[2] = nxp[1] + Math.cos(pairAngle) * (min_hmin - min_hmax);
        nyp[2] = nyp[1] + Math.sin(pairAngle) * (min_hmin - min_hmax);

        nxp[3] = nxp[2] + Math.cos(minAngle) * - pd;
        nyp[3] = nyp[2] + Math.sin(minAngle) * - pd;

//        makeSelection("polygon", nxp, nyp);
        return new ValuePair<Double[], Double[]>(nxp, nyp);
    }

    private Double dist2(Integer x1, Integer y1, Integer x2, Integer y2) {
        return Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2);
    }

    private Double perpDist(Integer p1x, Integer p1y, Integer p2x, Integer p2y, Integer x, Integer y){
        // signed distance from a point (x,y) to a line passing through p1 and p2
        return ((p2x - p1x)*(y - p1y) - (x - p1x)*(p2y - p1y))/Math.sqrt(dist2(p1x, p1y, p2x, p2y));
    }

    private Double parDist(Integer p1x, Integer p1y, Integer p2x, Integer p2y, Integer x, Integer y){
        // signed projection of vector (x,y)-p1 into a line passing through p1 and p2
        return ((p2x - p1x)*(x - p1x) + (y - p1y)*(p2y - p1y))/Math.sqrt(dist2(p1x, p1y, p2x, p2y));
    }
}


