package com.jug.gui.assignmentview;

import com.jug.config.ConfigurationManager;
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
import java.util.*;
import java.util.List;
import java.util.function.Supplier;

import static java.util.Objects.isNull;

/**
 * @author jug
 */
public class AssignmentsEditorViewer extends JTabbedPane implements ChangeListener {

    // -------------------------------------------------------------------------------------
    // statics
    // -------------------------------------------------------------------------------------
    private static final long serialVersionUID = 6588846114839723373L;

    private ConfigurationManager configurationManager;
    private Supplier<GrowthlaneTrackingILP> ilpSupplier;
    private Supplier<Integer> displayTimeGetter;

    // -------------------------------------------------------------------------------------
    // fields
    // -------------------------------------------------------------------------------------
    private AssignmentsEditorCanvasView activeAssignments;
    private AssignmentsEditorCanvasView inactiveMappingAssignments;
    private AssignmentsEditorCanvasView inactiveDivisionAssignments;
    private AssignmentsEditorCanvasView inactiveExitAssignments;
    private AssignmentsEditorCanvasView inactiveLysisAssignments;
    private int curTabIdx = 0;
    private Set<AbstractAssignment<Hypothesis<AdvancedComponent<FloatType>>>> data = new HashSet<>();
    private JComponent[] tabsToRoll;
    private String[] namesToRoll;
    private List<AssignmentsEditorCanvasView> assignmentViews;

    // -------------------------------------------------------------------------------------
    // construction
    // -------------------------------------------------------------------------------------

    /**
     *
     */
    public AssignmentsEditorViewer(final int height,
                                   ConfigurationManager configurationManager,
                                   Supplier<GrowthlaneTrackingILP> ilpSupplier,
                                   Supplier<Integer> displayTimeSupplier) {
        this.configurationManager = configurationManager;
        this.ilpSupplier = ilpSupplier;
        this.displayTimeGetter = displayTimeSupplier;
        this.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        buildGui(height);
    }

    private boolean isEnabled = false;

    public boolean isEnabled() {
        return isEnabled;
    }

    public void setEnabled(boolean enabled){
        isEnabled = enabled;
        super.setEnabled(enabled);
        assignmentViews.stream().forEach(assignmentView -> assignmentView.setEnabled(enabled));
    }

    /**
     * Switch to tab with index targetTabIndex, if targetTabIndex is within valid range. If not do nothing.
     *
     * @param targetTabIndex
     */
    public void switchToTab(int targetTabIndex) {
        if (targetTabIndex == curTabIdx) return;
        this.add(namesToRoll[targetTabIndex], tabsToRoll[targetTabIndex]);
        this.remove(tabsToRoll[curTabIdx]);
        curTabIdx = targetTabIndex;
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
        activeAssignments = new AssignmentsEditorCanvasView(height, configurationManager);
        inactiveMappingAssignments = new AssignmentsEditorCanvasView(height, configurationManager);
        inactiveDivisionAssignments = new AssignmentsEditorCanvasView(height, configurationManager);
        inactiveExitAssignments = new AssignmentsEditorCanvasView(height, configurationManager);
        inactiveLysisAssignments = new AssignmentsEditorCanvasView(height, configurationManager);

        assignmentViews = Arrays.asList(activeAssignments, inactiveMappingAssignments, inactiveDivisionAssignments, inactiveExitAssignments, inactiveLysisAssignments);

        tabsToRoll = new JComponent[]{activeAssignments, inactiveMappingAssignments, inactiveDivisionAssignments, inactiveExitAssignments, inactiveLysisAssignments};
        namesToRoll = new String[]{"O", "M", "D", "E", "L"};

        if (!OSValidator.isMac()) {
            this.add(namesToRoll[curTabIdx], tabsToRoll[curTabIdx]);
            this.setSelectedIndex(0);
        } else {
            for (int i = 0; i < tabsToRoll.length; i++) {
                this.add(namesToRoll[i], tabsToRoll[i]);
            }
        }
    }

    /**
     * Cyclically switch to next tab from the currently selected one. If the currently selected one is the last tab in the tab-list
     * it will switch to the first tab.
     */
    private void switchToNextTab() {
        int indexOfNextTab = curTabIdx + 1;
        if (indexOfNextTab >= tabsToRoll.length) indexOfNextTab = 0;
        switchToTab(indexOfNextTab);
    }

    /**
     * Draw this instance of assignmentViewer without having a fitting HashMap.
     */
    public void display() {
        display(new HashSet<>());
    }

    /**
     * Receives and visualizes a new HashMap of assignments.
     *
     * @param data a <code>HashMap</code> containing pairs of segmentation
     *                hypothesis at some time-point t and assignments towards t+1.
     */
    public void display(final Set<AbstractAssignment<Hypothesis<AdvancedComponent<FloatType>>>> data) {
        if (!data.equals(this.data)) {
            inactiveMappingAssignments.setData(GrowthlaneTrackingILP.filterAssignmentsWithPredicate(data, aa -> aa.isMappingAssignment()));
            inactiveDivisionAssignments.setData(GrowthlaneTrackingILP.filterAssignmentsWithPredicate(data, aa -> aa.isDivisionAssignment()));
            inactiveExitAssignments.setData(GrowthlaneTrackingILP.filterAssignmentsWithPredicate(data, aa -> aa.isExitAssignment()));
            inactiveLysisAssignments.setData(GrowthlaneTrackingILP.filterAssignmentsWithPredicate(data, aa -> aa.isLysisAssignment()));
        }
        if (!isNull(ilpSupplier.get())) {
            activeAssignments.display(ilpSupplier.get().getOptimalAssignments(displayTimeGetter.get()));
        }
        this.data = data;
    }

    /**
     * @see javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent)
     */
    @Override
    public void stateChanged(final ChangeEvent e) {
        if (this.getSelectedComponent().equals(activeAssignments)) {
            if (!isNull(ilpSupplier.get())) {
                activeAssignments.display(ilpSupplier.get().getOptimalAssignments(displayTimeGetter.get()));
            }
        } else if (this.getSelectedComponent().equals(inactiveMappingAssignments)) {
            inactiveMappingAssignments.setData(GrowthlaneTrackingILP.filterAssignmentsWithPredicate(data, aa -> aa.isMappingAssignment()));
        } else if (this.getSelectedComponent().equals(inactiveDivisionAssignments)) {
            inactiveDivisionAssignments.setData(GrowthlaneTrackingILP.filterAssignmentsWithPredicate(data, aa -> aa.isDivisionAssignment()));
        } else if (this.getSelectedComponent().equals(inactiveExitAssignments)) {
            inactiveExitAssignments.setData(GrowthlaneTrackingILP.filterAssignmentsWithPredicate(data, aa -> aa.isExitAssignment()));
        } else if (this.getSelectedComponent().equals(inactiveLysisAssignments)) {
            inactiveLysisAssignments.setData(GrowthlaneTrackingILP.filterAssignmentsWithPredicate(data, aa -> aa.isLysisAssignment()));
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
