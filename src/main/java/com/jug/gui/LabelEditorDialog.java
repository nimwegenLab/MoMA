package com.jug.gui;

import com.jug.lp.Hypothesis;
import net.imglib2.algorithm.componenttree.Component;
import net.imglib2.type.numeric.real.FloatType;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.List;

public class LabelEditorDialog extends JDialog {
    private final List<String> labelList;

    public LabelEditorDialog(final JComponent mmgui, List<String> labelList) {
        super((Frame) SwingUtilities.windowForComponent(mmgui), "Label Editor", true);
        this.labelList = labelList;
        this.dialogInit();
    }

    public void edit(Hypothesis<Component<FloatType, ?>> hyp) {
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

    private void buildGui(Hypothesis<Component<FloatType, ?>> hyp) {
        this.setRootPane(new JRootPane());
        MigLayout layout = new MigLayout("wrap 1", "", "");
        this.rootPane.setLayout(layout);
        for (String label : labelList) {
            JCheckBox checkbox = new JCheckBox();
            checkbox.setText(label);
            if (hyp.labels.contains(label)){
                checkbox.setSelected(true);
            }
            checkbox.addItemListener(e -> {
                if(checkbox.isSelected()){
                    hyp.labels.add(label);
                }
                else{
                    hyp.labels.remove(label);
                }
            });
            this.rootPane.add(checkbox);
        }
    }
}
