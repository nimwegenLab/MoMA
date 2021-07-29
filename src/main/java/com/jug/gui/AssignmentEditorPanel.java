package com.jug.gui;

import com.jug.gui.assignmentview.AssignmentsEditorViewer;
import com.jug.lp.AbstractAssignment;
import com.jug.lp.Hypothesis;
import net.imglib2.algorithm.componenttree.Component;
import net.imglib2.type.numeric.real.FloatType;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Set;

public class AssignmentEditorPanel extends JPanel {
    private final AssignmentsEditorViewer assignmentView;

    public AssignmentEditorPanel(final MoMAGui mmgui, int viewHeight) {
        assignmentView = new AssignmentsEditorViewer(viewHeight, mmgui);
        assignmentView.addChangeListener(mmgui);
        this.addAssignmentView(assignmentView);
        this.setAppearanceAndLayout();
    }

    private void addAssignmentView(AssignmentsEditorViewer assignmentView){
        this.add(assignmentView);
    }

    public void display(){
        assignmentView.display();
    }

    public void display(final HashMap<Hypothesis<Component<FloatType, ?>>, Set<AbstractAssignment<Hypothesis<Component<FloatType, ?>>>>> hashMap){
        assignmentView.display(hashMap);
    }

    private void setAppearanceAndLayout() {
        this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
    }
}
