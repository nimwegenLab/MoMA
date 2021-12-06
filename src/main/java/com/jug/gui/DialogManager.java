package com.jug.gui;

import javax.swing.*;

/**
 * This class manages the showing of dialogs within in MoMA. It is also meant to allow for correct behavior, when
 * running in headless mode.
 */
public class DialogManager implements IDialogManager {
    private MoMAGui gui;

    public DialogManager(MoMAGui gui){
        this.gui = gui;
    }

    /**
     * Show error dialog.
     */
    @Override
    public void showErrorDialog() {
        JOptionPane.showMessageDialog(gui, "File not found!", "Error!", JOptionPane.ERROR_MESSAGE);
    }
}
