package com.jug.gui;

import com.jug.GrowthlaneFrame;
import com.jug.exceptions.GuiInteractionException;
import com.jug.lp.GrowthlaneTrackingILP;
import com.jug.lp.Hypothesis;
import com.jug.util.componenttree.AdvancedComponent;
import gurobi.GRBException;
import ij.IJ;
import ij.ImagePlus;
import net.imglib2.converter.RealARGBConverter;
import net.imglib2.display.projector.IterableIntervalProjector2D;
import net.imglib2.display.screenimage.awt.ARGBScreenImage;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.loops.LoopBuilder;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.ValuePair;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;

import javax.swing.*;
import javax.swing.event.EventListenerList;
import javax.swing.event.MouseInputListener;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import static com.jug.util.ArgbDrawingUtils.drawOptionalSegmentation;
import static com.jug.util.ArgbDrawingUtils.drawSegments;
import static java.util.Objects.isNull;

/**
 * @author jug
 */
public class GrowthlaneViewer extends JComponent implements MouseInputListener, MouseWheelListener {

    private static final long serialVersionUID = 8284204775277266994L;
    private static final int OFFSET_DISPLAY_COSTS = -25;
    private final int myWidth;
    private final int myHeight;
    private final MoMAGui mmgui;
    protected EventListenerList listenerList = new EventListenerList();
    Hypothesis<AdvancedComponent<FloatType>> hoveredOptimalHypothesis = null;
    private final LabelEditorDialog labelEditorDialog;
    private IDialogManager dialogManager;
    private HypothesisRangeSelector hypothesisRangeSelector;
    private IterableIntervalProjector2D<?, ?> projector;
    private ARGBScreenImage screenImage;
    private ARGBScreenImage screenImageUnaltered;
    private IntervalView<FloatType> view;
    private GrowthlaneFrame glf;
    private boolean showSegmentationAnnotations = true;
    // tracking the mouse (when over)
    private boolean isMouseOver;
    private int mousePosY;
    // tracking the mouse (when dragging)
    private boolean isDragging;
    private String componentInfoString = "";
    private List<Hypothesis<AdvancedComponent<FloatType>>> hypothesesAtHoverPosition = new ArrayList<>();
    private int indexOfCurrentHoveredHypothesis = 0;
    private Hypothesis<AdvancedComponent<FloatType>> selectedHypothesis;

    public GrowthlaneViewer(final MoMAGui mmgui, LabelEditorDialog labelEditorDialog, IDialogManager dialogManager, final int myWidth, final int h, HypothesisRangeSelector hypothesisRangeSelector) {
        super();

        this.mmgui = mmgui;
        this.labelEditorDialog = labelEditorDialog;
        this.dialogManager = dialogManager;
        this.hypothesisRangeSelector = hypothesisRangeSelector;

        addMouseListener(this);
        addMouseMotionListener(this);
        addMouseWheelListener(this);

        this.myWidth = myWidth;
        this.myHeight = h;
        setPreferredSize(new Dimension(myWidth, h));
        this.screenImage = new ARGBScreenImage(myWidth, h);
        this.projector = null;
        this.view = null;
        this.glf = null;
    }

    /**
     * Sets the image data to be displayed when paintComponent is called.
     *
     * @param glf     the GrowthlaneFrame to be displayed
     * @param viewImg an IntervalView<FloatType> containing the desired view
     *                onto the raw image data
     */
    public void setScreenImage(final GrowthlaneFrame glf, final IntervalView<FloatType> viewImg) {
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
        screenImage = new ARGBScreenImage(myWidth, myHeight);
        screenImageUnaltered = new ARGBScreenImage(myWidth, myHeight);
        this.projector = null;
        this.view = null;
        this.glf = null;
    }

