package com.jug.lp;

/**
 * Methods in this class are used to test whether the combination of components in assignments are physically plausible.
 * They are used to determine whether an assignment should be added to the optimizer.
 */
public class AssignmentPlausibilityTester {

    double growthRateInFrames = 0;

    private double sizeDifferenceThreshold;

    private int shortestDoublingTimeInFrames;

    public AssignmentPlausibilityTester(int shortestDoublingTimeInFrames) {
        this.shortestDoublingTimeInFrames = shortestDoublingTimeInFrames;
        setShortestDoublingTimeInFrames(shortestDoublingTimeInFrames);
    }

    /**
     * Test if the size difference between the source component and the total size of the target components is plausible.
     * @param sourceComponentSize
     * @param totalTargetComponentSize
     * @return
     */
    public boolean sizeDifferenceIsPlausible(long sourceComponentSize, long totalTargetComponentSize) {
        double foldChange = ((double) totalTargetComponentSize) / ((double) sourceComponentSize);
        boolean res = foldChange <= sizeDifferenceThreshold;
        return res;
    }

    public void setShortestDoublingTimeInFrames(int shortestDoublingTimeInFrames) {
        this.shortestDoublingTimeInFrames = shortestDoublingTimeInFrames;
        this.growthRateInFrames = Math.log(2) / ((double) shortestDoublingTimeInFrames);
        this.sizeDifferenceThreshold = Math.exp(growthRateInFrames * 1); /* *1 is symbolic and represent the single frame time step for which we calculate the change; i.e.: N_t/N_0 = exp(growthRate*t) */
    }

    public double getGrowthRateInFrames() {
        return growthRateInFrames;
    }
}
