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

    private static final int dcId = 0;
    private int sourceTimeStep;
    private final HypothesisNeighborhoods<Hypothesis<AdvancedComponent<FloatType>>, AbstractAssignment<Hypothesis<AdvancedComponent<FloatType>>>> edges;
    private final Hypothesis<AdvancedComponent<FloatType>> who;

    /**
     * Creates an ExitAssignment.
     *
     * @param nodes
     * @param edges
     * @param who
     */
    public LysisAssignment(int sourceTimeStep, final GRBVar ilpVariable, final GrowthlaneTrackingILP ilp, final AssignmentsAndHypotheses<AbstractAssignment<Hypothesis<AdvancedComponent<FloatType>>>, Hypothesis<AdvancedComponent<FloatType>>> nodes, final HypothesisNeighborhoods<Hypothesis<AdvancedComponent<FloatType>>, AbstractAssignment<Hypothesis<AdvancedComponent<FloatType>>>> edges, final Hypothesis<AdvancedComponent<FloatType>> who) {
        super(GrowthlaneTrackingILP.ASSIGNMENT_LYSIS, ilpVariable, ilp);
        this.sourceTimeStep = sourceTimeStep;
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
     * This method is void. Lysis assignments do not come with assignment
     * specific constrains...
     */
    @Override
    public void addConstraintsToILP() throws GRBException {
    }

    /**
     * @see AbstractAssignment#getConstraintsToSave_PASCAL()
     */
    @Override
    public List<String> getConstraintsToSave_PASCAL() {
        return null;
    }

    /**
     * Adds a list of constraints and factors as strings.
     */
    @Override
    public void addFunctionsAndFactors(final FactorGraphFileBuilder_SCALAR fgFile, final List<Integer> regionIds) {
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
        return "LysisAtT" + sourceTimeStep + "_" + sourceHypothesis.getStringId();
    }
}
