package com.jug.util.componenttree;

import com.jug.util.ComponentTreeUtils;
import net.imagej.ops.OpService;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.img.ImgView;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.roi.labeling.LabelRegion;
import net.imglib2.roi.labeling.LabelRegions;
import net.imglib2.type.Type;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.Views;
import org.javatuples.Pair;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;


public class RecursiveComponentWatershedder<T extends Type<T>> {
    private final OpService ops;

    public RecursiveComponentWatershedder(OpService ops) {
        this.ops = ops;
    }

    /**
     * Recursively grow child components into their corresponding parent components. After running this the child
     * components in the tree will always possess the combined area as their corresponding parent component.
     *
     * @param tree
     * @return
     */
    public AdvancedComponentForest<T, AdvancedComponent<T>> recursivelyWatershedComponents(AdvancedComponentForest<T, AdvancedComponent<T>> tree) {
        Consumer<Pair<List<AdvancedComponent<T>>, Integer>> levelComponentsConsumer = (levelComponentsListAndLevel) -> {
            List<AdvancedComponent<T>> componentsOfLevel = levelComponentsListAndLevel.getValue0();
            {
                watershedChildrenOfThisLevel(componentsOfLevel);
            }
        };
        ComponentTreeUtils.doForEachComponentInTreeLevel(tree, levelComponentsConsumer);
        return tree;
    }

    /**
     * Perform the watershedding of all child components of the parents in {@param parentComponents}.
     *
     * @param parentComponents list of parent components
     */
    private void watershedChildrenOfThisLevel(List<AdvancedComponent<T>> parentComponents) {
        List<AdvancedComponent<T>> parentsWithChildren = new ArrayList<>();
        for (AdvancedComponent<T> parent : parentComponents) {
            if (parent.getChildren().size() != 0) {
                parentsWithChildren.add(parent);
            }
        }
        if (parentsWithChildren.size() == 0) return; /* If no parent has children, we are done. */

        Img<T> sourceImage = ImgView.wrap(parentsWithChildren.get(0).getSourceImage(), new ArrayImgFactory(new FloatType()));

        RandomAccessibleInterval<BitType> parentsMask = getParentsMask(parentsWithChildren);
        HashMap<Integer, AdvancedComponent<T>> childLabelToComponentMap = new HashMap<>();
        ImgLabeling<Integer, IntType> childLabeling = getChildLabeling(parentsWithChildren, childLabelToComponentMap);
        ImgLabeling<Integer, IntType> out = doWatershed(sourceImage, childLabeling, parentsMask);
        LabelRegions<Integer> regions = new LabelRegions<>(out);

        for (Integer label : childLabelToComponentMap.keySet()) {
            LabelRegion<Integer> region = regions.getLabelRegion(label);
            AdvancedComponent<T> child = childLabelToComponentMap.get(label);
            child.setRegion(region);
        }
    }

    /**
     * Returns a mask of the parent components in {@param parentComponents}, which is used for the masked watershedding.
     *
     * @param parentComponents
     * @return
     */
    @NotNull
    private RandomAccessibleInterval<BitType> getParentsMask(List<AdvancedComponent<T>> parentComponents) {
        Img<T> sourceImage = ImgView.wrap(parentComponents.get(0).getSourceImage(), new ArrayImgFactory(new FloatType()));
        ImgLabeling<Integer, IntType> parentLabeling = createLabelingImage(sourceImage);
        Integer label = 1;
        for (AdvancedComponent<T> parent : parentComponents) { /* write to labeling image */
            parent.writeLabels(parentLabeling, label);
        }
        RandomAccessibleInterval<BitType> mask = ops.convert().bit(Views.iterable(parentLabeling.getIndexImg())); /* convert labeling to mask*/
        return mask;
    }

    /**
     * Generates a labelling for all children components of the parent components in {@param parents}.
     * {@param childLabelToComponentMap} holds a mapping of child components to their corresponding label.
     *
     * @param parents
     * @param childLabelToComponentMap
     * @return
     */
    @NotNull
    private ImgLabeling<Integer, IntType> getChildLabeling(List<AdvancedComponent<T>> parents, HashMap<Integer, AdvancedComponent<T>> childLabelToComponentMap) {
        Img<T> sourceImage = ImgView.wrap(parents.get(0).getSourceImage(), new ArrayImgFactory(new FloatType()));
        ImgLabeling<Integer, IntType> childLabeling = createLabelingImage(sourceImage);
        Integer childLabel = 1;
        for (AdvancedComponent<T> parent : parents) {
            List<AdvancedComponent<T>> children = parent.getChildren();
            for (AdvancedComponent<T> child : children) {
                child.writeLabels(childLabeling, childLabel);
                childLabelToComponentMap.put(childLabel, child);
                ++childLabel;
            }
        }
        return childLabeling;
    }

    /**
     * Convenience method for creating a labeling image with same dimensions as the source image.
     *
     * @param sourceImage
     * @return
     */
    private ImgLabeling<Integer, IntType> createLabelingImage(RandomAccessibleInterval sourceImage) {
        long[] dims = new long[sourceImage.numDimensions()];
        sourceImage.dimensions(dims);
        Img<IntType> img = ArrayImgs.ints(dims);
        return new ImgLabeling<>(img);
    }

    /**
     * Perform the watershedding on the sourceImage using the parent-components as masks and the (eroded) child-components
     * as markers. We invert sourceImage, so that the locations of the cells (which have value 1) become the basins for
     * the watershedding.
     *
     * @param sourceImage
     * @param markers
     * @param mask
     * @return
     */
    private ImgLabeling<Integer, IntType> doWatershed(final Img sourceImage, final ImgLabeling<Integer, IntType> markers, RandomAccessibleInterval<BitType> mask) {
        long[] dims = new long[sourceImage.numDimensions()];
        sourceImage.dimensions(dims);
        Img<FloatType> invertedSourceImage = sourceImage.factory().create(sourceImage);
        ops.image().invert(invertedSourceImage, sourceImage); // invert the intensities of the probability maps, so that cell-areas are watershed basins
        return ops.image().watershed(null, invertedSourceImage, markers, false, true, mask);
    }
}
