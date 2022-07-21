package com.jug.lp;

import com.jug.config.ConfigurationManager;
import com.jug.util.componenttree.AdvancedComponent;
import gurobi.GRB;
import gurobi.GRBException;
import gurobi.GRBLinExpr;
import gurobi.GRBVar;
import net.imglib2.type.numeric.real.FloatType;

import java.util.ArrayList;
import java.util.List;

/**
 * @author jug
 */
@SuppressWarnings("restriction")
public class ExitAssignment extends AbstractAssignment<Hypothesis<AdvancedComponent<FloatType>>> {
    private final List<Hypothesis<AdvancedComponent<FloatType>>> Hup;
    private final HypothesisNeighborhoods<Hypothesis<AdvancedComponent<FloatType>>, AbstractAssignment<Hypothesis<AdvancedComponent<FloatType>>>> edges;
    private final Hypothesis<AdvancedComponent<FloatType>> sourceHyp;

    /**
     * Creates an ExitAssignment.
     *
     * @param nodes
     * @param edges
     * @param sourceHyp
     */
    public ExitAssignment(int sourceTimeStep, final GRBVar ilpVariable, final GrowthlaneTrackingILP ilp, final AssignmentsAndHypotheses<AbstractAssignment<Hypothesis<AdvancedComponent<FloatType>>>, Hypothesis<AdvancedComponent<FloatType>>> nodes, final HypothesisNeighborhoods<Hypothesis<AdvancedComponent<FloatType>>, AbstractAssignment<Hypothesis<AdvancedComponent<FloatType>>>> edges, final List<Hypothesis<AdvancedComponent<FloatType>>> Hup, final Hypothesis<AdvancedComponent<FloatType>> sourceHyp) {
        super(GrowthlaneTrackingILP.ASSIGNMENT_EXIT, ilpVariable, ilp, sourceTimeStep);
        this.Hup = Hup;
        this.edges = edges;
        this.sourceHyp = sourceHyp;
    }

    /**
     * Returns the segmentation hypothesis this assignment comes from
     * (the one at the earlier time-point t).
     *
     * @return the associated segmentation-hypothesis.
     */
    @Override
    public Hypothesis<AdvancedComponent<FloatType>> getSourceHypothesis() {
        return sourceHyp;
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
            ilp.model.addConstr(expr, GRB.LESS_EQUAL, Hup.size(), "ExitConstrT" + getSourceTimeStep() + "_" + getStringId());
        }
    }

    /**
     * Returns the segmentation hypothesis this exit-assignment is associated
     * with.
     *
     * @return the associated segmentation-hypothesis.
     */
    public Hypothesis<AdvancedComponent<FloatType>> getAssociatedHypothesis() {
        return sourceHyp;
    }

    /**
     * @see com.jug.lp.AbstractAssignment#getId()
     */
    @Override
    public int getId() {
        return sourceHyp.getId() + GrowthlaneTrackingILP.ASSIGNMENT_EXIT;
    }

    public static String buildStringId(int sourceTimeStep, Hypothesis sourceHypothesis) {
        return "ExitT" + sourceTimeStep + "_" + sourceHypothesis.getStringId();
    }
}
