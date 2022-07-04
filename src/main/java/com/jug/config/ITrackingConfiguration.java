package com.jug.config;

/**
 * This is the interface to settings related to the setting up and performing the tracking.
 */
public interface ITrackingConfiguration {
    /**
     * Return the maximal growth-rate that is allowed between the source and target component(s) in an assignment.
     * @return
     */
    double getMaximumGrowthRate();
}
