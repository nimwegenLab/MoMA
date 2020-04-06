package com.jug.gui.assignmentview;

import java.awt.*;

public abstract class AssignmentView {
    public void draw(final Graphics2D g2) {};

    public abstract boolean isHovered(int mousePosX, int mousePosY);

    public abstract void addAsGroundUntruth();

    public abstract void addAsGroundTruth();
}
