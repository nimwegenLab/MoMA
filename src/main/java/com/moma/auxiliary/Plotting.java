package com.moma.auxiliary;

import com.jug.lp.Hypothesis;
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
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;

import java.util.ArrayList;
import java.util.List;

public class Plotting {
    public static <C extends Component<FloatType, C>> void drawComponentTree(ComponentForest<C> ct, List<Hypothesis<Component<FloatType, ?>>> optimalSegs) {
        final ArrayList<RandomAccessibleInterval<ARGBType>> slices = new ArrayList<>();
        if (ct.roots().isEmpty()) {
            throw new ValueException("ct.roots() is empty");
        }

        List<Component<FloatType, ?>> optimalSegs2 = new ArrayList<>();
        for (Hypothesis<Component<FloatType, ?>> seg : optimalSegs) {
            optimalSegs2.add(seg.getWrappedHypothesis());
        }

        C first = ct.roots().iterator().next();
        IntervalView sourceImage = ((SimpleComponent) first).getSourceImage();
        long xDim = sourceImage.dimension(0);
        long yDim = sourceImage.dimension(1);

        ArrayImgFactory<ARGBType> imageFactory = new ArrayImgFactory<>(new ARGBType());
        for (final C root : ct.roots()) {
            ArrayList<C> componentList = new ArrayList<>();
            componentList.add(root);
            while (componentList.size() > 0) {
                final RandomAccessibleInterval<ARGBType> componentImageSlice = imageFactory.create(xDim, yDim);
                for (final Component<?, ?> ctn : componentList) {
                    boolean val = optimalSegs2.contains(ctn);
                    copyComponentPixelToImage(ctn, sourceImage, componentImageSlice, val);
                }
                slices.add(componentImageSlice);
                componentList = ComponentTreeUtils.getAllChildren(componentList);
            }
        }
        ImageJFunctions.show(Views.stack(slices));
    }

    private static void copyComponentPixelToImage(final Component<?, ?> ctn,
                                                  RandomAccessibleInterval<FloatType> sourceImage,
                                                  RandomAccessibleInterval<ARGBType> targetImage,
                                                  boolean ctnIsSelected) {
        RandomAccess<FloatType> source = sourceImage.randomAccess();
        RandomAccess<ARGBType> out = targetImage.randomAccess();

        for (Localizable location : ctn) {
            source.setPosition(location);
            out.setPosition(location);
            int level = (int) (source.get().getRealFloat() * 255);
            ARGBType value = new ARGBType(ARGBType.rgba(level, level, level, 0));
            if (ctnIsSelected) {
                value = new ARGBType(ARGBType.rgba(0, level, 0, 0));
            }
            out.get().set(value);
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
