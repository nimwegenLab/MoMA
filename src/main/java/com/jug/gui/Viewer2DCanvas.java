package com.jug.gui;

import com.jug.GrowthLineFrame;
import com.jug.lp.GrowthLineTrackingILP;
import com.jug.lp.Hypothesis;
import gurobi.GRBException;
import ij.IJ;
import ij.ImagePlus;
import net.imglib2.Point;
import net.imglib2.algorithm.componenttree.Component;
import net.imglib2.converter.RealARGBConverter;
import net.imglib2.display.projector.IterableIntervalProjector2D;
import net.imglib2.display.screenimage.awt.ARGBScreenImage;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;

import javax.swing.*;
import javax.swing.event.MouseInputListener;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.List;

/**
 * @author jug
 */
public class Viewer2DCanvas extends JComponent implements MouseInputListener {

	private static final long serialVersionUID = 8284204775277266994L;

	private final int w;
	private final int h;
	private IterableIntervalProjector2D< ?, ? > projector;
	private ARGBScreenImage screenImage;
	private IntervalView< FloatType > view;
	private GrowthLineFrame glf;

	private boolean showSegmentationAnnotations = true;

	// tracking the mouse (when over)
	private boolean isMouseOver;
	private int mousePosX;
	private int mousePosY;

	// tracking the mouse (when dragging)
	private boolean isDragging;

    private final MoMAGui mmgui;

	private static final int OFFSET_DISPLAY_COSTS = -25;
	private static int SYSTEM_SPECIFIC_POINTER_CORRECTION;

	public Viewer2DCanvas( final MoMAGui mmgui, final int w, final int h ) {
		super();

//		if ( OSValidator.isUnix() ) {
//			SYSTEM_SPECIFIC_POINTER_CORRECTION = 5;
//		}
//		if ( OSValidator.isMac() ) {
//			SYSTEM_SPECIFIC_POINTER_CORRECTION = -30;
//		}
//		if ( OSValidator.isWindows() ) {
//			SYSTEM_SPECIFIC_POINTER_CORRECTION = -25;
//		}

		this.mmgui = mmgui;

		addMouseListener( this );
		addMouseMotionListener( this );

		this.w = w;
		this.h = h;
		setPreferredSize( new Dimension( w, h ) );
		this.screenImage = new ARGBScreenImage( w, h );
		this.projector = null;
		this.view = null;
		this.glf = null;
	}

	/**
	 * Sets the image data to be displayed when paintComponent is called.
	 *
	 * @param glf
	 *            the GrowthLineFrameto be displayed
	 * @param viewImg
	 *            an IntervalView<FloatType> containing the desired view
	 *            onto the raw image data
	 */
	public void setScreenImage( final GrowthLineFrame glf, final IntervalView< FloatType > viewImg ) {
		setEmptyScreenImage();
		this.projector = new IterableIntervalProjector2D<>(0, 1, viewImg, screenImage, new RealARGBConverter<>(0, 1));
		this.view = viewImg;
		this.glf = glf;
		this.repaint();
	}

	/**
	 * Exports the part of the original image that is seen in this canvas.
	 *
	 * @param path
	 *            note that the extension you give determines the file format!
	 */
	public void exportScreenImage( final String path ) {
		final ImagePlus imagePlus = ImageJFunctions.wrapFloat( Views.interval( view, screenImage ), "export" );
		IJ.save( imagePlus, path );
	}

	/**
	 * Prepares to display an empty image.
	 */
	public void setEmptyScreenImage() {
		screenImage = new ARGBScreenImage( w, h );
		this.projector = null;
		this.view = null;
		this.glf = null;
	}


	private String strToShow = "";
	private String str2ToShow = " ";

	@Override
	public void paintComponent( final Graphics g ) {
		try {
			if ( projector != null ) {
				projector.map();
			}

			if ( showSegmentationAnnotations ) {
				final int t = glf.getParent().getFrames().indexOf( glf );

				// DRAW OPTIMAL SEGMENTATION + PRUNE-COLORING
				glf.drawOptimalSegmentation(
						screenImage,
						view,
						glf.getParent().getIlp().getOptimalSegmentation( t ) );
			}

		} catch ( final ArrayIndexOutOfBoundsException e ) {
			// this can happen if a growth line, due to shift, exists in one
			// frame, and does not exist in others.
			// If for this growth line we want to visualize a time where the
			// GrowthLine is empty, the projector
			// throws a ArrayIndexOutOfBoundsException that I catch
			// hereby... ;)
			System.err.println( "ArrayIndexOutOfBoundsException in paintComponent of Viewer2DCanvas!" );
			// e.printStackTrace();
		} catch ( final NullPointerException e ) {
			// System.err.println( "View or glf not yet set in MotherMachineGui!" );
			// e.printStackTrace();
		}

		// Mouse-position related stuff...
		strToShow = "";
		str2ToShow = " ";
		updateHypothesisInfoTooltip();
		drawHoveredOptionalHypothesis();
		drawHypothesisInfoTooltip(g);
	}

