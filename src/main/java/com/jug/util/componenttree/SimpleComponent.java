package com.jug.util.componenttree;

import net.imglib2.*;
import net.imglib2.algorithm.componenttree.Component;
import net.imglib2.img.Img;
import net.imglib2.img.ImgView;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.roi.labeling.*;
import net.imglib2.type.Type;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.type.numeric.real.FloatType;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

public final class SimpleComponent<T extends Type<T>>
        implements
        Component<T, SimpleComponent<T>> {

    /**
     * Pixels in the component.
     */
    private final ArrayList<Localizable> pixelList = new ArrayList<>();
    private final RandomAccessibleInterval<T> sourceImage;
    /**
     * Maximum threshold value of the connected component.
     */
    private final T value;
    /**
     * List of child nodes.
     */
    private ArrayList<SimpleComponent<T>> children = new ArrayList<>();
    /**
     * Parent node. Is null if this is a root component.
     */
    private SimpleComponent<T> parent;
    private double[] mean;
    private double[] sumPos;
    private ImgLabeling<Integer, IntType> labeling;
    private Integer label;
    private LabelRegion<Integer> region;

    /**
     * Constructor for fully connected component-node (with parent or children).
     */
    public <C extends Component<T, C>> SimpleComponent(ImgLabeling<Integer, IntType> labeling, Integer label, C wrappedComponent, RandomAccessibleInterval<T> sourceImage) {
        this.labeling = labeling;
        this.label = label;
        RandomAccess<LabelingType<Integer>> accessor = this.labeling.randomAccess();
        for (Localizable val : wrappedComponent) {
            pixelList.add(new Point(val));
            accessor.setPosition(val);
            accessor.get().add(label);
        }
        this.value = wrappedComponent.value();
        this.sourceImage = sourceImage;
        LabelRegions<Integer> regions = new LabelRegions<>(labeling);
        this.region = regions.getLabelRegion(this.label);
    }

    /**
     * Labels the corresponding pixels in image labeling with label.
     * @param labeling image that is labeled
     * @param label label that will be set for this component
     */
    public void writeLabels(ImgLabeling<Integer, IntType> labeling, Integer label){
        // WARNING: THIS METHOD SHOULD DO BOUNDARY CHECKING! IN CASE PIXELS IN PixelList lie outside of labeling!
        RandomAccess<LabelingType<Integer>> accessor = labeling.randomAccess();
        for (Localizable val : pixelList) {
            accessor.setPosition(val);
            accessor.get().add(label);
        }
    }


    /**
     * Labels the center of mass of this component in image labeling with label.
     * @param label label that will be set for this component
     */
    public ImgLabeling<Integer, IntType> getLabeling(Integer label) {
        Img<T> sourceImage = ImgView.wrap(this.getSourceImage(), new ArrayImgFactory(new FloatType()));
        return createLabelingImage(sourceImage);
    }

    private ImgLabeling<Integer, IntType> createLabelingImage(RandomAccessibleInterval<T> sourceImage){
        long[] dims = new long[sourceImage.numDimensions()];
        sourceImage.dimensions(dims);
        Img<IntType> img = ArrayImgs.ints(dims);
        return new ImgLabeling<>(img);
    }

    /**
     * Labels the center of mass of this component in image labeling with label.
     * @param labeling image that is labeled
     * @param label label that will be set for this component
     */
    public void writeCenterLabel(ImgLabeling<Integer, IntType> labeling, Integer label){
        // WARNING: THIS METHOD SHOULD DO BOUNDARY CHECKING! IN CASE PIXELS IN PixelList lie outside of labeling!
        RandomAccess<LabelingType<Integer>> accessor = labeling.randomAccess();
        double[] centerDouble = this.firstMomentPixelCoordinates();
        final int[] centerInt = new int[centerDouble.length];
        for (int i=0; i<centerInt.length; ++i)
            centerInt[i] = (int) centerDouble[i];
        accessor.setPosition(new Point(centerInt));
        accessor.get().add(label);
    }

    /**
     * Return copy the image from which this component was created.
     * @return copy of the image
     */
    public RandomAccessibleInterval<T> getSourceImage() {
        return ImgView.wrap(sourceImage, new ArrayImgFactory(new FloatType())).copy();
    }

    @Override
    public long size() {
        return pixelList.size();
    }

    @Override
    public T value() {
        return value;
    }

    @Override
    public SimpleComponent<T> getParent() {
        return parent;
    }

    void setParent(SimpleComponent<T> parent) {
        this.parent = parent;
    }

    @Override
    public List<SimpleComponent<T>> getChildren() {
        return children;
    }

    void addChild(SimpleComponent<T> child) {
        this.children.add(child);
    }

    @Override
    public Iterator<Localizable> iterator() {
        return pixelList.iterator();
    }

    /**
     * Returns an iterator to localizables, which are sorted with to the passed
     * comparator.
     */
    public Iterator<Localizable> sortedIterator(Comparator<Localizable> comparator){
        ArrayList<Localizable> myPixelList = (ArrayList<Localizable>) pixelList.clone();
        myPixelList.sort(comparator);
        return myPixelList.iterator();
    }

//    @Override
//    public Iterator<Localizable> iterator() {
//        return new RegionLocalizableIterator(region);
//    }

    public void setRegion(LabelRegion<Integer> region) {
        this.region = region;
        LabelRegionCursor c = region.cursor();
        while (c.hasNext()) {
            c.fwd();
            pixelList.add(new Point(c));
        }
    }

    public LabelRegion<Integer> getRegion(){
        return region;
    }

//    public void setRegion(LabelRegion<Integer> region) {
//        LabelRegion<Integer> newRegion = region;
//    }


    private double[] firstMomentPixelCoordinates = null;
    public double[] firstMomentPixelCoordinates() {
        if(firstMomentPixelCoordinates != null) return firstMomentPixelCoordinates;

        int n = pixelList.get(0).numDimensions();
        sumPos = new double[n];
        for (Localizable val : this) {
            for (int i = 0; i < n; ++i)
                sumPos[i] += val.getIntPosition(i);
        }

        mean = new double[n];
        for (int i = 0; i < n; ++i)
            mean[i] = sumPos[i] / size();
        firstMomentPixelCoordinates = mean;
        return mean;
    }

    public int getNodeLevel() {
        int nodeLevel = 0;
        SimpleComponent<T> parent = this.getParent();
        while (parent != null) {
            nodeLevel++;
            parent = parent.getParent();
        }
        return nodeLevel;
    }

    private class RegionLocalizableIterator implements Iterator<Localizable> {
        Cursor<Void> c;
        private LabelRegion<?> region;

        public RegionLocalizableIterator(LabelRegion<?> region) {
            this.region = region;
            c = region.cursor();
        }

        @Override
        public boolean hasNext() {
            return c.hasNext();
        }

        @Override
        public Localizable next() {
            c.fwd();
            return c;
        }
    }
}
