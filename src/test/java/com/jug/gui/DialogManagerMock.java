package com.jug.gui;

import com.jug.exceptions.GuiInteractionException;
import com.jug.gui.progress.DialogProgress;

import javax.swing.*;

public class DialogManagerMock implements IDialogManager {

    @Override
    public void showErrorDialogWithTextArea(String title, String message) {

    }

    @Override
    public void showPropertiesEditor() {

    }

    @Override
    public void showUserInteractionError(GuiInteractionException exception) {

    }

    @Override
    public DialogProgress getNewProgressDialog(JComponent parent, String message, int totalProgressNotificationsToCome) {
        return null;
    }

    @Override
    public DialogProgress getProgressDialog() {
        return null;
    }

    @Override
    public void closeProgressDialog() {

    }
}
