package com.jug;

import com.jug.util.componenttree.*;
import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.componenttree.ComponentForest;
import net.imglib2.type.numeric.real.FloatType;

import com.jug.util.filteredcomponents.FilteredComponent;
import com.jug.util.filteredcomponents.FilteredComponentTree;
import com.jug.util.filteredcomponents.FilteredComponentTree.Filter;
import com.jug.util.filteredcomponents.FilteredComponentTree.MaxGrowthPerStep;
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
public class GrowthLineFrame extends AbstractGrowthLineFrame< FilteredComponent< FloatType > > {

	private final Filter noFilterFilter = new MaxGrowthPerStep( 1000 );

    /**
	 * @see com.jug.AbstractGrowthLineFrame#buildIntensityTree(net.imglib2.RandomAccessibleInterval)
	 */
	@Override
	protected ComponentForest< FilteredComponent< FloatType >> buildIntensityTree( final RandomAccessibleInterval< FloatType > raiFkt ) {
		FilteredComponentTree<FloatType> componentTree = FilteredComponentTree.buildComponentTree(
				raiFkt,
				new FloatType(),
				200, // MoMA.MIN_CELL_LENGTH,
				Long.MAX_VALUE, //2000, // Long.MAX_VALUE,
				2,
				12,
				noFilterFilter, //maxGrowthPerStepRatioWithMinimalAbsoluteIncrease,
				false); // DarkToBright=true

		Predicate<Integer> widthCondition = (width) -> (width <= 20);
		ILocationTester ctester = new ComponentExtentTester(0, widthCondition);
		Predicate<Integer> condition = (pos) -> (pos >= GL_OFFSET_TOP && pos <= raiFkt.dimension(1) - GL_OFFSET_BOTTOM);
		ILocationTester boundaryTester = new PixelPositionTester(1, condition);
		ArrayList<ILocationTester> testers = new ArrayList<>();
		testers.add(ctester);
		testers.add(boundaryTester);
		ComponentTester<FloatType, FilteredComponent<FloatType>> tester = new ComponentTester<>(testers);

//		IntervalView<FloatType> currentImage = Views.hyperSlice(img, 2, frameIndex);

		return new SimpleComponentTree(componentTree, tester, raiFkt);

//		return MserTree.buildMserTree( raiFkt, MotherMachine.MIN_GAP_CONTRAST / 2.0, MotherMachine.MIN_CELL_LENGTH, Long.MAX_VALUE, 0.5, 0.33, true );
	}

	/*
	  @see com.jug.AbstractGrowthLineFrame#buildParaMaxFlowSumTree(net.imglib2.RandomAccessibleInterval)
	 */
//	@Override
//	protected ComponentForest< FilteredComponent< FloatType >> buildParaMaxFlowSumTree( final RandomAccessibleInterval< FloatType > raiFkt ) {
//		return FilteredComponentTree.buildComponentTree(
//				raiFkt,
//				new FloatType(),
//				3,
//				Long.MAX_VALUE,
//				noFilterFilter, //maxGrowthPerStepRatioWithMinimalAbsoluteIncrease,
//				true );
//	}

}
