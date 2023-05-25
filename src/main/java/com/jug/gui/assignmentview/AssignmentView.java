package com.jug.gui.assignmentview;

import com.jug.logging.LoggingHelper;
import com.jug.lp.AbstractAssignment;
import gurobi.GRBException;

import java.awt.*;
import java.awt.geom.GeneralPath;
import java.text.DecimalFormat;

public abstract class AssignmentView {
    AbstractAssignment abstractAssignment;
    int width;
    GeneralPath polygon;
    boolean isHidden;
    private boolean isSelected;

    abstract Color GetPrunedColor();
    abstract Color GetDefaultColor();

    public void hide() {
        LoggingHelper.logUiAction(this, "AssignmentView.hide()");
        isHidden = true;
    }

    public void unhide() {
        LoggingHelper.logUiAction(this, "AssignmentView.unhide()");
        isHidden = false;
    }

    public boolean isGroundTruth() {
        return abstractAssignment.isGroundTruth();
    }

    public boolean IsGroundUntruth() {
        return abstractAssignment.isGroundUntruth();
    }

    public boolean isChosen(){
        try {
            return abstractAssignment.isChosen();
        } catch (GRBException err) {
            return false;
        }
    }

    public void setIsSelected(boolean isSelected){
        this.isSelected = isSelected;
    }

    public void toggleGroundTruth() {
        LoggingHelper.logUiAction("AbstractAssignment.setGroundTruth(" + !abstractAssignment.isGroundTruth() + ")", abstractAssignment);
        abstractAssignment.setGroundTruth(!abstractAssignment.isGroundTruth());
        abstractAssignment.reoptimize();
    }

    public void toggleGroundUntruth() {
        LoggingHelper.logUiAction("AbstractAssignment.setGroundUntruth(" + !abstractAssignment.isGroundUntruth() + ")", abstractAssignment);
        abstractAssignment.setGroundUntruth(!abstractAssignment.isGroundUntruth());
        abstractAssignment.reoptimize();
    }

    public String getCostTooltipString() {
        if (abstractAssignment.ilpIsReady()) {
            return new DecimalFormat(".####").format(abstractAssignment.getCost());
        } else {
            return "NA";
        }
    }

    public String getAssignmentIdString() {
        return abstractAssignment.getStringId();
    }

    public double getCost() {
        if (abstractAssignment.ilpIsReady()) {
            return abstractAssignment.getCost();
        } else {
            return Integer.MIN_VALUE; /* return place-holder value, if the ILP is not ready. TODO-MM-20220919: This is a hack: We should deactivate all relevant GUI elements during Gurobi optimization, so that the model can not even be queried through the GUI */
        }
    }

    public boolean isHovered(int mousePosX, int mousePosY) {
        if(isHidden) return false; /* deactivate interaction, when hidden */
        return polygon.contains(mousePosX, mousePosY);
    }

    public void draw(final Graphics2D g2) {
        if (isHidden) return; /* do not draw this assignment */

        g2.setStroke(new BasicStroke(1));
        if (!abstractAssignment.isPruned()) {
            g2.setPaint(GetPrunedColor());
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
        } else if (this.isSelected) {
            g2.setPaint(Color.BLACK.darker());
            g2.setStroke(new BasicStroke(3));
        } else {
            g2.setPaint(GetDefaultColor());
        }
        g2.draw(polygon);
    }
}
