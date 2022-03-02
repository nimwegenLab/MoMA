package com.jug.lp.costs;

import com.jug.config.ConfigurationManager;
import com.jug.development.featureflags.ComponentCostCalculationMethod;
import com.jug.util.componenttree.AdvancedComponent;
import com.jug.util.componenttree.ComponentInterface;
import net.imglib2.algorithm.componenttree.Component;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Pair;
import net.imglib2.util.ValuePair;
import org.apache.commons.lang.NotImplementedException;

import java.util.List;
import java.util.stream.Collectors;

import static com.jug.development.featureflags.FeatureFlags.featureFlagComponentCost;
import static com.jug.util.ComponentTreeUtils.getComponentSize;

/**
 * @author jug
 */
public class CostFactory {
	private static float normalizer = 340; /* TODO-MM-20191111: This fixed parameter was added to remove dependence on
	the length of the growthlane, which was previously passed as normalizer to the functions, that use this.
	It should be removed in favor of having costs based on relative growth and/or movement at some point.
	NOTE: 340px is roughly the length of the GL, when Florian Jug designed the cost functions, so that is, the value that
	we are keeping for the moment.*/

	public static Pair< Float, float[] > getMigrationCost( final float sourcePosition, final float targetPosition ) {
		float scaledPositionDifference = ( sourcePosition - targetPosition ) / normalizer;
		float exponent;
		float migrationCost;
		if ( scaledPositionDifference > 0 ) { // upward migration
			scaledPositionDifference = Math.max( 0, scaledPositionDifference - 0.05f ); // going upwards for up to 5% is for free...
			exponent = 3.0f;
		} else { // downward migration
			Math.max( 0, scaledPositionDifference - 0.01f );  // going downwards for up to 1% is for free...
			exponent = 6.0f;
		}
		scaledPositionDifference = Math.abs( scaledPositionDifference );
		migrationCost = scaledPositionDifference * ( float ) Math.pow( 1 + scaledPositionDifference, exponent );
		return new ValuePair<>(migrationCost, new float[]{migrationCost});
	}

	public static Pair< Float, float[] > getGrowthCost( final float sourceSize, final float targetSize ) {
		float scaledSizeDifference = ( targetSize - sourceSize ) / normalizer; /* TODO-MM-20191119: deltaL < 1 for anything that is smaller than the GL;
																				however, it makes more sense to look at the
																				relative size change?! I will do so in the future. */
		float exponent;
		if ( scaledSizeDifference > 0 ) { // growth
			scaledSizeDifference = Math.max( 0, scaledSizeDifference - 0.05f ); // growing up 5% is free
			exponent = 4.0f;
		} else { // shrinkage
			exponent = 40.0f;
		}
		scaledSizeDifference = Math.abs( scaledSizeDifference );

		float growthCost = scaledSizeDifference * (float) Math.pow(1 + scaledSizeDifference, exponent); // since deltaL is <1 we add 1 before taking its power

		return new ValuePair<>(growthCost, new float[]{growthCost});
	}


	/**
	 * Calculate the component costs. The component cost is modulated between -0.2 and 0.2 using cost-factors, which
	 * depend on the component position relative to the upper boundary and the pixel-values of probability map.
	 *
	 * @param component
	 * @return
	 */
	public static float getComponentCost(final ComponentInterface component) {
		if (featureFlagComponentCost == ComponentCostCalculationMethod.Legacy) {
			return getComponentCostLegacy(component);
		} else if (featureFlagComponentCost == ComponentCostCalculationMethod.UsingWatershedLineOnly) {
			return getComponentCostUsingWatershedLines(component);
		} else if (featureFlagComponentCost == ComponentCostCalculationMethod.UsingFullProbabilityMaps)
		{
			return getComponentCostUsingFullProbabilityMap(component);
		}
		throw new NotImplementedException(); /* this will be thrown if no valid feature-flag was set */
	}

	public static double maximumComponentCost = 0.2; // maximum component cost
	public static double minimumComponentCost = -0.2; // minimum component cost

	public static float getComponentCostLegacy(final ComponentInterface component) {
		double exitCostFactor = getCostFactorComponentExit(component);
		float cost = (float) (maximumComponentCost + (minimumComponentCost - maximumComponentCost) * exitCostFactor);
		return cost;
	}

