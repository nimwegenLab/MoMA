package com.jug.util;

import com.jug.config.ComponentForestGeneratorConfigurationMock;
import com.jug.config.IConfiguration;
import com.jug.datahandling.IImageProvider;
import com.jug.lp.ImageProviderMock;
import com.jug.lp.costs.ICostFactory;
import com.jug.util.componenttree.*;
import com.jug.util.imglib2.Imglib2Utils;
import com.jug.util.math.Vector2D;
import com.jug.util.math.Vector2DPolyline;
import com.moma.auxiliary.Plotting;
import ij.ImagePlus;
import ij.gui.Overlay;
import net.imagej.ImageJ;
import net.imagej.ops.OpService;
import net.imagej.roi.DefaultROITree;
import net.imagej.roi.ROITree;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.binary.Thresholder;
import net.imglib2.algorithm.componenttree.ComponentForest;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.roi.MaskPredicate;
import net.imglib2.type.NativeType;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.ValuePair;
import net.imglib2.view.Views;
import org.jetbrains.annotations.NotNull;
//import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import java.awt.*;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TestUtils {
    private final ImageJ ij;
    private ImageProviderMock imageProviderMock;
    private int frameIndex;

    public TestUtils() {
        this(new ImageJ());
    }

    public double getDeltaDouble() {
        return 1e-6;
    }

    public int getDeltaInteger() {
        return 0;
    }

    public TestUtils(ImageJ ij) {
        this.ij = ij;
    }

