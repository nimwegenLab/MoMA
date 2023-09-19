package com.jug.lp.costs;

import com.jug.config.IConfiguration;
import com.jug.util.componenttree.AdvancedComponent;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Pair;
import net.imglib2.util.ValuePair;

import java.util.Arrays;

import static com.jug.util.ComponentTreeUtils.getComponentSize;

/**
 * This class implements the legacy methods for calculating the assignment costs.
 * The method for calculating the migration cost is defined by injecting different implementations for
 * {@link AssignmentCostCalculatorLegacy#migrationCostCalculator}.
 */
public class AssignmentCostCalculatorLegacy implements IAssignmentCostCalculator {
    private CostFactory costFactory;
    private ICostCalculator migrationCostCalculator;
    private IConfiguration configurationManager;

    public AssignmentCostCalculatorLegacy(CostFactory costFactory,
                                          ICostCalculator migrationCostCalculator,
                                          IConfiguration configurationManager) {
        this.costFactory = costFactory;
        this.migrationCostCalculator = migrationCostCalculator;
        this.configurationManager = configurationManager;
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

    /**
     * Computes the compatibility-mapping-costs between the two given
     * hypothesis.
     *
     * @param sourceComponent the segmentation hypothesis from which the mapping originates.
     * @return the cost we want to set for the given combination of segmentation
     * hypothesis.
     */
    public Float compatibilityCostOfDivision(
            final AdvancedComponent<FloatType> sourceComponent,
            final AdvancedComponent<FloatType> lowerTargetComponent,
            final AdvancedComponent<FloatType> upperTargetComponent) {

        final ValuePair<Integer, Integer> upperTargetBoundaries = upperTargetComponent.getVerticalComponentLimits();

        final long sourceSize = getComponentSize(sourceComponent, 1);
        final long upperTargetSize = getComponentSize(upperTargetComponent, 1);
        final long lowerTargetSize = getComponentSize(lowerTargetComponent, 1);
        final long summedTargetSize = upperTargetSize + lowerTargetSize;

        double averageMigrationCost = migrationCostCalculator.calculateCost(sourceComponent, Arrays.asList(lowerTargetComponent, upperTargetComponent));

        boolean upperTargetTouchesCellDetectionRoiTop = (upperTargetBoundaries.getA() <= configurationManager.getCellDetectionRoiOffsetTop());

        final Pair<Float, float[]> growthCost = costFactory.getGrowthCost(sourceSize, summedTargetSize, upperTargetTouchesCellDetectionRoiTop);

        float divisionCost = growthCost.getA() + (float) averageMigrationCost;
        return divisionCost;
    }

    /**
     * This method defines how the segmentation costs are influencing the costs
     * of division assignments during the ILP hypotheses substitution takes
     * place.
     *
     * @param sourceComponentCost
     * @param compatibilityCostOfDivision
     * @return
     */
    public float costModulationForSubstitutedILP(
            final float sourceComponentCost,
            final float upperTargetComponentCost,
            final float lowerTargetComponentCost,
            final float compatibilityCostOfDivision) {
        return sourceWeightingFactor * sourceComponentCost + targetWeightingFactor * (upperTargetComponentCost + lowerTargetComponentCost) + compatibilityCostOfDivision;
    }

    @Override
    public double calculateMappingCost(AdvancedComponent<FloatType> sourceComponent,
                                       AdvancedComponent<FloatType> targetComponent) {
        final Float compatibilityCostOfMapping = compatibilityCostOfMapping(sourceComponent, targetComponent);
        return costModulationForSubstitutedILP(sourceComponent.getCost(), targetComponent.getCost(), compatibilityCostOfMapping);
    }

    @Override
    public double calculateDivisionCost(AdvancedComponent<FloatType> sourceComponent,
                                        AdvancedComponent<FloatType> lowerTargetComponent,
                                        AdvancedComponent<FloatType> upperTargetComponent) {
        final Float compatibilityCostOfDivision = compatibilityCostOfDivision(
                sourceComponent,
                lowerTargetComponent,
                upperTargetComponent);

        float cost = costModulationForSubstitutedILP(
                sourceComponent.getCost(),
                upperTargetComponent.getCost(),
                lowerTargetComponent.getCost(),
                compatibilityCostOfDivision);
        return cost;
    }

    @Override
    public double calculateExitCost(AdvancedComponent<FloatType> sourceComponent) {
        return configurationManager.getExitAssignmentCost();
    }

    @Override
    public double calculateLysisCost(AdvancedComponent<FloatType> sourceComponent) {
        return configurationManager.getLysisAssignmentCost();
    }

    @Override
    public double calculateEnterCost(AdvancedComponent<FloatType> targetComponent) {
        return configurationManager.getEnterAssignmentCost();
    }
}