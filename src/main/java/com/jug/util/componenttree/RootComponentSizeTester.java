package com.jug.util.componenttree;

import net.imglib2.algorithm.componenttree.Component;
import net.imglib2.type.Type;

/**
 * This tester tests if the component is a root component and has a size smaller than the threshold value of
 * minimumSizeOfRootComponents. If so it will return false.
 * @param <T>
 * @param <C>
 */
public class RootComponentSizeTester<T extends Type<T>, C extends Component<T, C>>
        implements IComponentTester<T, C> {

    private final int minimumSizeOfRootComponents;

    public RootComponentSizeTester(int minimumSizeOfRootComponents) {
        this.minimumSizeOfRootComponents = minimumSizeOfRootComponents;
    }

    @Override
    public boolean IsValid(C component) {
        return component.getParent() != null || component.size() >= minimumSizeOfRootComponents; /* the component is root (i.e. has no parent) and is smaller than threshold */
    }

    public boolean discontinueBranch() {
        return true; /* if the root component is not valid, we do not want to add any child components of it */
    }
}
