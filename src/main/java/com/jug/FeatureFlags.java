package com.jug;

public class FeatureFlags {
    /**
     * Feature flags.
     */
    public static boolean featureFlagUseAssignmentPlausibilityFilter = false; /* Sets whether to filter assignments (currently only mapping-assignments) by their plausibility as determined from the amount of total "cell-area" above/below the source and target components. */
    public static boolean featureFlagDisableMaxCellDrop = false; /* Sets assignments are filtered for components which jump backward in the GL by a too high value. */
    public static boolean featureFlagUseComponentCostWithProbabilityMap = false; /* this enables the new method for calculating component-costs using the probability maps */
}
