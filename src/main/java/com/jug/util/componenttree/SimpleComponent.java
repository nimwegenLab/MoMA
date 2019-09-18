package com.jug.util.componenttree;

import com.jug.util.filteredcomponents.FilteredComponentTree;
import net.imglib2.Localizable;
import net.imglib2.algorithm.componenttree.Component;
import net.imglib2.algorithm.componenttree.pixellist.PixelList;
import net.imglib2.type.Type;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public final class SimpleComponent <T extends Type<T>>
        implements
        Component<T, SimpleComponent<T>> {

    /**
     * child nodes in the {@link FilteredComponentTree}.
     */
    private ArrayList<SimpleComponent< T >> children  = new ArrayList<>();

    /**
     * parent node in the {@link FilteredComponentTree}.
     */
    private SimpleComponent< T > parent;

    /**
     * Pixels in the component.
     */
    private final PixelList pixelList;

    /**
     * Maximum threshold value of the connected component.
     */
    private final T value;

    /**
     * Constructor for fully connected component-node (with parent or children).
     */
    public SimpleComponent(PixelList pixelList, T value) {
        this.pixelList = pixelList;
        this.value = value;
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

    private Boolean childrenWereReversed = false;

    @Override
    public List<SimpleComponent<T>> getChildren() {
        if(!childrenWereReversed){
            Collections.reverse(children);
            childrenWereReversed = true;
        }

        return children;
    }

    void addChild(SimpleComponent< T > child){
        this.children.add(child);
    }

    @Override
    public Iterator<Localizable> iterator() {
        return pixelList.iterator();
    }
}
