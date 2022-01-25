package com.jug.lp;

import com.jug.config.ConfigurationManager;
import com.jug.export.FactorGraphFileBuilder_SCALAR;
import com.jug.util.componenttree.AdvancedComponent;
import gurobi.GRB;
import gurobi.GRBException;
import gurobi.GRBLinExpr;
import gurobi.GRBVar;
import net.imglib2.type.numeric.real.FloatType;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author jug
 */
@SuppressWarnings("restriction")
public class ExitAssignment extends AbstractAssignment<Hypothesis<AdvancedComponent<FloatType>>> {

    private static int dcId = 0;
    private final List<Hypothesis<AdvancedComponent<FloatType>>> Hup;
    private final HypothesisNeighborhoods<Hypothesis<AdvancedComponent<FloatType>>, AbstractAssignment<Hypothesis<AdvancedComponent<FloatType>>>> edges;
    private final Hypothesis<AdvancedComponent<FloatType>> who;

    /**
     * Creates an ExitAssignment.
     *
     * @param nodes
     * @param edges
     * @param who
     */
    public ExitAssignment(final GRBVar ilpVariable, final GrowthlaneTrackingILP ilp, final AssignmentsAndHypotheses<AbstractAssignment<Hypothesis<AdvancedComponent<FloatType>>>, Hypothesis<AdvancedComponent<FloatType>>> nodes, final HypothesisNeighborhoods<Hypothesis<AdvancedComponent<FloatType>>, AbstractAssignment<Hypothesis<AdvancedComponent<FloatType>>>> edges, final List<Hypothesis<AdvancedComponent<FloatType>>> Hup, final Hypothesis<AdvancedComponent<FloatType>> who) {
        super(GrowthlaneTrackingILP.ASSIGNMENT_EXIT, ilpVariable, ilp);
        this.Hup = Hup;
        this.edges = edges;
        this.who = who;
    }

    /**
     * Returns the segmentation hypothesis this assignment comes from
     * (the one at the earlier time-point t).
     *
     * @return the associated segmentation-hypothesis.
     */
    @Override
    public Hypothesis<AdvancedComponent<FloatType>> getSourceHypothesis() {
        return who;
    }

    /**
     * Return list of target hypotheses.
     * @return
     */
    @Override
    public List<Hypothesis<AdvancedComponent<FloatType>>> getTargetHypotheses(){
        return new ArrayList<>(); /* Lysis assignment has no target hypothesis. */
    }

    /**
     * @throws GRBException
     */
    @Override
    public void addConstraintsToILP() throws GRBException { // builds equation 8, jug paper
        final GRBLinExpr expr = new GRBLinExpr();

        expr.addTerm(Hup.size(), this.getGRBVar());

        boolean add = false;
        for (final Hypothesis<AdvancedComponent<FloatType>> upperHyp : Hup) {
            if (edges.getRightNeighborhood(upperHyp) != null) {
                for (final AbstractAssignment<Hypothesis<AdvancedComponent<FloatType>>> a_j : edges.getRightNeighborhood(upperHyp)) {
                    add = true;
                    if (a_j.getType() == GrowthlaneTrackingILP.ASSIGNMENT_EXIT) {
                        continue;
                    }
                    // add term if assignment is NOT another exit-assignment
                    expr.addTerm(1.0, a_j.getGRBVar());
                }
            }
        }

        if (add && !ConfigurationManager.DISABLE_EXIT_CONSTRAINTS) {
            ilp.model.addConstr(expr, GRB.LESS_EQUAL, Hup.size(), "ExitConstraint_" + this.getId() + "_" + dcId);
        }
        dcId++;
    }

    /**
     * @see com.jug.lp.AbstractAssignment#getConstraintsToSave_PASCAL()
     */
    @Override
    public List<String> getConstraintsToSave_PASCAL() {
        final ArrayList<String> ret = new ArrayList<>();

        StringBuilder constraint = new StringBuilder();
        constraint.append(String.format("(%d,%d,1)", Hup.size(), this.getVarIdx()));

        for (final Hypothesis<AdvancedComponent<FloatType>> upperHyp : Hup) {
            if (edges.getRightNeighborhood(upperHyp) != null) {
                for (final AbstractAssignment<Hypothesis<AdvancedComponent<FloatType>>> a_j : edges.getRightNeighborhood(upperHyp)) {
                    if (a_j.getType() == GrowthlaneTrackingILP.ASSIGNMENT_EXIT) {
                        continue;
                    }
                    // add term if assignment is NOT another exit-assignment
                    constraint.append(String.format("+(1,%d,1)", a_j.getVarIdx()));
                }
            }
        }

        constraint.append(String.format(" <= %d", Hup.size()));

        ret.add(constraint.toString());
        return ret;
    }

    /**
     * Adds a list of constraints and factors as strings.
     */
    @Override
    public void addFunctionsAndFactors(final FactorGraphFileBuilder_SCALAR fgFile, final List<Integer> regionIds) {
        final List<Integer> varIds = new ArrayList<>();
        final List<Integer> coeffs = new ArrayList<>();

        // expr.addTerm( Hup.size(), this.getGRBVar() );
        coeffs.add(Hup.size());
//		varIds.add( new Integer( this.getVarIdx() ) );

        for (final Hypothesis<AdvancedComponent<FloatType>> upperHyp : Hup) {
            if (edges.getRightNeighborhood(upperHyp) != null) {
                for (final AbstractAssignment<Hypothesis<AdvancedComponent<FloatType>>> a_j : edges.getRightNeighborhood(upperHyp)) {
                    if (a_j.getType() == GrowthlaneTrackingILP.ASSIGNMENT_EXIT) {
                        continue;
                    }
                    // add term if assignment is NOT another exit-assignment
                    // expr.addTerm( 1.0, a_j.getGRBVar() );
                    coeffs.add(1);
//					varIds.add( new Integer( a_j.getVarIdx() ) );
                }
            }
        }

        // model.addConstr( expr, GRB.LESS_EQUAL, Hup.size(), "dc_" + dcId );
        final int fkt_id = fgFile.addConstraintFkt(coeffs, "<=", Hup.size());
        fgFile.addFactor(fkt_id, varIds, regionIds);
    }

    /**
     * Returns the segmentation hypothesis this exit-assignment is associated
     * with.
     *
     * @return the associated segmentation-hypothesis.
     */
    public Hypothesis<AdvancedComponent<FloatType>> getAssociatedHypothesis() {
        return who;
    }

    /**
     * @see com.jug.lp.AbstractAssignment#getId()
     */
    @Override
    public int getId() {
        return who.getId() + GrowthlaneTrackingILP.ASSIGNMENT_EXIT;
    }

    public static String buildStringId(int sourceTimeStep, Hypothesis sourceHypothesis) {
        return "ExitAssignmentAtTime" + sourceTimeStep + "_" + sourceHypothesis.getStringId();
    }
}
