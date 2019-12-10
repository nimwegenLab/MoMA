package com.jug.util.imglib2;

import net.imglib2.Cursor;
import net.imglib2.IterableInterval;
import net.imglib2.roi.Regions;
import net.imglib2.roi.labeling.LabelRegion;
import net.imglib2.type.Type;
import net.imglib2.type.numeric.real.FloatType;

public class Imglib2Utils {
    public static <T extends Type<T>> void setImageToValue(IterableInterval<T> image, T value){
        Cursor<T> cursor = image.cursor();
        while(cursor.hasNext()){
            cursor.next();
            cursor.get().set(value.copy());
        }
    }


    public static double sumIntensities(Cursor<FloatType> cursor){
        double totalIntensity = 0;
        while(cursor.hasNext()){
            totalIntensity += cursor.get().get();
            cursor.fwd();
        }
        return totalIntensity;
    }
}
