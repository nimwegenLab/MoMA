package com.jug.gui;

import com.jug.GrowthLineFrame;
import com.jug.lp.GrowthLineTrackingILP;
import com.jug.lp.Hypothesis;
import gurobi.GRBException;
import ij.IJ;
import ij.ImagePlus;
import net.imglib2.algorithm.componenttree.Component;
import net.imglib2.converter.RealARGBConverter;
import net.imglib2.display.projector.IterableIntervalProjector2D;
import net.imglib2.display.screenimage.awt.ARGBScreenImage;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.loops.LoopBuilder;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;

import javax.swing.*;
import javax.swing.event.MouseInputListener;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.ArrayList;
import java.util.List;

import static com.jug.util.ArgbDrawingUtils.drawOptionalSegmentation;
import static com.jug.util.ArgbDrawingUtils.drawSegments;

/**
 * @author jug
 */
public class GrowthlaneViewer extends JComponent implements MouseInputListener, MouseWheelListener {

    private static final long serialVersionUID = 8284204775277266994L;
    private static final int OFFSET_DISPLAY_COSTS = -25;
    private final int w;
    private final int h;
    private final MoMAGui mmgui;
    private LabelEditorDialog labelEditorDialog;
    Hypothesis<Component<FloatType, ?>> hoveredOptimalHypothesis = null;
    private IterableIntervalProjector2D<?, ?> projector;
    private ARGBScreenImage screenImage;
    private ARGBScreenImage screenImageUnaltered;
    private IntervalView<FloatType> view;
    private GrowthLineFrame glf;
    private boolean showSegmentationAnnotations = true;
    // tracking the mouse (when over)
    private boolean isMouseOver;
    private int mousePosX;
    private int mousePosY;
    // tracking the mouse (when dragging)
    private boolean isDragging;
    private String optimalSegmentInfoString = "";
    private String optionalSegmentInfoString = " ";
    private List<Hypothesis<Component<FloatType, ?>>> hypothesesAtHoverPosition = new ArrayList<>();
    private int indexOfCurrentHoveredHypothesis = 0;

    public GrowthlaneViewer(final MoMAGui mmgui, LabelEditorDialog labelEditorDialog, final int w, final int h) {
        super();

        this.mmgui = mmgui;
        this.labelEditorDialog = labelEditorDialog;

        addMouseListener(this);
        addMouseMotionListener(this);
        addMouseWheelListener(this);

        this.w = w;
        this.h = h;
        setPreferredSize(new Dimension(w, h));
        this.screenImage = new ARGBScreenImage(w, h);
        this.projector = null;
        this.view = null;
        this.glf = null;
    }

    /**
     * Sets the image data to be displayed when paintComponent is called.
     *
     * @param glf     the GrowthLineFrameto be displayed
     * @param viewImg an IntervalView<FloatType> containing the desired view
     *                onto the raw image data
     */
    public void setScreenImage(final GrowthLineFrame glf, final IntervalView<FloatType> viewImg) {
        setEmptyScreenImage();
        this.projector = new IterableIntervalProjector2D<>(0, 1, viewImg, screenImage, new RealARGBConverter<>(0, 1));
        this.view = viewImg;
        this.glf = glf;
        updateHoveredHypotheses();
        this.repaint();
    }

    /**
     * Exports the part of the original image that is seen in this canvas.
     *
     * @param path note that the extension you give determines the file format!
     */
    public void exportScreenImage(final String path) {
        final ImagePlus imagePlus = ImageJFunctions.wrapFloat(Views.interval(view, screenImage), "export");
        IJ.save(imagePlus, path);
    }

    /**
     * Prepares to display an empty image.
     */
    public void setEmptyScreenImage() {
        screenImage = new ARGBScreenImage(w, h);
        screenImageUnaltered = new ARGBScreenImage(w, h);
        this.projector = null;
        this.view = null;
        this.glf = null;
    }

