package com.moma.auxiliary;

import com.jug.util.ComponentTreeUtils;
import com.jug.util.componenttree.SimpleComponent;
import ij.IJ;
import ij.ImagePlus;
import ij.gui.Plot;
import jdk.nashorn.internal.runtime.regexp.joni.exception.ValueException;
import net.imglib2.Localizable;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.componenttree.Component;
import net.imglib2.algorithm.componenttree.ComponentForest;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.NativeType;
import net.imglib2.type.Type;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;

import java.util.ArrayList;

public class Plotting {
    public static <C extends Component<FloatType, C>> void drawComponentTree(ComponentForest<C> ct) {
        final ArrayList<RandomAccessibleInterval<FloatType>> slices = new ArrayList<>();
        if(ct.roots().isEmpty()){
            throw new ValueException("ct.roots() is empty");
        }
        C first = ct.roots().iterator().next();
        IntervalView sourceImage = ((SimpleComponent) first).getSourceImage();
        long xDim = sourceImage.dimension(0);
        long yDim = sourceImage.dimension(1);

        ArrayImgFactory<FloatType> imageFactory = new ArrayImgFactory<>(new FloatType());
        for (final C root : ct.roots()) {
            ArrayList<C> componentList = new ArrayList<>();
            componentList.add(root);
            while (componentList.size() > 0) {
                final RandomAccessibleInterval<FloatType> componentImageSlice = imageFactory.create(xDim, yDim);
                for (final Component<?, ?> ctn : componentList) {
                    drawComponent(ctn, componentImageSlice);
                }
                slices.add(componentImageSlice);
                componentList = ComponentTreeUtils.getAllChildren(componentList);
            }
        }
        ImageJFunctions.show(Views.stack(slices));
    }

    private static void drawComponent(final Component<?, ?> ctn, RandomAccessibleInterval<FloatType> image) {
        int xMin = Integer.MAX_VALUE;
        int xMax = Integer.MIN_VALUE;
        int yMin = Integer.MAX_VALUE;
        int yMax = Integer.MIN_VALUE;
        RandomAccess<FloatType> out = image.randomAccess();

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
