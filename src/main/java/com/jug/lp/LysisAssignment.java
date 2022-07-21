package com.jug.lp;

import com.jug.export.FactorGraphFileBuilder_SCALAR;
import com.jug.util.componenttree.AdvancedComponent;
import gurobi.GRBException;
import gurobi.GRBVar;
import net.imglib2.type.numeric.real.FloatType;

import java.util.ArrayList;
import java.util.List;

/**
 * @author jug
 */
@SuppressWarnings("restriction")
public class LysisAssignment extends AbstractAssignment<Hypothesis<AdvancedComponent<FloatType>>> {
    private final Hypothesis<AdvancedComponent<FloatType>> who;

    /**
     * Creates an ExitAssignment.
     *
     * @param who
     */
    public LysisAssignment(int sourceTimeStep, final GRBVar ilpVariable, final GrowthlaneTrackingILP ilp, final Hypothesis<AdvancedComponent<FloatType>> who) {
        super(GrowthlaneTrackingILP.ASSIGNMENT_LYSIS, ilpVariable, ilp, sourceTimeStep);
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
     * This method is void. Lysis assignments do not come with assignment
     * specific constrains...
     */
    @Override
    public void addConstraintsToILP() throws GRBException {
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
     * @see AbstractAssignment#getId()
     */
    @Override
    public int getId() {
        return who.getId() + GrowthlaneTrackingILP.ASSIGNMENT_LYSIS;
    }

    public static String buildStringId(int sourceTimeStep, Hypothesis sourceHypothesis) {
        return "LysT" + sourceTimeStep + "_" + sourceHypothesis.getStringId();
    }
}
