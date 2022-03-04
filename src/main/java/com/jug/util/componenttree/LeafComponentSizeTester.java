package com.jug.util.componenttree;

import net.imglib2.algorithm.componenttree.Component;
import net.imglib2.type.Type;

/**
 * This tester tests if the component is NOT a root component and has a size larger than the threshold value
 * minimumSizeOfLeafComponents. If so it will return false. This effectively determines the minimal allowed size for
 * leaf components in the resulting component tree.
 * @param <T>
 * @param <C>
 */
public class LeafComponentSizeTester<T extends Type<T>, C extends Component<T, C>>
        implements IComponentTester<T, C> {

    private final int minimumSizeOfLeafComponents;

    public LeafComponentSizeTester(int minimumSizeOfRootComponents) {
        this.minimumSizeOfLeafComponents = minimumSizeOfRootComponents;
    }

    @Override
    public boolean IsValid(C component) {
        if(component.getParent() == null) return true;
        return component.size() > minimumSizeOfLeafComponents; /* the component is not root (i.e. has a parent) and is smaller than threshold */
    }

    public boolean discontinueBranch() {
        return false;
    }
}