    @Override
    public void paintComponent(final Graphics g) {
        if (isNull(glf))
            return; /* this prevents a null pointer exception, when the view does not have corresponding a time-step; e.g. the left view, when t=0 is shown in the center-view */

        try {
            if (!isNull(projector)) {
                projector.map();
                LoopBuilder.setImages(screenImage, screenImageUnaltered).forEachPixel((src, dest) -> dest.set(src)); /* copy original image data, which will act as source for calculating overlay pixel values */
            }

            if (showSegmentationAnnotations) {
                final int t = glf.getParent().getFrames().indexOf(glf);
                if (glf.getParent().ilpIsReady()) {
                    long xOffset = view.min(0);
                    long yOffset = view.min(1);
                    drawSegments(screenImage,
                            screenImageUnaltered,
                            xOffset,
                            yOffset,
                            glf.getParent().getIlp().getOptimalSegmentation(t));
                    drawSegments(screenImage,
                            screenImageUnaltered,
                            xOffset,
                            yOffset,
                            glf.getParent().getIlp().getForcedHypotheses(t));
                    drawSegments(screenImage,
                            screenImageUnaltered,
                            xOffset,
                            yOffset,
                            glf.getParent().getIlp().getSelectedHypothesesAt(t));
                }
            }

        } catch (final ArrayIndexOutOfBoundsException e) {
            // this can happen if a growth line, due to shift, exists in one
            // frame, and does not exist in others.
            // If for this growth line we want to visualize a time, where the
            // Growthlane is empty, the projector
            // throws a ArrayIndexOutOfBoundsException that I catch
            // hereby... ;)
            System.err.println("ArrayIndexOutOfBoundsException in paintComponent of GrowthlaneViewer!");
            // e.printStackTrace();
        } catch (final NullPointerException e) {
            // System.err.println( "View or glf not yet set in MotherMachineGui!" );
            // e.printStackTrace();
        }

        // Mouse-position related stuff...
        componentInfoString = " ";
        updateHypothesisInfoTooltip();
        drawHoveredOptionalHypothesis();
        renderToGraphicsObject(g);
    }

    private Font textFont = new Font("default", Font.PLAIN, 8);

    void drawString(Graphics g, String text, int x, int y) {
        for (String line : text.split("\n")) {
            g.setFont(textFont);
            g.drawString(line, x - 1, y += g.getFontMetrics().getHeight());
        }
    }

    private void renderToGraphicsObject(Graphics g) {
        int xOffset = 0;
        g.drawImage(screenImage.image(), xOffset, 0, myWidth, myHeight, null);
        g.setColor(getStringColor());
        drawString(g, componentInfoString, 1, this.mousePosY - OFFSET_DISPLAY_COSTS); /* draw info-string for optimal segment */
    }

    private Color optionalHypothesisTextColor = Color.RED.brighter();
    private Color optimalHypothesisTextColor = Color.GREEN.darker();
    private Color noHypothesisTextColor = Color.RED.brighter();

    private Color getStringColor() {
        Hypothesis<AdvancedComponent<FloatType>> optimalHyp = getHoveredOptimalHypothesis();
        Hypothesis<AdvancedComponent<FloatType>> optionalHyp = getHoveredOptionalHypothesis();

        if (!isNull(optimalHyp) && isNull(optionalHyp)) {
            return optimalHypothesisTextColor;
        }
        if (isNull(optimalHyp) && !isNull(optionalHyp)) {
            return optionalHypothesisTextColor;
        }
        if (!isNull(optimalHyp) && !isNull(optionalHyp)) {
            if (optionalHyp == optimalHyp) {
                return optimalHypothesisTextColor;
            }
            if (optionalHyp != optimalHyp) {
                return optionalHypothesisTextColor;
            }
        }
        return noHypothesisTextColor;
    }

    private DecimalFormat costFormat = new DecimalFormat(".##");

