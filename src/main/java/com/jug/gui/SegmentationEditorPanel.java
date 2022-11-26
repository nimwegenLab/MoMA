package com.jug.gui;

import com.jug.GrowthlaneFrame;
import com.jug.config.ConfigurationManager;
import com.jug.datahandling.IImageProvider;
import com.jug.export.GroundTruthFramesExporter;
import com.jug.lp.CellCountConstraint;
import com.jug.lp.GrowthlaneTrackingILP;
import com.jug.util.Util;
import com.jug.util.componenttree.AdvancedComponent;
import com.jug.util.converter.RealFloatNormalizeConverter;
import com.moma.auxiliary.Plotting;
import gurobi.GRBException;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.converter.Converters;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static java.util.Objects.isNull;

public class SegmentationEditorPanel extends IlpVariableEditorPanel {
    private final MoMAModel momaModel;
    private final int timeStepOffset;
    private GroundTruthFramesExporter groundTruthFramesExporter;
    private ConfigurationManager configurationManager;
    private final IImageProvider imageProvider;
    public ColorChannel colorChannelToDisplay = ColorChannel.CHANNEL0;
    GrowthlaneViewer growthlaneViewer;
    private JCheckBox checkboxIsSelectedForSettingIlpConstraints;
    private JCheckBox checkboxIsSelectedAsGroundTruth;
    private JTextField txtNumCells;
    private JLabel labelTitle;
    private JButton showSegmentsButton;
    private Color groundTruthCheckboxDefaultColor;
    private MoMAGui mmgui;

    public SegmentationEditorPanel(final MoMAGui mmgui, MoMAModel momaModel, IImageProvider imageProvider, LabelEditorDialog labelEditorDialog, IDialogManager dialogManager, int viewWidth, int viewHeight, int timeStepOffset, boolean showGroundTruthExportFunctionality, GroundTruthFramesExporter groundTruthFramesExporter, ConfigurationManager configurationManager, HypothesisRangeSelector hypothesisRangeSelector) {
        this.mmgui = mmgui;
        this.momaModel = momaModel;
        this.imageProvider = imageProvider;
        this.timeStepOffset = timeStepOffset;
        this.groundTruthFramesExporter = groundTruthFramesExporter;
        this.configurationManager = configurationManager;
        growthlaneViewer = new GrowthlaneViewer(mmgui, labelEditorDialog, dialogManager, viewWidth, viewHeight, hypothesisRangeSelector);
        this.addTitleLabel();
        this.addGrowthlaneViewer(growthlaneViewer);
        this.addCheckboxForSettingIlpConstraints(mmgui);
        this.addCellNumberInputField(mmgui);
        this.addShowSegmentsButton();
        this.addCheckboxForSelectingGtExport(mmgui);
        checkboxIsSelectedAsGroundTruth.setVisible(showGroundTruthExportFunctionality);
        this.setAppearanceAndLayout();
    }

    private void addShowSegmentsButton() {
        showSegmentsButton = new JButton("Seg");
        showSegmentsButton.setFont(new Font("default", Font.BOLD, 11));
        showSegmentsButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        showSegmentsButton.addActionListener(e -> {
            ShowComponentsOfCurrentTimeStep();
        });
        showSegmentsButton.setMargin(new Insets(0, 0, 0, 0));
        this.add(showSegmentsButton);
    }

    /**
     * Enables calling code to open the corresponding segment view for this
     * instance.
     */
    public void openSegmentView() {
        showSegmentsButton.doClick();
    }

