package com.jug.logging;

import com.jug.gui.assignmentview.AssignmentView;
import com.jug.gui.slider.RangeSlider;
import com.jug.lp.AbstractAssignment;
import com.jug.lp.Hypothesis;

import javax.swing.*;

import static java.util.Objects.isNull;

public class LoggingHelper {
    private static String idString = "ui_action: ";

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
        System.out.println(idString + "dialog reply: " + replyString);
    }

    public static void logUiAction(JButton button) {
        System.out.println(idString + "button press: " + button.getText());
    }

    public static void logUiAction(JButton button, String additionalInfo) {
        System.out.println(idString + "button press: " + button.getText() + "; " + additionalInfo);
    }

    public static void logUiAction(JTextField button) {
        System.out.println(idString + "text input: " + button.getText());
    }

    public static void logUiAction(JTextField button, String additionalInfo) {
        System.out.println(idString + "text input: " + button.getText() + "; " + additionalInfo);
    }

    public static void logUiAction(JCheckBox checkBox) {
        System.out.println(idString + "checkbox selected: " + checkBox.isSelected());
    }

    public static void logUiAction(JCheckBox checkBox, String additionalInfo) {
        System.out.println(idString + "checkbox selected: " + checkBox.isSelected() + "; " + additionalInfo);
    }

    public static <T extends JSlider> void logUiAction(String actionDescription, T slider) {
        if (slider instanceof RangeSlider) {
            System.out.println(idString + actionDescription + "; RangeSlider.getValue(): " + slider.getValue() + "; RangeSlider.getUpperValue(): " + ((RangeSlider) slider).getUpperValue());
        } else if (slider instanceof JSlider) {
            System.out.println(idString + actionDescription + "; JSlider.getValue(): " + slider.getValue());
        }
    }

    public static void logUiAction(AssignmentView assignmentView, String additionalInfo) {
        System.out.println(idString + "assignment view: " + assignmentView.getAssignmentIdString() + "; " + additionalInfo);
    }

    public static void logUiAction(String actionDescription, AbstractAssignment assignment) {
        if (!isNull(assignment)) {
            System.out.println(idString + "" + actionDescription + "; " + assignment);
        }
    }

    public static void logHypothesisAction(String actionDescription, Hypothesis hypothesis) {
        if (!isNull(hypothesis)) {
            System.out.println(idString + "" + actionDescription + "; " + hypothesis);
        }
    }

    public static void logUiAction(String actionDescription) {
        System.out.println(idString + "" + actionDescription);
    }
    public static void logString(String message) {
        System.out.println(message);
    }
}
