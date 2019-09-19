package com.moma.auxiliary;

import com.jug.util.ComponentTreeUtils;
import com.jug.util.componenttree.SimpleComponent;
import ij.IJ;
import ij.ImagePlus;
import ij.gui.Plot;
import net.imglib2.Localizable;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.componenttree.Component;
import net.imglib2.algorithm.componenttree.ComponentForest;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.Views;

import java.util.ArrayList;

public class Plotting {

    public static <C extends Component<FloatType, C>> void drawComponentTree(ComponentForest<C> ct) {
        final ArrayList<RandomAccessibleInterval<UnsignedByteType>> slices = new ArrayList<>();

        long xDim = 0;
        long yDim = 0;

        for (final C root : ct.roots()) { // this is actually just need for a single root, as they all have the same source-image; but I don't know how to do this for a Set<> ATM
            if (root instanceof SimpleComponent) {
                xDim = ((SimpleComponent) root).getSourceImage().dimension(0);
                yDim = ((SimpleComponent) root).getSourceImage().dimension(1);
            }
        }

        int i = 0;
        for (final C root : ct.roots()) {
            int componentLevel = 0;
            ArrayList<C> componentList = new ArrayList<>();
            componentList.add(root);
            while (componentList.size() > 0) {
                final RandomAccessibleInterval<UnsignedByteType> componentImageSlice = ArrayImgs.unsignedBytes(xDim, yDim);
                for (final Component<?, ?> ctn : componentList) {
                    drawComponent(ctn, i, componentLevel, componentImageSlice);
                    i++;
                }
                slices.add(componentImageSlice);
                componentList = ComponentTreeUtils.getAllChildren(componentList);
                componentLevel++;
            }
        }
        ImageJFunctions.show(Views.stack(slices));
    }

    private static void drawComponent(final Component<?, ?> ctn, final int index, final int level, RandomAccessibleInterval<UnsignedByteType> image) {
        int xMin = Integer.MAX_VALUE;
        int xMax = Integer.MIN_VALUE;
        int yMin = Integer.MAX_VALUE;
        int yMax = Integer.MIN_VALUE;
        RandomAccess<UnsignedByteType> out = image.randomAccess();

        for (Localizable location : ctn) {
            final int xPos = location.getIntPosition(0);
            xMin = Math.min(xMin, xPos);
            xMax = Math.max(xMax, xPos);
            final int yPos = location.getIntPosition(1);
            yMin = Math.min(yMin, yPos);
            yMax = Math.max(yMax, yPos);

            ///////////// Draw component to image ///////////////////
            out.setPosition(location);
//			out.get().set(new ARGBType(ARGBType.blue(level)));
            out.get().set(255);
//			in.fwd();
//			out.setPosition(in);
//			out.get().set(in.get());
//			image.
        }
//		System.out.println("Component "+index+":");
//		System.out.println("\tlevel: "+level);
//		System.out.println("\txSize: "+xMin+", "+xMax);
//		System.out.println("\tySize: "+yMin+", "+yMax);
    }

    public static void surfacePlot(final RandomAccessibleInterval<FloatType> img, final int dimension, final long position) {
        ImagePlus imp = ImageJFunctions.wrap(Views.hyperSlice(img, dimension, position), "my image");
        IJ.run(imp, "3D Surface Plot", "");
    }

    static public void plotArray(float[] y) {
        plotArray(y, null, null, null);
    }

    private static void plotArray(float[] y, String title, String xLabel, String yLabel) {
        int[] x_vals = java.util.stream.IntStream.rangeClosed(0, y.length).toArray();
        float[] xvals_new = new float[x_vals.length];
        for (int i = 0; i < x_vals.length; i++) {
            xvals_new[i] = x_vals[i];
        }

        if (title == null)
            title = "A plot";
        if (xLabel == null)
            xLabel = "labels on the x-axis";
        if (yLabel == null)
            yLabel = "labels on the y-axis";

        Plot plot = new Plot(title, xLabel, yLabel, xvals_new, y);
        plot.show();
    }
}
