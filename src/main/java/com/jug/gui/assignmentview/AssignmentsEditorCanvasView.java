package com.jug.gui.assignmentview;

import com.jug.MoMA;
import com.jug.gui.MoMAGui;
import com.jug.lp.*;
import com.jug.util.OSValidator;
import net.imglib2.algorithm.componenttree.Component;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.ValuePair;

import javax.swing.*;
import javax.swing.event.MouseInputListener;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.geom.GeneralPath;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * @author jug
 */
public class AssignmentsEditorCanvasView extends JComponent implements MouseInputListener {

    /**
     *
     */
    private static final int DISPLAY_COSTS_ABSOLUTE_X = 10;

    /**
     *
     */
    private static final int LINEHEIGHT_DISPLAY_COSTS = 20;

    /**
     *
     */
    private static final int OFFSET_DISPLAY_COSTS = -15;
    // -------------------------------------------------------------------------------------
    // statics
    // -------------------------------------------------------------------------------------
    private static final long serialVersionUID = -2920396224787446598L;
    private static int HEIGHT_OFFSET;
    private static int ASSIGNMENT_DISPLAY_OFFSET;
    // -------------------------------------------------------------------------------------
    // fields
    // -------------------------------------------------------------------------------------
    private final int width;
    private final Set<AbstractAssignment<Hypothesis<Component<FloatType, ?>>>> filteredAssignments;
    private boolean doFilterDataByCost;
    private float filterMinCost = -100f;
    private float filterMaxCost = 100f;
    private boolean doFilterDataByIdentity = false;
    private boolean doAddToFilter = false; // if 'true' all assignments at the mouse location will be added to the filter next time repaint is called...
    private HashMap<Hypothesis<Component<FloatType, ?>>, Set<AbstractAssignment<Hypothesis<Component<FloatType, ?>>>>> data;

    private boolean isMouseOver = false; /* indicates if the mouse is inside this AssignmentsEditorCanvasView instance */
    private int mousePosX;
    private int mousePosY;
    private int currentCostLine;

    private boolean isDragging = false;
    private int dragX;
    private int dragY;
    private int dragInitiatingMouseButton = 0;
    private float dragStepWeight = 0;

    private boolean doAddAsGroundTruth;
    private boolean doAddAsGroundUntruth;

    private MoMAGui gui;

    private Color strokeColor;

    private ArrayList<AssignmentView> assignmentViews = new ArrayList<>();

    // -------------------------------------------------------------------------------------
    // construction
    // -------------------------------------------------------------------------------------
    public AssignmentsEditorCanvasView(final int height, final MoMAGui callbackGui) {
        this(height, -GrowthLineTrackingILP.CUTOFF_COST, GrowthLineTrackingILP.CUTOFF_COST);
        this.doFilterDataByCost = false;
        this.gui = callbackGui;
    }

    /**
     * @param height
     * @param filterMinCost
     * @param filterMaxCost
     */
    private AssignmentsEditorCanvasView(final int height, final float filterMinCost, final float filterMaxCost) {
        if (OSValidator.isUnix()) {
            HEIGHT_OFFSET = -10;
            ASSIGNMENT_DISPLAY_OFFSET = -7;
        }
        if (OSValidator.isMac()) {
            HEIGHT_OFFSET = -60;
            ASSIGNMENT_DISPLAY_OFFSET = -9;
        }
        if (OSValidator.isWindows()) {
            HEIGHT_OFFSET = -10;
            ASSIGNMENT_DISPLAY_OFFSET = -7;
        }

        this.width = 90;
        this.setPreferredSize(new Dimension(width, height + HEIGHT_OFFSET));

        this.addMouseListener(this);
        this.addMouseMotionListener(this);

        this.doFilterDataByCost = true;
        this.setCostFilterMin(filterMinCost);
        this.setCostFilterMax(filterMaxCost);

        this.filteredAssignments = new HashSet<>();
    }

