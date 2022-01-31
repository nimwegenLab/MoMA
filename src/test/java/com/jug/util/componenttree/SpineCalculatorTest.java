package com.jug.util.componenttree;

import com.jug.lp.costs.ComponentMock;
import com.jug.util.TestUtils;
import com.jug.util.imglib2.Imglib2Utils;
import com.jug.util.math.GeomUtils;
import com.jug.util.math.Vector2D;
import com.jug.util.math.Vector2DPolyline;
import ij.IJ;
import ij.ImagePlus;
import ij.gui.Overlay;
import ij.gui.Roi;
import net.imagej.ImageJ;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.roi.MaskPredicate;
import net.imglib2.roi.labeling.LabelRegion;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.ValuePair;
import org.junit.Test;
import org.scijava.convert.ConvertService;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

public class SpineCalculatorTest {
    private final ImageJ ij;
    private final TestUtils testUtils;
    private double delta = 1e-5;

    public SpineCalculatorTest() {
        ij = new ImageJ();
        testUtils = new TestUtils(ij);
    }

    public static void main(String... args) throws IOException {
        ImageJ ij = new ImageJ();
        ij.ui().showUI();
//        new SpineCalculatorTest().exploreSpineCalculator();
//        new SpineCalculatorTest().exploreSpineCalculator2();
//        new SpineCalculatorTest().exploreSpineCalculator3();
//        new SpineCalculatorTest().exploreSpineCalculator4();
//        new SpineCalculatorTest().exploreSpineCalculator5();
        new SpineCalculatorTest().exploreSpineCalculator6();
    }

    /**
     * Add test for calculating the medial line.
     *
     * @throws IOException
     * @throws InterruptedException
     */
    public void exploreSpineCalculator() throws IOException {
        String imageFile = new File("").getAbsolutePath() + "/src/test/resources/00_probability_maps/20201119_VNG1040_AB2h_2h_1_MMStack_Pos6_GL6/frame90_repeated__cropped__20201119_VNG1040_AB2h_2h_1_MMStack_Pos6_GL6__model_9e5727e4ed18802f4ab04c7494ef8992d798f4d64d5fd75e285b9a3d83b13ac9.tif";

        int componentIndex = 2;

        ValuePair<AdvancedComponent<FloatType>, RandomAccessibleInterval<BitType>> componentAndImage = testUtils.getComponentWithImage(imageFile,
                componentIndex,
                new BitType(true));
        AdvancedComponent<FloatType> component = componentAndImage.getA();
        RandomAccessibleInterval<BitType> image = component.getComponentImage(new BitType(true));

        MedialLineCalculator medialLineCalculator = new MedialLineCalculator(ij.op(), new Imglib2Utils(ij.op()));
        Vector2DPolyline medialLine = medialLineCalculator.calculate(image);
        ContourCalculator contourCalculator = new ContourCalculator(ij.op());

        LabelRegion<Integer> componentRegion = componentAndImage.getA().getRegion();
        Vector2DPolyline contour = contourCalculator.calculate(componentRegion);

        SpineCalculator sut = new SpineCalculator(21, 21, 3.5);

        Vector2DPolyline spine = sut.calculate(medialLine, contour, new ValuePair<>((int) image.min(1), (int) image.max(1)));

        contour.shiftMutate(new Vector2D(0.5, 0.5));
        medialLine.shiftMutate(new Vector2D(0.5, 0.5));
        spine.shiftMutate(new Vector2D(0.5, 0.5));

        List<MaskPredicate<?>> rois = Arrays.asList(
                contour.getPolygon2D(),
                medialLine.getPolyline(),
                spine.getPolyline()
        );
        testUtils.showImageWithOverlays(image, rois);
    }

