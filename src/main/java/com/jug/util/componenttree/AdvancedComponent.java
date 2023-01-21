package com.jug.util.componenttree;

import com.jug.datahandling.IImageProvider;
import com.jug.util.ComponentTreeUtils;
import com.jug.util.math.Vector2DPolyline;
import net.imglib2.RandomAccess;
import net.imglib2.*;
import net.imglib2.algorithm.componenttree.Component;
import net.imglib2.algorithm.morphology.Dilation;
import net.imglib2.algorithm.morphology.Erosion;
import net.imglib2.algorithm.neighborhood.RectangleShape;
import net.imglib2.img.Img;
import net.imglib2.img.ImgView;
import net.imglib2.img.array.ArrayImg;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.img.basictypeaccess.array.IntArray;
import net.imglib2.roi.MaskInterval;
import net.imglib2.roi.Masks;
import net.imglib2.roi.labeling.*;
import net.imglib2.type.NativeType;
import net.imglib2.type.Type;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.logic.NativeBoolType;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Pair;
import net.imglib2.util.ValuePair;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.*;
import java.util.stream.Collectors;

import static com.jug.util.ComponentTreeUtils.*;
import static java.util.Objects.isNull;

public class AdvancedComponent<T extends Type<T>> implements ComponentInterface<T, AdvancedComponent<T>> {

    private static final ComponentPositionComparator verticalComponentPositionComparator = new ComponentPositionComparator(1);
    /**
     * Pixels in the component.
     */
    private List<LocalizableImpl> pixelList;
    private final RandomAccessibleInterval<T> sourceImage;
    /**
     * Maximum threshold value of the connected component.
     */
    private final T value;
    /**
     * List of child nodes.
     */
    private final ArrayList<AdvancedComponent<T>> children = new ArrayList<>();
    private final ComponentProperties componentProperties;
    /**
     * Parent node. Is null if this is a root component.
     */
    private AdvancedComponent<T> parent;
    private double[] mean;
    private double[] sumPos;
    private final Integer label;
    private LabelRegion<Integer> region;
    private double[] firstMomentPixelCoordinates = null;
    private List<AdvancedComponent<T>> componentTreeRoots;

    private IImageProvider imageProvider;

    /**
     * Constructor for fully connected component-node (with parent or children).
     */
    public <C extends Component<T, C>> AdvancedComponent(Integer label,
                                                         C wrappedComponent,
                                                         RandomAccessibleInterval<T> sourceImage,
                                                         ComponentProperties componentProperties,
                                                         int frameNumber,
                                                         IImageProvider imageProvider) {
        this.value = wrappedComponent.value();
        this.sourceImage = sourceImage;
        this.componentProperties = componentProperties;
        this.label = label;
        this.frameNumber = frameNumber;
        this.imageProvider = imageProvider;
        copyPixelPositions(wrappedComponent);
        buildLabelRegion(pixelList, label, sourceImage);
    }

    private void buildLabelRegion(List<LocalizableImpl> pixelList, Integer label, RandomAccessibleInterval<T> sourceImage) {
        ImgLabeling<Integer, IntType> labeling = getLabelingImage(sourceImage);
        RandomAccess<LabelingType<Integer>> accessor = labeling.randomAccess();
        for (Localizable val : pixelList) {
            accessor.setPosition(val);
            accessor.get().add(label);
        }
        LabelRegions<Integer> regions = new LabelRegions<>(labeling);
        this.region = regions.getLabelRegion(this.label);
    }

    private void copyPixelPositions(Iterable<Localizable> wrappedComponent) {
        pixelList = new ArrayList<>();
        for (Localizable val : wrappedComponent) {
            LocalizableImpl myLocalizable = new LocalizableImpl(val);
            pixelList.add(myLocalizable); /* MM-20220920: We need to create copies of the Localizable to added to pixelList, because it is modified in the loop */
        }
    }

    @NotNull
    private ImgLabeling<Integer, IntType> getLabelingImage(RandomAccessibleInterval<T> sourceImage) {
        long[] dims = new long[sourceImage.numDimensions()];
        sourceImage.dimensions(dims);
        ArrayImg<IntType, IntArray> img = ArrayImgs.ints(dims);
        ImgLabeling<Integer, IntType> labeling = new ImgLabeling<>(img);
        return labeling;
    }

