package com.jug.gui;

import com.jug.gui.assignmentview.AssignmentsEditorViewer;
import com.jug.lp.GrowthLineTrackingILP;

import javax.swing.*;

public class AssignmentEditorPanel extends JPanel {
    AssignmentsEditorViewer assignmentView;
    JCheckBox checkboxIsSelected;
    int sourceTimeStepOffset;
    private MoMAModel momaModel;

    public AssignmentEditorPanel(final MoMAGui mmgui, MoMAModel model, int viewHeight, int sourceTimeStepOffset) {
        this.momaModel = model;
        assignmentView = new AssignmentsEditorViewer(viewHeight, mmgui);
        assignmentView.addChangeListener(mmgui);
        this.addAssignmentView(assignmentView);
        this.setAppearanceAndLayout();
        this.addSelectionCheckbox(mmgui);
        this.sourceTimeStepOffset = sourceTimeStepOffset;
    }

    public int getTimeStepToDisplay(){
        return momaModel.getCurrentTime() + sourceTimeStepOffset;
    }

    private void addAssignmentView(AssignmentsEditorViewer assignmentView){
        this.add(assignmentView);
    }

    private void addSelectionCheckbox(MoMAGui mmgui){
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

    public void display(){
        GrowthLineTrackingILP ilp = momaModel.getCurrentGL().getIlp();

        int timeStepToDisplay = getTimeStepToDisplay();

        if (ilp == null) {
            assignmentView.display();
            return;
        }
        if (timeStepToDisplay < 0 || timeStepToDisplay > momaModel.getTimeStepMaximum() - 1) { // TODO-MM-20210729: We need to use `timeStepToDisplay > momaModel.getTimeStepMaximum() - 1` or else exit-assignments will be displayed in the view. I do not understand this 100%, but it likely has to do with the last frame that was hacked in at some point.
            assignmentView.display();
            return;
        }
        assignmentView.display(ilp.getAllRightAssignmentsThatStartFromOptimalHypothesesAt(timeStepToDisplay));
    }
}
