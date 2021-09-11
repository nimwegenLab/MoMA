package com.jug.lp.costs;

import com.jug.util.componenttree.ComponentInterface;
import net.imglib2.algorithm.componenttree.Component;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.List;

public class ComponentMock implements ComponentInterface {
    private final double[] firstMomentPixelCoordinates;

    public ComponentMock(double[] firstMomentPixelCoordinates) {
        this.firstMomentPixelCoordinates = firstMomentPixelCoordinates;
    }

    public double[] firstMomentPixelCoordinates() {
        return firstMomentPixelCoordinates;
    }

    @Override
    public long size() {
        return 0;
    }

    @Override
    public Object value() {
        return null;
    }

    @Override
    public Component getParent() {
        return null;
    }

    @Override
    public List getChildren() {
        return null;
    }

    @NotNull
    @Override
    public Iterator iterator() {
        return null;
    }
}
