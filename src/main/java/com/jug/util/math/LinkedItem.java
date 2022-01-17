package com.jug.util.math;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * This was adapted from CircularNode
 * @author Jean Ollion
 */
// MM: This is a linked node that is used to define a circular contour; ie. a contour, where each contour-point has exactly two neighbors
public class LinkedItem<T> implements Comparable<LinkedItem> {
    LinkedItem<T> prev, next;
    T element;

    public LinkedItem(T element) {
        this.element = element;
    }

    public T getElement() {
        return element;
    }

    public void setElement(T element) {
        this.element = element;
    }

    public void setPrev(LinkedItem<T> prev) {
        this.prev = prev;
        prev.next = this;
    }

    public void setNext(LinkedItem<T> next) {
        this.next = next;
        next.prev = this;
    }

    public LinkedItem<T> setPrev(T element) {
        this.prev = new LinkedItem(element);
        this.prev.next = this;
        return this.prev;
    }

    public LinkedItem<T> insertPrev(T element) {
        LinkedItem<T> old = prev;
        LinkedItem<T> newN = setPrev(element);
        newN.setPrev(old);
        return newN;
    }

    public LinkedItem<T> setNext(T element) {
        this.next = new LinkedItem(element);
        this.next.prev = this;
        return this.next;
    }

    public LinkedItem<T> insertNext(T element) {
        LinkedItem<T> old = next;
        LinkedItem<T> newN = setNext(element);
        newN.setNext(old);
        return newN;
    }

    public LinkedItem<T> next() {
        return next;
    }

    public LinkedItem<T> prev() {
        return prev;
    }

    public LinkedItem<T> getFollowing(boolean next) {
        return next ? this.next : prev;
    }

    public LinkedItem<T> getInFollowing(T element, boolean next) {
        return next ? getInNext(element) : getInPrev(element);
    }

    public LinkedItem<T> getInNext(T element) {
        if (element == null) return null;
        if (element.equals(this.element)) return this;
        LinkedItem<T> search = this.next;
        while (!search.equals(this)) {
            if (element.equals(search.element)) return search;
            search = search.next;
        }
        return null;
    }

    /**
     * Idem as getFollowing but searching in other direction
     *
     * @param element
     * @return
     */
    public LinkedItem<T> getInPrev(T element) {
        if (element == null) return null;
        if (element.equals(this.element)) return this;
        LinkedItem<T> search = this.prev;
        while (!search.equals(this)) {
            if (element.equals(search.element)) return search;
            search = search.prev;
        }
        return null;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.element);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final LinkedItem<?> other = (LinkedItem<?>) obj;
        if (!Objects.equals(this.element, other.element)) {
            return false;
        }
        return true;
    }

    @Override
    public int compareTo(LinkedItem o) {
        if (this.equals(o)) return 0;
        if (this.prev.equals(o)) return 1;
        if (this.next.equals(o)) return -1;
        LinkedItem p = o.prev;
        LinkedItem n = o.next;
        while (!p.equals(n) || this.equals(p)) { // second condition if searched value is at the exaxt opposite of the contour
            if (this.equals(p)) return -1;
            if (this.equals(n)) return 1;
            p = p.prev;
            n = n.next;
        }
        throw new IllegalArgumentException("Circular Node not from the same list");
    }

    @Override
    public String toString() {
        return element.toString();
    }

    // HELPER METHOD
    public static <T> void apply(LinkedItem<T> circContour, Consumer<LinkedItem<T>> func, boolean next) {
        func.accept(circContour);
        if (next) {
            LinkedItem<T> n = circContour.next;
            while (circContour != n) {
                func.accept(n);
                n = n.next;
                if (n == null) return;
            }
        } else {
            LinkedItem<T> p = circContour.prev;
            while (circContour != p) {
                func.accept(p);
                p = p.prev;
                if (p == null) return;
            }
        }
    }

//    public static <U, V> LinkedItem<V> map(LinkedItem<U> source, Function<U, V> mapper) {
//        LinkedItem<V> firstDest = new LinkedItem(mapper.apply(source.element));
//        LinkedItem<U> currentSource = source.next();
//        LinkedItem<V> currentDest = firstDest;
//        while (!currentSource.equals(source)) {
//            currentDest = currentDest.setNext(mapper.apply(currentSource.getElement()));
//            currentSource = currentSource.next();
//        }
//        currentDest.setNext(firstDest); // closes circular contour
//        return firstDest;
//    }

    public static <T> LinkedItem<T> toCircularLinkedList(List<T> orderedList) {
        LinkedItem<T> first = new LinkedItem(orderedList.get(0));
        LinkedItem<T> previous = first;
        for (int i = 0; i < orderedList.size(); ++i) previous = previous.setNext(orderedList.get(i));
        previous.setNext(first); // closes contour
        return first;
    }

    public static <T> LinkedItem<T> toLinkedList(List<T> orderedList) {
        LinkedItem<T> first = new LinkedItem(orderedList.get(0));
        LinkedItem<T> previous = first;
        for (int i = 0; i < orderedList.size(); ++i) previous = previous.setNext(orderedList.get(i));
        return first;
    }
}
