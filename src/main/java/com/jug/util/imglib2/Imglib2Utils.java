package com.jug.util.imglib2;

import com.jug.util.componenttree.SimpleComponent;
import net.imagej.ops.Ops;
import net.imglib2.*;
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

    public static double getTotalIntensity(final Interval interval, final RandomAccessible<FloatType> img){
        IterableInterval< FloatType > region = Views.interval( img, interval );
        Cursor<FloatType> cursor = region.cursor();
        double totalIntensity = 0;
        while(cursor.hasNext()){
            float value = cursor.get().get();
            totalIntensity += value;
            cursor.fwd();
        }
        return totalIntensity;
    }
}
