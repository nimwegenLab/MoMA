package com.jug.util;

import com.jug.MoMA;
import com.jug.lp.Hypothesis;
import com.jug.util.componenttree.ComponentPositionComparator;
import com.jug.util.componenttree.SimpleComponent;
import net.imglib2.Localizable;
import net.imglib2.Point;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.componenttree.Component;
import net.imglib2.algorithm.componenttree.ComponentForest;
import net.imglib2.type.Type;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.ValuePair;
import org.javatuples.Pair;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;

/**
 * @author jug
 */
public class ComponentTreeUtils {

    /**
     * Get all leaf nodes under the specified component in component forest.
     *
     * @param forest
     * @return
     */
    public static <C extends Component<?, C>> List<C> getLeafNodes(final ComponentForest<C> forest) {
        final List<C> leaves = new ArrayList<>();
        for (final C root : forest.roots()) {
            recursivelyAddLeavesToList(root, leaves);
        }
        return leaves;
    }

    /**
     * Get all leaf nodes under the specified component node.
     *
     * @param component_node
     * @return
     */
    public static <T extends Type<T>> List<SimpleComponent<T>> getLeafNodes(final SimpleComponent<T> component_node) {
        final List<SimpleComponent<T>> leaves = new ArrayList<>();
        for (final SimpleComponent<T> node : component_node.getChildren()) {
            recursivelyAddLeavesToList(node, leaves);
        }
        return leaves;
    }

    /**
     * @param leaves
     */
    private static <C extends Component<?, C>> void recursivelyAddLeavesToList(final C node, final List<C> leaves) {
        if (node.getChildren().size() == 0) {
            leaves.add(node);
        } else {
            for (final C child : node.getChildren()) {
                recursivelyAddLeavesToList(child, leaves);
            }
        }
    }

    /**
     * @param leaves
     */
    private static <T extends Type<T>> void recursivelyAddLeavesToList(final SimpleComponent<T> node, final List<SimpleComponent<T>> leaves) {
        if (node.getChildren().size() == 0) {
            leaves.add(node);
        } else {
            for (final SimpleComponent child : node.getChildren()) {
                recursivelyAddLeavesToList(child, leaves);
            }
        }
    }

    private static ComponentPositionComparator verticalComponentPositionComparator = new ComponentPositionComparator(1);

    /**
     * Checks if {@param candidate} is above {@param hyp} inside the image.
     *
     * @param candidate candidate that we are interested in
     * @param hyp reference that we are testing against
     * @return boolean indicating if it is above or not
     */
    public static boolean isAbove(
            final Hypothesis<Component<FloatType, ?>> candidate,
            final Hypothesis<Component<FloatType, ?>> hyp) {
        SimpleComponent<FloatType> candidateComponent = (SimpleComponent<FloatType>)candidate.getWrappedComponent();
        SimpleComponent<FloatType> referenceComponent = (SimpleComponent<FloatType>)hyp.getWrappedComponent();
        return verticalComponentPositionComparator.compare(candidateComponent, referenceComponent) == -1; /* NOTE: since we are using image/matrix coordinates (e.g. origin at the top), the coordinate value for {@param candidate} will be lower than {@param hyp}, when it is above {@param hyp} */
    }

    /**
     * @param sourceComponent
     * @return
     */
    public static boolean isBelowByMoreThen(
            final Component< FloatType, ? > sourceComponent,
            final Component< FloatType, ? > targetComponent,
            final int maximumAllowedDownwardMovement) {
        final ValuePair< Integer, Integer > sourceComponentBoundaries = ComponentTreeUtils.getComponentPixelLimits(targetComponent, 1);
        final ValuePair< Integer, Integer > targetComponentBoundaries = ComponentTreeUtils.getComponentPixelLimits(sourceComponent, 1);
        final float targetUpperBoundary = targetComponentBoundaries.getA();
        final float sourceLowerBoundary = sourceComponentBoundaries.getB();

        return (targetUpperBoundary - sourceLowerBoundary) > maximumAllowedDownwardMovement;
    }

