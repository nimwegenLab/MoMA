package com.moma.auxiliary;

import com.jug.util.ComponentTreeUtils;
import com.jug.util.componenttree.AdvancedComponent;
import ij.IJ;
import ij.ImagePlus;
import ij.gui.Plot;
import ij.gui.TextRoi;
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
import net.imglib2.view.Views;
import org.javatuples.Pair;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static com.jug.util.imglib2.Imglib2Utils.setImageToValue;

public class Plotting {
    public static <C extends Component<FloatType, C>> void drawComponentTree(ComponentForest<C> ct,
                                                                             List<Component<FloatType, ?>> componentsInOptimalSolution, int timeStep) {
        if (ct.roots().isEmpty()) {
            throw new ValueException("ct.roots() is empty");
        }

        // create image factory with correct dimensions
        C first = ct.roots().iterator().next();
        RandomAccessibleInterval sourceImage = ((AdvancedComponent) first).getSourceImage();
        long xDim = sourceImage.dimension(0);
        long yDim = sourceImage.dimension(1);
        ArrayImgFactory<ARGBType> imageFactory = new ArrayImgFactory<>(new ARGBType());

        // define consumer that will draw components to image and add them to the image stack
        final ArrayList<RandomAccessibleInterval<ARGBType>> componentLevelImageStack = new ArrayList<>();
        Consumer<Pair<List<C>, Integer>> levelComponentsConsumer = (levelComponentsListAndLevel)-> {
            List<C> componentOfLevel = levelComponentsListAndLevel.getValue0();
            {
                final RandomAccessibleInterval<ARGBType> componentLevelImage = imageFactory.create(xDim, yDim);
                setImageToValue(Views.iterable(componentLevelImage), new ARGBType(ARGBType.rgba(100, 0, 0, 0)));
                for(C ctn : componentOfLevel){
                    boolean val = componentsInOptimalSolution.contains(ctn);
                    drawComponentToImage(ctn, sourceImage, componentLevelImage, val);
                }
                componentLevelImageStack.add(componentLevelImage);
            }
        };

        // run for components in each level
        ComponentTreeUtils.doForEachComponentInTreeLevel(ct, levelComponentsConsumer);

        // show
        ImagePlus imp = ImageJFunctions.show(Views.stack(componentLevelImageStack));
        TextRoi text = new TextRoi(0, 0, String.format("t=%d", timeStep));
        imp.setOverlay(text, Color.white, 0, Color.black);
    }

    private static void drawComponentToImage(final Component<?, ?> ctn,
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


    public static <C extends Component<FloatType, C>> void drawComponentTree2(ComponentForest<C> ct,
                                                                              List<C> componentsInOptimalSolution) {
        if (ct.roots().isEmpty()) {
            throw new ValueException("ct.roots() is empty");
        }

        // create image factory with correct dimensions
        C first = ct.roots().iterator().next();

        // define consumer that will draw components to image and add them to the image stack
        final ArrayList<RandomAccessibleInterval<ARGBType>> componentLevelImageStack = new ArrayList<>();
        Consumer<Pair<List<C>, Integer>> levelComponentsConsumer = (levelComponentsListAndLevel)-> {
            List<C> componentsOfLevel = levelComponentsListAndLevel.getValue0();
            {
                RandomAccessibleInterval<ARGBType> componentLevelImage = createImageWithComponents(componentsOfLevel, componentsInOptimalSolution);
                componentLevelImageStack.add(componentLevelImage);
            }
        };

        // run for components in each level
        ComponentTreeUtils.doForEachComponentInTreeLevel(ct, levelComponentsConsumer);

        // show
        ImageJFunctions.show(Views.stack(componentLevelImageStack));
    }

    public static <C extends Component<FloatType, C>> RandomAccessibleInterval<ARGBType> createImageWithComponents(List<C> components,
                                                                                                                   List<C> optimalComponents) {
        C first = components.get(0);
        RandomAccessibleInterval sourceImage = ((AdvancedComponent) first).getSourceImage();
        long xDim = sourceImage.dimension(0);
        long yDim = sourceImage.dimension(1);
        ArrayImgFactory<ARGBType> imageFactory = new ArrayImgFactory<>(new ARGBType());

        final RandomAccessibleInterval<ARGBType> resultImage = imageFactory.create(xDim, yDim);
        for (C ctn : components) {
            boolean val = optimalComponents.contains(ctn);
            drawComponentToImage2(ctn, resultImage, val);
        }
        return resultImage;
    }

    private static void drawComponentToImage2(final Component<?, ?> ctn,
                                              RandomAccessibleInterval<ARGBType> targetImage,
                                              boolean ctnIsSelected) {
//        RandomAccess<FloatType> source = sourceImage.randomAccess();
        RandomAccess<ARGBType> out = targetImage.randomAccess();

        for (Localizable location : ctn) {
//            source.setPosition(location);
            out.setPosition(location);
//            int level = (int) (source.get().getRealFloat() * 255);
            ARGBType value = new ARGBType(ARGBType.rgba(255, 255, 255, 0));
            if (ctnIsSelected) {
                value = new ARGBType(ARGBType.rgba(0, 255, 0, 0));
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
