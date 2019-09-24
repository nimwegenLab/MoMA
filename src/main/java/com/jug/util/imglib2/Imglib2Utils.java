package com.jug.util.imglib2;

import net.imglib2.Cursor;
import net.imglib2.IterableInterval;
import net.imglib2.type.Type;

public class Imglib2Utils {
    public static <T extends Type<T>> void setImageToValue(IterableInterval<T> image, T value){
        Cursor<T> cursor = image.cursor();
        while(cursor.hasNext()){
            cursor.next();
            cursor.get().set(value.copy());
        }
    }
}
