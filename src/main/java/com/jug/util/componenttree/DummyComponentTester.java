package com.jug.util.componenttree;

import net.imglib2.algorithm.componenttree.Component;
import net.imglib2.type.Type;

/**
 * This is a dummy component tester that always marks a component as valid. We can inject it, whenever a class expects
 * an IComponentTester, but we do not have or want a true instance of it.
 * @param <T>
 * @param <C>
 */
public class DummyComponentTester<T extends Type<T>, C extends Component<T, C>>
        implements
        IComponentTester<T, C> {

    public boolean IsValid(C component){ return true;}

    public boolean discontinueBranch() {
        return false;
    }
}
