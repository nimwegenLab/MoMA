package com.jug.util.componenttree;

import net.imglib2.Localizable;

/**
 * Tests if the distance between the two limiting pixel locations along the dimension `dim` is lower than the threshold
 * value maxValue.
 */
public class ComponentExtentTester implements ILocationTester {
    private final int maxValue;
    private final int dim;

    public ComponentExtentTester(int dim, int maxValue) {
        this.maxValue = maxValue;
        this.dim = dim;
    }

    int min = Integer.MAX_VALUE;
    int max = Integer.MIN_VALUE;

    @Override
    public boolean IsValid(Localizable location) {
        final int pos = location.getIntPosition(dim);
        min = Math.min(min, pos);
        max = Math.max(max, pos);
        int width = max - min;
        return width <= maxValue;
    }

    @Override
    public void Reset() {
        min = Integer.MAX_VALUE;
        max = Integer.MIN_VALUE;
    }
}
