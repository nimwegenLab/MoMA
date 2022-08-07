package com.jug.gui;

import com.jug.Growthlane;
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
        int initialTimeStep = 0; /* MM-20220620: I initialize the current time step to the start of the optimization interval. This is to ensure that the position of the current time-step is consistent with that of the optimization range. Because the optimization range will be adjusted otherwise with the current implementation. */
        timestepSlider = new JSlider(SwingConstants.HORIZONTAL, 0, model.getTimeStepMaximumOfCurrentGl(), initialTimeStep);
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
        timestepSlider.addChangeListener((e) -> {
            this.labelCurrentTime.setText(String.format(" t = %4d", timestepSlider.getValue()));
            this.model.setCurrentGLF(timestepSlider.getValue());
        });

        // --- Slider for TrackingRage ----------
        trackingRangeSlider = new RangeSlider(0, model.getTimeStepMaximumOfCurrentGl());
        trackingRangeSlider.setBorder(BorderFactory.createEmptyBorder(0, 7, 0, 7));
        trackingRangeSlider.setValue(initialTimeStep);
        trackingRangeSlider.setUpperValue(configurationManager.getMaxTime());

        final JLabel lblIgnoreBeyond =
                new JLabel(String.format("opt. range:", trackingRangeSlider.getValue()));
        lblIgnoreBeyond.setToolTipText("correct up to left slider / ignore data beyond right slider");

        this.add(lblIgnoreBeyond);
        this.add(trackingRangeSlider);
        this.add(labelCurrentTime);
        this.add(timestepSlider);

        final Growthlane currentGL = model.getCurrentGL();
        trackingRangeSlider.setEnabled(currentGL.ilpIsReady());

        initializationCallback = (e) -> { /* this callback is a hack to set the sliders to the correct state, once the ILP has been initialized; the boolean slidersInitialized serves to run it only once */
            Growthlane gl = ((Growthlane) e.getSource());
            if (gl.ilpIsReady()) {
                trackingRangeSlider.setEnabled(true);
                int optimizationRangeStart = gl.getIlp().getOptimizationRangeStart();
                setTrackingRangeStart(optimizationRangeStart);
                timestepSlider.setValue(optimizationRangeStart);
                int optimizationRangeEnd = gl.getIlp().getOptimizationRangeEnd();
                setTrackingRangeEnd(optimizationRangeEnd);
                currentGL.removeChangeListener(initializationCallback);
            }
        };
        currentGL.addChangeListener(initializationCallback);
    }

    ChangeListener initializationCallback;

    public void requestFocusOnTimeStepSlider(){
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

    public void setTimeStepSliderPosition(int timestep) {
        getTimestepSlider().setValue(timestep);
    }

    public int getTimeStepSliderPosition() {
        return getTimestepSlider().getValue();
    }

    public int getTimeStepSliderMaximum(){
        return getTimestepSlider().getMaximum();
    }

    public void setTrackingRangeEnd(int timeStep) {
        getTrackingRangeSlider().setUpperValue(timeStep);
    }

    public int getTrackingRangeEnd() {
        return getTrackingRangeSlider().getUpperValue();
    }

    public void setTrackingRangeStart(int timeStep) {
        getTrackingRangeSlider().setValue(timeStep);
    }

    public int getTrackingRangeStart() {
        return getTrackingRangeSlider().getValue();
    }

    public int getTrackingRangeSliderMaximum() {
        return getTrackingRangeSlider().getMaximum();
    }
}
