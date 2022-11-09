package com.jug.util.componenttree;

import com.jug.datahandling.IImageProvider;
import com.jug.lp.ImageProviderMock;
import com.jug.util.TestUtils;
import com.jug.util.math.Vector2D;
import com.moma.auxiliary.Plotting;
import net.imagej.ImageJ;
import net.imglib2.Localizable;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.roi.MaskInterval;
import net.imglib2.roi.Masks;
import net.imglib2.type.logic.NativeBoolType;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.Views;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mock;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.junit.Assert.*;

public class AdvancedComponentTests {
    private final TestUtils testUtils;

    public static void main(String... args) throws IOException {
        AdvancedComponentTests tests = new AdvancedComponentTests();
//        tests.testGetParentWatershedLineValues();
//        tests.exploreGetParentWatershedLineCoordinates();
//        tests.test__getWatershedLinePixelPositions();
//        tests.explore__getDilatedAndErodedComponents();
//        tests.explore_data();
    }

    public AdvancedComponentTests() {
        testUtils = new TestUtils();
    }

    public void explore_data() throws IOException {
        Path testDataFolder = testUtils.getAbsolutTestFilePath("src/test/resources/00_probability_maps/20211026_VNG1040_AB6min_2h_1_MMStack_Pos7_GL12/frames_445-460__20211026_VNG1040_AB6min_2h_1_MMStack_Pos7_GL12");
        IImageProvider imageProvider = testUtils.getImageProviderFromDataFolder(testDataFolder);
        testUtils.showImageStack(imageProvider);
        testUtils.showProbabilityMaps(imageProvider);
        AdvancedComponentForest<FloatType, AdvancedComponent<FloatType>> componentForest = testUtils.getComponentTreeFromDataFolder(testDataFolder, 0, 0.5f);
        List<AdvancedComponent<FloatType>> allComponents = componentForest.getAllComponents();
        AdvancedComponent<FloatType> component = allComponents.get(allComponents.size() - 2);
        testUtils.showComponent(component);
        System.out.println(allComponents.size() - 2);
        System.out.println("stop");
    }

    @Test
    public void getBackgroundIntensity__when_called_with_uncorrected_fl_channel__returns_correct_value() throws IOException {
        int phcChannelNumber = 2;
        double expectedIntensity = 115900.0;
        ComponentInterface sut = getTestComponent1();
        double actualIntensity = sut.getBackgroundIntensity(phcChannelNumber);
        Assert.assertEquals(expectedIntensity, actualIntensity, 1e-6);
    }

    @Test
    public void getBackgroundIntensity__when_called_with_background_corrected_fl_channel__returns_correct_value() throws IOException {
        int phcChannelNumber = 1;
        double expectedIntensity = 10567.031685829163;
        ComponentInterface sut = getTestComponent1();
        double actualIntensity = sut.getBackgroundIntensity(phcChannelNumber);
        Assert.assertEquals(expectedIntensity, actualIntensity, 1e-6);
    }

    @Test
    public void getBackgroundIntensity__when_called_with_phc_channel__returns_correct_value() throws IOException {
        int phcChannelNumber = 0;
        double expectedIntensity = -10.640667281084461;
        ComponentInterface sut = getTestComponent1();
        double actualIntensity = sut.getBackgroundIntensity(phcChannelNumber);
        Assert.assertEquals(expectedIntensity, actualIntensity, 1e-6);
    }

    @Test
    public void getMaskIntensity__when_calling_getMaskIntensity_twice__second_call_is_much_faster_due_to_caching() throws IOException {
        int BackgroundCorrectedFluorescenceChannelNumber = 1;
        ComponentInterface component = getTestComponent1();

        long startTime = System.nanoTime();
        component.getMaskIntensity(BackgroundCorrectedFluorescenceChannelNumber);
        long endTime = System.nanoTime();
        long execTime1 = endTime - startTime;

        startTime = System.nanoTime();
        component.getMaskIntensity(BackgroundCorrectedFluorescenceChannelNumber);
        endTime = System.nanoTime();
        long execTime2 = endTime - startTime;

        Assert.assertTrue(execTime1/execTime2 > 1000); /* this test the speed improvement due to getting the value from the HashMap as opposed to calculating it. */
    }

    @Test
    public void getMaskIntensity__when_called_with_uncorrected_fl_channel__returns_correct_value() throws IOException {
        int BackgroundCorrectedFluorescenceChannelNumber = 2;
        double expectedIntensity = 254183.0;
        ComponentInterface sut = getTestComponent1();
        double actualIntensity = sut.getMaskIntensity(BackgroundCorrectedFluorescenceChannelNumber);
        Assert.assertEquals(expectedIntensity, actualIntensity, 1e-6);
    }

