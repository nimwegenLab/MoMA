package com.jug.gui;

import com.jug.exceptions.GuiInteractionException;
import com.jug.gui.progress.DialogProgress;
import com.jug.logging.LoggingHelper;

import javax.swing.*;
import java.util.function.Supplier;

import static java.util.Objects.isNull;

/**
 * This class manages the showing of dialogs within in MoMA. It is also meant to allow for correct behavior, when
 * running in headless mode.
 */
public class DialogManager implements IDialogManager {
    private Supplier<MoMAGui> guiSupplier;
    private Supplier<DialogPropertiesEditor> propertiesEditorSupplier;
    private DialogPropertiesEditor propertiesEditor;

    public DialogManager(Supplier<MoMAGui> guiSupplier, Supplier<DialogPropertiesEditor> propertiesEditorSupplier){
        this.guiSupplier = guiSupplier;
        this.propertiesEditorSupplier = propertiesEditorSupplier;
        this.propertiesEditor = propertiesEditor;
    }

    /**
     * Show error dialog.
     */
    @Override
    public void showErrorDialogWithTextArea(String title, String message) {
        MoMAGui gui = guiSupplier.get();
        LoggingHelper.logString("Error message shown:\n" + "Title: " + message + "\n" + "Message: " + message);
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
        propertiesEditorSupplier.get().setVisible(true);
//        propertiesEditor.setVisible(true);
    }

    @Override
    public void showUserInteractionError(GuiInteractionException exception) {
        showErrorDialogWithTextArea(exception.getDialogTitle(), exception.getDialogMessage());
    }

    DialogProgress currentProgressDialog = null;

    @Override
    public DialogProgress getNewProgressDialog(final JComponent parent, final String message, final int totalProgressNotificationsToCome) {
        currentProgressDialog = new DialogProgress(parent, message, totalProgressNotificationsToCome);
        return currentProgressDialog;
    }

    @Override
    public DialogProgress getProgressDialog() {
        if(isNull(currentProgressDialog)){
            throw new RuntimeException("No progress dialog has been created yet. Please create one first.");
        }
        return currentProgressDialog;
    }

    @Override
    public void closeProgressDialog() {
        if (isNull(currentProgressDialog)) {
            throw new RuntimeException("No progress dialog has been created yet. Please create one first.");
        }
        currentProgressDialog.setVisible(false);
        currentProgressDialog.dispose();
        currentProgressDialog = null;
    }
}
