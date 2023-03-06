package com.jug.gui.assignmentview;

import com.jug.lp.EnterAssignment;
import com.jug.lp.Hypothesis;
import com.jug.util.componenttree.AdvancedComponent;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.ValuePair;

import java.awt.*;
import java.awt.geom.GeneralPath;

public class EnterAssignmentView extends AssignmentView {
    private final int ASSIGNMENT_DISPLAY_OFFSET;

    public EnterAssignmentView(final EnterAssignment enterAssignment, int width, int ASSIGNMENT_DISPLAY_OFFSET) {
        this.abstractAssignment = enterAssignment;
        this.width = width;
        this.ASSIGNMENT_DISPLAY_OFFSET = ASSIGNMENT_DISPLAY_OFFSET;
        setupPolygon();
    }

    @Override
    Color GetDefaultColor() {
        return Color.GREEN;
    }

    @Override
    Color GetPrunedColor() {
        return new Color(1f, 0f, 0f, 0.2f);
    }

    private void setupPolygon() {
        final Hypothesis<AdvancedComponent<FloatType>> enteringHypothesis = ((EnterAssignment) abstractAssignment).getTargetHypothesis(0);
        final ValuePair<Integer, Integer> verticalLimits = enteringHypothesis.getLocation();

        float centeringOffset = .5f;

        drawPolygonRectangle(
                this.width - this.width / 2.5f - 1 - centeringOffset,
                verticalLimits.getA() + ASSIGNMENT_DISPLAY_OFFSET,
                this.width / 2.5f, verticalLimits.getB() - verticalLimits.getA()
        );
    }
}
