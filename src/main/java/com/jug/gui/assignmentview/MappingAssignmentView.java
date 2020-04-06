package com.jug.gui.assignmentview;

import com.jug.gui.MoMAGui;
import com.jug.lp.Hypothesis;
import com.jug.lp.MappingAssignment;
import net.imglib2.algorithm.componenttree.Component;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.ValuePair;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.GeneralPath;

public class MappingAssignmentView extends AssignmentView2 {
    private MappingAssignment ma;
    private MoMAGui gui;
    private int width;
    private int ASSIGNMENT_DISPLAY_OFFSET;
    private int DISPLAY_COSTS_ABSOLUTE_X;
    private int OFFSET_DISPLAY_COSTS;
    private int LINEHEIGHT_DISPLAY_COSTS;
    private GeneralPath polygon;
    private boolean isHidden;

    public MappingAssignmentView(final MappingAssignment ma, MoMAGui gui, int width, int ASSIGNMENT_DISPLAY_OFFSET, int DISPLAY_COSTS_ABSOLUTE_X, int OFFSET_DISPLAY_COSTS, int LINEHEIGHT_DISPLAY_COSTS) {
        this.ma = ma;
        this.gui = gui;
        this.width = width;
        this.ASSIGNMENT_DISPLAY_OFFSET = ASSIGNMENT_DISPLAY_OFFSET;
        this.DISPLAY_COSTS_ABSOLUTE_X = DISPLAY_COSTS_ABSOLUTE_X;
        this.OFFSET_DISPLAY_COSTS = OFFSET_DISPLAY_COSTS;
        this.LINEHEIGHT_DISPLAY_COSTS = LINEHEIGHT_DISPLAY_COSTS;
        setupPolygon();
    }

    void setupPolygon(){
        final Hypothesis<net.imglib2.algorithm.componenttree.Component<FloatType, ? >> leftHyp = ma.getSourceHypothesis();
        final Hypothesis<Component< FloatType, ? >> rightHyp = ma.getDestinationHypothesis();

        final ValuePair< Integer, Integer > limitsLeft = leftHyp.getLocation();
        final ValuePair< Integer, Integer > limitsRight = rightHyp.getLocation();

        final int x1 = 0;
        final int y1 = limitsLeft.getA() + ASSIGNMENT_DISPLAY_OFFSET;
        final int x2 = 0;
        final int y2 = limitsLeft.getB() + ASSIGNMENT_DISPLAY_OFFSET;
        final int y3 = limitsRight.getB() + ASSIGNMENT_DISPLAY_OFFSET;
        final int y4 = limitsRight.getA() + ASSIGNMENT_DISPLAY_OFFSET;

        polygon = new GeneralPath();
        polygon.moveTo( x1, y1 );
        polygon.lineTo( x2, y2 );
        polygon.lineTo(this.width, y3 );
        polygon.lineTo(this.width, y4 );
        polygon.closePath();
    }

    public void hide(){
        isHidden = true;
    }

    public void show(){
        isHidden = false;
    }

    public boolean isGroundTruth(){
        return ma.isGroundTruth();
    }

    public boolean IsGroundUntruth(){
        return ma.isGroundUntruth();
    }

    public void addAsGroundTruth(){
        ma.setGroundTruth( !ma.isGroundTruth() );
        ma.reoptimize();
        SwingUtilities.invokeLater(() -> gui.dataToDisplayChanged());
    }

    public void addAsGroundUntruth(){
        ma.setGroundUntruth( !ma.isGroundUntruth() );
        ma.reoptimize();
        SwingUtilities.invokeLater(() -> gui.dataToDisplayChanged());
    }

    public String getCostTooltipString(){
         return String.format( "c=%.4f", ma.getCost() );
    }

    @Override
    public void draw(final Graphics2D g2){
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
        g2.setStroke( new BasicStroke( 1 ) );
        if ( !ma.isPruned() ) {
            g2.setPaint( new Color( 25 / 256f, 65 / 256f, 165 / 256f, 0.2f ) );
            if ( ma.isGroundTruth() || ma.isGroundUntruth() ) {
                g2.setPaint( g2.getColor().brighter().brighter() );
            }
            g2.fill( polygon );
        }
        if ( ma.isGroundTruth() ) {
            g2.setPaint( Color.GREEN.darker() );
            g2.setStroke( new BasicStroke( 3 ) );
        } else if ( ma.isGroundUntruth() ) {
            g2.setPaint( Color.RED.darker() );
            g2.setStroke( new BasicStroke( 3 ) );
        } else {
            g2.setPaint( new Color( 25 / 256f, 65 / 256f, 165 / 256f, 1.0f ) );
        }
        g2.draw( polygon );
    }

    private boolean isHovered(int mousePosX, int mousePosY) {
        return polygon.contains( mousePosX, mousePosY );
    }
}