	private void drawHypothesisInfoTooltip(Graphics g) {
		g.drawImage( screenImage.image(), 0, 0, w, h, null );
		if ( !strToShow.equals( "" ) ) {
			g.setColor( Color.DARK_GRAY );
			g.drawString( strToShow, 2, this.mousePosY - OFFSET_DISPLAY_COSTS + 1 );
			g.setColor( Color.GREEN.darker() );
			g.drawString( strToShow, 1, this.mousePosY - OFFSET_DISPLAY_COSTS );
		}
		if ( !str2ToShow.equals( "" ) ) {
			g.setColor( Color.DARK_GRAY );
			g.drawString( str2ToShow, this.mousePosX + 6, this.mousePosY - OFFSET_DISPLAY_COSTS + 36 );
			g.setColor( Color.ORANGE.brighter() );
			g.drawString( str2ToShow, this.mousePosX + 5, this.mousePosY - OFFSET_DISPLAY_COSTS + 35 );
		}
	}

	private void updateHypothesisInfoTooltip() {
		if (!this.isDragging && this.isMouseOver && glf != null && glf.getParent().getIlp() != null) {
			if (getHoveredOptimalHypothesis() != null) {
				float cost = getHoveredOptimalHypothesis().getCost();
				strToShow = String.format("c=%.4f", cost);
				str2ToShow = "-";
			}
			// figure out which hyps are at current location
			if (getHoveredOptionalHypothesis() != null) {
				if (str2ToShow.endsWith("-")) {
					str2ToShow += "/+";
				} else {
					str2ToShow += "+";
				}
			} else {
				str2ToShow = "  noseg";
			}
		}
	}

	private void drawHoveredOptionalHypothesis(){
		Hypothesis< Component< FloatType, ? > > hoverOptionalHyp = getHoveredOptionalHypothesis();
		if ( hoverOptionalHyp != null ) {
			final Component<FloatType, ?> comp = hoverOptionalHyp.getWrappedComponent();
			glf.drawOptionalSegmentation(screenImage, view, comp);
		}
	}

	Hypothesis<Component<FloatType, ?>> hoveredOptimalHypothesis = null;
	private void updateHoveredOptimalHypothesis() {
		hoveredOptimalHypothesis = null;
		final int t = glf.getTime();
		if (!this.isDragging && this.isMouseOver && glf != null && glf.getParent().getIlp() != null) {
			hoveredOptimalHypothesis = glf.getParent().getIlp().getOptimalSegmentationAtLocation(t, this.mousePosY + SYSTEM_SPECIFIC_POINTER_CORRECTION);
		}
	}
	private Hypothesis<Component<FloatType, ?>> getHoveredOptimalHypothesis() {
		return hoveredOptimalHypothesis;
	}

	private Hypothesis<Component<FloatType, ?>> hoverOptionalHyp = null;
	private void updateHoveredOptionalHypothesis() {
		hoverOptionalHyp = null;
		final int t = glf.getTime();
		if (!this.isDragging && this.isMouseOver && glf != null && glf.getParent().getIlp() != null) {
			Point mousePosition = new Point(this.mousePosX, this.mousePosY);
			hoverOptionalHyp = glf.getParent().getIlp().getLowestInTreeHypAt(t, mousePosition);
		}
	}
	private Hypothesis<Component<FloatType, ?>> getHoveredOptionalHypothesis() {
		return hoverOptionalHyp;
	}

	// -------------------------------------------------------------------------------------
	// MouseInputListener related methods
	// -------------------------------------------------------------------------------------

