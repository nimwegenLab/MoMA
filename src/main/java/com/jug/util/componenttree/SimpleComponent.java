package com.jug.util.componenttree;

import com.jug.util.filteredcomponents.FilteredComponentTree;
import net.imglib2.Localizable;
import net.imglib2.algorithm.componenttree.Component;
import net.imglib2.algorithm.componenttree.pixellist.PixelList;
import net.imglib2.type.Type;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public final class SimpleComponent <T extends Type<T>>
        implements
        Component<T, SimpleComponent<T>> {

    /**
     * child nodes in the {@link FilteredComponentTree}.
     */
    private ArrayList<SimpleComponent< T >> children;

    /**
     * parent node in the {@link FilteredComponentTree}.
     */
    private SimpleComponent< T > parent;

    /**
     * Pixels in the component.
     */
    public final PixelList pixelList;

    /**
     * Maximum threshold value of the connected component.
     */
    private final T value;

    /**
     * Constructor for fully connected component-node (with parent or children).
     */
    public SimpleComponent(PixelList pixelList, T value, SimpleComponent<T> parent, ArrayList<SimpleComponent< T >> children) {
        this.pixelList = pixelList;
        this.value = value;
        this.parent = parent;
        this.children = children;
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

    void setChildren(ArrayList<SimpleComponent< T >> children){
        this.children = children;
    }

    @Override
    public Iterator<Localizable> iterator() {
        return pixelList.iterator();
    }
}
