package com.jug.util.math;

import java.util.List;

public class LinkedItem<O extends Object> {
    private final O item;
    LinkedItem<O> previous, next;

    public LinkedItem(O item) {
        this.item = item;
    }

    public LinkedItem<O> setPrevious(O previous) {
        this.previous = new LinkedItem<>(previous);
        this.previous.next = this;
        return this.previous;
    }

    public O getPrevious(){
        return previous.item;
    }

    public O getNext() {
        return next.item;
    }

    public LinkedItem<O> setNext(O next) {
        this.next = new LinkedItem<>(next);
        this.next.previous = this;
        return this.next;
    }

    public static <O> LinkedItem<O> toCircularContour(List<O> orderedList) {
        LinkedItem<O> first = new LinkedItem(orderedList.get(0));
        LinkedItem<O> previous=first;
        for (int i = 0; i<orderedList.size();++i) previous = previous.setNext(orderedList.get(i));
        previous.setNext(first); // closes contour
        return first;
    }
}
