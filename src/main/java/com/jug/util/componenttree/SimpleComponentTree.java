package com.jug.util.componenttree;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.componenttree.Component;
import net.imglib2.algorithm.componenttree.ComponentForest;

import net.imglib2.type.Type;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
        ComponentForest<AdvancedComponent<T>> {
    private final List<AdvancedComponent<T>> nodes = new ArrayList<>();
    private final List<AdvancedComponent<T>> roots = new ArrayList<>();
    private final RandomAccessibleInterval<T> sourceImage;
    Integer label = 1;
    private final IComponentTester<T, C> tester;
    private ComponentProperties componentPropertiesCalculator;


    public SimpleComponentTree(ComponentForest<C> componentForest, RandomAccessibleInterval<T> sourceImage, ComponentProperties componentPropertiesCalculator) {
        this(componentForest, sourceImage, new DummyComponentTester(), componentPropertiesCalculator);
    }

    public SimpleComponentTree(ComponentForest<C> componentForest, RandomAccessibleInterval<T> sourceImage, IComponentTester<T, C> tester, ComponentProperties componentPropertiesCalculator) {
        this.sourceImage = sourceImage;
        this.tester = tester;
        this.componentPropertiesCalculator = componentPropertiesCalculator;
        CreateTree(componentForest);
        SortChildrenByPosition();
        sortRootNodes();
        writeRootNodesToAllNodes();
    }

    private void writeRootNodesToAllNodes() {
        for (AdvancedComponent<T> node : nodes) {
            node.setComponentTreeRoots(roots);
        }
    }

    private void sortRootNodes() {
        ComponentPositionComparator positionComparator = new ComponentPositionComparator(1);
        roots.sort(positionComparator);
    }

    private void SortChildrenByPosition() {
        for (final AdvancedComponent root : roots()) {
            SortChildrenRecursively(root);
        }
    }

    private void SortChildrenRecursively(AdvancedComponent parent) {
        List<AdvancedComponent<T>> children = parent.getChildren();
        ComponentPositionComparator positionComparator = new ComponentPositionComparator(1);
        children.sort(positionComparator);
        for (AdvancedComponent<T> component : children) {
            SortChildrenRecursively(component);
        }
    }

    private void CreateTree(ComponentForest<C> componentForest) {
        for (final C root : componentForest.roots()) {
            RecursivelyFindValidComponent(root);
        }
        for (AdvancedComponent<T> node : nodes) {
            if (node.getParent() == null) {
                roots.add(node);
            }
        }
    }

    private void RecursivelyFindValidComponent(C sourceComponent) {
        if (tester.IsValid(sourceComponent)) {
            AdvancedComponent<T> newRoot = new AdvancedComponent<>(label++, sourceComponent, sourceImage, componentPropertiesCalculator); // TODO-MM-20220330: Is it a bug that we do not create a new labeling image per component? It seems to be because child components overlap ...
            nodes.add(newRoot);
            RecursivelyAddToTree(sourceComponent, newRoot);
        } else {
            if (tester.discontinueBranch()) {
                return;
            }
            for (final C sourceChildren : sourceComponent.getChildren()) {
                RecursivelyFindValidComponent(sourceChildren);
            }
        }
    }

    private void RecursivelyAddToTree(C sourceParent, AdvancedComponent<T> targetParent) {
        for (final C sourceChild : sourceParent.getChildren()) {
            if (tester.IsValid(sourceChild)) {  // if child meets condition, add it and with its children
                AdvancedComponent<T> targetChild = CreateTargetChild(targetParent, sourceChild);
                RecursivelyAddToTree(sourceChild, targetChild);
            } else {  // continue search for deeper component-nodes
                RecursivelyAddToTree(sourceChild, targetParent);
            }
        }
    }

    @NotNull
    private AdvancedComponent<T> CreateTargetChild(AdvancedComponent<T> targetComponent, C sourceChild) {
        AdvancedComponent<T> targetChild = new AdvancedComponent<>(label++, sourceChild, sourceImage, componentPropertiesCalculator);
        targetChild.setParent(targetComponent);
        targetComponent.addChild(targetChild);
        nodes.add(targetChild);
        return targetChild;
    }

    @Override
    public Set<AdvancedComponent<T>> roots() {
        return new HashSet(roots);
    }

    public List<AdvancedComponent<T>> rootsSorted() {
        return roots;
    }

    public List<AdvancedComponent<T>> getAllComponents() {
        return nodes;
    }
}

