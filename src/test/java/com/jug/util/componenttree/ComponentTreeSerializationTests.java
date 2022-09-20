package com.jug.util.componenttree;

import com.jug.util.TestUtils;
import com.moma.auxiliary.Plotting;
import ij.ImagePlus;
import ij.gui.TextRoi;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.numeric.real.FloatType;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ComponentTreeSerializationTests {

    private final TestUtils testUtils;

    public ComponentTreeSerializationTests() {
        testUtils = new TestUtils();
    }

    public static void main(String... args) throws IOException, InterruptedException {
        new ComponentTreeSerializationTests().testComponentSerialization();
    }
    public void testComponentSerialization() throws IOException {
        String imageFile = new File("").getAbsolutePath() + "/src/test/resources/00_probability_maps/20201119_VNG1040_AB2h_2h_1__Pos5_GL17/cropped__20201119_VNG1040_AB2h_2h_1_MMStack_Pos5_GL17__model_9e5727e4ed18802f4ab04c7494ef8992d798f4d64d5fd75e285b9a3d83b13ac9.tif";
        int frameIndex = 0;
        AdvancedComponentForest<FloatType, AdvancedComponent<FloatType>> tree = (AdvancedComponentForest<FloatType, AdvancedComponent<FloatType>>) testUtils.getComponentTreeFromProbabilityImage(imageFile, frameIndex, 1.0f);
        Plotting.drawComponentTree2(tree, new ArrayList<>(), tree.rootsSorted().get(0).getSourceImage());
    }
}
