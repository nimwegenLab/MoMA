package com.jug.gui.assignmentview;

import com.jug.lp.Hypothesis;
import com.jug.lp.LysisAssignment;
import com.jug.util.componenttree.AdvancedComponent;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.ValuePair;

import java.awt.*;
import java.awt.geom.GeneralPath;

public class LysisAssignmentView extends AssignmentView {
    private final int ASSIGNMENT_DISPLAY_OFFSET;

    public LysisAssignmentView(final LysisAssignment lysisAssignment, int width, int ASSIGNMENT_DISPLAY_OFFSET) {
        this.abstractAssignment = lysisAssignment;
        this.width = width;
        this.ASSIGNMENT_DISPLAY_OFFSET = ASSIGNMENT_DISPLAY_OFFSET;
        setupPolygon();
    }

    @Override
    Color GetDefaultColor() {
        return Color.ORANGE;
    }

    @Override
    Color GetPrunedColor() {
        return new Color(1f, 1f, 0f, 0.2f);
    }

    private void setupPolygon() {
        final Hypothesis<AdvancedComponent<FloatType>> lysingHypothesis = ((LysisAssignment) abstractAssignment).getAssociatedHypothesis();
        final ValuePair<Integer, Integer> verticalLimits = lysingHypothesis.getLocation();

        float centeringOffset = .5f;

        drawPolygonRectangle(
                centeringOffset,
                verticalLimits.getA() + ASSIGNMENT_DISPLAY_OFFSET,
                this.width / 2.5f, verticalLimits.getB() - verticalLimits.getA()
        );
    }
}
