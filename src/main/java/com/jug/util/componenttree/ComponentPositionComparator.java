package com.jug.util.componenttree;

import java.util.Comparator;

/**
 * Compares the position of the two components that are passed using their center of mass as position.
 */
public class ComponentPositionComparator implements Comparator<SimpleComponent> {
    /**
     * Dimension of the components that will be compared.
     */
    private int dim;

    public ComponentPositionComparator(int dim) {
        this.dim = dim;
    }

    public int compare(SimpleComponent c1, SimpleComponent c2) {
        if (c1.firstMomentPixelCoordinates()[dim] < c2.firstMomentPixelCoordinates()[dim]) return -1;
        if (c1.firstMomentPixelCoordinates()[dim] > c2.firstMomentPixelCoordinates()[dim]) return 1;
        return 0;
    }
}
