package com.jug.lp;

import com.jug.export.FactorGraphFileBuilder_SCALAR;
import com.jug.util.componenttree.AdvancedComponent;
import gurobi.GRBVar;
import net.imglib2.type.numeric.real.FloatType;

import java.util.ArrayList;
import java.util.List;

/**
 * @author jug
 */
public class MappingAssignment extends AbstractAssignment<Hypothesis<AdvancedComponent<FloatType>>> {

    private final Hypothesis<AdvancedComponent<FloatType>> from;
    private final Hypothesis<AdvancedComponent<FloatType>> to;

    /**
     * Creates an MappingAssignment.
     *
     * @param nodes
     * @param edges
     * @param from
     * @param to
     */
    public MappingAssignment(final int t, final GRBVar ilpVariable, final GrowthlaneTrackingILP ilp, final AssignmentsAndHypotheses<AbstractAssignment<Hypothesis<AdvancedComponent<FloatType>>>, Hypothesis<AdvancedComponent<FloatType>>> nodes, final HypothesisNeighborhoods<Hypothesis<AdvancedComponent<FloatType>>, AbstractAssignment<Hypothesis<AdvancedComponent<FloatType>>>> edges, final Hypothesis<AdvancedComponent<FloatType>> from, final Hypothesis<AdvancedComponent<FloatType>> to) {
        super(GrowthlaneTrackingILP.ASSIGNMENT_MAPPING, ilpVariable, ilp);
        this.from = from;
        this.to = to;
    }

    /**
     * This method is void. MAPPING assignments do not come with assignment
     * specific constrains...
     */
    @Override
    public void addConstraintsToILP() {
    }

    /**
     * Mapping assignments do not come with constraints.
     */
    @Override
    public void addFunctionsAndFactors(final FactorGraphFileBuilder_SCALAR fgFile, final List<Integer> regionIds) {
    }

    /**
     * Returns the segmentation hypothesis this mapping-assignment comes from
     * (the one at the earlier time-point t).
     *
     * @return the associated segmentation-hypothesis.
     */
    @Override
    public Hypothesis<AdvancedComponent<FloatType>> getSourceHypothesis() {
        return from;
    }

    /**
     * Returns the segmentation hypothesis this mapping-assignment links to
     * (the one at the later time-point t+1).
     *
     * @return the associated segmentation-hypothesis.
     */
    public Hypothesis<AdvancedComponent<FloatType>> getDestinationHypothesis() {
        return to;
    }

    /**
     * @see com.jug.lp.AbstractAssignment#getId()
     */
    @Override
    public int getId() {
        return from.getId() + to.getId() + GrowthlaneTrackingILP.ASSIGNMENT_MAPPING;
    }

    /**
     * @see com.jug.lp.AbstractAssignment#getConstraintsToSave_PASCAL()
     */
    @Override
    public List<String> getConstraintsToSave_PASCAL() {
        return new ArrayList<>();
    }

}