    /**
     * @param sourceComponent
     * @return
     */
    public static List<SimpleComponent<FloatType>> getPlausibleTargetComponents(
            final SimpleComponent<FloatType> sourceComponent,
            final List<SimpleComponent<FloatType>> targetComponents,
            int sourceTime) {
        List<SimpleComponent<FloatType>> result = new ArrayList<>();
        for (SimpleComponent<FloatType> targetComponent : targetComponents) {
            if (isPlausibleTargetComponent(sourceComponent, targetComponent, sourceTime)) {
                result.add(targetComponent);
            }
        }
        return result;
    }

    private static int currentTime = -1;

    public static boolean isPlausibleTargetComponent(final SimpleComponent<FloatType> sourceComponent,
                                                     final SimpleComponent<FloatType> targetComponent,
                                                     int sourceTime) {
        int totalAreaBelowSourceComponent = sourceComponent.getTotalAreaOfComponentsBelow();
        int totalAreaIncludingSourceComponent = totalAreaBelowSourceComponent + (int) sourceComponent.size();

        int totalAreaBelowTargetComponent = targetComponent.getTotalAreaOfComponentsBelow();
        int totalAreaIncludingTargetComponent = totalAreaBelowTargetComponent + (int) targetComponent.size();

//        int differenceOfTotalArea = 0;
        int differenceOfTotalArea = targetComponent.getTotalAreaOfRootComponents() - sourceComponent.getTotalAreaOfRootComponents();
//        if (differenceOfTotalArea > 0) {
//            differenceOfTotalArea = 0; /* we only use the correction term for the reduction in area to account for cases when cells die */
//        }
//        if (differenceOfTotalArea < 0 ){
//            return true;
//        }

//        if (sourceTime != currentTime) {
//            System.out.println("sourceTime: " + sourceTime);
//            System.out.println("totalAreaSource: " + sourceComponent.getTotalAreaOfRootComponents());
//            System.out.println("totalAreaTarget: " + targetComponent.getTotalAreaOfRootComponents());
//            System.out.println("differenceOfTotalArea: " + differenceOfTotalArea);
//            System.out.println("");
//            currentTime = sourceTime;
//        }

//        int lowerTargetAreaLimit = (int) Math.floor(totalAreaBelowSourceComponent * (1 - MoMA.MAXIMUM_SHRINKAGE_PER_FRAME)) - Math.abs(differenceOfTotalArea);
        int lowerTargetAreaLimit;
        if (differenceOfTotalArea < 0){
            lowerTargetAreaLimit = (int) Math.floor(totalAreaBelowSourceComponent * (1 - MoMA.MAXIMUM_SHRINKAGE_PER_FRAME)) - (int) Math.ceil((1 + MoMA.MAXIMUM_GROWTH_PER_FRAME) * Math.abs(differenceOfTotalArea));
        }
        else{
            lowerTargetAreaLimit = (int) Math.floor(totalAreaBelowSourceComponent * (1 - MoMA.MAXIMUM_SHRINKAGE_PER_FRAME));
        }

        int upperTargetAreaLimit;
        if (differenceOfTotalArea > 0) {
            upperTargetAreaLimit = (int) Math.ceil(totalAreaIncludingSourceComponent * (1 + MoMA.MAXIMUM_GROWTH_PER_FRAME));
        }
        else {
            upperTargetAreaLimit = (int) Math.ceil(totalAreaIncludingSourceComponent * (1 + MoMA.MAXIMUM_GROWTH_PER_FRAME)) + (int) Math.ceil((1 + MoMA.MAXIMUM_SHRINKAGE_PER_FRAME) * Math.abs(differenceOfTotalArea));;
        }



//        double yCenterSource = sourceComponent.firstMomentPixelCoordinates()[1];
//        double yCenterTarget = targetComponent.firstMomentPixelCoordinates()[1];
//        if(Math.abs(yCenterSource - yCenterTarget) < 10) {
//            System.out.println("yCenterSource: " + yCenterSource);
//            System.out.println("yCenterTarget: " + yCenterTarget);
//            System.out.println("");
//            System.out.println("totalAreaBelowSourceComponent: " + totalAreaBelowSourceComponent);
//            System.out.println("totalAreaIncludingSourceComponent: " + totalAreaIncludingSourceComponent);
//            System.out.println("");
//            System.out.println("totalAreaBelowTargetComponent: " + totalAreaBelowTargetComponent);
//            System.out.println("totalAreaIncludingTargetComponent: " + totalAreaIncludingTargetComponent);
//            System.out.println("");
//            System.out.println("lowerTargetAreaLimit: " + lowerTargetAreaLimit);
//            System.out.println("upperTargetAreaLimit: " + upperTargetAreaLimit);
//            System.out.println("");
//            System.out.println("lower condition: " + (totalAreaBelowTargetComponent >= lowerTargetAreaLimit));
//            System.out.println("upper condition: " + (totalAreaIncludingTargetComponent <= upperTargetAreaLimit));
//            System.out.println("");
//            System.out.println("");
//            System.out.println("");
//        }
        boolean isValid;
        if (totalAreaBelowTargetComponent >= lowerTargetAreaLimit && totalAreaIncludingTargetComponent <= upperTargetAreaLimit) {
            isValid = true;
        }
        else{
            isValid = false;
        }

        if (sourceTime == 16) {
            if(sourceComponent.getRankRelativeToComponentsClosestToRoot() == 2) {
                if (targetComponent.getRankRelativeToComponentsClosestToRoot() == 0) {
                    System.out.println("sourceTime: " + sourceTime);
                    System.out.println("totalAreaSource: " + sourceComponent.getTotalAreaOfRootComponents());
                    System.out.println("totalAreaTarget: " + targetComponent.getTotalAreaOfRootComponents());
                    System.out.println("differenceOfTotalArea: " + differenceOfTotalArea);
                    System.out.println("");
                    currentTime = sourceTime;
                }
            }
        }
        return isValid;
    }

