package com.jug.util.componenttree;

import net.imglib2.Localizable;
import net.imglib2.Point;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.componenttree.Component;
import net.imglib2.algorithm.componenttree.mser.Mser;
import net.imglib2.algorithm.componenttree.pixellist.PixelList;
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
    private final List<Localizable> pixelList = new ArrayList<Localizable>();
    private final Mser wrappedComponent;
    private final RandomAccessibleInterval<T> sourceImage;

    public RandomAccessibleInterval<T> getSourceImage() {
        return sourceImage;
    }

    //    public Img<LongType> getLinkedList()
//    {
//        return linkedList;
//    }
//    public Img< LongType > getLinkedList() {
//        return pixelList.locationsAccess;
//    }

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

    /**
     * Constructor for fully connected component-node (with parent or children).
     */
    public <C extends Component<T,C>> SimpleComponent(C wrappedComponent, T value, RandomAccessibleInterval<T> sourceImage) {
        for(Localizable val : wrappedComponent)
        {
            pixelList.add(new Point(val));
        }
        this.wrappedComponent = (Mser)wrappedComponent;
        this.value = value;
        this.sourceImage = sourceImage;
    }

    @Override
    public long size() {
        return wrappedComponent.size();
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

    public double[] firstMomentPixelCoordinates(){
        return wrappedComponent.mean();
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
