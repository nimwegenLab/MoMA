package com.jug.lp.costs;

import com.jug.config.IConfiguration;
import com.jug.util.componenttree.AdvancedComponent;
import net.imglib2.type.numeric.real.FloatType;

public class AssignmentCostCalculatorUsingComponentLength implements IAssignmentCostCalculator {
    private final IConfiguration configuration;

    public AssignmentCostCalculatorUsingComponentLength(IConfiguration configuration) {
        this.configuration = configuration;
    }

    @Override
    public double calculateMappingCost(AdvancedComponent<FloatType> sourceComponent, AdvancedComponent<FloatType> targetComponent) {
        return 0;
    }

    @Override
    public double calculateDivisionCost(AdvancedComponent<FloatType> sourceComponent, AdvancedComponent<FloatType> lowerTargetComponent, AdvancedComponent<FloatType> upperTargetComponent) {
        return 0;
    }

    @Override
    public double calculateExitCost(AdvancedComponent<FloatType> sourceComponent) {
        return configuration.getExitAssignmentCost();
    }

    @Override
    public double calculateLysisCost(AdvancedComponent<FloatType> sourceComponent) {
        return configuration.getLysisAssignmentCost();
    }

    @Override
    public double calculateEnterCost(AdvancedComponent<FloatType> targetComponent) {
        return configuration.getEnterAssignmentCost();
    }
}
