package com.jug.util.componenttree;

import com.jug.config.IComponentTreeGeneratorConfiguration;
import com.jug.datahandling.IImageProvider;
import com.jug.util.imglib2.Imglib2Utils;
import com.moma.auxiliary.Plotting;
import net.imglib2.algorithm.binary.Thresholder;
import net.imglib2.algorithm.componenttree.ComponentForest;
import net.imglib2.algorithm.componenttree.mser.MserTree;
import net.imglib2.img.Img;
import net.imglib2.img.ImgView;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.real.FloatType;

import java.util.ArrayList;
import java.util.function.Predicate;

/**
 * Generates a tree based on the MSER algorithm. Filters the components.
 */
public class ComponentTreeGenerator {
    private IComponentTreeGeneratorConfiguration configuration;
    private RecursiveComponentWatershedder recursiveComponentWatershedder;
    private ComponentProperties componentPropertiesCalculator;
    private WatershedMaskGenerator watershedMaskGenerator;
    private Imglib2Utils imglib2Utils;

    public ComponentTreeGenerator(IComponentTreeGeneratorConfiguration configuration,
                                  RecursiveComponentWatershedder recursiveComponentWatershedder,
                                  ComponentProperties componentPropertiesCalculator,
                                  WatershedMaskGenerator watershedMaskGenerator,
                                  Imglib2Utils imglib2Utils) {
        this.configuration = configuration;
        this.recursiveComponentWatershedder = recursiveComponentWatershedder;
        this.componentPropertiesCalculator = componentPropertiesCalculator;
        this.watershedMaskGenerator = watershedMaskGenerator;
        this.imglib2Utils = imglib2Utils;
    }

    public ComponentForest<AdvancedComponent<FloatType>> buildIntensityTree(Img<FloatType> raiFkt, int frameIndex, float componentSplittingThreshold) {
        /* generate image mask for component generation; watershedMaskGenerator.generateMask(...) also merges adjacent connected components, if values between do fall below a given cutoff (see implementation) */
        Img<BitType> mask = watershedMaskGenerator.generateMask(ImgView.wrap(raiFkt));

        /* fill holes in water shedding mask to avoid components from having holes */
        mask = ImgView.wrap(imglib2Utils.fillHoles(mask));

        raiFkt = imglib2Utils.maskImage(raiFkt, mask, new FloatType(.0f));

        /* set values >componentSplittingThreshold to 1; this avoids over segmentation during component generation */
        Img<BitType> mask2 = Thresholder.threshold(raiFkt, new FloatType(componentSplittingThreshold), false, 1);
        raiFkt = imglib2Utils.maskImage(raiFkt, mask2, new FloatType(1.0f));


        final double delta = 0.0001;
//        final double delta = 0.02;
        final int minSize = 5; // this sets the minimum size of components during component generation for root components as well as child components. We set this to a low value to ensure a deep segmentation of our components. The minimum size of root and child components is then filtered using LeafComponentSizeTester and RootComponentSizeTester (see below).
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
        ComponentTester<FloatType, AdvancedComponent<FloatType>> tester = new ComponentTester<>(testers);

        SimpleComponentTree<FloatType, AdvancedComponent<FloatType>> tree = new SimpleComponentTree(componentTree, raiFkt, frameIndex, tester, componentPropertiesCalculator);
        tree = recursiveComponentWatershedder.recursivelyWatershedComponents(tree); /* IMPORTANT: this step watersheds components into their parent-components, which yields the final size of components; this needs to be done before performing the following filter-steps on component-size, etc. */

        IComponentTester rootSizeTester = new RootComponentSizeTester(configuration.getSizeMinimumOfParentComponent());
        tree = new SimpleComponentTree(tree, raiFkt, frameIndex, rootSizeTester , componentPropertiesCalculator);

        IComponentTester leafSizeTester = new LeafComponentSizeTester(configuration.getSizeMinimumOfLeafComponent());
        tree = new SimpleComponentTree(tree, raiFkt, frameIndex, leafSizeTester , componentPropertiesCalculator);

        HasSiblingsComponentTester<FloatType, AdvancedComponent<FloatType>> siblingTester = new HasSiblingsComponentTester<>();
        tree = new SimpleComponentTree(tree, raiFkt, frameIndex, siblingTester, componentPropertiesCalculator); /* IMPORTANT: this removes all child-nodes that do not have siblings; we need to do this at the very end, because the filters above may remove child-nodes, which can yield single child nodes _without_ sibling */

//        for (AdvancedComponent component : tree.getAllComponents()) {
//            if (component.getChildren().size() > 2) {
//                throw new RuntimeException("component" + component.getStringId() + " has >2 child-nodes.");
//            }
//        }

        return tree;
    }
}
