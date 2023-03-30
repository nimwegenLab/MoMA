package com.jug.lp.costs;

import com.jug.config.IConfiguration;
import com.jug.util.componenttree.AdvancedComponent;
import net.imglib2.type.numeric.real.FloatType;

import java.util.List;

public class AssignmentCostCalculatorUsingComponentLength implements IAssignmentCostCalculator {
    private final IConfiguration configuration;
    private final ICostFactory costFactory;

    public AssignmentCostCalculatorUsingComponentLength(IConfiguration configuration,
                                                        ICostFactory costFactory) {
        this.configuration = configuration;
        this.costFactory = costFactory;
    }

    private final double sizeMismatchCostScalingFactor = 0.1;
    private final double positionMismatchCostScalingFactor = 0.1;

    @Override
    public double calculateMappingCost(AdvancedComponent<FloatType> sourceComponent, AdvancedComponent<FloatType> targetComponent) {
        double totalComponentBenefit = sourceComponent.getCost() + targetComponent.getCost(); /* TODO-MM-20230329:
        This only returns floating precision. I should use costFactory.calculateLogLikelihoodComponentCost to
        get double precision, but that does not cache the results, which hurts performance; i.e.:
        double totalComponentBenefit =
                   costFactory.calculateLogLikelihoodComponentCost(sourceComponent)
                    + costFactory.calculateLogLikelihoodComponentCost(targetComponent);
        */
        double sizeMismatchCost = calculateSizeMismatchCost(getLength(sourceComponent), getLength(targetComponent));
        double positionMismatchCost = calculatePositionMismatchCostForMapping(sourceComponent, targetComponent);
        double cost = totalComponentBenefit + sizeMismatchCost + positionMismatchCost;
        return cost;
    }

    private double calculateSizeMismatchCost(double sourceComponentLength,
                                             double totalTargetComponentLength) {
        return sizeMismatchCostScalingFactor *
                Math.abs(
                        relativeChangeToSourceValue(
                                sourceComponentLength,
                                totalTargetComponentLength)
                );
    }

    private final double delta = 1;
    private final double typicalCellLengthInPixel = 1 / 0.065; /* TODO-MM-20230630: This magic number needs to be replaced with values, that are read from the config-file; namely the pixel-size and the typical cell-size after division. */

    private double calculatePositionMismatchCostForMapping(AdvancedComponent<FloatType> sourceComponent,
                                                           AdvancedComponent<FloatType> targetComponent) {
        double totalComponentLengthBelowSource = calculatedTotalComponentLengthBelow(sourceComponent);
        double totalComponentLengthBelowTarget = calculatedTotalComponentLengthBelow(targetComponent);

        if (totalComponentLengthBelowSource < delta && totalComponentLengthBelowTarget < delta) {
            return 0.0;
        } else if (totalComponentLengthBelowSource < delta && totalComponentLengthBelowTarget >= delta) {
            return totalComponentLengthBelowTarget / typicalCellLengthInPixel;
        }

        double cost = positionMismatchCostScalingFactor
                * Math.abs(
                relativeChangeToSourceValue(totalComponentLengthBelowSource, totalComponentLengthBelowTarget)
        );
        if (Double.isNaN(cost)) {
            throw new RuntimeException("NaN cost");
        }
        if (Double.isInfinite(cost)) {
            throw new RuntimeException("NaN cost");
        }
        return cost;
    }

    private double relativeChangeToSourceValue(double sourceValue, double targetValue) {
        return (targetValue - sourceValue) / sourceValue;
    }

    private double calculatedTotalComponentLengthBelow(AdvancedComponent<FloatType> component) {
        List<AdvancedComponent<FloatType>> componentsBelow = component.getComponentsBelowClosestToRoot();
        double totalLength = componentsBelow.stream()
                .map(cmp -> cmp.getMajorAxisLength())
                .reduce(0.0, (acc, length) -> acc + length);
        return totalLength;
    }

    @Override
    public double calculateDivisionCost(AdvancedComponent<FloatType> sourceComponent, AdvancedComponent<FloatType> lowerTargetComponent, AdvancedComponent<FloatType> upperTargetComponent) {
        double totalComponentBenefit = sourceComponent.getCost() + lowerTargetComponent.getCost() + upperTargetComponent.getCost(); /* TODO-MM-20230329:
        This only returns floating precision. I should use costFactory.calculateLogLikelihoodComponentCost to
        get double precision, but that does not cache the results, which hurts performance; i.e.:
        double totalComponentBenefit =
                   costFactory.calculateLogLikelihoodComponentCost(sourceComponent)
                    + costFactory.calculateLogLikelihoodComponentCost(lowerTargetComponent);
                    + costFactory.calculateLogLikelihoodComponentCost(upperTargetComponent);
        */
        double sizeMismatchCost = calculateSizeMismatchCost(getLength(sourceComponent), getLength(lowerTargetComponent) + getLength(upperTargetComponent));
        double positionMismatchCost = calculatePositionMismatchCostForMapping(sourceComponent, lowerTargetComponent);
        double cost = totalComponentBenefit + sizeMismatchCost + positionMismatchCost;
        return cost;
    }

    private static double getLength(AdvancedComponent<FloatType> component) {
        return component.getMajorAxisLength();
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
