package com.jug.util.componenttree;

import net.imglib2.Cursor;
import net.imglib2.IterableInterval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.componenttree.ComponentForest;
import net.imglib2.algorithm.componenttree.mser.MserTree;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.Views;

import java.util.ArrayList;
import java.util.function.Predicate;

/**
 * Generates a tree based on the MSER algorithm. Filters the components.
 */
public class ComponentTreeGenerator {
    public ComponentForest<SimpleComponent<FloatType>> buildIntensityTree(final RandomAccessibleInterval<FloatType> raiFkt) {
        float threshold = 0.5f; // TODO-PARAMETRIZE: this should probably become a parameter at some point!
////        Img<FloatType> raiFkt = ((Img<FloatType>) raiFktOrig).copy();
//        Img<FloatType> raiFkt = new ArrayImgFactory(new FloatType()).create(raiFktOrig.numDimensions());
//        DataMover.copy(raiFktOrig, (RandomAccessibleInterval<FloatType>) raiFkt);
////        Img<FloatType> raiFkt = ImgView.wrap(raiFktOrig, new ArrayImgFactory(new FloatType())).copy();
        setPixelBelowThresholdsToZero(raiFkt, threshold);

		final double delta = 0.0001;
//        final double delta = 0.02;
        final int minSize = 50; // minSize=50px seems safe, assuming pixel-area of a round cell with radius of have the bacterial width: 3.141*0.35**2/0.065**2, where pixelSize=0.065mu and width/2=0.35mu
        final long maxSize = Long.MAX_VALUE;
        final double maxVar = 1.0;
        final double minDiversity = 0.2;
        final boolean darkToBright = false;

        // generate MSER tree
        MserTree<FloatType> componentTree = MserTree.buildMserTree(raiFkt, delta, minSize, maxSize, maxVar, minDiversity, darkToBright);

        // filter components by width
        Predicate<Integer> widthCondition = (width) -> (width <= 20);
        ILocationTester widthLimit = new ComponentExtentTester(0, widthCondition);
        ArrayList<ILocationTester> testers = new ArrayList<>();
        testers.add(widthLimit);
        ComponentTester<FloatType, SimpleComponent<FloatType>> tester = new ComponentTester<>(testers);

        // filter components that do not have siblings
        ComponentProperties componentProperties = new ComponentProperties();
        SimpleComponentTree tree = new SimpleComponentTree(componentTree, raiFkt, tester, componentProperties);
        HasSiblingsComponentTester<FloatType, SimpleComponent<FloatType>> siblingTester = new HasSiblingsComponentTester<>();
        tree = new SimpleComponentTree(tree, raiFkt, siblingTester, componentProperties);

        // watershed components into their parent-components
        tree = new RecursiveComponentWatershedder().recursivelyWatershedComponents(tree);

        return tree;
    }

    /**
     * Set all pixels to 0 that are below {@param threshold} value in {@param image}.
     *
     * @param image
     * @param threshold
     */
    private static void setPixelBelowThresholdsToZero(final RandomAccessibleInterval<FloatType> image, float threshold) {
        IterableInterval<FloatType> iterableSource = Views.iterable(image);
        Cursor<FloatType> cursor = iterableSource.cursor();
        while (cursor.hasNext()) {
            cursor.next();
            float val = cursor.get().getRealFloat();
            if (val > 0.0f && val < threshold) {
                cursor.get().set(0);
            }
        }
    }

}
