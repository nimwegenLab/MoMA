package com.jug.util.componenttree;

import com.jug.util.imglib2.Imglib2Utils;
import net.imagej.ImageJ;
import net.imglib2.IterableInterval;
import net.imglib2.img.Img;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.IntervalView;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

public class WatershedMaskGeneratorTests {
    ImageJ ij = new ImageJ();

    public static void main(String... args) throws IOException, InterruptedException {
        WatershedMaskGeneratorTests tests = new WatershedMaskGeneratorTests();
        tests.test();
    }

    @Test
    public void test() throws IOException {
        float threshold = .5f;
        String relativeImagePath = "/src/test/resources/test_data/ComponentTreeGeneratorTestsData/lis_20201119__Pos6_GL6/frame_133__cropped__20201119_VNG1040_AB2h_2h_1_MMStack_Pos6_GL6__model_9e5727e4ed18802f4ab04c7494ef8992d798f4d64d5fd75e285b9a3d83b13ac9.tif";
        Img<FloatType> image = readImage(getFullPath(relativeImagePath));
        WatershedMaskGenerator componentMerger = getComponentMerger();
        Img<BitType> mergedMask = componentMerger.generateMask(image, threshold);
        Imglib2Utils utils = new Imglib2Utils(ij.op());
//        Img<FloatType> maskedImage = utils.maskImage(image, mergedMask, new FloatType(.0f));

        ij.ui().showUI();
//        ij.ui().show("image", image);
        ij.ui().show("mergedMask", mergedMask);
//        ij.ui().show("maskedImage", maskedImage);
//        ij.ui().show("labeling image", componentMerger.labelingImage);
    }

    private Img readImage(String imagePath) throws IOException {
        return (Img) ij.io().open(imagePath);
    }

    private String getFullPath(String relativePath) {
        return new File("").getAbsolutePath() + relativePath;
    }

    private WatershedMaskGenerator getComponentMerger() {
        return new WatershedMaskGenerator();
    }
}
