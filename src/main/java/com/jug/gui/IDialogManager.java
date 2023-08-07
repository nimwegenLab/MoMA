package com.jug.gui;

import com.jug.exceptions.GuiInteractionException;
import com.jug.gui.progress.DialogProgress;

import javax.swing.*;

/**
 * Interface for DialogManager to allow for testing.
 */
public interface IDialogManager {
    void showErrorDialogWithTextArea(String title, String message);

    void showPropertiesEditor();

    void showUserInteractionError(GuiInteractionException exception);

    DialogProgress getNewProgressDialog(final JComponent parent, final String message, final int totalProgressNotificationsToCome);

    DialogProgress getProgressDialog();

    void closeProgressDialog();
}
