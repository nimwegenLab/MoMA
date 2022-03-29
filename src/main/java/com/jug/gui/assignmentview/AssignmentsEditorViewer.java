package com.jug.gui.assignmentview;

import com.jug.gui.IlpModelChangedEventListener;
import com.jug.lp.AbstractAssignment;
import com.jug.lp.GrowthlaneTrackingILP;
import com.jug.lp.Hypothesis;
import com.jug.util.OSValidator;
import com.jug.util.componenttree.AdvancedComponent;
import net.imglib2.type.numeric.real.FloatType;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Set;

/**
 * @author jug
 */
public class AssignmentsEditorViewer extends JTabbedPane implements ChangeListener {

    // -------------------------------------------------------------------------------------
    // statics
    // -------------------------------------------------------------------------------------
    private static final long serialVersionUID = 6588846114839723373L;

    // -------------------------------------------------------------------------------------
    // fields
    // -------------------------------------------------------------------------------------
    private AssignmentsEditorCanvasView activeAssignments;
    private AssignmentsEditorCanvasView inactiveMappingAssignments;
    private AssignmentsEditorCanvasView inactiveDivisionAssignments;
    private AssignmentsEditorCanvasView inactiveExitAssignments;
    private AssignmentsEditorCanvasView inactiveLysisAssignments;
    private int curTabIdx = 0;
//    private JPanel nextTabHack;
    private HashMap<Hypothesis<AdvancedComponent<FloatType>>, Set<AbstractAssignment<Hypothesis<AdvancedComponent<FloatType>>>>> data = new HashMap<>();
    private JComponent[] tabsToRoll;
    private String[] namesToRoll;

    // -------------------------------------------------------------------------------------
    // construction
    // -------------------------------------------------------------------------------------

    /**
     *
     */
    public AssignmentsEditorViewer(final int height) {
        this.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        buildGui(height);
    }

    // -------------------------------------------------------------------------------------
    // getters and setters
    // -------------------------------------------------------------------------------------

    // -------------------------------------------------------------------------------------
    // methods
    // -------------------------------------------------------------------------------------

    public void switchToTab(int targetTab) {
        if (targetTab >= tabsToRoll.length) targetTab = 0;
        this.add(namesToRoll[targetTab], tabsToRoll[targetTab]);
        this.remove(tabsToRoll[curTabIdx]);
        curTabIdx = targetTab;
        this.setSelectedIndex(0);
    }

    /**
     * Builds the user interface.
     */
    private void buildGui(final int height) {
        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                switchToNextTab();
            }
        });
        activeAssignments = new AssignmentsEditorCanvasView(height);
        inactiveMappingAssignments = new AssignmentsEditorCanvasView(height);
        inactiveDivisionAssignments = new AssignmentsEditorCanvasView(height);
        inactiveExitAssignments = new AssignmentsEditorCanvasView(height);
        inactiveLysisAssignments = new AssignmentsEditorCanvasView(height);

        // Hack to enable non-Mac MoMA to only use one row of tabs
//        nextTabHack = new JPanel();
        tabsToRoll = new JComponent[]{activeAssignments, inactiveMappingAssignments, inactiveDivisionAssignments, inactiveExitAssignments, inactiveLysisAssignments};
        namesToRoll = new String[]{"O", "M", "D", "E", "L"};
