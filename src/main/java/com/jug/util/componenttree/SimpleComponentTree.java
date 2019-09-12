package com.jug.util.componenttree;

import com.jug.util.filteredcomponents.FilteredComponent;
import net.imglib2.Localizable;
import net.imglib2.algorithm.componenttree.Component;
import net.imglib2.algorithm.componenttree.ComponentForest;
import net.imglib2.type.Type;
import net.imglib2.util.ValuePair;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * This class is a reduced version of {@link com.jug.util.filteredcomponents.FilteredComponentTree}.
 * It can be created from another instance of {@link ComponentForest<FilteredComponent< T >>}, while filtering
 * the {@link FilteredComponent}s that are to be included.
 *
 * @param <T> value type of the input image.
 * @author Michael Mell
 */
public final class SimpleComponentTree<T extends Type<T>>
        implements
        ComponentForest<SimpleComponent<T>> {
    private final ArrayList<SimpleComponent<T>> nodes = new ArrayList<>();
    private final HashSet<SimpleComponent<T>> roots = new HashSet<>();
    private IComponentTester<T, FilteredComponent<T>> tester;

    public SimpleComponentTree(ComponentForest<FilteredComponent<T>> componentForest, IComponentTester<T, FilteredComponent<T>> tester) {
        this.tester = tester;
        CreateTree(componentForest);
    }

    private void CreateTree(ComponentForest<FilteredComponent<T>> componentForest) {
        for (final FilteredComponent<T> root : componentForest.roots()) {
            RecursivelyFindValidComponent(root);
        }
        for(SimpleComponent<T> node:nodes){
            if(node.getParent() == null)
            {
                roots.add(node);
            }
        }
    }

    private void RecursivelyFindValidComponent(FilteredComponent<T> sourceComponent){
        if (tester.IsValid(sourceComponent)) {
            SimpleComponent<T> newRoot = new SimpleComponent<>(sourceComponent.pixelList, sourceComponent.value());
            nodes.add(newRoot);
            RecursivelyAddToTree(sourceComponent, newRoot);
        }
        else{
            for (final FilteredComponent<T> sourceChildren : sourceComponent.getChildren()) {
                RecursivelyFindValidComponent(sourceChildren);
            }
        }
    }

    private void RecursivelyAddToTree(FilteredComponent<T> sourceParent, SimpleComponent<T> targetParent) {
        for (final FilteredComponent<T> sourceChild : sourceParent.getChildren()) {
            if (tester.IsValid(sourceChild)) {  // if child meets condition, add it and with its children
                SimpleComponent<T> targetChild = CreateTargetChild(targetParent, sourceChild);
                RecursivelyAddToTree(sourceChild, targetChild);
            } else {  // continue search for deeper component-nodes
                RecursivelyAddToTree(sourceChild, targetParent);
            }
        }
    }

    @NotNull
    private SimpleComponent<T> CreateTargetChild(SimpleComponent<T> targetComponent, FilteredComponent<T> sourceChild) {
        SimpleComponent<T> targetChild = new SimpleComponent<>(sourceChild.pixelList, sourceChild.value());
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