    // -------------------------------------------------------------------------------------
    // getters and setters
    // -------------------------------------------------------------------------------------

    /**
     * @return the filterMinCost
     */
    public float getCostFilterMin() {
        return filterMinCost;
    }

    /**
     * @param filterMinCost the filterMinCost to set
     */
    public void setCostFilterMin(final float filterMinCost) {
        this.filterMinCost = filterMinCost;
    }

    /**
     * @return the filterMaxCost
     */
    public float getCostFilterMax() {
        return filterMaxCost;
    }

    /**
     * @param filterMaxCost the filterMaxCost to set
     */
    public void setCostFilterMax(final float filterMaxCost) {
        this.filterMaxCost = filterMaxCost;
    }

    // -------------------------------------------------------------------------------------
    // methods
    // -------------------------------------------------------------------------------------

    /**
     * Turns of filtering and shows all the given data.
     *
     * @param data a <code>HashMap</code> containing pairs of segmentation
     *             hypothesis at some time-point t and assignments towards t+1.
     */
    public void display(final HashMap<Hypothesis<Component<FloatType, ?>>, Set<AbstractAssignment<Hypothesis<Component<FloatType, ?>>>>> data) {
        doFilterDataByCost = false;
        setData(data);

        this.repaint();
    }

    /**
     * In this overwritten method we added filtering and calling
     * <code>drawAssignment(...)</code>.
     *
     * @see java.awt.Component#paint(java.awt.Graphics)
     */
    @Override
    public void paint(final Graphics g) {
        if (data == null) return;

        drawGlOffsetTop((Graphics2D) g);

        this.currentCostLine = 0;
        for (final Set<AbstractAssignment<Hypothesis<Component<FloatType, ?>>>> setOfAssignments : data.values()) {
            for (final AbstractAssignment<Hypothesis<Component<FloatType, ?>>> assignment : setOfAssignments) {
                if (doFilterDataByCost && (assignment.getCost() < this.getCostFilterMin() || assignment.getCost() > this.getCostFilterMax())) {
                    continue;
                }
                drawAssignment(g, assignment);
            }
        }

        for (AssignmentView assView : assignmentViews) {
            assView.draw((Graphics2D) g);
        }

        if (this.isDragging) {
            g.setColor(Color.GREEN.darker());
            g.drawString(String.format("min: %.4f", this.getCostFilterMin()), 0, 10);
            g.setColor(Color.RED.darker());
            g.drawString(String.format("max: %.4f", this.getCostFilterMax()), 0, 30);
            g.setColor(Color.GRAY);
            g.drawString(String.format("dlta %.4f", this.dragStepWeight), 0, 50);
        }

        // in case we where adding assignments - stop now!
        this.doAddToFilter = false;
    }

    /**
     * Checks the type of assignment we have and call the corresponding drawing
     * method.
     *
     * @param g
     * @param assignment
     */
    private void drawAssignment(final Graphics g, final AbstractAssignment<Hypothesis<Component<FloatType, ?>>> assignment) {

        // Just return in case the given component is in the
        // set of filtered assignments.
        if (this.doFilterDataByIdentity && this.filteredAssignments.contains(assignment)) {
            return;
        }

        final int type = assignment.getType();

        final Graphics2D g2 = (Graphics2D) g;

        if (type == GrowthLineTrackingILP.ASSIGNMENT_EXIT) {
            drawExitAssignment(g2, (ExitAssignment) assignment);
        }
//		else if ( type == GrowthLineTrackingILP.ASSIGNMENT_MAPPING ) {
//			drawMappingAssignment(g2, ( MappingAssignment ) assignment);
//		}
        else if (type == GrowthLineTrackingILP.ASSIGNMENT_DIVISION) {
            drawDivisionAssignment(g2, (DivisionAssignment) assignment);
        }
    }

