package com.jug.util.componenttree;

import net.imglib2.*;
import net.imglib2.algorithm.componenttree.Component;
import net.imglib2.img.Img;
import net.imglib2.img.ImgView;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.roi.labeling.*;
import net.imglib2.type.Type;
import net.imglib2.type.logic.NativeBoolType;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.type.numeric.real.FloatType;
import org.apache.commons.lang.NotImplementedException;
import org.springframework.cglib.core.Local;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

public final class SimpleComponent<T extends Type<T>>
        implements
        Component<T, SimpleComponent<T>> {

    private static final ComponentPositionComparator verticalComponentPositionComparator = new ComponentPositionComparator(1);
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
    private final ArrayList<SimpleComponent<T>> children = new ArrayList<>();
    /**
     * Parent node. Is null if this is a root component.
     */
    private SimpleComponent<T> parent;
    private double[] mean;
    private double[] sumPos;
    private final ImgLabeling<Integer, IntType> labeling;
    private final Integer label;
    private LabelRegion<Integer> region;
    private double[] firstMomentPixelCoordinates = null;
    private List<SimpleComponent<T>> componentTreeRoots;

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
     *
     * @param labeling image that is labeled
     * @param label    label that will be set for this component
     */
    public void writeLabels(ImgLabeling<Integer, IntType> labeling, Integer label) {
        // WARNING: THIS METHOD SHOULD DO BOUNDARY CHECKING! IN CASE PIXELS IN PixelList lie outside of labeling!
        RandomAccess<LabelingType<Integer>> accessor = labeling.randomAccess();
        for (Localizable val : pixelList) {
            accessor.setPosition(val);
            accessor.get().add(label);
        }
    }

    /**
     * Labels the center of mass of this component in image labeling with label.
     *
     * @param label label that will be set for this component
     */
    public ImgLabeling<Integer, IntType> getLabeling(Integer label) {
        Img<T> sourceImage = ImgView.wrap(this.getSourceImage(), new ArrayImgFactory(new FloatType()));
        return createLabelingImage(sourceImage);
    }

    private ImgLabeling<Integer, IntType> createLabelingImage(RandomAccessibleInterval<T> sourceImage) {
        long[] dims = new long[sourceImage.numDimensions()];
        sourceImage.dimensions(dims);
        Img<IntType> img = ArrayImgs.ints(dims);
        return new ImgLabeling<>(img);
    }

    /**
     * Labels the center of mass of this component in image labeling with label.
     *
     * @param labeling image that is labeled
     * @param label    label that will be set for this component
     */
    public void writeCenterLabel(ImgLabeling<Integer, IntType> labeling, Integer label) {
        // WARNING: THIS METHOD SHOULD DO BOUNDARY CHECKING! IN CASE PIXELS IN PixelList lie outside of labeling!
        RandomAccess<LabelingType<Integer>> accessor = labeling.randomAccess();
        double[] centerDouble = this.firstMomentPixelCoordinates();
        final int[] centerInt = new int[centerDouble.length];
        for (int i = 0; i < centerInt.length; ++i)
            centerInt[i] = (int) centerDouble[i];
        accessor.setPosition(new Point(centerInt));
        accessor.get().add(label);
    }

    /**
     * Return copy the image from which this component was created.
     *
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

//    public SimpleComponent<T> getSibling() {
//        SimpleComponent<T> parent = this.getParent();
//        if (parent == null) {
//            return null; /* there is no parent component and hence no sibling component */
//        }
//        List<SimpleComponent<T>> children = parent.getChildren();
//        if (children.size() == 1) {
//            return null; /* there is only one child component of this component parent, which will be this component. Hence there is no sibling component. */
//        }
//        if (children.size() > 2) {
//            throw new NotImplementedException("children.size() > 2, but this method requires that there can only exist two child-component.");
//        }
//        children.remove(this);
//        SimpleComponent<T> sibling = children.get(0); /* we assume that there is only one child left here! */
//        return sibling;
//    }

    void setParent(SimpleComponent<T> parent) {
        this.parent = parent;
    }

//    @Override
//    public Iterator<Localizable> iterator() {
//        return new RegionLocalizableIterator(region);
//    }

    @Override
    public List<SimpleComponent<T>> getChildren() {
        return children;
    }

    void addChild(SimpleComponent<T> child) {
        this.children.add(child);
    }

//    public void setRegion(LabelRegion<Integer> region) {
//        LabelRegion<Integer> newRegion = region;
//    }

    @Override
    public Iterator<Localizable> iterator() {
        return pixelList.iterator();
    }

    public LabelRegion<Integer> getRegion() {
        return region;
    }

    public void setRegion(LabelRegion<Integer> region) {
        this.region = region;
        pixelList.clear();
        LabelRegionCursor c = region.cursor();
        while (c.hasNext()) {
            c.fwd();
            pixelList.add(new Point(c));
        }
    }

    public double[] firstMomentPixelCoordinates() {
        if (firstMomentPixelCoordinates != null) return firstMomentPixelCoordinates;

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

    public List<SimpleComponent<T>> getComponentTreeRoots() {
        return componentTreeRoots;
    }

    public void setComponentTreeRoots(List<SimpleComponent<T>> roots) {
        componentTreeRoots = roots;
    }

    /**
     * Returns list of all neighboring nodes below the current node.
     *
     * @return list of neighboring nodes
     */
    public List<SimpleComponent<T>> getLowerNeighbors() {
        if (Objects.isNull(lowerNeighbors)){
            lowerNeighbors = calculateLowerNeighbors();
        }
        return lowerNeighbors;
    }
    List<SimpleComponent<T>> lowerNeighbors;

    /**
     * Calculate list of all neighboring nodes below the current node.
     *
     * @return list of neighboring nodes
     */
    private List<SimpleComponent<T>> calculateLowerNeighbors() {
        final ArrayList<SimpleComponent<T>> neighbors = new ArrayList<>();
        SimpleComponent<T> neighbor = this.getLowerNeighborClosestToRootLevel();
        if (neighbor != null) {
            neighbors.add(neighbor);
            while (neighbor.getChildren().size() > 0) {
                neighbor = neighbor.getChildren().get(0);
                neighbors.add(neighbor);
            }
        }
        return neighbors;
    }

    /**
     * Returns the lower neighbor of {@param node}. The algorithm is written in such a way, that the component that is
     * returned as neighbor, will be the closest to root-level of the component tree.
     *
     * @return the lower neighbor node
     */
    public SimpleComponent<T> getLowerNeighborClosestToRootLevel() {
        if (Objects.isNull(lowerNeighborClosestToRootLevel)) {
            lowerNeighborClosestToRootLevel = calculateLowerNeighborClosestToRootLevel();
        }
        return lowerNeighborClosestToRootLevel;
    }
    SimpleComponent<T> lowerNeighborClosestToRootLevel;

    /**
     * Calculates the lower neighbor of {@param node}. The algorithm is written in such a way, that the component that is
     * returned as neighbor, will be the closest to root-level of the component tree.
     *
     * @return the lower neighbor node
     */
    private SimpleComponent<T> calculateLowerNeighborClosestToRootLevel() {
        final SimpleComponent<T> parentNode = this.getParent();
        if (parentNode != null) { /* {@param node} is child node, so we can get the sibling node below it (if {@param node} is not bottom-most child), which is its lower neighbor */
            final int idx = parentNode.getChildren().indexOf(this);
            if (idx + 1 < parentNode.getChildren().size()) {
                return parentNode.getChildren().get(idx + 1);
            } else { /* {@param node} is bottom-most child node, we therefore need to get bottom neighbor of its parent */
                return parentNode.calculateLowerNeighborClosestToRootLevel();
            }
        } else { /* {@param node} is a root, so we need to find the root below and return it, if it exists*/
            List<SimpleComponent<T>> roots = new ArrayList<>(getComponentTreeRoots());
            roots.sort(verticalComponentPositionComparator);
            final int idx = roots.indexOf(this);
            if (idx + 1 < roots.size()) {
                return roots.get(idx + 1);
            }
        }
        return null;
    }

    /**
     * Returns list of all neighboring nodes above the current node.
     *
     * @return list of neighboring nodes
     */
    public List<SimpleComponent<T>> getUpperNeighbors() {
        if (Objects.isNull(upperNeighbors)){
            upperNeighbors = calculateUpperNeighbors();
        }
        return upperNeighbors;
    }
    List<SimpleComponent<T>> upperNeighbors;

    /**
     * Calculate list of all neighboring nodes above the current node.
     *
     * @return list of neighboring nodes
     */
    private List<SimpleComponent<T>> calculateUpperNeighbors() {
        final ArrayList<SimpleComponent<T>> neighbors = new ArrayList<>();
        SimpleComponent<T> neighbor = this.getUpperNeighborClosestToRootLevel();
        if (neighbor != null) {
            neighbors.add(neighbor);
            while (neighbor.getChildren().size() - 1 >= 0) {
                neighbor = neighbor.getChildren().get(neighbor.getChildren().size() - 1); /* get last item in the list, which is the lowest one */
                neighbors.add(neighbor);
            }
        }
        return neighbors;
    }

    /**
     * Returns the upper neighbor of {@param node}.
     *
     * @return the lower neighbor node
     */
    public SimpleComponent<T> getUpperNeighborClosestToRootLevel() {
        if (Objects.isNull(upperNeighborClosestToRootLevel)) {
            upperNeighborClosestToRootLevel = calculateUpperNeighborClosestToRootLevel();
        }
        return upperNeighborClosestToRootLevel;
    }
    SimpleComponent<T> upperNeighborClosestToRootLevel;

    /**
     * Calculates the upper neighbor of {@param node}. The algorithm is written in such a way, that the component that is
     * returned as neighbor, will be the closest to root-level of the component tree.
     *
     * @return the upper neighbor node
     */
    private SimpleComponent<T> calculateUpperNeighborClosestToRootLevel() {
        final SimpleComponent<T> parentNode = this.getParent();
        if (parentNode != null) { /* {@param node} is child node, so we can get the sibling node below it (if {@param node} is not bottom-most child), which is its lower neighbor */
            final int idx = parentNode.getChildren().indexOf(this);
            if (idx - 1 >= 0) {
                return parentNode.getChildren().get(idx - 1);
            } else { /* {@param node} is bottom-most child node, we therefore need to get bottom neighbor of its parent */
                return parentNode.calculateUpperNeighborClosestToRootLevel();
            }
        } else { /* {@param node} is a root, so we need to find the root below and return it, if it exists*/
            List<SimpleComponent<T>> roots = new ArrayList<>(getComponentTreeRoots());
            roots.sort(verticalComponentPositionComparator);
            final int idx = roots.indexOf(this);
            if (idx - 1 >= 0) {
                return roots.get(idx - 1);
            }
        }
        return null;
    }

    private int totalAreaOfRoots = -1;
    public int getTotalAreaOfRootComponents() {
        if (totalAreaOfRoots < 0) {
            totalAreaOfRoots = calculateTotalAreaOfRootComponents();
        }
        return totalAreaOfRoots;
    }

    private int calculateTotalAreaOfRootComponents() {
        int area = 0;
        for (SimpleComponent<T> root : getComponentTreeRoots()){
            area += root.size();
        }
        return area;
    }

    private int totalComponentAreaAbove = -1;
    public int getTotalAreaOfComponentsAbove() {
        if (totalComponentAreaAbove < 0) {
            totalComponentAreaAbove = calculateTotalAreaOfComponentsAbove();
        }
        return totalComponentAreaAbove;
    }

    private int calculateTotalAreaOfComponentsAbove(){
        SimpleComponent<T> neighbor = this.getUpperNeighborClosestToRootLevel();
        int cellAreaPixels = 0;
        while (neighbor != null) {
            cellAreaPixels += neighbor.size();
            neighbor = neighbor.getUpperNeighborClosestToRootLevel(); /* iterate over neighboring components taking only the one closest to the root-component */
        }
        return cellAreaPixels;
    }

    private int totalComponentAreaBelow = -1;
    public int getTotalAreaOfComponentsBelow() {
        if (totalComponentAreaBelow < 0) {
            totalComponentAreaBelow = calculateTotalAreaOfComponentsBelow();
        }
        return totalComponentAreaBelow;
    }

    private int calculateTotalAreaOfComponentsBelow(){
        List<SimpleComponent<T>> componentsBelow = getComponentsBelowClosestToRoot();
        int totalCellAreaPixels = 0;
        for (SimpleComponent<T> component : componentsBelow) {
            totalCellAreaPixels += component.size();
        }
        return totalCellAreaPixels;
    }

    public int getRankRelativeToComponentsClosestToRoot(){
        List<SimpleComponent<T>> componentsBelow = getComponentsBelowClosestToRoot();
        return componentsBelow.size();
    }

    public List<SimpleComponent<T>> getComponentsBelowClosestToRoot(){
        List<SimpleComponent<T>> result = new ArrayList<>();
        SimpleComponent<T> neighbor = this.getLowerNeighborClosestToRootLevel();
        while (neighbor != null) {
            result.add(neighbor);
            neighbor = neighbor.getLowerNeighborClosestToRootLevel(); /* iterate over neighboring components taking only the one closest to the root-component */
        }
        return result;
    }

    List<T> componentPixelValues = null;

    List<T> getComponentPixelValues() {
        if (componentPixelValues != null) {
            return componentPixelValues;
        }

        componentPixelValues = new ArrayList<>();
        if (this.size() == 0) {
            return componentPixelValues; /* there is no watershed line; return empty array */
        }
        Iterator<Localizable> it = this.iterator();
        while(it.hasNext()) {
            Localizable pos = it.next();
            componentPixelValues.add(this.sourceImage.getAt(pos));
        }
        return componentPixelValues;
    }

    List<T> watershedLinePixelValues = null;

    public List<T> getWatershedLinePixelValues() {
        if (watershedLinePixelValues != null) {
            return watershedLinePixelValues;
        }
        List<Localizable> pixelPositions = this.getWatershedLinePixelPositions();
        watershedLinePixelValues = new ArrayList<>();
        if (pixelPositions.size() == 0) {
            return watershedLinePixelValues; /* there is no watershed line; return empty array */
        }
        Iterator<Localizable> it = watershedLinePixelPositions.iterator();
        while(it.hasNext()){
            Localizable pos = it.next();
            watershedLinePixelValues.add(this.sourceImage.getAt(pos));
        }
        return watershedLinePixelValues;
    }

    List<Localizable> watershedLinePixelPositions = null;

    public List<Localizable> getWatershedLinePixelPositions(){
        List<SimpleComponent<T>> children = this.getChildren();
        if (children.size() <= 1) {
            watershedLinePixelPositions = new ArrayList<>(); /* there exist zero or one child component and hence no watershed line. */
            return watershedLinePixelPositions;
        }
//        if (children.size() > 2) {
//            throw new NotImplementedException("children.size() > 2, but this method requires that there can only exist two child-component.");
//        }

        if(watershedLinePixelPositions == null){
            watershedLinePixelPositions = getWatershedLineInternal(this, children);
        }
        return watershedLinePixelPositions;
    }

    private List<Localizable> getWatershedLineInternal(SimpleComponent<T> parent, List<SimpleComponent<T>> children) {
        List<Localizable> watershedLinePositions = new ArrayList<>();
        Img<NativeBoolType> tmpImage = createImage(this.getSourceImage());
        RandomAccess<NativeBoolType> rndAccess = tmpImage.randomAccess();
        for (SimpleComponent<T> child : children) {
            for (Iterator<Localizable> it = child.iterator(); it.hasNext(); ) {
                rndAccess.setPosition(it.next());
                rndAccess.get().set(true);
            }
        }
        for (Iterator<Localizable> it = parent.iterator(); it.hasNext(); ) {
            Localizable loc = it.next();
            rndAccess.setPosition(loc);
            if (!rndAccess.get().get()) {
                watershedLinePositions.add(loc);
            }
        }
        return watershedLinePositions;
    }

    public Img<NativeBoolType> createImage(RandomAccessibleInterval sourceImage) {
        long[] dims = new long[sourceImage.numDimensions()];
        sourceImage.dimensions(dims);
        Img<NativeBoolType> img = ArrayImgs.booleans(dims);
        return img;
    }

    private class RegionLocalizableIterator implements Iterator<Localizable> {
        Cursor<Void> c;
        private final LabelRegion<?> region;

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
