package com.jug.util.componenttree;

import net.imglib2.algorithm.componenttree.Component;
import net.imglib2.type.Type;

/**
 * Tester to test if a component has a siblings.
 * @param <T>
 * @param <C>
 */
public class HasSiblingsComponentTester<T extends Type<T>, C extends Component<T, C>>
        implements IComponentTester<T, C> {
    /**
     * Tests if component has sibling.
     * @param component The {@link Component} to test
     * @return True if component has siblings or root-component.
     */
    @Override
    public boolean IsValid(C component) {
        if(component.getParent() == null) return true; // return True for root-component
        return component.getParent().getChildren().size() > 1;
    }

    public boolean discontinueBranch() {
        return false;
    }
}
