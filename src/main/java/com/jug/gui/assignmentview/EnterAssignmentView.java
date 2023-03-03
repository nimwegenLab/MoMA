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
        final Hypothesis<AdvancedComponent<FloatType>> rightHyp = ((EnterAssignment) abstractAssignment).getTargetHypothesis(0);
        final ValuePair<Integer, Integer> limitsLeft = rightHyp.getLocation();

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
