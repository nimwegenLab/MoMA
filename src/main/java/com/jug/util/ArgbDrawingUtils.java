package com.jug.util;

import com.jug.lp.Hypothesis;
import com.jug.util.componenttree.AdvancedComponent;
import net.imglib2.Localizable;
import net.imglib2.Point;
import net.imglib2.RandomAccess;
import net.imglib2.img.Img;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.real.FloatType;

import java.util.Iterator;
import java.util.function.Function;

/**
 * @author jug
 */
public class ArgbDrawingUtils {

    /**
     * Draws the optimal segmentation (determined by the solved ILP) into {@param imgSource}
     * using the pixel values in {@param imgDestination} as source.
     *
     * @param imgDestination image to draw calculated overlay pixel values to
     * @param imgSource      pixel value source
     * @param segments       a <code>List</code> of the hypotheses containing
     *                       component-tree-nodes that represent the optimal segmentation
     *                       (the one returned by the solution to the ILP)
     */
    public static void drawSegments(final Img<ARGBType> imgDestination, final Img<ARGBType> imgSource, final long offsetX, final long offsetY, final Iterable<Hypothesis<AdvancedComponent<FloatType>>> segments) {
        final RandomAccess<ARGBType> targetImage = imgDestination.randomAccess();
        final RandomAccess<ARGBType> sourceImage = imgSource.randomAccess();
        for (final Hypothesis<AdvancedComponent<FloatType>> hypothesis : segments) {
            final AdvancedComponent<FloatType> component = hypothesis.getWrappedComponent();
            Function<Integer, ARGBType> pixelOverlayColorCalculator;
            if (hypothesis.isPruned()) {
                pixelOverlayColorCalculator = grayscaleValue -> calculateGrayPixelOverlayValue(grayscaleValue); /* highlight pruned component in gray */
            } else if (hypothesis.isForced()) {
                pixelOverlayColorCalculator = grayscaleValue -> calculateYellowPixelOverlayValue(grayscaleValue); /* highlight enforced component in yellow */
            } else if (hypothesis.isIgnored) {
                pixelOverlayColorCalculator = grayscaleValue -> calculateRedPixelOverlayValue(grayscaleValue); /* highlight enforced component in yellow */
            } else {
                pixelOverlayColorCalculator = grayscaleValue -> calculateGreenPixelOverlayValue(grayscaleValue); /* highlight optimal component in green */
            }
            drawSegmentColorOverlay(component, targetImage, sourceImage, offsetX, offsetY, pixelOverlayColorCalculator);
            if (!hypothesis.labels.isEmpty()) {
                drawLabelingMarker(component, targetImage, offsetX, offsetY);
            }
        }
    }

    /**
     * Draws {@param pixelColorCalculator} {@param pixelColorCalculator} (determined by the solved ILP) into {@param imgSource}
     * using the pixel values in {@param imgDestination} as source.
     *
     * @param imgDestination  image to draw calculated overlay pixel values to
     * @param imgSource       pixel value source
     * @param optionalSegment a <code>List</code> of the hypotheses containing
     *                        component-tree-nodes that represent the optimal segmentation
     *                        (the one returned by the solution to the ILP)
     */
    public static void drawOptionalSegmentation(final Img<ARGBType> imgDestination, final Img<ARGBType> imgSource, final long offsetX, final long offsetY, final AdvancedComponent<FloatType> optionalSegment) {
        final RandomAccess<ARGBType> raAnnotationImg = imgDestination.randomAccess();
        final RandomAccess<ARGBType> raUnaltered = imgSource.randomAccess();
        Function<Integer, ARGBType> pixelColorCalculator = grayscaleValue -> calculateBluePixelOverlayValue(grayscaleValue); /* highlight optional component in blue */
        drawSegmentColorOverlay(optionalSegment, raAnnotationImg, raUnaltered, offsetX, offsetY, pixelColorCalculator);
    }

