package com.jug.util.componenttree;

import net.imglib2.Localizable;

import java.util.Comparator;

/**
 * Compares the pixel positions between two localizables. They are sorted in rows-first
 * fashion.
 */
public class RowsFirstPixelPositionComparator implements Comparator<Localizable> {
    /**
     * Compares the positions of the two localizables l1 and l2. Returns -1, 0 or 1 (following comparator
     * interface definition) in such a way that the localizables will be sorted rows-first.
     *
     * @param l1
     * @param l2
     * @return Returns int value for comparison of the localizable postion: -1: c1<c2, 0: c1==2, 1: c1>c2
     */
     public int compare(Localizable l1, Localizable l2) {
             if (l1.getDoublePosition(1) < l2.getDoublePosition(1)) return -1;  /* l1 row is larger than l2 */
             if (l1.getDoublePosition(1) == l2.getDoublePosition(1) && l1.getDoublePosition(0) < l2.getDoublePosition(0)) return -1;   /* l1 row is smaller than l2, but l1 column is smaller */
             if (l1.getDoublePosition(1) > l2.getDoublePosition(1)) return 1;  /* l1 row is smaller than l2 */
             if (l1.getDoublePosition(1) == l2.getDoublePosition(1) && l1.getDoublePosition(0) > l2.getDoublePosition(0)) return 1;
            return 0;
        }
}
