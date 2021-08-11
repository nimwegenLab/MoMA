package com.jug.lp.costs;

import java.util.List;

import com.jug.MoMA;
import com.jug.lp.Hypothesis;
import com.jug.util.ComponentTreeUtils;

import com.jug.util.componenttree.SimpleComponent;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.componenttree.Component;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Pair;
import net.imglib2.util.ValuePair;

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
	 * @param component
	 * @return
	 */
	public static float getComponentCost(final Component< ?, ? > component, final RandomAccessibleInterval<FloatType> imageProbabilities ) {
        float roiBoundaryPosition = (float) MoMA.GL_OFFSET_TOP; // position above which a component lies outside of the ROI
        double verticalPositionOfComponent = ((SimpleComponent<FloatType>) component).firstMomentPixelCoordinates()[1];
        double distanceFromBoundary = roiBoundaryPosition - verticalPositionOfComponent;
        double componentExitRange = MoMA.COMPONENT_EXIT_RANGE / 2.0f; // defines the range, over which the cost increases.
        double maximumCost = 0.2; // maximum component cost outside the ROI
        double minimumCost = -0.2; // minimum component cost inside the ROI
        float cost = (float) (minimumCost + (-minimumCost + maximumCost)/(1 + Math.exp(-distanceFromBoundary/componentExitRange)));
		return cost;
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
