/**
 *
 */
package com.jug.segmentation;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.Views;

/**
 * @author jug
 */
public class GrowthLineSegmentationMagic {

	private static SilentWekaSegmenter< FloatType > classifier;

	public static void setClassifier( final String folder, final String file ) {
		classifier = new SilentWekaSegmenter<>(folder, file);
	}

	public static void setClassifier( final SilentWekaSegmenter< FloatType > newClassifier ) {
		classifier = newClassifier;
	}

	public static SilentWekaSegmenter< FloatType > getClassifier() {
		return classifier;
	}

	public static RandomAccessibleInterval< FloatType > returnClassification( final RandomAccessibleInterval< FloatType > rai ) {
		final RandomAccessibleInterval< FloatType > classified = classifier.classifyPixels( rai, true );

		final long[] min = new long[ classified.numDimensions() ];
		classified.min( min );
		final long[] max = new long[ classified.numDimensions() ];
		classified.max( max );
		// TODO: FIXES A BUG IN THE IMGLIB... NEEDS TO BE REMOVED AFTER THE BUG IS REMOVED!!!
		if ( ( max[ 2 ] - min[ 2 ] + 1 ) % 2 == 1 ) {
			max[ 2 ]++;
		}

		return Views.subsample( Views.interval( classified, min, max ), 1, 1, 2 );
	}
}