    @Override
    public void paintComponent(final Graphics g) {
        if (glf == null)
            return; /* this prevents a null pointer exception, when the view does not have corresponding a time-step; e.g. the left view, when t=0 is shown in the center-view */

        try {
            if (projector != null) {
                projector.map();
                LoopBuilder.setImages(screenImage, screenImageUnaltered).forEachPixel((src, dest) -> dest.set(src)); /* copy original image data, which will act as source for calculating overlay pixel values */
            }

            if (showSegmentationAnnotations) {
                final int t = glf.getParent().getFrames().indexOf(glf);
                if (glf.getParent().getIlp() != null) {
                    drawSegments(screenImage, screenImageUnaltered, view.min(0), view.min(1), glf.getParent().getIlp().getOptimalSegmentation(t)); /* DRAW SEGMENTS + PRUNE-COLORING */
                    drawSegments(screenImage, screenImageUnaltered, view.min(0), view.min(1), glf.getParent().getIlp().getForcedHypotheses(t)); /* DRAW SEGMENTS + PRUNE-COLORING */
                }
            }

        } catch (final ArrayIndexOutOfBoundsException e) {
            // this can happen if a growth line, due to shift, exists in one
            // frame, and does not exist in others.
            // If for this growth line we want to visualize a time where the
            // GrowthLine is empty, the projector
            // throws a ArrayIndexOutOfBoundsException that I catch
            // hereby... ;)
            System.err.println("ArrayIndexOutOfBoundsException in paintComponent of GrowthlaneViewer!");
            // e.printStackTrace();
        } catch (final NullPointerException e) {
            // System.err.println( "View or glf not yet set in MotherMachineGui!" );
            // e.printStackTrace();
        }

        // Mouse-position related stuff...
        optimalSegmentInfoString = "";
        optionalSegmentInfoString = " ";
        updateHypothesisInfoTooltip();
        drawHoveredOptionalHypothesis();
        drawHypothesisInfoTooltip(g);
    }

    private void drawHypothesisInfoTooltip(Graphics g) {
        g.drawImage(screenImage.image(), 0, 0, w, h, null);
        g.setColor(Color.GREEN.darker());
        g.drawString(optimalSegmentInfoString, 1, this.mousePosY - OFFSET_DISPLAY_COSTS); /* draw info-string for optimal segment */
        g.setColor(Color.RED.brighter());
        g.drawString(optionalSegmentInfoString, 1, this.mousePosY - OFFSET_DISPLAY_COSTS + 14); /* draw info-string for optional segment */
    }

    private void updateHypothesisInfoTooltip() {
        if (!this.isDragging && this.isMouseOver && glf != null && glf.getParent().getIlp() != null) {
            if (getHoveredOptimalHypothesis() != null) {
                float cost = getHoveredOptimalHypothesis().getCost();
                optimalSegmentInfoString = String.format("c=%.4f", cost);
            } else {
                optimalSegmentInfoString = "---";
            }
            if (getHoveredOptionalHypothesis() != null) {
                float optionalCost = getHoveredOptionalHypothesis().getCost();
                optionalSegmentInfoString = String.format("c=%.4f", optionalCost);
            } else {
                optionalSegmentInfoString = "---";
            }
        }
    }

    private void drawHoveredOptionalHypothesis() {
        Hypothesis<Component<FloatType, ?>> hoverOptionalHyp = getHoveredOptionalHypothesis();
        if (hoverOptionalHyp != null) {
            final Component<FloatType, ?> comp = hoverOptionalHyp.getWrappedComponent();
            drawOptionalSegmentation(screenImage, screenImageUnaltered, view.min(0), view.min(1), comp);
        }
    }

    private void updateHoveredOptimalHypothesis() {
        hoveredOptimalHypothesis = null;
        final int t = glf.getTime();
        if (!this.isDragging && this.isMouseOver && glf != null && glf.getParent().getIlp() != null) {
            hoveredOptimalHypothesis = glf.getParent().getIlp().getOptimalSegmentationAtLocation(t, this.mousePosY);
        }
    }

    // -------------------------------------------------------------------------------------
    // MouseInputListener related methods
    // -------------------------------------------------------------------------------------

    private Hypothesis<Component<FloatType, ?>> getHoveredOptimalHypothesis() {
        return hoveredOptimalHypothesis;
    }

    private Hypothesis<Component<FloatType, ?>> getHoveredOptionalHypothesis() {
        List<Hypothesis<Component<FloatType, ?>>> currentHoveredHypotheses = getHypothesesAtHoverPosition();
        if (currentHoveredHypotheses.isEmpty()) {
            return null;
        }
        return currentHoveredHypotheses.get(indexOfCurrentHoveredHypothesis);
    }

    /**
     * @see java.awt.event.MouseWheelListener#mouseWheelMoved(java.awt.event.MouseWheelEvent)
     */
    @Override
    public void mouseWheelMoved(final MouseWheelEvent e) {
        if (glf == null)
            return; /* this prevents a null pointer exception, when the view does not have corresponding a time-step; e.g. the left view, when t=0 is shown in the center-view */

        int increment = e.getWheelRotation();

        if (indexOfCurrentHoveredHypothesis + increment >= getHypothesesAtHoverPosition().size()) {
            indexOfCurrentHoveredHypothesis = 0;
        } else if (indexOfCurrentHoveredHypothesis + increment < 0) {
            indexOfCurrentHoveredHypothesis = getHypothesesAtHoverPosition().size() - 1;
        } else {
            indexOfCurrentHoveredHypothesis += increment;
        }
        selectedHypothesis = hypothesesAtHoverPosition.get(indexOfCurrentHoveredHypothesis);
        repaint();
    }

