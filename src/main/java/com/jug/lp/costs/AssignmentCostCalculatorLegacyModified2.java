package com.jug.lp.costs;

import com.jug.config.IConfiguration;
import com.jug.util.componenttree.AdvancedComponent;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Pair;
import net.imglib2.util.ValuePair;

import java.util.ArrayList;
import java.util.List;

/**
 * This class implements the legacy methods for calculating the assignment costs.
 */
public class AssignmentCostCalculatorLegacyModified2 implements IAssignmentCostCalculator {
    private IConfiguration configurationManager;

    int offsetForDetectingIfCellTouchesRoiTop = 15;

    /**
     * If the size of a component at the detection ROI border is below this threshold, it is considered to be too small
     * to be correctly detected by U-Net.
     */
    private float sizeThresholdForComponentsAtDetectionRoiTop = 15;

    private double maxAssignmentCost = 10.0;

    public AssignmentCostCalculatorLegacyModified2(IConfiguration configurationManager) {
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
//        final long sourceComponentSize = getComponentSize(sourceComponent, 1);
//        final long targetComponentSize = getComponentSize(targetComponent, 1);
//        final float sourceComponentSize = (float)sourceComponent.getMajorAxisLength();
//        final float targetComponentSize = (float)targetComponent.getMajorAxisLength();
        final float sourceComponentSize = (float) sourceComponent.getOrientedBoundingBoxProperties().getHeight();
        final float targetComponentSize = (float) targetComponent.getOrientedBoundingBoxProperties().getHeight();

        final ValuePair<Integer, Integer> targetComponentBoundaries = targetComponent.getVerticalComponentLimits();

//        double averageMigrationCost = migrationCostCalculator.calculateCost(sourceComponent, Arrays.asList(targetComponent));
//        double averageMigrationCost = calculateMigrationCostForMapping(sourceComponent, targetComponent);
//        double averageMigrationCost = calculateMigrationCostUsingTotalCellLengthBelow(sourceComponent, targetComponent);
        double averageMigrationCost = calculateMigrationCostUsingTotalCellAreaBelow(sourceComponent, targetComponent);

        final Pair<Float, float[]> growthCost = this.getGrowthCost(sourceComponentSize, targetComponentSize);

        float mappingCost = growthCost.getA() + (float)averageMigrationCost;
        return mappingCost;
    }

    private double calculateMigrationCostForMapping(AdvancedComponent<FloatType> sourceComponent, AdvancedComponent<FloatType> targetComponent) {
        final ValuePair<Integer, Integer> sourceComponentBoundaries = sourceComponent.getVerticalComponentLimits();
        final ValuePair<Integer, Integer> targetComponentBoundaries = targetComponent.getVerticalComponentLimits();

        final float sourceUpperBoundary = sourceComponentBoundaries.getA();
        final float sourceLowerBoundary = sourceComponentBoundaries.getB();
        final float targetUpperBoundary = targetComponentBoundaries.getA();
        final float targetLowerBoundary = targetComponentBoundaries.getB();

        float averageMigrationCost = 0;
        final Pair<Float, float[]> migrationCostOfUpperBoundary = this.getMigrationCost(sourceUpperBoundary, targetUpperBoundary);
        final Pair<Float, float[]> migrationCostOfLowerBoundary = this.getMigrationCost(sourceLowerBoundary, targetLowerBoundary);
        averageMigrationCost = 0.5f * migrationCostOfLowerBoundary.getA() + 0.5f * migrationCostOfUpperBoundary.getA();

        return averageMigrationCost;
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

//        final long sourceSize = getComponentSize(sourceComponent, 1);
//        final long upperTargetSize = getComponentSize(upperTargetComponent, 1);
//        final long lowerTargetSize = getComponentSize(lowerTargetComponent, 1);

//        final float sourceSize = (float) sourceComponent.getMajorAxisLength();
//        final float upperTargetSize = (float) upperTargetComponent.getMajorAxisLength();
//        final float lowerTargetSize = (float) lowerTargetComponent.getMajorAxisLength();

        final float sourceSize = (float) sourceComponent.getOrientedBoundingBoxProperties().getHeight();
        final float upperTargetSize = (float) upperTargetComponent.getOrientedBoundingBoxProperties().getHeight();
        final float lowerTargetSize = (float) lowerTargetComponent.getOrientedBoundingBoxProperties().getHeight();

        final float summedTargetSize = upperTargetSize + lowerTargetSize;

//        double averageMigrationCost = migrationCostCalculator.calculateCost(sourceComponent, Arrays.asList(lowerTargetComponent, upperTargetComponent));
//        double averageMigrationCost = this.calculateMigrationCostForDivision(sourceComponent, lowerTargetComponent, upperTargetComponent);
//        double averageMigrationCost = calculateMigrationCostUsingTotalCellLengthBelow(sourceComponent, lowerTargetComponent);
        double averageMigrationCost = calculateMigrationCostUsingTotalCellAreaBelow(sourceComponent, lowerTargetComponent);

        final Pair<Float, float[]> growthCost = this.getGrowthCost(sourceSize, summedTargetSize);

        float divisionCost = growthCost.getA() + (float) averageMigrationCost;
        return divisionCost;
    }

