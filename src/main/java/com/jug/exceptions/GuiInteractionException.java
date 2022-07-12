package com.jug.exceptions;

abstract public class GuiInteractionException extends RuntimeException {
    abstract public String getDialogTitle();
    abstract public String getDialogMessage();
}
