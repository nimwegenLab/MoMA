package com.jug.util.componenttree;

import com.jug.util.FloatTypeImgLoader;
import ij.IJ;
import ij.ImagePlus;
import net.imagej.ImageJ;
import net.imglib2.img.Img;
import net.imglib2.type.numeric.real.FloatType;
import org.junit.Test;

import java.io.FileNotFoundException;
import java.util.ArrayList;

public class UnetProcessorTest {

    public static void main(String[] args) throws FileNotFoundException {
        new UnetProcessorTest().runNetwork();
    }

    @Test
    public void runNetwork() throws FileNotFoundException {
        ImageJ ij = new ImageJ();
        String basePath = "/home/micha/Documents/01_work/git/MoMA/src/test/resources/ImageFormatTest/";
        String path = basePath + "new_10frames_20190424_hi2_hi3_med2_rplN_4_MMStack_Pos0_GL3_shape_106x531_original.tif"; // wide and high enough; UNet input-layer shape is: 32x512
//        String path = basePath + "new_10frames_20190424_hi2_hi3_med2_rplN_4_MMStack_Pos0_GL3_shape_24x444.tif"; // too thin and too low; UNet input-layer shape is: 32x512
//        String path = basePath + "new_10frames_20190424_hi2_hi3_med2_rplN_4_MMStack_Pos0_GL3_shape_24x531.tif"; // too thin, but high enough; UNet input-layer shape is: 32x512
//        String path = basePath + "new_10frames_20190424_hi2_hi3_med2_rplN_4_MMStack_Pos0_GL3_shape_106x444.tif"; // wide enough, but too low; UNet input-layer shape is: 32x512
        ImagePlus imp = IJ.openImage(path);
        int minTime = 1;
        int maxTime = imp.getNFrames();
        int minChannelIdx = 1;
        int maxChannelIdx = imp.getNChannels();

        ArrayList<Img<FloatType>> rawChannelImgs = FloatTypeImgLoader.loadTiffsFromFileOrFolder(path, minTime, maxTime, minChannelIdx, maxChannelIdx);

        Img<FloatType> imgTemp = rawChannelImgs.get( 0 );
        Img<FloatType> imgProbs = new UnetProcessor().process(imgTemp);
        System.out.println("bla");
    }
}