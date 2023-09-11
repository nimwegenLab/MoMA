package com.jug.util.componenttree;

import com.jug.config.IUnetProcessingConfiguration;
import com.jug.util.FloatTypeImgLoader;
import com.jug.util.TestUtils;
import ij.IJ;
import ij.ImagePlus;
import net.imagej.ImageJ;
import net.imagej.patcher.LegacyInjector;
import net.imglib2.img.Img;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.numeric.real.FloatType;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

public class UnetProcessorTest {

    static {
        LegacyInjector.preinit();
    }

    public static void main(String[] args) throws FileNotFoundException {
        new UnetProcessorTest().process_runs_on_datasets_shapes_below_unet_input_layer();
//        new UnetProcessorTest().process_test_data_with_short_growthlane();
    }

    @Test
    public void process_runs_on_datasets_shapes_below_unet_input_layer() throws FileNotFoundException {
        ImageJ ij = new ImageJ();
        ij.ui().showUI();
        Path pathToResourcesDirectory = TestUtils.getPathToResourcesDirectory();
//        String imageFile = new File("").getAbsolutePath();
//        String basePath = "/home/micha/Documents/01_work/git/MoMA/src/test/resources/ImageFormatTest/";
        Path path = Paths.get(pathToResourcesDirectory.toString(), "ImageFormatTest", "new_10frames_20190424_hi2_hi3_med2_rplN_4_MMStack_Pos0_GL3_shape_106x531_original.tif"); // wide and high enough; UNet input-layer shape is: 32x512
//        String path = basePath + "new_10frames_20190424_hi2_hi3_med2_rplN_4_MMStack_Pos0_GL3_shape_24x444.tif"; // too thin and too low; UNet input-layer shape is: 32x512
//        String path = basePath + "new_10frames_20190424_hi2_hi3_med2_rplN_4_MMStack_Pos0_GL3_shape_24x531.tif"; // too thin, but high enough; UNet input-layer shape is: 32x512
//        String path = basePath + "new_10frames_20190424_hi2_hi3_med2_rplN_4_MMStack_Pos0_GL3_shape_106x444.tif"; // wide enough, but too low; UNet input-layer shape is: 32x512
        Img<FloatType> inputImage = readImageData(path.toString());
        Img<FloatType> processedImage = new UnetProcessor(ij.context(), new UnetProcessingConfigurationMock()).process(inputImage);
        ImageJFunctions.show(inputImage, "Input image");
        ImageJFunctions.show(processedImage, "Processed image");
    }


    @Test
    public void process_test_data_with_short_growthlane() throws FileNotFoundException {
        ImageJ ij = new ImageJ();
        ij.ui().showUI();
        String path = "/home/micha/Documents/01_work/git/MoMA/test_datasets/20190515_hi1_med1_med2_rpmB_glu_gly_7_MMStack_Pos25_preproc_GL01/20190515_hi1_med1_med2_rpmB_glu_gly_7_MMStack_Pos25_preproc_GL01__frames_400-450.tif";
//        String path = "/home/micha/Documents/01_work/git/MoMA/test_datasets/20191105_glc_spcm_1_MMStack_Pos7_preproc_GL15/20191105_glc_spcm_1_MMStack_Pos7_preproc_GL15.tif";
        Img<FloatType> inputImage = readImageData(path);
        Img<FloatType> processedImage = new UnetProcessor(ij.context(), new UnetProcessingConfigurationMock()).process(inputImage);
        ImageJFunctions.show(inputImage, "Input image");
        ImageJFunctions.show(processedImage, "Processed image");
    }


    /**
     * Read image data used in the tests.
     *
     * @param path
     * @return First channel of the image stack that was read.
     * @throws FileNotFoundException
     */
    private Img<FloatType> readImageData(String path) throws FileNotFoundException {
        ImagePlus imp = IJ.openImage(path);
        int minTime = 1;
        int maxTime = imp.getNFrames();
        int minChannelIdx = 1;
        int maxChannelIdx = imp.getNChannels();
        ArrayList<Img<FloatType>> rawChannelImgs = FloatTypeImgLoader.loadTiffsFromFileOrFolder(path, minTime, maxTime, minChannelIdx, maxChannelIdx);
        return rawChannelImgs.get( 0 );
    }

    private class UnetProcessingConfigurationMock implements IUnetProcessingConfiguration {
        @Override
        public int getCellDetectionRoiOffsetTop() {
            return 0;
        }

        @Override
        public Path getModelFilePath() {
            return null;
//            /home/micha/Documents/01_work/git/MoMA/src/test/resources/UnetProcessorTest/model_20210715_5b27d7aa__tensorflow_model.epoch-0200-val_binary_crossentropy_adapter-0.0208.zip
        }
    }
}