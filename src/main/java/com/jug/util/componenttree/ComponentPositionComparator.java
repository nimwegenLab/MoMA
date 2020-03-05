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

    /**
     * Compares the center positions of the two components. Returns a negative integer,
     * zero, or a positive integer as the first argument is less than, equal
     * to, or greater than the second.<p>
     *
     * @param c1
     * @param c2
     * @return Returns int value for comparison of center: -1: c1<c2, 0: c1==2, 1: c1>c2
     */
     public int compare(SimpleComponent c1, SimpleComponent c2) {
            if (c1.firstMomentPixelCoordinates()[dim] < c2.firstMomentPixelCoordinates()[dim]) return -1;
            if (c1.firstMomentPixelCoordinates()[dim] > c2.firstMomentPixelCoordinates()[dim]) return 1;
            return 0;
        }
}
