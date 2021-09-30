package com.jug.util.componenttree;

import net.imglib2.algorithm.componenttree.Component;
import net.imglib2.type.Type;
import net.imglib2.type.numeric.real.FloatType;

/**
 * Interface for testing if a {@link Component} is validity.
 *
 * @param <T> The image type {@link Type}.
 * @param <C> The type of {@link Component}
 */
public interface IComponentTester<T extends Type<T>, C extends Component<T, C>> {
    /**
     * Method for testing if the component is valid.
     *
     * @param component The {@link Component} to test
     * @return true if the component is valid.
     */
    boolean IsValid(C component);
}
