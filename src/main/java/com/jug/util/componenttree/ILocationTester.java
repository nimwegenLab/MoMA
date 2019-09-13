package com.jug.util.componenttree;

import net.imglib2.Localizable;

/**
 * Interface for testing a self-defined condition for {@link Localizable} that is passed into IsValid.
 */
public interface ILocationTester {
    /**
     * Tests if the condition of the LocationTester is still valid.
     * @param location
     * @return
     */
    boolean IsValid(Localizable location);

    /**
     * Reset state of LocationTester.
     */
    void Reset();
}
