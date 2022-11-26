package com.jug.gui;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class FunctionalAction extends AbstractAction {

    ActionListener action;

    public FunctionalAction(ActionListener customaction) {
        this.action = customaction;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        action.actionPerformed(e);
    }

    public ActionListener getAction() {
        return action;
    }

//    public void setAction(ActionListener action) {
//        this.action = action;
//    }
}