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
        hookUpPanelSliderEvents();
    }

    private void hookUpPanelSliderEvents() {
        initializationCallback = (e) -> { /* this callback is a hack to set the sliders to the correct state, once the ILP has been initialized; the boolean slidersInitialized serves to run it only once */
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
    }

    ChangeListener initializationCallback;
}