    /**
     * Add test for calculating the medial line.
     *
     * @throws IOException
     * @throws InterruptedException
     */
    @Test
    public void spine_calculation__using_GeomUtils_smooth_to_filter_the_medialline__returns_expected_result() throws IOException {
        String imageFile = new File("").getAbsolutePath() + "/src/test/resources/00_probability_maps/20201119_VNG1040_AB2h_2h_1_MMStack_Pos6_GL6/frame90_repeated__cropped__20201119_VNG1040_AB2h_2h_1_MMStack_Pos6_GL6__model_9e5727e4ed18802f4ab04c7494ef8992d798f4d64d5fd75e285b9a3d83b13ac9.tif";

        int componentIndex = 2;

        ValuePair<AdvancedComponent<FloatType>, RandomAccessibleInterval<BitType>> componentAndImage = testUtils.getComponentWithImage(imageFile,
                componentIndex,
                new BitType(true));
        AdvancedComponent<FloatType> component = componentAndImage.getA();
        RandomAccessibleInterval<BitType> image = component.getComponentImage(new BitType(true));

        MedialLineCalculator medialLineCalculator = new MedialLineCalculator(ij.op(), new Imglib2Utils(ij.op()));
        Vector2DPolyline medialLine = medialLineCalculator.calculate(image);
        ContourCalculator contourCalculator = new ContourCalculator(ij.op());

        LabelRegion<Integer> componentRegion = componentAndImage.getA().getRegion();
        Vector2DPolyline contour = contourCalculator.calculate(componentRegion);

        Function<Vector2DPolyline, Vector2DPolyline> medialLineProcessor = (input) -> GeomUtils.smooth(input, 21);
        SpineCalculator sut = new SpineCalculator(21, 3.5, medialLineProcessor);

        Vector2DPolyline spine = sut.calculate(medialLine, contour, new ValuePair<>((int) image.min(1), (int) image.max(1)));

        contour.shiftMutate(new Vector2D(0.5, 0.5));
        medialLine.shiftMutate(new Vector2D(0.5, 0.5));
        spine.shiftMutate(new Vector2D(0.5, 0.5));

        List<MaskPredicate<?>> rois = Arrays.asList(
                contour.getPolygon2D(),
                medialLine.getPolyline(),
                spine.getPolyline()
        );

        Vector2D first = spine.getFirst();
        TestUtils.assertEqual(new Vector2D(51.73809523809524, 95.5), first, delta);
        Vector2D centerPoint = spine.get(54);
        TestUtils.assertEqual(new Vector2D(54.785714285714285, 162.5), centerPoint, delta);
        Vector2D last = spine.getLast();
        TestUtils.assertEqual(new Vector2D(52.597995545657014, 232.5), last, delta);

//        testUtils.showImageWithOverlays(image, rois);
    }

    /**
     * Add test for calculating the medial line.
     *
     * @throws IOException
     * @throws InterruptedException
     */
    @Test
    public void spine_calculation__using_GeomUtils_smoothWithAdaptiveWindowSize_to_filter_the_medialline__returns_expected_result() throws IOException {
        String imageFile = new File("").getAbsolutePath() + "/src/test/resources/00_probability_maps/20201119_VNG1040_AB2h_2h_1_MMStack_Pos6_GL6/frame90_repeated__cropped__20201119_VNG1040_AB2h_2h_1_MMStack_Pos6_GL6__model_9e5727e4ed18802f4ab04c7494ef8992d798f4d64d5fd75e285b9a3d83b13ac9.tif";

        int componentIndex = 2;

        ValuePair<AdvancedComponent<FloatType>, RandomAccessibleInterval<BitType>> componentAndImage = testUtils.getComponentWithImage(imageFile,
                componentIndex,
                new BitType(true));
        AdvancedComponent<FloatType> component = componentAndImage.getA();
        RandomAccessibleInterval<BitType> image = component.getComponentImage(new BitType(true));

        MedialLineCalculator medialLineCalculator = new MedialLineCalculator(ij.op(), new Imglib2Utils(ij.op()));
        Vector2DPolyline medialLine = medialLineCalculator.calculate(image);
        ContourCalculator contourCalculator = new ContourCalculator(ij.op());

        LabelRegion<Integer> componentRegion = componentAndImage.getA().getRegion();
        Vector2DPolyline contour = contourCalculator.calculate(componentRegion);

        Function<Vector2DPolyline, Vector2DPolyline> medialLineProcessor = (input) -> GeomUtils.smoothWithAdaptiveWindowSize(input, 7, 21);
        SpineCalculator sut = new SpineCalculator(21, 3.5, medialLineProcessor);

        Vector2DPolyline spine = sut.calculate(medialLine, contour, new ValuePair<>((int) image.min(1), (int) image.max(1)));

        contour.shiftMutate(new Vector2D(0.5, 0.5));
        medialLine.shiftMutate(new Vector2D(0.5, 0.5));
        spine.shiftMutate(new Vector2D(0.5, 0.5));

        List<MaskPredicate<?>> rois = Arrays.asList(
                contour.getPolygon2D(),
                medialLine.getPolyline(),
                spine.getPolyline()
        );

        Vector2D first = spine.getFirst();
        TestUtils.assertEqual(new Vector2D(51.92857142857142, 95.5), first, delta);
        Vector2D centerPoint = spine.get(61);
        TestUtils.assertEqual(new Vector2D(54.785714285714285, 162.5), centerPoint, delta);
        Vector2D last = spine.getLast();
        TestUtils.assertEqual(new Vector2D(53.26470588235294, 232.5), last, delta);

        testUtils.showImageWithOverlays(image, rois);
    }

