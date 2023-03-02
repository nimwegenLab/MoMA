package com.jug.gui.assignmentview;

import com.jug.lp.ExitAssignment;
import com.jug.lp.Hypothesis;
import com.jug.util.componenttree.AdvancedComponent;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.ValuePair;

import java.awt.*;
import java.awt.geom.GeneralPath;

public class EnterAssignmentView extends AssignmentView {
    private final int ASSIGNMENT_DISPLAY_OFFSET;

    public EnterAssignmentView(final ExitAssignment exitAssignment, int width, int ASSIGNMENT_DISPLAY_OFFSET) {
        this.abstractAssignment = exitAssignment;
        this.width = width;
        this.ASSIGNMENT_DISPLAY_OFFSET = ASSIGNMENT_DISPLAY_OFFSET;
        setupPolygon();
    }

    @Override
    Color GetDefaultColor() {
        return Color.RED;
    }

    @Override
    Color GetPrunedColor() {
        return new Color(1f, 0f, 0f, 0.2f);
    }

    private void setupPolygon() {
        final Hypothesis<AdvancedComponent<FloatType>> leftHyp = ((ExitAssignment) abstractAssignment).getAssociatedHypothesis();
        final ValuePair<Integer, Integer> limitsLeft = leftHyp.getLocation();

        float centeringOffset = .5f;
        float xRightSide = this.width / 2.5f + centeringOffset;
        final float x1 = 0 + centeringOffset;
        final float y1 = limitsLeft.getA() + ASSIGNMENT_DISPLAY_OFFSET;
        final float x2 = 0 + centeringOffset;
        final float y2 = limitsLeft.getB() + ASSIGNMENT_DISPLAY_OFFSET;
        final float y3 = limitsLeft.getB() + ASSIGNMENT_DISPLAY_OFFSET;
        final float y4 = limitsLeft.getA() + ASSIGNMENT_DISPLAY_OFFSET;

        polygon = new GeneralPath();
        polygon.moveTo(x1, y1);
        polygon.lineTo(x2, y2);
        polygon.lineTo(xRightSide, y3);
        polygon.lineTo(xRightSide, y4);
        polygon.closePath();
    }
}
