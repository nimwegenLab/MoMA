package com.jug.gui.assignmentview;

import com.jug.lp.Hypothesis;
import com.jug.lp.MappingAssignment;
import net.imglib2.algorithm.componenttree.Component;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.ValuePair;

import java.awt.*;
import java.awt.geom.GeneralPath;

public class MappingAssignmentView extends AssignmentView {
    private int ASSIGNMENT_DISPLAY_OFFSET;

    public MappingAssignmentView(final MappingAssignment ma, int width, int ASSIGNMENT_DISPLAY_OFFSET) {
        this.abstractAssignment = ma;
        this.width = width;
        this.ASSIGNMENT_DISPLAY_OFFSET = ASSIGNMENT_DISPLAY_OFFSET;
        setupPolygon();
    }

    void setupPolygon() {
        MappingAssignment ma = (MappingAssignment) abstractAssignment;
        final Hypothesis<net.imglib2.algorithm.componenttree.Component<FloatType, ?>> leftHyp = ma.getSourceHypothesis();
        final Hypothesis<Component<FloatType, ?>> rightHyp = ma.getDestinationHypothesis();

        final ValuePair<Integer, Integer> limitsLeft = leftHyp.getLocation();
        final ValuePair<Integer, Integer> limitsRight = rightHyp.getLocation();

        final int x1 = 0;
        final int y1 = limitsLeft.getA() + ASSIGNMENT_DISPLAY_OFFSET;
        final int x2 = 0;
        final int y2 = limitsLeft.getB() + ASSIGNMENT_DISPLAY_OFFSET;
        final int y3 = limitsRight.getB() + ASSIGNMENT_DISPLAY_OFFSET;
        final int y4 = limitsRight.getA() + ASSIGNMENT_DISPLAY_OFFSET;

        polygon = new GeneralPath();
        polygon.moveTo(x1, y1);
        polygon.lineTo(x2, y2);
        polygon.lineTo(this.width, y3);
        polygon.lineTo(this.width, y4);
        polygon.closePath();
    }

    public void draw(final Graphics2D g2) {
        Color isPrunedColor = new Color(25 / 256f, 65 / 256f, 165 / 256f, 0.2f);
        Color defaultColor = new Color(25 / 256f, 65 / 256f, 165 / 256f, 1.0f);
        super.draw(g2, defaultColor, isPrunedColor);
    }
}
