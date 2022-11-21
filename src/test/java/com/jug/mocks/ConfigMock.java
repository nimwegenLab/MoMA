package com.jug.mocks;

import com.jug.config.IConfiguration;
import org.apache.commons.lang.NotImplementedException;

public class ConfigMock implements IConfiguration {

    @Override
    public long getBackgroundRoiWidth() {
        throw new NotImplementedException();
    }

    @Override
    public int getFluorescentAssignmentFilterChannel() {
        throw new NotImplementedException();
    }

    @Override
    public double getFluorescentAssignmentFilterNumberOfSigmas() {
        throw new NotImplementedException();
    }

    @Override
    public boolean getFilterAssignmentsUsingFluorescenceFeatureFlag() {
        return false;
    }

    @Override
    public boolean getCrossingConstraintFeatureFlag() {
        return false;
    }

    @Override
    public boolean getMigrationCostFeatureFlag() {
        return false;
    }

    @Override
    public boolean getIsReloading() {
        return false;
    }

    @Override
    public int getMaxTime() {
        return 0;
    }

    @Override
    public int getMinTime() {
        return 0;
    }

    @Override
    public boolean getRunIlpOnChange() {
        return false;
    }

    @Override
    public void setRunIlpOnChange(boolean runOnChange) {

    }

    @Override
    public String getPathForAutosaving() {
        return null;
    }

    @Override
    public double getGurobiTimeLimit() {
        return 0;
    }

    @Override
    public double getGurobiTimeLimitDuringCuration() {
        return 0;
    }

    @Override
    public boolean getIfRunningHeadless() {
        return false;
    }

    @Override
    public int getGlOffsetTop() {
        int GL_OFFSET_TOP = 65;
        return GL_OFFSET_TOP;
    }

    @Override
    public double getComponentExitRange() {
        float COMPONENT_EXIT_RANGE = 50;
        return COMPONENT_EXIT_RANGE;
    }

    @Override
    public float getAssignmentCostCutoff() {
        return Float.MAX_VALUE;
    }

    @Override
    public float getLysisAssignmentCost() {
        return 10.0f;
    }

    @Override
    public int getMaxCellDrop() {
        int MAX_CELL_DROP = 50;
        return MAX_CELL_DROP;
    }

    @Override
    public int getCellDetectionRoiOffsetTop() {
        int CELL_DETECTION_ROI_OFFSET_TOP = 120;
        return CELL_DETECTION_ROI_OFFSET_TOP;
    }

    @Override
    public float getMaximumShrinkagePerFrame() {
        return 0;
    }

    @Override
    public float getMaximumGrowthPerFrame() {
        return 0;
    }
}
