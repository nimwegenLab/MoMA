package com.moma.auxiliary;

import com.jug.util.ComponentTreeUtils;
import com.jug.util.componenttree.AdvancedComponent;
import com.jug.util.componenttree.ComponentInterface;
import com.jug.util.imglib2.Imglib2Utils;
import ij.IJ;
import ij.ImagePlus;
import ij.gui.Overlay;
import ij.gui.Plot;
import ij.gui.TextRoi;
import jdk.nashorn.internal.runtime.regexp.joni.exception.ValueException;
import net.imagej.ImageJ;
import net.imagej.roi.DefaultROITree;
import net.imagej.roi.ROITree;
import net.imglib2.Localizable;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.componenttree.Component;
import net.imglib2.algorithm.componenttree.ComponentForest;
import net.imglib2.img.Img;
import net.imglib2.img.ImgView;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.roi.MaskPredicate;
import net.imglib2.type.NativeType;
import net.imglib2.type.Type;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.Views;
import org.javatuples.Pair;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static com.jug.util.imglib2.Imglib2Utils.setImageToValue;

public class Plotting {
    public static void drawComponentTree(ComponentForest<AdvancedComponent<FloatType>> ct,
                                         List<AdvancedComponent<FloatType>> componentsInOptimalSolution, int timeStep) {
        if (ct.roots().isEmpty()) {
            throw new ValueException("ct.roots() is empty");
        }

        // create image factory with correct dimensions
        AdvancedComponent<FloatType> first = ct.roots().iterator().next();
        RandomAccessibleInterval sourceImage = ((AdvancedComponent) first).getSourceImage();
        long xDim = sourceImage.dimension(0);
        long yDim = sourceImage.dimension(1);
        ArrayImgFactory<ARGBType> imageFactory = new ArrayImgFactory<>(new ARGBType());

        // define consumer that will draw components to image and add them to the image stack
        final ArrayList<RandomAccessibleInterval<ARGBType>> componentLevelImageStack = new ArrayList<>();
        Consumer<Pair<List<AdvancedComponent<FloatType>>, Integer>> levelComponentsConsumer = (levelComponentsListAndLevel) -> {
            List<AdvancedComponent<FloatType>> componentOfLevel = levelComponentsListAndLevel.getValue0();
            {
                final RandomAccessibleInterval<ARGBType> componentLevelImage = imageFactory.create(xDim, yDim);
                setImageToValue(Views.iterable(componentLevelImage), new ARGBType(ARGBType.rgba(100, 0, 0, 0)));
                for (AdvancedComponent<FloatType> ctn : componentOfLevel) {
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

    public static <T extends Type<T>, C extends Component<T, C>> void showComponentTree(ComponentForest<C> ct,
                                                                                        List<C> componentsInOptimalSolution,
                                                                                        RandomAccessibleInterval sourceImage) {
        Img<ARGBType> img = drawComponentTreeToImg(ct, componentsInOptimalSolution, sourceImage);
        ImageJFunctions.show(img);
    }

    @NotNull
    public static <T extends Type<T>, C extends Component<T, C>> Img<ARGBType> drawComponentTreeToImg(ComponentForest<C> ct,
                                                                                                      List<C> componentsInOptimalSolution,
                                                                                                      RandomAccessibleInterval sourceImage) {
        if (ct.roots().isEmpty()) {
            throw new ValueException("ct.roots() is empty");
        }

        // create image factory with correct dimensions
        C first = ct.roots().iterator().next();

        // define consumer that will draw components to image and add them to the image stack
        final ArrayList<RandomAccessibleInterval<ARGBType>> componentLevelImageStack = new ArrayList<>();
        Consumer<Pair<List<C>, Integer>> levelComponentsConsumer = (levelComponentsListAndLevel) -> {
            List<C> componentsOfLevel = levelComponentsListAndLevel.getValue0();
            {
                RandomAccessibleInterval<ARGBType> componentLevelImage = createImageWithComponents(componentsOfLevel, componentsInOptimalSolution, sourceImage);
                componentLevelImageStack.add(componentLevelImage);
            }
        };

        // run for components in each level
        ComponentTreeUtils.doForEachComponentInTreeLevel(ct, levelComponentsConsumer);
        Img<ARGBType> img = ImgView.wrap(Views.stack(componentLevelImageStack));
        return img;
    }


    public static <T extends Type<T>, C extends Component<T, C>> RandomAccessibleInterval<ARGBType> createImageWithComponents(List<C> components,
                                                                                                                              List<C> optimalComponents,
                                                                                                                              RandomAccessibleInterval sourceImage) {
//        AdvancedComponent<FloatType> first = components.get(0);
//        RandomAccessibleInterval sourceImage = ((AdvancedComponent) first).getSourceImage();
        Imglib2Utils.createImageWithSameDimension(sourceImage, new ARGBType());
        final RandomAccessibleInterval<ARGBType> resultImage = getArgbTypeRandomAccessibleInterval(sourceImage);
        for (C ctn : components) {
            boolean val = optimalComponents.contains(ctn);
            drawComponentToImage2(ctn, resultImage, val);
        }
        return resultImage;
    }

    private static RandomAccessibleInterval<ARGBType> getArgbTypeRandomAccessibleInterval(RandomAccessibleInterval sourceImage) {
        long xDim = sourceImage.dimension(0);
        long yDim = sourceImage.dimension(1);
        ArrayImgFactory<ARGBType> imageFactory = new ArrayImgFactory<>(new ARGBType());

        final RandomAccessibleInterval<ARGBType> resultImage = imageFactory.create(xDim, yDim);
        return resultImage;
    }

    public static RandomAccessibleInterval<ARGBType> createImageWithComponent(ComponentInterface component) {
        ArrayList<ComponentInterface> componentList = new ArrayList<>();
        componentList.add(component);
        return createImageWithComponents(componentList, new ArrayList<>(), component.getSourceImage());
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

    public static <T extends NativeType<T>> RandomAccessibleInterval<T> createImageWithComponentsNew(List<ComponentInterface> components,
                                                                                                     T val) {
        ComponentInterface first = components.get(0);
        RandomAccessibleInterval sourceImage = ((AdvancedComponent) first).getSourceImage();
        long xDim = sourceImage.dimension(0);
        long yDim = sourceImage.dimension(1);
        ArrayImgFactory<T> imageFactory = new ArrayImgFactory(val);

        final RandomAccessibleInterval<T> resultImage = imageFactory.create(xDim, yDim);
        for (ComponentInterface ctn : components) {
            drawComponentToImage3(ctn, resultImage, val);
        }
        return resultImage;
    }

    public static <T extends Type<T>> void drawSegmentToImage(Iterable<Localizable> component,
                                                              T value,
                                                              RandomAccessibleInterval<T> targetImage) {
        RandomAccess<T> out = targetImage.randomAccess();
        for (Localizable location : component) {
            out.setPosition(location);
            out.get().set(value);
        }
    }

    private static <T extends NativeType> void drawComponentToImage3(final Component<?, ?> ctn,
                                                               RandomAccessibleInterval<T> targetImage,
                                                               T val) {
        RandomAccess<T> out = targetImage.randomAccess();
        for (Localizable location : ctn) {
            out.setPosition(location);
            out.get().set(val);
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

    public static <T extends NumericType<T>> void showImageWithOverlays(RandomAccessibleInterval<T> image, List<MaskPredicate<?>> rois) {
        ImageJ ij = new ImageJ();
        ROITree roiTree = new DefaultROITree();
        roiTree.addROIs(rois);
        Overlay overlay = ij.convert().convert(roiTree, Overlay.class);
        ImagePlus imagePlus = ImageJFunctions.wrap(image, "image");
        imagePlus.setOverlay(overlay);
//        imagePlus.show();
        ij.ui().show(imagePlus);
    }
}
