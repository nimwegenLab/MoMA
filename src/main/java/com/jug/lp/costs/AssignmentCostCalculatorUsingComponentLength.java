package com.jug.lp.costs;

import com.jug.config.IConfiguration;
import com.jug.util.componenttree.AdvancedComponent;
import net.imglib2.type.numeric.real.FloatType;

import java.util.List;

public class AssignmentCostCalculatorUsingComponentLength implements IAssignmentCostCalculator {
    private final IConfiguration configuration;
    private ICostFactory costFactory;

    public AssignmentCostCalculatorUsingComponentLength(IConfiguration configuration,
                                                        ICostFactory costFactory) {
        this.configuration = configuration;
        this.costFactory = costFactory;
    }

    private double sizeMismatchCostScalingFactor = 0.1;
    private double positionMismatchCostScalingFactor = 0.1;

    @Override
    public double calculateMappingCost(AdvancedComponent<FloatType> sourceComponent, AdvancedComponent<FloatType> targetComponent) {
//        return -1.0;
        double totalComponentBenefit = sourceComponent.getCost() + targetComponent.getCost(); /* TODO-MM-20230329: This only returns floating precision. I should use costFactory.calculateLogLikelihoodComponentCost to get double precision, but that does not cache the results, which hurts performance. */
//        double totalComponentBenefit =
//                costFactory.calculateLogLikelihoodComponentCost(sourceComponent)
//                + costFactory.calculateLogLikelihoodComponentCost(targetComponent);
        double sizeMismatchCost = calculateSizeMismatchCostForMapping(sourceComponent, targetComponent);
        double positionMismatchCost = calculatePositionMismatchCostForMapping(sourceComponent, targetComponent);
//        double cost = totalComponentBenefit + sizeMismatchCost + positionMismatchCost;
        double cost = totalComponentBenefit + sizeMismatchCost + positionMismatchCost;
        return cost;
//        return totalComponentBenefit + sizeMismatchCost + positionMismatchCost;
//        return totalComponentBenefit;
    }

    private double calculateSizeMismatchCostForMapping(AdvancedComponent<FloatType> sourceComponent,
                                                       AdvancedComponent<FloatType> targetComponent) {
        return sizeMismatchCostScalingFactor *
                Math.abs(
                        relativeChangeToSourceValue(
                                sourceComponent.getMajorAxisLength(),
                                targetComponent.getMajorAxisLength())
                );
    }

    private double calculatePositionMismatchCostForMapping(AdvancedComponent<FloatType> sourceComponent,
                                                           AdvancedComponent<FloatType> targetComponent) {
        double totalComponentLengthBelowSource = calculatedTotalComponentLengthBelow(sourceComponent);
        double totalComponentLengthBelowTarget = calculatedTotalComponentLengthBelow(targetComponent);
        return positionMismatchCostScalingFactor
                * Math.abs(
                        relativeChangeToSourceValue(totalComponentLengthBelowSource, totalComponentLengthBelowTarget)
        );
    }

    private double relativeChangeToSourceValue(double sourceValue, double targetValue) {
        return (targetValue - sourceValue) / sourceValue;
    }

    private double calculatedTotalComponentLengthBelow(AdvancedComponent<FloatType> component){
        List<AdvancedComponent<FloatType>> componentsBelow = component.getComponentsBelowClosestToRoot();
        double totalLength = componentsBelow.stream()
                .map(cmp -> cmp.getMajorAxisLength())
                .reduce(0.0, (acc, length) -> acc + length);
        return totalLength;
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
