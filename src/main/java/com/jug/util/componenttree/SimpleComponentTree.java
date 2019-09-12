package com.jug.util.componenttree;

import com.jug.util.filteredcomponents.FilteredComponent;
import net.imglib2.Localizable;
import net.imglib2.algorithm.componenttree.Component;
import net.imglib2.algorithm.componenttree.ComponentForest;
import net.imglib2.type.Type;
import net.imglib2.util.ValuePair;

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

    public SimpleComponentTree(ComponentForest<FilteredComponent<T>> componentForest) {
        int maxComponentWidth = 20;
        CreateTree(componentForest, maxComponentWidth);
    }

    private void CreateTree(ComponentForest<FilteredComponent<T>> componentForest, int max_width) {
        for (final FilteredComponent<T> root : componentForest.roots()) {
            RecursivelyFindValidComponent(root, max_width);
        }
        for(SimpleComponent<T> node:nodes){
            if(node.getParent() == null)
            {
                roots.add(node);
            }
        }
    }

    private void RecursivelyFindValidComponent(FilteredComponent<T> sourceComponent, int max_width){
        int width = ComponentWidth(sourceComponent);
        if (width <= max_width) {
            SimpleComponent<T> newRoot = new SimpleComponent<>(sourceComponent.pixelList, sourceComponent.value());
            nodes.add(newRoot);
            RecursivelyAddToTree(sourceComponent, newRoot, max_width);
        }
        else{
            for (final FilteredComponent<T> sourceChildren : sourceComponent.getChildren()) {
                RecursivelyFindValidComponent(sourceChildren, max_width);
            }
        }
    }

    private void RecursivelyAddToTree(FilteredComponent<T> sourceComponent, SimpleComponent<T> targetComponent, int max_width) {
        int width = ComponentWidth(sourceComponent);
        if (width <= max_width) {
            List<FilteredComponent<T>> children = sourceComponent.getChildren();
            for (final FilteredComponent<T> sourceChild : children) {
                SimpleComponent<T> targetChild = new SimpleComponent<>(sourceChild.pixelList, sourceChild.value());
                targetChild.setParent(targetComponent);
                targetComponent.addChild(targetChild);
                nodes.add(targetChild);
                RecursivelyAddToTree(sourceChild, targetChild, max_width);
            }
        }
        else{
            RecursivelyAddToTree(sourceComponent, targetComponent, max_width);
        }
    }

    @Override
    public HashSet<SimpleComponent<T>> roots() {
        return roots;
    }

    private int ComponentWidth(Component component) {
        ValuePair<Integer, Integer> limits = getComponentLimits(component.iterator(), 0);
        return limits.b - limits.a;
    }

    private ValuePair<Integer, Integer> getComponentLimits(Iterator<Localizable> pixelPositionIterator, int dim) {
        int min = Integer.MAX_VALUE;
        int max = Integer.MIN_VALUE;

        while (pixelPositionIterator.hasNext()) {
            Localizable location = pixelPositionIterator.next();
            final int pos = location.getIntPosition(dim);
            min = Math.min(min, pos);
            max = Math.max(max, pos);
        }
        return new ValuePair<>(min, max);
    }

}