    private float calculateMigrationCostForDivision(AdvancedComponent<FloatType> sourceComponent,
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
        final Pair<Float, float[]> migrationCostOfUpperBoundary = this.getMigrationCost(sourceUpperBoundary, upperTargetUpperBoundary);
        final Pair<Float, float[]> migrationCostOfLowerBoundary = this.getMigrationCost(sourceLowerBoundary, lowerTargetLowerBoundary);
        averageMigrationCost = .5f * migrationCostOfLowerBoundary.getA() + .5f * migrationCostOfUpperBoundary.getA();
//        }
        return averageMigrationCost;
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

        if (componentIsCloseToDetectionRoiTop(targetComponent) && componentIsTooSmallAtDetectionRoiTop(targetComponent)) {
            return maxAssignmentCost;
        }

//        double targetComponentCost = calculateCostOfComponentThatCanTouchBorder(targetComponent);
        double targetComponentCost = targetComponent.getCost();

        return costModulationForSubstitutedILP(sourceComponent.getCost(), (float) targetComponentCost, compatibilityCostOfMapping);
    }

    @Override
    public double calculateDivisionCost(AdvancedComponent<FloatType> sourceComponent,
                                        AdvancedComponent<FloatType> lowerTargetComponent,
                                        AdvancedComponent<FloatType> upperTargetComponent) {
        final Float compatibilityCostOfDivision = compatibilityCostOfDivision(
                sourceComponent,
                lowerTargetComponent,
                upperTargetComponent);

//        double upperComponentCost = calculateCostOfComponentThatCanTouchBorder(upperTargetComponent);
        double upperComponentCost = upperTargetComponent.getCost();

        float cost = costModulationForSubstitutedILP(
                sourceComponent.getCost(),
                (float) upperComponentCost,
                lowerTargetComponent.getCost(),
                compatibilityCostOfDivision);

        if (componentIsCloseToDetectionRoiTop(upperTargetComponent) && componentIsTooSmallAtDetectionRoiTop(upperTargetComponent)) {
            return maxAssignmentCost;
        }

        return cost;
    }

    private double calculateCostOfComponentThatCanTouchBorder(AdvancedComponent<FloatType> component) {
        double upperChildCost = 0;
        if (componentIsCloseToDetectionRoiTop(component) && !component.getChildren().isEmpty()) { /* component has child components, which could be smaller than the threshold for component size */
            List<AdvancedComponent> upperChildren = recursivelyGetListOfUpperChildrenInTree(new ArrayList<>(), component); /* list of upper child components in component tree */
            for (AdvancedComponent<FloatType> upperChild : upperChildren) {
                if (componentIsTooSmallAtDetectionRoiTop(upperChild) && componentIsCloseToDetectionRoiTop(upperChild)) {
                    upperChildCost = upperChild.getCost();
                }
            }
        }
        return component.getCost() - upperChildCost;
    }

    private List<AdvancedComponent> recursivelyGetListOfUpperChildrenInTree(List<AdvancedComponent> componentList, AdvancedComponent<FloatType> component) {
        if (component.getNumberOfChildren() < 1) {
            return componentList;
        }
        AdvancedComponent<FloatType> upperChild = component.getChildren().get(0);
        componentList.add(upperChild);
        return recursivelyGetListOfUpperChildrenInTree(componentList, upperChild);
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
        if (componentIsCloseToDetectionRoiTop(targetComponent) && componentIsTooSmallAtDetectionRoiTop(targetComponent)) {
            return 0;
        }
        return configurationManager.getEnterAssignmentCost();
    }

    /**
     * Code below was copied from CostFactory.java and adapted to improve tracking performance.
     */
    private Pair<Float, float[]> getMigrationCost(final float sourcePosition, final float targetPosition) {
        float scaledPositionDifference = (sourcePosition - targetPosition) / normalizer;
        float exponent;
        float migrationCost;
        if (scaledPositionDifference > 0) { // upward migration
            scaledPositionDifference = Math.max(0, scaledPositionDifference - 0.05f); // going upwards for up to 5% is for free...
            exponent = 3.0f;
        } else { // downward migration
//            Math.max(0, scaledPositionDifference - 0.01f);  // going downwards for up to 1% is for free...
            scaledPositionDifference = Math.min(0, scaledPositionDifference + 0.05f); // going upwards for up to 5% is for free...
            exponent = 3.0f;
        }
        scaledPositionDifference = Math.abs(scaledPositionDifference);
        migrationCost = scaledPositionDifference * (float) Math.pow(1 + scaledPositionDifference, exponent);
        return new ValuePair<>(migrationCost, new float[]{migrationCost});
    }

