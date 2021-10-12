package com.jug.config;

/**
 * This is the interface to settings related to the setting up and performing the tracking.
 */
public interface ITrackingConfiguration {
    /**
     * If this setting is true segment assignments will between components that exceed the maximal growth-rate will not
     * be added to the ILP.
     * @return
     */
    boolean filterAssignmentsByMaximalGrowthRate();

    /**
     * Return the maximal growth-rate that is allowed between the source and target component(s) in an assignment.
     * @return
     */
    double getMaximumGrowthRate();
}
