package com.jug.lp;

import com.jug.exceptions.GuiMessageException;

public class PruningException extends GuiMessageException {
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