    private float normalizer = 340; /* TODO-MM-20191111: This fixed parameter was added to remove dependence on
	the length of the growthlane, which was previously passed as normalizer to the functions, that use this.
	It should be removed in favor of having costs based on relative growth and/or movement at some point.
	NOTE: 340px is roughly the length of the GL, when Florian Jug designed the cost functions, so that is, the value that
	we are keeping for the moment.*/

    private Pair<Float, float[]> getGrowthCost(final float sourceSize, final float targetSize) {
        float scaledSizeDifference = (targetSize - sourceSize) / normalizer; /* TODO-MM-20191119: here we scale the size change with typical GL length; this does not make sense; it makes more sense to look at the relative size change */
        float exponent;
        if (scaledSizeDifference > 0) { // growth
            scaledSizeDifference = Math.max(0, scaledSizeDifference - 0.05f); // growing up 5% is free
            exponent = 4.0f;
        } else { // shrinkage
            scaledSizeDifference = Math.min(0, scaledSizeDifference + 0.05f); // shrinking up 5% is free
            exponent = 4.0f;
        }
        scaledSizeDifference = Math.abs(scaledSizeDifference);

        float growthCost = scaledSizeDifference * (float) Math.pow(1 + scaledSizeDifference, exponent); // since deltaL is <1 we add 1 before taking its power

        return new ValuePair<>(growthCost, new float[]{growthCost});
    }

    public double calculateMigrationCostUsingTotalCellLengthBelow(AdvancedComponent<FloatType> sourceComponent, AdvancedComponent<FloatType> lowerTargetComponent) {
        int sourceComponentTotalCellLengthBelow = sourceComponent.getTotalLengthOfComponentsBelow();
        int targetComponentTotalCellLengthBelow = lowerTargetComponent.getTotalLengthOfComponentsBelow();

        final Pair<Float, float[]> migrationCostOfLowerBoundary =
                getMigrationCost(
                        -sourceComponentTotalCellLengthBelow,
                        -targetComponentTotalCellLengthBelow); /* NOTE: We need to pass the negative total cell mass to CostFactory.getMigrationCost(), because getMigrationCost() assumes the y-axis points from the image top towards the bottom (ie. matrix-coordinates as used images). But this is the inverse for the total cell mass.*/
        final float averageMigrationCost = migrationCostOfLowerBoundary.getA();
        return averageMigrationCost;
    }

    public double calculateMigrationCostUsingTotalCellAreaBelow(AdvancedComponent<FloatType> sourceComponent, AdvancedComponent<FloatType> lowerTargetComponent) {
        float averageCellWidth = 8; /* average cell width in [px]; we use it to scale the cell area to a roughly corresponding cell length */

        float sourceComponentTotalCellLengthBelow = ((float) sourceComponent.getTotalAreaOfComponentsBelow()) / averageCellWidth;
        float targetComponentTotalCellLengthBelow = ((float) lowerTargetComponent.getTotalAreaOfComponentsBelow()) / averageCellWidth;

        final Pair<Float, float[]> migrationCostOfLowerBoundary =
                getMigrationCost(
                        -sourceComponentTotalCellLengthBelow,
                        -targetComponentTotalCellLengthBelow); /* NOTE: We need to pass the negative total cell mass to CostFactory.getMigrationCost(), because getMigrationCost() assumes the y-axis points from the image top towards the bottom (ie. matrix-coordinates as used images). But this is the inverse for the total cell mass.*/
        final float averageMigrationCost = migrationCostOfLowerBoundary.getA();
        return averageMigrationCost;
    }

    private boolean componentIsCloseToDetectionRoiTop(AdvancedComponent<FloatType> component) {
        Integer componentBoundaryTop = component.getVerticalComponentLimits().getA();
        return (componentBoundaryTop <= configurationManager.getCellDetectionRoiOffsetTop() + offsetForDetectingIfCellTouchesRoiTop);
    }

    private boolean componentIsTooSmallAtDetectionRoiTop(AdvancedComponent<FloatType> component) {
        final float sourceComponentSize = (float) component.getOrientedBoundingBoxProperties().getHeight();
        return sourceComponentSize < sizeThresholdForComponentsAtDetectionRoiTop;
    }
}
