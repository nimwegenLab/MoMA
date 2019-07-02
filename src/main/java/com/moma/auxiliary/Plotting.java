package com.moma.auxiliary;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.Plot;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.Views;
import org.scijava.Context;
import org.scijava.ui.UIService;

public class Plotting {

	public static void surfacePlot(final RandomAccessibleInterval<FloatType> img, final int dimension, final long position){
		ImagePlus imp = ImageJFunctions.wrap(Views.hyperSlice(img, dimension, position), "my image");
		IJ.run(imp, "3D Surface Plot", "");
	}

	static public void plotArray(float[] y) {
		plotArray(y, null, null, null);
	}
	
	static public void plotArray(float[] y, String title, String xLabel, String yLabel) {
		int[] x_vals = java.util.stream.IntStream.rangeClosed(0, y.length).toArray();
		float[] xvals_new = new float[x_vals.length];
		for(int i=0; i<x_vals.length; i++) {
		    xvals_new[i] = x_vals[i];
		}

		if(title == null)
			title = "A plot";
		if(xLabel == null)
			xLabel = "labels on the x-axis";
		if(yLabel == null)
			yLabel = "labels on the y-axis";

		Plot plot = new Plot(title, xLabel, yLabel,xvals_new, y);
		plot.show();
	}


// THIS CURRENTLY DOES NOT WORK, BECAUSE GETTING THE CONTEXT FAILS WITH THIS EXCEPTION: java.lang.IllegalArgumentException: Mismatched context: result
//	public static void showImage(Img<FloatType> img){
//		uiService().show(img);
//	}
//
//	private static UIService uiService(){
//		Context context = new Context();
//		return context.service(UIService.class);
//	}
}
