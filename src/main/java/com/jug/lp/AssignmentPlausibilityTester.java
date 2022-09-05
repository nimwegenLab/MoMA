package com.jug.lp;

import com.jug.config.ITrackingConfiguration;

/**
 * Methods in this class are used to test whether the combination of components in assignments are physically plausible.
 * They are used to determine whether an assignment should be added to the optimizer.
 */
public class AssignmentPlausibilityTester {

    private ITrackingConfiguration trackingConfiguration;

    public AssignmentPlausibilityTester(ITrackingConfiguration trackingConfiguration) {
        this.trackingConfiguration = trackingConfiguration;
    }

    /**
     * Test if the size difference between the source component and the total size of the target components is plausible.
     *
     * @param sourceComponentSize
     * @param totalTargetComponentSize
     * @return
     */
    int minSizeForFiltering = 15; /* length of cells in [px] below which we will not apply filtering */

    public boolean sizeDifferenceIsPlausible(double sourceComponentLength, double totalTargetComponentLength) {
        if (sourceComponentLength < minSizeForFiltering && totalTargetComponentLength < minSizeForFiltering) {
            return true; /* The relative size-fluctuations for small components can be very large and noisy. This ensures, that we do not accidentally filter them. */
        }
        double foldChange = totalTargetComponentLength / sourceComponentLength;
        boolean res = foldChange <= trackingConfiguration.getMaximumGrowthRate();
        return res;
    }
}
