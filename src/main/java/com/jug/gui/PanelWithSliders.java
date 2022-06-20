package com.jug.gui;

import com.jug.MoMA;
import com.jug.config.ConfigurationManager;
import com.jug.gui.slider.RangeSlider;

import javax.swing.*;
import javax.swing.event.ChangeListener;
import java.awt.*;

public class PanelWithSliders extends JPanel {
    private JLabel labelCurrentTime;
    private RangeSlider trackingRangeSlider;
    private ConfigurationManager configurationManager;
    private MoMAModel model;
    public JSlider timestepSlider;

    public PanelWithSliders(LayoutManager layout, ConfigurationManager configurationManager, final MoMAModel model){
        super(layout);
        this.configurationManager = configurationManager;
        this.model = model;
        build();
    }

    private void build() {
        // --- Slider for time and GL -------------
        timestepSlider = new JSlider(SwingConstants.HORIZONTAL, 0, model.getTimeStepMaximum(), 0);
        model.setCurrentGLF(timestepSlider.getValue());

        if (timestepSlider.getMaximum() < 200) {
            timestepSlider.setMajorTickSpacing(10);
            timestepSlider.setMinorTickSpacing(2);
        } else {
            timestepSlider.setMajorTickSpacing(100);
            timestepSlider.setMinorTickSpacing(10);
        }
        timestepSlider.setPaintTicks(true);
        timestepSlider.setPaintLabels(true);
        timestepSlider.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 3));

        labelCurrentTime = new JLabel(String.format(" t = %4d", timestepSlider.getValue()));
        timestepSlider.addChangeListener((e) -> this.updateCenteredTimeStep());

        // --- Slider for TrackingRage ----------
        int optimizationRangeStart = configurationManager.getOptimizationRangeStart();
        int optimizationRangeEnd = configurationManager.getOptimizationRangeEnd();
        if (MoMA.getInitialOptimizationRange() != -1) {
            optimizationRangeEnd = Math.min(MoMA.getInitialOptimizationRange(), model.getTimeStepMaximum());
        }
        trackingRangeSlider = new RangeSlider(0, model.getTimeStepMaximum());
        trackingRangeSlider.setBorder(BorderFactory.createEmptyBorder(0, 7, 0, 7));
        trackingRangeSlider.setValue(optimizationRangeStart);
        if (configurationManager.OPTIMISATION_INTERVAL_LENGTH >= 0) {
            trackingRangeSlider.setUpperValue(configurationManager.OPTIMISATION_INTERVAL_LENGTH);
        } else {
            trackingRangeSlider.setUpperValue(optimizationRangeEnd);
        }
        trackingRangeSlider.addChangeListener((e) -> {
            configurationManager.setOptimizationRangeStart(trackingRangeSlider.getValue());
            configurationManager.setOptimizationRangeEnd(trackingRangeSlider.getUpperValue());
        });

        final JLabel lblIgnoreBeyond =
                new JLabel(String.format("opt. range:", trackingRangeSlider.getValue()));
        lblIgnoreBeyond.setToolTipText("correct up to left slider / ignore data beyond right slider");

        this.add(lblIgnoreBeyond);
        this.add(trackingRangeSlider);
        this.add(labelCurrentTime);
        this.add(timestepSlider);
    }

    public void updateCenteredTimeStep() {
        this.labelCurrentTime.setText(String.format(" t = %4d", timestepSlider.getValue()));
        this.model.setCurrentGLF(timestepSlider.getValue());
    }

    public void requestFocus(){
        timestepSlider.requestFocus();
    }

    public JSlider getTimestepSlider() {
        return timestepSlider;
    }

    public RangeSlider getTrackingRangeSlider() {
        return trackingRangeSlider;
    }

    public void addListenerToTimeSlider(ChangeListener listener) {
        timestepSlider.addChangeListener(listener);
    }

    public void addListenerToRangeSlider(ChangeListener listener) {
        trackingRangeSlider.addChangeListener(listener);
    }
}
