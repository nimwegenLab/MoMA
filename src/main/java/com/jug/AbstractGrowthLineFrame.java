package com.jug;

import com.jug.lp.*;
import com.jug.util.ArgbDrawingUtils;
import com.jug.util.ComponentTreeUtils;
import net.imglib2.Localizable;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.componenttree.Component;
import net.imglib2.algorithm.componenttree.ComponentForest;
import net.imglib2.img.Img;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.ValuePair;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.Vector;

/**
 * @author jug
 * Represents one growth line (well) in which Bacteria can grow, at one
 * instance in time.
 * This corresponds to one growth line micrograph. The class
 * representing an entire time
 * series (2d+t) representation of an growth line is
 * <code>GrowthLine</code>.
 */
public abstract class AbstractGrowthLineFrame<C extends Component<FloatType, C>> {

    // -------------------------------------------------------------------------------------
    // private fields
    // -------------------------------------------------------------------------------------
    /**
     * Points at all the detected GrowthLine centers associated with this
     * GrowthLine.
     */
    private GrowthLine parent;
    private ComponentForest<C> componentTree;
    private Img<FloatType> image;

    // -------------------------------------------------------------------------------------
    // setters and getters
    // -------------------------------------------------------------------------------------

    // -------------------------------------------------------------------------------------
    // constructors
    // -------------------------------------------------------------------------------------
    AbstractGrowthLineFrame() {
    }

    public Img<FloatType> getImage() {
        return image.copy();
    }

    public void setImage(final Img<FloatType> image) {
        this.image = image;
    }

    /**
     * @return the growth line time series this one growth line is part of.
     */
    public GrowthLine getParent() {
        return parent;
    }

    /**
     * @param parent - the growth line time series this one growth line is part of.
     */
    public void setParent(final GrowthLine parent) {
        this.parent = parent;
    }

    /**
     * @return the componentTree
     */
    public ComponentForest<C> getComponentTree() { // MM-2019-06-10: This should probably be called getComponentForest?!
        return componentTree;
    }

    /**
     * @return the x-offset of the GrowthLineFrame given the original micrograph
     */
    public long getOffsetX() {
        return image.dimension(0) / 2;
    }

    /**
     * @return the y-offset of the GrowthLineFrame given the original micrograph
     */
    public long getOffsetY() {
        return 0;
    }

    /**
     * @return the f-offset of the GrowthLineFrame given the original micrograph
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
    public void generateSimpleSegmentationHypotheses(final Img<FloatType> img, int frameIndex) {
        IntervalView<FloatType> currentImage = Views.hyperSlice(img, 2, frameIndex);
        componentTree = buildIntensityTree(currentImage);
    }

    /**
     * Using the imglib2 component tree to find the most stable components
     * (bacteria).
     */
    protected abstract ComponentForest<C> buildIntensityTree(final RandomAccessibleInterval<FloatType> raiFkt);

    /**
     * @return the average X coordinate of the center line of this
     * <code>GrowthLine</code>
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
        final GrowthLineTrackingILP ilp = getParent().getIlp();
        for (final Set<AbstractAssignment<Hypothesis<Component<FloatType, ?>>>> set : ilp.getOptimalRightAssignments(this.getTime()).values()) {
            for (final AbstractAssignment<Hypothesis<Component<FloatType, ?>>> ora : set) {
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
    public int getSolutionStats_cellRank(final Hypothesis<Component<FloatType, ?>> hyp) {
        int pos = 0;

        final GrowthLineTrackingILP ilp = getParent().getIlp();
        for (final Set<AbstractAssignment<Hypothesis<Component<FloatType, ?>>>> optRightAssmnt : ilp.getOptimalRightAssignments(
                this.getTime()).values()) {

            for (final AbstractAssignment<Hypothesis<Component<FloatType, ?>>> ora : optRightAssmnt) {
                Hypothesis<Component<FloatType, ?>> srcHyp = null;
                if (ora instanceof MappingAssignment) {
                    srcHyp = ((MappingAssignment) ora).getSourceHypothesis();
                }
                if (ora instanceof DivisionAssignment) {
                    srcHyp = ((DivisionAssignment) ora).getSourceHypothesis();
                }
                if (ora instanceof ExitAssignment) {
                    srcHyp = ((ExitAssignment) ora).getAssociatedHypothesis();
                }
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
        for (final Hypothesis<Component<FloatType, ?>> hyp : getParent().getIlp().getOptimalRightAssignments(this.getTime()).keySet()) {

            final AbstractAssignment<Hypothesis<Component<FloatType, ?>>> aa = getParent().getIlp().getOptimalRightAssignments(this.getTime()).get(hyp).iterator().next();

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

    public Vector<ValuePair<Integer, Hypothesis<Component<FloatType, ?>>>> getSortedActiveHypsAndPos() {
        final Vector<ValuePair<Integer, Hypothesis<Component<FloatType, ?>>>> positionedHyps = new Vector<>();

        for (final Hypothesis<Component<FloatType, ?>> hyp : getParent().getIlp().getOptimalRightAssignments(this.getTime()).keySet()) {
            // find out where this hypothesis is located along the GL
            int min = Integer.MAX_VALUE, max = Integer.MIN_VALUE;
            for (Localizable localizable : hyp.getWrappedComponent()) {
                final int ypos = localizable.getIntPosition(0);
                min = Math.min(min, ypos);
                max = Math.max(max, ypos);
            }

            if (!hyp.isPruned()) {
                positionedHyps.add(new ValuePair<>(-max, hyp));
            }
        }

        positionedHyps.sort(Comparator.comparing(o -> o.a));

        return positionedHyps;
    }
}
