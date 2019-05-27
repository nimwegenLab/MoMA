package com.moma.auxiliary;
import ij.gui.Plot;

public class Plotting {
	static public void PlotArray(float[] y) {
		PlotArray(y, null, null, null);
	}
	
	static public void PlotArray(float[] y, String title, String xLabel, String yLabel) {
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
}