	public static float getComponentCostUsingWatershedLines(final ComponentInterface component) {
		double exitCostFactor = getCostFactorComponentExit(component);
		double componentWatershedLineFactor = getCostFactorComponentWatershedLine((AdvancedComponent<FloatType>) component);
		double parentComponentWatershedLineFactor = getCostFactorParentComponentWatershedLine((AdvancedComponent<FloatType>) component);
		float cost = (float) (maximumComponentCost + (minimumComponentCost - maximumComponentCost) * exitCostFactor * componentWatershedLineFactor * parentComponentWatershedLineFactor);
		return cost;
	}

	public static float getComponentCostUsingFullProbabilityMap(ComponentInterface component) {
		double exitCostFactor = getCostFactorComponentExit(component);
		double componentWatershedLineFactor = getCostFactorComponentWatershedLine((AdvancedComponent<FloatType>) component);
		double parentComponentWatershedLineFactor = getCostFactorParentComponentWatershedLine((AdvancedComponent<FloatType>) component);
		double componentProbabilityFactor = getCostFactorComponentProbability((AdvancedComponent<FloatType>) component);
		float cost = (float) (maximumComponentCost + (minimumComponentCost - maximumComponentCost) * exitCostFactor * componentWatershedLineFactor * parentComponentWatershedLineFactor * componentProbabilityFactor);
		return cost;
	}

	/**
	 * Calculate the prefactor for the component cost that is incurred, when the component exits the ROI.
	 *
	 * @param component
	 * @return ranges from 0 to 1.
	 */
	public static double getCostFactorComponentExit(ComponentInterface component) {
		float roiBoundaryPosition = (float) ConfigurationManager.GL_OFFSET_TOP; // position above which a component lies outside of the ROI
		double verticalPositionOfComponent = component.firstMomentPixelCoordinates()[1];
		double positionRelativeToRoiBoundary = verticalPositionOfComponent - roiBoundaryPosition;
		double componentExitRange = ConfigurationManager.COMPONENT_EXIT_RANGE / 2.0f; // defines the range, over which the cost increases.
		double exitCostFactor = 1 / (1 + Math.exp(-positionRelativeToRoiBoundary / componentExitRange)); /* this factor increases cost as the component exits the ROI boundary */
		return exitCostFactor;
	}

	/**
	 * Calculate the cost factor for the watershed line in the probability map of the component itself.
	 *
	 * @param component
	 * @return ranges from 0 to 1.
	 */
	public static double getCostFactorComponentWatershedLine(AdvancedComponent<FloatType> component){
		Double val = component.getWatershedLinePixelValueAverage();
		if (val == null) {
			return 1.0; /* there is no watershed line so we return 1.0 */
		}
		return val.doubleValue();
	}

	/**
	 * Calculate the likelihood value for the watershed line being ON.
	 *
	 * @param component
	 * @return ranges from 0 to 1.
	 */
	public static double getOnLikelihoodForComponentWatershedLine(AdvancedComponent<FloatType> component){
		List<Double> probabilities = component.getWatershedLinePixelValues().stream().map(value -> value.getRealDouble()).collect(Collectors.toList());
		return multiplyPixelValues(probabilities);
	}

	/**
	 * Calculate the likelihood value for the watershed line being OFF.
	 *
	 * @param component
	 * @return ranges from 0 to 1.
	 */
	public static double getOffLikelihoodForComponentWatershedLine(AdvancedComponent<FloatType> component){
		List<Double> probabilities = component.getWatershedLinePixelValues().stream().map(value -> 1. - value.getRealDouble()).collect(Collectors.toList());
		return multiplyPixelValues(probabilities);
	}

	/**
	 * Calculate the likelihood value for the component being ON.
	 *
	 * @param component
	 * @return ranges from 0 to 1.
	 */
	public static double getOnLikelihoodForComponent(AdvancedComponent<FloatType> component){
		List<Double> probabilities = component.getComponentPixelValues().stream().map(value -> value.getRealDouble()).collect(Collectors.toList());
		return multiplyPixelValues(probabilities);
	}

