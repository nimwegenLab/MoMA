package com.jug.util.componenttree;

import com.jug.datahandling.IImageProvider;
import com.jug.util.imglib2.Imglib2Utils;
import ij.ImagePlus;
import ij.process.ImageProcessor;
import net.imglib2.algorithm.componenttree.ComponentForest;
import net.imglib2.algorithm.componenttree.mser.MserTree;
import net.imglib2.img.Img;
import net.imglib2.img.ImgView;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.NativeType;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.Views;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.function.Predicate;

/**
 * Generates a tree based on the MSER algorithm. Filters the components.
 */
public class ComponentTreeGenerator {
    private RecursiveComponentWatershedder recursiveComponentWatershedder;
    private ComponentProperties componentPropertiesCalculator;
    private WatershedMaskGenerator watershedMaskGenerator;
    private Imglib2Utils imglib2Utils;

    public ComponentTreeGenerator(RecursiveComponentWatershedder recursiveComponentWatershedder,
                                  ComponentProperties componentPropertiesCalculator,
                                  WatershedMaskGenerator watershedMaskGenerator,
                                  Imglib2Utils imglib2Utils) {
        this.recursiveComponentWatershedder = recursiveComponentWatershedder;
        this.componentPropertiesCalculator = componentPropertiesCalculator;
        this.watershedMaskGenerator = watershedMaskGenerator;
        this.imglib2Utils = imglib2Utils;
    }

    public <T extends NumericType<T> & NativeType<T>> Img<T> scaleImageV001(Img<T> inputImage, int scaleFactor){
        ImagePlus raiFktImp = ImageJFunctions.wrap(inputImage, "tmp_image");
        raiFktImp.show();
        int targetWidth = scaleFactor * raiFktImp.getWidth();
        int targetHeight = scaleFactor * raiFktImp.getHeight();
        ImageProcessor ip = raiFktImp.getProcessor();
        ip.setInterpolationMethod(ImageProcessor.BICUBIC);
        ip = ip.resize(targetWidth, targetHeight);
        BufferedImage bufferedImage = ip.getBufferedImage();
        ImagePlus scaledImp = new ImagePlus("bufferedImage", bufferedImage);
        scaledImp.show();
        Img<T> res = ImageJFunctions.wrap(scaledImp);
        return res;
    }

    public ComponentForest<AdvancedComponent<FloatType>> buildIntensityTree(final IImageProvider imageProvider, int frameIndex) {
        Img<FloatType> img = imageProvider.getImgProbs();
        Img<FloatType> raiFktOld = ImgView.wrap(Views.hyperSlice(img, 2, frameIndex));

        double scaleFactor = 2.;

//        Img<FloatType> raiFkt = scaleImageV001(raiFktOld, scaleFactor);
        Img<FloatType> raiFkt = ImgView.wrap(imglib2Utils.scaleImage(raiFktOld, scaleFactor));

        Img<BitType> mask = watershedMaskGenerator.generateMask(ImgView.wrap(raiFkt));
        raiFkt = imglib2Utils.maskImage(raiFkt, mask, new FloatType(.0f));

		final double delta = 0.0001;
//        final double delta = 0.02;
        final int minSize = 50; // minSize=50px seems safe, assuming pixel-area of a round cell with radius of have the bacterial width: 3.141*0.35**2/0.065**2, where pixelSize=0.065mu and width/2=0.35mu
        final long maxSize = Long.MAX_VALUE;
        final double maxVar = 1.0;
        final double minDiversity = 0.2;
        final boolean darkToBright = false;
        final int maxWidth = 160;

        // generate MSER tree
        MserTree<FloatType> componentTree = MserTree.buildMserTree(raiFkt, delta, minSize, maxSize, maxVar, minDiversity, darkToBright);

        // filter components by width
        Predicate<Integer> widthCondition = (width) -> (width <= maxWidth);
        ILocationTester widthLimit = new ComponentExtentTester(0, widthCondition);
        ArrayList<ILocationTester> testers = new ArrayList<>();
        testers.add(widthLimit);
        ComponentTester<FloatType, AdvancedComponent<FloatType>> tester = new ComponentTester<>(testers);

        // filter components that do not have siblings
        SimpleComponentTree tree = new SimpleComponentTree(componentTree, raiFkt, tester, componentPropertiesCalculator);
        HasSiblingsComponentTester<FloatType, AdvancedComponent<FloatType>> siblingTester = new HasSiblingsComponentTester<>();
        tree = new SimpleComponentTree(tree, raiFkt, siblingTester, componentPropertiesCalculator);

        // watershed components into their parent-components
        tree = recursiveComponentWatershedder.recursivelyWatershedComponents(tree);

        return tree;
    }
}
