package com.jug.util.componenttree;

import com.jug.lp.costs.ComponentMock;
import com.jug.util.TestUtils;
import com.jug.util.converter.RealFloatToBitTypeConverter;
import com.jug.util.imglib2.Imglib2Utils;
import com.jug.util.math.Vector2DPolyline;
import net.imagej.ImageJ;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.binary.Thresholder;
import net.imglib2.converter.Converters;
import net.imglib2.img.Img;
import net.imglib2.img.ImgView;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.roi.MaskPredicate;
import net.imglib2.roi.labeling.LabelRegion;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.ValuePair;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class MedialLineCalculatorTest {
    private final ImageJ ij;
    private final TestUtils testUtils;

    public MedialLineCalculatorTest() {
        ij = new ImageJ();
        testUtils = new TestUtils(ij);
    }

    public static void main(String... args) throws IOException {
        ImageJ ij = new ImageJ();
        ij.ui().showUI();
//        new MedialLineCalculatorTest().exploreMedialLineCalculator();
//        new MedialLineCalculatorTest().exploreSpineCalculator();
        new MedialLineCalculatorTest().exploreSpineCalculator2();
    }

    /**
     * Add test for calculating the medial line.
     *
     * @throws IOException
     * @throws InterruptedException
     */
    public void exploreMedialLineCalculator() throws IOException {
        String imageFile = new File("").getAbsolutePath() + "/src/test/resources/00_probability_maps/20201119_VNG1040_AB2h_2h_1_MMStack_Pos6_GL6/frame90_repeated__cropped__20201119_VNG1040_AB2h_2h_1_MMStack_Pos6_GL6__model_9e5727e4ed18802f4ab04c7494ef8992d798f4d64d5fd75e285b9a3d83b13ac9.tif";

        int componentIndex = 4;

        ValuePair<AdvancedComponent<FloatType>, RandomAccessibleInterval<BitType>> componentAndImage = testUtils.getComponentWithImage(imageFile,
                componentIndex,
                new BitType(true));
        RandomAccessibleInterval<BitType> image = componentAndImage.getB();

        MedialLineCalculator sut = new MedialLineCalculator(ij.op(), new Imglib2Utils(ij.op()));
        Vector2DPolyline res = sut.calculate(image);

        List<MaskPredicate<?>> rois = Arrays.asList(
                res.getPolyline()
        );
        testUtils.showImageWithOverlays(image, rois);
    }

    /**
     * Add test for calculating the medial line.
     *
     * @throws IOException
     * @throws InterruptedException
     */
    public void exploreSpineCalculator() throws IOException {
        String imageFile = new File("").getAbsolutePath() + "/src/test/resources/00_probability_maps/20201119_VNG1040_AB2h_2h_1_MMStack_Pos6_GL6/frame90_repeated__cropped__20201119_VNG1040_AB2h_2h_1_MMStack_Pos6_GL6__model_9e5727e4ed18802f4ab04c7494ef8992d798f4d64d5fd75e285b9a3d83b13ac9.tif";

        int componentIndex = 1;

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
        SpineCalculator sut = new SpineCalculator();

        Vector2DPolyline spine = sut.calculate(medialLine, contour, 7, 7, new ValuePair<>((int) image.min(1), (int) image.max(1)));

        List<MaskPredicate<?>> rois = Arrays.asList(
                contour.getPolygon2D(),
//                medialLine.getPolyline()
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
    public void exploreSpineCalculator2() throws IOException {
//        String imageFile = new File("").getAbsolutePath() + "/src/test/resources/00_probability_maps/20201119_VNG1040_AB2h_2h_1_MMStack_Pos6_GL6/frame90_repeated__cropped__20201119_VNG1040_AB2h_2h_1_MMStack_Pos6_GL6__model_9e5727e4ed18802f4ab04c7494ef8992d798f4d64d5fd75e285b9a3d83b13ac9.tif";
        String imageFile = new File("").getAbsolutePath() + "/src/test/resources/ComponentMasks/component_2.tiff";
        Img<UnsignedByteType> input = (Img) ij.io().open(imageFile);
//        Img input = ij.io().open(imageFile);

//        RealFloatToBitTypeConverter converter = new RealFloatToBitTypeConverter<FloatType>(.5F);
//        Img<BitType> image = converter.convert(input);
//        Converters.convert(input, new RealFloatToBitTypeConverter<FloatType>(.5F));
//        double threshold = .5;
//        RandomAccessibleInterval<BitType> componentMask = Converters.convert((RandomAccessibleInterval<FloatType>) input, (i, o) -> o.set(i.getRealDouble() > threshold), new BitType());

        Img<BitType> componentMask = Thresholder.threshold(input, new UnsignedByteType(128), true, 1);
        ImageJFunctions.show(componentMask);

//        Img<BitType> image = ij.op().convert().bit(input);
        ComponentMock component = new ComponentMock(componentMask);
//        int componentIndex = 1;

//        ValuePair<AdvancedComponent<FloatType>, RandomAccessibleInterval<BitType>> componentAndImage = testUtils.getComponentWithImage(imageFile,
//                componentIndex,
//                new BitType(true));
//        AdvancedComponent<FloatType> component = componentAndImage.getA();
//        RandomAccessibleInterval<BitType> image = component.getComponentImage(new BitType(true));

        LabelRegion<Integer> componentRegion = component.getRegion();
        ContourCalculator contourCalculator = new ContourCalculator(ij.op());
        Vector2DPolyline contour = contourCalculator.calculate(componentRegion);

        MedialLineCalculator medialLineCalculator = new MedialLineCalculator(ij.op(), new Imglib2Utils(ij.op()));
        Vector2DPolyline medialLine = medialLineCalculator.calculate(componentMask);
//        LabelRegion<Integer> componentRegion = componentAndImage.getA().getRegion();
        SpineCalculator sut = new SpineCalculator();

        Vector2DPolyline spine = sut.calculate(medialLine, contour, 7, 7, new ValuePair<>((int) componentMask.min(1), (int) componentMask.max(1)));

        List<MaskPredicate<?>> rois = Arrays.asList(
                contour.getPolygon2D(),
//                medialLine.getPolyline()
                spine.getPolyline()
        );
        testUtils.showImageWithOverlays(componentMask, rois);
    }
}