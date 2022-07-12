package com.jug.exceptions;

abstract public class GuiMessageException extends Exception {
    abstract public String getDialogTitle();
    abstract public String getDialogMessage();
}