    @Test
    public void getMaskIntensity__when_called_with_background_corrected_fl_channel__returns_correct_value() throws IOException {
        int BackgroundCorrectedFluorescenceChannelNumber = 1;
        double expectedIntensity = 146598.8568496704;
        ComponentInterface sut = getTestComponent1();
        double actualIntensity = sut.getMaskIntensity(BackgroundCorrectedFluorescenceChannelNumber);
        Assert.assertEquals(expectedIntensity, actualIntensity, 1e-6);
    }

    @Test
    public void getMaskIntensity__when_called_with_phc_channel__returns_correct_value() throws IOException {
        int phcChannelNumber = 0;
        double expectedIntensity = 211.82272602943704;
        ComponentInterface sut = getTestComponent1();
        double actualIntensity = sut.getMaskIntensity(phcChannelNumber);
        Assert.assertEquals(expectedIntensity, actualIntensity, 1e-6);
    }

    private ComponentInterface getTestComponent1() throws IOException {
        int componentIndex = 8;
        Path testDataFolder = testUtils.getAbsolutTestFilePath("src/test/resources/00_probability_maps/20211026_VNG1040_AB6min_2h_1_MMStack_Pos7_GL12/frames_445-460__20211026_VNG1040_AB6min_2h_1_MMStack_Pos7_GL12");
        AdvancedComponentForest<FloatType, AdvancedComponent<FloatType>> componentForest = testUtils.getComponentTreeFromDataFolder(testDataFolder, 0, 0.5f);
        ComponentInterface component = testUtils.getTestComponent(componentForest, componentIndex);
        return component;
    }

    @Test
    public void test__getWatershedLinePixelValues() throws IOException {
        String imageFile = new File("").getAbsolutePath() + "/src/test/resources/00_probability_maps/probabilities_watershedding_000.tif";
        assertTrue(new File(imageFile).exists());

        ImageJ ij = new ImageJ();
        Img input = (Img) ij.io().open(imageFile);
        assertNotNull(input);
        int frameIndex = 10;
        IImageProvider imageProviderMock = new ImageProviderMock(input);
        RandomAccessibleInterval<FloatType> currentImage = Views.hyperSlice(input, 2, frameIndex);
        assertEquals(2, currentImage.numDimensions());

        ComponentForestGenerator componentForestGenerator = testUtils.getComponentTreeGenerator();

        AdvancedComponentForest<FloatType, AdvancedComponent<FloatType>> tree = componentForestGenerator.buildComponentForest(imageProviderMock, frameIndex, 1.0f);

        List<AdvancedComponent<FloatType>> roots = tree.rootsSorted();
        float[] expectedWatershedProbabilityValues = new float[]{0.7120329141616821F,0.7112422585487366F, 0.5284326672554016F};
        AdvancedComponent<FloatType> root = roots.get(0);
        List<FloatType> actualWatershedProbabilityValues = root.getWatershedLinePixelValues();

        for(int counter = 0; counter<actualWatershedProbabilityValues.size(); counter++){
            assertEquals(expectedWatershedProbabilityValues[counter], actualWatershedProbabilityValues.get(counter).getRealFloat(), 0.0001);
        }
    }

    @Test
    public void test__getWatershedLinePixelPositions() throws IOException {
        String imageFile = new File("").getAbsolutePath() + "/src/test/resources/00_probability_maps/probabilities_watershedding_000.tif";
        assertTrue(new File(imageFile).exists());

        ImageJ ij = new ImageJ();
        Img input = (Img) ij.io().open(imageFile);
        assertNotNull(input);
        int frameIndex = 10;
        IImageProvider imageProviderMock = new ImageProviderMock(input);
        RandomAccessibleInterval<FloatType> currentImage = Views.hyperSlice(input, 2, frameIndex);
        assertEquals(2, currentImage.numDimensions());

        ComponentForestGenerator componentForestGenerator = testUtils.getComponentTreeGenerator();

        AdvancedComponentForest<FloatType, AdvancedComponent<FloatType>> tree = componentForestGenerator.buildComponentForest(imageProviderMock, frameIndex, 1.0f);

        List<AdvancedComponent<FloatType>> roots = tree.rootsSorted();
        AdvancedComponent<FloatType> root = roots.get(0);
        if (root.getChildren().size() == 0) {
            return;
        }
        ArrayList<AdvancedComponent<FloatType>> componentsToDraw = new ArrayList<>();
        componentsToDraw.add(root);
        List<Localizable> watershedProbabilityPositions = root.getWatershedLinePixelPositions();

        /* test result */
        ArrayList<Vector2D> list = new ArrayList<>();
        list.add(new Vector2D(54, 177));
        list.add(new Vector2D(55, 176));
        list.add(new Vector2D(53, 178));
        int counter = 0;
        for (Localizable entry : watershedProbabilityPositions) {
            assertEquals(list.get(counter).getX(), entry.getDoublePosition(0), 0.001);
            assertEquals(list.get(counter).getY(), entry.getDoublePosition(1), 0.001);
            counter++;
        }
    }

