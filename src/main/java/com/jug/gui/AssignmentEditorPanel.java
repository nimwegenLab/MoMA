package com.jug.gui;

import com.jug.config.ConfigurationManager;
import com.jug.gui.assignmentview.AssignmentsEditorViewer;
import com.jug.lp.GrowthlaneTrackingILP;

import javax.swing.*;

import static java.util.Objects.isNull;

public class AssignmentEditorPanel extends IlpVariableEditorPanel {
    AssignmentsEditorViewer assignmentView;
    JCheckBox checkboxIsSelected;
    int sourceTimeStepOffset;
    private final MoMAModel momaModel;

    public AssignmentEditorPanel(final MoMAGui mmgui, MoMAModel momaModel, int viewHeight, int sourceTimeStepOffset, ConfigurationManager configurationManager) {
        this.momaModel = momaModel;
        assignmentView = new AssignmentsEditorViewer(viewHeight, configurationManager);
        assignmentView.addChangeListener(mmgui);
        this.addAssignmentView(assignmentView);
        this.setAppearanceAndLayout();
        this.addSelectionCheckbox(mmgui);
        this.sourceTimeStepOffset = sourceTimeStepOffset;
    }

    public int getTimeStepToDisplay() {
        return momaModel.getCurrentTimeOfCurrentGl() + sourceTimeStepOffset;
    }

    private void addAssignmentView(AssignmentsEditorViewer assignmentView) {
        this.add(assignmentView);
    }

    private void addSelectionCheckbox(MoMAGui mmgui) {
        checkboxIsSelected = new JCheckBox();
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
        checkboxIsSelected.setEnabled(currentTimeStepIsValid());
    }

    @Override
    public void display() {
        GrowthlaneTrackingILP ilp = momaModel.getCurrentGL().getIlp();
        updateSelectionCheckbox();

        if (isNull(ilp)) {
            assignmentView.display();
            return;
        }
        if (!currentTimeStepIsValid()) {
            assignmentView.display();
            return;
        }
        assignmentView.display(ilp.getAllRightAssignmentsThatStartFromOptimalHypothesesAt(getTimeStepToDisplay()));
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
        this.assignmentView.switchToTab(tabIndex);
    }

    public AssignmentsEditorViewer getAssignmentViewerPanel() {
        return assignmentView;
    }

    public void addIlpModelChangedEventListener(IlpModelChangedEventListener listener) {
        assignmentView.addIlpModelChangedEventListener(listener);
    }

    public boolean isMouseOver(){
        return assignmentView.isMouseOver();
    }
}
