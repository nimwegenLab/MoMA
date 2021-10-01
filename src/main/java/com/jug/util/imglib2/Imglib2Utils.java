package com.jug.util.imglib2;

import ij.IJ;
import ij.ImagePlus;
import net.imagej.ops.OpService;
import net.imglib2.*;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.loops.LoopBuilder;
import net.imglib2.type.Type;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;
import org.jetbrains.annotations.NotNull;

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
        return ops.stats().sum(region).getRealDouble();
    }

    public double getIntensityMean(final Interval interval, final RandomAccessible<FloatType> img) {
        IterableInterval<FloatType> region = Views.interval(img, interval);
        return ops.stats().mean(region).getRealDouble();
    }

    public double getIntensityStDev(final Interval interval, final RandomAccessible<FloatType> img) {
        IterableInterval<FloatType> region = Views.interval(img, interval);
        return ops.stats().stdDev(region).getRealDouble();
    }

    public double getIntensityCoeffVariation(final Interval interval, final RandomAccessible<FloatType> img) {
        IterableInterval<FloatType> region = Views.interval(img, interval);
        double std = ops.stats().stdDev(region).getRealDouble();
        double mean = ops.stats().mean(region).getRealDouble();
        return std / mean;
    }

    public <T extends Type<T>> void copyImage(RandomAccessibleInterval<T> sourceImage,
                                              RandomAccessibleInterval<T> targetImage) {
        LoopBuilder.setImages(sourceImage, targetImage).forEachPixel((src, dest) -> dest.set(src));
    }

    public <T extends NumericType<T>> void saveImage(RandomAccessibleInterval<T> imgResult, String path) {
        ImagePlus tmp_image = ImageJFunctions.wrap(imgResult, "imgResults");
        IJ.saveAsTiff(tmp_image, path);
    }

    @NotNull
    public <T extends Type<T>> IntervalView<T> getImageSlice(RandomAccessibleInterval<T> imgResult, int channel, int zSlice, int time) {
        IntervalView<T> slice = Views.hyperSlice(imgResult, 4, time);
        slice = Views.hyperSlice(slice, 3, zSlice);
        slice = Views.hyperSlice(slice, 2, channel);
        return slice;
    }
}
