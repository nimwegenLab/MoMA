package com.jug.lp;

import com.jug.datahandling.ArgumentValidation;
import com.jug.datahandling.IImageProvider;
import com.jug.util.imglib2.Imglib2Utils;
import net.imglib2.FinalInterval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.Views;
import org.jetbrains.annotations.NotNull;

public class ImageProperties {
    private final Imglib2Utils imglib2Utils;

    public ImageProperties(Imglib2Utils imglib2Utils) {
        this.imglib2Utils = imglib2Utils;
    }


    public double getBackgroundIntensityStd(IImageProvider imageProvider, int channelNumber) {
        Img<FloatType> img = imageProvider.getRawChannelImgs().get(channelNumber);
        FinalInterval leftBackgroundRoi = getLeftBackgroundRoi(img);
        FinalInterval rightBackgroundRoi = getRightBackgroundRoi(img);
        long leftNumberOfPixels = Views.interval(img, leftBackgroundRoi).size();
        long rightNumberOfPixels = Views.interval(img, rightBackgroundRoi).size();
        if (leftNumberOfPixels != rightNumberOfPixels) {
            throw new AssertionError(String.format("The areas for calculating the background intensities to the left/right of the growthlane are not equal (left=%d; right=%d)", leftNumberOfPixels, rightNumberOfPixels));
        }
        double leftStd = imglib2Utils.getIntensityStDev(leftBackgroundRoi, img);
        double rightStd = imglib2Utils.getIntensityStDev(rightBackgroundRoi, img);

        return (leftNumberOfPixels * leftStd + rightNumberOfPixels * rightStd) / (leftNumberOfPixels + rightNumberOfPixels);
    }

    public double getBackgroundIntensityMean(IImageProvider imageProvider, int channelNumber) {
        Img<FloatType> img = imageProvider.getRawChannelImgs().get(channelNumber);
        FinalInterval leftBackgroundRoi = getLeftBackgroundRoi(img);
        FinalInterval rightBackgroundRoi = getRightBackgroundRoi(img);
        long leftNumberOfPixels = Views.interval(img, leftBackgroundRoi).size();
        long rightNumberOfPixels = Views.interval(img, rightBackgroundRoi).size();
        if (leftNumberOfPixels != rightNumberOfPixels) {
            throw new AssertionError(String.format("The areas for calculating the background intensities to the left/right of the growthlane are not equal (left=%d; right=%d)", leftNumberOfPixels, rightNumberOfPixels));
        }
        double leftIntensity = imglib2Utils.getTotalIntensity(leftBackgroundRoi, img);
        double rightIntensity = imglib2Utils.getTotalIntensity(rightBackgroundRoi, img);

        return (leftIntensity + rightIntensity) / (leftNumberOfPixels + rightNumberOfPixels);
    }

    long background_roi_width = 5; /* ROI width in pixels*/

    @NotNull
    private FinalInterval getLeftBackgroundRoi(RandomAccessibleInterval<FloatType> img) {
        long xStart = 0;
        long xEnd = background_roi_width - 1;
        long yStart = 0;
        long yEnd = img.max(1);
        long tStart = 0;
        long tEnd = img.dimension(2);

        FinalInterval tmp = new FinalInterval(
                new long[]{xStart, yStart, tStart},
                new long[]{xEnd, yEnd, tEnd}
        );
        return tmp;
    }

    @NotNull
    private FinalInterval getRightBackgroundRoi(RandomAccessibleInterval<FloatType> img) {
        long xStart = img.max(0) - (background_roi_width - 1);
        long xEnd = img.max(0);
        long yStart = 0;
        long yEnd = img.max(1);
        long tStart = 0;
        long tEnd = img.dimension(2);

        FinalInterval tmp = new FinalInterval(
                new long[]{xStart, yStart, tStart},
                new long[]{xEnd, yEnd, tEnd}
        );
        return tmp;
    }
}
