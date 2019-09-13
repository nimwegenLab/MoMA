package com.jug.util.componenttree;

import net.imglib2.Localizable;

/**
 *
 */
public class ComponentBoundaryTester implements ILocationTester {
    private final int maxValue;
    private final int dim;

    public ComponentBoundaryTester(int maxValue, int dim) {
        this.maxValue = maxValue;
        this.dim = dim;
    }

    @Override
    public boolean IsValid(Localizable location) {
        final int pos = location.getIntPosition(dim);
        return pos <= maxValue;
    }

    @Override
    public void Reset() {
        // this need to do nothing
    }
}
