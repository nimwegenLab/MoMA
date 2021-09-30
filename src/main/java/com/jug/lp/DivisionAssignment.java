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
public class DivisionAssignment extends AbstractAssignment<Hypothesis<AdvancedComponent<FloatType>>> {

    private final Hypothesis<AdvancedComponent<FloatType>> from;
    private final Hypothesis<AdvancedComponent<FloatType>> toUpper;
    private final Hypothesis<AdvancedComponent<FloatType>> toLower;

    /**
     * Creates an DivisionAssignment.
     *
     * @param from
     */
    public DivisionAssignment(final GRBVar ilpVariable,
                              final GrowthlaneTrackingILP ilp,
                              final Hypothesis<AdvancedComponent<FloatType>> from,
                              final Hypothesis<AdvancedComponent<FloatType>> toUpper,
                              final Hypothesis<AdvancedComponent<FloatType>> toLower) {
        super(GrowthlaneTrackingILP.ASSIGNMENT_DIVISION, ilpVariable, ilp);
        this.from = from;
        this.toUpper = toUpper;
        this.toLower = toLower;
    }

    /**
     * This method is void. DIVISION assignments do not come with assignment
     * specific constrains...
     */
    @Override
    public void addConstraintsToILP() {
    }

    /**
     * Division assignments do not come with constraints.
     */
    @Override
    public void addFunctionsAndFactors(final FactorGraphFileBuilder_SCALAR fgFile, final List<Integer> regionIds) {
    }

    /**
     * Returns the segmentation hypothesis this division-assignment comes from
     * (the one at the earlier time-point t).
     *
     * @return the associated segmentation-hypothesis.
     */
    public Hypothesis<AdvancedComponent<FloatType>> getSourceHypothesis() {
        return from;
    }

    /**
     * Returns the upper of the two segmentation hypothesis this
     * division-assignment links to (the upper of the two at the later
     * time-point t+1).
     *
     * @return the associated segmentation-hypothesis.
     */
    public Hypothesis<AdvancedComponent<FloatType>> getUpperDesinationHypothesis() {
        return toUpper;
    }

    /**
     * Returns the upper of the two segmentation hypothesis this
     * division-assignment links to (the upper of the two at the later
     * time-point t+1).
     *
     * @return the associated segmentation-hypothesis.
     */
    public Hypothesis<AdvancedComponent<FloatType>> getLowerDesinationHypothesis() {
        return toLower;
    }

    /**
     * @see com.jug.lp.AbstractAssignment#getId()
     */
    @Override
    public int getId() {
        return from.getId() + toUpper.getId() + toLower.getId() + GrowthlaneTrackingILP.ASSIGNMENT_DIVISION;
    }

    /**
     * @see com.jug.lp.AbstractAssignment#getConstraintsToSave_PASCAL()
     */
    @Override
    public List<String> getConstraintsToSave_PASCAL() {
        return new ArrayList<>();
    }
}
