package com.jug.export.measurements;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import net.imagej.ops.Ops.Geometric.SecondMoment;
import net.imagej.ops.geom.GeomUtils;
import net.imglib2.RealLocalizable;
import net.imglib2.roi.geom.real.Polygon2D;
import net.imglib2.type.numeric.real.DoubleType;
import org.scijava.plugin.Plugin;
import org.javatuples.Triplet;

public class CustomDefaultMinorMajorAxisCustom {
    public CustomDefaultMinorMajorAxisCustom() {
    }

    private double[] getMinorMajorAxis(Polygon2D input, List<RealLocalizable> points) {
        double[] moments = this.getMoments(input, points);
        double m00 = moments[0];
        double u20 = moments[1];
        double u11 = moments[2];
        double u02 = moments[3];
        double m4 = 4.0D * Math.abs(u02 * u20 - u11 * u11);
        if (m4 < 1.0E-6D) {
            m4 = 1.0E-6D;
        }

        double a11 = u02 / m4;
        double a12 = u11 / m4;
        double a22 = u20 / m4;
        double tmp = a11 - a22;
        if (tmp == 0.0D) {
            tmp = 1.0E-6D;
        }

        double ta = 0.5D * Math.atan(2.0D * a12 / tmp);
        if (ta < 0.0D) {
            ++ta;
        }

        if (a12 > 0.0D) {
            ++ta;
        } else if (a12 == 0.0D) {
            if (a22 > a11) {
                ta = 0.0D;
                tmp = a22;
                a22 = a11;
                a11 = tmp;
            } else if (a11 != a22) {
                ta = 1.5707963267948966D;
            }
        }

        tmp = Math.sin(ta);
        if (tmp == 0.0D) {
            tmp = 1.0E-6D;
        }

        double z = a12 * Math.cos(ta) / tmp;
        double major = Math.sqrt(1.0D / Math.abs(a22 + z));
        double minor = Math.sqrt(1.0D / Math.abs(a11 - z));
        double scale = Math.sqrt(m00 / (3.141592653589793D * major * minor));
        major = major * scale * 2.0D;
        minor = minor * scale * 2.0D;
        double angle = 180.0D * ta / 3.141592653589793D;
        if (angle == 180.0D) {
            angle = 0.0D;
        }

        if (major < minor) {
            tmp = major;
            major = minor;
            minor = tmp;
        }

        return new double[]{minor, major, angle};
    }

    private double[] getMoments(Polygon2D input, List<RealLocalizable> points) {
        double m00 = 0.0D;
        double m01 = 0.0D;
        double m02 = 0.0D;
        double m10 = 0.0D;
        double m11 = 0.0D;
        double m20 = 0.0D;

        for(int i = 1; i < points.size(); ++i) {
            double a = this.getX(input, i - 1) * this.getY(input, i) - this.getX(input, i) * this.getY(input, i - 1);
            m00 += a;
            m10 += a * (this.getX(input, i - 1) + this.getX(input, i));
            m01 += a * (this.getY(input, i - 1) + this.getY(input, i));
            m20 += a * (Math.pow(this.getX(input, i - 1), 2.0D) + this.getX(input, i - 1) * this.getX(input, i) + Math.pow(this.getX(input, i), 2.0D));
            m11 += a * (2.0D * this.getX(input, i - 1) * this.getY(input, i - 1) + this.getX(input, i - 1) * this.getY(input, i) + this.getX(input, i) * this.getY(input, i - 1) + 2.0D * this.getX(input, i) * this.getY(input, i));
            m02 += a * (Math.pow(this.getY(input, i - 1), 2.0D) + this.getY(input, i - 1) * this.getY(input, i) + Math.pow(this.getY(input, i), 2.0D));
        }

        m00 /= 2.0D;
        m01 /= 6.0D * m00;
        m02 /= 12.0D * m00;
        m10 /= 6.0D * m00;
        m11 /= 24.0D * m00;
        m20 /= 12.0D * m00;
        double n20 = m20 - Math.pow(m10, 2.0D);
        double n11 = m11 - m10 * m01;
        double n02 = m02 - Math.pow(m01, 2.0D);
        return new double[]{m00, n20, n11, n02};
    }

    private double getY(Polygon2D input, int index) {
        int i = index;
        if (index == input.numVertices()) {
            i = 0;
        }

        return input.vertex(i).getDoublePosition(1);
    }

    private double getX(Polygon2D input, int index) {
        int i = index;
        if (index == input.numVertices()) {
            i = 0;
        }

        return input.vertex(i).getDoublePosition(0);
    }

    public Triplet<DoubleType, DoubleType, DoubleType> calculate(Polygon2D input) {
        List<RealLocalizable> points = new ArrayList(GeomUtils.vertices(input));
        Collections.sort(points, new Comparator<RealLocalizable>() {
            public int compare(RealLocalizable o1, RealLocalizable o2) {
                Double o1x = new Double(o1.getDoublePosition(0));
                Double o2x = new Double(o2.getDoublePosition(0));
                int result = o2x.compareTo(o1x);
                return result == 0 ? (new Double(o2.getDoublePosition(1))).compareTo(new Double(o1.getDoublePosition(1))) : result;
            }
        });
        points.add(points.get(0));
        double[] minorMajorAxis = this.getMinorMajorAxis(input, points);
//        return new ValuePair(new DoubleType(minorMajorAxis[0]), new DoubleType(minorMajorAxis[1]));
        return new Triplet(new DoubleType(minorMajorAxis[0]), new DoubleType(minorMajorAxis[1]), new DoubleType(minorMajorAxis[2]));
    }
}
