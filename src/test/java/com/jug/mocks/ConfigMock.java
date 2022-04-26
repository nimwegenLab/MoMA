package com.jug.mocks;

import com.jug.config.IConfiguration;

public class ConfigMock implements IConfiguration {

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
    public double getGurobiMaxOptimalityGap() {
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
}