    /**
     * Draw {@param component} to {@param ArgbImageTarget} offsetting the component
     * position by {@param offsetX} and {@param offsetX}. The color of the
     * component pixels in {@param ArgbImageTarget} is calculated by applying
     * {@param pixelColorCalculator} to the pixel values in {@param ArgbImageTarget}.
     *
     * @param component            component to draw to image
     * @param ArgbImageSource      image from which to get the pixel values from
     * @param ArgbImageTarget      image to draw the overlay pixel values to
     * @param offsetX              x-offset
     * @param offsetY              y-offset
     * @param pixelColorCalculator lambda function to calculate the ARGB value
     *                             of each component pixel based on the its previous
     *                             grayscale value
     */
    @SuppressWarnings("unchecked")
    private static void drawSegmentColorOverlay(final AdvancedComponent<FloatType> component, final RandomAccess<ARGBType> ArgbImageTarget, final RandomAccess<ARGBType> ArgbImageSource, final long offsetX, final long offsetY, Function<Integer, ARGBType> pixelColorCalculator) {
        Iterator<Localizable> componentIterator = component.iterator();
        while (componentIterator.hasNext()) {
            Localizable position = componentIterator.next();
            final int xpos = position.getIntPosition(0);
            final int ypos = position.getIntPosition(1);
            final Point p = new Point(xpos + offsetX, offsetY + ypos);
            final long[] imgPos = Util.pointLocation(p);
            ArgbImageSource.setPosition(imgPos);
            ArgbImageTarget.setPosition(imgPos);
            final int currentPixelValue = ArgbImageSource.get().get();
            ArgbImageTarget.get().set(pixelColorCalculator.apply(currentPixelValue));
        }
    }

    /**
     * Draw {@param component} to {@param ArgbImageTarget} offsetting the component
     * position by {@param offsetX} and {@param offsetX}. The color of the
     * component pixels in {@param ArgbImageTarget} is calculated by applying
     * {@param pixelColorCalculator} to the pixel values in {@param ArgbImageTarget}.
     *
     * @param component       component to draw to image
     * @param ArgbImageTarget image to draw the overlay pixel values to
     * @param offsetX         x-offset
     * @param offsetY         y-offset
     */
    @SuppressWarnings("unchecked")
    private static void drawLabelingMarker(final AdvancedComponent<FloatType> component, final RandomAccess<ARGBType> ArgbImageTarget, final long offsetX, final long offsetY) {
        long[] centerPixelPos = calculateCenterOfMass(component.iterator());

        for (long x = -2; x < 3; x++) {
            for (long y = -2; y < 3; y++) {
                long[] currentPos = new long[]{centerPixelPos[0] + x + offsetX, centerPixelPos[1] + y + offsetY};
                ArgbImageTarget.setPosition(currentPos);
                ArgbImageTarget.get().set(ARGBType.blue(255));
            }
        }
    }

    private static long[] calculateCenterOfMass(Iterator<Localizable> positionIterator) {
        long xPosSum = 0;
        long yPosSum = 0;
        long counter = 0;
        while (positionIterator.hasNext()) {
            Localizable position = positionIterator.next();
            xPosSum += position.getLongPosition(0);
            yPosSum += position.getLongPosition(1);
            counter++;
        }
        return new long[]{xPosSum / counter, yPosSum / counter};
    }

    /**
     * Calculate green ARGB pixel value from current {@param grayscaleValue}.
     *
     * @param grayscaleValue current grayscale value.
     * @return ARBG pixel value.
     */
    private static ARGBType calculateGreenPixelOverlayValue(int grayscaleValue) {
        final int redToUse = (int) (Math.min(10, (255 - ARGBType.red(grayscaleValue))) / 1.25);
        final int greenToUse = Math.min(35, (255 - ARGBType.green(grayscaleValue)));
        final int blueToUse = (int) (Math.min(10, (255 - ARGBType.blue(grayscaleValue))) / 1.25);
        return new ARGBType(ARGBType.rgba(ARGBType.red(grayscaleValue) + (redToUse), ARGBType.green(grayscaleValue) + (greenToUse), ARGBType.blue(grayscaleValue) + (blueToUse), ARGBType.alpha(grayscaleValue)));
    }

