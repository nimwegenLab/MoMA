package com.jug.gui;

import com.jug.MoMA;
import com.jug.config.ConfigurationManager;
import com.jug.gui.slider.RangeSlider;

import javax.swing.*;
import javax.swing.event.ChangeListener;
import java.awt.*;

public class RangeSliderPanel extends JPanel {
    private JLabel labelCurrentTime;
    private RangeSlider sliderTrackingRange;
    private ConfigurationManager configurationManager;
    private MoMAModel model;
    public JSlider sliderTime;

    public RangeSliderPanel(LayoutManager layout, ConfigurationManager configurationManager, final MoMAModel model){
        super(layout);
        this.configurationManager = configurationManager;
        this.model = model;
        build();
    }

    private void build() {
        // --- Slider for time and GL -------------
        sliderTime = new JSlider(SwingConstants.HORIZONTAL, 0, model.getTimeStepMaximum(), 0);
        model.setCurrentGLF(sliderTime.getValue());

        if (sliderTime.getMaximum() < 200) {
            sliderTime.setMajorTickSpacing(10);
            sliderTime.setMinorTickSpacing(2);
        } else {
            sliderTime.setMajorTickSpacing(100);
            sliderTime.setMinorTickSpacing(10);
        }
        sliderTime.setPaintTicks(true);
        sliderTime.setPaintLabels(true);
        sliderTime.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 3));

        labelCurrentTime = new JLabel(String.format(" t = %4d", sliderTime.getValue()));

        // --- Slider for TrackingRage ----------

        int max = model.getTimeStepMaximum();
        if (MoMA.getInitialOptimizationRange() != -1) {
            max = Math.min(MoMA.getInitialOptimizationRange(), model.getTimeStepMaximum());
        }
        sliderTrackingRange =
                new RangeSlider(0, model.getTimeStepMaximum());
        sliderTrackingRange.setBorder(BorderFactory.createEmptyBorder(0, 7, 0, 7));
        sliderTrackingRange.setValue(0);
        if (configurationManager.OPTIMISATION_INTERVAL_LENGTH >= 0) {
            sliderTrackingRange.setUpperValue(configurationManager.OPTIMISATION_INTERVAL_LENGTH);
        } else {
            sliderTrackingRange.setUpperValue(max);
        }
//        sliderTrackingRange.addChangeListener(this);
        final JLabel lblIgnoreBeyond =
                new JLabel(String.format("opt. range:", sliderTrackingRange.getValue()));
        lblIgnoreBeyond.setToolTipText("correct up to left slider / ignore data beyond right slider");

        // --- Assemble sliders -----------------
        this.add(lblIgnoreBeyond);
        this.add(sliderTrackingRange);
        this.add(labelCurrentTime);
        this.add(sliderTime);
    }

    public void updateCenteredTimeStep(){
        this.labelCurrentTime.setText(String.format(" t = %4d", sliderTime.getValue()));
    }

    public void requestFocus(){
        sliderTime.requestFocus();
    }

    public JSlider getSliderTime() {
        return sliderTime;
    }

    public RangeSlider getSliderTrackingRange() {
        return sliderTrackingRange;
    }

    public void addListenerToTimeSlider(ChangeListener listener) {
        sliderTime.addChangeListener(listener);
    }

    public void addListenerToRangeSlider(ChangeListener listener) {
        sliderTrackingRange.addChangeListener(listener);
    }
}
