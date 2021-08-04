package com.jug.lp;

import java.util.ArrayList;
import java.util.List;

import com.jug.export.FactorGraphFileBuilder_SCALAR;

import gurobi.GRBVar;
import net.imglib2.algorithm.componenttree.Component;
import net.imglib2.type.numeric.real.FloatType;

/**
 * @author jug
 */
public class MappingAssignment extends AbstractAssignment< Hypothesis< Component< FloatType, ? > > > {

    private final Hypothesis< Component< FloatType, ? >> from;
	private final Hypothesis< Component< FloatType, ? >> to;

	/**
	 * Creates an MappingAssignment.
	 *
	 * @param nodes
	 * @param edges
	 * @param from
	 * @param to
     */
	public MappingAssignment( final int t, final GRBVar ilpVariable, final GrowthLineTrackingILP ilp, final AssignmentsAndHypotheses< AbstractAssignment< Hypothesis< Component< FloatType, ? > > >, Hypothesis< Component< FloatType, ? > > > nodes, final HypothesisNeighborhoods< Hypothesis< Component< FloatType, ? > >, AbstractAssignment< Hypothesis< Component< FloatType, ? > > > > edges, final Hypothesis< Component< FloatType, ? >> from, final Hypothesis< Component< FloatType, ? >> to ) {
		super( GrowthLineTrackingILP.ASSIGNMENT_MAPPING, ilpVariable, ilp );
		this.from = from;
		this.to = to;
    }

	/**
	 * This method is void. MAPPING assignments do not come with assignment
	 * specific constrains...
	 *
     */
	@Override
	public void addConstraintsToILP() {}

	/**
	 * Mapping assignments do not come with constraints.
	 *
	 */
	@Override
	public void addFunctionsAndFactors( final FactorGraphFileBuilder_SCALAR fgFile, final List< Integer > regionIds ) {}

	/**
	 * Returns the segmentation hypothesis this mapping-assignment comes from
	 * (the one at the earlier time-point t).
	 *
	 * @return the associated segmentation-hypothesis.
	 */
	public Hypothesis< Component< FloatType, ? >> getSourceHypothesis() {
		return from;
	}

	/**
	 * Returns the segmentation hypothesis this mapping-assignment links to
	 * (the one at the later time-point t+1).
	 *
	 * @return the associated segmentation-hypothesis.
	 */
	public Hypothesis< Component< FloatType, ? >> getDestinationHypothesis() {
		return to;
	}

	/**
	 * @see com.jug.lp.AbstractAssignment#getId()
	 */
	@Override
	public int getId() {
		return from.getId() + to.getId() + GrowthLineTrackingILP.ASSIGNMENT_MAPPING;
	}

	/**
	 * @see com.jug.lp.AbstractAssignment#getConstraintsToSave_PASCAL()
	 */
	@Override
	public List< String > getConstraintsToSave_PASCAL() {
		return new ArrayList<>();
	}

}
