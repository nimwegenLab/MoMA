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
        this.width = width - 2;
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
        final Hypothesis<AdvancedComponent<FloatType>> rightHypUpper = da.getUpperDestinationHypothesis();
        final Hypothesis<AdvancedComponent<FloatType>> rightHypLower = da.getLowerDestinationHypothesis();

        final ValuePair<Integer, Integer> limitsLeft = leftHyp.getLocation();
        final ValuePair<Integer, Integer> limitsRightUpper = rightHypUpper.getLocation();
        final ValuePair<Integer, Integer> limitsRightLower = rightHypLower.getLocation();

        Integer upperComponentSize = limitsRightUpper.getB() - limitsRightUpper.getA();
        Integer lowerComponentSize = limitsRightLower.getB() - limitsRightLower.getA();
        Integer sourceComponentSize = limitsLeft.getB() - limitsLeft.getA();

        float divisionRatio = (float) upperComponentSize / (float) (lowerComponentSize + upperComponentSize);
        float positionOffsetWithinSourceComponent = divisionRatio * sourceComponentSize;
        float divisionLocation = limitsLeft.getA() + Math.round(positionOffsetWithinSourceComponent);

        float centeringOffset = .5f;
        final float x1 = centeringOffset;
        final float y1 = limitsLeft.getA() + ASSIGNMENT_DISPLAY_OFFSET;
        final float x2 = centeringOffset;
        final float y2 = limitsLeft.getB() + ASSIGNMENT_DISPLAY_OFFSET;
        final float y3 = limitsRightLower.getB() + ASSIGNMENT_DISPLAY_OFFSET;
        final float y4 = limitsRightLower.getA() + ASSIGNMENT_DISPLAY_OFFSET;
        final float x5 = this.width / 4 + centeringOffset;
        final float y5 = divisionLocation + ASSIGNMENT_DISPLAY_OFFSET;

        final float x6 = this.width + centeringOffset;
        final float y6 = limitsRightUpper.getB() + ASSIGNMENT_DISPLAY_OFFSET;
        final float y7 = limitsRightUpper.getA() + ASSIGNMENT_DISPLAY_OFFSET;

        polygon = new GeneralPath();
        polygon.moveTo(x1, y1);
        polygon.lineTo(x2, y2);
        polygon.lineTo(x6, y3);
        polygon.lineTo(x6, y4);
        polygon.lineTo(x5, y5);
        polygon.lineTo(x6, y6);
        polygon.lineTo(x6, y7);
        polygon.closePath();
    }
}