    /**
     * Returns the smallest and largest value on the y-axis that is spanned by
     * this component-tree-node, which is in direction of the growthlane.
     *
     * @param node the node in question.
     * @return a <code>Pair</code> or two <code>Integers</code> giving the
     * leftmost and rightmost point on the x-axis that is covered by
     * this component-tree-node respectively.
     */
    public static ValuePair<Integer, Integer> getTreeNodeInterval(final Component<?, ?> node) {
        return getComponentPixelLimits(node, 1);
    }

    /**
     * Returns the pixel limits of the component along a given dimension.
     * @param component the component to process
     * @param dim the dimension in which component limits are determined
     * @return ValuePair<int min, int max> minimum and maximum pixel limits.
     */
    public static ValuePair<Integer, Integer> getComponentPixelLimits(final Component<?, ?> component, int dim){
        int min = Integer.MAX_VALUE;
        int max = Integer.MIN_VALUE;
        for (Localizable localizable : component) {
            final int pos = localizable.getIntPosition(dim);
            min = Math.min(min, pos);
            max = Math.max(max, pos);
        }
        return new ValuePair<>(min, max);
    }

    /**
     * Checks if the y-position of {@param position} is contained in the y-range of the
     * bounding box of {@param component}.
     *
     * @param component component for which we test the bounding box
     * @param position position to test
     * @return true if y-position is in the bounding box
     */
    public static boolean componentContainsYPosition(final Component<?, ?> component, Point position) {
        int pos[] = new int[2]; // this works only for 2D images
        position.localize(pos);
        ValuePair<Integer, Integer> yLimits = ComponentTreeUtils.getComponentPixelLimits(component, 1);
        if (pos[1] < yLimits.getA() || pos[1] > yLimits.getB()) {
            return false;
        }
        return true;
    }

    /**
     * Returns the pixel size of the component along the given dimension.
     * @param component the component to process
     * @param dim the dimension along which the size will be determined
     * @return integer value, which is the difference between the starting and end pixel-positions of the component.
     */
    public static int getComponentSize(final Component<?,?> component, int dim){
        ValuePair<Integer, Integer> limits = getComponentPixelLimits(component, dim);
        return limits.b - limits.a;
    }