    /**
     * Draw the top offset and cross-over range to the assignment view.
     *
     * @param g
     */
    private void drawGlOffsetTop(final Graphics2D g) {
        double componentExitRange = MoMA.COMPONENT_EXIT_RANGE / 2.0f; // defines the range, over which the cost increases.

        final int x1 = 0;
        strokeColor = Color.RED.darker();
        BasicStroke dashedStroke = new BasicStroke(1, 1, 1, 1, new float[]{1.0f, 2.0f}, 1.0f);
        BasicStroke solidStroke = new BasicStroke(1);

        GeneralPath polygon = new GeneralPath();
        polygon.moveTo(x1, MoMA.GL_OFFSET_TOP);
        polygon.lineTo(this.width, MoMA.GL_OFFSET_TOP);
        polygon.closePath();
        g.setPaint(strokeColor);
        g.setStroke(solidStroke);
        g.draw(polygon);

        polygon = new GeneralPath();
        polygon.moveTo(x1, MoMA.GL_OFFSET_TOP - componentExitRange);
        polygon.lineTo(this.width, MoMA.GL_OFFSET_TOP - componentExitRange);
        polygon.closePath();
        g.setPaint(strokeColor);
        g.setStroke(dashedStroke);
        g.draw(polygon);

        polygon = new GeneralPath();
        polygon.moveTo(x1, MoMA.GL_OFFSET_TOP + componentExitRange);
        polygon.lineTo(this.width, MoMA.GL_OFFSET_TOP + componentExitRange);
        polygon.closePath();
        g.setPaint(strokeColor);
        g.setStroke(dashedStroke);
        g.draw(polygon);
    }

    /**
     * This methods draws the given mapping-assignment into the component.
     *
     * @param g2
     * @param ma
     */
    private void drawMappingAssignment(final Graphics2D g2, final MappingAssignment ma) {
        final Hypothesis<Component<FloatType, ?>> leftHyp = ma.getSourceHypothesis();
        final Hypothesis<Component<FloatType, ?>> rightHyp = ma.getDestinationHypothesis();

        final ValuePair<Integer, Integer> limitsLeft = leftHyp.getLocation();
        final ValuePair<Integer, Integer> limitsRight = rightHyp.getLocation();

        final int x1 = 0;
        final int y1 = limitsLeft.getA() + ASSIGNMENT_DISPLAY_OFFSET;
        final int x2 = 0;
        final int y2 = limitsLeft.getB() + ASSIGNMENT_DISPLAY_OFFSET;
        final int y3 = limitsRight.getB() + ASSIGNMENT_DISPLAY_OFFSET;
        final int y4 = limitsRight.getA() + ASSIGNMENT_DISPLAY_OFFSET;

        final GeneralPath polygon = new GeneralPath();
        polygon.moveTo(x1, y1);
        polygon.lineTo(x2, y2);
        polygon.lineTo(this.width, y3);
        polygon.lineTo(this.width, y4);
        polygon.closePath();

        // Interaction with mouse:
        if (!this.isDragging && this.isMouseOver && polygon.contains(this.mousePosX, this.mousePosY)) {
            if (doAddToFilter) {
                // this case happens after shift-click
                this.filteredAssignments.add(ma);
            } else if (this.doAddAsGroundTruth) {
                this.doAddAsGroundTruth = false;
                ma.setGroundTruth(!ma.isGroundTruth());
                ma.reoptimize();
            } else if (this.doAddAsGroundUntruth) {
                this.doAddAsGroundUntruth = false;
                ma.setGroundUntruth(!ma.isGroundUntruth());
                ma.reoptimize();
            } else {
                final float cost = ma.getCost();
                if (ma.isGroundTruth()) {
                    g2.setPaint(Color.GREEN.darker());
                } else if (ma.isGroundUntruth()) {
                    g2.setPaint(Color.RED.darker());
                } else {
                    g2.setPaint(new Color(25 / 256f, 65 / 256f, 165 / 256f, 1.0f).darker().darker());
                }
                g2.drawString(
                        String.format("c=%.4f", cost),
                        DISPLAY_COSTS_ABSOLUTE_X,
                        this.mousePosY + OFFSET_DISPLAY_COSTS - this.currentCostLine * LINEHEIGHT_DISPLAY_COSTS);
                this.currentCostLine++;
            }
        }

        // draw it!
        g2.setStroke(new BasicStroke(1));
        if (!ma.isPruned()) {
            g2.setPaint(new Color(25 / 256f, 65 / 256f, 165 / 256f, 0.2f));
            if (ma.isGroundTruth() || ma.isGroundUntruth()) {
                g2.setPaint(g2.getColor().brighter().brighter());
            }
            g2.fill(polygon);
        }
        if (ma.isGroundTruth()) {
            g2.setPaint(Color.GREEN.darker());
            g2.setStroke(new BasicStroke(3));
        } else if (ma.isGroundUntruth()) {
            g2.setPaint(Color.RED.darker());
            g2.setStroke(new BasicStroke(3));
        } else {
            g2.setPaint(new Color(25 / 256f, 65 / 256f, 165 / 256f, 1.0f));
        }
        g2.draw(polygon);
    }

