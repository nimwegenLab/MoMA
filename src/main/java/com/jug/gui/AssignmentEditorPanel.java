package com.jug.gui;

import com.jug.config.ConfigurationManager;
import com.jug.gui.assignmentview.AssignmentsEditorViewer;
import com.jug.logging.LoggingHelper;
import com.jug.lp.GrowthlaneTrackingILP;

import javax.swing.*;

import static java.util.Objects.isNull;

public class AssignmentEditorPanel extends IlpVariableEditorPanel {
    AssignmentsEditorViewer assignmentsEditorViewer;
    JCheckBox checkboxIsSelected;
    int sourceTimeStepOffset;
    private final MoMAModel momaModel;

    public AssignmentEditorPanel(final MoMAGui mmgui, MoMAModel momaModel, int viewHeight, int sourceTimeStepOffset, ConfigurationManager configurationManager) {
        this.momaModel = momaModel;
        assignmentsEditorViewer = new AssignmentsEditorViewer(viewHeight, configurationManager);
        assignmentsEditorViewer.addChangeListener(mmgui);
        this.add(assignmentsEditorViewer);
        this.setAppearanceAndLayout();
        this.addSelectionCheckbox(mmgui);
        this.sourceTimeStepOffset = sourceTimeStepOffset;
    }

    private boolean isEnabled = false;

    public boolean isEnabled() {
        return isEnabled;
    }

    public void setEnabled(boolean enabled){
        isEnabled = enabled;
        super.setEnabled(enabled);
        assignmentsEditorViewer.setEnabled(enabled);
    }

    public int getTimeStepToDisplay() {
        return momaModel.getCurrentTimeOfCurrentGl() + sourceTimeStepOffset;
    }

    private void addSelectionCheckbox(MoMAGui mmgui) {
        checkboxIsSelected = new JCheckBox();
        checkboxIsSelected.addActionListener((e) -> LoggingHelper.logUiAction(checkboxIsSelected, " AssignmentEditorPanel.checkboxIsSelected for timeStep=" + getTimeStepToDisplay()));
        checkboxIsSelected.addActionListener(mmgui);
        checkboxIsSelected.setAlignmentX(java.awt.Component.CENTER_ALIGNMENT);
        this.add(checkboxIsSelected);
    }

    private void setAppearanceAndLayout() {
        this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
    }

    public boolean isSelected() {
        return checkboxIsSelected.isSelected();
    }

    private void updateSelectionCheckbox() {
        checkboxIsSelected.setEnabled(currentTimeStepIsValid() && isEnabled());
    }

    @Override
    public void display() {
        GrowthlaneTrackingILP ilp = momaModel.getCurrentGL().getIlp();
        updateSelectionCheckbox();

        if (isNull(ilp)) {
            assignmentsEditorViewer.display();
            return;
        }
        if (!currentTimeStepIsValid()) {
            assignmentsEditorViewer.display();
            return;
        }
        assignmentsEditorViewer.setTimeStep(getTimeStepToDisplay());
        assignmentsEditorViewer.display(ilp.getAllRightAssignmentsThatStartFromOptimalHypothesesAt(getTimeStepToDisplay()));
        assignmentsEditorViewer.setEnabled(isEnabled());
    }

    private boolean currentTimeStepIsValid() {
        int timeStepToDisplay = getTimeStepToDisplay();
        boolean timeStepIsInvalid = timeStepToDisplay < 0 || timeStepToDisplay > momaModel.getTimeStepMaximumOfCurrentGl();
        return !timeStepIsInvalid;
    }

    /***
     * This method set constraints for all ILP variables of the current time-step that are in the solution.
     */
    public void setVariableConstraints() {
        final GrowthlaneTrackingILP ilp = momaModel.getCurrentGL().getIlp();
        if (ilp != null) {
            if (this.isSelected()) {
                ilp.fixAssignmentsAsAre(getTimeStepToDisplay());
            }
        }
    }

    /***
     * This method unsets/removes constraints for all ILP variables of the current time-step that are in the solution.
     */
    public void unsetVariableConstraints() {
        final GrowthlaneTrackingILP ilp = momaModel.getCurrentGL().getIlp();
        if (ilp != null) {
            if (this.isSelected()) {
                ilp.removeAllAssignmentConstraints(getTimeStepToDisplay());
            }
        }
    }

    public void showSegmentationAnnotations(final boolean showSegmentationAnnotations) {
    }

    public void switchToTab(int tabIndex) {
        this.assignmentsEditorViewer.switchToTab(tabIndex);
    }

    public AssignmentsEditorViewer getAssignmentViewerPanel() {
        return assignmentsEditorViewer;
    }

    public void addIlpModelChangedEventListener(IlpModelChangedEventListener listener) {
        assignmentsEditorViewer.addIlpModelChangedEventListener(listener);
    }

    public boolean isMouseOver(){
        return assignmentsEditorViewer.isMouseOver();
    }
}
