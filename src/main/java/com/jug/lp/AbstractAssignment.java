package com.jug.lp;

import com.jug.util.componenttree.ComponentInterface;
import gurobi.*;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
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
public abstract class AbstractAssignment<H extends Hypothesis<?>> {

	private final int sourceTimeStep;
	private int type;

	GrowthlaneTrackingILP ilp;

	private GRBVar ilpVar;

	private boolean isPruned = false;

	/**
	 * Creates an assignment...
	 */
	AbstractAssignment(final int type, final GRBVar ilpVariable, final GrowthlaneTrackingILP ilp, int sourceTimeStep) {
		this.sourceTimeStep = sourceTimeStep;
		this.type = type;
		setGRBVar( ilpVariable );
		setGrowthlaneTrackingILP( ilp );
	}

	abstract public int getId();

	/**
	 * @return the type
	 */
	public int getType() {
		return type;
	}

	abstract public H getSourceHypothesis();

	abstract public List<H> getTargetHypotheses();

	public H getTargetHypothesis(int hypothesisInd) {
		return getTargetHypotheses().get(hypothesisInd);
	}

	public ComponentInterface getTargetComponent(int componentInd) {
		return getTargetHypothesis(componentInd).getWrappedComponent();
	}

	public List<ComponentInterface> getTargetComponents() {
		List<ComponentInterface> list = new ArrayList<ComponentInterface>();
		for (Hypothesis hyp : getTargetHypotheses()) {
			list.add(getSourceHypothesis().getWrappedComponent());
		}
		return list;
	}

	/**
	 * @return the ilpVar
	 */
	public GRBVar getGRBVar() {
		return ilpVar;
	}

	private String stringId;

	public String getStringId() {
		if (isNull(stringId)) {
			try {
				stringId = getGRBVar().get(GRB.StringAttr.VarName);
			} catch (GRBException err) {
				throw new RuntimeException("Could not retrieve name of the Gurobi variable of assignment.");
			}
		}
		return stringId;
	}

	@NotNull
	private String getStorageLockConstraintName() {
		return "StoreLockConstr_" + getStringId();
	}

	public static String getPreOptimizationRangeConstraintNamePrefix(){
		return "PreOptimRangeLockConstr_";
	}

	private String preOptimizationRangeLockConstraintName;

	@NotNull
	private String getPreOptimizationRangeLockConstraintName() {
		if (isNull(preOptimizationRangeLockConstraintName)) {
			preOptimizationRangeLockConstraintName = getPreOptimizationRangeConstraintNamePrefix() + getStringId();
		}
		return preOptimizationRangeLockConstraintName;
	}

	public static String getPostOptimizationRangeConstraintNamePrefix() {
		return "PostOptimRangeLockConstr_";
	}

