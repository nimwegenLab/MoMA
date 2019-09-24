package com.jug.util.componenttree;

import net.imglib2.Localizable;

import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Tests if the bounding box of a component in dimension `dim` is `<= maxValue`.
 */
public class PixelPositionTester implements ILocationTester {
    private final int dim;
    private Predicate<Integer> condition;

    public PixelPositionTester(int dim, Predicate<Integer> condition) {
        this.dim = dim;
        this.condition = condition;
    }

    @Override
    public boolean IsValid(Localizable location) {
        final int pos = location.getIntPosition(dim);
        return condition.test(pos);
    }

    @Override
    public void Reset() {
        // this need to do nothing
    }
}
