package com.jug.util.math;

import net.imglib2.RealLocalizable;
import org.apache.commons.math3.exception.MathArithmeticException;
import org.apache.commons.math3.exception.util.LocalizedFormats;

public class Vector2D implements RealLocalizable {
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

    public void plusMutate(Vector2D operand) {
        x = x + operand.getX();
        y = y + operand.getY();
    }

    public Vector2D minus(Vector2D operand) {
        return new Vector2D(x - operand.getX(), y - operand.getY());
    }

    public double getLength() {
        return Math.sqrt(Math.pow(getX(), 2.0) + Math.pow(getY(), 2.0));
    }

    public double getPolarAngle() {
        return Math.atan2(getY(), getX());
    }

    public Vector2D multiply(double factor) {
        return new Vector2D(factor * getX(), factor * getY());
    }

    public void multiplyMutate(double factor) {
        x = factor * x; y = factor * y;
    }

    public Boolean equals(Vector2D operand, double tolerance) {
        if (operand == null) {
            return false;
        }
        return (Math.abs(getX() - operand.getX()) < tolerance) && (Math.abs(getY() - operand.getY()) < tolerance);
    }

    public double angleWith(Vector2D operand) {
        return Math.acos(this.scalarProduct(operand) / (this.getLength() * operand.getLength()));
    }

    @Override
    public String toString() {
        return "Vector2D{" +
                "x=" + x +
                ", y=" + y +
                '}';
    }

    @Override // RealLocalizable
    public void localize(float[] position) {
        if (position.length == 2) {
            position[0] = (float) getX();
            position[1] = (float) getY();
        }
        throw new RuntimeException("requested dimension out of bounds for Vector2D");
//        RealLocalizable.super.localize(position);
    }

    @Override // RealLocalizable
    public void localize(double[] position) {
        if (position.length == 2) {
            position[0] = getX();
            position[1] = getY();
        }
        throw new RuntimeException("requested dimension out of bounds for Vector2D");
//        RealLocalizable.super.localize(position);
    }

    @Override // RealLocalizable
    public float getFloatPosition(int d) {
//        return RealLocalizable.super.getFloatPosition(d);
        return (float) getDoublePosition(d);
    }

    @Override // RealLocalizable
    public double getDoublePosition(int i) {
        if (i == 0) return this.getX();
        if (i == 1) return this.getY();
        throw new RuntimeException("requested dimension out of bounds for Vector2D");
    }

    @Override // RealLocalizable
    public int numDimensions() {
        return 2;
    }

    public Vector2D normalize() throws MathArithmeticException { /* taken from: org.apache.commons.math3.geometry.euclidean.twod.Vector2D */
        double s = this.getNorm();
        if (s == 0.0D) {
            throw new MathArithmeticException(LocalizedFormats.CANNOT_NORMALIZE_A_ZERO_NORM_VECTOR, new Object[0]);
        } else {
            return this.multiply(1.0D / s);
        }
    }

    public double getNorm() {
        return Math.sqrt(this.x * this.x + this.y * this.y);
    }

    public double dotProduct(Vector2D vector) {
        return this.getX() * vector.getX() + this.getY() * vector.getY();
    }
}
