package com.jug.lp.costs;

import com.jug.util.componenttree.AdvancedComponent;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Pair;
import net.imglib2.util.ValuePair;

import java.util.List;

/**
 * This class implements the legacy method for calculating the migration cost.
 */
public class MigrationCostCalculatorLegacy implements ICostCalculator {
    private CostFactory costFactory;

    public MigrationCostCalculatorLegacy(CostFactory costFactory) {
        this.costFactory = costFactory;
    }

    @Override
    public double calculateCost(AdvancedComponent<FloatType> sourceComponent, List<AdvancedComponent<FloatType>> targetComponents) {
        if (targetComponents.size() == 1) {
            return calculateCostForMapping(sourceComponent, targetComponents.get(0));
        }
        if (targetComponents.size() == 2) {
            return calculateCostForDivision(sourceComponent, targetComponents.get(0), targetComponents.get(1));
        }

        throw new RuntimeException(String.format("Migration cost calculation is not defined for the number of targetComponents that was passed (=%d).", targetComponents.size()));
    }

    private double calculateCostForMapping(AdvancedComponent<FloatType> sourceComponent, AdvancedComponent<FloatType> targetComponent) {
        final ValuePair<Integer, Integer> sourceComponentBoundaries = sourceComponent.getVerticalComponentLimits();
        final ValuePair<Integer, Integer> targetComponentBoundaries = targetComponent.getVerticalComponentLimits();

        final float sourceUpperBoundary = sourceComponentBoundaries.getA();
        final float sourceLowerBoundary = sourceComponentBoundaries.getB();
        final float targetUpperBoundary = targetComponentBoundaries.getA();
        final float targetLowerBoundary = targetComponentBoundaries.getB();

        float averageMigrationCost = 0;
        final Pair<Float, float[]> migrationCostOfUpperBoundary = costFactory.getMigrationCost(sourceUpperBoundary, targetUpperBoundary);
        final Pair<Float, float[]> migrationCostOfLowerBoundary = costFactory.getMigrationCost(sourceLowerBoundary, targetLowerBoundary);
        averageMigrationCost = 0.5f * migrationCostOfLowerBoundary.getA() + 0.5f * migrationCostOfUpperBoundary.getA();

        return averageMigrationCost;
    }

    private float calculateCostForDivision(AdvancedComponent<FloatType> sourceComponent,
                                           AdvancedComponent<FloatType> lowerTargetComponent,
                                           AdvancedComponent<FloatType> upperTargetComponent) {
//        AdvancedComponent<FloatType> lowerTargetComponent = targetComponents.get(0);
//        AdvancedComponent<FloatType> upperTargetComponent = targetComponents.get(1);

        final ValuePair<Integer, Integer> sourceBoundaries = sourceComponent.getVerticalComponentLimits();
        final ValuePair<Integer, Integer> upperTargetBoundaries = upperTargetComponent.getVerticalComponentLimits();
        final ValuePair<Integer, Integer> lowerTargetBoundaries = lowerTargetComponent.getVerticalComponentLimits();

        final float sourceUpperBoundary = sourceBoundaries.getA();
        final float sourceLowerBoundary = sourceBoundaries.getB();
        final float upperTargetUpperBoundary = upperTargetBoundaries.getA();
        final float lowerTargetLowerBoundary = lowerTargetBoundaries.getB();

        float averageMigrationCost = 0;
//        if(configurationManager.getMigrationCostFeatureFlag()){
            final Pair<Float, float[]> migrationCostOfUpperBoundary = costFactory.getMigrationCost(sourceUpperBoundary, upperTargetUpperBoundary);
            final Pair<Float, float[]> migrationCostOfLowerBoundary = costFactory.getMigrationCost(sourceLowerBoundary, lowerTargetLowerBoundary);
            averageMigrationCost = .5f * migrationCostOfLowerBoundary.getA() + .5f * migrationCostOfUpperBoundary.getA();
//        }
        return averageMigrationCost;
    }
}
