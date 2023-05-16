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
        System.out.println("UI action: Dialog reply: " + replyString);
    }

    public static void logUiAction(JButton button) {
        System.out.println("UI action: Button press: " + button.getText());
    }

    public static void logString(String message) {
        System.out.println(message);
    }
}
