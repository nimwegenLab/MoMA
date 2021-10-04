package com.jug.gui.assignmentview;

import com.jug.lp.DivisionAssignment;
import com.jug.lp.Hypothesis;
import com.jug.util.componenttree.AdvancedComponent;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.ValuePair;

import java.awt.*;
import java.awt.geom.GeneralPath;

public class DivisionAssignmentView extends AssignmentView {
    private final int ASSIGNMENT_DISPLAY_OFFSET;

    public DivisionAssignmentView(final DivisionAssignment da, int width, int ASSIGNMENT_DISPLAY_OFFSET) {
        this.abstractAssignment = da;
        this.width = width;
        this.ASSIGNMENT_DISPLAY_OFFSET = ASSIGNMENT_DISPLAY_OFFSET;
        setupPolygon();
    }

    @Override
    Color GetDefaultColor() {
        return new Color(250 / 256f, 150 / 256f, 40 / 256f, 1.0f);
    }

    @Override
    Color GetPrunedColor() {
        return new Color(250 / 256f, 150 / 256f, 40 / 256f, 0.2f);
    }

    void setupPolygon() {
        DivisionAssignment da = (DivisionAssignment) abstractAssignment;
        final Hypothesis<AdvancedComponent<FloatType>> leftHyp = da.getSourceHypothesis();
        final Hypothesis<AdvancedComponent<FloatType>> rightHypUpper = da.getUpperDesinationHypothesis();
        final Hypothesis<AdvancedComponent<FloatType>> rightHypLower = da.getLowerDesinationHypothesis();

        final ValuePair<Integer, Integer> limitsLeft = leftHyp.getLocation();
        final ValuePair<Integer, Integer> limitsRightUpper = rightHypUpper.getLocation();
        final ValuePair<Integer, Integer> limitsRightLower = rightHypLower.getLocation();

        final int x1 = 0;
        final int y1 = limitsLeft.getA() + ASSIGNMENT_DISPLAY_OFFSET;
        final int x2 = 0;
        final int y2 = limitsLeft.getB() + ASSIGNMENT_DISPLAY_OFFSET;
        final int y3 = limitsRightLower.getB() + ASSIGNMENT_DISPLAY_OFFSET;
        final int y4 = limitsRightLower.getA() + ASSIGNMENT_DISPLAY_OFFSET;
        final int x5 = this.width / 3;
        final int y5 =
                ASSIGNMENT_DISPLAY_OFFSET + (2 * (limitsLeft.getA() + limitsLeft.getB()) / 2 + (limitsRightUpper.getB() + limitsRightLower.getA()) / 2) / 3;
        final int y6 = limitsRightUpper.getB() + ASSIGNMENT_DISPLAY_OFFSET;
        final int y7 = limitsRightUpper.getA() + ASSIGNMENT_DISPLAY_OFFSET;

        polygon = new GeneralPath();
        polygon.moveTo(x1, y1);
        polygon.lineTo(x2, y2);
        polygon.lineTo(this.width, y3);
        polygon.lineTo(this.width, y4);
        polygon.lineTo(x5, y5);
        polygon.lineTo(this.width, y6);
        polygon.lineTo(this.width, y7);
        polygon.closePath();
    }
}
