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
 *         Represents one growth line (well) in which Bacteria can grow, at one
 *         instance in time.
 *         This corresponds to one growth line micrograph. The class
 *         representing an entire time
 *         series (2d+t) representation of an growth line is
 *         <code>GrowthLine</code>.
 */
public abstract class AbstractGrowthLineFrame< C extends Component< FloatType, C > > {

	// -------------------------------------------------------------------------------------
	// private fields
	// -------------------------------------------------------------------------------------
	/**
	 * Points at all the detected GrowthLine centers associated with this
	 * GrowthLine.
	 */
	private GrowthLine parent;
	private ComponentForest< C > componentTree;
	private Img<FloatType> image;

	// -------------------------------------------------------------------------------------
	// setters and getters
	// -------------------------------------------------------------------------------------

    public void setImage(final Img<FloatType> image){
        this.image = image;
    }

    public Img<FloatType> getImage(){
        return image.copy();
    }

	/**
	 * @return the growth line time series this one growth line is part of.
	 */
	public GrowthLine getParent() {
		return parent;
	}

	/**
	 * @param parent
	 *            - the growth line time series this one growth line is part of.
	 */
	public void setParent( final GrowthLine parent ) {
		this.parent = parent;
	}

	/**
	 * @return the componentTree
	 */
	public ComponentForest< C > getComponentTree() { // MM-2019-06-10: This should probably be called getComponentForest?!
		return componentTree;
	}

	/**
	 * @return the x-offset of the GrowthLineFrame given the original micrograph
	 */
	public long getOffsetX() {
	    return image.dimension(0)/2;
    }

	/**
	 * @return the y-offset of the GrowthLineFrame given the original micrograph
	 */
	public long getOffsetY() {
		return 0;
	}

	/**
	 * @return the f-offset of the GrowthLineFrame given the original micrograph
	 *         (stack)
	 */
	public long getOffsetF() {
		return parent.getFrames().indexOf( this );
	}

	// -------------------------------------------------------------------------------------
	// constructors
	// -------------------------------------------------------------------------------------
	AbstractGrowthLineFrame() {}

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
	protected abstract ComponentForest< C > buildIntensityTree( final RandomAccessibleInterval< FloatType > raiFkt );

	/**
	 * Draws the optimal segmentation (determined by the solved ILP) into the
	 * given <code>Img</code>.
	 *
	 * @param img
	 *            the Img to draw into.
	 * @param view
	 *            the active view on that Img (in order to know the pixel
	 *            offsets)
	 * @param optimalSegmentation
	 *            a <code>List</code> of the hypotheses containing
	 *            component-tree-nodes that represent the optimal segmentation
	 *            (the one returned by the solution to the ILP).
	 */
	public void drawOptimalSegmentation( final Img< ARGBType > img, final IntervalView< FloatType > view, final List< Hypothesis< Component< FloatType, ? >>> optimalSegmentation ) {
		final RandomAccess< ARGBType > raAnnotationImg = img.randomAccess();

		long offsetX = 0;
		long offsetY = 0;

		if ( view != null ) {
			// Lord, forgive me!
			if ( view.min( 0 ) == 0 ) {
				// In case I give the cropped paramaxflow-baby I lost the offset and must do ugly shit...
				// I promise this is only done because I need to finish the f****** paper!
				offsetX = -( this.getAvgXpos() - MoMA.GL_WIDTH_IN_PIXELS / 2 - MoMA.GL_PIXEL_PADDING_IN_VIEWS );
				offsetY = view.min( 1 );
			} else {
				offsetX = view.min( 0 );
				offsetY = view.min( 1 );
			}
		}

		for ( final Hypothesis< Component< FloatType, ? >> hyp : optimalSegmentation ) {
			final Component< FloatType, ? > ctn = hyp.getWrappedComponent();
			if ( hyp.isPruned() ) {
				ArgbDrawingUtils.taintPrunedComponentTreeNode(
						hyp.isPruneRoot(),
						ctn,
						raAnnotationImg,
						offsetX + getAvgXpos(),
						offsetY );
			} else if ( hyp.getSegmentSpecificConstraint() != null ) {
				ArgbDrawingUtils.taintForcedComponentTreeNode(
						ctn,
						raAnnotationImg,
						offsetX + getAvgXpos(),
						offsetY );
			} else {
				ArgbDrawingUtils.taintComponentTreeNode(
						ctn,
						raAnnotationImg,
						offsetX + getAvgXpos(),
						offsetY );
			}
		}
	}

	public void drawOptionalSegmentation( final Img< ARGBType > img, final IntervalView< FloatType > view, final Component< FloatType, ? > optionalSegmentation ) {
		final RandomAccess< ARGBType > raAnnotationImg = img.randomAccess();

		long offsetX = 0;
		long offsetY = 0;

		if ( view != null ) {
			// Lord, forgive me!
			if ( view.min( 0 ) == 0 ) {
				// In case I give the cropped paramaxflow-baby I lost the offset and must do ugly shit...
				// I promise this is only done because I need to finish the f****** paper!
				offsetX = -( this.getAvgXpos() - MoMA.GL_WIDTH_IN_PIXELS / 2 - MoMA.GL_PIXEL_PADDING_IN_VIEWS );
				offsetY = view.min( 1 );
			} else {
				offsetX = view.min( 0 );
				offsetY = view.min( 1 );
			}
		}

		ArgbDrawingUtils.taintInactiveComponentTreeNode( optionalSegmentation, raAnnotationImg, offsetX + getAvgXpos(), offsetY );
	}

