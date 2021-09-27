package com.jug.util.imglib2;

import net.imagej.ops.OpService;
import net.imglib2.*;
import net.imglib2.algorithm.componenttree.Component;
import net.imglib2.type.Type;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.Views;
import org.apache.commons.lang.NotImplementedException;

import java.util.List;

public class Imglib2Utils {
    private OpService ops;

    public Imglib2Utils(OpService ops) {
        this.ops = ops;
    }

    public static <T extends Type<T>> void setImageToValue(IterableInterval<T> image, T value){
        Cursor<T> cursor = image.cursor();
        while(cursor.hasNext()){
            cursor.next();
            cursor.get().set(value.copy());
        }
    }

    public double getTotalIntensity(final Interval interval, final RandomAccessible<FloatType> img){
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

    public double getAverageIntensity(final Interval interval, final RandomAccessible<FloatType> img){
        IterableInterval< FloatType > region = Views.interval( img, interval );
        DoubleType output = new DoubleType();
        ops.stats().mean(output, region);
        double result = output.getRealDouble();
        return result;
    }
}
