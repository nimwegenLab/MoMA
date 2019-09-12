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
    private final HashSet<FilteredComponent<T>> roots = new HashSet<>();
    private final HashSet<SimpleComponent<T>> roots_new = new HashSet<>();

    public SimpleComponentTree(ComponentForest<FilteredComponent<T>> componentForest) {
        int maxComponentWidth = 20;

        CreateTree(componentForest, maxComponentWidth);

//        for (final FilteredComponent<T> root : componentForest.roots()) {
//            RecursivelyAddNodeIfValid(root, maxComponentWidth);
//
////                ArrayList< C > ctnLevel = new ArrayList<>();
////				ctnLevel.add( root );
////				while ( ctnLevel.size() > 0 ) {
////					for ( final Component< ?, ? > ctn : ctnLevel ) {
////					}
////					ctnLevel = ComponentTreeUtils.getAllChildren( ctnLevel );
////				}
//        }

//        for ( final FilteredComponent< T > c : component.children )
//            roots.remove( c );
//        roots.add( component );
//        nodes.add( component );
    }

//    private void RecursivelyAddNodeIfValid(FilteredComponent<T> component, int max_width) {
//        int width = ComponentWidth(component);
//        if (width <= max_width) {
//            roots.add(component);
//        } else {
//            List<FilteredComponent<T>> children = component.getChildren();
//            for (final FilteredComponent<T> child : children) {
//                RecursivelyAddNodeIfValid(child, max_width);
//            }
//        }
//    }

    private void CreateTree(ComponentForest<FilteredComponent<T>> componentForest, int max_width) {
        for (final FilteredComponent<T> root : componentForest.roots()) {
            RecursivelyFindValidComponent(root, max_width);
        }
        for(SimpleComponent<T> node:nodes){
            if(node.getParent() == null)
            {
                roots_new.add(node);
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
//            List<C> children = component.getChildren();
//            }

        // if(component.getParent == null)
//            roots.add(component);



//        int width = ComponentWidth(component);
//        if (width <= max_width) {
//            roots.add(component);
//        } else {
//            List<C> children = component.getChildren();
//            for (final C child : children) {
//                RecursivelyAddNodeIfValid(child, max_width);
//            }
//        }
    }

    @Override
    public HashSet<SimpleComponent<T>> roots() {
        return roots_new;
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

