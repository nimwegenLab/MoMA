package com.jug.util.componenttree;

import net.imglib2.Localizable;

public class ComponentExtentTester implements ILocationTester {
    private int maxComponentWidth;
    private int dim;

    public ComponentExtentTester(int maxComponentWidth, int dim) {
        this.maxComponentWidth = maxComponentWidth;
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
        return width <= maxComponentWidth;
    }

    @Override
    public void Reset() {
        min = Integer.MAX_VALUE;
        max = Integer.MIN_VALUE;
    }
}