    /**
     * This methods draws the given division-assignment into the component.
     *
     * @param g2
     * @param da
     */
    private void drawDivisionAssignment(final Graphics2D g2, final DivisionAssignment da) {
        final Hypothesis<Component<FloatType, ?>> leftHyp = da.getSourceHypothesis();
        final Hypothesis<Component<FloatType, ?>> rightHypUpper = da.getUpperDesinationHypothesis();
        final Hypothesis<Component<FloatType, ?>> rightHypLower = da.getLowerDesinationHypothesis();

        final ValuePair<Integer, Integer> limitsLeft = leftHyp.getLocation();
        final ValuePair<Integer, Integer> limitsRightUpper = rightHypUpper.getLocation();
        final ValuePair<Integer, Integer> limitsRightLower = rightHypLower.getLocation();

        final int x1 = 0;
        final int y1 = limitsLeft.getA() + ASSIGNMENT_DISPLAY_OFFSET;
        final int x2 = 0;
        final int y2 = limitsLeft.getB() + ASSIGNMENT_DISPLAY_OFFSET;
        final int y3 = limitsRightLower.getB() + ASSIGNMENT_DISPLAY_OFFSET;
        final int y4 = limitsRightLower.getA() + ASSIGNMENT_DISPLAY_OFFSET;
        final int x5 = this.width / 3;
        final int y5 =
                ASSIGNMENT_DISPLAY_OFFSET + (2 * (limitsLeft.getA() + limitsLeft.getB()) / 2 + (limitsRightUpper.getB() + limitsRightLower.getA()) / 2) / 3;
        final int y6 = limitsRightUpper.getB() + ASSIGNMENT_DISPLAY_OFFSET;
        final int y7 = limitsRightUpper.getA() + ASSIGNMENT_DISPLAY_OFFSET;

        final GeneralPath polygon = new GeneralPath();
        polygon.moveTo(x1, y1);
        polygon.lineTo(x2, y2);
        polygon.lineTo(this.width, y3);
        polygon.lineTo(this.width, y4);
        polygon.lineTo(x5, y5);
        polygon.lineTo(this.width, y6);
        polygon.lineTo(this.width, y7);
        polygon.closePath();

        // Interaction with mouse:
        if (!this.isDragging && this.isMouseOver && polygon.contains(this.mousePosX, this.mousePosY)) {
            if (doAddToFilter) {
                // this case happens after shift-click
                this.filteredAssignments.add(da);
            } else if (this.doAddAsGroundTruth) {
                this.doAddAsGroundTruth = false;
                da.setGroundTruth(!da.isGroundTruth());
                da.reoptimize();
                SwingUtilities.invokeLater(() -> gui.dataToDisplayChanged());
            } else if (this.doAddAsGroundUntruth) {
                this.doAddAsGroundUntruth = false;
                da.setGroundUntruth(!da.isGroundUntruth());
                da.reoptimize();
                SwingUtilities.invokeLater(() -> gui.dataToDisplayChanged());
            } else {
                final float cost = da.getCost();
                if (da.isGroundTruth()) {
                    g2.setPaint(Color.GREEN.darker());
                } else if (da.isGroundUntruth()) {
                    g2.setPaint(Color.RED.darker());
                } else {
                    g2.setPaint(new Color(250 / 256f, 150 / 256f, 40 / 256f, 1.0f).darker().darker());
                }
                g2.drawString(
                        String.format("c=%.4f", cost),
                        DISPLAY_COSTS_ABSOLUTE_X,
                        this.mousePosY + OFFSET_DISPLAY_COSTS - this.currentCostLine * LINEHEIGHT_DISPLAY_COSTS);
                this.currentCostLine++;
            }
        }

        // draw it!
        g2.setStroke(new BasicStroke(1));
        if (!da.isPruned()) {
            g2.setPaint(new Color(250 / 256f, 150 / 256f, 40 / 256f, 0.2f));
            if (da.isGroundTruth() || da.isGroundUntruth()) {
                g2.setPaint(g2.getColor().brighter().brighter());
            }
            g2.fill(polygon);
        }
        if (da.isGroundTruth()) {
            g2.setPaint(Color.GREEN.darker());
            g2.setStroke(new BasicStroke(3));
        } else if (da.isGroundUntruth()) {
            g2.setPaint(Color.RED.darker());
            g2.setStroke(new BasicStroke(3));
        } else {
            g2.setPaint(new Color(250 / 256f, 150 / 256f, 40 / 256f, 1.0f));
        }
        g2.draw(polygon);
    }