//        final ChangeListener changeListener = changeEvent -> {
//            final JTabbedPane sourceTabbedPane = (JTabbedPane) changeEvent.getSource();
//            if (sourceTabbedPane.getSelectedComponent().equals(nextTabHack)) {
//                switchToNextTab();
//            }
//        };

        activeAssignments.display(GrowthlaneTrackingILP.getActiveAssignments(data));
        inactiveMappingAssignments.display(GrowthlaneTrackingILP.filterAssignmentsWithPredicate(data, aa -> aa.getType() == GrowthlaneTrackingILP.ASSIGNMENT_MAPPING));
        inactiveDivisionAssignments.display(GrowthlaneTrackingILP.filterAssignmentsWithPredicate(data, aa -> aa.getType() == GrowthlaneTrackingILP.ASSIGNMENT_DIVISION));
        inactiveExitAssignments.display(GrowthlaneTrackingILP.filterAssignmentsWithPredicate(data, aa -> aa.getType() == GrowthlaneTrackingILP.ASSIGNMENT_EXIT));
        inactiveLysisAssignments.display(GrowthlaneTrackingILP.filterAssignmentsWithPredicate(data, aa -> aa.getType() == GrowthlaneTrackingILP.ASSIGNMENT_LYSIS));

        if (!OSValidator.isMac()) {
//            this.add("", nextTabHack);
            this.add(namesToRoll[curTabIdx], tabsToRoll[curTabIdx]);
            this.setSelectedIndex(0);
//            this.addChangeListener(changeListener);
        } else {
            for (int i = 0; i < tabsToRoll.length; i++) {
                this.add(namesToRoll[i], tabsToRoll[i]);
            }
        }
    }

    private void switchToNextTab() {
        int selectedTab = curTabIdx + 1;
        switchToTab(selectedTab);
    }

    /**
     * Draw this instance of assignmentViewer without having a fitting HashMap.
     */
    public void display() {
        HashMap<Hypothesis<AdvancedComponent<FloatType>>, Set<AbstractAssignment<Hypothesis<AdvancedComponent<FloatType>>>>> emptyHashMap = new HashMap<>();
        display(emptyHashMap);
    }

    /**
     * Receives and visualizes a new HashMap of assignments.
     *
     * @param hashMap a <code>HashMap</code> containing pairs of segmentation
     *                hypothesis at some time-point t and assignments towards t+1.
     */
    public void display(final HashMap<Hypothesis<AdvancedComponent<FloatType>>, Set<AbstractAssignment<Hypothesis<AdvancedComponent<FloatType>>>>> hashMap) {
        if (!hashMap.equals(this.data)) {
            inactiveMappingAssignments.setData(GrowthlaneTrackingILP.filterAssignmentsWithPredicate(hashMap, aa -> aa.getType() == GrowthlaneTrackingILP.ASSIGNMENT_MAPPING));
            inactiveDivisionAssignments.setData(GrowthlaneTrackingILP.filterAssignmentsWithPredicate(hashMap, aa -> aa.getType() == GrowthlaneTrackingILP.ASSIGNMENT_DIVISION));
            inactiveExitAssignments.setData(GrowthlaneTrackingILP.filterAssignmentsWithPredicate(hashMap, aa -> aa.getType() == GrowthlaneTrackingILP.ASSIGNMENT_EXIT));
            inactiveLysisAssignments.setData(GrowthlaneTrackingILP.filterAssignmentsWithPredicate(hashMap, aa -> aa.getType() == GrowthlaneTrackingILP.ASSIGNMENT_LYSIS));
        }
        activeAssignments.setData(GrowthlaneTrackingILP.getActiveAssignments(hashMap));
        this.data = hashMap;
    }

    /**
     * @see javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent)
     */
    @Override
    public void stateChanged(final ChangeEvent e) {
        if (this.getSelectedComponent().equals(activeAssignments)) {
            activeAssignments.setData(GrowthlaneTrackingILP.getActiveAssignments(data));
        } else if (this.getSelectedComponent().equals(inactiveMappingAssignments)) {
            inactiveMappingAssignments.setData(GrowthlaneTrackingILP.filterAssignmentsWithPredicate(data, aa -> aa.getType() == GrowthlaneTrackingILP.ASSIGNMENT_MAPPING));
        } else if (this.getSelectedComponent().equals(inactiveDivisionAssignments)) {
            inactiveDivisionAssignments.setData(GrowthlaneTrackingILP.filterAssignmentsWithPredicate(data, aa -> aa.getType() == GrowthlaneTrackingILP.ASSIGNMENT_DIVISION));
        } else if (this.getSelectedComponent().equals(inactiveExitAssignments)) {
            inactiveExitAssignments.setData(GrowthlaneTrackingILP.filterAssignmentsWithPredicate(data, aa -> aa.getType() == GrowthlaneTrackingILP.ASSIGNMENT_EXIT));
        } else if (this.getSelectedComponent().equals(inactiveLysisAssignments)) {
            inactiveLysisAssignments.setData(GrowthlaneTrackingILP.filterAssignmentsWithPredicate(data, aa -> aa.getType() == GrowthlaneTrackingILP.ASSIGNMENT_LYSIS));
        }
    }

    /**
     * Returns the <code>AssignmentsEditorCanvasView</code> that holds all active
     * assignments.
     *
     * @return
     */
    public AssignmentsEditorCanvasView getActiveAssignmentsForHtmlExport() {
        return this.activeAssignments;
    }

    public void addIlpModelChangedEventListener(IlpModelChangedEventListener listener) {
        activeAssignments.addIlpModelChangedEventListener(listener);
        inactiveMappingAssignments.addIlpModelChangedEventListener(listener);
        inactiveDivisionAssignments.addIlpModelChangedEventListener(listener);
        inactiveExitAssignments.addIlpModelChangedEventListener(listener);
        inactiveLysisAssignments.addIlpModelChangedEventListener(listener);
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
