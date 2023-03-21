package com.jug.lp.costs;

import com.jug.util.componenttree.AdvancedComponent;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Pair;

import java.util.List;

/**
 * This class calculates the migration cost based on the total length of components below the source-component and the
 * lower target-component under consideration.
 */
public class MigrationCostCalculatorUsingTotalComponentLengthBelow implements ICostCalculator {
    private CostFactory costFactory;

    public MigrationCostCalculatorUsingTotalComponentLengthBelow(CostFactory costFactory) {
        this.costFactory = costFactory;
    }

    @Override
    public double calculateCost(AdvancedComponent<FloatType> sourceComponent, List<AdvancedComponent<FloatType>> targetComponents) {
        AdvancedComponent<FloatType>lowerTargetComponent = targetComponents.get(0);
        final Pair<Float, float[]> migrationCostOfLowerBoundary =
                costFactory.getMigrationCost(
                        -sourceComponent.getTotalLengthOfComponentsBelow(),
                        -lowerTargetComponent.getTotalLengthOfComponentsBelow()); /* NOTE: We need to pass the negative total cell mass to CostFactory.getMigrationCost(), because getMigrationCost() assumes the y-axis points from the image top towards the bottom (ie. matrix-coordinates as used images). But this is the inverse for the total cell mass.*/
        final float averageMigrationCost = migrationCostOfLowerBoundary.getA();
        return averageMigrationCost;
    }
}
