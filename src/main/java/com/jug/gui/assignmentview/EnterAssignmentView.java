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
        float xLeft = this.width - this.width / 2.5f - 1 - centeringOffset;
        float xRight = this.width - 1 - centeringOffset;
        final float yTop = limitsLeft.getA() + ASSIGNMENT_DISPLAY_OFFSET;
        final float yBottom = limitsLeft.getB() + ASSIGNMENT_DISPLAY_OFFSET;

        polygon = new GeneralPath();
        polygon.moveTo(xLeft, yTop);
        polygon.lineTo(xLeft, yBottom);
        polygon.lineTo(xRight, yBottom);
        polygon.lineTo(xRight, yTop);
        polygon.closePath();
    }
}
