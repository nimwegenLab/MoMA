package com.jug.util;

import com.jug.lp.Hypothesis;
import net.imglib2.Localizable;
import net.imglib2.Point;
import net.imglib2.RandomAccess;
import net.imglib2.algorithm.componenttree.Component;
import net.imglib2.img.Img;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.IntervalView;

import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

/**
 * @author jug
 */
public class ArgbDrawingUtils {

	/**
	 * Draws the optimal segmentation (determined by the solved ILP) into the
	 * given <code>Img</code>.
	 *
	 * @param img                 the Img to draw into.
	 * @param view                the active view on that Img (in order to know the pixel
	 *                            offsets)
	 * @param optimalSegmentation a <code>List</code> of the hypotheses containing
	 *                            component-tree-nodes that represent the optimal segmentation
	 *                            (the one returned by the solution to the ILP).
	 */
	public static void drawOptimalSegmentation(final Img<ARGBType> img, final long offsetX, final long offsetY, final List<Hypothesis<Component<FloatType, ?>>> optimalSegmentation) {
		final RandomAccess<ARGBType> raArgbImg = img.randomAccess();
		for (final Hypothesis<Component<FloatType, ?>> hyp : optimalSegmentation) {
			final Component<FloatType, ?> ctn = hyp.getWrappedComponent();
			Function<Integer, ARGBType> pixelOverlayColorCalculator;
			if (hyp.isPruned()) {
				pixelOverlayColorCalculator = grayscaleValue -> calculateGrayPixelOverlayValue(grayscaleValue); /* highlight pruned component in gray */
			} else if (hyp.getSegmentSpecificConstraint() != null) {
				pixelOverlayColorCalculator = grayscaleValue -> calculateYellowPixelOverlayValue(grayscaleValue); /* highlight enforced component in yellow */
			} else {
				pixelOverlayColorCalculator = grayscaleValue -> calculateGreenPixelOverlayValue(grayscaleValue); /* highlight optimal component in green */
			}
			drawSegmentColorOverlay( ctn, raArgbImg, offsetX, offsetY, pixelOverlayColorCalculator);
		}
	}

	public static void drawOptionalSegmentation(final Img<ARGBType> img, final long offsetX, final long offsetY, final Component<FloatType, ?> optionalSegmentation) {
		final RandomAccess<ARGBType> raAnnotationImg = img.randomAccess();
		Function<Integer, ARGBType> redPixelOverlayCalculator = grayscaleValue -> calculateRedPixelOverlayValue(grayscaleValue); /* highlight optional component in red */
		drawSegmentColorOverlay( optionalSegmentation, raAnnotationImg, offsetX, offsetY, redPixelOverlayCalculator );
	}

	/**
	 * Draw {@param component} to gray-scale {@param ArgbImage} offsetting its
	 * position by {@param offsetX} and {@param offsetX}. The color of the
	 * component pixels in the image is calculated using
	 * the {@param pixelColorCalculator}.
	 *
	 * @param component component to draw to image
	 * @param ArgbImage image to draw to
	 * @param offsetX x-offset
	 * @param offsetY y-offset
	 * @param pixelColorCalculator lambda function to calculate the ARGB value
	 *                             of each component pixel based on the its previous
	 *                             grayscale value
	 */
	@SuppressWarnings( "unchecked" )
	private static void drawSegmentColorOverlay(final Component< FloatType, ? > component, final RandomAccess< ARGBType > ArgbImage, final long offsetX, final long offsetY, Function<Integer, ARGBType> pixelColorCalculator ) {
		Iterator< Localizable > componentIterator = component.iterator();
		while (componentIterator.hasNext()) {
			Localizable position = componentIterator.next();
			final int xpos = position.getIntPosition(0);
			final int ypos = position.getIntPosition(1);
			final Point p = new Point(xpos + offsetX, offsetY + ypos);
			final long[] imgPos = Util.pointLocation(p);
			ArgbImage.setPosition(imgPos);
			final int curCol = ArgbImage.get().get();
			ArgbImage.get().set(pixelColorCalculator.apply(curCol));
		}
	}

