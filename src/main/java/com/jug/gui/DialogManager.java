package com.jug.gui;

import javax.swing.*;
import java.util.function.Supplier;

/**
 * This class manages the showing of dialogs within in MoMA. It is also meant to allow for correct behavior, when
 * running in headless mode.
 */
public class DialogManager implements IDialogManager {
    private Supplier<MoMAGui> guiSupplier;

    public DialogManager(Supplier<MoMAGui> guiSupplier){
        this.guiSupplier = guiSupplier;
    }

    /**
     * Show error dialog.
     */
    @Override
    public void showErrorDialog(String title, String message) {
        MoMAGui gui = guiSupplier.get();
        JOptionPane.showMessageDialog(gui, message, title, JOptionPane.ERROR_MESSAGE);
    }
}
