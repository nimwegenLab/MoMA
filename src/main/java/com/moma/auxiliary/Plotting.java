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
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.img.display.imagej.ImageJFunctions;
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
                    copyComponentPixelToComponentImage(ctn, sourceImage, componentImageSlice);
                }
                slices.add(componentImageSlice);
                componentList = ComponentTreeUtils.getAllChildren(componentList);
            }
        }
        ImageJFunctions.show(Views.stack(slices));
    }

    private static void copyComponentPixelToComponentImage(final Component<?, ?> ctn,
                                                           RandomAccessibleInterval<FloatType> sourceImage,
                                                           RandomAccessibleInterval<FloatType> targetImage) {
        RandomAccess<FloatType> source = sourceImage.randomAccess();
        RandomAccess<FloatType> out = targetImage.randomAccess();

        for (Localizable location : ctn) {
            source.setPosition(location);
            out.setPosition(location);
//			out.get().set(new ARGBType(ARGBType.blue(level)));
            FloatType valueCopy = source.get().copy();
            out.get().set(valueCopy);
        }
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
