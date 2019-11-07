package com.jug.util.componenttree;

import com.jug.util.ComponentTreeUtils;
import net.imagej.ops.OpService;
import net.imglib2.Cursor;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.componenttree.Component;
import net.imglib2.algorithm.neighborhood.RectangleShape;
import net.imglib2.img.Img;
import net.imglib2.img.ImgView;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.roi.labeling.LabelRegion;
import net.imglib2.roi.labeling.LabelRegions;
import net.imglib2.roi.labeling.LabelingType;
import net.imglib2.type.Type;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.Views;
import org.javatuples.Pair;
import org.jetbrains.annotations.NotNull;
import org.scijava.Context;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;


public class RecursiveComponentWatershedder<T extends Type<T>, C extends Component<T, C>> {
    private OpService ops = (new Context()).service(OpService.class);

    /**
     * Recursively grow child components into their corresponding parent components. After running this the child
     * components in the tree will always possess the combined area as their corresponding parent component.
     *
     * @param tree
     * @return
     */
    public SimpleComponentTree<T, C> recursivelyWatershedComponents(SimpleComponentTree<T, C> tree) {
        Consumer<Pair<List<SimpleComponent<T>>, Integer>> levelComponentsConsumer = (levelComponentsListAndLevel) -> {
            List<SimpleComponent<T>> componentsOfLevel = levelComponentsListAndLevel.getValue0();
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
    private void watershedChildrenOfThisLevel(List<SimpleComponent<T>> parentComponents) {
        List<SimpleComponent<T>> parentsWithChildren = new ArrayList<>();
        for (SimpleComponent<T> parent : parentComponents) {
            if (parent.getChildren().size() != 0) {
                parentsWithChildren.add(parent);
            }
        }
        if (parentsWithChildren.size() == 0) return; /* If no parent has children, we are done. */

        Img<T> sourceImage = ImgView.wrap(parentsWithChildren.get(0).getSourceImage(), new ArrayImgFactory(new FloatType()));

        RandomAccessibleInterval<BitType> parentsMask = getParentsMask(parentsWithChildren);
        HashMap<Integer, SimpleComponent<T>> childLabelToComponentMap = new HashMap<>();
        ImgLabeling<Integer, IntType> childLabeling = getChildLabeling(parentsWithChildren, childLabelToComponentMap);
        childLabeling = erodeLabels(childLabeling);
        ImgLabeling<Integer, IntType> out = doWatershed(sourceImage, childLabeling, parentsMask);
        LabelRegions<Integer> regions = new LabelRegions<>(out);

        for (Integer label : childLabelToComponentMap.keySet()) {
            LabelRegion<Integer> region = regions.getLabelRegion(label);
            SimpleComponent<T> child = childLabelToComponentMap.get(label);
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
    private RandomAccessibleInterval<BitType> getParentsMask(List<SimpleComponent<T>> parentComponents) {
        Img<T> sourceImage = ImgView.wrap(parentComponents.get(0).getSourceImage(), new ArrayImgFactory(new FloatType()));
        ImgLabeling<Integer, IntType> parentLabeling = createLabelingImage(sourceImage);
        Integer label = 1;
        for (SimpleComponent<T> parent : parentComponents) { /* write to labeling image */
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
    private ImgLabeling<Integer, IntType> getChildLabeling(List<SimpleComponent<T>> parents, HashMap<Integer, SimpleComponent<T>> childLabelToComponentMap) {
        Img<T> sourceImage = ImgView.wrap(parents.get(0).getSourceImage(), new ArrayImgFactory(new FloatType()));
        ImgLabeling<Integer, IntType> childLabeling = createLabelingImage(sourceImage);
        Integer childLabel = 1;
        for (SimpleComponent<T> parent : parents) {
            List<SimpleComponent<T>> children = parent.getChildren();
            for (SimpleComponent<T> child : children) {
                child.writeLabels(childLabeling, childLabel);
                childLabelToComponentMap.put(childLabel, child);
                ++childLabel;
            }
        }
        return childLabeling;
    }

    /**
     * This method erodes the child labeling. This is necessary, because there is a bug in ops.image().watershed, which
     * fails, if markers partially overlap with the border of a masked area (here: the region inside the parent mask).
     * When this bug is fixed, then this eroding of labels should not be needed anymore.
     *
     * @param labeling
     * @return
     */
    private ImgLabeling<Integer, IntType> erodeLabels(ImgLabeling<Integer, IntType> labeling) {
        ImgLabeling<Integer, IntType> labelingEroded = createLabelingImage(labeling.getIndexImg());
        RandomAccessibleInterval<IntType> backingImage = labeling.getIndexImg();
        backingImage = (RandomAccessibleInterval) ops.morphology().erode(backingImage, new RectangleShape(1, false));
        Cursor<LabelingType<Integer>> labelingErodedCursor = labelingEroded.cursor();
        Cursor<LabelingType<Integer>> labelingCursor = labeling.cursor();
        RandomAccess<IntType> rndAcc = backingImage.randomAccess();

        while (labelingErodedCursor.hasNext()) {
            labelingErodedCursor.fwd();
            labelingCursor.fwd();
            rndAcc.setPosition(labelingErodedCursor);
            if (rndAcc.get().get() != 0) {
                labelingErodedCursor.get().set(labelingCursor.get().copy());
            }
        }
        return labelingEroded;
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
        return ops.image().watershed(null, invertedSourceImage, markers, false, false, mask);
    }
}
