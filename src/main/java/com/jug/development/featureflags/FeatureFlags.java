package com.jug.development.featureflags;

public class FeatureFlags {
    /**
     * Feature flags.
     */
    public static final boolean featureFlagUseAssignmentPlausibilityFilter = false; /* Sets whether to filter assignments (currently only mapping-assignments) by their plausibility as determined from the amount of total "cell-area" above/below the source and target components. */
    public static final ComponentCostCalculationMethod featureFlagComponentCost = ComponentCostCalculationMethod.UsingLogLikelihoodCost; /* this controls how the component cost will be calculated */
}
