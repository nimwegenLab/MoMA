package com.jug.gui.assignmentview;

import com.jug.lp.AbstractAssignment;

import java.awt.*;
import java.awt.geom.GeneralPath;

public abstract class AssignmentView {
    AbstractAssignment abstractAssignment;
    int width;
    GeneralPath polygon;
    boolean isHidden;

    public void hide() {
        isHidden = true;
    }

    public void unhide() {
        isHidden = false;
    }

    public boolean isGroundTruth() {
        return abstractAssignment.isGroundTruth();
    }

    public boolean IsGroundUntruth() {
        return abstractAssignment.isGroundUntruth();
    }

    public void addAsGroundTruth() {
        abstractAssignment.setGroundTruth(!abstractAssignment.isGroundTruth());
        abstractAssignment.reoptimize();
    }

    public void addAsGroundUntruth() {
        abstractAssignment.setGroundUntruth(!abstractAssignment.isGroundUntruth());
        abstractAssignment.reoptimize();
    }

    public String getCostTooltipString() {
        return String.format("c=%.4f", abstractAssignment.getCost());
    }

    public boolean isHovered(int mousePosX, int mousePosY) {
        if(isHidden) return false; /* deactivate interaction, when hidden */
        return polygon.contains(mousePosX, mousePosY);
    }

    public abstract void draw(final Graphics2D g2);

    protected void draw(final Graphics2D g2, Color defaultColor, Color isPrunedColor) {
        if (isHidden) return; /* do not draw this assignment */

        g2.setStroke(new BasicStroke(1));
        if (!abstractAssignment.isPruned()) {
            g2.setPaint(isPrunedColor);
            if (abstractAssignment.isGroundTruth() || abstractAssignment.isGroundUntruth()) {
                g2.setPaint(g2.getColor().brighter().brighter());
            }
            g2.fill(polygon);
        }
        if (abstractAssignment.isGroundTruth()) {
            g2.setPaint(Color.GREEN.darker());
            g2.setStroke(new BasicStroke(3));
        } else if (abstractAssignment.isGroundUntruth()) {
            g2.setPaint(Color.RED.darker());
            g2.setStroke(new BasicStroke(3));
        } else {
            g2.setPaint(defaultColor);
        }
        g2.draw(polygon);
    }
}
