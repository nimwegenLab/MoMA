package com.jug.util.componenttree;

import net.imagej.ops.geom.GeomUtils;
import net.imglib2.RealLocalizable;
import net.imglib2.roi.geom.real.Polygon2D;
import org.javatuples.Sextet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class CentralMomentsCalculator {
    /**
     * Calculates moments for {@link Polygon2D}.
     * Code taken from: https://raw.githubusercontent.com/imagej/imagej-ops/master/src/main/java/net/imagej/ops/geom/geom2d/DefaultMinorMajorAxis.java
     *
     * @param points
     *            vertices of polygon in counter clockwise order.
     * @return moments m00, n20, n11 and n02
     * @see "On  Calculation of Arbitrary Moments of Polygon2Ds, Carsten Steger, October 1996"
     */
    private double[] getMoments(final Polygon2D input, final List<RealLocalizable> points) {
        // calculate normalized moment
        double m00 = 0;
        double m01 = 0;
        double m02 = 0;
        double m10 = 0;
        double m11 = 0;
        double m20 = 0;

        for (int i = 1; i < points.size(); i++) {
            double a = getX(input, i - 1) * getY(input, i) - getX(input, i) * getY(input, i - 1);

            m00 += a;
            m10 += a * (getX(input, i - 1) + getX(input, i));
            m01 += a * (getY(input, i - 1) + getY(input, i));

            m20 += a * (Math.pow(getX(input, i - 1), 2) + getX(input, i - 1) * getX(input, i)
                    + Math.pow(getX(input, i), 2));
            m11 += a * (2 * getX(input, i - 1) * getY(input, i - 1) + getX(input, i - 1) * getY(input, i)
                    + getX(input, i) * getY(input, i - 1) + 2 * getX(input, i) * getY(input, i));
            m02 += a * (Math.pow(getY(input, i - 1), 2) + getY(input, i - 1) * getY(input, i)
                    + Math.pow(getY(input, i), 2));
        }

        m00 /= 2d;
        m01 /= 6 * m00;
        m02 /= 12d * m00;
        m10 /= 6d * m00;
        m11 /= 24d * m00;
        m20 /= 12d * m00;

        // calculate central moments
        double n20 = m20 - Math.pow(m10, 2);
        double n11 = m11 - m10 * m01;
        double n02 = m02 - Math.pow(m01, 2);

        /* Values:
         * m00: area
         * m10: centroid X coordinate
         * m01: centroid Y coordinate
         * n20: variance X coordinate
         * n02: variance Y coordinate
         * n11: covariance X*Y
         */
        return new double[] { m00, m10, m01, n20, n02, n11 };
    }

    private double getY(final Polygon2D input, final int index) {
        int i = index;
        if (i == input.numVertices())
            i = 0;
        return input.vertex(i).getDoublePosition(1);
    }

    private double getX(final Polygon2D input, final int index) {
        int i = index;
        if (i == input.numVertices())
            i = 0;
        return input.vertex(i).getDoublePosition(0);
    }

    /**
     * Calculate the central moments of Polygon2D input. The values in the returned Sextet are:
     * m00: area
     * m10: centroid X coordinate
     * m01: centroid Y coordinate
     * n20: variance X coordinate
     * n02: variance Y coordinate
     * n11: covariance of X and Y
     *
     * @param input
     * @return
     */
    public Sextet<Double, Double, Double, Double, Double, Double> calculate(final Polygon2D input) {

        List<RealLocalizable> points = new ArrayList<>(GeomUtils.vertices(input));

        // Sort RealLocalizables of P by x-coordinate (in case of a tie,
        // sort by y-coordinate). Sorting is counter clockwise.
        Collections.sort(points, new Comparator<RealLocalizable>() {

            @Override
            public int compare(final RealLocalizable o1, final RealLocalizable o2) {
                final Double o1x = new Double(o1.getDoublePosition(0));
                final Double o2x = new Double(o2.getDoublePosition(0));
                final int result = o2x.compareTo(o1x);
                if (result == 0) {
                    return new Double(o2.getDoublePosition(1)).compareTo(new Double(o1.getDoublePosition(1)));
                }
                return result;
            }
        });
        points.add(points.get(0));

        double[] moments = getMoments(input, points); // calculate area, first and second order moments
        return new Sextet<>(new Double(moments[0]),
                new Double(moments[1]),
                new Double(moments[2]),
                new Double(moments[3]),
                new Double(moments[4]),
                new Double(moments[5]));
    }
}