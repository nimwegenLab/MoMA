package com.jug.lp;

import com.jug.config.ConfigurationManager;
import com.jug.util.componenttree.AdvancedComponent;
import com.jug.util.componenttree.ComponentInterface;
import gurobi.GRB;
import gurobi.GRBException;
import gurobi.GRBLinExpr;
import gurobi.GRBVar;
import net.imglib2.type.numeric.real.FloatType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author Michael Mell
 */
@SuppressWarnings("restriction")
public class EnterAssignment extends AbstractAssignment<Hypothesis<AdvancedComponent<FloatType>>> {
    private final List<Hypothesis<AdvancedComponent<FloatType>>> Hup;
    private final HypothesisNeighborhoods<Hypothesis<AdvancedComponent<FloatType>>, AbstractAssignment<Hypothesis<AdvancedComponent<FloatType>>>> edges;
    private final Hypothesis<AdvancedComponent<FloatType>> targetHyp;

    /**
     * Creates an ExitAssignment.
     *
     * @param nodes
     * @param edges
     * @param targetHyp
     */
    public EnterAssignment(int sourceTimeStep, final GRBVar ilpVariable, final GrowthlaneTrackingILP ilp, final AssignmentsAndHypotheses<AbstractAssignment<Hypothesis<AdvancedComponent<FloatType>>>, Hypothesis<AdvancedComponent<FloatType>>> nodes, final HypothesisNeighborhoods<Hypothesis<AdvancedComponent<FloatType>>, AbstractAssignment<Hypothesis<AdvancedComponent<FloatType>>>> edges, final List<Hypothesis<AdvancedComponent<FloatType>>> Hup, final Hypothesis<AdvancedComponent<FloatType>> targetHyp) {
        super(GrowthlaneTrackingILP.ASSIGNMENT_ENTER, ilpVariable, ilp, sourceTimeStep);
        this.Hup = Hup;
        this.edges = edges;
        this.targetHyp = targetHyp;
    }

    /**
     * Returns the segmentation hypothesis this assignment comes from
     * (the one at the earlier time-point t).
     *
     * @return the associated segmentation-hypothesis.
     */
    @Override
    public Hypothesis<AdvancedComponent<FloatType>> getSourceHypothesis() {
        throw new RuntimeException(String.format("EnterAssignment does not have a source hypothesis (assignment id: %s).", getStringId()));
    } /* Enter assignments has no source hypothesis. */

    /**
     * Return list of target hypotheses.
     * @return
     */
    @Override
    public List<Hypothesis<AdvancedComponent<FloatType>>> getTargetHypotheses() {
        return Collections.singletonList(targetHyp);
    }

    /**
     * Add constraint that force EnterAssignments to only be the top-most assignment in the GL. If an EnterAssignment is
     * active for a cell, which is not the top-most cell, then all _incoming_ assignment above this cell must also be
     * type EnterAssignment.
     * This constraint is analogous to how we constrain ExitAssignment to be the top-most _outgoing_ assignment
     * (see method {@link ExitAssignment.addConstraintsToILP}).
     *
     * @throws GRBException
     */
    @Override
    public void addConstraintsToILP() throws GRBException {
        final GRBLinExpr expr = new GRBLinExpr();

        expr.addTerm(Hup.size(), this.getGRBVar());

        for (final Hypothesis<AdvancedComponent<FloatType>> upperHyp : Hup) {
            if (edges.getRightNeighborhood(upperHyp) != null) {
                for (final AbstractAssignment<Hypothesis<AdvancedComponent<FloatType>>> a_j : edges.getLeftNeighborhood(upperHyp)) {
                    if (a_j instanceof EnterAssignment) { // add term if assignment is NOT another EnterAssignment
                        continue;
                    }
                    expr.addTerm(1.0, a_j.getGRBVar());
                }
            }
        }

        ilp.model.addConstr(expr, GRB.LESS_EQUAL, Hup.size(), "EnterConstrT" + getSourceTimeStep() + "_" + getStringId());
    }

    /**
     * @see AbstractAssignment#getId()
     */
    @Override
    public int getId() {
        return targetHyp.getId() + GrowthlaneTrackingILP.ASSIGNMENT_ENTER;
    }

    public static String buildStringId(int sourceTimeStep, ComponentInterface target) {
        return "EnterT" + sourceTimeStep + "_" + target.getStringId();
    }
}
