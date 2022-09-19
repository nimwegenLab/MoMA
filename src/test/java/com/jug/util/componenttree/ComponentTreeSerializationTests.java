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
        new ComponentTreeSerializationTests().testPrintRankOfSegment();
    }
    public void testPrintRankOfSegment() throws IOException {
        String imageFile = new File("").getAbsolutePath() + "/src/test/resources/00_probability_maps/cropped__20200922_M9glc_VNG1040-hi2_AB_1_MMStack_Pos0_GL30__probability_map_frame_126_duplicated_frame__20210812.tif";
        int frameIndex = 0;

        AdvancedComponentForest<FloatType, AdvancedComponent<FloatType>> tree = (AdvancedComponentForest<FloatType, AdvancedComponent<FloatType>>) testUtils.getComponentTreeFromProbabilityImage(imageFile, frameIndex, 1.0f);

        List<AdvancedComponent<FloatType>> roots = tree.rootsSorted();

        for (AdvancedComponent<FloatType> root : roots) {
            ArrayList<AdvancedComponent<FloatType>> componentsToDraw = new ArrayList<>();
            componentsToDraw.add(root);
            ImagePlus imp = ImageJFunctions.show(Plotting.createImageWithComponents(componentsToDraw, new ArrayList<>(), root.getSourceImage()));
            int rank = root.getRankRelativeToComponentsClosestToRoot();
            TextRoi text = new TextRoi(0, 0, String.format("rank: %d", rank));
            imp.setOverlay(text, Color.white, 0, Color.black);
        }

        Plotting.drawComponentTree2(tree, new ArrayList<>(), tree.rootsSorted().get(0).getSourceImage());
    }
}
