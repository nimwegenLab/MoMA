package com.jug.util.componenttree;

import com.jug.util.filteredcomponents.FilteredComponentTree;
import net.imglib2.Localizable;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.componenttree.Component;
import net.imglib2.algorithm.componenttree.pixellist.PixelList;
import net.imglib2.img.Img;
import net.imglib2.type.Type;
import net.imglib2.type.numeric.integer.LongType;
import net.imglib2.view.IntervalView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public final class SimpleComponent<T extends Type<T>>
        implements
        Component<T, SimpleComponent<T>> {

    /**
     * Pixels in the component.
     */
    private final PixelList pixelList;
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
     * child nodes in the {@link FilteredComponentTree}.
     */
    private ArrayList<SimpleComponent<T>> children = new ArrayList<>();
    /**
     * parent node in the {@link FilteredComponentTree}.
     */
    private SimpleComponent<T> parent;
    /**
     * We need to reverse the list of children, so that the division assignment has the children in the correct order
     * (meaning top and bottom children are assign correctly). This is a hackish work around to solve an issue that I
     * do not fully understand yet and should probably be fixed here: ComponentTreeUtils.getRightNeighbor(...)
     */
    private Boolean childrenWereReversed = false;

    /**
     * Constructor for fully connected component-node (with parent or children).
     */
    public SimpleComponent(PixelList pixelList, T value, RandomAccessibleInterval<T> sourceImage) {
        this.pixelList = pixelList;
        this.value = value;
        this.sourceImage = sourceImage;
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
        if (!childrenWereReversed) {
            Collections.reverse(children);
            childrenWereReversed = true;
        }
        return children;
    }

    void addChild(SimpleComponent<T> child) {
        this.children.add(child);
    }

    @Override
    public Iterator<Localizable> iterator() {
        return pixelList.iterator();
    }
}
