package com.jug.gui;

import javax.swing.*;

public abstract class IlpVariableEditorPanel extends JPanel {
    abstract public void display();

    abstract public void unsetVariableConstraints();

    abstract public void setVariableConstraints();

    abstract public void showSegmentationAnnotations(final boolean showSegmentationAnnotations);

    abstract public void addIlpModelChangedEventListener(IlpModelChangedEventListener listener);
}
