package com.jug.gui;

import com.jug.GrowthLineFrame;
import com.jug.MoMA;
import com.jug.lp.GrowthLineTrackingILP;
import gurobi.GRBException;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;

import javax.swing.*;
import java.awt.*;

public class SegmentationEditorPanel extends IlpVariableEditorPanel {
    private final MoMAModel momaModel;
    private final int timeStepOffset;
    GrowthlaneViewer growthlaneViewer;
    JCheckBox checkboxIsSelected;
    private JTextField txtNumCells;
    private JLabel labelTitle;

    public SegmentationEditorPanel(final MoMAGui mmgui, MoMAModel momaModel, LabelEditorDialog labelEditorDialog, int viewWidth, int viewHeight, int timeStepOffset) {
        this.momaModel = momaModel;
        this.timeStepOffset = timeStepOffset;
        growthlaneViewer = new GrowthlaneViewer(mmgui, labelEditorDialog, viewWidth, viewHeight);
        this.addTitleLabel();
        this.addGrowthlaneViewer(growthlaneViewer);
        this.addSelectionCheckbox(mmgui);
        this.addCellNumberInputField(mmgui);
        this.setAppearanceAndLayout();
    }

    private void addGrowthlaneViewer(GrowthlaneViewer growthlaneViewer) {
        this.add(growthlaneViewer);
    }

    private void setAppearanceAndLayout() {
        this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
        if (timeStepOffset == 0) {
            this.setBorder(BorderFactory.createMatteBorder(2, 2, 2, 2, Color.RED));
        } else {
            this.setBorder(BorderFactory.createMatteBorder(2, 2, 2, 2, Color.GRAY));
        }
    }

    public void showSegmentationAnnotations(final boolean showSegmentationAnnotations) {
        growthlaneViewer.showSegmentationAnnotations(showSegmentationAnnotations);
    }

    private void addTitleLabel() {
        String title = getTitleLabel();
        labelTitle = new JLabel(title);
        labelTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        this.add(labelTitle);
    }

    private String getTitleLabel() {
        if (currentTimeStepIsValid()) {
            return Integer.toString(timeStepToDisplay());
        }
        return "NA";
    }

    private void updateTitleLable() {
        labelTitle.setText(getTitleLabel());
    }

    private void addCellNumberInputField(MoMAGui mmgui) {
        txtNumCells = new JTextField("-", 2);
        txtNumCells.setHorizontalAlignment(SwingConstants.CENTER);
        txtNumCells.setMaximumSize(txtNumCells.getPreferredSize());
        txtNumCells.addActionListener(e -> {
            momaModel.getCurrentGL().getIlp().autosave();

            int numCells;
            final GrowthLineTrackingILP ilp = momaModel.getCurrentGL().getIlp();
            try {
                numCells = Integer.parseInt(txtNumCells.getText());
            } catch (final NumberFormatException nfe) {
                numCells = -1;
                txtNumCells.setText("-");
                ilp.removeSegmentsInFrameCountConstraint(timeStepToDisplay());
            }
            if (numCells != -1) {
                try {
                    ilp.removeSegmentsInFrameCountConstraint(timeStepToDisplay());
                    ilp.addSegmentsInFrameCountConstraint(timeStepToDisplay(), numCells);
                } catch (final GRBException e1) {
                    e1.printStackTrace();
                }
            }

            final Thread t = new Thread(() -> {
                momaModel.getCurrentGL().getIlp().run();
                mmgui.dataToDisplayChanged();
                mmgui.sliderTime.requestFocus();
            });
            t.start();
        });
        this.add(txtNumCells);
    }

    private void updateSelectionCheckbox() {
        checkboxIsSelected.setEnabled(currentTimeStepIsValid());
    }

    private void updateCellNumberInputField() {
        if (momaModel.getCurrentGL().getIlp() == null) {
            return;
        }

        if (!currentTimeStepIsValid()) {
            txtNumCells.setEnabled(false);
            txtNumCells.setText("-");
            txtNumCells.setBackground(Color.WHITE);
            return;
        }

        final int rhs =
                momaModel.getCurrentGL().getIlp().getSegmentsInFrameCountConstraintRHS(timeStepToDisplay());
        txtNumCells.setEnabled(true);
        if (rhs == -1) {
            txtNumCells.setText("-");
            txtNumCells.setBackground(Color.WHITE);
        } else {
            txtNumCells.setText("" + rhs);
            txtNumCells.setBackground(Color.ORANGE);
        }
    }

    private void addSelectionCheckbox(MoMAGui mmgui) {
        checkboxIsSelected = new JCheckBox();
        checkboxIsSelected.addActionListener(mmgui);
        checkboxIsSelected.setAlignmentX(Component.CENTER_ALIGNMENT);
        this.add(checkboxIsSelected);
    }

    public boolean isSelected() {
        return checkboxIsSelected.isSelected();
    }

    private int timeStepToDisplay() {
        return momaModel.getCurrentTime() + this.timeStepOffset;
    }

    public void display() {
        updateTitleLable();
        updateCellNumberInputField();
        updateSelectionCheckbox();

        if (!currentTimeStepIsValid()) {
            growthlaneViewer.setEmptyScreenImage();
            return;
        }
        GrowthLineFrame glf = momaModel.getGrowthLineFrame(timeStepToDisplay());
        IntervalView<FloatType> viewImgRightActive = Views.offset(Views.hyperSlice(momaModel.mm.getImgRaw(), 2, glf.getOffsetF()), glf.getOffsetX() - MoMA.GL_WIDTH_IN_PIXELS / 2 - MoMA.GL_PIXEL_PADDING_IN_VIEWS, glf.getOffsetY());
        growthlaneViewer.setScreenImage(glf, viewImgRightActive);
    }

    private boolean currentTimeStepIsValid() {
        boolean timeStepIsInvalid = timeStepToDisplay() < 0 || timeStepToDisplay() > momaModel.getTimeStepMaximum() - 1; // TODO-MM-20210729: We need to use `timeStepToDisplay > momaModel.getTimeStepMaximum() - 1` or else exit-assignments will be displayed in the view. I do not understand this 100%, but it likely has to do with the last frame that was hacked in at some point.
        return !timeStepIsInvalid;
    }

    /***
     * This method set constraints for all ILP variables of the current time-step that are in the solution.
     */
    public void setVariableConstraints() {
        final GrowthLineTrackingILP ilp = momaModel.getCurrentGL().getIlp();
        if (ilp != null) {
            if (this.isSelected()) {
                ilp.fixSegmentationAsIs(timeStepToDisplay());
            }
        }
    }

    /***
     * This method unsets/removes constraints for all ILP variables of the current time-step that are in the solution.
     */
    public void unsetVariableConstraints() {
        final GrowthLineTrackingILP ilp = momaModel.getCurrentGL().getIlp();
        if (ilp != null) {
            if (this.isSelected()) {
                ilp.removeAllSegmentConstraints(timeStepToDisplay());
            }
        }
    }

    public GrowthlaneViewer getGrowthlaneViewer() {
        return this.growthlaneViewer;
    }

    public void addIlpModelChangedEventListener(IlpModelChangedEventListener listener) {
        growthlaneViewer.addIlpModelChangedEventListener(listener);
    }
}