	/**
	 * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
	 */
	@Override
	public void mouseClicked( final MouseEvent e ) {
		final int t = glf.getTime();
		final GrowthLineTrackingILP ilp = glf.getParent().getIlp();

		ilp.autosave();

		if ( e.isControlDown() ) {
			if ( e.isShiftDown() ) {
				// ctrl + shift == PRUNING
				// -----------------------
				final List< Hypothesis< Component< FloatType, ? >>> hypsUnderMouse =
						ilp.getSegmentsAtLocation( t, this.mousePosY + SYSTEM_SPECIFIC_POINTER_CORRECTION );
				for ( final Hypothesis< Component< FloatType, ? >> hyp : hypsUnderMouse ) {
					if ( ilp.isSelected( hyp ) ) {
						hyp.setPruneRoot( !hyp.isPruneRoot(), ilp );
					}
				}
				mmgui.dataToDisplayChanged();
				return; // avoid re-optimization!
			} else {
				// ctrl alone == AVOIDING
				// ----------------------
				final List< Hypothesis< Component< FloatType, ? >>> hyps2avoid =
						ilp.getSegmentsAtLocation( t, this.mousePosY + SYSTEM_SPECIFIC_POINTER_CORRECTION );
				try {
					for ( final Hypothesis< Component< FloatType, ? >> hyp2avoid : hyps2avoid ) {
						if ( hyp2avoid.getSegmentSpecificConstraint() != null ) {
							ilp.model.remove( hyp2avoid.getSegmentSpecificConstraint() );
						}
						ilp.addSegmentNotInSolutionConstraint( hyp2avoid );
					}
				} catch ( final GRBException e1 ) {
					e1.printStackTrace();
				}
			}
		} else {
			// simple click == SELECTING
			// -------------------------
			Point mousePosition = new Point(this.mousePosX, this.mousePosY);
			final Hypothesis< Component< FloatType, ? > > hyp2add =
					ilp.getLowestInTreeHypAt( t, mousePosition );
			final List< Hypothesis< Component< FloatType, ? >>> hyps2remove = ilp.getOptimalSegmentationsInConflict( t, hyp2add );

			try {
				if ( hyp2add.getSegmentSpecificConstraint() != null ) {
					ilp.model.remove( hyp2add.getSegmentSpecificConstraint() );
				}
				ilp.addSegmentInSolutionConstraint( hyp2add, hyps2remove );
			} catch ( final GRBException e1 ) {
				e1.printStackTrace();
			}
		}

		class IlpThread extends Thread {

			@Override
			public void run() {
				ilp.run();
			}
		}
		final IlpThread thread = new IlpThread();
		thread.start();
		mmgui.focusOnSliderTime();
	}

	/**
	 * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
	 */
	@Override
	public void mousePressed( final MouseEvent e ) {}

	/**
	 * @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
	 */
	@Override
	public void mouseEntered( final MouseEvent e ) {
		this.isMouseOver = true;
	}

	/**
	 * @see java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent)
	 */
	@Override
	public void mouseExited( final MouseEvent e ) {
		this.isMouseOver = false;
		updateHoveredOptimalHypothesis();
		updateHoveredOptionalHypothesis();
		this.repaint();
	}

	/**
	 * @see java.awt.event.MouseMotionListener#mouseDragged(java.awt.event.MouseEvent)
	 */
	@Override
	public void mouseDragged( final MouseEvent e ) {
		if ( e.getButton() == MouseEvent.BUTTON1 || e.getButton() == MouseEvent.BUTTON3 ) {
			this.isDragging = true;
            int dragX = e.getX();
            int dragY = e.getY();
		}
		repaint();
	}

	/**
	 * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
	 */
	@Override
	public void mouseReleased( final MouseEvent e ) {
		this.isDragging = false;
		repaint();
		mmgui.focusOnSliderTime();
	}

	/**
	 * @see java.awt.event.MouseMotionListener#mouseMoved(java.awt.event.MouseEvent)
	 */
	@Override
	public void mouseMoved( final MouseEvent e ) {
		this.mousePosX = e.getX();
		this.mousePosY = e.getY();
		updateHoveredOptimalHypothesis();
		updateHoveredOptionalHypothesis();
		this.repaint();
	}

	@Override
	public int getWidth() {
		return w;
	}

	@Override
	public int getHeight() {
		return h;
	}

	/**
	 * @return the showSegmentationAnnotations
	 */
	public boolean isShowingSegmentationAnnotations() {
		return showSegmentationAnnotations;
	}

	/**
	 * @param showSegmentationAnnotations
	 *            the showSegmentationAnnotations to set
	 */
	public void showSegmentationAnnotations( final boolean showSegmentationAnnotations ) {
		this.showSegmentationAnnotations = showSegmentationAnnotations;
	}
}
