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
public final class SimpleComponentTree<C extends Component<T, C>, T extends Type<T>>
        implements
        ComponentForest<C> {
    private final ArrayList<C> nodes = new ArrayList<>();
    private final HashSet<C> roots = new HashSet<>();

    public SimpleComponentTree(ComponentForest<C> componentForest) {
        int max_width = 12;
//            List<C> leaves = ComponentTreeUtils.getListOfLeavesInOrder(ct);
//            for (C leave: leaves){
//                if(ComponentWidth(leave) < max_width){
//
//                }
//            }
        for (final C root : componentForest.roots()) {
            RecursivelyAddAsRootIfValid(root, max_width);

//                ArrayList< C > ctnLevel = new ArrayList<>();
//				ctnLevel.add( root );
//				while ( ctnLevel.size() > 0 ) {
//					for ( final Component< ?, ? > ctn : ctnLevel ) {
//					}
//					ctnLevel = ComponentTreeUtils.getAllChildren( ctnLevel );
//				}
        }

//        for ( final FilteredComponent< T > c : component.children )
//            roots.remove( c );
//        roots.add( component );
//        nodes.add( component );
    }

    private void RecursivelyAddAsRootIfValid(C component, int max_width) {
        int width = ComponentWidth(component);
        if (width < max_width) {
            roots.add(component);
        } else {
            List<C> children = component.getChildren();
            for (final C child : children) {
                RecursivelyAddAsRootIfValid(child, max_width);
            }
        }
    }

    @Override
    public HashSet<C> roots() {
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

