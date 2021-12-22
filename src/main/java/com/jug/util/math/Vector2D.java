package com.jug.util.math;

import net.imglib2.RealLocalizable;

public class Vector2D {
    private double x;
    private double y;

    public Vector2D(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public Vector2D(RealLocalizable localizable) {
        x = localizable.getDoublePosition(0);
        y = localizable.getDoublePosition(1);
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double scalarProduct(Vector2D operand) {
        return x * operand.getX() + y * operand.getY();
    }

    public Vector2D plus(Vector2D operand) {
        return new Vector2D(x + operand.getX(), y + operand.getY());
    }

    public Vector2D minus(Vector2D operand) {
        return new Vector2D(x - operand.getX(), y - operand.getY());
    }

    public double length(){
        return Math.sqrt(Math.pow(getX(), 2.0) + Math.pow(getY(), 2.0));
    }

    public Vector2D multiply(double factor) {
        return new Vector2D(factor * getX(), factor * getY());
    }

    public Boolean equals(Vector2D operand, double tolerance) {
        if (operand == null) {
            return false;
        }
        return (Math.abs(getX() - operand.getX()) < tolerance) && (Math.abs(getY() - operand.getY()) < tolerance);
    }
}
