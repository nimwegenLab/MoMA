package com.jug.util.componenttree;

import net.imglib2.Localizable;

import java.util.function.Predicate;

/**
 * Tests if the distance between the two limiting pixel locations along the dimension `dim` is lower than the threshold
 * value maxValue.
 */
public class ComponentExtentTester implements ILocationTester {
    private final Predicate<Integer> condition;
    private final int dim;
    int min = Integer.MAX_VALUE;
    int max = Integer.MIN_VALUE;

    public ComponentExtentTester(int dim, Predicate<Integer> condition) {
        this.condition = condition;
        this.dim = dim;
    }

    @Override
    public boolean IsValid(Localizable location) {
        final int pos = location.getIntPosition(dim);
        min = Math.min(min, pos);
        max = Math.max(max, pos);
        int width = max - min;
        return condition.test(width);
    }

    @Override
    public void Reset() {
        min = Integer.MAX_VALUE;
        max = Integer.MIN_VALUE;
    }
}
