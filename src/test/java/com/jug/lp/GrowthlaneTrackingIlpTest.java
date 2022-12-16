package com.jug.lp;

import com.jug.Growthlane;
import com.jug.GrowthlaneFrame;
import com.jug.config.CommandLineArgumentsParser;
import com.jug.config.ComponentForestGeneratorConfigurationMock;
import com.jug.config.IConfiguration;
import com.jug.config.ITrackingConfiguration;
import com.jug.datahandling.GlFileManager;
import com.jug.datahandling.IImageProvider;
import com.jug.gui.DialogManagerMock;
import com.jug.gui.IDialogManager;
import com.jug.gui.progress.DialogGurobiProgress;
import com.jug.gui.progress.IDialogGurobiProgress;
import com.jug.lp.costs.CostFactory;
import com.jug.lp.costs.ICostFactory;
import com.jug.mocks.ConfigMock;
import com.jug.util.PseudoDic;
import com.jug.util.TestUtils;
import com.jug.util.Timer;
import com.jug.util.componenttree.*;
import com.jug.util.imglib2.Imglib2Utils;
import gurobi.GRBCallback;
import gurobi.GRBException;
import net.imagej.ImageJ;
import net.imagej.ops.OpService;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.Views;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class GrowthlaneTrackingIlpTest {
    public static void main(String... args) throws IOException, GRBException {
//        new GrowthlaneTrackingIlpTest().testMappingAssignmentGeneration();
        new GrowthlaneTrackingIlpTest().reproduceAssertionError();
    }

    private final TestUtils testUtils;

    public GrowthlaneTrackingIlpTest() {
            testUtils = new TestUtils();
    }

    @Test
   public void reproduceAssertionError() throws IOException, GRBException {
        int frameIndexStart = 0;
        int frameIndexStop = 1;
        Path testDataFolder = testUtils.getAbsolutTestFilePath("src/test/resources/00_probability_maps/20220701_VNG1040_AB2h_4_MMStack_Pos0_Pos7_GL2/frames_172-173__probability_maps__20220701_VNG1040_AB2h_4_MMStack_Pos0_Pos7_GL2");
        List<AdvancedComponentForest<FloatType, AdvancedComponent<FloatType>>> componentTress = testUtils.getComponentForestListFromDataFolder(
                testDataFolder,
                frameIndexStart,
                frameIndexStop,
                1.5f,
                0.1f,
                0.5f,
                10,
                10);
        AdvancedComponentForest<FloatType, AdvancedComponent<FloatType>> sourceTree = componentTress.get(0);
        AdvancedComponentForest<FloatType, AdvancedComponent<FloatType>> targetTree = componentTress.get(1);
        
//        IDialogManager dialogManagerMock = new DialogManagerMock();
        GRBModelAdapterMock mockGrbModel = new GRBModelAdapterMock();

//        ConfigMock configMock = new ConfigMock();
        IConfiguration configMock = testUtils.getConfigMock(
                true,
                1,
                2,
                true,
                true,
                0.0,
                50,
                2.0,
                120);

//        GlFileManager glFileManagerMock = new GlFileManager();
//        Growthlane gl = new Growthlane(dialogManagerMock, configMock, glFileManagerMock, glFileManagerMock);

//        gl.getFrames().get(t).getComponentForest()
        Growthlane gl = mock(Growthlane.class);
        when(gl.numberOfFrames()).thenReturn(2);
        ArrayList<GrowthlaneFrame> listOfFrames = new ArrayList<>();
        listOfFrames.add(mock(GrowthlaneFrame.class));
        when(listOfFrames.get(0).getComponentForest()).thenReturn(sourceTree);
        listOfFrames.add(mock(GrowthlaneFrame.class));
        when(listOfFrames.get(1).getComponentForest()).thenReturn(targetTree);
        when(gl.getFrames()).thenReturn(listOfFrames);

        PseudoDic dic = testUtils.getPseudoDicMock(true, true);
        
        ITrackingConfiguration trackingConfiguration = mock(ITrackingConfiguration.class);
        when(trackingConfiguration.getMaximumGrowthRate()).thenReturn(1.5);
        AssignmentPlausibilityTester assignmentPlausibilityTester = new AssignmentPlausibilityTester(trackingConfiguration);
                
        IAssignmentFilter assignmentFilterMock = mock(IAssignmentFilter.class);

        GrowthlaneTrackingILP ilp = new GrowthlaneTrackingILP(gl,
                mockGrbModel,
                assignmentPlausibilityTester, configMock, "mockVersionString", new CostFactory(configMock),
                false,
                () -> new GurobiCallbackMock(),
                () -> new DialogGurobiProgressMock(),
                assignmentFilterMock,
                dic);
        ilp.buildILP();
//        int t = 0; /* has to be zero, to avoid entering the IF-statement inside addMappingAssignment: if (t > 0) { .... }*/
//        for(int t=0; t<=2; t++){
//            ilp.addMappingAssignments(t, sourceTree, targetTree);
//        }
//        ilp.addCrossingConstraints();
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

        ComponentForestGenerator componentForestGenerator = getComponentTreeGenerator(ij);

        AdvancedComponentForest<FloatType, AdvancedComponent<FloatType>> sourceTree = componentForestGenerator.buildComponentForest(imageProviderMock, frameIndex, 1.0f);
        AdvancedComponentForest<FloatType, AdvancedComponent<FloatType>> targetTree = componentForestGenerator.buildComponentForest(imageProviderMock, frameIndex, 1.0f);

        IDialogManager dialogManagerMock = new DialogManagerMock();
        GRBModelAdapterMock mockGrbModel = new GRBModelAdapterMock();
        ConfigMock configMock = new ConfigMock();
        GlFileManager glFileManagerMock = new GlFileManager();
        Growthlane gl = new Growthlane(dialogManagerMock, configMock, glFileManagerMock, glFileManagerMock);
        IAssignmentFilter assignmentFilterMock = mock(IAssignmentFilter.class);
        PseudoDic dic = mock(PseudoDic.class);
        GrowthlaneTrackingILP ilp = new GrowthlaneTrackingILP(gl,
                mockGrbModel,
                new AssignmentPlausibilityTester(new TrackingConfigMock()), configMock, "mockVersionString", new CostFactory(configMock),
                false,
                () -> new GurobiCallbackMock(),
                () -> new DialogGurobiProgressMock(),
                assignmentFilterMock,
                dic);
        int t = 0; /* has to be zero, to avoid entering the IF-statement inside addMappingAssignment: if (t > 0) { .... }*/
        ilp.addMappingAssignments(t, sourceTree, targetTree);
    }

    @NotNull
    private ComponentForestGenerator getComponentTreeGenerator(ImageJ ij) {
        OpService ops = ij.op();
        Imglib2Utils imglib2Utils = new Imglib2Utils(ops);
        ComponentProperties componentProperties = new ComponentProperties(ops, imglib2Utils, new CostFactoryMock(), mock(IConfiguration.class));
        RecursiveComponentWatershedder recursiveComponentWatershedder = new RecursiveComponentWatershedder(ij.op());
        WatershedMaskGenerator watershedMaskGenerator = new WatershedMaskGenerator(0, 0.5f);
        ComponentForestGeneratorConfigurationMock config = new ComponentForestGeneratorConfigurationMock(60, Integer.MIN_VALUE);
        ComponentForestGenerator componentForestGenerator = new ComponentForestGenerator(config, recursiveComponentWatershedder, componentProperties, watershedMaskGenerator, imglib2Utils);
        return componentForestGenerator;
    }

    class TrackingConfigMock implements ITrackingConfiguration {
        @Override
        public double getMaximumGrowthRate() {
            return 0;
        }
    }

    class CostFactoryMock implements ICostFactory {
        @Override
        public float getComponentCost(ComponentInterface component) {
            return 0;
        }
    }

    private class GurobiCallbackMock extends GurobiCallbackAbstract {
        @Override
        protected void callback() {
            /* does nothing */
        }

        @Override
        public double getLatestGap() {
            return 0;
        }
    }

    private class DialogGurobiProgressMock implements IDialogGurobiProgress {
        @Override
        public void setVisible(boolean isVisible) {

        }

        @Override
        public void notifyGurobiTermination() {

        }

        @Override
        public void pushStatus(String s) {

        }

        @Override
        public void dispose() {

        }

        @Override
        public void actionPerformed(ActionEvent e) {

        }
    }
}
