package com.jug.lp;

import com.jug.Growthlane;
import com.jug.config.ITrackingConfiguration;
import com.jug.datahandling.IImageProvider;
import com.jug.gui.DialogManagerMock;
import com.jug.gui.IDialogManager;
import com.jug.util.componenttree.*;
import com.jug.util.imglib2.Imglib2Utils;
import gurobi.GRBException;
import net.imagej.ImageJ;
import net.imagej.ops.OpService;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.Views;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.*;

public class GrowthlaneTrackingIlpTest {
    public static void main(String... args) throws IOException, GRBException {
        new GrowthlaneTrackingIlpTest().testMappingAssignmentGeneration();
    }

    @Test
    public void testMappingAssignmentGeneration() throws IOException, GRBException {
        String imageFile = new File("").getAbsolutePath() + "/src/test/resources/00_probability_maps/cropped__20200922_M9glc_VNG1040-hi2_AB_1_MMStack_Pos0_GL30__probability_map_frame_126_duplicated_frame__20210812.tif";
//        String imageFile = "/home/micha/Documents/01_work/git/MoMA/test_datasets/001_bugfixing/20210812__issue_with_generating_plausible_assignments/cropped__20200922_M9glc_VNG1040-hi2_AB_1_MMStack_Pos0_GL30__frame_126__6_repeats_of_same_frame__20210812.tif";
        assertTrue(new File(imageFile).exists());

        ImageJ ij = new ImageJ();
        Img currentImageStack = (Img) ij.io().open(imageFile);
        assertNotNull(currentImageStack);
        int frameIndex = 0;
        RandomAccessibleInterval<FloatType> currentImage = Views.hyperSlice(currentImageStack, 2, frameIndex);

        assertEquals(3, currentImageStack.numDimensions());
        assertEquals(2, currentImage.numDimensions());

        IImageProvider imageProviderMock = new ImageProviderMock(currentImageStack);


        ComponentTreeGenerator componentTreeGenerator = getComponentTreeGenerator(ij);

        SimpleComponentTree<FloatType, AdvancedComponent<FloatType>> sourceTree = (SimpleComponentTree<FloatType, AdvancedComponent<FloatType>>) componentTreeGenerator.buildIntensityTree(imageProviderMock, frameIndex, 1.0f);
        SimpleComponentTree<FloatType, AdvancedComponent<FloatType>> targetTree = (SimpleComponentTree<FloatType, AdvancedComponent<FloatType>>) componentTreeGenerator.buildIntensityTree(imageProviderMock, frameIndex, 1.0f);

        IDialogManager dialogManagerMock = new DialogManagerMock();
        Growthlane gl = new Growthlane(imageProviderMock, dialogManagerMock);
        GRBModelAdapterMock mockGrbModel = new GRBModelAdapterMock();
        GrowthlaneTrackingILP ilp = new GrowthlaneTrackingILP(gl, mockGrbModel, imageProviderMock, new AssignmentPlausibilityTester(new TrackingConfigMock()), new TrackingConfigMock(), "mockVersionString");
        int t = 0; /* has to be zero, to avoid entering the IF-statement inside addMappingAssignment: if (t > 0) { .... }*/
        ilp.addMappingAssignments(t, sourceTree, targetTree);
    }

    @NotNull
    private ComponentTreeGenerator getComponentTreeGenerator(ImageJ ij) {
        OpService ops = ij.op();
        Imglib2Utils imglib2Utils = new Imglib2Utils(ops);
        ComponentProperties componentProperties = new ComponentProperties(ops, imglib2Utils);
        RecursiveComponentWatershedder recursiveComponentWatershedder = new RecursiveComponentWatershedder(ij.op());
        WatershedMaskGenerator watershedMaskGenerator = new WatershedMaskGenerator(0, 0.5f);
        ComponentTreeGenerator componentTreeGenerator = new ComponentTreeGenerator(recursiveComponentWatershedder, componentProperties, watershedMaskGenerator, imglib2Utils);
        return componentTreeGenerator;
    }

    class TrackingConfigMock implements ITrackingConfiguration {

        @Override
        public boolean filterAssignmentsByMaximalGrowthRate() {
            return false;
        }

        @Override
        public double getMaximumGrowthRate() {
            return 0;
        }
    }
}
