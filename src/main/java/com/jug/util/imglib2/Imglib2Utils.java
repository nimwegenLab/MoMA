package com.jug.util.imglib2;

import ij.IJ;
import ij.ImagePlus;
import net.imagej.ops.OpService;
import net.imglib2.*;
import net.imglib2.img.Img;
import net.imglib2.img.ImgView;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.loops.LoopBuilder;
import net.imglib2.roi.geom.real.Polygon2D;
import net.imglib2.roi.labeling.LabelRegion;
import net.imglib2.roi.labeling.LabelRegionCursor;
import net.imglib2.type.NativeType;
import net.imglib2.type.Type;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;
import org.apache.commons.lang.NotImplementedException;
import org.jetbrains.annotations.NotNull;
import net.imagej.ops.Ops;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imagej.ops.convert.normalizeScale.NormalizeScaleRealTypes;


public class Imglib2Utils {
    private final OpService ops;

    public Imglib2Utils(OpService ops) {
        this.ops = ops;
    }

    public static <T extends Type<T>> void setImageToValue(IterableInterval<T> image, T value) {
        Cursor<T> cursor = image.cursor();
        while (cursor.hasNext()) {
            cursor.next();
            cursor.get().set(value.copy());
        }
    }

    public double getTotalIntensity(final Interval interval, final RandomAccessible<FloatType> img) {
        IterableInterval<FloatType> region = Views.interval(img, interval);
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
    public <T extends Type<T>> IntervalView<T> getImageSlice(RandomAccessibleInterval<T> rai, int channel, int zSlice, int time) {
        IntervalView<T> slice = Views.hyperSlice(rai, 4, time);
        slice = Views.hyperSlice(slice, 3, zSlice);
        slice = Views.hyperSlice(slice, 2, channel);
        return slice;
    }

    public <T extends Type<T>> Img<T> maskImage(RandomAccessibleInterval<T> source, RandomAccessibleInterval<BitType> mask, T maskedValue) {
//        long[] dimensions = new long[source.numDimensions()];
//        source.dimensions(dimensions);
//        T type = Util.getTypeFromInterval(source);
//        Img<T> target = new ArrayImgFactory(type).create(dimensions);
//        target.factory().imgFactory(source);

        Img<T> sourceImg = ImgView.wrap(source);
        Img<T> target = sourceImg.copy();
//        Mask res = Masks.allMask(3);
//        Masks.masktoMask(mask)
//        ArrayImgFactory<IntType> imgFactory = new ArrayImgFactory<>(new IntType());
        LoopBuilder.setImages(target, mask).forEachPixel((dest, msk) -> {
            if (!msk.get()) {
                dest.set(maskedValue);
            }
        });
        return target;
//        throw new NotImplementedException();
    }

    /* ATTEMPT 1 TO SAVE IMG TO DISK */
//        ImagePlus tmp_image = ImageJFunctions.wrap(Views.permute(imgResult, 2, 3 ), "imgResults");
////        ImagePlus tmp_image = ImageJFunctions.wrap(imgResult, "imgResults");
//        IJ.saveAsTiff(tmp_image, outputFile.getAbsolutePath());

//        /* ATTEMPT 2 TO SAVE IMG TO DISK */
//        ImgSaver saver = new ImgSaver(context);
//        FileLocation imgName = new FileLocation(outputFile);
//        try {
//            saver.saveImg(imgName, imgResult);
//        }
//        catch (Exception exc) {
//            exc.printStackTrace();
//        }

    /* ATTEMPT 3 */
//        IOPlugin<Img<IntType>> saver = new DefaultIOService().getSaver(imgResult, outputFile.getAbsolutePath());
//        try {
//            saver.save(imgResult, outputFile.getAbsolutePath());
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

//        /* ATTEMPT 4 */
//        ImagePlus tmp_image = ImageJFunctions.wrap(imgResult, "imgResults");
//        IJ.saveAsTiff(tmp_image, outputFile.getAbsolutePath());

    /**
     * Convenience method for creating a labeling image with same dimensions as the source image.
     *
     * @param sourceImage
     * @return
     */
    public <T extends NativeType<T>> Img<T> createImageWithSameDimension(RandomAccessibleInterval sourceImage,
                                                                          T type) {
        long[] dims = new long[sourceImage.numDimensions()];
        sourceImage.dimensions(dims);
        Img<T> img = new ArrayImgFactory(type).create(dims);
        return img;
    }

//    public Img<UnsignedByteType> convert(Img input){
////        Img converted = ops.convert().float32(input);
//        Img<BitType> converted = ops.convert().bit(input);
//
////        NormalizeScaleRealTypes converter = new NormalizeScaleRealTypes();
////        converter.setEnvironment(ops);
////        converter.initialize();
////        converter.checkInput(input);
////
////        Img<UnsignedByteType> out = ops.create().img(input, new UnsignedByteType());
////        ops.run(Ops.Convert.ImageType.class, out, input, converter);
////        return out;
//    }
}