    /**
     * Add test for calculating the medial line.
     *
     * @throws IOException
     * @throws InterruptedException
     */
    public void exploreSpineCalculator4() throws IOException {
        String imageFile = new File("").getAbsolutePath() + "/src/test/resources/00_probability_maps/20201119_VNG1040_AB2h_2h_1_MMStack_Pos6_GL6/frame90_repeated__cropped__20201119_VNG1040_AB2h_2h_1_MMStack_Pos6_GL6__model_9e5727e4ed18802f4ab04c7494ef8992d798f4d64d5fd75e285b9a3d83b13ac9.tif";

        int componentIndex = 4;

        ValuePair<AdvancedComponent<FloatType>, RandomAccessibleInterval<BitType>> componentAndImage = testUtils.getComponentWithImage(imageFile,
                componentIndex,
                new BitType(true));
        AdvancedComponent<FloatType> component = componentAndImage.getA();
        RandomAccessibleInterval<BitType> image = component.getComponentImage(new BitType(true));

        MedialLineCalculator medialLineCalculator = new MedialLineCalculator(ij.op(), new Imglib2Utils(ij.op()));
        Vector2DPolyline medialLine = medialLineCalculator.calculate(image);
        ContourCalculator contourCalculator = new ContourCalculator(ij.op());

        LabelRegion<Integer> componentRegion = componentAndImage.getA().getRegion();
        Vector2DPolyline contour = contourCalculator.calculate(componentRegion);

        Function<Vector2DPolyline, Vector2DPolyline> medialLineProcessor = (input) -> GeomUtils.smoothWithAdaptiveWindowSize(input, 5, 21);
        SpineCalculator sut = new SpineCalculator(5, 3.5, medialLineProcessor);

        Vector2DPolyline spine = sut.calculate(medialLine, contour, new ValuePair<>((int) image.min(1), (int) image.max(1)));

        contour.shiftMutate(new Vector2D(0.5, 0.5));
        medialLine.shiftMutate(new Vector2D(0.5, 0.5));
        spine.shiftMutate(new Vector2D(0.5, 0.5));

        List<Vector2DPolyline> contours = new ArrayList<>();
//        contours.add(contour);
        contours.add(medialLine);
        contours.add(spine);

        testUtils.showImageWithOverlays2(image, contours);
    }