    private void updateHypothesisInfoTooltip() {
        if (!this.isDragging && this.isMouseOver && glf != null && glf.getParent().ilpIsReady()) {
            if (getHoveredOptionalHypothesis() != null) {
                float optionalCost = getHoveredOptionalHypothesis().getCost();
                AdvancedComponent<FloatType> component = getHoveredOptionalHypothesis().getWrappedComponent();
                ValuePair<Integer, Integer> limits = component.getVerticalComponentLimits();
                componentInfoString =
                        "\nI:" + component.getMaskIntensityMean(1) +
                        "\nB:" + component.getMaskIntensityMean(1) +
                        "\nR:" + component.getRankRelativeToLeafComponent() +
                        "\nO:" + component.getOrdinalValue() +
                        "\nC:" + costFormat.format(optionalCost) +
                        "\nT:" + limits.getA() +
                        "\nB:" + limits.getB() +
                        "\nL:" + (limits.getB() - limits.getA()) +
                        "\nA:" + component.size() +
                        "\nN:" + component.getNodeLevel();
            } else {
                componentInfoString = "---";
            }
        }
    }

    // -------------------------------------------------------------------------------------
    // MouseInputListener related methods
    // -------------------------------------------------------------------------------------

    private void drawHoveredOptionalHypothesis() {
        Hypothesis<AdvancedComponent<FloatType>> hoverOptionalHyp = getHoveredOptionalHypothesis();
        if (hoverOptionalHyp != null) {
            final AdvancedComponent<FloatType> comp = hoverOptionalHyp.getWrappedComponent();
            drawOptionalSegmentation(screenImage, screenImageUnaltered, view.min(0), view.min(1), comp);
        }
    }

    private void updateHoveredOptimalHypothesis() {
        hoveredOptimalHypothesis = null;
        final int t = glf.getTime();
        if (!this.isDragging && this.isMouseOver && glf != null && glf.getParent().ilpIsReady()) {
            hoveredOptimalHypothesis = glf.getParent().getIlp().getOptimalSegmentationAtLocation(t, this.mousePosY);
        }
    }

    private Hypothesis<AdvancedComponent<FloatType>> getHoveredOptimalHypothesis() {
        return hoveredOptimalHypothesis;
    }

