package com.jug.util.imglib2;

import net.imglib2.*;
import net.imglib2.algorithm.componenttree.Component;
import net.imglib2.type.Type;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.Views;
import org.apache.commons.lang.NotImplementedException;

import java.util.List;

public class Imglib2Utils {
    public static <T extends Type<T>> void setImageToValue(IterableInterval<T> image, T value){
        Cursor<T> cursor = image.cursor();
        while(cursor.hasNext()){
            cursor.next();
            cursor.get().set(value.copy());
        }
    }

    public static double getTotalIntensity(final Interval interval, final RandomAccessible<FloatType> img){
        IterableInterval< FloatType > region = Views.interval( img, interval );
        Cursor<FloatType> cursor = region.cursor();
        double totalIntensity = 0;
        while(cursor.hasNext()){
            cursor.fwd();
            float value = cursor.get().get();
            totalIntensity += value;
        }
        return totalIntensity;
    }

    public static List<Localizable> calculateCenterOfMass(final Component<FloatType, ?> component){

        throw new NotImplementedException();
    }
}
