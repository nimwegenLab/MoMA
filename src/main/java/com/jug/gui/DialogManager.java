package com.jug.gui;

import javax.swing.*;
import java.util.function.Supplier;

/**
 * This class manages the showing of dialogs within in MoMA. It is also meant to allow for correct behavior, when
 * running in headless mode.
 */
public class DialogManager implements IDialogManager {
    private Supplier<MoMAGui> guiSupplier;
    private DialogPropertiesEditor propertiesEditor;

    public DialogManager(Supplier<MoMAGui> guiSupplier, DialogPropertiesEditor propertiesEditor){
        this.guiSupplier = guiSupplier;
        this.propertiesEditor = propertiesEditor;
    }

    /**
     * Show error dialog.
     */
    @Override
    public void showErrorDialogWithTextArea(String title, String message) {
        MoMAGui gui = guiSupplier.get();

        SwingUtilities.invokeLater(() -> {
            JTextArea textArea = new JTextArea(12, 55);
            textArea.setText(message);
            textArea.setEditable(false);
            JScrollPane scrollPane = new JScrollPane(textArea);
            JOptionPane.showMessageDialog(gui, scrollPane, title, JOptionPane.ERROR_MESSAGE);
        });
    }

    @Override
    public void showPropertiesEditor() {
        propertiesEditor.setVisible(true);
    }
}
