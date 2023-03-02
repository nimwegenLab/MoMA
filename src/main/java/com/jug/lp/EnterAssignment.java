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
        super(GrowthlaneTrackingILP.ASSIGNMENT_EXIT, ilpVariable, ilp, sourceTimeStep);
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
        return null;
    } /* Enter assignments has no source hypothesis. */

    /**
     * Return list of target hypotheses.
     * @return
     */
    @Override
    public List<Hypothesis<AdvancedComponent<FloatType>>> getTargetHypotheses(){
        return new ArrayList<>();
    }

    /**
     * @throws GRBException
     */
    @Override
    public void addConstraintsToILP() throws GRBException { // builds equation 8, jug paper
        /* TODO-MM-20230302: this does nothing for the moment; we will need to add a constraint later, which only
        * allow enter assignments for the top-most cells - similar to how the exit assignment is handled.
        */
    }

    /**
     * @see AbstractAssignment#getId()
     */
    @Override
    public int getId() {
        return targetHyp.getId() + GrowthlaneTrackingILP.ASSIGNMENT_ENTER;
    }

    public static String buildStringId(int targetTimeStep, ComponentInterface target) {
        return "EnterT" + targetTimeStep + "_" + target.getStringId();
    }
}
