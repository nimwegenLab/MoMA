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
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;


public class RecursiveComponentWatershedder<T extends Type<T>, C extends Component<T, C>> {
    private OpService ops = (new Context()).service(OpService.class);

    @NotNull
    private RandomAccessibleInterval<BitType> getParentsMask(List<SimpleComponent<T>> parentsWithChildren) {
        Img<T> sourceImage = ImgView.wrap(parentsWithChildren.get(0).getSourceImage(), new ArrayImgFactory(new FloatType()));
        ImgLabeling<Integer, IntType> parentLabeling = createLabelingImage(sourceImage);
        Integer label = 1;
        for(SimpleComponent<T> parent : parentsWithChildren){ // write parents to mask
            parent.writeLabels(parentLabeling, label);
        }
        RandomAccessibleInterval<BitType> mask = ops.convert().bit(Views.iterable(parentLabeling.getIndexImg()));
        return mask;
    }

    @NotNull
    private ImgLabeling<Integer, IntType> getChildLabeling(List<SimpleComponent<T>> parents, HashMap<Integer, SimpleComponent<T>> childLabelToComponentMap) {
        Img<T> sourceImage = ImgView.wrap(parents.get(0).getSourceImage(), new ArrayImgFactory(new FloatType()));
        ImgLabeling<Integer, IntType> childLabeling = createLabelingImage(sourceImage);
        Integer childLabel = 1;
        for(SimpleComponent<T> parent : parents){
            List<SimpleComponent<T>> children = parent.getChildren();
            for(SimpleComponent<T> child: children){
                child.writeLabels(childLabeling, childLabel);
                childLabelToComponentMap.put(childLabel, child);
                ++childLabel;
            }
        }
        return childLabeling;
    }

    private void watershedChildrenIntoThisLevel(List<SimpleComponent<T>> componentsOfLevel) {
        List<SimpleComponent<T>> parentsWithChildren = new ArrayList<>();
        for (SimpleComponent<T> parent : componentsOfLevel) {
            if (parent.getChildren().size() != 0) {
                parentsWithChildren.add(parent);
            }
        }
        if (parentsWithChildren.size() == 0) return; // If no parent has children, we are done.

        Img<T> sourceImage = ImgView.wrap(parentsWithChildren.get(0).getSourceImage(), new ArrayImgFactory(new FloatType()));

        RandomAccessibleInterval<BitType> parentsMask = getParentsMask(parentsWithChildren);
        HashMap<Integer, SimpleComponent<T>> childLabelToComponentMap = new HashMap<>();
        ImgLabeling<Integer, IntType> childLabeling = getChildLabeling(parentsWithChildren, childLabelToComponentMap);
        childLabeling = erodeLabels(childLabeling, sourceImage);
        ImgLabeling<Integer, IntType> out = doWatershed(sourceImage, childLabeling, parentsMask);
        LabelRegions<Integer> regions = new LabelRegions<>(out);

        for (Integer label : childLabelToComponentMap.keySet()) {
            LabelRegion<Integer> region = regions.getLabelRegion(label);
            SimpleComponent<T> child = childLabelToComponentMap.get(label);
            child.setRegion(region);
        }
//
//
//        ///////////////////////////////////////////////////////////////////////////////////////////////
//
//        List<SimpleComponent<T>> children = parent.getChildren();
//        if (children.size() == 0) return; // no children to watershed
//
//        Img<T> sourceImage = ImgView.wrap(parent.getSourceImage(), new ArrayImgFactory(new FloatType()));
//        ImgLabeling<Integer, IntType> parentLabeling = createLabelingImage(sourceImage);
//        ImgLabeling<Integer, IntType> childLabeling = createLabelingImage(sourceImage);
//
//        Integer label = 1;
//        parent.writeLabels(parentLabeling, label);
//        for(SimpleComponent<T> child: children){
//            child.writeLabels(childLabeling, label);
//            ++label;
//        }
//        childLabeling = erodeLabels(childLabeling, sourceImage);
//        RandomAccessibleInterval<BitType> mask = ops.convert().bit(Views.iterable(parentLabeling.getIndexImg()));
//
////        ImageJFunctions.show(mask, "mask");
////        ImageJFunctions.show(childLabeling.getIndexImg(), "merged child image");
//
//        ImgLabeling<Integer, IntType> out = doWatershed(sourceImage, childLabeling, mask);
//        LabelRegions<Integer> regions = new LabelRegions<>(out);
//
//        label = 1;
//        for(SimpleComponent<T> child: children){
//            LabelRegion<Integer> region = regions.getLabelRegion(label);
//            child.setRegion(region);
//            ++label;
//        }
    }

    public SimpleComponentTree<T, C> recursivelyWatershedComponents(SimpleComponentTree<T, C> tree) {

        Consumer<Pair<List<SimpleComponent<T>>, Integer>> levelComponentsConsumer = (levelComponentsListAndLevel)-> {
            List<SimpleComponent<T>> componentsOfLevel = levelComponentsListAndLevel.getValue0();
            {
                watershedChildrenIntoThisLevel(componentsOfLevel);
            }
        };

        // run for components in each level
        ComponentTreeUtils.doForEachComponentInTreeLevel(tree, levelComponentsConsumer);
//
//
//        for (final SimpleComponent root : tree.roots()) {
//            RecursivelyWatershed(root);
//        }
//        return tree;
//        throw new NotImplementedException();
        return tree;
    }

