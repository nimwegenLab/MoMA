package com.jug.config;

public interface IFluorescenceAssignmentFilterConfiguration {
    /**
     * Returns the color channel the fluorescence filter will consider.
     *
     * @return
     */
    int getFluorescenceAssignmentFilterChannel();

    /**
     * This is the maximal allowed intensity ratio, when comparing fluorescence intensities of source- and
     * target-components. Assignments will be deactivated, if the intensity ratio is above this value.
     */
    double getFluorescenceAssignmentFilterIntensityRatioThresholdUpper();

    /**
     * This is the minimal allowed intensity ratio, when comparing fluorescence intensities of source- and
     * target-components. Assignments will be deactivated, if the intensity ratio is below this value.
     */
    double getFluorescenceAssignmentFilterIntensityRatioThresholdLower();

    /**
     * Returns whether to use the fluorescence intensity of components to filter assignments.
     *
     * @return
     */
    boolean getFilterAssignmentsUsingFluorescenceFeatureFlag();
}