    /**
     * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
     */
    @Override
    public void mouseClicked(final MouseEvent e) {
        if (glf == null)
            return; /* this prevents a null pointer exception, when the view does not have corresponding a time-step; e.g. the left view, when t=0 is shown in the center-view */

        final int t = glf.getTime();
        final GrowthLineTrackingILP ilp = glf.getParent().getIlp();

        ilp.autosave();

        if (e.isAltDown()) {
            // ALT + CLICK: OPEN LABEL EDITOR
            // ----------------------
            Hypothesis<Component<FloatType, ?>> hyp = getHoveredOptimalHypothesis();
            labelEditorDialog.edit(hyp);
            mmgui.focusOnSliderTime();
            return;
        }

        if (e.isControlDown()  && !e.isShiftDown()) {
            // CTRL + CLICK: ADD/REMOVE IGNORING CONSTRAINT HYPOTHESIS
            // ----------------------
            Hypothesis<Component<FloatType, ?>> selectedParentHypothesis = getSelectedHypothesis();
            final List<Hypothesis<Component<FloatType, ?>>> hyps2avoid = ilp.getConflictingChildSegments(t, selectedParentHypothesis);
            hyps2avoid.add(selectedParentHypothesis); /* add hypothesis of parent segment itself */

            if (hyps2avoid == null) return;

            try {
                if (!selectedParentHypothesis.isIgnored) {
                    for (final Hypothesis<Component<FloatType, ?>> hyp2avoid : hyps2avoid) {
                        if (hyp2avoid.getSegmentSpecificConstraint() != null) {
                            ilp.removeSegmentConstraints(hyp2avoid);
                        }
                        ilp.addSegmentNotInSolutionConstraint(hyp2avoid);
                    }
                }
                else {
                    for (final Hypothesis<Component<FloatType, ?>> hyp2avoid : hyps2avoid) {
                        if (hyp2avoid.getSegmentSpecificConstraint() != null) {
                            ilp.removeSegmentConstraints(hyp2avoid);
                        }
                    }
                }
            } catch (final GRBException e1) {
                e1.printStackTrace();
            }
            mmgui.dataToDisplayChanged();
            runIlpAndFocusSlider(ilp);
            return;
        }

        if (e.isControlDown() && e.isShiftDown()) {
            // CTRL + SHIFT: PRUNE HYPOTHESIS AND FOLLOWING LINEAGE
            // -----------------------
            Hypothesis<Component<FloatType, ?>> hyp = getSelectedHypothesis();
            if (hyp == null) return;
            hyp.setPruneRoot(!hyp.isPruneRoot(), ilp);
            mmgui.dataToDisplayChanged();
            return; // avoid re-optimization!
        }

        // simple CLICK: ADD/REMOVE ENFORCING CONSTRAINT HYPOTHESIS
        // -------------------------
        Hypothesis<Component<FloatType, ?>> hyp2add = getSelectedHypothesis();
        if (hyp2add == null) return; /* failed to get a non-null hypothesis, so return */

        if(hyp2add.isForced){
            ilp.removeSegmentConstraints(hyp2add);
        }
        else{
            final List<Hypothesis<Component<FloatType, ?>>> hyps2remove = ilp.getOptimalSegmentationsInConflict(t, hyp2add);

            try {
                if (hyp2add.getSegmentSpecificConstraint() != null) {
                    ilp.removeSegmentConstraints(hyp2add);
                }
                ilp.addSegmentInSolutionConstraint(hyp2add, hyps2remove);
            } catch (final GRBException e1) {
                e1.printStackTrace();
            }
        }
        mmgui.dataToDisplayChanged();
        runIlpAndFocusSlider(ilp);
    }

