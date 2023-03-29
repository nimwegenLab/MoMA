package com.jug.lp.costs;

import com.jug.config.IConfiguration;
import com.jug.util.componenttree.AdvancedComponent;
import com.jug.util.componenttree.ComponentInterface;
import net.imglib2.type.numeric.real.FloatType;

public class AssignmentCostCalculatorUsingComponentLength implements IAssignmentCostCalculator {
    private final IConfiguration configuration;
    private ICostFactory costFactory;

    public AssignmentCostCalculatorUsingComponentLength(IConfiguration configuration,
                                                        ICostFactory costFactory) {
        this.configuration = configuration;
        this.costFactory = costFactory;
    }

    private double sizeMismatchCostScalingFactor = 0.1;

    @Override
    public double calculateMappingCost(AdvancedComponent<FloatType> sourceComponent, AdvancedComponent<FloatType> targetComponent) {
        double totalComponentBenefit =
                costFactory.calculateLogLikelihoodComponentCost(sourceComponent)
                + costFactory.calculateLogLikelihoodComponentCost(targetComponent);
        double sizeMismatchCost = sizeMismatchCostScalingFactor *
                Math.abs(
                        (targetComponent.getMajorAxisLength() - sourceComponent.getMajorAxisLength())
                        /
                        sourceComponent.getMajorAxisLength()
                );
        return totalComponentBenefit - sizeMismatchCost;
    }

    @Override
    public double calculateDivisionCost(AdvancedComponent<FloatType> sourceComponent, AdvancedComponent<FloatType> lowerTargetComponent, AdvancedComponent<FloatType> upperTargetComponent) {
        return -1;
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
