package com.jug.util.imglib2;

import com.jug.datahandling.IImageProvider;
import com.jug.util.TestUtils;
import net.imglib2.FinalInterval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.type.numeric.real.FloatType;
import org.jetbrains.annotations.NotNull;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Path;

public class Imglib2UtilsTest {

    private final TestUtils testUtils;
    private Imglib2Utils imglib2Utils;

    public Imglib2UtilsTest() {
        testUtils = new TestUtils();
        imglib2Utils = testUtils.getImglib2Utils();
    }

    @Test
    public void getIntensityMean__when_called_across_image_stack__returns_expected_value() throws IOException {
        double expectedMeanIntensity = 9.18003135902371;
        Path testDataFolder = testUtils.getAbsolutTestFilePath("src/test/resources/00_probability_maps/20211026_VNG1040_AB6min_2h_1_MMStack_Pos7_GL12/frames_445-460__20211026_VNG1040_AB6min_2h_1_MMStack_Pos7_GL12");
        IImageProvider imageProvider = testUtils.getImageProviderFromDataFolder(testDataFolder);
        
        Img<FloatType> img = imageProvider.getRawChannelImgs().get(1);
        FinalInterval leftBackgroundRoi = getLeftBackgroundRoi(img);
        double actualMeanIntensity = imglib2Utils.getIntensityMean(leftBackgroundRoi, img);
        FinalInterval rightBackgroundRoi = getRightBackgroundRoi(img);
        double actualMeanIntensityRight = imglib2Utils.getIntensityMean(rightBackgroundRoi, img);

        Assert.assertEquals(expectedMeanIntensity, actualMeanIntensity, 1e-6);
    }

    @Test
    public void getIntensityStDev__when_called_across_image_stack__returns_expected_value() throws IOException {
        double expectedStdIntensity = 23.675810591584096;
        Path testDataFolder = testUtils.getAbsolutTestFilePath("src/test/resources/00_probability_maps/20211026_VNG1040_AB6min_2h_1_MMStack_Pos7_GL12/frames_445-460__20211026_VNG1040_AB6min_2h_1_MMStack_Pos7_GL12");
        IImageProvider imageProvider = testUtils.getImageProviderFromDataFolder(testDataFolder);

        Img<FloatType> img = imageProvider.getRawChannelImgs().get(1);
        FinalInterval leftBackgroundRoi = getLeftBackgroundRoi(img);
        double actualStdIntensity = imglib2Utils.getIntensityStDev(leftBackgroundRoi, img);
        FinalInterval rightBackgroundRoi = getRightBackgroundRoi(img);
        double actualStdIntensityRight = imglib2Utils.getIntensityStDev(rightBackgroundRoi, img);

        Assert.assertEquals(expectedStdIntensity, actualStdIntensity, 1e-6);
    }

    long background_roi_width = 5; /* ROI width in pixels*/

    @NotNull
    private FinalInterval getLeftBackgroundRoi(RandomAccessibleInterval<FloatType> img) {
        long xStart = 0;
        long xEnd = img.dimension(0) - 1;
        long yStart = 0;
        long yEnd = background_roi_width - 1;
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
        long yEnd = background_roi_width - 1;
        long tStart = 0;
        long tEnd = img.dimension(2);

        FinalInterval tmp = new FinalInterval(
                new long[]{xStart, yStart, tStart},
                new long[]{xEnd, yEnd, tEnd}
        );
//        FinalInterval tmp = new FinalInterval(
//                new long[]{img.max(0) - (background_roi_width - 1), vert_start},
//                new long[]{img.max(0), vert_stop}
//        );
        return tmp;
    }
}