    private void runIlpAndFocusSlider(GrowthLineTrackingILP ilp) {
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

    private Hypothesis<Component<FloatType, ?>> selectedHypothesis;

    private Hypothesis<Component<FloatType, ?>> getSelectedHypothesis() {
        return selectedHypothesis;
    }

    private void updateHypothesesAtHoverPosition() {
        final int t = glf.getTime();
        if (!this.isMouseOver) {
            hypothesesAtHoverPosition = new ArrayList<>();
            indexOfCurrentHoveredHypothesis = -1;
        } else if (!this.isDragging && this.isMouseOver && glf != null && glf.getParent().getIlp() != null) {
            List<Hypothesis<Component<FloatType, ?>>> newHypothesesAtHoverPosition = glf.getParent().getIlp().getSegmentsAtLocation(t, this.mousePosY);
            if (!hypothesesAtHoverPosition.equals(newHypothesesAtHoverPosition)) {
                hypothesesAtHoverPosition = newHypothesesAtHoverPosition;
                GrowthLineTrackingILP ilp = glf.getParent().getIlp();

                selectedHypothesis = hypothesesAtHoverPosition.stream().filter((hyp) -> hyp.isForced) // FIRST TRY TO GET A FORCED HYPOTHESIS
                        .findFirst()
                        .orElse(null);
                if (selectedHypothesis == null) {
                    selectedHypothesis = hypothesesAtHoverPosition.stream().filter((hyp) -> hyp.isIgnored) // SECOND TRY TO GET AN IGNORED HYPOTHESIS
                            .findFirst()
                            .orElse(null);
                }
                if (selectedHypothesis == null) {
                    selectedHypothesis = hypothesesAtHoverPosition.stream().filter((hyp) -> ilp.isSelected(hyp)) // IF NO FORCED OR IGNORED HYPOTHESIS EXISTS, THEN RETURN THE OPTIMAL ONE, IF IT EXISTS
                            .findFirst()
                            .orElse(null);
                }
                if (selectedHypothesis != null) { /* there is an optimal hypothesis at the hover position; get it */
                    indexOfCurrentHoveredHypothesis = hypothesesAtHoverPosition.indexOf(selectedHypothesis); /* set indexOfCurrentHoveredHypothesis to optimal hypothesis at that position */
                } else { /* there is no optimal hypothesis at the hover position; use the first hypothesis in the list */
                    indexOfCurrentHoveredHypothesis = 0;
                }
            }
        }
    }

    private List<Hypothesis<Component<FloatType, ?>>> getHypothesesAtHoverPosition() {
        return hypothesesAtHoverPosition;
    }

    /**
     * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
     */
    @Override
    public void mousePressed(final MouseEvent e) {
        if (glf == null)
            return; /* this prevents a null pointer exception, when the view does not have corresponding a time-step; e.g. the left view, when t=0 is shown in the center-view */
    }

    /**
     * @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
     */
    @Override
    public void mouseEntered(final MouseEvent e) {
        if (glf == null)
            return; /* this prevents a null pointer exception, when the view does not have corresponding a time-step; e.g. the left view, when t=0 is shown in the center-view */
        this.isMouseOver = true;
    }

    /**
     * @see java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent)
     */
    @Override
    public void mouseExited(final MouseEvent e) {
        if (glf == null)
            return; /* this prevents a null pointer exception, when the view does not have corresponding a time-step; e.g. the left view, when t=0 is shown in the center-view */
        this.isMouseOver = false;
        updateHoveredHypotheses();
        this.repaint();
    }

    private void updateHoveredHypotheses() {
        updateHoveredOptimalHypothesis();
        updateHypothesesAtHoverPosition();
    }

    /**
     * @see java.awt.event.MouseMotionListener#mouseDragged(java.awt.event.MouseEvent)
     */
    @Override
    public void mouseDragged(final MouseEvent e) {
        if (glf == null)
            return; /* this prevents a null pointer exception, when the view does not have corresponding a time-step; e.g. the left view, when t=0 is shown in the center-view */
        if (e.getButton() == MouseEvent.BUTTON1 || e.getButton() == MouseEvent.BUTTON3) {
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
    public void mouseReleased(final MouseEvent e) {
        if (glf == null)
            return; /* this prevents a null pointer exception, when the view does not have corresponding a time-step; e.g. the left view, when t=0 is shown in the center-view */
        this.isDragging = false;
        repaint();
        mmgui.focusOnSliderTime();
    }

    /**
     * @see java.awt.event.MouseMotionListener#mouseMoved(java.awt.event.MouseEvent)
     */
    @Override
    public void mouseMoved(final MouseEvent e) {
        if (glf == null)
            return; /* this prevents a null pointer exception, when the view does not have corresponding a time-step; e.g. the left view, when t=0 is shown in the center-view */
        this.mousePosX = e.getX();
        this.mousePosY = e.getY();
        updateHoveredHypotheses();
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
     * @param showSegmentationAnnotations the showSegmentationAnnotations to set
     */
    public void showSegmentationAnnotations(final boolean showSegmentationAnnotations) {
        this.showSegmentationAnnotations = showSegmentationAnnotations;
    }
}