    /**
     * Add test for calculating the medial line.
     *
     * @throws IOException
     * @throws InterruptedException
     */
    public void exploreSpineCalculator2() throws IOException {
        String imageFile = new File("").getAbsolutePath() + "/src/test/resources/ComponentMasks/component_2.tiff";
        Img<BitType> componentMask = testUtils.readComponentMask(imageFile);

//        componentMask = ImgView.wrap(Views.zeroMin(Views.interval(componentMask, new long[]{40, 171}, new long[]{67, 244})));
//        ImageJFunctions.show(componentMask);

        ComponentMock component = new ComponentMock(componentMask);

        LabelRegion<Integer> componentRegion = component.getRegion();
        ContourCalculator contourCalculator = new ContourCalculator(ij.op());
        Vector2DPolyline contour = contourCalculator.calculate(componentRegion);

        MedialLineCalculator medialLineCalculator = new MedialLineCalculator(ij.op(), new Imglib2Utils(ij.op()));
        Vector2DPolyline medialLine = medialLineCalculator.calculate(componentMask);

        double maxVerticalDistanceFromStartAndEnd = 3.5;
        SpineCalculator sut = new SpineCalculator(7, 7, maxVerticalDistanceFromStartAndEnd);

        Vector2DPolyline spine = sut.calculate(medialLine, contour, new ValuePair<>((int) componentMask.min(1), (int) componentMask.max(1)));

        contour.shiftMutate(new Vector2D(0.5, 0.5));
        medialLine.shiftMutate(new Vector2D(0.5, 0.5));
        spine.shiftMutate(new Vector2D(0.5, 0.5));

        List<MaskPredicate<?>> rois = Arrays.asList(
                contour.getPolygon2D(),
                medialLine.getPolyline(),
                spine.getPolyline()
        );
        testUtils.showImageWithOverlays(componentMask, rois);
//        IJ.run("Set... ", "zoom=400 x=12 y=39"); // does not work, but see here if interested to get it working: https://forum.image.sc/t/programmatically-set-display-zoom-level-in-imagej-fiji/49862
//        ij.op().
    }

    /**
     * Add test for calculating the medial line.
     *
     * @throws IOException
     * @throws InterruptedException
     */
    public void exploreSpineCalculator5() throws IOException {
        String imageFile = new File("").getAbsolutePath() + "/src/test/resources/ComponentMasks/component_2.tiff";
        Img<BitType> componentMask = testUtils.readComponentMask(imageFile);

//        componentMask = ImgView.wrap(Views.zeroMin(Views.interval(componentMask, new long[]{40, 171}, new long[]{67, 244})));
//        ImageJFunctions.show(componentMask);

        ComponentMock component = new ComponentMock(componentMask);

        LabelRegion<Integer> componentRegion = component.getRegion();
        ContourCalculator contourCalculator = new ContourCalculator(ij.op());
        Vector2DPolyline contour = contourCalculator.calculate(componentRegion);

        MedialLineCalculator medialLineCalculator = new MedialLineCalculator(ij.op(), new Imglib2Utils(ij.op()));
        Vector2DPolyline medialLine = medialLineCalculator.calculate(componentMask);

        double maxVerticalDistanceFromStartAndEnd = 3.5;
        SpineCalculator sut = new SpineCalculator(7, 7, maxVerticalDistanceFromStartAndEnd);

        Vector2DPolyline spine = sut.calculate(medialLine, contour, new ValuePair<>((int) componentMask.min(1), (int) componentMask.max(1)));

        contour.shiftMutate(new Vector2D(0.5, 0.5));
        medialLine.shiftMutate(new Vector2D(0.5, 0.5));
        spine.shiftMutate(new Vector2D(0.5, 0.5));

        ConvertService convertService = ij.convert();
//        ConvertService convertService = new DefaultConvertService();
        Roi contourRoi = convertService.convert(contour.getPolygon2D(), Roi.class);
        Roi spineRoi = convertService.convert(spine.getPolyline(), Roi.class);

//        ImagePlus imp = IJ.createHyperStack("HyperStack", (int) componentMask.dimension(0), (int) componentMask.dimension(1), 2, 1, 5, 8);

        ImagePlus imagePlus = ImageJFunctions.wrap(componentMask, "image");
//        List<MaskPredicate<?>> rois = Arrays.asList(
//                contour.getPolygon2D(),
//                medialLine.getPolyline(),
//                spine.getPolyline()
//        );
//        testUtils.showImageWithOverlays(componentMask, rois);
        Overlay overlay = new Overlay();
        contourRoi.setStrokeColor(Color.RED);
        contourRoi.setStrokeWidth(.2);
        overlay.add(contourRoi);
        spineRoi.setStrokeColor(Color.BLUE);
        spineRoi.setStrokeWidth(.2);
        overlay.add(spineRoi);
        imagePlus.setOverlay(overlay);
        ij.ui().show(imagePlus);
        IJ.save(imagePlus, "/home/micha/TemporaryFiles/20220119_imagej_overlays/overlay_test_image_2.tif");
//        IJ.run("Set... ", "zoom=400 x=12 y=39"); // does not work, but see here if interested to get it working: https://forum.image.sc/t/programmatically-set-display-zoom-level-in-imagej-fiji/49862
//        ij.op().
    }

