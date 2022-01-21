package com.jug.export.measurements;

import com.jug.export.ResultTable;
import com.jug.util.componenttree.ComponentInterface;
import com.jug.util.componenttree.ComponentProperties;
import com.jug.util.math.Vector2D;
import com.jug.util.math.Vector2DPolyline;
import net.imglib2.type.numeric.real.DoubleType;
import org.javatuples.Triplet;

public class BestFitEllipseMeasurement implements SegmentMeasurementInterface {
    private ComponentProperties componentProperties;

    public BestFitEllipseMeasurement(ComponentProperties componentProperties) {
        this.componentProperties = componentProperties;
    }

    @Override
    public void setOutputTable(ResultTable outputTable) {

    }

    @Override
    /**
     * WARNING: THIS DOES NOT WORK YET!!!
     */
    public void measure(ComponentInterface component) {
        Triplet<DoubleType, DoubleType, DoubleType> minorAndMajorAxis = componentProperties.getEllipseProperties(component);
        double minorAxisLength = minorAndMajorAxis.getValue0().get();
        double majorAxisLength = minorAndMajorAxis.getValue1().get();
        double angle = minorAndMajorAxis.getValue2().get();
        double[] componentCenter = component.firstMomentPixelCoordinates();

        Vector2DPolyline ellipse = calculateEllipseContour(componentCenter[0], componentCenter[1], majorAxisLength, minorAxisLength, angle);

        component.addComponentFeature("ellipse", ellipse);
    }

    /**
     * WARNING: THIS DOES NOT WORK YET!!!
     */
    private Vector2DPolyline calculateEllipseContour(double x, double y, double a, double b, double angle){
        /*angle: angle of */
        a = a / 2;
        b = b / 2;
        Vector2DPolyline result = new Vector2DPolyline();
        result.setType(Vector2DPolyline.PolyshapeType.POLYGON);
//        double beta = -angle * (Math.PI / 180);
        double beta = -angle;
            for (int i=0; i<=360; i+=2) {
                double alpha = i * (Math.PI / 180) - Math.PI;
                double X = x + a * Math.cos(alpha) * Math.cos(beta) - b * Math.sin(alpha) * Math.sin(beta);
                double Y = y + a * Math.cos(alpha) * Math.sin(beta) + b * Math.sin(alpha) * Math.cos(beta);
                result.add(new Vector2D(X,Y));
//                if (i==0) moveTo(X, Y); else lineTo(X,Y);
//                if (i==0) {ax1=X; ay1=Y;}
//                if (i==90) {bx1=X; by1=Y;}
//                if (i==180) {ax2=X; ay2=Y;}
//                if (i==270) {bx2=X; by2=Y;}
            }
//            drawLine(ax1, ay1, ax2, ay2);
//            drawLine(bx1, by1, bx2, by2);
//            updateDisplay;
        return result;
    }

//    private double sqr(double x) {
//        return x*x;
//    }
//
//    public Vector2DPolyline drawEllipse(double xCenter, double yCenter, double minor, double major, double theta) {
////        /** X centroid */
////        double xCenter;
////
////        /** X centroid */
////        double  yCenter;
////
////        /** Length of major axis */
////        double major;
////
////        /** Length of minor axis */
////        double minor;
////
////        /** Angle in degrees */
////        double angle;
////
////        /** Angle in radians */
////        double theta;
//
//        if (major==0.0 && minor==0.0) return null;
//
////        int xc = (int)Math.round(xCenter);
////        int yc = (int)Math.round(yCenter);
//
//        double xc = Math.round(xCenter);
//        double yc = Math.round(yCenter);
//
////        int maxY = ip.getHeight();
//        int xmin, xmax;
//        double sint, cost, rmajor2, rminor2, g11, g12, g22, k1, k2, k3;
//        int x, xsave, ymin, ymax;
////        int[] txmin = new int[maxY];
////        int[] txmax = new int[maxY];
//        double j1, j2, yr;
//
//        sint = Math.sin(theta);
//        cost = Math.cos(theta);
//        rmajor2 = 1.0 / sqr(major/2);
//        rminor2 = 1.0 / sqr(minor/2);
//        g11 = rmajor2 * sqr(cost) + rminor2 * sqr(sint);
//        g12 = (rmajor2 - rminor2) * sint * cost;
//        g22 = rmajor2 * sqr(sint) + rminor2 * sqr(cost);
//        k1 = -g12 / g11;
//        k2 = (sqr(g12) - g11 * g22) / sqr(g11);
//        k3 = 1.0 / g11;
//        ymax = (int)Math.floor(Math.sqrt(Math.abs(k3 / k2)));
////        if (ymax>maxY)
////            ymax = maxY;
////        if (ymax<1)
////            ymax = 1;
//        ymin = -ymax;
//        // Precalculation and use of symmetry speed things up
//        for (int y=0; y<=ymax; y++) {
//            //GetMinMax(y, aMinMax);
//            j2 = Math.sqrt(k2 * sqr(y) + k3);
//            j1 = k1 * y;
//            txmin[y] = (int)Math.round(j1 - j2);
//            txmax[y] = (int)Math.round(j1 + j2);
//        }
//        if (record) {
//            xCoordinates[nCoordinates] = xc + txmin[ymax - 1];
//            yCoordinates[nCoordinates] = yc + ymin;
//            nCoordinates++;
//        } else
//            ip.moveTo(xc + txmin[ymax - 1], yc + ymin);
//        for (int y=ymin; y<ymax; y++) {
//            x = y<0?txmax[-y]:-txmin[y];
//            if (record) {
//                xCoordinates[nCoordinates] = xc + x;
//                yCoordinates[nCoordinates] = yc + y;
//                nCoordinates++;
//            } else
//                ip.lineTo(xc + x, yc + y);
//        }
//        for (int y=ymax; y>ymin; y--) {
//            x = y<0?txmin[-y]:-txmax[y];
//            if (record) {
//                xCoordinates[nCoordinates] = xc + x;
//                yCoordinates[nCoordinates] = yc + y;
//                nCoordinates++;
//            } else
//                ip.lineTo(xc + x, yc + y);
//        }
//    }
}
