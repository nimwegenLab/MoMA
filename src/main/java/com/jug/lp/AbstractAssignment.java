package com.jug.lp;

import com.jug.export.FactorGraphFileBuilder_SCALAR;
import gurobi.*;

import java.util.List;

/**
 * Partially implemented class for everything that wants to be an assignment.
 * The main purpose of such a class is to store the value of the corresponding
 * Gurobi assignment variable and the ability to add assignment specific
 * constraints to the ILP (model).
 *
 * @author jug
 */
@SuppressWarnings( "restriction" )
public abstract class AbstractAssignment< H extends Hypothesis< ? > > {

	private int type;

	GrowthlaneTrackingILP ilp;

	private int exportVarIdx = -1;
	private GRBVar ilpVar;

	private boolean isGroundTruth = false;
	private boolean isGroundUntruth = false;
	private GRBConstr constrGroundTruth;

	private boolean isPruned = false;

	/**
	 * Creates an assignment...
	 */
	AbstractAssignment(final int type, final GRBVar ilpVariable, final GrowthlaneTrackingILP ilp) {
		this.setType( type );
		setGRBVar( ilpVariable );
		setGrowthlaneTrackingILP( ilp );
	}

	abstract public int getId();

	public String getStringId() {
		try {
			return getGrbVarName();
		} catch (GRBException err) {
			return "AssignmentNameUndefined";
		}
	}

	/**
	 * @return the type
	 */
	public int getType() {
		return type;
	}

	/**
	 * @param type
	 *            the type to set
	 */
	void setType(final int type) {
		this.type = type;
	}

	abstract public H getSourceHypothesis();

	abstract public List<H> getTargetHypotheses();

	//	/**
//	 * This function is for example used when exporting a FactorGraph
//	 * that describes the entire optimization problem at hand.
//	 *
//	 * @return a variable index that is unique for the indicator
//	 *         variable used for this assignment.
//	 * @throws Exception
//	 */
	public int getVarIdx() {
		if ( exportVarIdx == -1 ) {
			System.out.println( "AAAAACHTUNG!!! Variable index not initialized before export was attempted!" );
		}
		return exportVarIdx;
	}

	/**
	 * @return the ilpVar
	 */
	public GRBVar getGRBVar() {
		return ilpVar;
	}

	public String getGrbVarName() throws GRBException {
		return getGRBVar().get(GRB.StringAttr.VarName);
	}

	/**
	 * @param ilpVar
	 *            the ilpVar to set
	 */
	private void setGRBVar(final GRBVar ilpVar) {
		this.ilpVar = ilpVar;
	}

//	/**
//	 * One can set a variable id.
//	 * This is used for exporting purposes like e.g. by
//	 * <code>FactorGraphFileBuilder</code>.
//	 *
//	 * @param varId
//	 */
	public void setVarId( final int varId ) {
		this.exportVarIdx = varId;
	}

	/**
	 */
	private void setGrowthlaneTrackingILP(final GrowthlaneTrackingILP ilp) {
		this.ilp = ilp;
	}

	/**
	 * @return the cost
	 */
	public float getCost() {
		float cost = 0;
		try {
			cost = ( float ) getGRBVar().get( GRB.DoubleAttr.Obj );
		} catch ( final GRBException e ) {
			System.err.println( "CRITICAL: cost could not be read out of Gurobi ILP!" );
//			e.printStackTrace();
		}
		return cost;
	}

    /**
	 * @return true, if the ilpVar of this Assignment is equal to 1.0.
	 */
	private boolean previousIsChoosen = false;
	public boolean isChoosen() throws GRBException {
		if (ilp.getStatus() == IlpStatus.OPTIMIZATION_NEVER_PERFORMED)
			throw new GRBException();  /* ilp.getStatus() == 0: corresponds to OPTIMIZATION_NEVER_PERFORMED; this hack is needed to stay compatible, because the first time that isChoosen() is called from program code, it throws GRBException. And this first call is needed to run the first optimization and initialize `previousIsChoosen`. Furthermore, we cannot simply return `previousIsChoosen=false`, because then the state of the assignments will not be correctly initialized. */
		if (ilp.getStatus() == IlpStatus.OPTIMIZATION_IS_RUNNING || ilp.getStatus() == IlpStatus.UNDEFINED) {
			return previousIsChoosen;
		}
		try {
			GRBVar grbVarOfAssignment = getGRBVar();
			long binary_selection_variable_value_rounded = Math.round(grbVarOfAssignment.get(GRB.DoubleAttr.X));
			previousIsChoosen = (binary_selection_variable_value_rounded == 1);
			return previousIsChoosen;
		} catch (GRBException err) {
			return previousIsChoosen; /* This will be returned in case the ILP optimization fails. In that case GRBException will be thrown. We then return the previous state of all assignments, so that the user can correct any mistakes she mad. */
		}
	}

	/**
	 * Abstract method that will, once implemented, add a set of assignment
	 * related constraints to the ILP (model) later to be solved by Gurobi.
	 */
	public abstract void addConstraintsToILP() throws GRBException;

	public boolean isGroundTruth() {
		return isGroundTruth;
	}

	public boolean isGroundUntruth() {
		return isGroundUntruth;
	}

	public void setGroundTruth( final boolean groundTruth ) {
		this.isGroundTruth = groundTruth;
		this.isGroundUntruth = false;
		addOrRemoveGroundTruthConstraint( groundTruth );
	}

	public void setGroundUntruth( final boolean groundUntruth ) {
		this.isGroundTruth = false;
		this.isGroundUntruth = groundUntruth;
		addOrRemoveGroundTruthConstraint( groundUntruth );
	}

	public void reoptimize() {
		try {
			ilp.model.update();
			final Thread t = new Thread(() -> ilp.run());
			t.start();
		} catch ( final GRBException e ) {
			e.printStackTrace();
		}
	}

	/**
	 *
	 */
	private void addOrRemoveGroundTruthConstraint(final boolean add) {
		try {
			if (add) {
				final float value = (this.isGroundUntruth) ? 0f : 1f; /* sets whether the assignment will be added as ground-truth or ground-untruth */

				final GRBLinExpr exprGroundTruth = new GRBLinExpr();
				exprGroundTruth.addTerm(1.0, getGRBVar());
				constrGroundTruth = ilp.model.addConstr(exprGroundTruth, GRB.EQUAL, value, "AssignmentGtConstraint_" + getGrbVarName());
			} else {
				if (constrGroundTruth != null) {
					ilp.model.remove(constrGroundTruth);
					constrGroundTruth = null;
				}
			}
		} catch (final GRBException e) {
			e.printStackTrace();
		}
	}

	/**
	 *
	 * @return null if not set, otherwise the GRBConstr.
	 */
	public GRBConstr getGroundTruthConstraint() {
		return constrGroundTruth;
	}

	/**
	 * @param value set if assignment is pruned
	 */
	public void setPruned( final boolean value ) {
		this.isPruned = value;
	}

	/**
	 *
	 * @return if assignment is pruned
	 */
	public boolean isPruned() {
		return isPruned;
	}
}