	/**
	 * Calculate the likelihood value for the component being OFF.
	 *
	 * @param component
	 * @return ranges from 0 to 1.
	 */
	public static double getOffLikelihoodForComponent(AdvancedComponent<FloatType> component) {
		List<Double> probabilities = component.getComponentPixelValues().stream().map(value -> 1. - value.getRealDouble()).collect(Collectors.toList());
		return multiplyPixelValues(probabilities);
	}

	public static double multiplyPixelValues(List<Double> pixelVals) {
//		List<Double> res = pixelVals.stream().map(value -> value.getRealDouble()).collect(Collectors.toList());
//		List<Double> res = pixelVals.stream().map(value -> value.getRealDouble()).reduce(1, Math::multiplyExact);
		return pixelVals.stream().reduce(1.0, (acc, value) -> acc * value).doubleValue();
	}

	/**
	 * Calculate for the parent component the cost factor for the watershed line that gave rise to this child component.
	 * The probability of the child being a valid component, is inverse to the value of the watershed line values of
	 * the parent-component:
	 * This means that if the watershed-line of the parent has a high (average) value, then the corresponding child
	 * component is likely not valid.
	 *
	 * @param component
	 * @return ranges from 0 to 1.
	 */
	public static double getCostFactorParentComponentWatershedLine(AdvancedComponent<FloatType> component){
		AdvancedComponent<FloatType> parent = component.getParent();
		if (parent == null) {
			return 1.0; /* If there is no parent component then this is a root component. We set the factor to 1, because this means that all surrounding pixel probabilities fall below the global threshold. */
		}
		return 1. - getCostFactorComponentWatershedLine(parent);
	}

	public static double getCostFactorComponentProbability(AdvancedComponent<FloatType> component) {
		return component.getPixelValueAverage();
//		double total = component.getPixelValueTotal();
//		double hullArea = component.getConvexHullArea();
//		return total / hullArea;
	}

	/**
	 * @param sourceComponent
	 * @return
	 */
	public static float getDivisionLikelihoodCost( final Component< FloatType, ? > sourceComponent ) {
if ( sourceComponent.getChildren().size() > 2 ) { return 1.5f; } /* is this the reason we have value 1.3 for some divisions? There is a bug, where we sometimes have 3 child-components, so this if-statement would hold!// */
if ( sourceComponent.getChildren().size() <= 1 ) { return 1.5f; } /* is this the reason we have value 1.3 for some divisions? There is a bug, where we sometimes have 3 child-components, so this if-statement would hold!// */

// if two children, eveluate likelihood of being pre-division
final List< Component< FloatType, ? > > listOfChildren = ( List< Component< FloatType, ? >> ) sourceComponent.getChildren();
final long sizeChild1 = getComponentSize(listOfChildren.get( 0 ), 1);
final long sizeChild2 = getComponentSize(listOfChildren.get( 1 ), 1);

final long sizeSourceComponent = getComponentSize(sourceComponent, 1);

final long deltaSizeBetweenChildren = Math.abs( sizeChild1 - sizeChild2 ) / Math.min( sizeChild1, sizeChild2 ); // in multiples of smaller one
final long deltaSizeChildrenToSourceComponent = Math.abs( sizeChild1 + sizeChild2 - sizeSourceComponent ) / ( sizeChild1 + sizeChild2 ); // in multiples of A+B

return 0.1f * deltaSizeBetweenChildren + 0.1f * deltaSizeChildrenToSourceComponent;
	}

	public static Pair<Double, Double> getLikelihoodExtremaWithinRange(List<ComponentInterface> components, double rangeMin, double rangeMax) {
		double minRet = Double.MAX_VALUE;
		double maxRet = -Double.MAX_VALUE;
		for (ComponentInterface component : components) {
			List<Double> pixelValues = component.getComponentPixelValuesAsDouble();
			for (Double val : pixelValues){
				if (val > rangeMin && val < minRet) minRet = val;
				if (val < rangeMax && val > maxRet) maxRet = val;
			}
		}
		return new ValuePair<>(minRet, maxRet);
	}

//	public static Pair<Double, Double> getLikelihoodExtremaWithinRange(List<Double> pixelValues, double rangeMin, double rangeMax) {
//
//		throw new NotImplementedException();
//	}
}
