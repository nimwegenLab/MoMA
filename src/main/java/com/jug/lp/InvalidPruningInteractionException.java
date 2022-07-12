package com.jug.lp;

import com.jug.exceptions.GuiInteractionException;

/**
 * This exception is thrown, when the user tries to perform an invalid pruning interaction on one of the segments.
 */
public class InvalidPruningInteractionException extends GuiInteractionException {
    private final String dialogTitle;
    private final String dialogMessage;

    public InvalidPruningInteractionException(String dialogTitle, String dialogMessage) {
        this.dialogTitle = dialogTitle;
        this.dialogMessage = dialogMessage;
    }

    @Override
    public String getDialogTitle() {
        return dialogTitle;
    }

    @Override
    public String getDialogMessage() {
        return dialogMessage;
    }
}
