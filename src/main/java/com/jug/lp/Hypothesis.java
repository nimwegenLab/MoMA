package com.jug.lp;

import com.jug.util.ComponentTreeUtils;
import com.jug.util.componenttree.AdvancedComponent;
import gurobi.GRBConstr;
import gurobi.GRBException;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.ValuePair;

import java.util.ArrayList;
import java.util.LinkedList;

/**
 * This class is used to wrap away whatever object that represents one of the
 * segmentation hypothesis. See {@link AbstractAssignment} for a place where
 * this is
 * used.
 *
 * @author jug
 */
@SuppressWarnings("restriction")
public class Hypothesis<T extends AdvancedComponent<FloatType>> {

    private final T wrappedComponent;
    private final float cost;
    private final HypLoc location;
    public ArrayList<String> labels = new ArrayList<>();
    public boolean isForced = false;
    public boolean isIgnored = false;
    /**
     * Used to store a 'segment in solution constraint' after it was added to
     * the ILP. If such a constraint does not exist for this hypothesis, this
     * value is null.
     */
    private GRBConstr segmentSpecificConstraint = null;
    /**
     * Used to store track-branch pruning sources. This is a way to easily
     * exclude branches from data export etc.
     */
    private boolean isPruneRoot = false;
    private boolean isPruned = false;
    public Hypothesis(final int t, final T elementToWrap, final float cost) {
        // setSegmentHypothesis( elementToWrap );
        this.wrappedComponent = elementToWrap;
        this.cost = cost;
        location = new HypLoc(t, elementToWrap);
    }

    public int getId() {
        return location.limits.getA() * 1000 + location.limits.getB();  // TODO-MM20210721: Why the factor 1000?! This would better be a hash-value based on (multiple) hypothesis properties (e.g. max. segment probability, segment limits, etc.).
    }

    public String getStringId() {
        return "HypAtT" + location.t + "Top" + location.limits.getA() + "Bottom" + location.limits.getB();
    }

    /**
     * @return the wrapped segmentHypothesis
     */
    public T getWrappedComponent() {
        return wrappedComponent;
    }

    /**
     * @return the costs
     */
    public float getCost() {
        return cost;
    }

    // /**
    // * @param elementToWrap
    // * the segmentHypothesis to wrap inside this
    // * {@link Hypothesis}
    // */
    // public void setSegmentHypothesis( final T elementToWrap ) {
    // this.wrappedHypothesis = elementToWrap;
    // }

    /**
     * @return the stored gurobi constraint that either forces this hypothesis
     * to be part of any solution to the ILP or forces this hypothesis
     * to be NOT included. Note: this function returns 'null' if such a
     * constraint was never created.
     */
    public GRBConstr getSegmentSpecificConstraint() {
        return this.segmentSpecificConstraint;
    }

    /**
     * Used to store a 'segment in solution constraint' or a 'segment not in
     * solution constraint' after it was added to the ILP.
     *
     * @param constr the installed constraint.
     */
    public void setSegmentSpecificConstraint(final GRBConstr constr) {
        this.segmentSpecificConstraint = constr;
    }

    public ValuePair<Integer, Integer> getLocation() {
        return location.limits;
    }

    public HypLoc getHypLoc() {
        return location;
    }

    public int getTime() {
        return location.t;
    }

    /**
     *
     */
    public void setPruneRoot(final boolean value, final GrowthlaneTrackingILP ilp) {

        this.isPruneRoot = value;

        final LinkedList<Hypothesis<AdvancedComponent<FloatType>>> queue =
                new LinkedList<>();
        // TODO there will be no time, but this is of course not nice...
        queue.add((Hypothesis<AdvancedComponent<FloatType>>) this);
        while (!queue.isEmpty()) {
            final Hypothesis<AdvancedComponent<FloatType>> node = queue.removeFirst();
            node.setPruned(value);

            AbstractAssignment<Hypothesis<AdvancedComponent<FloatType>>> assmnt;
            try {
                assmnt = ilp.getOptimalRightAssignment(node);

                if (assmnt != null) {
                    assmnt.setPruned(value);

                    switch (assmnt.getType()) {
                        case GrowthlaneTrackingILP.ASSIGNMENT_DIVISION:
                            if (!((DivisionAssignment) assmnt).getUpperDestinationHypothesis().isPruneRoot()) {
                                queue.add(((DivisionAssignment) assmnt).getUpperDestinationHypothesis());
                            }
                            if (!((DivisionAssignment) assmnt).getLowerDestinationHypothesis().isPruneRoot()) {
                                queue.add(((DivisionAssignment) assmnt).getLowerDestinationHypothesis());
                            }
                            break;
                        case GrowthlaneTrackingILP.ASSIGNMENT_MAPPING:
                            if (!((MappingAssignment) assmnt).getDestinationHypothesis().isPruneRoot()) {
                                queue.add(((MappingAssignment) assmnt).getDestinationHypothesis());
                            }
                            break;
                    }
                }
            } catch (final GRBException e) {
//				e.printStackTrace();
            }
        }
    }

    /**
     * @return
     */
    public boolean isPruneRoot() {
        return isPruneRoot;
    }

    /**
     * @return
     */
    public boolean isPruned() {
        return this.isPruned;
    }

    /**
     * @param value
     */
    void setPruned(final boolean value) {
        this.isPruned = value;
    }

    public class HypLoc {

        final int t;
        final ValuePair<Integer, Integer> limits;

        HypLoc(final int t, final T segment) {
            this.t = t;
            this.limits = ComponentTreeUtils.getTreeNodeInterval(segment);
        }
    }
}