    private ImgLabeling<Integer, IntType> erodeLabels(ImgLabeling<Integer, IntType> labeling, RandomAccessibleInterval<T> sourceImage){
        ImgLabeling<Integer, IntType> labelingEroded = createLabelingImage(sourceImage);
        RandomAccessibleInterval<IntType> backingImage = labeling.getIndexImg();
        backingImage = (RandomAccessibleInterval) ops.morphology().erode(backingImage, new RectangleShape(1, false));
        Cursor<LabelingType<Integer>> labelingErodedCursor = labelingEroded.cursor();
        Cursor<LabelingType<Integer>> labelingCursor = labeling.cursor();
        RandomAccess<IntType> rndAcc = backingImage.randomAccess();

        while(labelingErodedCursor.hasNext()){
            labelingErodedCursor.fwd();
            labelingCursor.fwd();
            rndAcc.setPosition(labelingErodedCursor);
            if(rndAcc.get().get() != 0) {
                labelingErodedCursor.get().set(labelingCursor.get().copy());
            }
        }

//        ImageJFunctions.show(labeling.getIndexImg(), "eroded");
//        ImageJFunctions.show(labelingOrig.getIndexImg(), "non-eroded");

        return labelingEroded;
    }

    private void RecursivelyWatershed(SimpleComponent<T> parent) {
        List<SimpleComponent<T>> children = parent.getChildren();
        if (children.size() == 0) return; // no children to watershed

        Img<T> sourceImage = ImgView.wrap(parent.getSourceImage(), new ArrayImgFactory(new FloatType()));
        ImgLabeling<Integer, IntType> parentLabeling = createLabelingImage(sourceImage);
        ImgLabeling<Integer, IntType> childLabeling = createLabelingImage(sourceImage);

        Integer label = 1;
        parent.writeLabels(parentLabeling, label);
        for(SimpleComponent<T> child: children){
            child.writeLabels(childLabeling, label);
            ++label;
        }
        childLabeling = erodeLabels(childLabeling, sourceImage);
        RandomAccessibleInterval<BitType> mask = ops.convert().bit(Views.iterable(parentLabeling.getIndexImg()));

//        ImageJFunctions.show(mask, "mask");
//        ImageJFunctions.show(childLabeling.getIndexImg(), "merged child image");

        ImgLabeling<Integer, IntType> out = doWatershed(sourceImage, childLabeling, mask);
        LabelRegions<Integer> regions = new LabelRegions<>(out);



        // TODO: THIS NEEDS TO BE FIXED, TO MAKE SURE THE LABELLING FROM THE WATERSHEDDING CORRESPONDS TO THE LABELING FROM THE MARKER-IMAGE AND CORRESPONDING COMPONENTS
        label = 1;
        for(SimpleComponent<T> child: children){
            LabelRegion<Integer> region = regions.getLabelRegion(label);
            child.setRegion(region);
            ++label;
        }

        for(SimpleComponent<T> child: children){
            RecursivelyWatershed(child);
        }

//        ImageJFunctions.show((Img) sourceImage);
//        ImageJFunctions.show(childLabeling.getIndexImg());
//        ImageJFunctions.show(mask);
//        ImageJFunctions.show(out.getIndexImg());

//        printRegionSizes(childLabeling);
//        System.out.println("------");
//        printRegionSizes(out);

    }

    private ImgLabeling<Integer, IntType> createLabelingImage(RandomAccessibleInterval<T> sourceImage){
        long[] dims = new long[sourceImage.numDimensions()];
        sourceImage.dimensions(dims);
        Img<IntType> img = ArrayImgs.ints(dims);
        return new ImgLabeling<>(img);
    }

    private ImgLabeling<Integer, IntType> doWatershed(final Img in, final ImgLabeling<Integer, IntType> markers, RandomAccessibleInterval<BitType> mask) {
//        return (ImgLabeling<Integer, IntType>) ops.run(WatershedSeeded.class, null, in,
//                markers, true, false, mask);
        long[] dims = new long[in.numDimensions()];
        in.dimensions(dims);
        Img<IntType> img = ArrayImgs.ints(dims);
        ImgLabeling<Integer, IntType> res = new ImgLabeling<Integer, IntType>(img);
        Img<FloatType> input = in.factory().create(in); //  QUESTION: if I use generic Img<t> here, the compiler issues a warning. Why?!
        ops.image().invert(input, in);
        return ops.image().watershed(null, input, markers, false, false, mask);
//        try
//        {
//        res = ops.image().watershed(null, input, markers, false, false, mask);
//            return res;
//        }
//        catch(java.util.NoSuchElementException err)
//        {
//            ImageJFunctions.show(in);
//            ImageJFunctions.show(markers.getSource());
//            ImageJFunctions.show(mask);
//            return res;
//        }
    }

    private void printRegionSizes(ImgLabeling<Integer, IntType> seedLabels) {
        LabelRegions<Integer> regions = new LabelRegions<>(seedLabels);
        Iterator<LabelRegion<Integer>> regionIterator = regions.iterator();
        while (regionIterator.hasNext()) {
            LabelRegion<Integer> region = regionIterator.next();
            System.out.println(String.format("label: %d", region.getLabel()));
            System.out.println(String.format("region size: %d", region.size()));
        }
    }
}
