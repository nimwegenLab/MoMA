package com.jug.gui;

import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;

public class WindowFocusListenerImplementation implements WindowFocusListener {
    private MoMAGui momaGui;

    public WindowFocusListenerImplementation(MoMAGui momaGui){
        this.momaGui = momaGui;
    }

    @Override
    public void windowGainedFocus(WindowEvent e) {
        momaGui.requestFocusOnTimeStepSlider();
    }

    @Override
    public void windowLostFocus(WindowEvent e) {
        /* do nothing*/
    }
}
