package com.jug.gui.assignmentview;

import com.jug.lp.Hypothesis;
import com.jug.lp.MappingAssignment;
import com.jug.util.componenttree.AdvancedComponent;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.ValuePair;

import java.awt.*;
import java.awt.geom.GeneralPath;

public class MappingAssignmentView extends AssignmentView {
    private final int ASSIGNMENT_DISPLAY_OFFSET;

    public MappingAssignmentView(final MappingAssignment ma, int width, int ASSIGNMENT_DISPLAY_OFFSET) {
        this.abstractAssignment = ma;
        this.width = width - 2;
        this.ASSIGNMENT_DISPLAY_OFFSET = ASSIGNMENT_DISPLAY_OFFSET;
        setupPolygon();
    }

    @Override
    Color getDefaultFaceColor() {
        return new Color(25 / 256f, 65 / 256f, 165 / 256f, 1.0f);
    }

    @Override
    Color getDefaultEdgeColor() {
        return new Color(25 / 256f, 65 / 256f, 165 / 256f, 0.2f);
    }

    void setupPolygon() {
        MappingAssignment ma = (MappingAssignment) abstractAssignment;
        final Hypothesis<AdvancedComponent<FloatType>> leftHyp = ma.getSourceHypothesis();
        final Hypothesis<AdvancedComponent<FloatType>> rightHyp = ma.getDestinationHypothesis();

        final ValuePair<Integer, Integer> limitsLeft = leftHyp.getLocation();
        final ValuePair<Integer, Integer> limitsRight = rightHyp.getLocation();

        float centeringOffset = .5f;
        float xRight = this.width + centeringOffset;
        final float x1 = 0 + centeringOffset;
        final float y1 = limitsLeft.getA() + ASSIGNMENT_DISPLAY_OFFSET;
        final float x2 = 0 + centeringOffset;
        final float y2 = limitsLeft.getB() + ASSIGNMENT_DISPLAY_OFFSET;
        final float y3 = limitsRight.getB() + ASSIGNMENT_DISPLAY_OFFSET;
        final float y4 = limitsRight.getA() + ASSIGNMENT_DISPLAY_OFFSET;

        polygon = new GeneralPath();
        polygon.moveTo(x1, y1);
        polygon.lineTo(x2, y2);
        polygon.lineTo(xRight, y3);
        polygon.lineTo(xRight, y4);
        polygon.closePath();
    }
}
