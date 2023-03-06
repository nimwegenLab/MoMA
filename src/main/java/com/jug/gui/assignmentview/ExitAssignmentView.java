package com.jug.gui.assignmentview;

import com.jug.lp.ExitAssignment;
import com.jug.lp.Hypothesis;
import com.jug.util.componenttree.AdvancedComponent;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.ValuePair;

import java.awt.*;

public class ExitAssignmentView extends AssignmentView {
    private final int ASSIGNMENT_DISPLAY_OFFSET;

    public ExitAssignmentView(final ExitAssignment exitAssignment, int width, int ASSIGNMENT_DISPLAY_OFFSET) {
        this.abstractAssignment = exitAssignment;
        this.width = width;
        this.ASSIGNMENT_DISPLAY_OFFSET = ASSIGNMENT_DISPLAY_OFFSET;
        setupPolygon();
    }

    @Override
    Color getDefaultFaceColor() {
        return Color.RED;
    }

    @Override
    Color getDefaultEdgeColor() {
        return new Color(1f, 0f, 0f, 0.2f);
    }

    private void setupPolygon() {
        final Hypothesis<AdvancedComponent<FloatType>> exitingHypothesis = ((ExitAssignment) abstractAssignment).getAssociatedHypothesis();
        final ValuePair<Integer, Integer> verticalLimits = exitingHypothesis.getLocation();

        float centeringOffset = .5f;

        drawPolygonRectangle(
                centeringOffset,
                verticalLimits.getA() + ASSIGNMENT_DISPLAY_OFFSET,
                this.width / 2.5f, verticalLimits.getB() - verticalLimits.getA()
        );
    }
}
