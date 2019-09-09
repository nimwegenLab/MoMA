/**
 *
 */
package com.jug.lp.costs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import gurobi.GRBVar;

/**
 * MM: CostManager contains a list 'featureList' of all features that correspond to a GRBVar/Gurobi model entry. The hash-map
 * 'var2row' contains the mapping between the GRB-model-entry and corresponding entry in 'featureList'.
 * @author jug
 */
public class CostManager {

	private final int numFeatures;
	private final int numMappingFeatures;
	private final int numDivisionFeatures;

	private final double[] weights;
	private final List< float[] > featureList;

	private final Map< GRBVar, Integer > var2row;

	public CostManager( final int numMappingFeatures, final int numDivisionFeatures ) {
		this.numFeatures = numMappingFeatures + numDivisionFeatures;
		this.numMappingFeatures = numMappingFeatures;
		this.numDivisionFeatures = numDivisionFeatures;

		this.weights = new double[ numFeatures ];
		this.featureList = new ArrayList<>();
		this.var2row = new HashMap<>();
	}

	public int getDimensions() {
		return numFeatures;
	}

	public double[] getWeights() {
		return weights;
	}

	public void setWeights( final double[] weights ) {
		if ( weights.length != numFeatures ) { throw new IllegalArgumentException( "Dimension mismatch of given weight vector." ); }
		System.arraycopy( weights, 0, this.weights, 0, numFeatures );
	}

	public void addRow( final GRBVar var, final float[] values ) {
		if ( values.length != numFeatures ) { throw new IllegalArgumentException( "Given feature values do not match featureList dimensions" ); }
		var2row.put( var, featureList.size() );
		featureList.add( values );
	}

	public float[] getRow( final GRBVar var ) {
		final Integer muh = var2row.get( var );
		if ( muh != null ) {
			return featureList.get( muh );
		} else {
			return new float[ numFeatures ];
		}
	}

	public void addMappingVariable( final GRBVar var, final float[] values ) {
		final float[] features = new float[ numFeatures ];
		if ( values.length != numMappingFeatures ) { throw new IllegalArgumentException( "Given feature values for a mapping do not match featureList dimensions" ); }
		System.arraycopy( values, 0, features, 0, values.length );
		addRow( var, features );
	}

	public void addDivisionVariable( final GRBVar var, final float[] values ) {
		final float[] features = new float[ numFeatures ];
		if ( values.length != numDivisionFeatures ) { throw new IllegalArgumentException( "Given feature values for a division do not match featureList dimensions" ); }
		System.arraycopy( values, 0, features, numMappingFeatures, values.length );
		addRow( var, features );
	}

	public double getCurrentCost( final GRBVar var ) {
		double ret = 0;
		final Integer muh = var2row.get( var );
		if ( muh == null )
			return 0;
		final float[] row = featureList.get( muh );
		for ( int i = 0; i < row.length; i++ ) {
			ret += row[ i ] * weights[ i ];
		}
		return ret;
	}
}