    private Hypothesis<AdvancedComponent<FloatType>> getHoveredOptionalHypothesis() {
        List<Hypothesis<AdvancedComponent<FloatType>>> currentHoveredHypotheses = getHypothesesAtHoverPosition();
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
        if (isNull(glf)) {
            return; /* this prevents a null pointer exception, when the view does not have corresponding a time-step; e.g. the left view, when t=0 is shown in the center-view */
        }
        if (hypothesesAtHoverPosition.isEmpty()) {
            return; /* no entries in the list of hovered hypothesis that we could scroll through */
        }
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

    public void addIlpModelChangedEventListener(IlpModelChangedEventListener listener) {
        listenerList.add(IlpModelChangedEventListener.class, listener);
    }

    private void fireIlpModelChangedEvent(IlpModelChangedEvent evt) {
        Object[] listeners = listenerList.getListenerList();
        for (int i = 0; i < listeners.length; i = i + 2) { /* Do not understand why we need this weird indexing, but it is done here: http://www.java2s.com/Code/Java/Event/CreatingaCustomEvent.htm */
            if (listeners[i] == IlpModelChangedEventListener.class) {
                ((IlpModelChangedEventListener) listeners[i + 1]).IlpModelChangedEventOccurred(evt);
            }
        }
    }

    /**
     * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
     */
    @Override
    public void mouseClicked(final MouseEvent e) {
        if (isNull(glf))
            return; /* this prevents a null pointer exception, when the view does not have corresponding a time-step; e.g. the left view, when t=0 is shown in the center-view */

        final int t = glf.getTime();
        final GrowthlaneTrackingILP ilp = glf.getParent().getIlp();

        ilp.autosave();

        if(e.isAltDown()  && !e.isControlDown() && !e.isShiftDown()){
            Hypothesis<AdvancedComponent<FloatType>> hyp2add = getSelectedHypothesis();

            if(SwingUtilities.isLeftMouseButton(e)){
                hypothesisRangeSelector.setStartHypothesis(hyp2add);
            }
            if(SwingUtilities.isRightMouseButton(e)){
                hypothesisRangeSelector.setEndHypothesis(hyp2add);
            }
            mmgui.dataToDisplayChanged();
            return;
        }

        if (SwingUtilities.isLeftMouseButton(e) && e.isControlDown() && !e.isShiftDown() && !e.isAltDown()) {
            // ALT + CLICK: OPEN LABEL EDITOR
            // ----------------------
            Hypothesis<AdvancedComponent<FloatType>> hyp = getHoveredOptimalHypothesis();
            labelEditorDialog.edit(hyp);
            mmgui.requestFocusOnTimeStepSlider();
            return;
        }

        if (SwingUtilities.isRightMouseButton(e)  && !e.isAltDown()) {
            // CTRL + CLICK: ADD/REMOVE IGNORING CONSTRAINT HYPOTHESIS
            // ----------------------
            Hypothesis<AdvancedComponent<FloatType>> selectedParentHypothesis = getSelectedHypothesis();
            final List<Hypothesis<AdvancedComponent<FloatType>>> hyps2avoid = ilp.getConflictingChildSegments(t, selectedParentHypothesis);
            hyps2avoid.add(selectedParentHypothesis); /* add hypothesis of parent segment itself */

            if (isNull(hyps2avoid)) return;

            if (!selectedParentHypothesis.isForceIgnored()) {
                for (final Hypothesis<AdvancedComponent<FloatType>> hyp2avoid : hyps2avoid) {
                    hyp2avoid.setIsForceIgnored(true);
                }
            } else {
                for (final Hypothesis<AdvancedComponent<FloatType>> hyp2avoid : hyps2avoid) {
                    ilp.removeSegmentConstraints(hyp2avoid);
                }
            }

            updateMomaState(ilp);
            return;
        }

        if (SwingUtilities.isLeftMouseButton(e) && e.isControlDown() && e.isShiftDown()  && !e.isAltDown()) {
            // CTRL + SHIFT: PRUNE HYPOTHESIS AND FOLLOWING LINEAGE
            // -----------------------
            if (isNull(getSelectedHypothesis())) return;
            try {
                getSelectedHypothesis().toggleIsPrunedRoot();
            } catch (GuiInteractionException exception) {
                dialogManager.showUserInteractionError(exception);
            }
            mmgui.dataToDisplayChanged();
            return; // avoid re-optimization!
        }

        // simple CLICK: ADD/REMOVE ENFORCING CONSTRAINT HYPOTHESIS
        // -------------------------
        Hypothesis<AdvancedComponent<FloatType>> hyp2add = getSelectedHypothesis();
        if (isNull(hyp2add)) return; /* failed to get a non-null hypothesis, so return */

        if (hyp2add.isForced()) {
            ilp.removeSegmentConstraints(hyp2add);
        } else {
            final List<Hypothesis<AdvancedComponent<FloatType>>> hyps2remove = ilp.getConflictingHypotheses(hyp2add);

            try {
                if (hyp2add.getSegmentSpecificConstraint() != null) {
                    ilp.removeSegmentConstraints(hyp2add);
                }
                ilp.addSegmentInSolutionConstraintAndRemoveConflictingSegmentConstraints(hyp2add, hyps2remove);
            } catch (final GRBException e1) {
                e1.printStackTrace();
            }
        }
        updateMomaState(ilp);
    }

    private void updateMomaState(GrowthlaneTrackingILP ilp) {
        mmgui.dataToDisplayChanged();
        fireIlpModelChangedEvent(new IlpModelChangedEvent(this));
        runIlpAndFocusSlider(ilp);
    }

    private void runIlpAndFocusSlider(GrowthlaneTrackingILP ilp) {
        class IlpThread extends Thread {

            @Override
            public void run() {
                ilp.run();
            }
        }
        final IlpThread thread = new IlpThread();
        thread.start();
        mmgui.requestFocusOnTimeStepSlider();
    }

    private Hypothesis<AdvancedComponent<FloatType>> getSelectedHypothesis() {
        return selectedHypothesis;
    }

    private void updateHypothesesAtHoverPosition() {
        final int t = glf.getTime();
        if (!this.isMouseOver) {
            hypothesesAtHoverPosition = new ArrayList<>();
            indexOfCurrentHoveredHypothesis = -1;
        } else if (!this.isDragging && this.isMouseOver && glf != null && glf.getParent().ilpIsReady()) {
            List<Hypothesis<AdvancedComponent<FloatType>>> newHypothesesAtHoverPosition = glf.getParent().getIlp().getSegmentsAtLocation(t, this.mousePosY);
            if (!hypothesesAtHoverPosition.equals(newHypothesesAtHoverPosition)) {
                hypothesesAtHoverPosition = newHypothesesAtHoverPosition;
                GrowthlaneTrackingILP ilp = glf.getParent().getIlp();

                selectedHypothesis = hypothesesAtHoverPosition.stream().filter((hyp) -> hyp.isForced()) // FIRST TRY TO GET A FORCED HYPOTHESIS
                        .findFirst()
                        .orElse(null);
                if (selectedHypothesis == null) {
                    selectedHypothesis = hypothesesAtHoverPosition.stream().filter((hyp) -> hyp.isForceIgnored()) // SECOND TRY TO GET AN IGNORED HYPOTHESIS
                            .findFirst()
                            .orElse(null);
                }
                if (selectedHypothesis == null) {
                    selectedHypothesis = hypothesesAtHoverPosition.stream().filter((hyp) -> ilp.isSelected(hyp)) // IF NO FORCED OR IGNORED HYPOTHESIS EXISTS, THEN RETURN THE OPTIMAL ONE, IF IT EXISTS
                            .findFirst()
                            .orElse(null);
                }
                if (selectedHypothesis == null) {
                    if (hypothesesAtHoverPosition.size() > 0) {
                        selectedHypothesis = hypothesesAtHoverPosition.get(0);
                    }
                }
                if (selectedHypothesis != null) { /* there is an optimal hypothesis at the hover position; get it */
                    indexOfCurrentHoveredHypothesis = hypothesesAtHoverPosition.indexOf(selectedHypothesis); /* set indexOfCurrentHoveredHypothesis to optimal hypothesis at that position */
                } else { /* there is no optimal hypothesis at the hover position; use the first hypothesis in the list */
                    indexOfCurrentHoveredHypothesis = 0;
                }
            }
        }
    }

    private List<Hypothesis<AdvancedComponent<FloatType>>> getHypothesesAtHoverPosition() {
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
        mmgui.requestFocusOnTimeStepSlider();
    }

    /**
     * @see java.awt.event.MouseMotionListener#mouseMoved(java.awt.event.MouseEvent)
     */
    @Override
    public void mouseMoved(final MouseEvent e) {
        if (glf == null)
            return; /* this prevents a null pointer exception, when the view does not have corresponding a time-step; e.g. the left view, when t=0 is shown in the center-view */
        this.mousePosY = e.getY();
        updateHoveredHypotheses();
        this.repaint();
    }

    @Override
    public int getWidth() {
        return myWidth;
    }

    @Override
    public int getHeight() {
        return myHeight;
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

    private boolean mouseIsOverDisplayPanel() {
        return MouseInfo.getPointerInfo().getLocation().x >= this.getLocationOnScreen().x
                && MouseInfo.getPointerInfo().getLocation().x <= this.getLocationOnScreen().x + this.getWidth()
                && MouseInfo.getPointerInfo().getLocation().y >= this.getLocationOnScreen().y
                && MouseInfo.getPointerInfo().getLocation().y <= this.getLocationOnScreen().y + this.getHeight();
    }

    public boolean isMouseOver() {
        return mouseIsOverDisplayPanel();
    }
}
