package com.jug.lp.costs;

import com.jug.MoMA;
import com.jug.util.componenttree.SimpleComponent;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.componenttree.Component;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Pair;
import net.imglib2.util.ValuePair;

import java.util.List;

import static com.jug.FeatureFlags.featureFlagUseComponentCostWithProbabilityMap;
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
	public static float getComponentCost(final SimpleComponent<FloatType> component, final RandomAccessibleInterval<FloatType> imageProbabilities) {
		double maximumCost = 0.2; // maximum component cost
		double minimumCost = -0.2; // minimum component cost
		double exitCostFactor = getCostFactorComponentExit((SimpleComponent<FloatType>) component);
		if (!featureFlagUseComponentCostWithProbabilityMap) {
			float cost = (float) (minimumCost + (maximumCost - minimumCost) * exitCostFactor);
			return cost;
		} else {
			double componentWatershedLineFactor = getCostFactorComponentWatershedLine((SimpleComponent<FloatType>) component);
			double parentComponentWatershedLineFactor = getCostFactorParentComponentWatershedLine((SimpleComponent<FloatType>) component);
			float cost = (float) (minimumCost + (maximumCost - minimumCost) * exitCostFactor * componentWatershedLineFactor * parentComponentWatershedLineFactor);
			return cost;
		}
	}

	/**
	 * Calculate the prefactor for the component cost that is incurred, when the component exits the ROI.
	 *
	 * @param component
	 * @return ranges from 0 to 1.
	 */
	public static double getCostFactorComponentExit(SimpleComponent<FloatType> component) {
		float roiBoundaryPosition = (float) MoMA.GL_OFFSET_TOP; // position above which a component lies outside of the ROI
		double verticalPositionOfComponent = component.firstMomentPixelCoordinates()[1];
		double positionRelativeToRoiBoundary = roiBoundaryPosition - verticalPositionOfComponent;
		double componentExitRange = MoMA.COMPONENT_EXIT_RANGE / 2.0f; // defines the range, over which the cost increases.
		double exitCostFactor = 1 / (1 + Math.exp(-positionRelativeToRoiBoundary / componentExitRange)); /* this factor increases cost as the component exits the ROI boundary */
		return exitCostFactor;
	}

	/**
	 * Calculate the cost factor for the watershed line in the probability map of the component itself.
	 *
	 * @param component
	 * @return ranges from 0 to 1.
	 */
	public static double getCostFactorComponentWatershedLine(SimpleComponent<FloatType> component){
		List<FloatType> vals = component.getWatershedLinePixelValues();
		double avg = vals.stream()
				.map(d -> d.getRealDouble())
				.mapToDouble(d -> d)
				.average()
				.orElse(1.0);
		return 1.0 - avg;
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
	public static double getCostFactorParentComponentWatershedLine(SimpleComponent<FloatType> component){
		SimpleComponent<FloatType> parent = component.getParent();
		if (parent == null) {
			return 1.0; /* If there is no parent component then this is a root component. We set the factor to 1, because this means that all surrounding pixel probabilities fall below the global threshold. */
		}
		List<FloatType> vals = parent.getWatershedLinePixelValues();
		double avg = vals.stream()
				.map(d -> d.getRealDouble())
				.mapToDouble(d -> d)
				.average()
				.orElse(0.0); /* return 0.0, if the parent component has no watershed line */
		return 1 - avg; /* the probability of the child being a valid component, is inverse to the value of the watershed line values of the parent-component; this means that if the watershed-line of the parent has a high value, then the child component is likely not valid */
	}

	/**
	 * @param sourceComponent
	 * @return
	 */
	public static float getDivisionLikelihoodCost( final Component< FloatType, ? > sourceComponent ) {
if ( sourceComponent.getChildren().size() > 2 ) { return 1.5f; }
if ( sourceComponent.getChildren().size() <= 1 ) { return 1.5f; }

// if two children, eveluate likelihood of being pre-division
final List< Component< FloatType, ? > > listOfChildren = ( List< Component< FloatType, ? >> ) sourceComponent.getChildren();
final long sizeChild1 = getComponentSize(listOfChildren.get( 0 ), 1);
final long sizeChild2 = getComponentSize(listOfChildren.get( 1 ), 1);

final long sizeSourceComponent = getComponentSize(sourceComponent, 1);

final long deltaSizeBetweenChildren = Math.abs( sizeChild1 - sizeChild2 ) / Math.min( sizeChild1, sizeChild2 ); // in multiples of smaller one
final long deltaSizeChildrenToSourceComponent = Math.abs( sizeChild1 + sizeChild2 - sizeSourceComponent ) / ( sizeChild1 + sizeChild2 ); // in multiples of A+B

return 0.1f * deltaSizeBetweenChildren + 0.1f * deltaSizeChildrenToSourceComponent;
	}
}
