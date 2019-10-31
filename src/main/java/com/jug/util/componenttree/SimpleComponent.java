package com.jug.util.componenttree;

import net.imglib2.*;
import net.imglib2.algorithm.componenttree.Component;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.roi.labeling.LabelRegion;
import net.imglib2.roi.labeling.LabelRegions;
import net.imglib2.roi.labeling.LabelingType;
import net.imglib2.type.Type;
import net.imglib2.type.numeric.integer.IntType;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public final class SimpleComponent<T extends Type<T>>
        implements
        Component<T, SimpleComponent<T>> {

    /**
     * Pixels in the component.
     */
    private final List<Localizable> pixelList = new ArrayList<>();
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
        region = regions.getLabelRegion(this.label);
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

    public RandomAccessibleInterval<T> getSourceImage() {
        return sourceImage;
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

//    @Override
//    public Iterator<Localizable> iterator() {
//        return pixelList.iterator();
//    }

    @Override
    public Iterator<Localizable> iterator() {
//        return region.iterator();
        return new RegionLocalizableIterator(region);
    }

    public double[] firstMomentPixelCoordinates() {
        int n = pixelList.get(0).numDimensions();
        sumPos = new double[n];
        for (Localizable val : this) {
            for (int i = 0; i < n; ++i)
                sumPos[i] += val.getIntPosition(i);
        }

        mean = new double[n];
        for (int i = 0; i < n; ++i)
            mean[i] = sumPos[i] / size();
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
