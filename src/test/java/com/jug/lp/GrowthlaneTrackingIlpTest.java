package com.jug.lp;

import com.jug.GrowthLine;
import com.jug.util.componenttree.ComponentTreeGenerator;
import com.jug.util.componenttree.SimpleComponent;
import com.jug.util.componenttree.SimpleComponentTree;
import com.moma.auxiliary.Plotting;
import gurobi.GRBException;
import ij.ImagePlus;
import ij.gui.TextRoi;
import net.imagej.ImageJ;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.numeric.real.FloatType;
import org.junit.Test;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class GrowthlaneTrackingIlpTest {
    public static void main(String... args) throws IOException, GRBException {
        new GrowthlaneTrackingIlpTest().testMappingAssignmentGeneration();
    }

    @Test
    public void testMappingAssignmentGeneration() throws IOException, GRBException {
        String imageFile = new File("").getAbsolutePath() + "/src/test/resources/00_probability_maps/cropped__20200922_M9glc_VNG1040-hi2_AB_1_MMStack_Pos0_GL30__probability_map_frame_127__20210812.tif";
        assertTrue(new File(imageFile).exists());

        ImageJ ij = new ImageJ();
        Img input = (Img) ij.io().open(imageFile);
        assertNotNull(input);
        RandomAccessibleInterval<FloatType> currentImage = input;
        assertEquals(2, currentImage.numDimensions());

        SimpleComponentTree<FloatType, SimpleComponent<FloatType>> sourceTree = (SimpleComponentTree<FloatType, SimpleComponent<FloatType>>) new ComponentTreeGenerator().buildIntensityTree(currentImage);
        SimpleComponentTree<FloatType, SimpleComponent<FloatType>> targetTree = (SimpleComponentTree<FloatType, SimpleComponent<FloatType>>) new ComponentTreeGenerator().buildIntensityTree(currentImage);

        GrowthLine gl = new GrowthLine();
        GRBModelAdapterMock mockGrbModel = new GRBModelAdapterMock();
        GrowthLineTrackingILP ilp = new GrowthLineTrackingILP(gl, mockGrbModel);
        int t = 0; /* has to be zero, to avoid entering the IF-statement inside addMappingAssignment: if (t > 0) { .... }*/
        ilp.addMappingAssignments(t, sourceTree, targetTree);
    }
}
