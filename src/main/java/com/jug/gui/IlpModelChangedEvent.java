package com.jug.gui;

import java.util.EventListener;
import java.util.EventObject;

public class IlpModelChangedEvent extends EventObject {
    public IlpModelChangedEvent(Object source) {
        super(source);
    }
}

interface IlpModelChangedEventListener extends EventListener {
    public void IlpModelChangedEventOccurred(IlpModelChangedEvent evt);
}
