package com.jug.gui;

import com.jug.lp.Hypothesis;
import com.jug.util.componenttree.AdvancedComponent;
import net.imglib2.type.numeric.real.FloatType;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import static javax.swing.JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT;

public class LabelEditorDialog extends JDialog {
    private final List<String> labelList;

    public LabelEditorDialog(final JComponent mmgui, List<String> labelList) {
        super((Frame) SwingUtilities.windowForComponent(mmgui), "Label Editor", true);
        this.labelList = labelList;
        this.dialogInit();
    }

    public void edit(Hypothesis<AdvancedComponent<FloatType>> hyp) {
        buildGui(hyp);
        this.setVisibleNew(true);
    }

    public void setVisibleNew(final boolean show) {
        final int width = 500;
        final int height = 500;
        final int x = super.getParent().getX() + super.getParent().getWidth() / 2 - width / 2;
        final int y = super.getParent().getY() + super.getParent().getHeight() / 2 - height / 2;
        this.setBounds(x, y, width, height);
        super.setVisible(show);
    }

    private void buildGui(Hypothesis<AdvancedComponent<FloatType>> hyp) {
        this.setRootPane(new JRootPane());
        MigLayout layout = new MigLayout("wrap 1", "", "");
        this.rootPane.setLayout(layout);
        ArrayList<JCheckBox> checkBoxes = new ArrayList<>();
        int keyNumber = 1;
        for (String label : labelList) {
            String id = "key_" + keyNumber;
            JCheckBox checkbox = new JCheckBox();
            checkbox.setText(label + " (" + keyNumber + ")");
            if (hyp.labels.contains(label)) {
                checkbox.setSelected(true);
            }
            checkbox.addItemListener(e -> {
                if (checkbox.isSelected()) {
                    hyp.labels.add(label);
                } else {
                    hyp.labels.remove(label);
                }
            });
            this.rootPane.getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT)
                    .put(KeyStroke.getKeyStroke(Integer.toString(keyNumber)), id);
            this.rootPane.getActionMap().put(id, new ToggleSelectionAction(checkbox, id));
            this.rootPane.add(checkbox);
            checkBoxes.add(checkbox);
            keyNumber++;
        }

        /* define clear button */
        JButton clearButton = new JButton();
        clearButton.setText("Clear");
        clearButton.addActionListener(e -> {
            for (JCheckBox checkBox : checkBoxes) {
                checkBox.setSelected(false);
            }
        });
        this.rootPane.add(clearButton);
        Action deleteAction = new AbstractAction("Close") {
            public void actionPerformed(ActionEvent e) {
                clearButton.doClick();
            }
        };
        this.rootPane.getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT)
                .put(KeyStroke.getKeyStroke("DELETE"), "deleteAction");
        this.rootPane.getActionMap().put("deleteAction", deleteAction);

        /* attach close action to ESCAPE */
        Action closeAction = new AbstractAction("Close") {
            public void actionPerformed(ActionEvent e) {
                java.awt.Component component = (java.awt.Component) e.getSource();
                JDialog dialog = (JDialog) SwingUtilities.getRoot(component);
                dialog.dispose();
            }
        };
        this.rootPane.getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT)
                .put(KeyStroke.getKeyStroke("ESCAPE"), "escapeAction");
        this.rootPane.getActionMap().put("escapeAction", closeAction);
    }
}
