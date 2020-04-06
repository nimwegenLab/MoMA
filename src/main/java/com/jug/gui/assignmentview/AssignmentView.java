package com.jug.gui.assignmentview;

import com.jug.lp.AbstractAssignment;

import java.awt.*;
import java.awt.geom.GeneralPath;

public abstract class AssignmentView {
    AbstractAssignment abstractAssignment;
    int width;
    GeneralPath polygon;
    private boolean isHidden;

    public void hide() {
        isHidden = true;
    }

    public void show() {
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

    public void draw(final Graphics2D g2) {
        if (isHidden) return; /* do not draw this assignment */
//        // Interaction with mouse:
//        if ( !isDragging && isHovered(mousePosX, mousePosY) ) {
//                final float cost = ma.getCost();
//                if ( ma.isGroundTruth() ) {
//                    g2.setPaint( Color.GREEN.darker() );
//                } else if ( ma.isGroundUntruth() ) {
//                    g2.setPaint( Color.RED.darker() );
//                } else {
//                    g2.setPaint( new Color( 25 / 256f, 65 / 256f, 165 / 256f, 1.0f ).darker().darker() );
//                }
//                g2.drawString(
//                        String.format( "c=%.4f", cost ),
//                        DISPLAY_COSTS_ABSOLUTE_X,
//                        mousePosY + OFFSET_DISPLAY_COSTS - this.currentCostLine * LINEHEIGHT_DISPLAY_COSTS );
//                this.currentCostLine++;
//        }

        // draw it!
        g2.setStroke(new BasicStroke(1));
        if (!abstractAssignment.isPruned()) {
            g2.setPaint(new Color(25 / 256f, 65 / 256f, 165 / 256f, 0.2f));
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
            g2.setPaint(new Color(25 / 256f, 65 / 256f, 165 / 256f, 1.0f));
        }
        g2.draw(polygon);
    }

    public boolean isHovered(int mousePosX, int mousePosY) {
        return polygon.contains(mousePosX, mousePosY);
    }
}
