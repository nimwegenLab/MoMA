package com.jug.util.componenttree;

import net.imglib2.Localizable;
import net.imglib2.Point;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.componenttree.Component;
import net.imglib2.type.Type;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public final class SimpleComponent<T extends Type<T>>
        implements
        Component<T, SimpleComponent<T>> {

    /**
     * Pixels in the component.
     */
    private final List<Localizable> pixelList = new ArrayList<>();
    private final RandomAccessibleInterval<T> sourceImage;
    /**
     * Maximum threshold value of the connected component.
     */
    private final T value;
    /**
     * List of child nodes.
     */
    private ArrayList<SimpleComponent<T>> children = new ArrayList<>();
    /**
     * Parent node. Is null if this is a root component.
     */
    private SimpleComponent<T> parent;
    private double[] mean;
    private double[] sumPos;

    /**
     * Constructor for fully connected component-node (with parent or children).
     */
    public <C extends Component<T, C>> SimpleComponent(C wrappedComponent, T value, RandomAccessibleInterval<T> sourceImage) {
        for (Localizable val : wrappedComponent) {
            pixelList.add(new Point(val));
        }
        this.value = value;
        this.sourceImage = sourceImage;
    }

    public RandomAccessibleInterval<T> getSourceImage() {
        return sourceImage;
    }

    @Override
    public long size() {
        return pixelList.size();
    }

    @Override
    public T value() {
        return value;
    }

    @Override
    public SimpleComponent<T> getParent() {
        return parent;
    }

    void setParent(SimpleComponent<T> parent) {
        this.parent = parent;
    }

    @Override
    public List<SimpleComponent<T>> getChildren() {
        return children;
    }

    void addChild(SimpleComponent<T> child) {
        this.children.add(child);
    }

    @Override
    public Iterator<Localizable> iterator() {
        return pixelList.iterator();
    }

    public double[] firstMomentPixelCoordinates() {
        int n = pixelList.get(0).numDimensions();
        sumPos = new double[n];
        for (Localizable val : this) {
            for (int i = 0; i < n; ++i)
                sumPos[i] += val.getIntPosition(i);
        }

        mean = new double[n];
        for (int i = 0; i < n; ++i)
            mean[i] = sumPos[i] / size();
        return mean;
    }

    public int getNodeLevel() {
        int nodeLevel = 0;
        SimpleComponent<T> parent = this.getParent();
        while (parent != null) {
            nodeLevel++;
            parent = parent.getParent();
        }
        return nodeLevel;
    }
}