    /**
     * This methods draws the given exit-assignment into the component.
     *
     * @param g2
     * @param ea
     */
    private void drawExitAssignment(final Graphics2D g2, final ExitAssignment ea) {
        final Hypothesis<Component<FloatType, ?>> hyp = ea.getAssociatedHypothesis();
        final ValuePair<Integer, Integer> limits = hyp.getLocation();

        final int x1 = 0;
        final int x2 = this.getWidth() / 5;
        final int y1 = limits.getA() + ASSIGNMENT_DISPLAY_OFFSET;
        final int y2 = y1 + limits.getB() - limits.getA();

        if (!this.isDragging && this.isMouseOver && this.mousePosX > x1 && this.mousePosX < x2 && this.mousePosY > y1 && this.mousePosY < y2) {
            if (doAddToFilter) {
                // this case happens after shift-click
                this.filteredAssignments.add(ea);
            } else if (this.doAddAsGroundTruth) {
                this.doAddAsGroundTruth = false;
                ea.setGroundTruth(!ea.isGroundTruth());
                ea.reoptimize();
                SwingUtilities.invokeLater(() -> gui.dataToDisplayChanged());
            } else if (this.doAddAsGroundUntruth) {
                this.doAddAsGroundUntruth = false;
                ea.setGroundUntruth(!ea.isGroundUntruth());
                ea.reoptimize();
                SwingUtilities.invokeLater(() -> gui.dataToDisplayChanged());
            } else {
                final float cost = ea.getCost();
                g2.drawString(String.format("c=%.4f", cost), 10, this.mousePosY - 10 - this.currentCostLine * 20);
                this.currentCostLine++;
            }
        }

        // draw it!
        g2.setStroke(new BasicStroke(1));
        if (!ea.isPruned()) {
            g2.setPaint(new Color(1f, 0f, 0f, 0.2f));
            if (ea.isGroundTruth() || ea.isGroundUntruth()) {
                g2.setPaint(g2.getColor().brighter().brighter());
            }
            g2.fillRect(x1, y1, x2 - x1, y2 - y1);
        }
        if (ea.isGroundTruth()) {
            g2.setPaint(Color.GREEN.darker());
            g2.setStroke(new BasicStroke(3));
        } else if (ea.isGroundUntruth()) {
            g2.setPaint(Color.RED.darker());
            g2.setStroke(new BasicStroke(3));
        } else {
            g2.setPaint(Color.RED);
        }
        g2.drawRect(x1, y1, x2 - x1, y2 - y1);
    }

