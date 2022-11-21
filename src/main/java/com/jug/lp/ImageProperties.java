package com.jug.lp;

import com.jug.config.IConfiguration;
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
    private IConfiguration configuration;
    private IImageProvider imageProvider;

    public ImageProperties(IImageProvider imageProvider, Imglib2Utils imglib2Utils, IConfiguration configuration) {
        this.imglib2Utils = imglib2Utils;
        this.configuration = configuration;
        this.imageProvider = imageProvider;
    }

    public double getBackgroundIntensityStdAtFrame(int channelNumber, int frame) {
        Img<FloatType> img = imageProvider.getChannelImg(channelNumber);
        FinalInterval leftBackgroundRoi = getLeftBackgroundRoiAtFrame(img, frame);
        FinalInterval rightBackgroundRoi = getRightBackgroundRoiAtFrame(img, frame);
        long leftNumberOfPixels = Views.interval(img, leftBackgroundRoi).size();
        long rightNumberOfPixels = Views.interval(img, rightBackgroundRoi).size();
        if (leftNumberOfPixels != rightNumberOfPixels) {
            throw new AssertionError(String.format("The areas for calculating the background intensities to the left/right of the growthlane are not equal (left=%d; right=%d)", leftNumberOfPixels, rightNumberOfPixels));
        }
        double leftStd = imglib2Utils.getIntensityStDev(leftBackgroundRoi, img);
        double rightStd = imglib2Utils.getIntensityStDev(rightBackgroundRoi, img);

        return (leftNumberOfPixels * leftStd + rightNumberOfPixels * rightStd) / getBackgroundRoiSizeAtFrame(channelNumber, frame);
    }

    public double getBackgroundIntensityMeanAtFrame(int channelNumber, int frame) {
        return getBackgroundIntensityTotalAtFrame(channelNumber, frame) / getBackgroundRoiSizeAtFrame(channelNumber, frame);
    }

    public double getBackgroundIntensityTotalAtFrame(int channelNumber, int frame) {
        Img<FloatType> img = imageProvider.getChannelImg(channelNumber);
        FinalInterval leftBackgroundRoi = getLeftBackgroundRoiAtFrame(img, frame);
        FinalInterval rightBackgroundRoi = getRightBackgroundRoiAtFrame(img, frame);
        double leftIntensity = imglib2Utils.getIntensityTotal(leftBackgroundRoi, img);
        double rightIntensity = imglib2Utils.getIntensityTotal(rightBackgroundRoi, img);
        return leftIntensity + rightIntensity;
    }

    public long getBackgroundRoiSizeAtFrame(int channelNumber, long frame) {
        Img<FloatType> img = imageProvider.getChannelImg(channelNumber);
        long leftNumberOfPixels = Views.interval(img, getLeftBackgroundRoiAtFrame(img, frame)).size();
        long rightNumberOfPixels = Views.interval(img, getRightBackgroundRoiAtFrame(img, frame)).size();
        return leftNumberOfPixels + rightNumberOfPixels;
    }

    public double getBackgroundIntensityStd(int channelNumber) {
        Img<FloatType> img = imageProvider.getChannelImg(channelNumber);
        FinalInterval leftBackgroundRoi = getLeftBackgroundRoi(img);
        FinalInterval rightBackgroundRoi = getRightBackgroundRoi(img);
        long leftNumberOfPixels = Views.interval(img, leftBackgroundRoi).size();
        long rightNumberOfPixels = Views.interval(img, rightBackgroundRoi).size();
        if (leftNumberOfPixels != rightNumberOfPixels) {
            throw new AssertionError(String.format("The areas for calculating the background intensities to the left/right of the growthlane are not equal (left=%d; right=%d)", leftNumberOfPixels, rightNumberOfPixels));
        }
        double leftStd = imglib2Utils.getIntensityStDev(leftBackgroundRoi, img);
        double rightStd = imglib2Utils.getIntensityStDev(rightBackgroundRoi, img);

        return (leftNumberOfPixels * leftStd + rightNumberOfPixels * rightStd) / getBackgroundRoiSize();
    }

    public double getBackgroundIntensityTotal(int channelNumber) {
        Img<FloatType> img = imageProvider.getChannelImg(channelNumber);
        FinalInterval leftBackgroundRoi = getLeftBackgroundRoi(img);
        FinalInterval rightBackgroundRoi = getRightBackgroundRoi(img);
        double leftIntensity = imglib2Utils.getIntensityTotal(leftBackgroundRoi, img);
        double rightIntensity = imglib2Utils.getIntensityTotal(rightBackgroundRoi, img);
        return leftIntensity + rightIntensity;
    }

    public double getBackgroundIntensityMean(int channelNumber) {
        return getBackgroundIntensityTotal(channelNumber) / getBackgroundRoiSize();
    }

    public long getBackgroundRoiSize() {
        Img<FloatType> img = imageProvider.getChannelImg(0);
        FinalInterval leftBackgroundRoi = getLeftBackgroundRoi(img);
        FinalInterval rightBackgroundRoi = getRightBackgroundRoi(img);
        long leftNumberOfPixels = Views.interval(img, leftBackgroundRoi).size();
        long rightNumberOfPixels = Views.interval(img, rightBackgroundRoi).size();
        if (leftNumberOfPixels != rightNumberOfPixels) {
            throw new AssertionError(String.format("The areas for calculating the background intensities to the left/right of the growthlane are not equal (left=%d; right=%d)", leftNumberOfPixels, rightNumberOfPixels));
        }
        return leftNumberOfPixels + rightNumberOfPixels;
    }

    @NotNull
    private FinalInterval getLeftBackgroundRoiAtFrame(RandomAccessibleInterval<FloatType> img, long frame) {
        long xStart = 0;
        long xEnd = configuration.getBackgroundRoiWidth() - 1;
        long yStart = 0;
        long yEnd = img.max(1);
        long tStart = frame;
        long tEnd = frame;

        FinalInterval tmp = new FinalInterval(
                new long[]{xStart, yStart, tStart},
                new long[]{xEnd, yEnd, tEnd}
        );
        return tmp;
    }

    @NotNull
    private FinalInterval getRightBackgroundRoiAtFrame(RandomAccessibleInterval<FloatType> img, long frame) {
        long xStart = img.max(0) - (configuration.getBackgroundRoiWidth() - 1);
        long xEnd = img.max(0);
        long yStart = 0;
        long yEnd = img.max(1);
        long tStart = frame;
        long tEnd = frame;

        FinalInterval tmp = new FinalInterval(
                new long[]{xStart, yStart, tStart},
                new long[]{xEnd, yEnd, tEnd}
        );
        return tmp;
    }
    @NotNull
    private FinalInterval getLeftBackgroundRoi(RandomAccessibleInterval<FloatType> img) {
        long xStart = 0;
        long xEnd = configuration.getBackgroundRoiWidth() - 1;
        long yStart = 0;
        long yEnd = img.max(1);
        long tStart = 0;
        long tEnd = img.max(2);

        FinalInterval tmp = new FinalInterval(
                new long[]{xStart, yStart, tStart},
                new long[]{xEnd, yEnd, tEnd}
        );
        return tmp;
    }

    @NotNull
    private FinalInterval getRightBackgroundRoi(RandomAccessibleInterval<FloatType> img) {
        long xStart = img.max(0) - (configuration.getBackgroundRoiWidth() - 1);
        long xEnd = img.max(0);
        long yStart = 0;
        long yEnd = img.max(1);
        long tStart = 0;
        long tEnd = img.max(2);

        FinalInterval tmp = new FinalInterval(
                new long[]{xStart, yStart, tStart},
                new long[]{xEnd, yEnd, tEnd}
        );
        return tmp;
    }
}
