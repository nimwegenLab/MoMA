package com.jug.gui;

import com.jug.exceptions.GuiInteractionException;

/**
 * Interface for DialogManager to allow for testing.
 */
public interface IDialogManager {
    void showErrorDialogWithTextArea(String title, String message);

    void showPropertiesEditor();

    void showUserInteractionError(GuiInteractionException exception);
}
