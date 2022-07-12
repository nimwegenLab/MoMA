package com.jug.lp;

import com.jug.exceptions.GuiInteractionException;

public class PruningException extends GuiInteractionException {
    private final String dialogTitle;
    private final String dialogMessage;

    public PruningException(String dialogTitle, String dialogMessage) {
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
