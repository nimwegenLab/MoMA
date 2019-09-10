/**
 *
 */
package com.jug.lp;

import java.util.ArrayList;
import java.util.List;

import com.jug.export.FactorGraphFileBuilder_SCALAR;

import gurobi.GRBException;
import gurobi.GRBVar;
import net.imglib2.algorithm.componenttree.Component;
import net.imglib2.type.numeric.real.FloatType;

/**
 * @author jug
 */
public class DivisionAssignment extends AbstractAssignment< Hypothesis< Component< FloatType, ? > > > {

    private final Hypothesis< Component< FloatType, ? >> from;
	private final Hypothesis< Component< FloatType, ? >> toUpper;
	private final Hypothesis< Component< FloatType, ? >> toLower;

	/**
	 * Creates an DivisionAssignment.
	 *
	 * @param nodes
	 * @param edges
	 * @param from
     */
	public DivisionAssignment(final GRBVar ilpVariable, final GrowthLineTrackingILP ilp, final AssignmentsAndHypotheses<AbstractAssignment<Hypothesis<Component<FloatType, ?>>>, Hypothesis<Component<FloatType, ?>>> nodes, final HypothesisNeighborhoods<Hypothesis<Component<FloatType, ?>>, AbstractAssignment<Hypothesis<Component<FloatType, ?>>>> edges, final Hypothesis<Component<FloatType, ?>> from, final Hypothesis<Component<FloatType, ?>> toUpper, final Hypothesis<Component<FloatType, ?>> toLower) {
		super( GrowthLineTrackingILP.ASSIGNMENT_DIVISION, ilpVariable, ilp );
		this.from = from;
		this.toUpper = toUpper;
		this.toLower = toLower;
    }

	/**
	 * This method is void. DIVISION assignments do not come with assignment
	 * specific constrains...
	 *
     */
	@Override
	public void addConstraintsToLP() {}

	/**
	 * Division assignments do not come with constraints.
	 *
     */
	@Override
	public void addFunctionsAndFactors( final FactorGraphFileBuilder_SCALAR fgFile, final List< Integer > regionIds ) {}

	/**
	 * Returns the segmentation hypothesis this division-assignment comes from
	 * (the one at the earlier time-point t).
	 *
	 * @return the associated segmentation-hypothesis.
	 */
	public Hypothesis< Component< FloatType, ? >> getSourceHypothesis() {
		return from;
	}

	/**
	 * Returns the upper of the two segmentation hypothesis this
	 * division-assignment links to (the upper of the two at the later
	 * time-point t+1).
	 *
	 * @return the associated segmentation-hypothesis.
	 */
	public Hypothesis< Component< FloatType, ? >> getUpperDesinationHypothesis() {
		return toUpper;
	}

	/**
	 * Returns the upper of the two segmentation hypothesis this
	 * division-assignment links to (the upper of the two at the later
	 * time-point t+1).
	 *
	 * @return the associated segmentation-hypothesis.
	 */
	public Hypothesis< Component< FloatType, ? >> getLowerDesinationHypothesis() {
		return toLower;
	}

	/**
	 * @see com.jug.lp.AbstractAssignment#getId()
	 */
	@Override
	public int getId() {
		return from.getId() + toUpper.getId() + toLower.getId();
	}

	/**
	 * @see com.jug.lp.AbstractAssignment#getConstraintsToSave_PASCAL()
	 */
	@Override
	public List< String > getConstraintsToSave_PASCAL() {
		return new ArrayList<>();
	}
}
