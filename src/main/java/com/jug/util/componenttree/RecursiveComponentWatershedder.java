package com.jug.util.componenttree;

import net.imagej.ops.OpService;
import net.imagej.ops.image.watershed.WatershedSeeded;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.componenttree.Component;
import net.imglib2.img.Img;
import net.imglib2.img.ImgView;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.roi.labeling.LabelRegion;
import net.imglib2.roi.labeling.LabelRegions;
import net.imglib2.type.Type;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.Views;
import org.scijava.Context;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.Iterator;
import java.util.List;


public class RecursiveComponentWatershedder<T extends Type<T>, C extends Component<T, C>> {
    private OpService ops = (new Context()).service(OpService.class);

    public SimpleComponentTree<T, C> recursivelyWatershedComponents(SimpleComponentTree<T, C> tree) {
        for (final SimpleComponent root : tree.roots()) {
            RecursivelyWatershed(root);
        }
        throw new NotImplementedException();
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
            ++label;
            child.writeCenterLabel(childLabeling, label);
        }
        RandomAccessibleInterval<BitType> mask = ops.convert().bit(Views.iterable(parentLabeling.getIndexImg()));
        ImageJFunctions.show((Img) sourceImage);
        ImageJFunctions.show(childLabeling.getIndexImg());
        ImageJFunctions.show(mask);
        ImgLabeling<Integer, IntType> out = doWatershed(sourceImage, childLabeling, mask);
        ImageJFunctions.show(out.getIndexImg());

        printRegionSizes(childLabeling);
        System.out.println("------");
        printRegionSizes(out);

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
        Img<FloatType> input = in.factory().create(in); //  QUESTION: if I use generic Img<t> here, the compiler issues a warning. Why?!
        ops.image().invert(input, in);
        return ops.image().watershed(null, input, markers, false, false, mask);
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