    static void showCroppedImage(ImagePlus imp, int x, int y, int width, int height) {
        imp.setRoi(x, y, width, height);
        ImagePlus impCropped = imp.crop();
        impCropped.show();

    }

    static ImagePlus addRoiOverlay(ImagePlus imagePlus1, Roi roi, Color color) {
        Overlay overlay1 = new Overlay();
        roi.setStrokeColor(color);
        roi.setStrokeWidth(.4);
        overlay1.add(roi);
        imagePlus1.setOverlay(overlay1);
        return imagePlus1;
    }


    /**
     * Add test for calculating the medial line.
     *
     * @throws IOException
     * @throws InterruptedException
     */
    public void exploreSpineCalculator6() throws IOException {
        String imageFile = new File("").getAbsolutePath() + "/src/test/resources/00_probability_maps/20201119_VNG1040_AB2h_2h_1_MMStack_Pos6_GL6/frame90_repeated__cropped__20201119_VNG1040_AB2h_2h_1_MMStack_Pos6_GL6__model_9e5727e4ed18802f4ab04c7494ef8992d798f4d64d5fd75e285b9a3d83b13ac9.tif";

        int componentIndex = 4;

        ValuePair<AdvancedComponent<FloatType>, RandomAccessibleInterval<BitType>> componentAndImage = testUtils.getComponentWithImage(imageFile,
                componentIndex,
                new BitType(true));
        AdvancedComponent<FloatType> component = componentAndImage.getA();
        RandomAccessibleInterval<BitType> componentMask = component.getComponentImage(new BitType(true));



        LabelRegion<Integer> componentRegion = component.getRegion();
        ContourCalculator contourCalculator = new ContourCalculator(ij.op());
        Vector2DPolyline contour = contourCalculator.calculate(componentRegion);

        MedialLineCalculator medialLineCalculator = new MedialLineCalculator(ij.op(), new Imglib2Utils(ij.op()));
        Vector2DPolyline medialLine = medialLineCalculator.calculate(componentMask);

        RandomAccessibleInterval componentMaskThinned = ij.op().morphology().thinGuoHall(componentMask);

        double maxVerticalDistanceFromStartAndEnd = 3.5;
//        SpineCalculator sut = new SpineCalculator(7, 7, maxVerticalDistanceFromStartAndEnd);

        Function<Vector2DPolyline, Vector2DPolyline> medialLineProcessor =
                (input) -> GeomUtils.smoothWithAdaptiveWindowSize(input,5,21);
        SpineCalculator sut = new SpineCalculator(5,3.5,medialLineProcessor);

        Vector2DPolyline spine = sut.calculate(medialLine, contour, new ValuePair<>((int) componentMask.min(1), (int) componentMask.max(1)));

        contour.shiftMutate(new Vector2D(0.5, 0.5));
        medialLine.shiftMutate(new Vector2D(0.5, 0.5));
        spine.shiftMutate(new Vector2D(0.5, 0.5));

        ConvertService convertService = ij.convert();
//        ConvertService convertService = new DefaultConvertService();
        Roi contourRoi = convertService.convert(contour.getPolygon2D(), Roi.class);
        Roi spineRoi = convertService.convert(spine.getPolyline(), Roi.class);
        Roi medialLineRoi = convertService.convert(medialLine.getPolyline(), Roi.class);

        Vector2DPolyline smoothedMedialLine = GeomUtils.smoothWithAdaptiveWindowSize(medialLine, 5, 21);
        Roi smoothedMedialLineRoi = convertService.convert(smoothedMedialLine.getPolyline(), Roi.class);

//        ImagePlus imp = IJ.createHyperStack("HyperStack", (int) componentMask.dimension(0), (int) componentMask.dimension(1), 2, 1, 5, 8);

        int x = 35, y = 274, width = 36, height = 97;

        showCroppedImage(ImageJFunctions.wrap(componentMask, "componentMaskThinned"), x, y, width, height);

        ImagePlus componentMaskThinnedImagePlus = ImageJFunctions.wrap(componentMaskThinned, "componentMaskThinned");
        showCroppedImage(componentMaskThinnedImagePlus, x, y, width, height);

        ImagePlus imagePlus1 = ImageJFunctions.wrap(componentMask, "image");
        imagePlus1 = addRoiOverlay(imagePlus1, medialLineRoi, Color.RED);
        showCroppedImage(imagePlus1, x, y, width, height);

        ImagePlus imagePlus2 = ImageJFunctions.wrap(componentMask, "image");
        imagePlus2 = addRoiOverlay(imagePlus2, smoothedMedialLineRoi, Color.RED);
        showCroppedImage(imagePlus2, x, y, width, height);

        ImagePlus imagePlus3 = ImageJFunctions.wrap(componentMask, "image");
        imagePlus3 = addRoiOverlay(imagePlus3, spineRoi, Color.RED);
        showCroppedImage(imagePlus3, x, y, width, height);

        ImagePlus imagePlus4 = ImageJFunctions.wrap(componentMask, "image");
        Overlay overlay2 = new Overlay();
        spineRoi.setStrokeColor(Color.RED);
        spineRoi.setStrokeWidth(.4);
        overlay2.add(spineRoi);
        contourRoi.setStrokeColor(Color.BLUE);
        contourRoi.setStrokeWidth(.4);
        overlay2.add(contourRoi);
        imagePlus4.setOverlay(overlay2);
        showCroppedImage(imagePlus4, x, y, width, height);
    }

