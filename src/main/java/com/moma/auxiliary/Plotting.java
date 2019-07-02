package com.moma.auxiliary;

import com.jug.util.ComponentTreeUtils;
import ij.IJ;
import ij.ImagePlus;
import ij.gui.Plot;
import net.imglib2.Localizable;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.componenttree.Component;
import net.imglib2.algorithm.componenttree.ComponentForest;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.Views;

import java.util.ArrayList;
import java.util.Iterator;

public class Plotting {

	public static < C extends Component< FloatType, C >> void drawComponentTree(ComponentForest< C > ct){
		int i = 0;
		for ( final C root : ct.roots() ) {
			int componentLevel = 0;
			ArrayList< C > componentList = new ArrayList< C >();
			componentList.add( root );
			while ( componentList.size() > 0 ) {
				for ( final Component< ?, ? > ctn : componentList ) {
					drawComponent( ctn, i, componentLevel );
					i++;
				}
				componentList = ComponentTreeUtils.getAllChildren( componentList );
				System.out.println("componentList.size(): "+componentList.size());
				componentLevel++;
			}
		}
	}

	private static void drawComponent( final Component< ?, ? > ctn, final int index, final int level ) {
		int xMin = Integer.MAX_VALUE;
		int xMax = Integer.MIN_VALUE;
		int yMin = Integer.MAX_VALUE;
		int yMax = Integer.MIN_VALUE;

		Iterator<Localizable> componentIterator = ctn.iterator();
		while(componentIterator.hasNext()){
			final int xPos = componentIterator.next().getIntPosition( 0 );
			xMin = Math.min( xMin, xPos );
			xMax = Math.max( xMax, xPos );
			final int yPos = componentIterator.next().getIntPosition( 1 );
			yMin = Math.min( yMin, yPos );
			yMax = Math.max( yMax, yPos );
		}
		System.out.println("Component "+index+":");
		System.out.println("\tlevel: "+level);
		System.out.println("\txSize: "+xMin+", "+xMax);
		System.out.println("\tySize: "+yMin+", "+yMax);
	}

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
}