    /**
     * Returns the minimal pixel intensity of the component.
     *
     * @param node the node in question.
     * @return a <code>Pair</code> or two <code>Integers</code> giving the
     * leftmost and rightmost point on the x-axis that is covered by
     * this component-tree-node respectively.
     */
    public static ValuePair<Float, Float> getTreeNodeMinMaxIntensity(final Component<?, ?> node, final RandomAccessibleInterval<FloatType> img) {
        float minimumPixelIntensity = Float.MAX_VALUE;
        float maximumPixelIntensity = Float.MIN_VALUE;
        RandomAccess<FloatType> ra = img.randomAccess();
        final Iterator<Localizable> componentIterator = node.iterator();
        FloatType currentPixelValue;
        while (componentIterator.hasNext()) {
            Localizable pos = componentIterator.next();
            ra.setPosition(pos);
            currentPixelValue = ra.get();
            minimumPixelIntensity = Math.min(minimumPixelIntensity, currentPixelValue.getRealFloat());
            maximumPixelIntensity = Math.max(maximumPixelIntensity, currentPixelValue.getRealFloat());
//            ra.get().set(1.0f);
        }
//		ImageJFunctions.show(img, "Segment Image");
        return new ValuePair<>(minimumPixelIntensity, maximumPixelIntensity);
    }

    /**
     * @param ct
     * @return
     */
    public static <C extends Component<?, C>> int countNodes(final ComponentForest<C> ct) {
        int nodeCount = ct.roots().size();
        for (final C root : ct.roots()) {
            nodeCount += countNodes(root);
        }
        return nodeCount;
    }

    /**
     * @return
     */
    private static <C extends Component<?, C>> int countNodes(final C ctn) {
        int nodeCount = ctn.getChildren().size();
        for (final C child : ctn.getChildren()) {
            nodeCount += countNodes(child);
        }
        return nodeCount;
    }

    /**
     * @param ct
     * @return
     */
    public static <C extends Component<FloatType, C>> List<C> getListOfNodes(final ComponentForest<C> ct) {
        final ArrayList<C> ret = new ArrayList<>();
        for (final C root : ct.roots()) {
            ret.add(root);
            addListOfNodes(root, ret);
        }
        return ret;
    }

    /**
     * @param list
     */
    public static <C extends Component<FloatType, C>> void addListOfNodes(final C ctn, final ArrayList<C> list) {
        for (final C child : ctn.getChildren()) {
            list.add(child);
            addListOfNodes(child, list);
        }
    }

    /**
     * @param ctnLevel
     * @return
     */
    public static <C extends Component<?, C>> ArrayList<C> getAllChildren(final ArrayList<C> ctnLevel) {
        final ArrayList<C> nextCtnLevel = new ArrayList<>();
        for (final C ctn : ctnLevel) {
            nextCtnLevel.addAll(ctn.getChildren());
        }
        return nextCtnLevel;
    }

    /**
     * @param ctn
     * @return
     */
    public static int getLevelInTree(final Component<?, ?> ctn) {
        int level = 0;
        Component<?, ?> runner = ctn;
        while (runner.getParent() != null) {
            level++;
            runner = runner.getParent();
        }
        return level;
    }


    /**
     * Works through the component tree and at each tree-level passes a list of all components in that tree level to
     * componentlevelListConsumer. componentlevelListConsumer takes Pair<List<C>, Integer>, where List<C> is the list of
     * components in the level and Integer is the number of the level with 0 being the root-level.
     * @param componentForest componentForest being processed.
     * @param componentlevelListConsumer consumer of the list of components in the level and corresponding level number.
     * @param <C> type of component being processed.
     */
    public static <C extends Component<T, C>, T extends Type<T>> void doForEachComponentInTreeLevel(final ComponentForest<C> componentForest,
                                                                                                 Consumer<Pair<List<C>, Integer>> componentlevelListConsumer ){
        int level = 0;
        ArrayList<C> ctnLevel = new ArrayList<>();
        for ( final C root : componentForest.roots() ) { // populate the root-level component list
            ctnLevel.add(root);
        }

        while ( ctnLevel.size() > 0 ) {
            componentlevelListConsumer.accept(new Pair<>(ctnLevel, level));
            ctnLevel = ComponentTreeUtils.getAllChildren( ctnLevel );
            level++;
        }
    }
}
