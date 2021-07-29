package com.jug.gui;

import com.jug.GrowthLineFrame;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.IntervalView;

import javax.swing.*;
import java.awt.*;

public class SegmentationEditorPanel extends JPanel {
    GrowthlaneViewer growthlaneViewer;
    public SegmentationEditorPanel(final MoMAGui mmgui, String title, int viewWidth, int viewHeight){
        this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
        growthlaneViewer = new GrowthlaneViewer(mmgui, viewWidth, viewHeight);
        this.add(createTitleLabel(title));
        this.add(growthlaneViewer);
        this.setBorder(BorderFactory.createMatteBorder(2, 2, 2, 2, Color.GRAY));
    }

    public void setScreenImage(final GrowthLineFrame glf, final IntervalView<FloatType> viewImg){
        growthlaneViewer.setScreenImage(glf, viewImg);
    }

    private JLabel createTitleLabel(String title){
        JLabel labelTitle = new JLabel(title);
        labelTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        return labelTitle;
    }
}