    /**
     * Labels the corresponding pixels in image labeling with label.
     *
     * @param labeling image that is labeled
     * @param label    label that will be set for this component
     */
    public void writeLabels(ImgLabeling<Integer, IntType> labeling, Integer label) {
        // WARNING: THIS METHOD SHOULD DO BOUNDARY CHECKING! IN CASE PIXELS IN PixelList lie outside labeling!
        RandomAccess<LabelingType<Integer>> accessor = labeling.randomAccess();
        for (Localizable val : pixelList) {
            accessor.setPosition(val);
            accessor.get().add(label);
        }
    }

//    /**
//     * Labels the center of mass of this component in image labeling with label.
//     *
//     * @param label label that will be set for this component
//     */
//    public ImgLabeling<Integer, IntType> getLabeling(Integer label) {
//        Img<T> sourceImage = ImgView.wrap(this.getSourceImage(), new ArrayImgFactory(new FloatType()));
//        return createLabelingImage(sourceImage);
//    }

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
    public RandomAccessibleInterval<FloatType> getSourceImage() {
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
    public AdvancedComponent<T> getParent() {
        return parent;
    }

    String parentStringId;

    List<String> childStringIds;

    public List<String> getChildrenStringIds() {
        if (isNull(childStringIds)) {
            childStringIds = new ArrayList<>();
            if (!getChildren().isEmpty()) {
                for (AdvancedComponent<T> child : getChildren()) {
                    childStringIds.add(child.getStringId());
                }
            }
        }
        return childStringIds;
    }

    public String getParentStringId() {
        if (isNull(parentStringId)) {
            if (isNull(getParent())) {
                parentStringId = "NA"; /* this component is root-component so return "NA" for not available */
            } else {
                parentStringId = getParent().getStringId();
            }
        }
        return parentStringId;
    }

    public Integer getLabel() {
        return label;
    }

    private AdvancedComponent<T> root;

    public AdvancedComponent<T> getRoot() {
        if (getParent() == null) {
            return null; /* this is the root component and hence it has no corresponding root */
        }
        if (root == null) {
            root = this.getParent();
            while (root.getParent() != null) {
                root = root.getParent();
            }
        }
        return root;
    }

    public List<AdvancedComponent<T>> getCompatibleChildNodes() {
        ArrayList<AdvancedComponent<T>> ret = new ArrayList<>();
        AdvancedComponent<T> siblingBranch = this.getSibling();
        AdvancedComponent<T> targetBranch = this;
        if (siblingBranch == null) {
            return ret;
        }
        while (siblingBranch != null) {
            ret.add(siblingBranch);
            targetBranch = targetBranch.getParent();
            siblingBranch = targetBranch.getSibling();
        }
        return ret;
    }

    public AdvancedComponent<T> getSibling() {
        AdvancedComponent<T> parent = this.getParent();
        if (parent == null) {
            return null; /* there is no parent component and hence no sibling component */
        }
        List<AdvancedComponent<T>> children = parent.getChildren();
        if (children.size() == 1) {
            throw new RuntimeException("children.size() == 1: the target component does not have a sibling. This should not happen and is an error.");
//            return null; /* there is only one child component of this component parent, which will be this component. Hence there is no sibling component. */
        }
        if (children.size() > 2) {
            throw new RuntimeException("children.size() > 2, but this method requires that there can only exist two child-component.");
        }
        children.remove(this);
        AdvancedComponent<T> sibling = children.get(0); /* we assume that there is only one child left here! */
        return sibling;
    }

    void setParent(AdvancedComponent<T> parent) {
        this.parent = parent;
    }

//    @Override
//    public Iterator<Localizable> iterator() {
//        return new RegionLocalizableIterator(region);
//    }

    @Override
    public List<AdvancedComponent<T>> getChildren() {
        return children;
    }

    void addChild(AdvancedComponent<T> child) {
        this.children.add(child);
        if (children.size() > 2) {
//            throw new RuntimeException("component" + getStringId() + " has >2 child-nodes.");
        }
    }

//    public void setRegion(LabelRegion<Integer> region) {
//        LabelRegion<Integer> newRegion = region;
//    }

    @Override
    public Iterator<Localizable> iterator() {
        List<Localizable> tmp = new ArrayList<>();
        for (Localizable loc : pixelList) {
            tmp.add(loc);
        }
        return tmp.iterator();
    }

    @Override
    public LabelRegion<Integer> getRegion() {
        return region;
    }

    public void setRegion(LabelRegion<Integer> region) {
        this.region = region;
        pixelList.clear();
        LabelRegionCursor c = region.cursor();
        while (c.hasNext()) {
            c.fwd();
            pixelList.add(new LocalizableImpl(c));
        }
    }

    double majorAxisLength = -1;
    double minorAxisLength = -1;

    public double getMajorAxisLength() {
        if (majorAxisLength > 0) {
            return majorAxisLength;
        }
        ValuePair<Double, Double> minorAndMajorAxis = componentProperties.getMinorMajorAxis(this);
        minorAxisLength = minorAndMajorAxis.getA();
        majorAxisLength = minorAndMajorAxis.getB();
        return majorAxisLength;
    }

    public double getMinorAxisLength() {
        if (minorAxisLength > 0) {
            return minorAxisLength;
        }
        ValuePair<Double, Double> minorAndMajorAxis = componentProperties.getMinorMajorAxis(this);
        minorAxisLength = minorAndMajorAxis.getA();
        majorAxisLength = minorAndMajorAxis.getB();
        return minorAxisLength;
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
        AdvancedComponent<T> parent = this.getParent();
        while (parent != null) {
            nodeLevel++;
            parent = parent.getParent();
        }
        return nodeLevel;
    }

    public List<AdvancedComponent<T>> getComponentTreeRoots() {
        return componentTreeRoots;
    }

    private ValuePair<Integer, Integer> verticalComponentLimits;

    public ValuePair<Integer, Integer> getVerticalComponentLimits() {
        if (verticalComponentLimits == null)
            verticalComponentLimits = ComponentTreeUtils.getComponentPixelLimits(this, 1);
        return verticalComponentLimits;
    }

    private ValuePair<Integer, Integer> horizontalComponentLimits;

    public ValuePair<Integer, Integer> getHorizontalComponentLimits() {
        if (horizontalComponentLimits == null)
            horizontalComponentLimits = ComponentTreeUtils.getComponentPixelLimits(this, 0);
        return horizontalComponentLimits;
    }

    private int frameNumber;

    public int getFrameNumber() {
        return frameNumber;
    }

    String stringId;

    public String getStringId() {
        if (isNull(stringId)) {
            frameNumber = getFrameNumber();
            stringId = "HypT" + getFrameNumber() + "T" + getVerticalComponentLimits().getA() + "B" + getVerticalComponentLimits().getB() + "L" + getHorizontalComponentLimits().getA() + "R" + getHorizontalComponentLimits().getB() + "H" + hashCode();
        }
        return stringId;
    }

    Map<Integer, Double> maskIntensitiesStd = new HashMap<>();

    @Override
    public double getMaskIntensitiesStd(int channelNumber) {
        Double intensityStd = maskIntensitiesStd.get(channelNumber);
        if (isNull(intensityStd)) {
            final IntervalView<FloatType> channelFrame = Views.hyperSlice(imageProvider.getRawChannelImgs().get(channelNumber), 2, frameNumber);
            intensityStd = componentProperties.getIntensityStd(this, channelFrame);
            maskIntensitiesStd.put(channelNumber, intensityStd);
        }
        return intensityStd;
    }

    @Override
    public double getMaskIntensityMean(int channelNumber) {
        return getMaskIntensityTotal(channelNumber) / size();
    }

    Map<Integer, Double> maskIntensities = new HashMap<>();

    @Override
    public double getMaskIntensityTotal(int channelNumber) {
        Double intensity = maskIntensities.get(channelNumber);
        if (isNull(intensity)) {
            final IntervalView<FloatType> channelFrame = Views.hyperSlice(imageProvider.getChannelImg(channelNumber), 2, frameNumber);
            intensity = componentProperties.getIntensityTotal(this, channelFrame);
            maskIntensities.put(channelNumber, intensity);
        }
        return intensity;
    }


    Map<Integer, Double> backgroundIntensities = new HashMap<>();

    @Override
    public double getBackgroundIntensityTotal(int channelNumber) {
        Double intensity = backgroundIntensities.get(channelNumber);
        if (isNull(intensity)) {
            final IntervalView<FloatType> channelFrame = Views.hyperSlice(imageProvider.getChannelImg(channelNumber), 2, frameNumber);
            intensity = componentProperties.getBackgroundIntensityTotal(this, channelFrame);
            backgroundIntensities.put(channelNumber, intensity);
        }
        return intensity;
    }

    @Override
    public long getBackgroundRoiSize() {
        int channelNumber = 0;
        final IntervalView<FloatType> channelFrame = Views.hyperSlice(imageProvider.getChannelImg(channelNumber), 2, frameNumber);
        return componentProperties.getBackgroundArea(this, channelFrame);
    }

    Map<Integer, Double> backgroundIntensitiesStd = new HashMap<>();

    public double getBackgroundIntensityStd(int channelNumber) {
        Double intensity = backgroundIntensitiesStd.get(channelNumber);
        if (isNull(intensity)) {
            final IntervalView<FloatType> channelFrame = Views.hyperSlice(imageProvider.getChannelImg(channelNumber), 2, frameNumber);
            intensity = componentProperties.getBackgroundIntensityStd(this, channelFrame);
            backgroundIntensitiesStd.put(channelNumber, intensity);
        }
        return intensity;
    }

    public void setComponentTreeRoots(List<AdvancedComponent<T>> roots) {
        componentTreeRoots = roots;
    }

    /**
     * Returns list of all neighboring nodes below the current node.
     *
     * @return list of neighboring nodes
     */
    public List<AdvancedComponent<T>> getLowerNeighbors() {
        if (isNull(lowerNeighbors)) {
            lowerNeighbors = calculateLowerNeighbors();
        }
        return lowerNeighbors;
    }

    List<AdvancedComponent<T>> lowerNeighbors;

    /**
     * Calculate list of all neighboring nodes below the current node.
     *
     * @return list of neighboring nodes
     */
    private List<AdvancedComponent<T>> calculateLowerNeighbors() {
        final ArrayList<AdvancedComponent<T>> neighbors = new ArrayList<>();
        AdvancedComponent<T> neighbor = this.getLowerNeighborClosestToRootLevel();
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
    public AdvancedComponent<T> getLowerNeighborClosestToRootLevel() {
        if (isNull(lowerNeighborClosestToRootLevel)) {
            lowerNeighborClosestToRootLevel = calculateLowerNeighborClosestToRootLevel();
        }
        return lowerNeighborClosestToRootLevel;
    }

    AdvancedComponent<T> lowerNeighborClosestToRootLevel;

    /**
     * Calculates the lower neighbor of {@param node}. The algorithm is written in such a way, that the component that is
     * returned as neighbor, will be the closest to root-level of the component tree.
     *
     * @return the lower neighbor node
     */
    private AdvancedComponent<T> calculateLowerNeighborClosestToRootLevel() {
        final AdvancedComponent<T> parentNode = this.getParent();
        if (parentNode != null) { /* {@param node} is child node, so we can get the sibling node below it (if {@param node} is not bottom-most child), which is its lower neighbor */
            final int idx = parentNode.getChildren().indexOf(this);
            if (idx + 1 < parentNode.getChildren().size()) {
                return parentNode.getChildren().get(idx + 1);
            } else { /* {@param node} is bottom-most child node, we therefore need to get bottom neighbor of its parent */
                return parentNode.calculateLowerNeighborClosestToRootLevel();
            }
        } else { /* {@param node} is a root, so we need to find the root below and return it, if it exists*/
            List<AdvancedComponent<T>> roots = new ArrayList<>(getComponentTreeRoots());
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
    public List<AdvancedComponent<T>> getUpperNeighbors() {
        if (isNull(upperNeighbors)) {
            upperNeighbors = calculateUpperNeighbors();
        }
        return upperNeighbors;
    }

    List<AdvancedComponent<T>> upperNeighbors;

    /**
     * Calculate list of all neighboring nodes above the current node.
     *
     * @return list of neighboring nodes
     */
    private List<AdvancedComponent<T>> calculateUpperNeighbors() {
        final ArrayList<AdvancedComponent<T>> neighbors = new ArrayList<>();
        AdvancedComponent<T> neighbor = this.getUpperNeighborClosestToRootLevel();
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
    public AdvancedComponent<T> getUpperNeighborClosestToRootLevel() {
        if (isNull(upperNeighborClosestToRootLevel)) {
            upperNeighborClosestToRootLevel = calculateUpperNeighborClosestToRootLevel();
        }
        return upperNeighborClosestToRootLevel;
    }

    AdvancedComponent<T> upperNeighborClosestToRootLevel;

    /**
     * Calculates the upper neighbor of {@param node}. The algorithm is written in such a way, that the component that is
     * returned as neighbor, will be the closest to root-level of the component tree.
     *
     * @return the upper neighbor node
     */
    private AdvancedComponent<T> calculateUpperNeighborClosestToRootLevel() {
        final AdvancedComponent<T> parentNode = this.getParent();
        if (parentNode != null) { /* {@param node} is child node, so we can get the sibling node below it (if {@param node} is not bottom-most child), which is its lower neighbor */
            final int idx = parentNode.getChildren().indexOf(this);
            if (idx - 1 >= 0) {
                return parentNode.getChildren().get(idx - 1);
            } else { /* {@param node} is bottom-most child node, we therefore need to get bottom neighbor of its parent */
                return parentNode.calculateUpperNeighborClosestToRootLevel();
            }
        } else { /* {@param node} is a root, so we need to find the root below and return it, if it exists*/
            List<AdvancedComponent<T>> roots = new ArrayList<>(getComponentTreeRoots());
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
        for (AdvancedComponent<T> root : getComponentTreeRoots()) {
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

    private int calculateTotalAreaOfComponentsAbove() {
        AdvancedComponent<T> neighbor = this.getUpperNeighborClosestToRootLevel();
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

    private int calculateTotalAreaOfComponentsBelow() {
        List<AdvancedComponent<T>> componentsBelow = getComponentsBelowClosestToRoot();
        int totalCellAreaPixels = 0;
        for (AdvancedComponent<T> component : componentsBelow) {
            totalCellAreaPixels += component.size();
        }
        return totalCellAreaPixels;
    }

    public int getRankRelativeToComponentsClosestToRoot() {
        List<AdvancedComponent<T>> componentsBelow = getComponentsBelowClosestToRoot();
        return componentsBelow.size();
    }

    public List<AdvancedComponent<T>> getComponentsBelowClosestToRoot() {
        List<AdvancedComponent<T>> result = new ArrayList<>();
        AdvancedComponent<T> neighbor = this.getLowerNeighborClosestToRootLevel();
        while (neighbor != null) {
            result.add(neighbor);
            neighbor = neighbor.getLowerNeighborClosestToRootLevel(); /* iterate over neighboring components taking only the one closest to the root-component */
        }
        return result;
    }

    public List<AdvancedComponent<T>> getAllComponentsBelow() {
        List<AdvancedComponent<T>> result = new ArrayList<>();
        List<AdvancedComponent<T>> componentsOfInterest = getComponentsBelowClosestToRoot();
        result.addAll(componentsOfInterest);
        for (AdvancedComponent<T> component : componentsOfInterest) {
            recursivelyAddChildrenToList(component, result);
        }
        return result;
    }

    public double getOrdinalValue() {
        if (children.size() == 0) { /* this is a leaf component; so calculate its ordinal based on the number of leafs below */
            return Math.pow(2, getRankRelativeToLeafComponent());
        } else if (children.size() > 0) {
            double ordinal = 0;
            for (AdvancedComponent<T> child : children){
                ordinal += child.getOrdinalValue(); /* this is not a leaf-component; its ordinal value is the sum of ordinals below it */
            }
            return ordinal;
        }
        throw new RuntimeException("ERROR: This value should never be reached, because each node should either be a leaf or not.");
    }

    public double getRankRelativeToLeafComponent() {
        return getLeafComponentsBelow().size();
    }

    List<AdvancedComponent<T>> listOfLeafsBelow;

    private List<AdvancedComponent<T>> getLeafComponentsBelow() {
        if (isNull(listOfLeafsBelow)) {
            listOfLeafsBelow = calculateListOfLeafsBelow();
        }
        return listOfLeafsBelow;
    }

    private List<AdvancedComponent<T>> calculateListOfLeafsBelow() {
        ArrayList<AdvancedComponent<T>> listOfLeafsBelow = new ArrayList<>();
        List<AdvancedComponent<T>> componentsBelow = getAllComponentsBelow();
        for(AdvancedComponent<T> component : componentsBelow){
            if(component.children.size() == 0){
                listOfLeafsBelow.add(component);
            }
        }
        return listOfLeafsBelow;
    }

    double pixelValueAverage = 0;

    public double getPixelValueAverage() {
        if (!(pixelValueAverage < 0.001)) {
            return pixelValueAverage;
        }
        pixelValueAverage = calculateAverageOrReturnDefault((List<FloatType>) getComponentPixelValues(), Double.MIN_VALUE);
        return pixelValueAverage;
    }

    double pixelValueTotal = -1;

    public double getPixelValueTotal() {
        if (pixelValueTotal > 0) {
            return pixelValueTotal;
        }
        pixelValueTotal = calculateSum((List<FloatType>) getComponentPixelValues());
        return pixelValueTotal;
    }

    double convexHullArea = -1;

    public double getConvexHullArea() {
        if (convexHullArea > 0) {
            return convexHullArea;
        }
        convexHullArea = componentProperties.getConvexHullArea(this);
        return convexHullArea;
    }

    List<T> componentPixelValues = null;

    public List<Double> getComponentPixelValuesAsDouble() {
        List<Double> probabilities = ((AdvancedComponent<FloatType>) this).getComponentPixelValues().stream().map(value -> value.getRealDouble()).collect(Collectors.toList());
        return probabilities;
    }

    public Pair<Double, Double> getPixelValueExtremaInsideRange(double rangeMin, double rangeMax) {
        double minRet = Double.MAX_VALUE;
        double maxRet = -Double.MAX_VALUE;
        List<Double> pixelValues = this.getComponentPixelValuesAsDouble();
        for (Double val : pixelValues) {
            if (val > rangeMin && val < minRet) minRet = val;
            if (val < rangeMax && val > maxRet) maxRet = val;
        }
        return new ValuePair<>(minRet, maxRet);
    }

    public List<T> getComponentPixelValues() {
        if (componentPixelValues != null) {
            return componentPixelValues;
        }

        componentPixelValues = new ArrayList<>();
        if (this.size() == 0) {
            return componentPixelValues; /* there is no watershed line; return empty array */
        }
        Iterator<Localizable> it = this.iterator();
        while (it.hasNext()) {
            Localizable pos = it.next();
            componentPixelValues.add(this.sourceImage.getAt(pos));
        }
        return componentPixelValues;
    }

    Double watershedLinePixelValueAverage = Double.MIN_VALUE;

    /**
     * Return the average value of the pixels of the watershed line. Returns Null if there is no watershed line.
     *
     * @return
     */
    public Double getWatershedLinePixelValueAverage() {
        if (watershedLinePixelValueAverage == null) {
            return watershedLinePixelValueAverage;
        }
        if (!(Math.abs(watershedLinePixelValueAverage - Double.MIN_VALUE) < 0.001)) {
            return watershedLinePixelValueAverage;
        }

        List<FloatType> vals = (List<FloatType>) this.getWatershedLinePixelValues();
        if (vals.size() == 0) {
            watershedLinePixelValueAverage = null;
            return watershedLinePixelValueAverage;
        }
        watershedLinePixelValueAverage = calculateAverageOrReturnDefault(vals, 1.0);
        return watershedLinePixelValueAverage;
    }

    private Double calculateAverageOrReturnDefault(List<FloatType> listOfValues, Double defaultValue) {
        return listOfValues.stream()
                .map(d -> d.getRealDouble())
                .mapToDouble(d -> d)
                .average()
                .orElse(defaultValue);
    }

    private Double calculateSum(List<FloatType> listOfValues) {
        return listOfValues.stream()
                .map(d -> d.getRealDouble())
                .mapToDouble(d -> d)
                .sum();
    }

    public List<Double> getWatershedLinePixelValuesAsDoubles() {
        return ((AdvancedComponent<FloatType>) this).getWatershedLinePixelValues().stream().map(value -> value.getRealDouble()).collect(Collectors.toList());
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
        while (it.hasNext()) {
            Localizable pos = it.next();
            watershedLinePixelValues.add(this.sourceImage.getAt(pos));
        }
        return watershedLinePixelValues;
    }

    List<Localizable> watershedLinePixelPositions = null;

    /**
     * Returns the pixels of the watershed-line that splits this component into its children.
     *
     * @return
     */
    public List<Localizable> getWatershedLinePixelPositions() {
        List<AdvancedComponent<T>> children = this.getChildren();
        if (children.size() <= 1) {
            watershedLinePixelPositions = new ArrayList<>(); /* there exist zero or one child component and hence no watershed line. */
            return watershedLinePixelPositions;
        }
//        if (children.size() > 2) {
//            throw new NotImplementedException("children.size() > 2, but this method requires that there can only exist two child-component.");
//        }

        if (watershedLinePixelPositions == null) {
            watershedLinePixelPositions = getWatershedLineInternal(this, children);
        }
        return watershedLinePixelPositions;
    }

    /**
     * Calculate the pixels in the parent component, which are *not* in the child-components.
     *
     * @param parent
     * @param children
     * @return
     */
    private List<Localizable> getWatershedLineInternal(AdvancedComponent<T> parent, List<AdvancedComponent<T>> children) {
        List<Localizable> watershedLinePositions = new ArrayList<>();
        Img<NativeBoolType> tmpImage = createImage(this.getSourceImage());
        RandomAccess<NativeBoolType> rndAccess = tmpImage.randomAccess();
        for (AdvancedComponent<T> child : children) {
            for (Iterator<Localizable> it = child.iterator(); it.hasNext(); ) {
                rndAccess.setPosition(it.next());
                rndAccess.get().set(true);
            }
        }
        for (Iterator<Localizable> it = parent.iterator(); it.hasNext(); ) {
            Localizable loc = it.next();
            rndAccess.setPosition(loc);
            if (!rndAccess.get().get()) {
                watershedLinePositions.add(loc); /* add pixels that are in parent component but not in child components; this could be much more easily be done with a set-operation */
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

    /**
     * Returns an image of the component with values set to {@param pixelValue}.
     *
     * @param pixelValue
     * @param <T>
     * @return
     */
    public <T extends NativeType<T>> Img<T> getComponentImage(T pixelValue) {
        Img<T> img = createImageWithSameDimension(pixelValue);
        RandomAccess<T> rndAccess = img.randomAccess();
        for (Iterator<Localizable> it = this.iterator(); it.hasNext(); ) {
            rndAccess.setPosition(it.next());
            rndAccess.get().set(pixelValue);
        }
        return img;
    }

    public <T extends NativeType<T>> Img<T> createImageWithSameDimension(T type) {
        long[] dims = new long[sourceImage.numDimensions()];
        sourceImage.dimensions(dims);
        Img<T> img = new ArrayImgFactory(type).create(dims);
        return img;
    }

    Float cost = null;

    public float getCost() {
        if (isNull(cost)) {
            cost = componentProperties.getCost(this);
        }
        return cost;
    }

    HashMap<String, Vector2DPolyline> componentFeatures = new HashMap<>();

    @Override
    public void addComponentFeature(String featureName, Vector2DPolyline feature) {
        componentFeatures.put(featureName, feature);
    }

    @Override
    public Vector2DPolyline getComponentFeature(String featureName) {
        return componentFeatures.get(featureName);
    }

    @Override
    public Set<String> getComponentFeatureNames() {
        return componentFeatures.keySet();
    }

    MaskInterval componentBorderMask;

    public MaskInterval getBorderMask() {
        if (componentBorderMask != null) {
            return componentBorderMask;
        }
        MaskInterval dilatedMask = getDilatedMask();
        MaskInterval componentCoreMask = getErodedMask();
        componentBorderMask = dilatedMask.minus(componentCoreMask);
        return componentBorderMask;
    }

    Img<BitType> erodedImg;

    public Img<BitType> getCoreMaskImg() {
        if (erodedImg != null) {
            return erodedImg;
        }
        RectangleShape shape = new RectangleShape(1, false);
        Img<BitType> componentImage = getComponentImage(new BitType(true));
        erodedImg = Erosion.erode(componentImage, shape, 1);
        return erodedImg;
    }

    MaskInterval erodedMask;

    public MaskInterval getErodedMask() {
        if (erodedMask != null) {
            return erodedMask;
        }
        erodedMask = Masks.toMaskInterval(getCoreMaskImg());
        return erodedMask;
    }

    MaskInterval dilatedMask;

    public MaskInterval getDilatedMask() {
        if (dilatedMask != null) {
            return dilatedMask;
        }
        RectangleShape shape = new RectangleShape(1, false);
        Img<BitType> componentImage = getComponentImage(new BitType(true));
        Img<BitType> dilatedImg = Dilation.dilate(componentImage, shape, 1);
        dilatedMask = Masks.toMaskInterval(dilatedImg);
        return dilatedMask;
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

    Integer hashCode = null;

    public int hashCode() {
        if (!isNull(hashCode)) {
            return hashCode;
        }
        hashCode = calculateHashCode();
        return hashCode;
    }

    private int calculateHashCode() {
        int result = 777;
        int t = 11;
        for (Iterator var3 = pixelList.iterator(); var3.hasNext(); t += 3) {
            Localizable v = (Localizable) var3.next();

            for (int d = 0; d < v.numDimensions(); ++d) {
                int p = v.getIntPosition(d);
                result = result + t * p * p;
            }
        }
        return result;
    }

    public AdvancedComponentPojo getSerializableRepresentation() {
        return new AdvancedComponentPojo(getStringId(),
                getFrameNumber(),
                getLabel(),
                getParentStringId(),
                getChildrenStringIds(),
                ((FloatType) value()).getRealDouble(),
                pixelList,
                maskIntensities,
                maskIntensitiesStd,
                backgroundIntensities,
                backgroundIntensitiesStd,
                getCost());
    }

    public static <T extends Type<T>> AdvancedComponent<T> createFromPojo(AdvancedComponentPojo pojo,
                                                                          ComponentProperties componentProperties,
                                                                          RandomAccessibleInterval<T> sourceImage,
                                                                          IImageProvider imageProvider) {
        return new AdvancedComponent<>(pojo, componentProperties, sourceImage, imageProvider);
    }

    private AdvancedComponent(AdvancedComponentPojo pojo,
                              ComponentProperties componentProperties,
                              RandomAccessibleInterval<T> sourceImage,
                              IImageProvider imageProvider) {
        stringId = pojo.getStringId();
        frameNumber = pojo.getFrameNumber();
        this.componentProperties = componentProperties;
        parentStringId = pojo.getParentStringId();
        label = pojo.getLabel();
        childStringIds = pojo.getChildrenStringIds();
        value = (T) new FloatType((float)pojo.getValue()); /* TODO-MM-20220921: This is dangerous: We need to check that this cast is valid using something like: if(T instanceof FloatType) (e.g.: https://www.baeldung.com/java-instanceof). But I currently do not know how do this with the generic T. */
        pixelList = pojo.getPixelList();
        this.sourceImage = sourceImage;
        this.imageProvider = imageProvider;
        buildLabelRegion(pixelList, label, sourceImage);
        maskIntensities = pojo.getMaskIntensities();
        maskIntensitiesStd = pojo.getMaskIntensitiesStd();
        backgroundIntensities = pojo.getBackgroundIntensities();
        backgroundIntensitiesStd = pojo.getBackgroundIntensitiesStd();
        cost = pojo.getCost();
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof AdvancedComponent)) {
            return false;
        }
        AdvancedComponent<FloatType> other = (AdvancedComponent<FloatType>) o;
        boolean isEqual = getStringId().equals(other.getStringId()) &&
                getFrameNumber() == other.getFrameNumber() &&
                getLabel() == other.getLabel() &&
                getParentStringId().equals(other.getParentStringId()) &&
                value().equals(other.value()) &&
                hashCode() == other.hashCode() &&
                pixelListIsEqual(other.pixelList);
        return isEqual;
    }

    private boolean pixelListIsEqual(List<LocalizableImpl> otherPixelList) {
        if (pixelList.size() != otherPixelList.size()) {
            return false;
        }
        for(int ind=0; ind<pixelList.size(); ind++){
            if(pixelList.get(ind).equals(otherPixelList.get(ind))){
                return false;
            }
        }
        return true;
    }

    public IImageProvider getImageProvider() {
        return imageProvider;
    }
}
