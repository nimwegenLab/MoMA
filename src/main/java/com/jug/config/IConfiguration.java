package com.jug.config;

public interface IConfiguration {
    /**
     * Get the maximum value of the time-range that will be analyzed.
     */
    int getMaxTime();

    /**
     * Get the maximum value of the time-range that will be analyzed.
     */
    int getMinTime();

    /**
     * Gets if the optimization should be run on each change to it performed by the user. If not, it will only run, once
     * the user selects this action.
     * @return if it this is active
     */
    boolean getRunIlpOnChange();

    /**
     * Sets if the optimization should be run on each change to it performed by the user. If not, it will only run, once
     * the user selects this action.
     * @param runOnChange if it this is active
     * @return
     */
    void setRunIlpOnChange(boolean runOnChange);

    /**
     * Returns the path to the file for auto saving. Auto saving is done before each optimization.
     * @return path to
     */
    String getPathForAutosaving();

    /**
     * Returns the time limit or maximum duration for the Gurobi optimization as set in the configuration.
     * @return the time limit in seconds
     */
    double getGurobiTimeLimit();

    /**
     * Returns the maximum allowed optimality gap below which optimization can/will be terminated by the Gurobi
     * optimizer.
     * @return optimality gap in percent
     */
    double getGurobiMaxOptimalityGap();

    /**
     * Returns if MoMA is being executed without GUI (i.e. in headless mode).
     * @return
     */
    boolean getIfRunningHeadless();

    int getGlOffsetTop();

    double getComponentExitRange();

    float getAssignmentCostCutoff();

    float getLysisAssignmentCost();

    int getMaxCellDrop();

    int getCellDetectionRoiOffsetTop();

    float getMaximumShrinkagePerFrame();

    float getMaximumGrowthPerFrame();
}
