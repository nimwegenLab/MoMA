package com.jug.util.componenttree;

import com.jug.config.IComponentForestGeneratorConfiguration;
import com.jug.datahandling.IImageProvider;
import com.jug.util.imglib2.Imglib2Utils;
import com.moma.auxiliary.Plotting;
import net.imglib2.algorithm.binary.Thresholder;
import net.imglib2.algorithm.componenttree.mser.MserTree;
import net.imglib2.img.Img;
import net.imglib2.img.ImgView;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.real.FloatType;

import java.util.ArrayList;
import java.util.function.Predicate;

/**
 * Generates a tree based on the MSER algorithm. Filters the components.
 */
public class ComponentForestGenerator implements IComponentForestGenerator {
    private IComponentForestGeneratorConfiguration configuration;
    private RecursiveComponentWatershedder recursiveComponentWatershedder;
    private ComponentProperties componentPropertiesCalculator;
    private WatershedMaskGenerator watershedMaskGenerator;
    private Imglib2Utils imglib2Utils;

    public ComponentForestGenerator(IComponentForestGeneratorConfiguration configuration,
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

    public AdvancedComponentForest<FloatType, AdvancedComponent<FloatType>> buildComponentForest(IImageProvider imageProvider, int frameIndex, float componentSplittingThreshold) {
        Img<FloatType> raiFkt = imageProvider.getImgProbsAt(frameIndex);

        /* generate image mask for component generation; watershedMaskGenerator.generateMask(...) also merges adjacent connected components, if values between do fall below a given cutoff (see implementation) */
        Img<BitType> mask = watershedMaskGenerator.generateMask(ImgView.wrap(raiFkt));

        /* fill holes in water shedding mask to avoid components from having holes */
        mask = ImgView.wrap(imglib2Utils.fillHoles(mask));

        Img<FloatType> raiFktMasked = imglib2Utils.maskImage(raiFkt, mask, new FloatType(.0f));

        /* set values >componentSplittingThreshold to 1; this avoids over-segmentation during component generation */
        Img<BitType> mask2 = Thresholder.threshold(raiFktMasked, new FloatType(componentSplittingThreshold), false, 1);
        raiFktMasked = imglib2Utils.maskImage(raiFktMasked, mask2, new FloatType(1.0f));

        final double delta = 0.0001;
//        final double delta = 0.02;
        final int minSize = 5; // this sets the minimum size of components during component generation for root components as well as child components. We set this to a low value to ensure a deep segmentation of our components. The minimum size of root and child components is then filtered using LeafComponentSizeTester and RootComponentSizeTester (see below).
        final long maxSize = Long.MAX_VALUE;
        final double maxVar = 1.0;
        final double minDiversity = 0.2;
        final boolean darkToBright = false;

        // generate MSER tree
        MserTree<FloatType> componentTree = MserTree.buildMserTree(raiFktMasked, delta, minSize, maxSize, maxVar, minDiversity, darkToBright);

        // filter components by width
        Predicate<Integer> widthCondition = (width) -> (width <= configuration.getMaximumComponentWidth());
        ILocationTester widthLimit = new ComponentExtentTester(0, widthCondition);
        ArrayList<ILocationTester> testers = new ArrayList<>();
        testers.add(widthLimit);
        ComponentTester<FloatType, AdvancedComponent<FloatType>> tester = new ComponentTester<>(testers);

        AdvancedComponentForest<FloatType, AdvancedComponent<FloatType>> tree = new AdvancedComponentForest(componentTree, raiFktMasked, frameIndex, tester, componentPropertiesCalculator, imageProvider);
        tree = recursiveComponentWatershedder.recursivelyWatershedComponents(tree); /* IMPORTANT: this step watersheds components into their parent-components, which yields the final size of components; this needs to be done before performing the following filter-steps on component-size, etc. */

        IComponentTester rootSizeTester = new RootComponentSizeTester(configuration.getSizeMinimumOfParentComponent());
        tree = new AdvancedComponentForest(tree, raiFktMasked, frameIndex, rootSizeTester , componentPropertiesCalculator, imageProvider);

        IComponentTester leafSizeTester = new LeafComponentSizeTester(configuration.getSizeMinimumOfLeafComponent());
        tree = new AdvancedComponentForest(tree, raiFktMasked, frameIndex, leafSizeTester , componentPropertiesCalculator, imageProvider);

        HasSiblingsComponentTester<FloatType, AdvancedComponent<FloatType>> siblingTester = new HasSiblingsComponentTester<>();
        tree = new AdvancedComponentForest(tree, raiFktMasked, frameIndex, siblingTester, componentPropertiesCalculator, imageProvider); /* IMPORTANT: this removes all child-nodes that do not have siblings; we need to do this at the very end, because the filters above may remove child-nodes, which can yield single child nodes _without_ sibling */

//        for (AdvancedComponent component : tree.getAllComponents()) {
//            if (component.getChildren().size() > 2) {
//                throw new RuntimeException("component" + component.getStringId() + " has >2 child-nodes.");
//            }
//        }

//        Plotting.showComponentForest(tree, new ArrayList<>(), tree.getSourceImage()); // TODO-20221214: remove this, when done with debugging.
//        Img<ARGBType> img = Plotting.getComponentForestAsImg(tree, new ArrayList<>(), tree.getSourceImage()); // TODO-20221214: remove this, when done with debugging.
//        Imglib2Utils.saveImage(img,
//                "component_tree_frame_" + frameIndex,
//                "/home/micha/Documents/01_work/15_moma_notes/02_moma_development/bugfix/20220112-fix-spurious-ilp-infeasible-error/debug_scratch_folder/component_tree_frame_"+frameIndex+".tiff");
        return tree;
    }
}