	private String postOptimizationRangeLockConstraintName;
	@NotNull
	private String getPostOptimizationRangeLockConstraintName() {
		if(isNull(postOptimizationRangeLockConstraintName)){
			postOptimizationRangeLockConstraintName = getPostOptimizationRangeConstraintNamePrefix() + getStringId();
		}
		return postOptimizationRangeLockConstraintName;
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

	public boolean ilpIsReady() {
		return ilp.isReady();
	}

	/**
	 * @return the cost
	 * @throws RuntimeException if the underlying Gurobi model is not ready to be queried for the component cost.
	 */
	public float getCost() {
		try {
			return (float) getGRBVar().get(GRB.DoubleAttr.Obj);
		} catch (final GRBException e) {
			throw new RuntimeException("ERROR: Assignment cost could not be read from Gurobi model for assignment: " + getStringId());
		}
	}

	/**
	 * @return true, if the ilpVar of this Assignment is equal to 1.0.
	 */
	private boolean previousIsChoosen = false;
	public boolean isChosen() throws GRBException {
		if (ilp.getStatus() == IlpStatus.OPTIMIZATION_NEVER_PERFORMED)
			throw new GRBException();  /* ilp.getStatus() == 0: corresponds to OPTIMIZATION_NEVER_PERFORMED; this hack is needed to stay compatible, because the first time that isChosen() is called from program code, it throws GRBException. And this first call is needed to run the first optimization and initialize `previousIsChoosen`. Furthermore, we cannot simply return `previousIsChoosen=false`, because then the state of the assignments will not be correctly initialized. */
		if (ilp.getStatus() == IlpStatus.OPTIMIZATION_IS_RUNNING || ilp.getStatus() == IlpStatus.UNDEFINED) {
			return previousIsChoosen;
		}
		try {
			boolean result = getIsChosenStateFromIlpCached();
			previousIsChoosen = result;
			return previousIsChoosen;
		} catch (GRBException err) {
			return previousIsChoosen; /* This will be returned in case the ILP optimization fails. In that case GRBException will be thrown. We then return the previous state of all assignments, so that the user can correct any mistakes she mad. */
		}
	}

	Boolean isChosen = null;

	public boolean getIsChosenStateFromIlpCached() throws GRBException {
		if(isNull(isChosen)){
			isChosen = getIsChosenStateFromIlp();
		}
		return isChosen;
	}

	private boolean getIsChosenStateFromIlp() throws GRBException {
		GRBVar grbVarOfAssignment = getGRBVar();
		long binary_selection_variable_value_rounded = Math.round(grbVarOfAssignment.get(GRB.DoubleAttr.X));
		return (binary_selection_variable_value_rounded == 1);
	}

	/**
	 * Abstract method that will, once implemented, add a set of assignment
	 * related constraints to the ILP (model) later to be solved by Gurobi.
	 */
	public abstract void addConstraintsToILP() throws GRBException;

	public void invalidateCache() {
		isGroundTruth = null;
		isGroundUntruth = null;
		isChosen = null;
	}

	public void cache() {
		invalidateCache();
		isGroundTruth = isGroundTruth();
		isGroundUntruth = isGroundUntruth();
		try {
			isChosen = isChosen();
		} catch (GRBException e) {
			throw new RuntimeException(e);
		}
	}

	Boolean isGroundTruth = null;

	public boolean isGroundTruth() {
		if (isNull(isGroundTruth)) {
			isGroundTruth = isGroundTruthInternal();
		}
		return isGroundTruth;
	}

	private boolean isGroundTruthInternal() {
		GRBConstr grbConstr = getGroundTruthConstraint();
		if (isNull(grbConstr)) {
			return false; /* no variable was found so this assignment is not forced */
		}

		Double constraintValue;
		try {
			constraintValue = grbConstr.get(GRB.DoubleAttr.RHS);
		} catch (GRBException e) {
			throw new RuntimeException(e);
		}
		return constraintValue > 0.5; /* condition for ground truth is LHS value == 0; we test against due 0.5 for numerical precision */
	}

	@NotNull
	private String getGroundTruthConstraintName() {
		return "GroundTruthConstr_" + getStringId();
	}

	Boolean isGroundUntruth = null;

	public boolean isGroundUntruth(){
		if(isNull(isGroundUntruth)){
			isGroundUntruth = isGroundUntruthInternal();
		}
		return isGroundUntruth;
	}

	private boolean isGroundUntruthInternal() {
		GRBConstr grbConstr = getGroundTruthConstraint();
		if (isNull(grbConstr)) {
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

	public void setGroundTruth(final boolean targetState) {
		try {
			if (targetState == isGroundTruth()) {
				return;
			}
			if (isGroundUntruth() && targetState) {
				removeGroundUntruthConstraint();
			}
			if (targetState) {
				addGroundTruthConstraint();
			} else {
				removeGroundTruthConstraint();
			}
			invalidateCache();
		} catch (GRBException e) {
			throw new RuntimeException(e);
		}
	}

	public void setGroundUntruth(final boolean targetState) {
		try {
			if (targetState == isGroundUntruth()) {
				return;
			}
			if (isGroundTruth() && targetState) {
				removeGroundTruthConstraint();
			}
			if (targetState) {
				addGroundUntruthConstraint();
			} else {
				removeGroundUntruthConstraint();
			}
			invalidateCache();
		} catch (GRBException e) {
			throw new RuntimeException(e);
		}
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
		addConstraint(1.0, getGroundTruthConstraintName());
	}

	private void addGroundUntruthConstraint() throws GRBException {
		addConstraint(0.0, getGroundTruthConstraintName());
	}

	private void addConstraint(double rhsValue, String constraintName) throws GRBException {
		if (constraintExistsWithName(constraintName)) {
			System.out.println("WARNING: Tried to add constraint (\""+constraintName+"\"), which was already in the model.");
			return;
		}
		final GRBLinExpr exprGroundTruth = new GRBLinExpr();
		exprGroundTruth.addTerm(1.0, getGRBVar());
		ilp.model.addConstr(exprGroundTruth, GRB.EQUAL, rhsValue, constraintName);
	}

	private void addFreezeConstraintWithName(String constraintName) {
		try {
			if (this.isChosen()) {
				addConstraint(1.0, constraintName);
			} else {
				addConstraint(0.0, constraintName);
			}
		} catch (GRBException e) {
			throw new RuntimeException(e);
		}
	}

	private boolean constraintExistsWithName(String constraintName){
		try {
			return !isNull(ilp.model.getConstrByName(constraintName));
		} catch (GRBException e) {
			throw new RuntimeException(e);
		}
	}

	private void removeGroundTruthConstraint() throws GRBException {
		removeConstraintWithName(getGroundTruthConstraintName());
	}

	private void removeGroundUntruthConstraint() throws GRBException {
		removeGroundTruthConstraint(); /* the constraint for ground-truth and ground untruth have the same name, so that the remove function is the same; hence this is just an alias */
	}


	private void removeConstraintWithName(String constraintName) {
		GRBConstr constraint = getConstraint(constraintName);
		if (!isNull(constraint)) {
			try {
				ilp.model.remove(constraint);
			} catch (GRBException e) {
				throw new RuntimeException(e);
			}
		}
	}

	public void addStorageLockConstraint() {
		addFreezeConstraintWithName(getStorageLockConstraintName());
	}

	public void removeStorageLockConstraint() {
		removeConstraintWithName(getStorageLockConstraintName());
	}

	public boolean hasPreOptimizationRangeLockConstraint() {
		return constraintExistsWithName(getPreOptimizationRangeLockConstraintName());
	}

	public void addPreOptimizationRangeLockConstraint() {
		addFreezeConstraintWithName(getPreOptimizationRangeLockConstraintName());
	}

	public void removePreOptimizationRangeLockConstraint() {
		removeConstraintWithName(getPreOptimizationRangeLockConstraintName());
	}

	public void addPostOptimizationRangeLockConstraint() {
		addFreezeConstraintWithName(getPostOptimizationRangeLockConstraintName());
	}

	public void removePostOptimizationRangeLockConstraint() {
		removeConstraintWithName(getPostOptimizationRangeLockConstraintName());
	}

	public boolean hasPostOptimizationRangeLockConstraint() {
		return constraintExistsWithName(getPostOptimizationRangeLockConstraintName());
	}

	/**
	 *
	 * @return null if not set, otherwise the GRBConstr.
	 */
	public GRBConstr getGroundTruthConstraint() {
		return getConstraint(getGroundTruthConstraintName());
	}

	private GRBConstr getConstraint(String constraintName) {
		GRBConstr grbConstr;
		try {
			grbConstr = ilp.model.getConstrByName(constraintName);
		} catch (GRBException e) {
			return null;
		}
		return grbConstr;
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

	public int getSourceTimeStep() {
		return sourceTimeStep;
	}

	public int getTargetTimeStep() {
		return sourceTimeStep + 1;
	}

	public String toString() {
		return getStringId();
	}
}