	/**
	 * Calculate green ARGB pixel value from current {@param grayscaleValue}.
	 *
	 * @param grayscaleValue current grayscale value.
	 * @return ARBG pixel value.
	 */
	private static ARGBType calculateGreenPixelOverlayValue(int grayscaleValue){
		final int redToUse = (int) (Math.min(10, (255 - ARGBType.red(grayscaleValue))) / 1.25);
		final int greenToUse = Math.min(35, (255 - ARGBType.green(grayscaleValue)));
		final int blueToUse = (int) (Math.min(10, (255 - ARGBType.blue(grayscaleValue))) / 1.25);
		return new ARGBType(ARGBType.rgba(ARGBType.red(grayscaleValue) + (redToUse), ARGBType.green(grayscaleValue) + (greenToUse), ARGBType.blue(grayscaleValue) + (blueToUse), ARGBType.alpha(grayscaleValue)));
	}

	/**
	 * Calculate red ARGB pixel value from current {@param grayscaleValue}.
	 *
	 * @param grayscaleValue current grayscale value.
	 * @return ARBG pixel value.
	 */
	private static ARGBType calculateRedPixelOverlayValue(int grayscaleValue){
		final int redToUse = Math.min(100, (255 - ARGBType.red(grayscaleValue)));
		final int greenToUse = Math.min( 10, ( 255 - ARGBType.green( grayscaleValue ) ) ) / 4;
		final int blueToUse = Math.min( 10, ( 255 - ARGBType.blue( grayscaleValue ) ) ) / 4;
		return new ARGBType(ARGBType.rgba(ARGBType.red(grayscaleValue) + (redToUse), ARGBType.green(grayscaleValue) + (greenToUse), ARGBType.blue(grayscaleValue) + (blueToUse), ARGBType.alpha(grayscaleValue)));
	}

	/**
	 * Calculate yellow ARGB pixel value from current {@param grayscaleValue}.
	 *
	 * @param grayscaleValue current grayscale value.
	 * @return ARBG pixel value.
	 */
	private static ARGBType calculateYellowPixelOverlayValue(int grayscaleValue){
		final int redToUse = Math.min(100, (255 - ARGBType.red(grayscaleValue)));
		final int greenToUse = ( int ) ( Math.min( 75, ( 255 - ARGBType.green( grayscaleValue ) ) ) / 1.25 );
		final int blueToUse = Math.min( 10, ( 255 - ARGBType.blue( grayscaleValue ) ) ) / 4;
		return new ARGBType(ARGBType.rgba(ARGBType.red(grayscaleValue) + (redToUse), ARGBType.green(grayscaleValue) + (greenToUse), ARGBType.blue(grayscaleValue) + (blueToUse), ARGBType.alpha(grayscaleValue)));
	}

	/**
	 * Calculate gray ARGB pixel value from current {@param grayscaleValue}.
	 *
	 * @param grayscaleValue current grayscale value.
	 * @return ARBG pixel value.
	 */
	private static ARGBType calculateGrayPixelOverlayValue(int grayscaleValue){
		int minHelper = 100;
		int bgHelper = 175;
		final int redToUse = ( Math.min( minHelper, ( bgHelper - ARGBType.red( grayscaleValue ) ) ) );
		final int greenToUse = ( Math.min( minHelper, ( bgHelper - ARGBType.green( grayscaleValue ) ) ) );
		final int blueToUse = ( Math.min( minHelper, ( bgHelper - ARGBType.blue( grayscaleValue ) ) ) );
		return new ARGBType(ARGBType.rgba(ARGBType.red(grayscaleValue) + (redToUse), ARGBType.green(grayscaleValue) + (greenToUse), ARGBType.blue(grayscaleValue) + (blueToUse), ARGBType.alpha(grayscaleValue)));
	}

}
