package com.jug.util.iterator;

import net.imglib2.Localizable;
import net.imglib2.util.ValuePair;

import java.util.Iterator;

public class IteratorUtils {
    public static ValuePair<Integer,Integer> getLimits(Iterator<Localizable> pixelPositionIterator, int dim){
        int min = Integer.MAX_VALUE;
        int max = Integer.MIN_VALUE;

        while(pixelPositionIterator.hasNext()){
            Localizable location = pixelPositionIterator.next();
            final int pos =  location.getIntPosition( dim );
            min = Math.min( min, pos );
            max = Math.max( max, pos );
        }
        return new ValuePair<>(min, max);
    }
}
