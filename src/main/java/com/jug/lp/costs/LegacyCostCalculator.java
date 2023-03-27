package com.jug.lp.costs;

import com.jug.config.IConfiguration;
import com.jug.util.componenttree.AdvancedComponent;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Pair;
import net.imglib2.util.ValuePair;
import org.apache.commons.lang.NotImplementedException;

import java.util.Arrays;
import java.util.List;

import static com.jug.util.ComponentTreeUtils.getComponentSize;

public class LegacyCostCalculator implements ICostCalculator {
    private CostFactory costFactory;
    private ICostCalculator migrationCostCalculator;
    private IConfiguration configurationManager;

    public LegacyCostCalculator(CostFactory costFactory,
                                ICostCalculator migrationCostCalculator,
                                IConfiguration configurationManager) {
        this.costFactory = costFactory;
        this.migrationCostCalculator = migrationCostCalculator;
        this.configurationManager = configurationManager;
    }

    @Override
    public double calculateCost(AdvancedComponent<FloatType> sourceComponent,
                                List<AdvancedComponent<FloatType>> targetComponents) {
        if (targetComponents.size() == 1) {
            return calculateCostForMapping(sourceComponent, targetComponents.get(0));
        }
        throw new RuntimeException(String.format("Cost calculation is not defined for the number of targetComponents that was passed (=%d).", targetComponents.size()));
    }

    private double calculateCostForMapping(AdvancedComponent<FloatType> sourceComponent, AdvancedComponent<FloatType> targetComponent) {
        final Float compatibilityCostOfMapping = compatibilityCostOfMapping(sourceComponent, targetComponent);
        return costModulationForSubstitutedILP(sourceComponent.getCost(), targetComponent.getCost(), compatibilityCostOfMapping);
    }

    /**
     * Computes the compatibility-mapping-costs between the two given
     * hypothesis.
     *
     * @param sourceComponent the segmentation hypothesis from which the mapping originates.
     * @param targetComponent the segmentation hypothesis towards which the
     *                        mapping-assignment leads.
     * @return the cost we want to set for the given combination of segmentation
     * hypothesis.
     */
    public Float compatibilityCostOfMapping(
            final AdvancedComponent<FloatType> sourceComponent,
            final AdvancedComponent<FloatType> targetComponent) {
        final long sourceComponentSize = getComponentSize(sourceComponent, 1);
        final long targetComponentSize = getComponentSize(targetComponent, 1);

        final ValuePair<Integer, Integer> targetComponentBoundaries = targetComponent.getVerticalComponentLimits();

        double averageMigrationCost = migrationCostCalculator.calculateCost(sourceComponent, Arrays.asList(targetComponent));

        boolean targetTouchesCellDetectionRoiTop = (targetComponentBoundaries.getA() <= configurationManager.getCellDetectionRoiOffsetTop());

        final Pair<Float, float[]> growthCost = costFactory.getGrowthCost(sourceComponentSize, targetComponentSize, targetTouchesCellDetectionRoiTop);

        float mappingCost = growthCost.getA() + (float)averageMigrationCost;
        return mappingCost;
    }

    /**
     * This method defines how the segmentation costs are influencing the costs
     * of mapping assignments during the ILP hypotheses substitution takes
     * place.
     *
     * @param sourceComponentCost
     * @param targetComponentCost
     * @param mappingCosts
     * @return
     */
    public float costModulationForSubstitutedILP(
            final float sourceComponentCost,
            final float targetComponentCost,
            final float mappingCosts) {
        return sourceWeightingFactor * sourceComponentCost + targetWeightingFactor * targetComponentCost + mappingCosts; /* here again we fold the costs from the nodes into the corresponding assignment;
																  we should probably do 50%/50%, but we did different and it's ok */
    }

    private float sourceWeightingFactor = 0.5f;

    private float targetWeightingFactor = (1 - sourceWeightingFactor);
}