    /**
     * Set the assignment data that will be displayed in this instance of {@link AssignmentsEditorCanvasView}.
     *
     * @param data: assignment data to display
     */
    public void setData(final HashMap<Hypothesis<Component<FloatType, ?>>, Set<AbstractAssignment<Hypothesis<Component<FloatType, ?>>>>> data) {
        this.data = data;
        initializeAssignmentViews();
        this.repaint();
    }

    private void initializeAssignmentViews() {
        assignmentViews.clear();
        for (final Set<AbstractAssignment<Hypothesis<Component<FloatType, ?>>>> setOfAssignments : data.values()) {
            for (final AbstractAssignment<Hypothesis<Component<FloatType, ?>>> assignment : setOfAssignments) {
                if (assignment.getType() == GrowthLineTrackingILP.ASSIGNMENT_MAPPING) {
                    assignmentViews.add(new MappingAssignmentView((MappingAssignment) assignment, width, ASSIGNMENT_DISPLAY_OFFSET));
                }
            }
        }
    }

//	private void filterShownAssignmentsByCost(){
//		if ( doFilterDataByCost && ( assignment.getCost() < this.getCostFilterMin() || assignment.getCost() > this.getCostFilterMax() ) ) {
//			continue;
//		}
//	}

    /**
     * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
     */
    @Override
    public void mouseClicked(final MouseEvent e) {
        if (selectedAssignment == null) { /* currently there is no assignment hovered */
            return;
        }
        // unmodified click -- include assignment
        // ctrl click       -- avoid assignment
        if (!isDragging) {
            if (e.getClickCount() == 1 && !e.isAltDown() && !e.isShiftDown() && e.getButton() == MouseEvent.BUTTON1) {
                if (e.isControlDown()) {
                    this.doAddAsGroundUntruth = true;
                    selectedAssignment.addAsGroundUntruth();
					/*
//						for(assignmentView assView : assViews) {
//							if(assView.isHovered(mousePosX, mousePosY)) {
//								assView.addAsGroundTruth(); // this
//							}
//						}
						hoveredAssignmentViews[currentlyHighlightedIndex].addAsGroundTruth();
					*/
                } else {
                    selectedAssignment.addAsGroundTruth();
                    this.doAddAsGroundTruth = true;
                }
            }
        }

        // unmod. float_click  --  show filter window thing
        if (e.getClickCount() == 2 && !e.isShiftDown()) {
            new DialogAssignmentViewSetup(this, e.getXOnScreen(), e.getYOnScreen()).setVisible(true);
        }

        repaint();
    }

    /**
     * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
     */
    @Override
    public void mousePressed(final MouseEvent e) {
        System.out.println("shift:    " + e.isShiftDown());
        System.out.println("ctrl:     " + e.isControlDown());
        System.out.println("alt:      " + e.isAltDown());
        System.out.println("click-c.: " + e.getClickCount());
        System.out.println("button:   " + e.getButton());

        // shift-click   --   hide assignments
        if (!e.isAltDown() && !e.isControlDown() && e.isShiftDown() && e.getButton() == MouseEvent.BUTTON1) {
            System.out.println("Filter!");
            this.doFilterDataByIdentity = true;
            this.doAddToFilter = true; // when repainting component next time...
            for(AssignmentView assignmentView : hoveredAssignments){
                assignmentView.hide();
            }
        }

        // right_click or shift-right_click  --  clear list of hidden assignments
        if (!e.isAltDown() && !e.isControlDown() && e.getButton() == MouseEvent.BUTTON3) {
            this.doFilterDataByIdentity = false;
            this.filteredAssignments.clear();
            for(AssignmentView assignmentView : assignmentViews){
                assignmentView.unhide();
            }
        }

        // I repaint before detecting dragging (since this can interfere with filtering)
        repaint();

        // plain click to initiate dragging
        if (!e.isShiftDown() && !e.isControlDown() && !e.isAltDown() &&
                (e.getButton() == MouseEvent.BUTTON1 || e.getButton() == MouseEvent.BUTTON3)) {
            System.out.println("Dragging!");
            this.isDragging = true;
            this.dragX = e.getX();
            this.dragY = e.getY();
            this.dragInitiatingMouseButton = e.getButton();
        }
    }

