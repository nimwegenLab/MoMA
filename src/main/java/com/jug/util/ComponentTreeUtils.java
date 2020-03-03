package com.jug.util;

import com.jug.lp.*;
import com.jug.util.componenttree.ComponentPositionComparator;
import com.jug.util.componenttree.SimpleComponent;
import net.imglib2.Localizable;
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
     * @param tree
     * @return
     */
    public static <C extends Component<?, C>> List<C> getListOfLeavesInOrder(final ComponentForest<C> tree) {
        final List<C> leaves = new ArrayList<>();

        for (final C root : tree.roots()) {
            recursivelyAddLeavesToList(root, leaves);
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
     * @param to
     * @return
     */
    public static boolean isBelowByMoreThen(
            final Component< FloatType, ? > to,
            final Component< FloatType, ? > from,
            final int numPixels) {

//        final ValuePair<Integer, Integer> toMinMax = to.getLocation();
//        final ValuePair<Integer, Integer> fromMinMax = from.getLocation();
        final ValuePair< Integer, Integer > toMinMax = ComponentTreeUtils.getComponentPixelLimits(to, 1);;
        final ValuePair< Integer, Integer > fromMinMax = ComponentTreeUtils.getComponentPixelLimits(from, 1);;
        return (toMinMax.getA() - fromMinMax.getB()) > numPixels;
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
     * Returns list of all neighboring nodes below the current node.
     *
     * @return list of neighboring nodes
     */
    public static List<Component<FloatType, ?>> getLowerNeighbors(final Component<FloatType, ?> node,
                                                                  final ComponentForest<SimpleComponent<FloatType>> componentForest) {
        final ArrayList<Component<FloatType, ?>> neighbors = new ArrayList<>();
        Component<FloatType, ?> neighbor = getLowerNeighborClosestToRootLevel(node, componentForest);
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
     * @param node node for which the neighbor is returned.
     * @return the lower neighbor node
     */
    private static Component<FloatType, ?> getLowerNeighborClosestToRootLevel(final Component<FloatType, ?> node,
                                                                              final ComponentForest<SimpleComponent<FloatType>> componentTree) {
        final Component<FloatType, ?> parentNode = node.getParent();
        if(parentNode != null) { /* {@param node} is child node, so we can get the sibling node below it (if {@param node} is not bottom-most child), which is its lower neighbor */
            final int idx = parentNode.getChildren().indexOf(node);
            if (idx + 1 < parentNode.getChildren().size()) {
                return parentNode.getChildren().get(idx + 1);
            } else { /* {@param node} is bottom-most child node, we therefore need to get bottom neighbor of its parent */
                return getLowerNeighborClosestToRootLevel(parentNode, componentTree);
            }
        }
        else { /* {@param node} is a root, so we need to find the root below and return it, if it exists*/
            List<SimpleComponent<FloatType>> roots = new ArrayList<>(componentTree.roots());
            roots.sort(verticalComponentPositionComparator);
            final int idx = roots.indexOf(node);
            if (idx + 1 < roots.size()) {
                return roots.get(idx + 1);
            }
        }
        return null;
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
    public static <C extends Component<?, C>> List<C> getListOfNodes(final ComponentForest<C> ct) {
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
    private static <C extends Component<?, C>> void addListOfNodes(final C ctn, final ArrayList<C> list) {
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
