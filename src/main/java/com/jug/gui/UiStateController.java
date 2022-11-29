package com.jug.gui;

import com.jug.Growthlane;

import javax.swing.event.ChangeListener;

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
        momaModel.getCurrentGL().addChangeListener(initializationCallback);
        momaModel.getCurrentGL().addChangeListener(e ->{
            Growthlane gl = ((Growthlane) e.getSource());
            sliderPanel.setEnabled(gl.getIlp().isReady() || gl.getIlp().isInfeasible());
        });
    }

    ChangeListener initializationCallback;
}
