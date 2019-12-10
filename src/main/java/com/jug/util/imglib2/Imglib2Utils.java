package com.jug.util.imglib2;

import com.jug.util.componenttree.SimpleComponent;
import net.imglib2.Cursor;
import net.imglib2.Interval;
import net.imglib2.IterableInterval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.roi.Regions;
import net.imglib2.roi.labeling.LabelRegion;
import net.imglib2.type.Type;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.Views;

public class Imglib2Utils {
    public static <T extends Type<T>> void setImageToValue(IterableInterval<T> image, T value){
        Cursor<T> cursor = image.cursor();
        while(cursor.hasNext()){
            cursor.next();
            cursor.get().set(value.copy());
        }
    }

    public static double getTotalIntensity(final Interval interval, final RandomAccessibleInterval<FloatType> img){
        IterableInterval< FloatType > region = Views.interval( img, interval );
        Cursor<FloatType> cursor = region.cursor();
        double totalIntensity = 0;
        while(cursor.hasNext()){
            totalIntensity += cursor.get().get();
            cursor.fwd();
        }
        return totalIntensity;
    }
}