    /**
     * Calculate blue ARGB pixel value from current {@param grayscaleValue}.
     *
     * @param grayscaleValue current grayscale value.
     * @return ARBG pixel value.
     */
    private static ARGBType calculateBluePixelOverlayValue(int grayscaleValue) {
        final int redToUse = Math.min(100, (255 - ARGBType.red(grayscaleValue))) / 4;
        final int greenToUse = Math.min(100, (255 - ARGBType.green(grayscaleValue))) / 4;
        final int blueToUse = Math.min(255, (255 - ARGBType.blue(grayscaleValue)));
        return new ARGBType(ARGBType.rgba(ARGBType.red(grayscaleValue) + (redToUse), ARGBType.green(grayscaleValue) + (greenToUse), ARGBType.blue(grayscaleValue) + (blueToUse), ARGBType.alpha(grayscaleValue)));
    }

    /**
     * Calculate red ARGB pixel value from current {@param grayscaleValue}.
     *
     * @param grayscaleValue current grayscale value.
     * @return ARBG pixel value.
     */
    private static ARGBType calculateRedPixelOverlayValue(int grayscaleValue) {
        final int redToUse = Math.min(100, (255 - ARGBType.red(grayscaleValue)));
        final int greenToUse = Math.min(10, (255 - ARGBType.green(grayscaleValue))) / 4;
        final int blueToUse = Math.min(10, (255 - ARGBType.blue(grayscaleValue))) / 4;
        return new ARGBType(ARGBType.rgba(ARGBType.red(grayscaleValue) + (redToUse), ARGBType.green(grayscaleValue) + (greenToUse), ARGBType.blue(grayscaleValue) + (blueToUse), ARGBType.alpha(grayscaleValue)));
    }

    /**
     * Calculate yellow ARGB pixel value from current {@param grayscaleValue}.
     *
     * @param grayscaleValue current grayscale value.
     * @return ARBG pixel value.
     */
    private static ARGBType calculateYellowPixelOverlayValue(int grayscaleValue) {
        final int redToUse = Math.min(100, (255 - ARGBType.red(grayscaleValue)));
        final int greenToUse = (int) (Math.min(75, (255 - ARGBType.green(grayscaleValue))) / 1.25);
        final int blueToUse = Math.min(10, (255 - ARGBType.blue(grayscaleValue))) / 4;
        return new ARGBType(ARGBType.rgba(ARGBType.red(grayscaleValue) + (redToUse), ARGBType.green(grayscaleValue) + (greenToUse), ARGBType.blue(grayscaleValue) + (blueToUse), ARGBType.alpha(grayscaleValue)));
    }

    /**
     * Calculate gray ARGB pixel value from current {@param grayscaleValue}.
     *
     * @param grayscaleValue current grayscale value.
     * @return ARBG pixel value.
     */
    private static ARGBType calculateGrayPixelOverlayValue(int grayscaleValue) {
        int minHelper = 100;
        int bgHelper = 175;
        final int redToUse = (Math.min(minHelper, (bgHelper - ARGBType.red(grayscaleValue))));
        final int greenToUse = (Math.min(minHelper, (bgHelper - ARGBType.green(grayscaleValue))));
        final int blueToUse = (Math.min(minHelper, (bgHelper - ARGBType.blue(grayscaleValue))));
        return new ARGBType(ARGBType.rgba(ARGBType.red(grayscaleValue) + (redToUse), ARGBType.green(grayscaleValue) + (greenToUse), ARGBType.blue(grayscaleValue) + (blueToUse), ARGBType.alpha(grayscaleValue)));
    }

}
