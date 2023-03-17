package com.jug;

import com.jug.lp.*;
import com.jug.util.ComponentTreeUtils;
import com.jug.util.componenttree.AdvancedComponent;
import com.jug.util.componenttree.AdvancedComponentForest;
import net.imglib2.Localizable;
import net.imglib2.algorithm.componenttree.Component;
import net.imglib2.algorithm.componenttree.ComponentForest;
import net.imglib2.img.Img;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.ValuePair;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author jug
 * Represents one growth line (well) in which Bacteria can grow, at one
 * instance in time.
 * This corresponds to one growth line micrograph. The class
 * representing an entire time
 * series (2d+t) representation of an growth line is
 * <code>Growthlane</code>.
 */
public abstract class AbstractGrowthlaneFrame<C extends Component<FloatType, C>> {

    // -------------------------------------------------------------------------------------
    // private fields
    // -------------------------------------------------------------------------------------
    /**
     * Points at all the detected Growthlane centers associated with this
     * Growthlane.
     */
    private Growthlane parent;
    private AdvancedComponentForest<FloatType, AdvancedComponent<FloatType>> componentForest;
    private Img<FloatType> image;

    public Img<FloatType> getImage() {
        return image.copy();
    }

    public void setImage(final Img<FloatType> image) {
        this.image = image;
    }

    /**
     * @return the growth line time series this one growth line is part of.
     */
    public Growthlane getParent() {
        return parent;
    }

    /**
     * @param parent - the growth line time series this one growth line is part of.
     */
    public void setParent(final Growthlane parent) {
        this.parent = parent;
    }

    /**
     * @return the componentTree
     */
    public AdvancedComponentForest<FloatType, AdvancedComponent<FloatType>> getComponentForest() {
        return componentForest;
    }

    /**
     * @return the x-offset of the GrowthlaneFrame given the original micrograph
     */
    public long getOffsetX() {
        return image.dimension(0) / 2;
    }

    /**
     * @return the y-offset of the GrowthlaneFrame given the original micrograph
     */
    public long getOffsetY() {
        return 0;
    }

    /**
     * @return the f-offset of the GrowthlaneFrame given the original micrograph
     * (stack)
     */
    public long getOffsetF() {
        return parent.getFrames().indexOf(this);
    }

    // -------------------------------------------------------------------------------------
    // methods
    // -------------------------------------------------------------------------------------

    /**
     * Using the imglib2 component tree to find the most stable components
     * (bacteria).
     */
    public void generateSimpleSegmentationHypotheses() {
        componentForest = buildComponentForest();
    }

    /**
     * Using the imglib2 component tree to find the most stable components
     * (bacteria).
     */
    protected abstract AdvancedComponentForest<FloatType, AdvancedComponent<FloatType>> buildComponentForest();

    /**
     * @return the average X coordinate of the center line of this
     * <code>Growthlane</code>
     */
    public int getAvgXpos() {
        return (int) getOffsetX();
    }

    /**
     * @return the time-step this GLF corresponds to in the GL it is part of.
     */
    public int getTime() {
        return this.getParent().getFrames().indexOf(this);
    }

    /**
     * Returns the number of cells in this GLF.
     */
    public int getSolutionStats_numberOfTrackedCells() {
        int cells = 0;
        final GrowthlaneTrackingILP ilp = getParent().getIlp();
        for (final Set<AbstractAssignment<Hypothesis<AdvancedComponent<FloatType>>>> set : ilp.getOptimalRightAssignments(this.getTime()).values()) {
            for (final AbstractAssignment<Hypothesis<AdvancedComponent<FloatType>>> ora : set) {
                cells++;
            }
        }
        return cells;
    }

    /**
     * Returns the rank of the given hypothesis {@param hyp} in the GL.
     *
     * @param hyp Hypothesis for which the rank will be determined
     * @return The lower-most segmented cell returns 0. For each active
     * segment with center above {@param hyp} the return value is
     * increased by 1.
     */
    public int getSolutionStats_cellRank(final Hypothesis<AdvancedComponent<FloatType>> hyp) {
        int pos = 0;

        final GrowthlaneTrackingILP ilp = getParent().getIlp();
        for (final Set<AbstractAssignment<Hypothesis<AdvancedComponent<FloatType>>>> optRightAssmnt : ilp.getOptimalRightAssignments(
                this.getTime()).values()) {

            for (final AbstractAssignment<Hypothesis<AdvancedComponent<FloatType>>> ora : optRightAssmnt) {
                Hypothesis<AdvancedComponent<FloatType>> srcHyp = ora.getSourceHypothesis();
                if (srcHyp != null) {
                    if (ComponentTreeUtils.isAbove(hyp, srcHyp)) {
                        pos++;
                    }
                }
            }
        }
        return pos;
    }

    public Vector<ValuePair<ValuePair<Integer, Integer>, ValuePair<Integer, Integer>>> getSolutionStats_limitsAndRightAssType() {
        final Vector<ValuePair<ValuePair<Integer, Integer>, ValuePair<Integer, Integer>>> ret = new Vector<>();
        for (final Hypothesis<AdvancedComponent<FloatType>> hyp : getParent().getIlp().getOptimalRightAssignments(this.getTime()).keySet()) {

            final AbstractAssignment<Hypothesis<AdvancedComponent<FloatType>>> aa = getParent().getIlp().getOptimalRightAssignments(this.getTime()).get(hyp).iterator().next();

            int min = Integer.MAX_VALUE, max = Integer.MIN_VALUE;
            for (Localizable localizable : hyp.getWrappedComponent()) {
                final int ypos = localizable.getIntPosition(0);
                min = Math.min(min, ypos);
                max = Math.max(max, ypos);
            }

            ret.add(new ValuePair<>(new ValuePair<>(min, max), new ValuePair<>(aa.getType(), (aa.isGroundTruth() || aa.isGroundUntruth()) ? 1 : 0)));
        }

        ret.sort(Comparator.comparing(o -> o.a.a));
        return ret;
    }

    public List<Hypothesis<AdvancedComponent<FloatType>>> getSortedActiveHypsAndPos() {
        List<Hypothesis<AdvancedComponent<FloatType>>> hyps2 = new ArrayList(getParent().getIlp().getOptimalHypotheses(getTime()));
        hyps2.sort(Comparator.comparing(o -> -o.getWrappedComponent().getVerticalComponentLimits().getB())); /* return list of hypotheses sorted by the inverse value of their bottom boundary; taking the inverse gives the mother-cell the smallest value and makes it first in the list */
        return hyps2;

        /**
         * TODO-MM-20230307: Replace the above code with dedicate methods that do this; i.e. something like this (this still needs to be tested):
         * List<Hypothesis<AdvancedComponent<FloatType>>> hypothesesOfInterest = getParent().getIlp().getHypothesesAt(this.getTime()).stream().filter(hyp -> hyp.isActive()).filter(hyp -> !hyp.isPruned()).collect(Collectors.toList());
         * hypothesesOfInterest.sort(Comparator.comparing(hyp -> hyp.getWrappedComponent().getVerticalComponentLimits().getB()));
         **/
    }
}
