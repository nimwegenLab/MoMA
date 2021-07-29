package com.jug.gui;

import com.jug.GrowthLineFrame;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.IntervalView;

import javax.swing.*;
import java.awt.*;

public class SegmentationEditorPanel extends JPanel {
    GrowthlaneViewer growthlaneViewer;
    JCheckBox checkboxIsSelected;

    public SegmentationEditorPanel(final MoMAGui mmgui, String title, int viewWidth, int viewHeight){
        growthlaneViewer = new GrowthlaneViewer(mmgui, viewWidth, viewHeight);
        this.addTitleLabel(title);
        this.addGrowthlaneViewer(growthlaneViewer);
        this.addSelectionCheckbox(mmgui);
        this.setAppearanceAndLayout();
    }

    private void addGrowthlaneViewer(GrowthlaneViewer growthlaneViewer) {
        JPanel centeringPanel = new JPanel();
        this.add(centeringPanel.add(growthlaneViewer));
    }

    private void setAppearanceAndLayout() {
        this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
        this.setBorder(BorderFactory.createMatteBorder(2, 2, 2, 2, Color.GRAY));
    }

    public void setScreenImage(final GrowthLineFrame growthLineFrame, final IntervalView<FloatType> imageView){
        growthlaneViewer.setScreenImage(growthLineFrame, imageView);
    }

    private void addTitleLabel(String title){
        JLabel labelTitle = new JLabel(title);
        labelTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        JPanel centeringPanel = new JPanel();
        this.add(centeringPanel.add(labelTitle));
    }

    private void addSelectionCheckbox(MoMAGui mmgui){
        checkboxIsSelected = new JCheckBox();
        checkboxIsSelected.addActionListener(mmgui);
        JPanel centeringPanel = new JPanel();
        this.add(centeringPanel.add(checkboxIsSelected));
    }

    public boolean isSelected() {
        return checkboxIsSelected.isSelected();
    }
}