    /**
     * Add test for calculating the medial line.
     *
     * @throws IOException
     * @throws InterruptedException
     */
    public void exploreSpineCalculator3() throws IOException {
        String imageFile = new File("").getAbsolutePath() + "/src/test/resources/ComponentMasks/component_3.tiff";
        Img<BitType> componentMask = testUtils.readComponentMask(imageFile);

//        componentMask = ImgView.wrap(Views.zeroMin(Views.interval(componentMask, new long[]{40, 171}, new long[]{67, 244})));
//        ImageJFunctions.show(componentMask);

        ComponentMock component = new ComponentMock(componentMask);

        LabelRegion<Integer> componentRegion = component.getRegion();
        ContourCalculator contourCalculator = new ContourCalculator(ij.op());
        Vector2DPolyline contour = contourCalculator.calculate(componentRegion);

        MedialLineCalculator medialLineCalculator = new MedialLineCalculator(ij.op(), new Imglib2Utils(ij.op()));
        Vector2DPolyline medialLine = medialLineCalculator.calculate(componentMask);

        double maxVerticalDistanceFromStartAndEnd = 3.5;
        SpineCalculator sut = new SpineCalculator(7, 7, maxVerticalDistanceFromStartAndEnd);

        Vector2DPolyline spine = sut.calculate(medialLine, contour, new ValuePair<>((int) componentMask.min(1), (int) componentMask.max(1)));

        contour.shiftMutate(new Vector2D(0.5, 0.5));
        medialLine.shiftMutate(new Vector2D(0.5, 0.5));
        spine.shiftMutate(new Vector2D(0.5, 0.5));

        List<MaskPredicate<?>> rois = Arrays.asList(
                contour.getPolygon2D(),
                medialLine.getPolyline(),
                spine.getPolyline()
        );
        testUtils.showImageWithOverlays(componentMask, rois);
//        IJ.run("Set... ", "zoom=400 x=12 y=39"); // does not work, but see here if interested to get it working: https://forum.image.sc/t/programmatically-set-display-zoom-level-in-imagej-fiji/49862
//        ij.op().
    }
}