    /**
     * Show a stack of the components of the current time step in a separate window.
     */
    private void ShowComponentsOfCurrentTimeStep() {
        List<AdvancedComponent<FloatType>> optimalSegs = new ArrayList<>();
        int timeStep = timeStepToDisplay();
        GrowthlaneFrame glf = momaModel.getGlfAtTimeStep(timeStep);
        if (isNull(glf)) {
            return; /* this method was called at an invalid time-step so there is no component-tree; do nothing */
        }
        GrowthlaneTrackingILP ilp = momaModel.getCurrentGL().getIlp();
        if (!isNull(ilp)) {
            optimalSegs = glf.getParent().getIlp().getOptimalComponents(timeStep);
        }
        Plotting.drawComponentTree(glf.getComponentForest(), optimalSegs, timeStep);
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

    private void updateTitleLabel() {
        labelTitle.setText(getTitleLabel());
    }

    private void addCellNumberInputField(MoMAGui mmgui) {
        txtNumCells = new JTextField("-", 2);
        txtNumCells.setHorizontalAlignment(SwingConstants.CENTER);
        txtNumCells.setMaximumSize(txtNumCells.getPreferredSize());
        txtNumCells.addActionListener(e -> {
            momaModel.getCurrentGL().getIlp().autosave();

            int numCells;
            final GrowthlaneTrackingILP ilp = momaModel.getCurrentGL().getIlp();
            try {
                numCells = Integer.parseInt(txtNumCells.getText());
            } catch (final NumberFormatException nfe) {
                numCells = -1;
                txtNumCells.setText("-");
                CellCountConstraint constraint = ilp.getCellCountConstraint(timeStepToDisplay());
                constraint.remove();
            }
            if (numCells != -1) {
                try {
                    ilp.getCellCountConstraint(timeStepToDisplay()).remove();
                    ilp.addCellCountConstraint(timeStepToDisplay(), numCells);
                } catch (final GRBException e1) {
                    e1.printStackTrace();
                }
            }

            final Thread t = new Thread(() -> {
                momaModel.getCurrentGL().getIlp().run();
                mmgui.updateGui();
                mmgui.requestFocusOnTimeStepSlider();
            });
            t.start();
        });
        this.add(txtNumCells);
    }

    private void updateSelectionCheckboxes() {
        checkboxIsSelectedForSettingIlpConstraints.setEnabled(currentTimeStepIsValid());
    }

    public void toggleGroundTruthSelectionCheckbox() {
        boolean isAlreadySelected = groundTruthFramesExporter.containsFrame(timeStepToDisplay());
        if (isAlreadySelected) {
            unselectGroundTruthSelectionCheckbox();
        } else {
            selectGroundTruthSelectionCheckbox();
        }
    }

    public void selectGroundTruthSelectionCheckbox() {
        checkboxIsSelectedAsGroundTruth.setSelected(true);
        groundTruthFramesExporter.addFrame(timeStepToDisplay());
        mmgui.dataToDisplayChanged();
    }

    public void unselectGroundTruthSelectionCheckbox() {
        checkboxIsSelectedAsGroundTruth.setSelected(false);
        groundTruthFramesExporter.removeFrame(timeStepToDisplay());
        mmgui.dataToDisplayChanged();
    }

    private void updateGroundTruthSelectionCheckbox() {
        checkboxIsSelectedAsGroundTruth.setEnabled(currentTimeStepIsValid());
        if (groundTruthFramesExporter.containsFrame(timeStepToDisplay())){
            checkboxIsSelectedAsGroundTruth.setSelected(true);
            checkboxIsSelectedAsGroundTruth.setBackground(Color.GREEN);
        }
        else{
            checkboxIsSelectedAsGroundTruth.setSelected(false);
            checkboxIsSelectedAsGroundTruth.setBackground(groundTruthCheckboxDefaultColor);
        }
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

        CellCountConstraint constraint = momaModel.getCurrentGL().getIlp().getCellCountConstraint(timeStepToDisplay());
        final int numberOfCells = constraint.getNumberOfCells();
        txtNumCells.setEnabled(true);
        if (numberOfCells == -1) {
            txtNumCells.setText("-");
            txtNumCells.setBackground(Color.WHITE);
        } else {
            txtNumCells.setText(Integer.toString(numberOfCells));
            txtNumCells.setBackground(Color.ORANGE);
        }
    }

    private void addCheckboxForSettingIlpConstraints(MoMAGui mmgui) {
        checkboxIsSelectedForSettingIlpConstraints = new JCheckBox();
        checkboxIsSelectedForSettingIlpConstraints.addActionListener(mmgui);
        checkboxIsSelectedForSettingIlpConstraints.setAlignmentX(Component.CENTER_ALIGNMENT);
        this.add(checkboxIsSelectedForSettingIlpConstraints);
    }

    private void addCheckboxForSelectingGtExport(MoMAGui mmgui) {
        checkboxIsSelectedAsGroundTruth = new JCheckBox();
        groundTruthCheckboxDefaultColor = checkboxIsSelectedAsGroundTruth.getBackground();
        checkboxIsSelectedAsGroundTruth.addActionListener((x)->{
            if(checkboxIsSelectedAsGroundTruth.isSelected()){
                selectGroundTruthSelectionCheckbox();
            }
            else{
                unselectGroundTruthSelectionCheckbox();
            }
            mmgui.requestFocusOnTimeStepSlider();
        });
        checkboxIsSelectedAsGroundTruth.setAlignmentX(Component.CENTER_ALIGNMENT);
        this.add(checkboxIsSelectedAsGroundTruth);
    }

    public boolean isSelectedForSettingIlpConstraints() {
        return checkboxIsSelectedForSettingIlpConstraints.isSelected();
    }

    private int timeStepToDisplay() {
        return momaModel.getCurrentTimeOfCurrentGl() + this.timeStepOffset;
    }

    @Override
    public void display() {
        updateTitleLabel();
        updateCellNumberInputField();
        updateSelectionCheckboxes();
        updateGroundTruthSelectionCheckbox();
        updateShowSegmentsButton();

        if (!currentTimeStepIsValid()) {
            growthlaneViewer.setEmptyScreenImage();
            return;
        }
        GrowthlaneFrame glf = momaModel.getGrowthlaneFrame(timeStepToDisplay());
        IntervalView<FloatType> viewImgRightActive = getImageToDisplay(glf);
        growthlaneViewer.setScreenImage(glf, viewImgRightActive);
    }

    private void updateShowSegmentsButton() {
        showSegmentsButton.setEnabled(currentTimeStepIsValid());
    }

    private IntervalView<FloatType> getImageToDisplay(GrowthlaneFrame glf) {
        /**
         * The view onto <code>imgRaw</code> that is supposed to be shown on screen
         * (center one in active assignments view).
         */
        IntervalView<FloatType> viewImgCenterActive;
        if (colorChannelToDisplay == ColorChannel.CHANNEL1) {
            viewImgCenterActive = Views.hyperSlice(imageProvider.getRawChannelImgs().get(1), 2, glf.getOffsetF());
            viewImgCenterActive = normalizeImage(glf, viewImgCenterActive);
        } else if (colorChannelToDisplay == ColorChannel.CHANNEL2) {
            viewImgCenterActive = Views.hyperSlice(imageProvider.getRawChannelImgs().get(2), 2, glf.getOffsetF());
            viewImgCenterActive = normalizeImage(glf, viewImgCenterActive);
        } else { // default value to ColorChannel.CHANNEL0
            viewImgCenterActive = Views.offset(Views.hyperSlice(imageProvider.getImgRaw(), 2, glf.getOffsetF()), glf.getOffsetX() - configurationManager.GL_WIDTH_IN_PIXELS / 2 - configurationManager.GL_PIXEL_PADDING_IN_VIEWS, glf.getOffsetY());
        }
        return viewImgCenterActive;
    }

    @NotNull
    private IntervalView<FloatType> normalizeImage(GrowthlaneFrame glf, IntervalView<FloatType> viewToShow) {
        IntervalView<FloatType> viewImgCenterActive;
        final FloatType min = new FloatType();
        final FloatType max = new FloatType();
        Util.computeMinMax(Views.iterable(viewToShow), min, max);
        viewImgCenterActive =
                Views.offset(
                        Converters.convert(
                                (RandomAccessibleInterval<FloatType>) viewToShow,
                                new RealFloatNormalizeConverter(max.get()),
                                new FloatType()),
                        glf.getOffsetX() - configurationManager.GL_WIDTH_IN_PIXELS / 2 - configurationManager.GL_PIXEL_PADDING_IN_VIEWS,
                        glf.getOffsetY());
        return viewImgCenterActive;
    }

    private boolean currentTimeStepIsValid() {
        boolean timeStepIsInvalid = timeStepToDisplay() < 0 || timeStepToDisplay() > momaModel.getTimeStepMaximumOfCurrentGl();
        return !timeStepIsInvalid;
    }

    /***
     * This method set constraints for all ILP variables of the current time-step that are in the solution.
     */
    public void setVariableConstraints() {
        final GrowthlaneTrackingILP ilp = momaModel.getCurrentGL().getIlp();
        if (ilp != null) {
            if (this.isSelectedForSettingIlpConstraints()) {
                ilp.fixSegmentationAsIs(timeStepToDisplay());
            }
        }
    }

    /***
     * This method unsets/removes constraints for all ILP variables of the current time-step that are in the solution.
     */
    public void unsetVariableConstraints() {
        final GrowthlaneTrackingILP ilp = momaModel.getCurrentGL().getIlp();
        if (ilp != null) {
            if (this.isSelectedForSettingIlpConstraints()) {
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

    public boolean isMouseOver() {
        return growthlaneViewer.isMouseOver();
    }
}