    public void exploreGetParentWatershedLineCoordinates() throws IOException {
        String imageFile = new File("").getAbsolutePath() + "/src/test/resources/00_probability_maps/probabilities_watershedding_000.tif";
        assertTrue(new File(imageFile).exists());

        ImageJ ij = new ImageJ();
        Img input = (Img) ij.io().open(imageFile);
        assertNotNull(input);
        int frameIndex = 10;
        IImageProvider imageProviderMock = new ImageProviderMock(input);
        RandomAccessibleInterval<FloatType> currentImage = Views.hyperSlice(input, 2, frameIndex);
        assertEquals(2, currentImage.numDimensions());

        ComponentForestGenerator componentForestGenerator = testUtils.getComponentTreeGenerator();

        AdvancedComponentForest<FloatType, AdvancedComponent<FloatType>> tree = componentForestGenerator.buildComponentForest(imageProviderMock, frameIndex, 1.0f);

        List<AdvancedComponent<FloatType>> roots = tree.rootsSorted();
        for (AdvancedComponent<FloatType> root : roots) {
            if (root.getChildren().size() == 0) {
                return;
            }
            ArrayList<AdvancedComponent<FloatType>> componentsToDraw = new ArrayList<>();
            componentsToDraw.add(root);
            List<Localizable> watershedProbabilityPositions = root.getWatershedLinePixelPositions();
            Img<NativeBoolType> imgOfWatershedLine = root.createImage(root.getSourceImage());
            RandomAccess<NativeBoolType> rndAcc = imgOfWatershedLine.randomAccess();
            for (Localizable loc : watershedProbabilityPositions) {
                rndAcc.setPosition(loc);
                rndAcc.get().set(true);
            }
            RandomAccessibleInterval<ARGBType> parentComponentImage = root.getComponentImage(new ARGBType(ARGBType.rgba(255,255,255,255)));
            ImageJFunctions.show(parentComponentImage);
            RandomAccessibleInterval<ARGBType> childComponentsImage = Plotting.createImageWithComponents(root.getChildren(), new ArrayList<>(), root.getChildren().get(0).getSourceImage());
            ImageJFunctions.show(childComponentsImage);
            ImageJFunctions.show(imgOfWatershedLine);
            break;
        }
    }

    public void explore__getDilatedAndErodedComponents() throws IOException {
        String imageFile = new File("").getAbsolutePath() + "/src/test/resources/00_probability_maps/probabilities_watershedding_000.tif";
        assertTrue(new File(imageFile).exists());

        ImageJ ij = new ImageJ();
        Img input = (Img) ij.io().open(imageFile);
        assertNotNull(input);
        int frameIndex = 10;
        IImageProvider imageProviderMock = new ImageProviderMock(input);
        RandomAccessibleInterval<FloatType> currentImage = Views.hyperSlice(input, 2, frameIndex);
        assertEquals(2, currentImage.numDimensions());

        ComponentForestGenerator componentForestGenerator = testUtils.getComponentTreeGenerator();

        AdvancedComponentForest<FloatType, AdvancedComponent<FloatType>> tree = componentForestGenerator.buildComponentForest(imageProviderMock, frameIndex, 1.0f);

        List<AdvancedComponent<FloatType>> roots = tree.rootsSorted();
        AdvancedComponent<FloatType> component = roots.get(1);
//        Img<BitType> componentImage = component.getComponentImage(new BitType(true));
//        ImageJFunctions.show(componentImage);
        MaskInterval dilatedMask = component.getDilatedMask();

        MaskInterval erodedMask = component.getErodedMask();

        new ij.ImageJ();
        ImageJFunctions.show(Masks.toRandomAccessibleInterval(dilatedMask));
        ImageJFunctions.show(Masks.toRandomAccessibleInterval(erodedMask));

        MaskInterval differenceMask = dilatedMask.minus(erodedMask);
        ImageJFunctions.show(Masks.toRandomAccessibleInterval(differenceMask));
    }
}
