package com.jug.util.componenttree;

import com.jug.util.DataMover;
import net.imglib2.Cursor;
import net.imglib2.IterableInterval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.componenttree.ComponentForest;
import net.imglib2.algorithm.componenttree.mser.MserTree;
import net.imglib2.img.Img;
import net.imglib2.img.ImgView;
import net.imglib2.img.array.ArrayImg;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.Views;

import java.util.ArrayList;
import java.util.function.Predicate;

import static com.jug.MoMA.GL_OFFSET_BOTTOM;
import static com.jug.MoMA.GL_OFFSET_TOP;

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
        SimpleComponentTree tree = new SimpleComponentTree(componentTree, raiFkt, tester);
        HasSiblingsComponentTester<FloatType, SimpleComponent<FloatType>> siblingTester = new HasSiblingsComponentTester<>();
        tree = new SimpleComponentTree(tree, raiFkt, siblingTester);

        // watershed components into their parent-components
        tree = new RecursiveComponentWatershedder().recursivelyWatershedComponents(tree);

        // filter components that are (partially) outside of our ROI
        Predicate<Integer> condition = (pos) -> (pos >= GL_OFFSET_TOP && pos <= raiFkt.dimension(1) - GL_OFFSET_BOTTOM);
        ILocationTester verticalBoundsLimit = new PixelPositionTester(1, condition);
        testers = new ArrayList<>();
        testers.add(verticalBoundsLimit);
        tester = new ComponentTester<>(testers);
        tree = new SimpleComponentTree(tree, raiFkt, tester);

//        Plotting.drawComponentTree2(tree, new ArrayList<>());
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
