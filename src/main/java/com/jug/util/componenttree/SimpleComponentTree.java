package com.jug.util.componenttree;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.componenttree.Component;
import net.imglib2.algorithm.componenttree.ComponentForest;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.type.Type;
import net.imglib2.type.numeric.integer.IntType;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * This class is a new version of {@link Component}. The goal is for all other code to depend on this class and remove
 * all dependencies for library implemenations of {@link Component} (such as e.g. MSER, FilteredComponent, etc.).
 * It can be created from another instance of {@link ComponentForest<C>}, while filtering test criteria with
 * {@link IComponentTester<T, C>}.
 *
 * @param <T> value type of the input image.
 * @author Michael Mell
 */
public final class SimpleComponentTree<T extends Type<T>, C extends Component<T, C>>
        implements
        ComponentForest<SimpleComponent<T>> {
    final ImgLabeling<Integer, IntType> labeling;
    private final ArrayList<SimpleComponent<T>> nodes = new ArrayList<>();
    private final HashSet<SimpleComponent<T>> roots = new HashSet<>();
    private final RandomAccessibleInterval<T> sourceImage;
    private final Img<IntType> img;
    Integer label = 1;
    private IComponentTester<T, C> tester;

    public SimpleComponentTree(ComponentForest<C> componentForest, RandomAccessibleInterval<T> sourceImage) {
        this(componentForest, sourceImage, new DummyComponentTester());
    }

    public SimpleComponentTree(ComponentForest<C> componentForest, RandomAccessibleInterval<T> sourceImage, IComponentTester<T, C> tester) {
        this.sourceImage = sourceImage;
        this.tester = tester;
        long[] dims = new long[sourceImage.numDimensions()];
        sourceImage.dimensions(dims);
        img = ArrayImgs.ints(dims);
        labeling = new ImgLabeling<>(img);
        CreateTree(componentForest);
        SortChildrenByPosition();
    }

    private void SortChildrenByPosition() {
        for (final SimpleComponent root : roots()) {
            SortChildrenRecursively(root);
        }
    }

    private void SortChildrenRecursively(SimpleComponent parent) {
        List<SimpleComponent<T>> children = parent.getChildren();
        ComponentPositionComparator positionComparator = new ComponentPositionComparator(1);
        children.sort(positionComparator);
        for (SimpleComponent<T> component : children) {
            SortChildrenRecursively(component);
        }
    }

    private void CreateTree(ComponentForest<C> componentForest) {
        for (final C root : componentForest.roots()) {
            RecursivelyFindValidComponent(root);
        }
        for (SimpleComponent<T> node : nodes) {
            if (node.getParent() == null) {
                roots.add(node);
            }
        }
    }

    private void RecursivelyFindValidComponent(C sourceComponent) {
        if (tester.IsValid(sourceComponent)) {
            SimpleComponent<T> newRoot = new SimpleComponent<>(labeling, label++, sourceComponent, sourceImage);
            nodes.add(newRoot);
            RecursivelyAddToTree(sourceComponent, newRoot);
        } else {
            for (final C sourceChildren : sourceComponent.getChildren()) {
                RecursivelyFindValidComponent(sourceChildren);
            }
        }
    }

    private void RecursivelyAddToTree(C sourceParent, SimpleComponent<T> targetParent) {
        for (final C sourceChild : sourceParent.getChildren()) {
            if (tester.IsValid(sourceChild)) {  // if child meets condition, add it and with its children
                SimpleComponent<T> targetChild = CreateTargetChild(targetParent, sourceChild);
                RecursivelyAddToTree(sourceChild, targetChild);
            } else {  // continue search for deeper component-nodes
                RecursivelyAddToTree(sourceChild, targetParent);
            }
        }
    }

    @NotNull
    private SimpleComponent<T> CreateTargetChild(SimpleComponent<T> targetComponent, C sourceChild) {
        SimpleComponent<T> targetChild = new SimpleComponent<>(labeling, label++, sourceChild, sourceImage);
        targetChild.setParent(targetComponent);
        targetComponent.addChild(targetChild);
        nodes.add(targetChild);
        return targetChild;
    }

    @Override
    public HashSet<SimpleComponent<T>> roots() {
        return roots;
    }

}

