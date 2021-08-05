package com.jug.gui;

import java.util.EventListener;

public interface IlpModelChangedEventListener extends EventListener {
    void IlpModelChangedEventOccurred(IlpModelChangedEvent evt);
}
