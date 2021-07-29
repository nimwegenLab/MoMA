package com.jug.gui;

import com.jug.GrowthLineFrame;
import com.jug.MoMA;
import com.jug.lp.GrowthLineTrackingILP;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;

import javax.swing.*;
import java.awt.*;

public class SegmentationEditorPanel extends IlpVariableEditorPanel {
    GrowthlaneViewer growthlaneViewer;
    JCheckBox checkboxIsSelected;
    private MoMAModel momaModel;
    private int timeStepOffset;

    public SegmentationEditorPanel(final MoMAGui mmgui, MoMAModel momaModel, String title, int viewWidth, int viewHeight, int timeStepOffset){
        this.momaModel = momaModel;
        this.timeStepOffset = timeStepOffset;
        growthlaneViewer = new GrowthlaneViewer(mmgui, viewWidth, viewHeight);
        this.addTitleLabel(title);
        this.addGrowthlaneViewer(growthlaneViewer);
        this.addSelectionCheckbox(mmgui);
        this.setAppearanceAndLayout();
    }

    private void addGrowthlaneViewer(GrowthlaneViewer growthlaneViewer) {
        this.add(growthlaneViewer);
    }

    private void setAppearanceAndLayout() {
        this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
        this.setBorder(BorderFactory.createMatteBorder(2, 2, 2, 2, Color.GRAY));
    }

    public void setScreenImage(final GrowthLineFrame growthLineFrame, final IntervalView<FloatType> imageView){
        growthlaneViewer.setScreenImage(growthLineFrame, imageView);
    }

    public void setEmptyScreenImage(){
        growthlaneViewer.setEmptyScreenImage();
    }

    public void showSegmentationAnnotations(final boolean showSegmentationAnnotations){
        growthlaneViewer.showSegmentationAnnotations(showSegmentationAnnotations);
    }

    private void addTitleLabel(String title){
        JLabel labelTitle = new JLabel(title);
        labelTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        this.add(labelTitle);
    }

    private void addSelectionCheckbox(MoMAGui mmgui){
        checkboxIsSelected = new JCheckBox();
        checkboxIsSelected.addActionListener(mmgui);
        checkboxIsSelected.setAlignmentX(Component.CENTER_ALIGNMENT);
        this.add(checkboxIsSelected);
    }

    public boolean isSelected() {
        return checkboxIsSelected.isSelected();
    }

    private int getTimeStepToDisplay() {
        return momaModel.getCurrentTime() + this.timeStepOffset;
    }

    public void display(){
        int timeStepToDisplay = getTimeStepToDisplay();

        if (timeStepToDisplay < 0 || timeStepToDisplay > momaModel.getTimeStepMaximum() - 1) { // TODO-MM-20210729: We need to use `timeStepToDisplay > momaModel.getTimeStepMaximum() - 1` or else exit-assignments will be displayed in the view. I do not understand this 100%, but it likely has to do with the last frame that was hacked in at some point.
            growthlaneViewer.setEmptyScreenImage();
            return;
        }
        GrowthLineFrame glf = momaModel.getGrowthLineFrame(timeStepToDisplay);
        IntervalView<FloatType> viewImgRightActive = Views.offset(Views.hyperSlice(momaModel.mm.getImgRaw(), 2, glf.getOffsetF()), glf.getOffsetX() - MoMA.GL_WIDTH_IN_PIXELS / 2 - MoMA.GL_PIXEL_PADDING_IN_VIEWS, glf.getOffsetY());
        growthlaneViewer.setScreenImage(glf, viewImgRightActive);
    }

    /***
     * This method set constraints for all ILP variables of the current time-step that are in the solution.
     */
    public void setVariableConstraints() {
        final GrowthLineTrackingILP ilp = momaModel.getCurrentGL().getIlp();
        if (ilp != null) {
            if (this.isSelected()) {
                ilp.fixSegmentationAsIs(getTimeStepToDisplay());
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
                ilp.removeAllSegmentConstraints(getTimeStepToDisplay());
            }
        }
    }
}
