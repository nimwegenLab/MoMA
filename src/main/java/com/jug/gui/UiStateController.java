package com.jug.gui;

import com.jug.Growthlane;

import javax.swing.*;
import javax.swing.event.ChangeListener;
import java.util.ArrayList;
import java.util.List;

public class UiStateController {
    private final MoMAModel momaModel;
    private final MoMAGui momaGui;
    private final PanelWithSliders sliderPanel;

    public UiStateController(MoMAModel momaModel, MoMAGui momaGui, PanelWithSliders sliderPanel) {
        this.momaModel = momaModel;
        this.momaGui = momaGui;
        this.sliderPanel = sliderPanel;
        this.sliderPanel.setEnabled(false);
        hookUpPanelSliderEvents();
        hookUpButtons();
        setInitialUiState();
    }

    private void hookUpPanelSliderEvents() {
        initializationCallback = e -> { /* this callback is a hack to set the sliders to the correct state, once the ILP has been initialized; the boolean slidersInitialized serves to run it only once */
            Growthlane gl = ((Growthlane) e.getSource());
            if (gl.ilpIsReady()) {
                momaModel.getCurrentGL().removeChangeListener(initializationCallback);
                int optimizationRangeStart = gl.getIlp().getOptimizationRangeStart();
                int optimizationRangeEnd = gl.getIlp().getOptimizationRangeEnd();
                sliderPanel.setTrackingRangeStart(optimizationRangeStart);
                sliderPanel.setTrackingRangeEnd(optimizationRangeEnd);
            }
        };
    }

    private void setInitialUiState() {
        getAllComponentsToUpdate().stream().forEach(jComponent -> jComponent.setEnabled(true));
        getComponentsToDeactivateWhenOptimizationWasNeverRun().stream().forEach(jComponent -> jComponent.setEnabled(false));
    }

    private void hookUpButtons() {
        momaModel.getCurrentGL().addChangeListener(e -> {
            Growthlane gl = (Growthlane) e.getSource();
            getAllComponentsToUpdate().stream().forEach(jComponent -> jComponent.setEnabled(true));
            if (gl.getIlp().isRunning()) {
                getComponentsToDeactivateWhenOptimizationIsRunning().stream().forEach(jComponent -> jComponent.setEnabled(false));
            }
            if (gl.getIlp().isOptimizationNotPerformed()) {
                getComponentsToDeactivateWhenOptimizationWasNeverRun().stream().forEach(jComponent -> jComponent.setEnabled(false));
            }
            if (gl.getIlp().isInfeasible()) {
                getComponentsToDeactivateWhenIlpIsInfeasible().stream().forEach(jComponent -> jComponent.setEnabled(false));
            }
        });
    }

    private List<JComponent> getAllComponentsToUpdate() {
        List<JComponent> list = new ArrayList<>(momaGui.getAllComponentsToUpdate());
        list.add(sliderPanel.getTrackingRangeSlider());
        list.add(sliderPanel.getTimestepSlider());
        return list;
    }

    private List<JComponent> getComponentsToDeactivateWhenOptimizationIsRunning(){
        List<JComponent> list = new ArrayList<>(momaGui.getComponentsToDeactivateWhenOptimizationIsRunning());
        list.add(sliderPanel.getTrackingRangeSlider());
        list.add(sliderPanel.getTimestepSlider());
        return list;
    }

    public List<JComponent> getComponentsToDeactivateWhenOptimizationWasNeverRun() {
        List<JComponent> list = new ArrayList<>(momaGui.getComponentsToDeactivateWhenOptimizationWasNeverRun());
        list.add(sliderPanel.getTrackingRangeSlider());
        return list;
    }

    public List<JComponent> getComponentsToDeactivateWhenIlpIsInfeasible() {
        List<JComponent> list = new ArrayList<>(momaGui.getComponentsToDeactivateWhenIlpIsInfeasible());
        return list;
    }

    ChangeListener initializationCallback;
}
