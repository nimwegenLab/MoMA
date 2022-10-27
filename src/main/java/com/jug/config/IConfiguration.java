package com.jug.config;

public interface IConfiguration {
    /**
     * Returns whether crossing constraints should be used.
     * @return feature flag
     */
    boolean getCrossingConstraintFeatureFlag();

    /**
     * Returns whether migration costs should be used.
     * @return feature flag
     */
    boolean getMigrationCostFeatureFlag();

    /**
     * Return a GL is being load. This modifies the startup behavior of MoMA.
     */
    boolean getIsReloading();

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
     * Returns the time limit or maximum duration for the Gurobi optimization.
     * @return the time limit in seconds
     */
    double getGurobiTimeLimit();

    /**
     * Returns the time limit or maximum duration for the Gurobi optimization during curation. This allows the curation
     * process to be more responsive for cases, where optimization takes long.
     * @return time limit in seconds
     */
    double getGurobiTimeLimitDuringCuration();

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
