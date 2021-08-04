package com.jug.gui;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class ToggleSelectionAction extends AbstractAction {
    private JToggleButton checkbox;

    public ToggleSelectionAction(JToggleButton checkbox, String id) {
        super(id);
        this.checkbox = checkbox;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        this.checkbox.setSelected(!this.checkbox.isSelected());
    }
}

