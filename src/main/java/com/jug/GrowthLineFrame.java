package com.jug;

import com.jug.util.componenttree.*;
import com.moma.auxiliary.Plotting;
import net.imglib2.Cursor;
import net.imglib2.IterableInterval;
import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.componenttree.ComponentForest;
import net.imglib2.algorithm.componenttree.mser.MserTree;
import net.imglib2.type.numeric.real.FloatType;

import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;

import java.util.ArrayList;
import java.util.function.Predicate;

import static com.jug.MoMA.GL_OFFSET_BOTTOM;
import static com.jug.MoMA.GL_OFFSET_TOP;

/**
 * @author jug
 *         Represents one growth line (well) in which Bacteria can grow, at one
 *         instance in time.
 *         This corresponds to one growth line micrograph. The class
 *         representing an entire time
 *         series (2d+t) representation of an growth line is
 *         <code>GrowthLine</code>.
 */
public class GrowthLineFrame extends AbstractGrowthLineFrame< SimpleComponent< FloatType > > {

    /**
	 * @see com.jug.AbstractGrowthLineFrame#buildIntensityTree(net.imglib2.RandomAccessibleInterval)
	 */
	@Override
	protected ComponentForest< SimpleComponent< FloatType >> buildIntensityTree( final RandomAccessibleInterval< FloatType > raiFkt ) {
		float threshold = 0.1f;
		IterableInterval< FloatType > iterableSource = Views.iterable(raiFkt);
		Cursor<FloatType> cursor = iterableSource.cursor();
		while(cursor.hasNext()){
			cursor.next();
			float val = cursor.get().getRealFloat();
			if(val > 0.0f && val < threshold){
				cursor.get().set(0);
			}
		}

//		final double delta = 0.0001;
        final double delta = 0.02;
        final int minSize = 50; // minSize=50px seems safe, assuming pixel-area of a round cell with radius of have the bacterial width: 3.141*0.35**2/0.065**2, where pixelSize=0.065mu and width/2=0.35mu
        final long maxSize = Long.MAX_VALUE;
        final double maxVar = 1.0;
        final double minDiversity = 0.2;
        final boolean darkToBright = false;
        MserTree<FloatType> componentTree = MserTree.buildMserTree(raiFkt, delta, minSize, maxSize, maxVar, minDiversity, darkToBright);

//        Plotting.drawComponentTree2(componentTree, new ArrayList<>());

		Predicate<Integer> widthCondition = (width) -> (width <= 20);
		ILocationTester ctester = new ComponentExtentTester(0, widthCondition);
		Predicate<Integer> condition = (pos) -> (pos >= GL_OFFSET_TOP && pos <= raiFkt.dimension(1) - GL_OFFSET_BOTTOM);
		ILocationTester boundaryTester = new PixelPositionTester(1, condition);
		ArrayList<ILocationTester> testers = new ArrayList<>();
		testers.add(ctester);
		testers.add(boundaryTester);
		ComponentTester<FloatType, SimpleComponent<FloatType>> tester = new ComponentTester<>(testers);

//		IntervalView<FloatType> currentImage = Views.hyperSlice(img, 2, frameIndex);

		return new SimpleComponentTree(componentTree, raiFkt, tester);
//		return new SimpleComponentTree(componentTree, raiFkt);

//		return MserTree.buildMserTree( raiFkt, MotherMachine.MIN_GAP_CONTRAST / 2.0, MotherMachine.MIN_CELL_LENGTH, Long.MAX_VALUE, 0.5, 0.33, true );
	}
}
