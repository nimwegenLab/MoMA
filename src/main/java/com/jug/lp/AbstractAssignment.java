package com.jug.lp;

import gurobi.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static java.util.Objects.isNull;

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

	private GRBVar ilpVar;

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
		GRBConstr grbConstr = getGrbConstr();
		if (isNull(grbConstr)) {
			return false;  /* no variable was found so this assignment is not forced */
		}

		Double constraintValue;
		try {
			constraintValue = grbConstr.get(GRB.DoubleAttr.RHS);
		} catch (GRBException e) {
			throw new RuntimeException(e);
		}
		return constraintValue > 0.5; /* condition for ground truth is LHS value == 0; we test against due 0.5 for numerical precision */
	}

	@Nullable
	private GRBConstr getGrbConstr() {
		GRBConstr grbConstr;
		try {
			grbConstr = ilp.model.getConstrByName("AssignmentGtConstraint_" + getGrbVarName());
		} catch (GRBException e) {
			return null;
		}
		return grbConstr;
	}

	public boolean isGroundUntruth() {
		GRBConstr grbConstr = getGrbConstr();
		if(isNull(grbConstr)){
			return false; /* no variable was found so this assignment is not forced */
		}

		Double constraintValue;
		try {
			constraintValue = grbConstr.get(GRB.DoubleAttr.RHS);
		} catch (GRBException e) {
			throw new RuntimeException(e);
		}
		return constraintValue < 0.5; /* condition for ground truth is LHS value == 0; we test against due 0.5 for numerical precision */
	}

	public void setGroundTruth(final boolean targetStateIsTrue) {
		try {
			if (targetStateIsTrue) {
				if (isGroundUntruth()) {
					removeConstraint();
				}
				addGroundTruthConstraint();
				return;
			}
			if (!targetStateIsTrue && isGroundTruth()) {
				removeConstraint();
				return;
			}
		} catch (GRBException e) {
			throw new RuntimeException(e);
		}
		throw new RuntimeException("There was an error in the Gurobi model, which caused an undefined state."); /* this should never be reached*/
	}

	public void setGroundUntruth(final boolean targetStateIsTrue) {
		try {
			if (targetStateIsTrue) {
				if (isGroundTruth()) {
					removeConstraint();
				}
				addGroundUntruthConstraint();
				return;
			}
			if (!targetStateIsTrue && isGroundUntruth()) {
				removeConstraint();
				return;
			}
		} catch (GRBException e) {
			throw new RuntimeException(e);
		}
		throw new RuntimeException("There was an error in the Gurobi model, which caused an undefined state."); /* this should never be reached*/
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

	private void addGroundTruthConstraint() throws GRBException {
		addConstraint(1.0, "AssignmentGtConstraint_" + getGrbVarName());
	}

	private void addGroundUntruthConstraint() throws GRBException {
		addConstraint(0.0, "AssignmentGtConstraint_" + getGrbVarName());
	}

	private void addConstraint(double rhsValue, String constraintName) throws GRBException {
		final GRBLinExpr exprGroundTruth = new GRBLinExpr();
		exprGroundTruth.addTerm(1.0, getGRBVar());
		ilp.model.addConstr(exprGroundTruth, GRB.EQUAL, rhsValue, constraintName);
	}

	private void removeConstraint() throws GRBException {
		GRBConstr constrGroundTruth = getGrbConstr();
		if (!isNull(getGrbConstr())) {
			ilp.model.remove(constrGroundTruth);
		}
	}

	public void addLockingConstraintForStorage() {
		try {
			if (this.isChoosen()) {
				addConstraint(1.0, getStorageLockConstraintName());
			} else {
				addConstraint(0.0, getStorageLockConstraintName());
			}
		} catch (GRBException e) {
			throw new RuntimeException(e);
		}
	}

	@NotNull
	private String getStorageLockConstraintName() throws GRBException {
		return "AssignmentStorageLock_" + getGrbVarName();
	}

	/**
	 *
	 * @return null if not set, otherwise the GRBConstr.
	 */
	public GRBConstr getGroundTruthConstraint() {
		return getGrbConstr();
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
