package com.jug.logging;

import javax.swing.*;

public class LoggingHelper {
    public static void logUiAction(int dialogChoice) {
        String replyString = "UNDEFINED";
        switch (dialogChoice) {
            case JOptionPane.YES_OPTION: /* this has same value as JOptionPane.OK_OPTION */
                replyString = "YES_OPTION";
                break;
            case JOptionPane.NO_OPTION:
                replyString = "NO_OPTION";
                break;
            case JOptionPane.CANCEL_OPTION:
                replyString = "CANCEL_OPTION";
                break;
            case JOptionPane.CLOSED_OPTION:
                replyString = "CLOSED_OPTION";
                break;
        }
        System.out.println("UI action: dialog reply: " + replyString);
    }

    public static void logUiAction(JButton button) {
        System.out.println("UI action: button press: " + button.getText());
    }

    public static void logUiAction(JButton button, String additionalInfo) {
        System.out.println("UI action: button press: " + button.getText() + "; " + additionalInfo);
    }

    public static void logUiAction(JTextField button) {
        System.out.println("UI action: text input: " + button.getText());
    }

    public static void logUiAction(JTextField button, String additionalInfo) {
        System.out.println("UI action: text input: " + button.getText() + "; " + additionalInfo);
    }

    public static void logUiAction(JCheckBox checkBox) {
        System.out.println("UI action: checkBox selected: " + checkBox.isSelected());
    }

    public static void logUiAction(JCheckBox checkBox, String additionalInfo) {
        System.out.println("UI action: checkBox selected: " + checkBox.isSelected() + "; " + additionalInfo);
    }

    public static void logString(String message) {
        System.out.println(message);
    }
}
