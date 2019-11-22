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
	It should be removed in favor of having costs based relative growth and/or movement.
	NOTE: 340px is roughly the length of the GL, when Florian Jug designed the cost functions, so that is, the value that
	we are keeping for the moment.*/

	public static Pair< Float, float[] > getMigrationCost( final float oldPosition, final float newPosition ) {
		float deltaH = ( oldPosition - newPosition ) / normalizer;
		float power;
		float costDeltaH;
		if ( deltaH > 0 ) { // upward migration
			deltaH = Math.max( 0, deltaH - 0.05f ); // going upwards for up to 5% is for free...
			power = 3.0f;
		} else { // downward migration
			Math.max( 0, deltaH - 0.01f );  // going downwards for up to 1% is for free...
			power = 6.0f;
		}
		deltaH = Math.abs( deltaH );
		costDeltaH = deltaH * ( float ) Math.pow( 1 + deltaH, power );
//		latestCostEvaluation = String.format( "c_h = %.4f * %.4f^%.1f = %.4f", deltaH, 1 + deltaH, power, costDeltaH );
		return new ValuePair<>(costDeltaH, new float[]{costDeltaH});
	}

	public static Pair< Float, float[] > getGrowthCost( final float oldSize, final float newSize ) {
		float deltaL = ( newSize - oldSize ) / normalizer; /* TODO-MM-20191119: deltaL < 1 for anything that is smaller than the GL; however, it makes more sense to look at the relative size change?! I will do so in the future. */
		float power;
		if ( deltaL > 0 ) { // growth
			deltaL = Math.max( 0, deltaL - 0.05f ); // growing up 5% is free
			power = 4.0f;
		} else { // shrinkage
			power = 40.0f;
		}
		deltaL = Math.abs( deltaL );

		float costDeltaL = deltaL * (float) Math.pow(1 + deltaL, power); // since deltaL is <1 we add 1 before taking its power

		float relativeGrowth = ( newSize - oldSize ) / oldSize;
		if(relativeGrowth > 0.4f){
			costDeltaL = costDeltaL * 20;
		}
		else if(relativeGrowth < -0.4f){
			costDeltaL = costDeltaL * 20;
		}
		return new ValuePair<>(costDeltaL, new float[]{costDeltaL});
	}


    public static float getUnevenDivisionCost( final float sizeFirstChild, final float sizeSecondChild ) {
		final float deltaS = Math.abs( sizeFirstChild - sizeSecondChild ) / Math.min( sizeFirstChild, sizeSecondChild );
		float power = 2.0f;
		float costDeltaL;
		if ( deltaS > 1.15 ) {
			power = 7.0f;
		}
		costDeltaL = ( float ) Math.pow( deltaS, power );

//		latestCostEvaluation = String.format( "c_d = %.4f^%.1f = %.4f", deltaS, power, costDeltaL );
		return costDeltaL;
	}

	/**
	 * @param component
	 * @return
	 */
	public static float getComponentCost(final Component< ?, ? > component, final RandomAccessibleInterval<FloatType> imageProbabilities ) {
        float top_roi_boundary = (float) MoMA.GL_OFFSET_TOP;
        double verticalPosition = ((SimpleComponent<FloatType>) component).firstMomentPixelCoordinates()[1];
        double distanceFromBoundary = top_roi_boundary - verticalPosition;
        double sigma = 4; // defines the range, over which the cost increases.
        double maximumCost = 5; // maximum cost possible for a component above the boundary
        double minimumCost = -1; // minimum cost of a component below the boundary
        float cost = (float) (minimumCost + (-minimumCost + maximumCost)/(1 + Math.exp(-distanceFromBoundary/sigma)));
		return cost;
//		return 1.0f;
//        double rand = Math.random() - 0.5;
//        if(rand > 0)
//            return 1.1f;
//        else
//            return -1f;
    }

    /**
	 * @param from
	 * @return
	 */
	public static float getDivisionLikelihoodCost( final Hypothesis< Component< FloatType, ? >> from ) {
		if ( from.getWrappedComponent().getChildren().size() > 2 ) { return 1.5f; }
		if ( from.getWrappedComponent().getChildren().size() <= 1 ) { return 1.5f; }

		// if two children, eveluate likelihood of being pre-division
		final List< Component< FloatType, ? > > children = ( List< Component< FloatType, ? >> ) from.getWrappedComponent().getChildren();
		final long sizeA = getComponentSize(children.get( 0 ), 1);
		final long sizeB = getComponentSize(children.get( 1 ), 1);

//		final float valParent = from.getWrappedComponent().value().get();
		final long sizeParent = getComponentSize(from.getWrappedComponent(), 1);

		final long deltaSizeAtoB = Math.abs( sizeA - sizeB ) / Math.min( sizeA, sizeB ); // in multiples of smaller one
		final long deltaSizeABtoP = Math.abs( sizeA + sizeB - sizeParent ) / ( sizeA + sizeB ); // in multiples of A+B

		return 0.1f * deltaSizeAtoB + 0.1f * deltaSizeABtoP;
	}
}