	/**
	 * @return the average X coordinate of the center line of this
	 *         <code>GrowthLine</code>
	 */
	public int getAvgXpos() {
		return (int) getOffsetX();
	}

	/**
	 * @return the time-step this GLF corresponds to in the GL it is part of.
	 */
	public int getTime() {
		return this.getParent().getFrames().indexOf( this );
	}

	/**
	 * Returns the number of cells in this GLF.
	 */
	public int getSolutionStats_numCells() {
		int cells = 0;
		final GrowthLineTrackingILP ilp = getParent().getIlp();
		for ( final Set< AbstractAssignment< Hypothesis< Component< FloatType, ? >>> > set : ilp.getOptimalRightAssignments( this.getTime() ).values() ) {
			for ( final AbstractAssignment< Hypothesis< Component< FloatType, ? >>> ora : set ) {
				cells++;
			}
		}
		return cells;
	}

	/**
	 * Returns the rank of the given hypothesis in the GL.
	 *
	 * @return the uppermost segmented cell would return a 1. For each active
	 *         segmentation that is strictly above the given hypothesis the
	 *         return value is increased by 1.
	 */
	public int getSolutionStats_cellPos( final Hypothesis< Component< FloatType, ? >> hyp ) {
		int pos = 1;

		final GrowthLineTrackingILP ilp = getParent().getIlp();
		for ( final Set< AbstractAssignment< Hypothesis< Component< FloatType, ? >>> > optRightAssmnt : ilp.getOptimalRightAssignments(
				this.getTime() ).values() ) {

			for ( final AbstractAssignment< Hypothesis< Component< FloatType, ? >>> ora : optRightAssmnt ) {
				Hypothesis< Component< FloatType, ? >> srcHyp = null;
				if ( ora instanceof MappingAssignment ) {
					srcHyp = ( ( MappingAssignment ) ora ).getSourceHypothesis();
				}
				if ( ora instanceof DivisionAssignment ) {
					srcHyp = ( ( DivisionAssignment ) ora ).getSourceHypothesis();
				}
				if ( ora instanceof ExitAssignment ) {
					srcHyp = ( ( ExitAssignment ) ora ).getAssociatedHypothesis();
				}
				if ( srcHyp != null ) {
					if (ComponentTreeUtils.isAbove(srcHyp, hyp)) {
						pos++;
					}
				}
			}
		}
		return pos;
	}

	public Vector< ValuePair< ValuePair< Integer, Integer >, ValuePair< Integer, Integer > >> getSolutionStats_limitsAndRightAssType() {
		final Vector< ValuePair< ValuePair< Integer, Integer >, ValuePair< Integer, Integer > >> ret = new Vector<>();
		for ( final Hypothesis< Component< FloatType, ? > > hyp : getParent().getIlp().getOptimalRightAssignments( this.getTime() ).keySet() ) {

			final AbstractAssignment< Hypothesis< Component< FloatType, ? >>> aa = getParent().getIlp().getOptimalRightAssignments( this.getTime() ).get( hyp ).iterator().next();

			int min = Integer.MAX_VALUE, max = Integer.MIN_VALUE;
			for (Localizable localizable : hyp.getWrappedComponent()) {
				final int ypos = localizable.getIntPosition(0);
				min = Math.min(min, ypos);
				max = Math.max(max, ypos);
			}

			ret.add(new ValuePair<>(new ValuePair<>(min, max), new ValuePair<>(aa.getType(), (aa.isGroundTruth() || aa.isGroundUntruth()) ? 1 : 0)) );
		}

		ret.sort(Comparator.comparing(o -> o.a.a));
		return ret;
	}

	public Vector< ValuePair< Integer, Hypothesis< Component< FloatType, ? > >>> getSortedActiveHypsAndPos() {
		final Vector< ValuePair< Integer, Hypothesis< Component< FloatType, ? > >>> positionedHyps = new Vector<>();

		for ( final Hypothesis< Component< FloatType, ? > > hyp : getParent().getIlp().getOptimalRightAssignments( this.getTime() ).keySet() ) {
			// find out where this hypothesis is located along the GL
			int min = Integer.MAX_VALUE, max = Integer.MIN_VALUE;
			for (Localizable localizable : hyp.getWrappedComponent()) {
				final int ypos = localizable.getIntPosition(0);
				min = Math.min(min, ypos);
				max = Math.max(max, ypos);
			}

			if ( !hyp.isPruned() ) {
				positionedHyps.add(new ValuePair<>(-max, hyp) );
			}
		}

		positionedHyps.sort(Comparator.comparing(o -> o.a));

		return positionedHyps;
	}
}