    /**
     * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
     */
    @Override
    public void mouseReleased(final MouseEvent e) {
        this.isDragging = false;
        repaint();
    }

    /**
     * @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
     */
    @Override
    public void mouseEntered(final MouseEvent e) {
        this.isMouseOver = true;
    }

    /**
     * @see java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent)
     */
    @Override
    public void mouseExited(final MouseEvent e) {
        this.isMouseOver = false;
        this.repaint();
    }

    /**
     * @see java.awt.event.MouseMotionListener#mouseDragged(java.awt.event.MouseEvent)
     */
    @Override
    public void mouseDragged(final MouseEvent e) {
        this.doFilterDataByCost = true;

        final float minstep = 0.01f;
        final float xsensitivity = 30.0f;
        final int dX = e.getX() - this.dragX;
        final int dY = this.dragY - e.getY();

        final float fac = (float) Math.pow(2, Math.abs((xsensitivity + dX) / xsensitivity));
        if (dX > 0) {
            this.dragStepWeight = minstep * fac;
        } else {
            this.dragStepWeight = minstep / fac;
        }

        if (this.dragInitiatingMouseButton == MouseEvent.BUTTON1) {
            System.out.println(" b1");
            this.setCostFilterMax(this.getCostFilterMax() + dY * this.dragStepWeight);
        }
        if (this.dragInitiatingMouseButton == MouseEvent.BUTTON3) {
            System.out.println(" b3");
            this.setCostFilterMin(this.getCostFilterMin() + dY * this.dragStepWeight);
        }

        this.dragY = e.getY();
        repaint();
    }

    ArrayList<AssignmentView> hoveredAssignments = new ArrayList<>();
    int selectedAssignmentIndex = 0;
    AssignmentView selectedAssignment;

    /**
     * @see java.awt.event.MouseMotionListener#mouseMoved(java.awt.event.MouseEvent)
     */
    @Override
    public void mouseMoved(final MouseEvent e) {
        this.mousePosX = e.getX();
        this.mousePosY = e.getY();
        ArrayList<AssignmentView> updatedHoveredAssignments = getHoveredAssignmentViews(e.getX(), e.getY());
        if(updatedHoveredAssignments.isEmpty()){
            selectedAssignment = null;
        }
        else if(updatedHoveredAssignments != hoveredAssignments){
            hoveredAssignments = updatedHoveredAssignments;
            /* TODO:
                Upon updated we need to:
                 1) sort hoveredAssignments by cost, so that when the user switches through it with the mouse wheel, he
                 will see assignments in order of increasing cost.
                 2) we need to figure out here, which of the hovered assignments is the optimal assignment/forced (if any) and set the index to it.
                Else set the index to the assignment with the lowest cost (0 if we sort it).
                */
            selectedAssignmentIndex = 0;
            selectedAssignment = hoveredAssignments.get(selectedAssignmentIndex);
        }
        this.doAddAsGroundTruth = false;
        this.doAddAsGroundUntruth = false;
        this.repaint();
    }

    private ArrayList<AssignmentView> getHoveredAssignmentViews(int mousePosX, int mousePosY){
        ArrayList<AssignmentView> hoveredAssignments = new ArrayList<>();
        for(AssignmentView assView : assignmentViews){
            if(assView.isHovered(mousePosX, mousePosY)){
                hoveredAssignments.add(assView);
            }
        }
        return hoveredAssignments;
    }
}