//    public ValuePair<AdvancedComponent<FloatType>, RandomAccessibleInterval<FloatType>> getComponentWithImageNew(String imageFile, int componentIndex) throws IOException {
//        ComponentForest<AdvancedComponent<FloatType>> tree = getComponentTree(imageFile);
//        ComponentPositionComparator verticalComponentPositionComparator = new ComponentPositionComparator(1);
//        List<AdvancedComponent<FloatType>> roots = new ArrayList<>(tree.roots());
//        roots.sort(verticalComponentPositionComparator);
//        AdvancedComponent<FloatType> component = roots.get(componentIndex);
//        ArrayList<AdvancedComponent<FloatType>> componentList = new ArrayList<>();
//        componentList.add(component);
//        RandomAccessibleInterval<FloatType> image = Plotting.createImageWithComponentsNew(componentList, new ArrayList<>());
//        return new ValuePair<>(component, image);
//    }

    public IImageProvider getImageProvider() {
        return imageProviderMock;
    }

    public Integer getFrameIndex(){
        return frameIndex;
    }

    public void drawComponentTree(String imageFile) throws IOException {
        ComponentForest<AdvancedComponent<FloatType>> tree = getComponentTree(imageFile);
        List<AdvancedComponent<FloatType>> roots = new ArrayList<>(tree.roots());
        AdvancedComponent<FloatType> res = roots.get(0);
        Plotting.drawComponentTree2(tree, new ArrayList<>(), res.getSourceImage());
    }

    public <T extends NativeType> ValuePair<AdvancedComponent<FloatType>, RandomAccessibleInterval<T>> getComponentWithImage(String imageFile,
                                                                                                                             int componentIndex,
                                                                                                                             T pixelValue) throws IOException {
        ComponentForest<AdvancedComponent<FloatType>> tree = getComponentTree(imageFile);
        ComponentPositionComparator verticalComponentPositionComparator = new ComponentPositionComparator(1);
        List<AdvancedComponent<FloatType>> roots = new ArrayList<>(tree.roots());
        roots.sort(verticalComponentPositionComparator);
        AdvancedComponent<FloatType> component = roots.get(componentIndex);
        RandomAccessibleInterval<T> image = component.getComponentImage(pixelValue);
        return new ValuePair<>(component, image);
    }

    public ComponentForest<AdvancedComponent<FloatType>> getComponentTree(String imageFile) throws IOException {
        assertTrue(new File(imageFile).exists());
        Img input = (Img) ij.io().open(imageFile);
        assertNotNull(input);
        frameIndex = 0;
        imageProviderMock = new ImageProviderMock(input);
        RandomAccessibleInterval<FloatType> currentImage = Views.hyperSlice(input, 2, frameIndex);
        assertEquals(2, currentImage.numDimensions());
        ComponentForestGenerator componentForestGenerator = getComponentTreeGenerator();
        ComponentForest<AdvancedComponent<FloatType>> tree = componentForestGenerator.buildComponentForest(imageProviderMock, frameIndex, 1.0f);
        return tree;
    }

    public ComponentProperties getComponentProperties() {
        OpService ops = ij.op();
        Imglib2Utils imglib2Utils = new Imglib2Utils(ops);
        IConfiguration configurationMock = mock(IConfiguration.class);
        when(configurationMock.getBackgroundRoiWidth()).thenReturn(5L);
        return new ComponentProperties(ops, imglib2Utils, new CostFactoryMock(), configurationMock);
    }

    @NotNull
    public ComponentForestGenerator getComponentTreeGenerator() {
        Imglib2Utils imglib2Utils = getImglib2Utils();
        ComponentProperties componentProperties = getComponentProperties();
        RecursiveComponentWatershedder recursiveComponentWatershedder = getRecursiveComponentWatershedder();
        WatershedMaskGenerator watershedMaskGenerator = new WatershedMaskGenerator(0.5f, 0.5f);
        ComponentForestGeneratorConfigurationMock config = new ComponentForestGeneratorConfigurationMock(60, Integer.MIN_VALUE);
        ComponentForestGenerator componentForestGenerator = new ComponentForestGenerator(config, recursiveComponentWatershedder, componentProperties, watershedMaskGenerator, imglib2Utils);
        return componentForestGenerator;
    }

    @NotNull
    private RecursiveComponentWatershedder getRecursiveComponentWatershedder() {
        RecursiveComponentWatershedder recursiveComponentWatershedder = new RecursiveComponentWatershedder(ij.op());
        return recursiveComponentWatershedder;
    }

    @NotNull
    public Imglib2Utils getImglib2Utils() {
        OpService ops = ij.op();
        Imglib2Utils imglib2Utils = new Imglib2Utils(ops);
        return imglib2Utils;
    }

    public <T extends NumericType<T>> void showImageWithOverlays2(RandomAccessibleInterval<T> image, List<Vector2DPolyline> polylines) {
        List<MaskPredicate<?>> rois = new ArrayList<>();
        polylines.forEach(polyline -> rois.add(polyline.getPolyline()));
        showImageWithOverlays(image, rois);
    }

    public <T extends NumericType<T>> void showImageWithOverlays(RandomAccessibleInterval<T> image, List<MaskPredicate<?>> rois) {
        ROITree roiTree = new DefaultROITree();
        roiTree.addROIs(rois);
        Overlay overlay = ij.convert().convert(roiTree, Overlay.class);
        ImagePlus imagePlus = ImageJFunctions.wrap(image, "image");
        overlay.setStrokeColor(Color.RED);
        overlay.setStrokeWidth(.2);
        imagePlus.setOverlay(overlay);
        ij.ui().show(imagePlus);
//
//        double mag = 5.; // between 0 and 1
//        int newWidth = (int) (imagePlus.getWidth() * mag);
//        int newHeight = (int) ( imagePlus.getHeight() * mag);
//
//        ImageCanvas canvas = imagePlus.getCanvas();
//        canvas.setMagnification(mag);
//
//        ImageWindow win = imagePlus.getWindow();
//        win.setSize(newWidth, newHeight);
    }

    public Img<BitType> readComponentMask(String imageFile) throws IOException {
        Img<UnsignedByteType> input = (Img) ij.io().open(imageFile);
        Img<BitType> componentMask = Thresholder.threshold(input, new UnsignedByteType(128), true, 1); /* BitType images */
        return componentMask;
    }

    public static void assertEqual(Vector2D expected, Vector2D actual, double delta){
        for (int posInd = 0; posInd < 2; posInd++) {
            if (doubleIsDifferent(expected.getDoublePosition(posInd), actual.getDoublePosition(posInd), delta)) {
                throw new AssertionError("Vectors not equal:\nexpected: " + expected + "\nactual: " + actual);
            }
        }
    }

    private static boolean doubleIsDifferent(double d1, double d2, double delta) {
        if (Double.compare(d1, d2) == 0) {
            return false;
        } else {
            return !(Math.abs(d1 - d2) <= delta);
        }
    }

    public static Path getPathToResourcesDirectory(){
        String pathToMomaGitRepo = new File("").getAbsolutePath();
        Path pathToResources = Paths.get(pathToMomaGitRepo, "src/test/resources");
        return pathToResources;
    }


    @NotNull
    public List<AdvancedComponentForest<FloatType, AdvancedComponent<FloatType>>> getAdvancedComponentForestList(Path imageFile, int frameIndexStart, int frameIndexStop) throws IOException {
        List<AdvancedComponentForest<FloatType, AdvancedComponent<FloatType>>> componentForests = new ArrayList<>();
        for (int frameIndex = frameIndexStart; frameIndex < frameIndexStop; frameIndex++) {
            componentForests.add(this.getComponentForestFromProbabilityImage(imageFile, frameIndex, 1.0f));
        }
        return componentForests;
    }

    public AdvancedComponentForest<FloatType, AdvancedComponent<FloatType>> getComponentForestFromProbabilityImage(Path imageFile, int frameIndex, float componentSplittingThreshold) throws IOException {
        IImageProvider imageProvider = getImageProviderFromProbabilityImage(imageFile);
        ComponentForestGenerator componentForestGenerator = getComponentTreeGenerator();
        AdvancedComponentForest<FloatType, AdvancedComponent<FloatType>> tree = componentForestGenerator.buildComponentForest(imageProvider, frameIndex, componentSplittingThreshold);
        return tree;
    }

    public ComponentInterface getTestComponent(AdvancedComponentForest<FloatType, AdvancedComponent<FloatType>> componentForest, int index) {
        List<AdvancedComponent<FloatType>> allComponents = componentForest.getAllComponents();
        AdvancedComponent<FloatType> component = allComponents.get(index);
        return component;
    }

    public List<AdvancedComponentForest<FloatType, AdvancedComponent<FloatType>>> getComponentForestListFromDataFolder(Path testDataFolder, int firstFrame, int lastFrame, float componentSplittingThreshold) throws IOException {
        IImageProvider imageProvider = getImageProviderFromDataFolder(testDataFolder);
        ComponentForestGenerator componentForestGenerator = getComponentTreeGenerator();
        List<AdvancedComponentForest<FloatType, AdvancedComponent<FloatType>>> componentForests = new ArrayList<>();
        for (int frameIndex = firstFrame; frameIndex <= lastFrame; frameIndex++) {
            componentForests.add(componentForestGenerator.buildComponentForest(imageProvider, frameIndex, componentSplittingThreshold));
        }
        return componentForests;
    }

    public AdvancedComponentForest<FloatType, AdvancedComponent<FloatType>> getComponentForestFromDataFolder(Path testDataFolder, int frameIndex, float componentSplittingThreshold) throws IOException {
        IImageProvider imageProvider = getImageProviderFromDataFolder(testDataFolder);
        ComponentForestGenerator componentForestGenerator = getComponentTreeGenerator();
        AdvancedComponentForest<FloatType, AdvancedComponent<FloatType>> tree = componentForestGenerator.buildComponentForest(imageProvider, frameIndex, componentSplittingThreshold);
        return tree;
    }

    public IImageProvider getImageProviderFromDataFolder(Path testDataFolder) throws IOException {
        assertTrue(testDataFolder.toFile().exists());
        Path testImagePath = getTestImageFilePath(testDataFolder);
        Img imageStack = (Img) ij.io().open(testImagePath.toString());
        assertNotNull(imageStack);

        Path probabilityMapsImageFilePath = getProbabilityMapsImageFilePath(testDataFolder);
        Img probabilityMapsImage = (Img) ij.io().open(probabilityMapsImageFilePath.toString());
        assertNotNull(imageStack);

        return new ImageProviderMock(probabilityMapsImage, imageStack);
    }

    public Path getAbsolutTestFilePath(String relativePath) {
        return Paths.get(new File("").getAbsolutePath(), relativePath);
    }

    public IImageProvider getImageProviderFromProbabilityImage(Path imageFile) throws IOException {
        assertTrue(imageFile.toFile().exists());
        Img input = (Img) ij.io().open(imageFile.toString());
        assertNotNull(input);
        return new ImageProviderMock(input);
    }

    public Path getTestImageFilePath(Path testDataPath){
        assertTrue(testDataPath.toFile().exists());
        final FilenameFilter filter = (dir, name) -> name.contains( "images__frames_" );
        File[] list = testDataPath.toFile().listFiles(filter);
        return list[0].toPath();
    }

    public Path getProbabilityMapsImageFilePath(Path testDataPath){
        assertTrue(testDataPath.toFile().exists());
        final FilenameFilter filter = (dir, name) -> name.contains( "probability_maps__model_" );
        File[] list = testDataPath.toFile().listFiles(filter);
        return list[0].toPath();
    }

    public <T extends NumericType<T>> ImagePlus show(RandomAccessibleInterval<T> img) {
        return ImageJFunctions.show(img, "");
    }

    public void showImageStack(IImageProvider imageProvider) {
        ImageJFunctions.show(imageProvider.getImgRaw());
    }

    public void showProbabilityMaps(IImageProvider imageProvider) {
        ImageJFunctions.show(imageProvider.getImgProbs());
    }

    class CostFactoryMock implements ICostFactory {
        @Override
        public float getComponentCost(ComponentInterface component) {
            return 0;
        }
    }

    public ImagePlus showComponent(ComponentInterface component) {
        ArrayList<ComponentInterface> componentsToDraw = new ArrayList<>();
        componentsToDraw.add(component);
        return ImageJFunctions.show(Plotting.createImageWithComponents(componentsToDraw, new ArrayList<>(), component.getSourceImage()));
    }

    /**
     * Return Img with specified dimensions and value/value-type.
     *
     * @param dims
     * @param value
     * @return
     * @param <T>
     */
    public <T extends NativeType<T>> Img<T> getImageWithValue(long[] dims, T value) {
        ArrayImgFactory<T> imgFactory = new ArrayImgFactory<>(value);
        Img<T> img = imgFactory.create(dims);
        img.iterator().forEachRemaining(val -> val.set(value));
        return img;
    }

    /**
     * Return Img with specified dimensions and value/value-type.
     *
     * @param dims
     * @return
     */
    public Img<FloatType> getImageWithNormallyDistributedIntensities(long[] dims, double expectedMean, double expectedStd) {
        ArrayImgFactory<FloatType> imgFactory = new ArrayImgFactory<>(new FloatType());
        Img<FloatType> img = imgFactory.create(dims);
        img.spliterator().forEachRemaining(val -> val.set((float)(expectedStd * ThreadLocalRandom.current().nextGaussian() + expectedMean)));
        return img;
    }
}
